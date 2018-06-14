/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.name.IHomotypicalGroupDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;
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

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TaxonDaoHibernateImpl.class);

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
        Query query = getSession().createQuery("select count(distinct derivationEvent) from DerivationEvent derivationEvent join derivationEvent.originals occurence where occurence = :occurence");
        query.setParameter("occurence", occurence);

        return (Long)query.uniqueResult();
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
        checkNotInPriorView("OccurrenceDaoHibernateImpl.countMedia(SpecimenOrObservationBase occurence)");
        Query query = getSession().createQuery("select count(media) from SpecimenOrObservationBase occurence join occurence.media media where occurence = :occurence");
        query.setParameter("occurence", occurence);

        return (Long)query.uniqueResult();
    }

    @Override
    public List<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize,Integer pageNumber, List<String> propertyPaths) {
        checkNotInPriorView("OccurrenceDaoHibernateImpl.getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize,Integer pageNumber)");
        Query query = getSession().createQuery("select distinct derivationEvent from DerivationEvent derivationEvent join derivationEvent.originals occurence where occurence = :occurence");
        query.setParameter("occurence", occurence);

        addPageSizeAndNumber(query, pageSize, pageNumber);

        @SuppressWarnings("unchecked")
        List<DerivationEvent> result = query.list();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

    @Override
    public List<DeterminationEvent> getDeterminations(SpecimenOrObservationBase occurrence, TaxonBase taxonBase, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
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
    public List<Media> getMedia(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        checkNotInPriorView("OccurrenceDaoHibernateImpl.getMedia(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths)");
        Query query = getSession().createQuery(
                "   SELECT media "
                + " FROM SpecimenOrObservationBase occurence "
                + " JOIN occurence.media media "
                + " WHERE occurence = :occurence");
        query.setParameter("occurence", occurence);

        addPageSizeAndNumber(query, pageSize, pageNumber);

        @SuppressWarnings("unchecked")
        List<Media> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
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
            Taxon associatedTaxon, TaxonName associatedTaxonName, MatchMode matchmode, Integer limit, Integer start,
            List<OrderHint> orderHints) {
        Criteria criteria = createFindOccurrenceCriteria(clazz, queryString, significantIdentifier, recordBasis,
                associatedTaxon, associatedTaxonName, matchmode, limit, start, orderHints, null);
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
        }
        return Collections.emptyList();
    }

    @Override
    public <T extends SpecimenOrObservationBase> List<T> findOccurrences(Class<T> clazz, String queryString,
            String significantIdentifier, SpecimenOrObservationType recordBasis, Taxon associatedTaxon, TaxonName associatedTaxonName,
            MatchMode matchmode, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

        Criteria criteria = createFindOccurrenceCriteria(clazz, queryString, significantIdentifier, recordBasis,
                associatedTaxon, associatedTaxonName, matchmode, limit, start, orderHints, propertyPaths);
        if(criteria!=null){
            @SuppressWarnings("unchecked")
            List<T> results = criteria.list();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        }
        return Collections.emptyList();
    }

    private <T extends SpecimenOrObservationBase> Criteria createFindOccurrenceCriteria(Class<T> clazz, String queryString,
            String significantIdentifier, SpecimenOrObservationType recordBasis, Taxon associatedTaxon, TaxonName associatedTaxonName, MatchMode matchmode, Integer limit,
            Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
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
        Set<SpecimenOrObservationType> typeAndSubtypes = new HashSet<SpecimenOrObservationType>();
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

        Set<UUID> associationUuids = new HashSet<UUID>();
        //taxon associations
        if(associatedTaxon!=null){
            List<UuidAndTitleCache<SpecimenOrObservationBase>> associatedTaxaList = listUuidAndTitleCacheByAssociatedTaxon(clazz, associatedTaxon, limit, start, orderHints);
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
                for (SpecimenOrObservationBase specimenOrObservationBase : associatedTaxaList) {
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
        if(limit != null) {
            if(start != null) {
                criteria.setFirstResult(start);
            } else {
                criteria.setFirstResult(0);
            }
            criteria.setMaxResults(limit);
        }

        if(orderHints!=null){
            addOrder(criteria,orderHints);
        }
        return criteria;
    }


    @Override
    public <T extends SpecimenOrObservationBase> long countOccurrences(Class<T> clazz, String queryString,
            String significantIdentifier, SpecimenOrObservationType recordBasis, Taxon associatedTaxon, TaxonName associatedTaxonName,
            MatchMode matchmode, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

        Criteria criteria = createFindOccurrenceCriteria(clazz, queryString, significantIdentifier, recordBasis,
                associatedTaxon, associatedTaxonName, matchmode, limit, start, orderHints, propertyPaths);

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
        Query query;
        if (pattern != null){
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            pattern = pattern + "%";
            query = session.createQuery(hql +" AND titleCache like :pattern");
            query.setParameter("pattern", pattern);
        } else {
            query = session.createQuery(hql);
        }
        if (limit != null){
           query.setMaxResults(limit);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

        for(Object[] object : result){
            list.add(new UuidAndTitleCache<DerivedUnit>(DerivedUnit.class, (UUID) object[0], (Integer)object[1], (String) object[2]));
        }

        return list;
    }

    @Override
    public List<UuidAndTitleCache<FieldUnit>> getFieldUnitUuidAndTitleCache() {
        List<UuidAndTitleCache<FieldUnit>> list = new ArrayList<>();
        Session session = getSession();

        Query query = session.createQuery("select uuid, id, titleCache from " + type.getSimpleName() + " where dtype = " + FieldUnit.class.getSimpleName());

        @SuppressWarnings("unchecked")
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

        Query query = getSession().createQuery(queryString);
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

    private List<SpecimenNodeWrapper> querySpecimen(
            Query query, List<UUID> taxonNodeUuids,
            Integer limit, Integer start){
        query.setParameterList("taxonNodeUuids", taxonNodeUuids);

        addLimitAndStart(query, limit, start);

        List<SpecimenNodeWrapper> list = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();
        for(Object[] object : result){
            list.add(new SpecimenNodeWrapper(
                    new UuidAndTitleCache<>(
                            (UUID) object[0],
                            (Integer) object[1],
                            (String) object[2]),
                    (TaxonNode)object[3]));
        }
        return list;
    }

    private List<SpecimenNodeWrapper> queryIndividualAssociatedSpecimen(List<UUID> taxonNodeUuids,
            Integer limit, Integer start){
        String queryString =  "SELECT "
                + "de.associatedSpecimenOrObservation.uuid, "
                + "de.associatedSpecimenOrObservation.id, "
                + "de.associatedSpecimenOrObservation.titleCache, "
                + "tn "
                + "FROM DescriptionElementBase AS de "
                + "LEFT JOIN de.inDescription AS d "
                + "LEFT JOIN d.taxon AS t "
                + "JOIN t.taxonNodes AS tn "
                + "WHERE d.class = 'TaxonDescription' "
                + "AND tn.uuid in (:taxonNodeUuids) "
                ;
        Query query = getSession().createQuery(queryString);
        return querySpecimen(query, taxonNodeUuids, limit, start);
    }

    private List<SpecimenNodeWrapper> queryTypeSpecimen(List<UUID> taxonNodeUuids,
            Integer limit, Integer start){
        String queryString =  "SELECT "
                + "td.typeSpecimen.uuid, "
                + "td.typeSpecimen.id, "
                + "td.typeSpecimen.titleCache, "
                + "tn "
                + "FROM SpecimenTypeDesignation AS td "
                + "LEFT JOIN td.typifiedNames AS tn "
                + "LEFT JOIN tn.taxonBases AS t "
                + "JOIN t.taxonNodes AS tn "
                + "WHERE tn.uuid in (:taxonNodeUuids) "
                ;
        Query query = getSession().createQuery(queryString);
        return querySpecimen(query, taxonNodeUuids, limit, start);
    }

    private List<SpecimenNodeWrapper> queryTaxonDeterminations(List<UUID> taxonNodeUuids,
            Integer limit, Integer start){
        String queryString =  "SELECT "
                + "det.identifiedUnit.uuid, "
                + "det.identifiedUnit.id, "
                + "det.identifiedUnit.titleCache, "
                + "tn "
                + "FROM DeterminationEvent AS det "
                + "LEFT JOIN det.taxon AS t "
                + "JOIN t.taxonNodes AS tn "
                + "WHERE tn.uuid in (:taxonNodeUuids) "
                ;
        Query query = getSession().createQuery(queryString);
        return querySpecimen(query, taxonNodeUuids, limit, start);
    }

    private List<SpecimenNodeWrapper> queryTaxonNameDeterminations(List<UUID> taxonNodeUuids,
            Integer limit, Integer start){
        String queryString =  "SELECT "
                + "det.identifiedUnit.uuid, "
                + "det.identifiedUnit.id, "
                + "det.identifiedUnit.titleCache, "
                + "tn "
                + "FROM DeterminationEvent AS det "
                + "LEFT JOIN det.taxonName AS n "
                + "LEFT JOIN n.taxonBases AS t "
                + "JOIN t.taxonNodes AS tn "
                + "WHERE tn.uuid in (:taxonNodeUuids) "
                ;
        Query query = getSession().createQuery(queryString);
        return querySpecimen(query, taxonNodeUuids, limit, start);
    }

    @Override
    public Collection<SpecimenNodeWrapper> listUuidAndTitleCacheByAssociatedTaxon(List<UUID> taxonNodeUuids,
            Integer limit, Integer start){

        Collection<SpecimenNodeWrapper> wrappers = new HashSet<>();
        wrappers.addAll(queryIndividualAssociatedSpecimen(taxonNodeUuids, limit, start));
        wrappers.addAll(queryTaxonDeterminations(taxonNodeUuids, limit, start));
        wrappers.addAll(queryTaxonNameDeterminations(taxonNodeUuids, limit, start));
        wrappers.addAll(queryTypeSpecimen(taxonNodeUuids, limit, start));

        return wrappers;
    }

    @Override
    public <T extends SpecimenOrObservationBase> List<UuidAndTitleCache<SpecimenOrObservationBase>> listUuidAndTitleCacheByAssociatedTaxon(Class<T> clazz, Taxon associatedTaxon,
            Integer limit, Integer start, List<OrderHint> orderHints){
        Query query = createSpecimenQuery("sob.uuid, sob.id, sob.titleCache", clazz, associatedTaxon, limit, start, orderHints, null);
        if(query==null){
            return Collections.emptyList();
        }
        List<UuidAndTitleCache<SpecimenOrObservationBase>> list = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();
        for(Object[] object : result){
            list.add(new UuidAndTitleCache<>((UUID) object[0],(Integer) object[1], (String) object[2]));
        }
        return list;
    }

    @Override
    public <T extends SpecimenOrObservationBase> List<T> listByAssociatedTaxon(Class<T> clazz,
            Taxon associatedTaxon, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        Query query = createSpecimenQuery("sob", clazz, associatedTaxon, limit, start, orderHints, propertyPaths);
        if(query==null){
            return Collections.emptyList();
        }
        @SuppressWarnings("unchecked")
        List<T> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    private <T extends SpecimenOrObservationBase> Query createSpecimenQuery(String select, Class<T> clazz,
            Taxon associatedTaxon, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths){
        Set<SpecimenOrObservationBase> setOfAll = new HashSet<>();
        Set<Integer> setOfAllIds = new HashSet<>();

        Criteria criteria = null;
        if(clazz == null) {
            criteria = getSession().createCriteria(type, "specimen");
        } else {
            criteria = getSession().createCriteria(clazz, "specimen");
        }

        Disjunction determinationOr = Restrictions.disjunction();

        // A Taxon may be referenced by the DeterminationEvent of the SpecimenOrObservationBase
        Criteria determinationsCriteria = criteria.createCriteria("determinations");

        determinationOr.add(Restrictions.eq("taxon", associatedTaxon));
        //check also for synonyms
        for (Synonym synonym : associatedTaxon.getSynonyms()) {
            determinationOr.add(Restrictions.eq("taxon", synonym));
        }

        //check also for name determinations
        determinationOr.add(Restrictions.eq("taxonName", associatedTaxon.getName()));
        for (TaxonName synonymName : associatedTaxon.getSynonymNames()) {
            determinationOr.add(Restrictions.eq("taxonName", synonymName));
        }

        determinationsCriteria.add(determinationOr);

        if(limit != null) {
            if(start != null) {
                criteria.setFirstResult(start);
            } else {
                criteria.setFirstResult(0);
            }
            criteria.setMaxResults(limit);
        }
        criteria.setProjection(Projections.property("id"));

        addOrder(criteria,orderHints);

        @SuppressWarnings("unchecked")
        List<Integer> detResults = criteria.list();
        setOfAllIds.addAll(detResults);

        // The IndividualsAssociation elements in a TaxonDescription contain DerivedUnits
        setOfAllIds.addAll(descriptionDao.getIndividualAssociationSpecimenIDs(
                associatedTaxon.getUuid(), null, null, 0, null));


        // SpecimenTypeDesignations may be associated with the TaxonName.
        setOfAllIds.addAll(taxonNameDao.getTypeSpecimenIdsForTaxonName(
                associatedTaxon.getName(), null, null, null));

        // SpecimenTypeDesignations may be associated with any HomotypicalGroup related to the specific Taxon.
        //TODO adapt to set of ids
        for(HomotypicalGroup homotypicalGroup :  associatedTaxon.getHomotypicSynonymyGroups()) {
            List<SpecimenTypeDesignation> byHomotypicalGroup = homotypicalGroupDao.getTypeDesignations(homotypicalGroup, SpecimenTypeDesignation.class, null, null, 0, null);
            for (SpecimenTypeDesignation specimenTypeDesignation : byHomotypicalGroup) {
                setOfAll.add(specimenTypeDesignation.getTypeSpecimen());
            }
        }

        if(setOfAllIds.size() == 0){
            // no need querying the data base
            return null;
        }

        String queryString =
            "select "+select+
            " from SpecimenOrObservationBase sob" +
            " where sob.id in (:setOfAllIds)";

        if(clazz != null && !clazz.equals(SpecimenOrObservationBase.class)){
            queryString += " and sob.class = :type ";
        }

        if(orderHints != null && orderHints.size() > 0){
            queryString += " order by ";
            String orderStr = "";
            for(OrderHint orderHint : orderHints){
                if(orderStr.length() > 0){
                    orderStr += ", ";
                }
                queryString += "sob." + orderHint.getPropertyName() + " " + orderHint.getSortOrder().toHql();
            }
            queryString += orderStr;
        }

        Query query = getSession().createQuery(queryString);
        query.setParameterList("setOfAllIds", setOfAllIds);

        if(clazz != null && !clazz.equals(SpecimenOrObservationBase.class)){
            query.setParameter("type", clazz.getSimpleName());
        }

        addLimitAndStart(query, limit, start);

        return query;
    }

    @Override
    public Collection<SpecimenOrObservationBase> listBySpecimenOrObservationType(SpecimenOrObservationType type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        String queryString = "FROM SpecimenOrObservationBase specimens "
                + " WHERE specimens.recordBasis = :type ";

        queryString += orderByClause("specimens", orderHints);

        Query query = getSession().createQuery(queryString);
        query.setParameter("type", type);

        addLimitAndStart(query, limit, start);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<SpecimenOrObservationBase> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }


    @Override
    public Collection<DeterminationEvent> listDeterminationEvents(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        String queryString = "FROM DeterminationEvent determination "
                + " WHERE determination.identifiedUnit = :specimen";

        queryString += orderByClause("determination", orderHints);

        Query query = getSession().createQuery(queryString);
        query.setParameter("specimen", specimen);

        addLimitAndStart(query, limit, start);

        @SuppressWarnings("unchecked")
        List<DeterminationEvent> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public Collection<SpecimenTypeDesignation> listTypeDesignations(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        String queryString = "FROM SpecimenTypeDesignation designations "
                + " WHERE designations.typeSpecimen = :specimen";

        queryString += orderByClause("designations", orderHints);

        Query query = getSession().createQuery(queryString);
        query.setParameter("specimen", specimen);

        addLimitAndStart(query, limit, start);

        @SuppressWarnings("unchecked")
        List<SpecimenTypeDesignation> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }


    @Override
    public Collection<IndividualsAssociation> listIndividualsAssociations(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        //DISTINCT is necessary if more than one description exists for a taxon because we create the cross product of all taxon descriptions and description elements
        String queryString = "FROM IndividualsAssociation associations WHERE associations.associatedSpecimenOrObservation = :specimen";

        queryString += orderByClause("associations", orderHints);

        Query query = getSession().createQuery(queryString);
        query.setParameter("specimen", specimen);

        addLimitAndStart(query, limit, start);

        @SuppressWarnings("unchecked")
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

        Query query = getSession().createQuery(queryString);
        query.setParameter("specimen", specimen);

        addLimitAndStart(query, limit, start);

        @SuppressWarnings("unchecked")
        List<DescriptionBase<?>> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FieldUnit> getFieldUnitsForGatheringEvent(UUID gatheringEventUuid, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        String queryString = "FROM SpecimenOrObservationBase sob "
                + "WHERE sob.gatheringEvent.uuid = :gatheringEventUuid";

        queryString += orderByClause("sob", orderHints);

        Query query = getSession().createQuery(queryString);
        query.setParameter("gatheringEventUuid", gatheringEventUuid);

        addLimitAndStart(query, limit, start);

        @SuppressWarnings("unchecked")
        List<FieldUnit> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DerivedUnit> getByGeneticAccessionNumber(String accessionNumberString, List<String> propertyPaths) {
        String queryString = "SELECT dnaSample FROM DnaSample dnaSample join dnaSample.sequences sequence WHERE sequence.geneticAccessionNumber LIKE :accessionNumberString";
        Query query = getSession().createQuery(queryString);
        query.setParameter("accessionNumberString", accessionNumberString);
        @SuppressWarnings("unchecked")
        List<DerivedUnit> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

}
