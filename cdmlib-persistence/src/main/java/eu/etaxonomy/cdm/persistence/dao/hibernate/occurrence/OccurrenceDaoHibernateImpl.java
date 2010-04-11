/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
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
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
@Repository
public class OccurrenceDaoHibernateImpl extends IdentifiableDaoBase<SpecimenOrObservationBase> implements IOccurrenceDao {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonDaoHibernateImpl.class);

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

	public int countDerivationEvents(SpecimenOrObservationBase occurence) {
		checkNotInPriorView("OccurrenceDaoHibernateImpl.countDerivationEvents(SpecimenOrObservationBase occurence)");
		Query query = getSession().createQuery("select count(distinct derivationEvent) from DerivationEvent derivationEvent join derivationEvent.originals occurence where occurence = :occurence");
		query.setParameter("occurence", occurence);
		
		return ((Long)query.uniqueResult()).intValue();
	}

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
			return (Integer)criteria.uniqueResult();
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

	public int countMedia(SpecimenOrObservationBase occurence) {
		checkNotInPriorView("OccurrenceDaoHibernateImpl.countMedia(SpecimenOrObservationBase occurence)");
		Query query = getSession().createQuery("select count(media) from SpecimenOrObservationBase occurence join occurence.media media where occurence = :occurence");
		query.setParameter("occurence", occurence);
		
		return ((Long)query.uniqueResult()).intValue();
	}

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
}