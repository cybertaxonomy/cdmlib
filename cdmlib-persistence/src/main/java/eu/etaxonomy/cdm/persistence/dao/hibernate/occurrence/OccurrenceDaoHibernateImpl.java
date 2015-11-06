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
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.name.IHomotypicalGroupDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
@Repository
public class OccurrenceDaoHibernateImpl extends IdentifiableDaoBase<SpecimenOrObservationBase> implements IOccurrenceDao {

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
    public int countDerivationEvents(SpecimenOrObservationBase occurence) {
        checkNotInPriorView("OccurrenceDaoHibernateImpl.countDerivationEvents(SpecimenOrObservationBase occurence)");
        Query query = getSession().createQuery("select count(distinct derivationEvent) from DerivationEvent derivationEvent join derivationEvent.originals occurence where occurence = :occurence");
        query.setParameter("occurence", occurence);

        return ((Long)query.uniqueResult()).intValue();
    }

    @Override
    public int countDeterminations(SpecimenOrObservationBase occurrence, TaxonBase taxonBase) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(DeterminationEvent.class);
            if(occurrence != null) {
                criteria.add(Restrictions.eq("identifiedUnit",occurrence));
            }

            if(taxonBase != null) {
                criteria.add(Restrictions.eq("taxon",taxonBase));
            }

            criteria.setProjection(Projections.rowCount());
            return ((Number)criteria.uniqueResult()).intValue();
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(DeterminationEvent.class,auditEvent.getRevisionNumber());

            if(occurrence != null) {
                query.add(AuditEntity.relatedId("identifiedUnit").eq(occurrence.getId()));
            }

            if(taxonBase != null) {
                query.add(AuditEntity.relatedId("taxon").eq(taxonBase.getId()));
            }
            query.addProjection(AuditEntity.id().count());

