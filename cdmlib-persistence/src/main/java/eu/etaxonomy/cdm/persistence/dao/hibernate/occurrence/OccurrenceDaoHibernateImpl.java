/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Tuple;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.query.Query;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.api.filter.TaxonOccurrenceRelationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.IHomotypicalGroupDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @since 01.09.2008
 */
@Repository
public class OccurrenceDaoHibernateImpl
          extends IdentifiableDaoBase<SpecimenOrObservationBase>
          implements IOccurrenceDao {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private IDescriptionDao descriptionDao;

    @Autowired
    private ITaxonNameDao taxonNameDao;

    @Autowired
    private IHomotypicalGroupDao homotypicalGroupDao;

    public OccurrenceDaoHibernateImpl() {
        super(SpecimenOrObservationBase.class);
        indexedClasses = new Class[7];
        indexedClasses[0] = FieldUnit.class;
        indexedClasses[1] = DerivedUnit.class;
        indexedClasses[5] = DnaSample.class;
    }

    @Override
    public long countDerivationEvents(SpecimenOrObservationBase occurence) {
        checkNotInPriorView("OccurrenceDaoHibernateImpl.countDerivationEvents(SpecimenOrObservationBase occurence)");
        Query<Long> query = getSession().createQuery("select count(distinct derivationEvent) from DerivationEvent derivationEvent join derivationEvent.originals occurence where occurence = :occurence", Long.class);
        query.setParameter("occurence", occurence);

        return query.uniqueResult();
    }

    @Override
    public long countDeterminations(SpecimenOrObservationBase occurrence, TaxonBase taxonBase) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getCriteria(DeterminationEvent.class);
            if(occurrence != null) {
                criteria.add(Restrictions.eq("identifiedUnit",occurrence));
            }

            if(taxonBase != null) {
                criteria.add(Restrictions.eq("taxon",taxonBase));
            }

            criteria.setProjection(Projections.rowCount());
            return (Long)criteria.uniqueResult();
        } else {
            AuditQuery query = makeAuditQuery(DeterminationEvent.class,auditEvent);

            if(occurrence != null) {
                query.add(AuditEntity.relatedId("identifiedUnit").eq(occurrence.getId()));
            }

            if(taxonBase != null) {
                query.add(AuditEntity.relatedId("taxon").eq(taxonBase.getId()));
            }
            query.addProjection(AuditEntity.id().count());

            return (Long)query.getSingleResult();
        }
    }

    @Override
    public long countMedia(SpecimenOrObservationBase occurence) {
        return this.getMediaIds(occurence).size();
    }

    @Override
    public List<Media> getMedia(SpecimenOrObservationBase occurence,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        checkNotInPriorView("OccurrenceDaoHibernateImpl.getMedia(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths)");
        List<Integer> ids = this.getMediaIds(occurence);
        Query<Media> query = getSession().createQuery(
                "   SELECT m "
                + " FROM Media m "
                + " WHERE m.id in (:mediaIds)", Media.class);
        query.setParameterList("mediaIds", ids);

        addPageSizeAndNumber(query, pageSize, pageNumber);

        List<Media> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    private List<Integer> getMediaIds(SpecimenOrObservationBase occurence) {
        Query query = getSession().createQuery(
                "   SELECT DISTINCT m.id "
                + " FROM SpecimenOrObservationBase occ JOIN occ.descriptions d "
                + " JOIN d.descriptionElements el JOIN el.media m "
                + " WHERE occ = :occurence AND d.imageGallery = true "
                + " ORDER BY m.id ");
        query.setParameter("occurence", occurence);
        @SuppressWarnings("unchecked")
        List<Integer> list = query.list();

        if (occurence.isInstanceOf(MediaSpecimen.class)){
            String q2Str = " SELECT DISTINCT m.id "
                    + " FROM MediaSpecimen spec "
                    + " JOIN spec.mediaSpecimen m "
                    + " WHERE spec = :occurence ";
            Query<Integer> q2 = getSession().createQuery(q2Str, Integer.class);
            q2.setParameter("occurence", occurence);
            List<Integer> list2 = q2.list();
            list.addAll(list2);
            Set<Integer> dedupSet = new HashSet<>(list);
            list = new ArrayList<>(dedupSet);
            Collections.sort(list);
        }

        return list;
    }

    @Override
    public List<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize,Integer pageNumber, List<String> propertyPaths) {
        checkNotInPriorView("OccurrenceDaoHibernateImpl.getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize,Integer pageNumber)");
        Query<DerivationEvent> query = getSession().createQuery("SELECT DISTINCT derivationEvent FROM DerivationEvent derivationEvent JOIN derivationEvent.originals occurence WHERE occurence = :occurence", DerivationEvent.class);
        query.setParameter("occurence", occurence);

        addPageSizeAndNumber(query, pageSize, pageNumber);

        List<DerivationEvent> result = query.list();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

    @Override
    public List<DeterminationEvent> getDeterminations(SpecimenOrObservationBase occurrence,
            TaxonBase taxonBase, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(DeterminationEvent.class);
            if(occurrence != null) {
                criteria.add(Restrictions.eq("identifiedUnit",occurrence));
            }

            if(taxonBase != null) {
                criteria.add(Restrictions.eq("taxon",taxonBase));
            }

            addPageSizeAndNumber(criteria, pageSize, pageNumber);

            @SuppressWarnings("unchecked")
            List<DeterminationEvent> result = criteria.list();
            defaultBeanInitializer.initializeAll(result, propertyPaths);
            return result;
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(DeterminationEvent.class,auditEvent.getRevisionNumber());
            if(occurrence != null) {
                query.add(AuditEntity.relatedId("identifiedUnit").eq(occurrence.getId()));
            }

            if(taxonBase != null) {
                query.add(AuditEntity.relatedId("taxon").eq(taxonBase.getId()));
            }
            addPageSizeAndNumber(query, pageSize, pageNumber);

            @SuppressWarnings("unchecked")
            List<DeterminationEvent> result = query.getResultList();
            defaultBeanInitializer.initializeAll(result, propertyPaths);
            return result;
        }
    }

    @Override
    public void rebuildIndex() {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());

        for(SpecimenOrObservationBase<?> occurrence : list(null,null)) { // re-index all taxon base

            for(DeterminationEvent determination : occurrence.getDeterminations()) {
                Hibernate.initialize(determination.getActor());
                Hibernate.initialize(determination.getTaxon());
            }
            Hibernate.initialize(occurrence.getDefinition());
            if(occurrence instanceof DerivedUnit) {
                DerivedUnit derivedUnit = (DerivedUnit) occurrence;
                Hibernate.initialize(derivedUnit.getCollection());
                if(derivedUnit.getCollection() != null) {
                    Hibernate.initialize(derivedUnit.getCollection().getSuperCollection());
                    Hibernate.initialize(derivedUnit.getCollection().getInstitute());
                }
                Hibernate.initialize(derivedUnit.getStoredUnder());
                SpecimenOrObservationBase<?> original = derivedUnit.getOriginalUnit();
                if(original != null && original.isInstanceOf(FieldUnit.class)) {
                    FieldUnit fieldUnit = CdmBase.deproxy(original, FieldUnit.class);
                    Hibernate.initialize(fieldUnit.getGatheringEvent());
                    if(fieldUnit.getGatheringEvent() != null) {
                        Hibernate.initialize(fieldUnit.getGatheringEvent().getActor());
                    }
                }
            }
            fullTextSession.index(occurrence);
        }
        fullTextSession.flushToIndexes();
    }

    @Override
    public long count(Class<? extends SpecimenOrObservationBase> clazz,	TaxonName determinedAs) {

        Criteria criteria = getCriteria(clazz);

        criteria.createCriteria("determinations").add(Restrictions.eq("taxonName", determinedAs));
        criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
        return (Long)criteria.uniqueResult();
    }

    @Override
    public List<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> clazz, TaxonName determinedAs,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Criteria criteria = getCriteria(clazz);

        criteria.createCriteria("determinations").add(Restrictions.eq("taxonName", determinedAs));

        addPageSizeAndNumber(criteria, pageSize, pageNumber);
        addOrder(criteria,orderHints);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<SpecimenOrObservationBase> results = criteria.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public long count(Class<? extends SpecimenOrObservationBase> clazz,	TaxonBase determinedAs) {

        Criteria criteria = getCriteria(clazz);

        criteria.createCriteria("determinations").add(Restrictions.eq("taxon", determinedAs));
        criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
        return (Long)criteria.uniqueResult();
    }


    @Override
    public List<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> clazz, TaxonBase determinedAs,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

        Criteria criteria = getCriteria(clazz);

        criteria.createCriteria("determinations").add(Restrictions.eq("taxon", determinedAs));

        addPageSizeAndNumber(criteria, pageSize, pageNumber);
        addOrder(criteria,orderHints);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<SpecimenOrObservationBase> results = criteria.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public <T extends SpecimenOrObservationBase> List<UuidAndTitleCache<SpecimenOrObservationBase>> findOccurrencesUuidAndTitleCache(
            Class<T> clazz, String queryString, String significantIdentifier, SpecimenOrObservationType recordBasis,
            Taxon associatedTaxon, TaxonName associatedTaxonName, MatchMode matchmode, boolean includeUnpublished,
            EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Integer limit, Integer start, List<OrderHint> orderHints) {

        Criteria criteria = createFindOccurrenceCriteria(clazz, queryString, significantIdentifier, recordBasis,
                associatedTaxon, associatedTaxonName, matchmode, includeUnpublished,
                taxonOccurrenceRelTypes,
                limit, start, orderHints, null);
        if(criteria!=null){
            ProjectionList projectionList = Projections.projectionList();
            projectionList.add(Projections.property("uuid"));
            projectionList.add(Projections.property("id"));
            projectionList.add(Projections.property("titleCache"));
            criteria.setProjection(projectionList);

            @SuppressWarnings("unchecked")
            List<Object[]> result = criteria.list();
            List<UuidAndTitleCache<SpecimenOrObservationBase>> uuidAndTitleCacheList = new ArrayList<>();
            for(Object[] object : result){
                uuidAndTitleCacheList.add(new UuidAndTitleCache<>((UUID) object[0],(Integer) object[1], (String) object[2]));
            }
            return uuidAndTitleCacheList;
        }else{
            return Collections.emptyList();
        }
    }

    @Override
    public <T extends SpecimenOrObservationBase> List<T> findOccurrences(Class<T> clazz, String queryString,
            String significantIdentifier, SpecimenOrObservationType recordBasis, Taxon associatedTaxon, TaxonName associatedTaxonName,
            MatchMode matchmode, boolean includeUnpublished, EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

        Criteria criteria = createFindOccurrenceCriteria(clazz, queryString, significantIdentifier, recordBasis,
                associatedTaxon, associatedTaxonName, matchmode, includeUnpublished,
                taxonOccurrenceRelTypes,
                limit, start, orderHints, propertyPaths);
        if(criteria!=null){
            @SuppressWarnings("unchecked")
            List<T> results = criteria.list();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        }else{
            return Collections.emptyList();
        }
    }

    private <T extends SpecimenOrObservationBase> Criteria createFindOccurrenceCriteria(Class<T> clazz, String queryString,
            String significantIdentifier, SpecimenOrObservationType recordBasis, Taxon associatedTaxon,
            TaxonName associatedTaxonName, MatchMode matchmode, boolean includeUnpublished,
            EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

        Criteria criteria = null;

        if(clazz == null) {
            criteria = getSession().createCriteria(type);
        } else {
            criteria = getSession().createCriteria(clazz);
        }

        //queryString
        if (queryString != null) {
            if(matchmode == null) {
                matchmode = MatchMode.ANYWHERE;
                criteria.add(Restrictions.ilike("titleCache", matchmode.queryStringFrom(queryString)));
            } else if(matchmode == MatchMode.BEGINNING) {
                criteria.add(Restrictions.ilike("titleCache", matchmode.queryStringFrom(queryString), org.hibernate.criterion.MatchMode.START));
            } else if(matchmode == MatchMode.END) {
                criteria.add(Restrictions.ilike("titleCache", matchmode.queryStringFrom(queryString), org.hibernate.criterion.MatchMode.END));
            } else if(matchmode == MatchMode.EXACT) {
                criteria.add(Restrictions.ilike("titleCache", matchmode.queryStringFrom(queryString), org.hibernate.criterion.MatchMode.EXACT));
            } else {
                criteria.add(Restrictions.ilike("titleCache", matchmode.queryStringFrom(queryString), org.hibernate.criterion.MatchMode.ANYWHERE));
            }
        }

        //significant identifier
        if (significantIdentifier != null) {
            criteria.add(Restrictions.or(Restrictions.ilike("accessionNumber", significantIdentifier),
                    Restrictions.ilike("catalogNumber", significantIdentifier), Restrictions.ilike("barcode", significantIdentifier)));
        }

        //recordBasis/SpecimenOrObservationType
        Set<SpecimenOrObservationType> typeAndSubtypes = new HashSet<>();
        if(recordBasis==null){
            //add all types
            SpecimenOrObservationType[] values = SpecimenOrObservationType.values();
            for (SpecimenOrObservationType specimenOrObservationType : values) {
                typeAndSubtypes.add(specimenOrObservationType);
            }
        }
        else{
            typeAndSubtypes = recordBasis.getGeneralizationOf(true);
            typeAndSubtypes.add(recordBasis);
        }
        criteria.add(Restrictions.in("recordBasis", typeAndSubtypes));

        Set<UUID> associationUuids = new HashSet<>();
        //taxon associations
        if(associatedTaxon!=null){
            List<UuidAndTitleCache<SpecimenOrObservationBase>> associatedTaxaList = listUuidAndTitleCacheByAssociatedTaxon(
                    clazz, associatedTaxon, includeUnpublished, taxonOccurrenceRelTypes,
                    limit, start, orderHints);
            if(associatedTaxaList!=null){
                for (UuidAndTitleCache<SpecimenOrObservationBase> uuidAndTitleCache : associatedTaxaList) {
                    associationUuids.add(uuidAndTitleCache.getUuid());
                }
            }
        }
        //taxon name associations
        else if(associatedTaxonName!=null){
            List<? extends SpecimenOrObservationBase> associatedTaxaList = listByAssociatedTaxonName(clazz, associatedTaxonName, limit, start, orderHints, propertyPaths);
            if(associatedTaxaList!=null){
                for (SpecimenOrObservationBase<?> specimenOrObservationBase : associatedTaxaList) {
                    associationUuids.add(specimenOrObservationBase.getUuid());
                }
            }
        }
        if(associatedTaxon!=null || associatedTaxonName!=null){
            if(!associationUuids.isEmpty()){
                criteria.add(Restrictions.in("uuid", associationUuids));
            }
            else{
                return null;
            }
        }
        addLimitAndStart(criteria, limit, start);

        if(orderHints!=null){
            addOrder(criteria, orderHints);
        }
        return criteria;
    }


    @Override
    public <T extends SpecimenOrObservationBase> long countOccurrences(Class<T> clazz, String queryString,
            String significantIdentifier, SpecimenOrObservationType recordBasis, Taxon associatedTaxon, TaxonName associatedTaxonName,
            MatchMode matchmode, boolean includeUnpublished, EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

        Criteria criteria = createFindOccurrenceCriteria(clazz, queryString, significantIdentifier, recordBasis,
                associatedTaxon, associatedTaxonName, matchmode, includeUnpublished, taxonOccurrenceRelTypes,
                limit, start, orderHints, propertyPaths);

        if(criteria!=null){
            criteria.setProjection(Projections.rowCount());
            return (Long)criteria.uniqueResult();
        }else{
            return 0;
        }
    }

    @Override
    public List<UuidAndTitleCache<DerivedUnit>> getDerivedUnitUuidAndTitleCache(Integer limit, String pattern) {
        List<UuidAndTitleCache<DerivedUnit>> list = new ArrayList<>();
        Session session = getSession();
        String hql = "SELECT uuid, id, titleCache "
                + " FROM " + type.getSimpleName()
                + " WHERE NOT dtype = " + FieldUnit.class.getSimpleName();
        Query<Object[]> query;
        if (pattern != null){
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            pattern = pattern + "%";
            query = session.createQuery(hql +" AND titleCache like :pattern", Object[].class);
            query.setParameter("pattern", pattern);
        } else {
            query = session.createQuery(hql, Object[].class);
        }
        if (limit != null){
           query.setMaxResults(limit);
        }

        List<Object[]> result = query.list();

        for(Object[] object : result){
            list.add(new UuidAndTitleCache<DerivedUnit>(DerivedUnit.class, (UUID) object[0], (Integer)object[1], (String) object[2]));
        }

        return list;
    }

    @Override
    public List<UuidAndTitleCache<FieldUnit>> getFieldUnitUuidAndTitleCache() {
        List<UuidAndTitleCache<FieldUnit>> list = new ArrayList<>();

        Query<Object[]> query = getSession().createQuery("select uuid, id, titleCache from " + type.getSimpleName() + " where dtype = " + FieldUnit.class.getSimpleName(), Object[].class);

        List<Object[]> result = query.list();

        for(Object[] object : result){
            list.add(new UuidAndTitleCache<FieldUnit>(FieldUnit.class, (UUID) object[0], (Integer)object[1], (String) object[2]));
        }

        return list;
    }

    @Override
    public <T extends SpecimenOrObservationBase> List<T> listByAssociatedTaxonName(Class<T> type,
            TaxonName associatedTaxonName, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

        @SuppressWarnings("rawtypes")
        Set<SpecimenOrObservationBase> setOfAll = new HashSet<>();

        // A Taxon name may be referenced by the DeterminationEvent of the SpecimenOrObservationBase
        @SuppressWarnings("rawtypes")
        List<SpecimenOrObservationBase> byDetermination = list(type, associatedTaxonName, null, 0, null, null);
        setOfAll.addAll(byDetermination);

        if(setOfAll.size() == 0){
            // no need querying the data base
            return new ArrayList<T>();
        }

        String queryString =
            "SELECT sob " +
            " FROM SpecimenOrObservationBase sob" +
            " WHERE sob in (:setOfAll)";

        if(type != null && !type.equals(SpecimenOrObservationBase.class)){
            queryString += " AND sob.class = :type";
        }
        queryString += orderByClause("sob", orderHints);

        @SuppressWarnings("unchecked")
        Query<T> query = getSession().createQuery(queryString);
        query.setParameterList("setOfAll", setOfAll);

        if(type != null && !type.equals(SpecimenOrObservationBase.class)){
            query.setParameter("type", type.getSimpleName());
        }

        addLimitAndStart(query, limit, start);

        @SuppressWarnings("unchecked")
        List<T> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    private List<SpecimenNodeWrapper> querySpecimen(Query<Object[]> query, List<UUID> taxonNodeUuids,
            Integer limit, Integer start){
        query.setParameterList("taxonNodeUuids", taxonNodeUuids);

        addLimitAndStart(query, limit, start);

        List<SpecimenNodeWrapper> list = new ArrayList<>();
        List<Object[]> result = query.list();
        for(Object[] object : result){
            SpecimenNodeWrapper wrapper = new SpecimenNodeWrapper(
                    new UuidAndTitleCache<>(
                            (UUID) object[0],
                            (Integer) object[1],
                            (String) object[2]),
                    (SpecimenOrObservationType)object[3],
                    new TaxonNodeDto((TaxonNode)object[4]));
            if(object.length>5) {
                wrapper.setTaxonDescriptionUuid((UUID)object[5]);
            }
            list.add(wrapper);
        }
        return list;
    }

    private List<SpecimenNodeWrapper> queryIndividualAssociatedSpecimen(List<UUID> taxonNodeUuids,
            Integer limit, Integer start){
        String queryString =  "SELECT "
                + "de.associatedSpecimenOrObservation.uuid, "
                + "de.associatedSpecimenOrObservation.id, "
                + "de.associatedSpecimenOrObservation.titleCache, "
                + "de.associatedSpecimenOrObservation.recordBasis, "
                + "tn, "
                + "d.uuid "
                + "FROM DescriptionElementBase AS de "
                + "LEFT JOIN de.inDescription AS d "
                + "LEFT JOIN d.taxon AS t "
                + "JOIN t.taxonNodes AS tn "
                + "WHERE d.class = 'TaxonDescription' "
                + "AND tn.uuid in (:taxonNodeUuids) "
                ;
        Query<Object[]> query = getSession().createQuery(queryString, Object[].class);
        return querySpecimen(query, taxonNodeUuids, limit, start);
    }

    private List<SpecimenNodeWrapper> queryTypeSpecimen(List<UUID> taxonNodeUuids,
            Integer limit, Integer start){
        String queryString =  "SELECT "
                + "td.typeSpecimen.uuid, "
                + "td.typeSpecimen.id, "
                + "td.typeSpecimen.titleCache, "
                + "td.typeSpecimen.recordBasis, "
                + "tn "
                + "FROM SpecimenTypeDesignation AS td "
                + "LEFT JOIN td.typifiedNames AS tn "
                + "LEFT JOIN tn.taxonBases AS t "
                + "JOIN t.taxonNodes AS tn "
                + "WHERE tn.uuid in (:taxonNodeUuids) "
                ;
        Query<Object[]> query = getSession().createQuery(queryString, Object[].class);
        return querySpecimen(query, taxonNodeUuids, limit, start);
    }

    private List<SpecimenNodeWrapper> queryTaxonDeterminations(List<UUID> taxonNodeUuids,
            Integer limit, Integer start){
        String queryString =  "SELECT "
                + "det.identifiedUnit.uuid, "
                + "det.identifiedUnit.id, "
                + "det.identifiedUnit.titleCache, "
                + "det.identifiedUnit.recordBasis, "
                + "tn "
                + "FROM DeterminationEvent AS det "
                + "LEFT JOIN det.taxon AS t "
                + "JOIN t.taxonNodes AS tn "
                + "WHERE tn.uuid in (:taxonNodeUuids) "
                ;
        Query<Object[]> query = getSession().createQuery(queryString, Object[].class);
        return querySpecimen(query, taxonNodeUuids, limit, start);
    }

    private List<SpecimenNodeWrapper> queryTaxonNameDeterminations(List<UUID> taxonNodeUuids,
            Integer limit, Integer start){
        String queryString =  "SELECT "
                + "det.identifiedUnit.uuid, "
                + "det.identifiedUnit.id, "
                + "det.identifiedUnit.titleCache, "
                + "det.identifiedUnit.recordBasis, "
                + "tn "
                + "FROM DeterminationEvent AS det "
                + "LEFT JOIN det.taxonName AS n "
                + "LEFT JOIN n.taxonBases AS t "
                + "JOIN t.taxonNodes AS tn "
                + "WHERE tn.uuid in (:taxonNodeUuids) "
                ;
        Query<Object[]> query = getSession().createQuery(queryString, Object[].class);
        return querySpecimen(query, taxonNodeUuids, limit, start);
    }

    @Override
    public Collection<SpecimenNodeWrapper> listUuidAndTitleCacheByAssociatedTaxon(List<UUID> taxonNodeUuids,
            Integer limit, Integer start){

        Set<SpecimenNodeWrapper> testSet = new HashSet<>();

        testSet.addAll(queryIndividualAssociatedSpecimen(taxonNodeUuids, limit, start));
        testSet.addAll(queryTaxonDeterminations(taxonNodeUuids, limit, start));
        testSet.addAll(queryTaxonNameDeterminations(taxonNodeUuids, limit, start));
        testSet.addAll(queryTypeSpecimen(taxonNodeUuids, limit, start));

        Collection<SpecimenNodeWrapper> wrappers = new HashSet<>();
        wrappers.addAll(testSet);
        return wrappers;
    }

    @Override
    public <T extends SpecimenOrObservationBase> List<UuidAndTitleCache<SpecimenOrObservationBase>> listUuidAndTitleCacheByAssociatedTaxon(
            Class<T> clazz, Taxon associatedTaxon, boolean includeUnpublished,
            EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Integer limit, Integer start, List<OrderHint> orderHints){

        Query<Object[]> query = createSpecimenQuery("sob.uuid, sob.id, sob.titleCache", clazz,
                associatedTaxon, includeUnpublished, taxonOccurrenceRelTypes,
                limit, start, orderHints, Object[].class);
        if(query==null){
            return Collections.emptyList();
        }
        List<UuidAndTitleCache<SpecimenOrObservationBase>> list = new ArrayList<>();
        List<Object[]> result = query.list();
        for(Object[] object : result){
            list.add(new UuidAndTitleCache<>((UUID) object[0],(Integer) object[1], (String) object[2]));
        }
        return list;
    }

    @Override
    public <T extends SpecimenOrObservationBase> List<T> listByAssociatedTaxon(Class<T> clazz,
            Taxon associatedTaxon, boolean includeUnpublished,
            EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

        @SuppressWarnings("rawtypes")
        Query<SpecimenOrObservationBase> query = createSpecimenQuery(
                "sob", clazz, associatedTaxon, includeUnpublished, taxonOccurrenceRelTypes,
                limit, start, orderHints, SpecimenOrObservationBase.class);
        if(query==null){
            return Collections.emptyList();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<T> results = (List)query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    private <T extends SpecimenOrObservationBase, R extends Object> Query<R> createSpecimenQuery(
            String select, Class<T> clazz, Taxon associatedTaxon,
            boolean includeUnpublished,
            EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Integer limit, Integer start,
            List<OrderHint> orderHints, Class<R> returnClass){

        Set<Integer> setOfAllIds = new HashSet<>();
        boolean classFilterExists = clazz != null && !clazz.equals(SpecimenOrObservationBase.class);
        boolean requiresClassFilter = false;

        //Note: we don't pass limits and order to individual results query as the data is merged with other results

        //add determinations
        if (taxonOccurrenceRelTypes.contains(TaxonOccurrenceRelationType.Determination)
                || taxonOccurrenceRelTypes.contains(TaxonOccurrenceRelationType.CurrentDetermination)) {
            boolean currentOnly = !taxonOccurrenceRelTypes.contains(TaxonOccurrenceRelationType.Determination);
            List<Integer> detResults = addAssociatedDeterminations(clazz, associatedTaxon, currentOnly);
            setOfAllIds.addAll(detResults);
        }

        //add specimen associated via IndividualsAssociation
        if (taxonOccurrenceRelTypes.contains(TaxonOccurrenceRelationType.IndividualsAssociation)) {
            List<Integer> iaResults = descriptionDao.getIndividualAssociationSpecimenIDs(
                    associatedTaxon.getUuid(), null, includeUnpublished, null, null, null);
            //NOTE: iaResults are not yet filtered by clazz
            requiresClassFilter |= classFilterExists && !iaResults.isEmpty();
            setOfAllIds.addAll(iaResults);
        }

        // add specimen associated via type designation
        if (taxonOccurrenceRelTypes.contains(TaxonOccurrenceRelationType.TypeDesignation)) {
            //... of accepted taxon name
            List<Integer> accTdResults = taxonNameDao.getTypeSpecimenIdsForTaxonName(
                    associatedTaxon.getName(), null, null, null);
            //NOTE: accTdResults are not yet filtered by clazz
            requiresClassFilter |= classFilterExists && !accTdResults.isEmpty();
            setOfAllIds.addAll(accTdResults);

            //... and of synonym names (via homotypic groups)
            Set<Integer> synTdResults = getTypeSpecimenIdsForSynonyms(associatedTaxon);
            //NOTE: synTdResults are not yet filtered by clazz
            requiresClassFilter |= classFilterExists && !synTdResults.isEmpty();
            setOfAllIds.addAll(synTdResults);
        }

        if(setOfAllIds.isEmpty()){
            // no need querying the data base
            return null;
        }

        //query
        String queryString =
            " SELECT "+select+
            " FROM SpecimenOrObservationBase sob" +
            " WHERE sob.id in (:setOfAllIds)";

        if (!includeUnpublished) {
            queryString += " AND sob.publish = TRUE ";
        }

        if(requiresClassFilter){
            queryString += " AND sob.class = :type ";
        }

        if(orderHints != null && orderHints.size() > 0){
            queryString += " ORDER BY ";
            String orderStr = "";
            for(OrderHint orderHint : orderHints){
                if(orderStr.length() > 0){
                    orderStr += ", ";
                }
                queryString += "sob." + orderHint.getPropertyName() + " " + orderHint.getSortOrder().toHql();
            }
            queryString += orderStr;
        }

        Query<R> query = getSession().createQuery(queryString, returnClass);
        query.setParameterList("setOfAllIds", setOfAllIds);

        if(requiresClassFilter){
            //note: null warning is incorrect here
            query.setParameter("type", clazz.getSimpleName());
        }

        addLimitAndStart(query, limit, start);

        return query;
    }

    private Set<Integer> getTypeSpecimenIdsForSynonyms(Taxon associatedTaxon) {
        //TODO check if there is a real synonym relationship between accepted taxon and name of homotypic group
        Set<Integer> synTdResults = new HashSet<>();
        for(HomotypicalGroup homotypicalGroup :  associatedTaxon.getHomotypicSynonymyGroups()) {
            //TODO fetch specimen IDs only instead of loading the type designation
            List<SpecimenTypeDesignation> byHomotypicalGroup = homotypicalGroupDao.getTypeDesignations(
                    homotypicalGroup, SpecimenTypeDesignation.class, null, null, 0, null);
            for (SpecimenTypeDesignation specimenTypeDesignation : byHomotypicalGroup) {
                if (specimenTypeDesignation.getTypeSpecimen() != null){
                    synTdResults.add(specimenTypeDesignation.getTypeSpecimen().getId());
                }
            }
        }
        return synTdResults;
    }

    /**
     * Computes the IDs of the specimen associated with a taxon via determinations
     */
    private List<Integer> addAssociatedDeterminations(Class<? extends SpecimenOrObservationBase> clazz,
            Taxon associatedTaxon, boolean currentOnly) {

        Criteria criteria = null;
        if(clazz == null) {
            criteria = getSession().createCriteria(type, "specimen");
        } else {
            criteria = getSession().createCriteria(clazz, "specimen");
        }

        Criteria determinationsCriteria = criteria.createCriteria("determinations");
        if (currentOnly) {
            determinationsCriteria.add(Restrictions.eq("preferredFlag", Boolean.TRUE));
        }

        Disjunction determinationOr = Restrictions.disjunction();

        //taxon
        determinationOr.add(Restrictions.eq("taxon", associatedTaxon));
        //synonyms
        for (Synonym synonym : associatedTaxon.getSynonyms()) {
            determinationOr.add(Restrictions.eq("taxon", synonym));
        }

        //determinations via names, to be used only if determination taxon
        //... accepted name
        determinationOr.add(Restrictions.and(
                Restrictions.eq("taxonName", associatedTaxon.getName()),
                Restrictions.isNull("taxon")));
        //... synonyms
        for (TaxonName synonymName : associatedTaxon.getSynonymNames()) {
            determinationOr.add(Restrictions.and(
                    Restrictions.eq("taxonName", synonymName),
                    Restrictions.isNull("taxon")));
        }

        determinationsCriteria.add(determinationOr);

        criteria.setProjection(Projections.property("id"));

        @SuppressWarnings("unchecked")
        List<Integer> detResults = criteria.list();
        return detResults;
    }

    @Override
    public Collection<SpecimenOrObservationBase> listBySpecimenOrObservationType(SpecimenOrObservationType type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        String queryString = "FROM SpecimenOrObservationBase specimens "
                + " WHERE specimens.recordBasis = :type ";

        queryString += orderByClause("specimens", orderHints);

        Query<SpecimenOrObservationBase> query = getSession().createQuery(queryString, SpecimenOrObservationBase.class);
        query.setParameter("type", type);

        addLimitAndStart(query, limit, start);

        @SuppressWarnings("rawtypes")
        List<SpecimenOrObservationBase> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }


    @Override
    public Collection<DeterminationEvent> listDeterminationEvents(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        String queryString = "FROM DeterminationEvent determination "
                + " WHERE determination.identifiedUnit = :specimen";

        queryString += orderByClause("determination", orderHints);

        Query<DeterminationEvent> query = getSession().createQuery(queryString, DeterminationEvent.class);
        query.setParameter("specimen", specimen);

        addLimitAndStart(query, limit, start);

        List<DeterminationEvent> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public Collection<SpecimenTypeDesignation> listTypeDesignations(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        String queryString = "FROM SpecimenTypeDesignation designations "
                + " WHERE designations.typeSpecimen = :specimen";

        queryString += orderByClause("designations", orderHints);

        Query<SpecimenTypeDesignation> query = getSession().createQuery(queryString, SpecimenTypeDesignation.class);
        query.setParameter("specimen", specimen);

        addLimitAndStart(query, limit, start);

        List<SpecimenTypeDesignation> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public Collection<IndividualsAssociation> listIndividualsAssociations(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        //DISTINCT is necessary if more than one description exists for a taxon because we create the cross product of all taxon descriptions and description elements
        String queryString = "FROM IndividualsAssociation associations WHERE associations.associatedSpecimenOrObservation = :specimen";

        queryString += orderByClause("associations", orderHints);

        Query<IndividualsAssociation> query = getSession().createQuery(queryString, IndividualsAssociation.class);
        query.setParameter("specimen", specimen);

        addLimitAndStart(query, limit, start);

        List<IndividualsAssociation> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public Collection<DescriptionBase<?>> listDescriptionsWithDescriptionSpecimen(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        //DISTINCT is necessary if more than one description exists for a taxon because we create the cross product of all taxon descriptions and description elements
        String queryString = "FROM DescriptionBase descriptions "
                + " WHERE descriptions.describedSpecimenOrObservation = :specimen";

        queryString += orderByClause("descriptions", orderHints);

        Query<DescriptionBase<?>> query = getSession().createQuery(queryString);
        query.setParameter("specimen", specimen);

        addLimitAndStart(query, limit, start);

        List<DescriptionBase<?>> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public List<FieldUnit> findFieldUnitsForGatheringEvent(UUID gatheringEventUuid, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        String queryString = "FROM FieldUnit fu "
                + "WHERE fu.gatheringEvent.uuid = :gatheringEventUuid";

        queryString += orderByClause("sob", orderHints);

        Query<FieldUnit> query = getSession().createQuery(queryString, FieldUnit.class);
        query.setParameter("gatheringEventUuid", gatheringEventUuid);

        addLimitAndStart(query, limit, start);

        List<FieldUnit> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public DnaSample findByGeneticAccessionNumber(String accessionNumberString, List<String> propertyPaths) {
        String queryString = "SELECT dnaSample FROM DnaSample as dnaSample join dnaSample.sequences as sequences WITH sequences.geneticAccessionNumber LIKE :accessionNumberString";
        Query query = getSession().createQuery(queryString);
        query.setParameter("accessionNumberString", accessionNumberString);
        @SuppressWarnings("unchecked")
        List<DnaSample> dnaSamples = query.list();
        defaultBeanInitializer.initializeAll(dnaSamples, propertyPaths);

        if (dnaSamples.isEmpty()){
            logger.debug("there is no dnaSample for genetic accession number " + accessionNumberString + " this should not happen.");
            return null;
        }else if (dnaSamples.size() == 1){
            return dnaSamples.get(0);
        } else{
            logger.debug("there are more than one dnaSample for genetic accession number " + accessionNumberString + " this should not happen.");
            return null;
        }
    }

   @Override
   public long countByGeneticAccessionNumber(String accessionNumberString) {
       String queryString = "SELECT count(dnaSample) FROM DnaSample dnaSample JOIN dnaSample.sequences sequence WHERE sequence.geneticAccessionNumber LIKE :accessionNumberString";
       Query<Long> query = getSession().createQuery(queryString, Long.class);
       query.setParameter("accessionNumberString", accessionNumberString);
       long result = query.uniqueResult();
       return result;
   }

    private void extractDeterminedOriginals(List<DerivedUnit> samples, List<DerivedUnit> results) {
        for (DerivedUnit sample:samples){
            if (sample.getDeterminations() != null && !sample.getDeterminations().isEmpty()){
                results.add(sample);
            }else{
                if (sample instanceof DerivedUnit){
                    Set<SpecimenOrObservationBase> originals = sample.getDerivedFrom().getOriginals();
                    List<DerivedUnit> originalDerivedUnits = new ArrayList<>();
                    for (SpecimenOrObservationBase original: originals){
                        if (original instanceof DerivedUnit){
                            originalDerivedUnits.add((DerivedUnit)original);
                        }
                    }
                    if(!originalDerivedUnits.isEmpty()){
                        extractDeterminedOriginals(originalDerivedUnits, results);
                    }
                }
            }
        }
    }

    @Override
    public List<SpecimenOrObservationBase> findOriginalsForDerivedUnit(UUID derivedUnitUuid, List<String> propertyPaths) {
        String queryString = "SELECT DISTINCT o FROM DerivedUnit du"
                + " JOIN du.derivedFrom.originals o WHERE du.uuid LIKE :derivedUnitUuid";
        Query<SpecimenOrObservationBase> query = getSession().createQuery(queryString, SpecimenOrObservationBase.class);
        query.setParameter("derivedUnitUuid", derivedUnitUuid);
        List<SpecimenOrObservationBase> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public List<Point> findPointsForFieldUnitList(List<UUID> fieldUnitUuids) {
        String queryString = "SELECT DISTINCT fu.gatheringEvent.exactLocation FROM FieldUnit fu"
                + "  WHERE fu.uuid IN (:fieldUnitUuids)";
        Query<Point> query = getSession().createQuery(queryString, Point.class);
        query.setParameterList("fieldUnitUuids", fieldUnitUuids);
        List<Point> results = query.list();

        return results;
    }

    @Override
    public String findMostSignificantIdentifier(UUID derivedUnitUuid) {

        String queryString = "SELECT du.catalogNumber as catalogNumber, du.accessionNumber as accessionNumber, du.barcode as barcode FROM DerivedUnit du"
                + " WHERE du.uuid LIKE :derivedUnitUuid";
        Query<Tuple> query = getSession().createQuery(queryString, Tuple.class);
        query.setParameter("derivedUnitUuid", derivedUnitUuid);
        List<Tuple> results = query.list();
        if (results.isEmpty()){
            return null;
        }
        Tuple stringResult = results.get(0);
        if (stringResult.get("accessionNumber") != null && stringResult.get("accessionNumber") instanceof String){
            return (String)stringResult.get("accessionNumber");
        }
        if (stringResult.get("barcode") != null && stringResult.get("barcode") instanceof String){
            return (String)stringResult.get("barcode");
        }
        if (stringResult.get("catalogNumber") != null && stringResult.get("catalogNumber") instanceof String){
            return (String)stringResult.get("catalogNumber");
        }

        return null;
    }

}
