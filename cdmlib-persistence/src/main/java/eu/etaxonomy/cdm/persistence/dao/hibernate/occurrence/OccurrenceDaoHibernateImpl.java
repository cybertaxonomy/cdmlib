/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence;

import java.util.ArrayList;
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

import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.Fossil;
import eu.etaxonomy.cdm.model.occurrence.LivingBeing;
import eu.etaxonomy.cdm.model.occurrence.Observation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.name.IHomotypicalGroupDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
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
		indexedClasses[0] = FieldObservation.class;
		indexedClasses[1] = DerivedUnit.class;
		indexedClasses[2] = LivingBeing.class;
		indexedClasses[3] = Observation.class;
		indexedClasses[4] = Specimen.class;
		indexedClasses[5] = DnaSample.class;
		indexedClasses[6] = Fossil.class;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao#countDerivationEvents(eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase)
	 */
	@Override
	public int countDerivationEvents(SpecimenOrObservationBase occurence) {
		checkNotInPriorView("OccurrenceDaoHibernateImpl.countDerivationEvents(SpecimenOrObservationBase occurence)");
		Query query = getSession().createQuery("select count(distinct derivationEvent) from DerivationEvent derivationEvent join derivationEvent.originals occurence where occurence = :occurence");
		query.setParameter("occurence", occurence);
		
		return ((Long)query.uniqueResult()).intValue();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao#countDeterminations(eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase, eu.etaxonomy.cdm.model.taxon.TaxonBase)
	 */
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
			query.addProjection(AuditEntity.id().count("id"));

			return ((Long)query.getSingleResult()).intValue();
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao#countMedia(eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase)
	 */
	@Override
	public int countMedia(SpecimenOrObservationBase occurence) {
		checkNotInPriorView("OccurrenceDaoHibernateImpl.countMedia(SpecimenOrObservationBase occurence)");
		Query query = getSession().createQuery("select count(media) from SpecimenOrObservationBase occurence join occurence.media media where occurence = :occurence");
		query.setParameter("occurence", occurence);
		
		return ((Long)query.uniqueResult()).intValue();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao#getDerivationEvents(eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase, java.lang.Integer, java.lang.Integer, java.util.List)
	 */
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
		
		List<DerivationEvent> result = (List<DerivationEvent>)query.list();
		defaultBeanInitializer.initializeAll(result, propertyPaths);
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao#getDeterminations(eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase, eu.etaxonomy.cdm.model.taxon.TaxonBase, java.lang.Integer, java.lang.Integer, java.util.List)
	 */
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
			List<DeterminationEvent> result = (List<DeterminationEvent>)criteria.list();
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
			List<DeterminationEvent> result = (List<DeterminationEvent>)query.getResultList();
            defaultBeanInitializer.initializeAll(result, propertyPaths);			
			return result;
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao#getMedia(eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase, java.lang.Integer, java.lang.Integer, java.util.List)
	 */
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
		
		List<Media> results = (List<Media>)query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);		
		return results;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase#rebuildIndex()
	 */
	@Override
	public void rebuildIndex() {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());
		
		for(SpecimenOrObservationBase occurrence : list(null,null)) { // re-index all taxon base

			for(DeterminationEvent determination : (Set<DeterminationEvent>)occurrence.getDeterminations()) {
			    Hibernate.initialize(determination.getActor());
			    Hibernate.initialize(determination.getTaxon());
			}
			Hibernate.initialize(occurrence.getDefinition());
			if(occurrence instanceof DerivedUnitBase) {
				DerivedUnitBase derivedUnit = (DerivedUnitBase) occurrence;
				Hibernate.initialize(derivedUnit.getCollection());
				if(derivedUnit.getCollection() != null) {
					Hibernate.initialize(derivedUnit.getCollection().getSuperCollection());
					Hibernate.initialize(derivedUnit.getCollection().getInstitute());
				}
				Hibernate.initialize(derivedUnit.getStoredUnder());
				SpecimenOrObservationBase original = derivedUnit.getOriginalUnit();
				if(original != null && original.isInstanceOf(FieldObservation.class)) {
					FieldObservation fieldObservation = original.deproxy(original, FieldObservation.class);
					Hibernate.initialize(fieldObservation.getGatheringEvent());
					if(fieldObservation.getGatheringEvent() != null) {
						Hibernate.initialize(fieldObservation.getGatheringEvent().getActor());
					}
				}
			}
			fullTextSession.index(occurrence);
		}
		fullTextSession.flushToIndexes();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao#count(java.lang.Class, eu.etaxonomy.cdm.model.taxon.TaxonBase)
	 */
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

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao#list(java.lang.Class, eu.etaxonomy.cdm.model.taxon.TaxonBase, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
	 */
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
		
		List<SpecimenOrObservationBase> results = (List<SpecimenOrObservationBase>)criteria.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results; 
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao#getDerivedUnitBaseUuidAndTitleCache()
	 */
	@Override
	public List<UuidAndTitleCache<DerivedUnitBase>> getDerivedUnitBaseUuidAndTitleCache() {
		List<UuidAndTitleCache<DerivedUnitBase>> list = new ArrayList<UuidAndTitleCache<DerivedUnitBase>>();
		Session session = getSession();
		
		Query query = session.createQuery("select uuid, titleCache from " + type.getSimpleName() + " where NOT dtype = " + FieldObservation.class.getSimpleName());
		
		List<Object[]> result = query.list();
		
		for(Object[] object : result){
			list.add(new UuidAndTitleCache<DerivedUnitBase>(DerivedUnitBase.class, (UUID) object[0], (String) object[1]));
		}
		
		return list;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao#getFieldObservationUuidAndTitleCache()
	 */
	@Override
	public List<UuidAndTitleCache<FieldObservation>> getFieldObservationUuidAndTitleCache() {
		List<UuidAndTitleCache<FieldObservation>> list = new ArrayList<UuidAndTitleCache<FieldObservation>>();
		Session session = getSession();
		
		Query query = session.createQuery("select uuid, titleCache from " + type.getSimpleName() + " where dtype = " + FieldObservation.class.getSimpleName());
		
		List<Object[]> result = query.list();
		
		for(Object[] object : result){
			list.add(new UuidAndTitleCache<FieldObservation>(FieldObservation.class, (UUID) object[0], (String) object[1]));
		}
		
		return list;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao#listByAnyAssociation(java.lang.Class, eu.etaxonomy.cdm.model.taxon.Taxon, java.util.List)
	 */
	@Override
	public <T extends SpecimenOrObservationBase> List<T> listByAssociatedTaxon(Class<T> type,
			Taxon associatedTaxon, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
		
		Set<SpecimenOrObservationBase> setOfAll = new HashSet<SpecimenOrObservationBase>();
		
		// A Taxon may be referenced by the DeterminationEvent of the SpecimenOrObservationBase
		List<SpecimenOrObservationBase> byDetermination = list(type, associatedTaxon, null, 0, null, null);
		setOfAll.addAll(byDetermination);
		
		// The IndividualsAssociation elements in a TaxonDescription contain DerivedUnitBases 
		List<IndividualsAssociation> byIndividualsAssociation = descriptionDao.getDescriptionElementForTaxon(
				associatedTaxon, null, IndividualsAssociation.class, null, 0, null);
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
		    
		
	    List<T> results = (List<T>) query.list();
	    defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}
}