            return ((Long)query.getSingleResult()).intValue();
        }
    }

    @Override
    public int countMedia(SpecimenOrObservationBase occurence) {
        checkNotInPriorView("OccurrenceDaoHibernateImpl.countMedia(SpecimenOrObservationBase occurence)");
        Query query = getSession().createQuery("select count(media) from SpecimenOrObservationBase occurence join occurence.media media where occurence = :occurence");
        query.setParameter("occurence", occurence);

        return ((Long)query.uniqueResult()).intValue();
    }

    @Override
    public List<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize,Integer pageNumber, List<String> propertyPaths) {
        checkNotInPriorView("OccurrenceDaoHibernateImpl.getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize,Integer pageNumber)");
        Query query = getSession().createQuery("select distinct derivationEvent from DerivationEvent derivationEvent join derivationEvent.originals occurence where occurence = :occurence");
        query.setParameter("occurence", occurence);

        if(pageSize != null) {
            query.setMaxResults(pageSize);
            if(pageNumber != null) {
                query.setFirstResult(pageNumber * pageSize);
            } else {
                query.setFirstResult(0);
            }
        }

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

            if(pageSize != null) {
                criteria.setMaxResults(pageSize);
                if(pageNumber != null) {
                    criteria.setFirstResult(pageNumber * pageSize);
                } else {
                    criteria.setFirstResult(0);
                }
            }
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
            if(pageSize != null) {
                query.setMaxResults(pageSize);
                if(pageNumber != null) {
                    query.setFirstResult(pageNumber * pageSize);
                } else {
                    query.setFirstResult(0);
                }
            }
            List<DeterminationEvent> result = query.getResultList();
            defaultBeanInitializer.initializeAll(result, propertyPaths);
            return result;
        }
    }

    @Override
    public List<Media> getMedia(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        checkNotInPriorView("OccurrenceDaoHibernateImpl.getMedia(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths)");
        Query query = getSession().createQuery("select media from SpecimenOrObservationBase occurence join occurence.media media where occurence = :occurence");
        query.setParameter("occurence", occurence);

        if(pageSize != null) {
            query.setMaxResults(pageSize);
            if(pageNumber != null) {
                query.setFirstResult(pageNumber * pageSize);
            } else {
                query.setFirstResult(0);
            }
        }

        List<Media> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public void rebuildIndex() {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());

        for(SpecimenOrObservationBase occurrence : list(null,null)) { // re-index all taxon base

            for(DeterminationEvent determination : (Set<DeterminationEvent>)occurrence.getDeterminations()) {
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
                SpecimenOrObservationBase original = derivedUnit.getOriginalUnit();
                if(original != null && original.isInstanceOf(FieldUnit.class)) {
                    FieldUnit fieldUnit = original.deproxy(original, FieldUnit.class);
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
    public int count(Class<? extends SpecimenOrObservationBase> clazz,	TaxonNameBase determinedAs) {

        Criteria criteria = null;
        if(clazz == null) {
            criteria = getSession().createCriteria(type);
        } else {
            criteria = getSession().createCriteria(clazz);
        }

        criteria.createCriteria("determinations").add(Restrictions.eq("taxonName", determinedAs));
        criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
        return ((Number)criteria.uniqueResult()).intValue();
    }

    @Override
    public List<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> clazz, TaxonNameBase determinedAs, Integer limit, Integer start,	List<OrderHint> orderHints, List<String> propertyPaths) {
        Criteria criteria = null;
        if(clazz == null) {
            criteria = getSession().createCriteria(type);
        } else {
            criteria = getSession().createCriteria(clazz);
        }

        criteria.createCriteria("determinations").add(Restrictions.eq("taxonName", determinedAs));

        if(limit != null) {
            if(start != null) {
                criteria.setFirstResult(start);
            } else {
                criteria.setFirstResult(0);
            }
            criteria.setMaxResults(limit);
        }

        addOrder(criteria,orderHints);

        List<SpecimenOrObservationBase> results = criteria.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public int count(Class<? extends SpecimenOrObservationBase> clazz,	TaxonBase determinedAs) {

        Criteria criteria = null;
        if(clazz == null) {
            criteria = getSession().createCriteria(type);
        } else {
            criteria = getSession().createCriteria(clazz);
        }

        criteria.createCriteria("determinations").add(Restrictions.eq("taxon", determinedAs));
        criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
        return ((Number)criteria.uniqueResult()).intValue();
    }

    @Override
    public List<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> clazz, TaxonBase determinedAs, Integer limit, Integer start,	List<OrderHint> orderHints, List<String> propertyPaths) {
        Criteria criteria = null;
        if(clazz == null) {
            criteria = getSession().createCriteria(type);
        } else {
            criteria = getSession().createCriteria(clazz);
        }

        criteria.createCriteria("determinations").add(Restrictions.eq("taxon", determinedAs));

        if(limit != null) {
            if(start != null) {
                criteria.setFirstResult(start);
            } else {
                criteria.setFirstResult(0);
            }
            criteria.setMaxResults(limit);
        }

        addOrder(criteria,orderHints);

        List<SpecimenOrObservationBase> results = criteria.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public <T extends SpecimenOrObservationBase> List<T> findOccurrences(Class<T> clazz, String queryString,
            String significantIdentifier, SpecimenOrObservationType recordBasis, Taxon associatedTaxon, TaxonNameBase associatedTaxonName,
            MatchMode matchmode, Integer limit,
            Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

        Criteria criteria = createFindOccurrenceCriteria(clazz, queryString, significantIdentifier, recordBasis,
                associatedTaxon, associatedTaxonName, matchmode, limit, start, orderHints, propertyPaths);

        if(criteria!=null){

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

            List<T> results = criteria.list();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        }
        return Collections.EMPTY_LIST;
    }

    private <T extends SpecimenOrObservationBase> Criteria createFindOccurrenceCriteria(Class<T> clazz, String queryString,
            String significantIdentifier, SpecimenOrObservationType recordBasis, Taxon associatedTaxon, TaxonNameBase associatedTaxonName, MatchMode matchmode, Integer limit,
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

        Set<UUID> associatedTaxonUuids = new HashSet<UUID>();
        //taxon associations
        if(associatedTaxon!=null){
            List<? extends SpecimenOrObservationBase> associatedTaxaList = listByAssociatedTaxon(clazz, associatedTaxon, limit, start, orderHints, propertyPaths);
            if(associatedTaxaList!=null){
                for (SpecimenOrObservationBase specimenOrObservationBase : associatedTaxaList) {
                    associatedTaxonUuids.add(specimenOrObservationBase.getUuid());
                }
            }
        }

        //taxon name associations
        if(associatedTaxonName!=null){
            List<? extends SpecimenOrObservationBase> associatedTaxaList = listByAssociatedTaxonName(clazz, associatedTaxonName, limit, start, orderHints, propertyPaths);
            if(associatedTaxaList!=null){
                for (SpecimenOrObservationBase specimenOrObservationBase : associatedTaxaList) {
                    associatedTaxonUuids.add(specimenOrObservationBase.getUuid());
                }
            }
        }
        if(!associatedTaxonUuids.isEmpty()){
            criteria.add(Restrictions.in("uuid", associatedTaxonUuids));
        }
        else{
            return null;
        }

        return criteria;
    }


    @Override
    public <T extends SpecimenOrObservationBase> int countOccurrences(Class<T> clazz, String queryString,
            String significantIdentifier, SpecimenOrObservationType recordBasis, Taxon associatedTaxon, TaxonNameBase associatedTaxonName,
            MatchMode matchmode, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        Criteria criteria = createFindOccurrenceCriteria(clazz, queryString, significantIdentifier, recordBasis,
                associatedTaxon, associatedTaxonName, matchmode, limit, start, orderHints, propertyPaths);

        if(criteria!=null){

            criteria.setProjection(Projections.rowCount());

            return ((Number)criteria.uniqueResult()).intValue();
        }
        return 0;
    }

    @Override
    public List<UuidAndTitleCache<DerivedUnit>> getDerivedUnitUuidAndTitleCache() {
        List<UuidAndTitleCache<DerivedUnit>> list = new ArrayList<UuidAndTitleCache<DerivedUnit>>();
        Session session = getSession();

        Query query = session.createQuery("select uuid, id, titleCache from " + type.getSimpleName() + " where NOT dtype = " + FieldUnit.class.getSimpleName());

        List<Object[]> result = query.list();

        for(Object[] object : result){
            list.add(new UuidAndTitleCache<DerivedUnit>(DerivedUnit.class, (UUID) object[0], (Integer)object[1], (String) object[2]));
        }

        return list;
    }

    @Override
    public List<UuidAndTitleCache<FieldUnit>> getFieldUnitUuidAndTitleCache() {
        List<UuidAndTitleCache<FieldUnit>> list = new ArrayList<UuidAndTitleCache<FieldUnit>>();
        Session session = getSession();

        Query query = session.createQuery("select uuid, id, titleCache from " + type.getSimpleName() + " where dtype = " + FieldUnit.class.getSimpleName());

        List<Object[]> result = query.list();

        for(Object[] object : result){
            list.add(new UuidAndTitleCache<FieldUnit>(FieldUnit.class, (UUID) object[0], (Integer)object[1], (String) object[2]));
        }

        return list;
    }

    @Override
    public <T extends SpecimenOrObservationBase> List<T> listByAssociatedTaxonName(Class<T> type,
            TaxonNameBase associatedTaxonName, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        Set<SpecimenOrObservationBase> setOfAll = new HashSet<SpecimenOrObservationBase>();

        // A Taxon name may be referenced by the DeterminationEvent of the SpecimenOrObservationBase
        List<SpecimenOrObservationBase> byDetermination = list(type, associatedTaxonName, null, 0, null, null);
        setOfAll.addAll(byDetermination);

        if(setOfAll.size() == 0){
            // no need querying the data base
            return new ArrayList<T>();
        }

        String queryString =
            "select sob " +
            " from SpecimenOrObservationBase sob" +
            " where sob in (:setOfAll)";

        if(type != null){
            queryString += " and sob.class = :type";
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
        query.setParameterList("setOfAll", setOfAll);

        if(type != null){
            query.setParameter("type", type.getSimpleName());
        }

        if(limit != null) {
            if(start != null) {
                query.setFirstResult(start);
            } else {
                query.setFirstResult(0);
            }
            query.setMaxResults(limit);
        }


        List<T> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }
    @Override
    public <T extends SpecimenOrObservationBase> List<T> listByAssociatedTaxon(Class<T> type,
            Taxon associatedTaxon, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

        Set<SpecimenOrObservationBase> setOfAll = new HashSet<SpecimenOrObservationBase>();

        // A Taxon may be referenced by the DeterminationEvent of the SpecimenOrObservationBase
        List<SpecimenOrObservationBase> byDetermination = list(type, associatedTaxon, null, 0, null, null);
        setOfAll.addAll(byDetermination);

        // The IndividualsAssociation elements in a TaxonDescription contain DerivedUnits
        List<IndividualsAssociation> byIndividualsAssociation = descriptionDao.getDescriptionElementForTaxon(
                associatedTaxon.getUuid(), null, IndividualsAssociation.class, null, 0, null);
        for(IndividualsAssociation individualsAssociation : byIndividualsAssociation){
            setOfAll.add(individualsAssociation.getAssociatedSpecimenOrObservation());
        }

        // SpecimenTypeDesignations may be associated with the TaxonName.
        List<SpecimenTypeDesignation> bySpecimenTypeDesignation = taxonNameDao.getTypeDesignations(associatedTaxon.getName(),
                SpecimenTypeDesignation.class, null, null, 0, null);
        for (SpecimenTypeDesignation specimenTypeDesignation : bySpecimenTypeDesignation) {
            setOfAll.add(specimenTypeDesignation.getTypeSpecimen());
        }

        // SpecimenTypeDesignations may be associated with any HomotypicalGroup related to the specific Taxon.
        for(HomotypicalGroup homotypicalGroup :  associatedTaxon.getHomotypicSynonymyGroups()) {
            List<SpecimenTypeDesignation> byHomotypicalGroup = homotypicalGroupDao.getTypeDesignations(homotypicalGroup, SpecimenTypeDesignation.class, null, null, 0, null);
            for (SpecimenTypeDesignation specimenTypeDesignation : byHomotypicalGroup) {
                setOfAll.add(specimenTypeDesignation.getTypeSpecimen());
            }
        }

        if(setOfAll.size() == 0){
            // no need querying the data base
            return new ArrayList<T>();
        }

        String queryString =
            "select sob " +
            " from SpecimenOrObservationBase sob" +
            " where sob in (:setOfAll)";

        if(type != null){
            queryString += " and sob.class = :type";
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
        query.setParameterList("setOfAll", setOfAll);

        if(type != null){
            query.setParameter("type", type.getSimpleName());
        }

        if(limit != null) {
            if(start != null) {
                query.setFirstResult(start);
            } else {
                query.setFirstResult(0);
            }
            query.setMaxResults(limit);
        }


        List<T> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public Collection<SpecimenOrObservationBase> listBySpecimenOrObservationType(SpecimenOrObservationType type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        String queryString = "FROM SpecimenOrObservationBase specimens WHERE specimens.recordBasis = :type";

        if(orderHints != null && orderHints.size() > 0){
            queryString += " order by ";
            String orderStr = "";
            for(OrderHint orderHint : orderHints){
                if(orderStr.length() > 0){
                    orderStr += ", ";
                }
                queryString += "specimens." + orderHint.getPropertyName() + " " + orderHint.getSortOrder().toHql();
            }
            queryString += orderStr;
        }

        Query query = getSession().createQuery(queryString);
        query.setParameter("type", type);

        if(limit != null) {
            if(start != null) {
                query.setFirstResult(start);
            } else {
                query.setFirstResult(0);
            }
            query.setMaxResults(limit);
        }

        List results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public Collection<SpecimenTypeDesignation> listTypeDesignations(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        String queryString = "FROM SpecimenTypeDesignation designations WHERE designations.typeSpecimen = :specimen";

        if(orderHints != null && orderHints.size() > 0){
            queryString += " order by ";
            String orderStr = "";
            for(OrderHint orderHint : orderHints){
                if(orderStr.length() > 0){
                    orderStr += ", ";
                }
                queryString += "designations." + orderHint.getPropertyName() + " " + orderHint.getSortOrder().toHql();
            }
            queryString += orderStr;
        }

        Query query = getSession().createQuery(queryString);
        query.setParameter("specimen", specimen);

        if(limit != null) {
            if(start != null) {
                query.setFirstResult(start);
            } else {
                query.setFirstResult(0);
            }
            query.setMaxResults(limit);
        }

        List results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public Collection<IndividualsAssociation> listIndividualsAssociations(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        //DISTINCT is necessary if more than one description exists for a taxon because we create the cross product of all taxon descriptions and description elements
        String queryString = "FROM IndividualsAssociation associations WHERE associations.associatedSpecimenOrObservation = :specimen";

        if(orderHints != null && orderHints.size() > 0){
            queryString += " order by ";
            String orderStr = "";
            for(OrderHint orderHint : orderHints){
                if(orderStr.length() > 0){
                    orderStr += ", ";
                }
                queryString += "associations." + orderHint.getPropertyName() + " " + orderHint.getSortOrder().toHql();
            }
            queryString += orderStr;
        }

        Query query = getSession().createQuery(queryString);
        query.setParameter("specimen", specimen);

        if(limit != null) {
            if(start != null) {
                query.setFirstResult(start);
            } else {
                query.setFirstResult(0);
            }
            query.setMaxResults(limit);
        }

        List results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public Collection<DescriptionBase<?>> listDescriptionsWithDescriptionSpecimen(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        //DISTINCT is necessary if more than one description exists for a taxon because we create the cross product of all taxon descriptions and description elements
        String queryString = "FROM DescriptionBase descriptions WHERE descriptions.describedSpecimenOrObservation = :specimen";

        if(orderHints != null && orderHints.size() > 0){
            queryString += " order by ";
            String orderStr = "";
            for(OrderHint orderHint : orderHints){
                if(orderStr.length() > 0){
                    orderStr += ", ";
                }
                queryString += "descriptions." + orderHint.getPropertyName() + " " + orderHint.getSortOrder().toHql();
            }
            queryString += orderStr;
        }

        Query query = getSession().createQuery(queryString);
        query.setParameter("specimen", specimen);

        if(limit != null) {
            if(start != null) {
                query.setFirstResult(start);
            } else {
                query.setFirstResult(0);
            }
            query.setMaxResults(limit);
        }

        List results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

}