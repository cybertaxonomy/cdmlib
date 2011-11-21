/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.Scope;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.OperationNotSupportedInPriorViewException;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository 
@Qualifier("descriptionDaoImpl")
public class DescriptionDaoImpl extends IdentifiableDaoBase<DescriptionBase> implements IDescriptionDao{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DescriptionDaoImpl.class);

	public DescriptionDaoImpl() {
		super(DescriptionBase.class); 
		indexedClasses = new Class[3];
		indexedClasses[0] = TaxonDescription.class;
		indexedClasses[1] = TaxonNameDescription.class;
		indexedClasses[2] = SpecimenDescription.class;
	}

	public int countDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase status) {
		checkNotInPriorView("DescriptionDaoImpl.countDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase status)");
		Query query = null;
		
		if(status == null) {
			query = getSession().createQuery("select count(distinct description) from TaxonDescription description left join description.descriptionElements element join element.area area where area in (:namedAreas)");
		} else {
			query = getSession().createQuery("select count(distinct description) from TaxonDescription description left join description.descriptionElements element join element.area area  join element.status status where area in (:namedAreas) and status = :status");
			query.setParameter("status", status);
		}
		query.setParameterList("namedAreas", namedAreas);
		
		return ((Long)query.uniqueResult()).intValue();
	}

	public int countDescriptionElements(DescriptionBase description, Set<Feature> features, Class<? extends DescriptionElementBase> clazz) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = null;
			if(clazz == null) {
		        criteria = getSession().createCriteria(DescriptionElementBase.class);
			} else {
			    criteria = getSession().createCriteria(clazz);	
			}	
			
		    if(description != null) {
		        criteria.add(Restrictions.eq("inDescription", description));
		    }
		
		    if(features != null && !features.isEmpty()) {
			    criteria.add(Restrictions.in("feature", features));
		    }
		
		    criteria.setProjection(Projections.rowCount());
		
		    return (Integer)criteria.uniqueResult();
		} else {
			if(features != null && !features.isEmpty()) {
				Integer count = 0;
			    for(Feature f : features) {
			        AuditQuery query = null;
			        if(clazz == null) {
			        	query = getAuditReader().createQuery().forEntitiesAtRevision(DescriptionElementBase.class,auditEvent.getRevisionNumber());
			        } else {
			        	query = getAuditReader().createQuery().forEntitiesAtRevision(clazz,auditEvent.getRevisionNumber());
			        }
			    
			        if(description != null) {
			    	    query.add(AuditEntity.relatedId("inDescription").eq(description.getId()));
			        }
			     
			        query.add(AuditEntity.relatedId("feature").eq(f.getId()));
			        query.addProjection(AuditEntity.id().count("id"));
			        count += ((Long)query.getSingleResult()).intValue();
			    }
			    
			    return count;
			} else {
				AuditQuery query = null;
		        if(clazz == null) {
		        	query = getAuditReader().createQuery().forEntitiesAtRevision(DescriptionElementBase.class,auditEvent.getRevisionNumber());
		        } else {
		        	query = getAuditReader().createQuery().forEntitiesAtRevision(clazz,auditEvent.getRevisionNumber());
		        }
			    
		        if(description != null) {
		    	    query.add(AuditEntity.relatedId("inDescription").eq(description.getId()));
		        }
		        query.addProjection(AuditEntity.id().count("id"));
		        return ((Long)query.getSingleResult()).intValue();
			}
		}
	}

	public int countDescriptions(Class<? extends DescriptionBase> clazz, Boolean hasImages, Boolean hasText, Set<Feature> features) {
		checkNotInPriorView("DescriptionDaoImpl.countDescriptions(Class<TYPE> type, Boolean hasImages, Boolean hasText, Set<Feature> features)");
		Criteria inner = null;
		
		if(clazz == null) {
			inner = getSession().createCriteria(type);
		} else {
			inner = getSession().createCriteria(clazz);
		}
		
		Criteria elementsCriteria = inner.createCriteria("descriptionElements");
		if(hasText != null) {
			if(hasText) {
				elementsCriteria.add(Restrictions.isNotEmpty("multilanguageText"));
			} else {
				elementsCriteria.add(Restrictions.isEmpty("multilanguageText"));
			}
		}
		
		if(hasImages != null) {
			if(hasImages) {
				elementsCriteria.add(Restrictions.isNotEmpty("media"));
			} else {
				elementsCriteria.add(Restrictions.isEmpty("media"));
			}
		}
		
		if(features != null && !features.isEmpty()) {
			elementsCriteria.add(Restrictions.in("feature", features));
		}
		
		inner.setProjection(Projections.countDistinct("id"));

		return (Integer) inner.uniqueResult();
	}

	public int countTaxonDescriptions(Taxon taxon, Set<Scope> scopes,Set<NamedArea> geographicalScopes, Set<MarkerType> markerTypes) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getSession().createCriteria(TaxonDescription.class);

			if(taxon != null) {
				criteria.add(Restrictions.eq("taxon", taxon));
			}

			if(scopes != null && !scopes.isEmpty()) {
				Set<Integer> scopeIds = new HashSet<Integer>();
				for(Scope s : scopes) {
					scopeIds.add(s.getId());
				}
				criteria.createCriteria("scopes").add(Restrictions.in("id", scopeIds));
			}

			if(geographicalScopes != null && !geographicalScopes.isEmpty()) {
				Set<Integer> geoScopeIds = new HashSet<Integer>();
				for(NamedArea n : geographicalScopes) {
					geoScopeIds.add(n.getId());
				}
				criteria.createCriteria("geoScopes").add(Restrictions.in("id", geoScopeIds));
			}

			
			addMarkerTypesCriterion(markerTypes, criteria);

			
			criteria.setProjection(Projections.rowCount());

			return (Integer)criteria.uniqueResult();
		} else {
			if((scopes == null || scopes.isEmpty())&& (geographicalScopes == null || geographicalScopes.isEmpty()) && (markerTypes == null || markerTypes.isEmpty())) {
				AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonDescription.class,auditEvent.getRevisionNumber());
				if(taxon != null) {
				    query.add(AuditEntity.relatedId("taxon").eq(taxon.getId()));
				}
				
				query.addProjection(AuditEntity.id().count("id"));
				
				return ((Long)query.getSingleResult()).intValue();
			} else {
				throw new OperationNotSupportedInPriorViewException("countTaxonDescriptions(Taxon taxon, Set<Scope> scopes,Set<NamedArea> geographicalScopes)");
			}
		}
	}

	/**
	 * @param markerTypes
	 * @param criteria
	 *
	 */
	//TODO move to AnnotatableEntityDao(?)
	private void addMarkerTypesCriterion(Set<MarkerType> markerTypes,
			Criteria criteria) {
		if(markerTypes != null && !markerTypes.isEmpty()) {
			Set<Integer> markerTypeIds = new HashSet<Integer>();
			for(MarkerType markerType : markerTypes) {
				markerTypeIds.add(markerType.getId());
			}
			criteria.createCriteria("markers").add(Restrictions.eq("flag", true))
					.createAlias("markerType", "mt")
			 		.add(Restrictions.in("id", markerTypeIds));
		}
	}

	public List<DescriptionElementBase> getDescriptionElements(DescriptionBase description, Set<Feature> features,Class<? extends DescriptionElementBase> clazz, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = null;
            if(clazz == null) {
            	criteria = getSession().createCriteria(DescriptionElementBase.class);
            } else {
            	criteria = getSession().createCriteria(clazz);
            }
		
            if(description != null) {
		        criteria.add(Restrictions.eq("inDescription", description));
		    }
		
		    if(features != null && !features.isEmpty()) {
			    criteria.add(Restrictions.in("feature", features));
		    }
		
		    if(pageSize != null) {
			    criteria.setMaxResults(pageSize);
		        if(pageNumber != null) {
		    	    criteria.setFirstResult(pageNumber * pageSize);
		        }
		    }
		    
		    List<DescriptionElementBase> results = (List<DescriptionElementBase>)criteria.list();
		    
		    defaultBeanInitializer.initializeAll(results, propertyPaths);
		
	    	return results; 
		} else {
			List<DescriptionElementBase> result = new ArrayList<DescriptionElementBase>();
			if(features != null && !features.isEmpty()) {
				
			    for(Feature f : features) {
			    	AuditQuery query = null;
			    	if(clazz == null) {
			            query = getAuditReader().createQuery().forEntitiesAtRevision(DescriptionElementBase.class,auditEvent.getRevisionNumber());
			    	} else {
			    		query = getAuditReader().createQuery().forEntitiesAtRevision(clazz,auditEvent.getRevisionNumber());
			    	}
			    	
			        if(description != null) {
			    	    query.add(AuditEntity.relatedId("inDescription").eq(description.getId()));
			        }
			     
			        query.add(AuditEntity.relatedId("feature").eq(f.getId()));
			        result.addAll((List<DescriptionElementBase>)query.getResultList());
			    }
			} else {
				AuditQuery query = null;
		    	if(clazz == null) {
		            query = getAuditReader().createQuery().forEntitiesAtRevision(DescriptionElementBase.class,auditEvent.getRevisionNumber());
		    	} else {
		    		query = getAuditReader().createQuery().forEntitiesAtRevision(clazz,auditEvent.getRevisionNumber());
		    	}
			    
		        if(description != null) {
		    	    query.add(AuditEntity.relatedId("inDescription").eq(description.getId()));
		        }
		        
		        result = query.getResultList();
			}
			
			defaultBeanInitializer.initializeAll(result, propertyPaths);
			
			return result;
		}
	}

	public List<TaxonDescription> getTaxonDescriptions(Taxon taxon,	Set<Scope> scopes, Set<NamedArea> geographicalScopes, Set<MarkerType> markerTypes, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getSession().createCriteria(TaxonDescription.class);

			if(taxon != null) {
				criteria.add(Restrictions.eq("taxon", taxon));
			}

			if(scopes != null && !scopes.isEmpty()) {
				Set<Integer> scopeIds = new HashSet<Integer>();
				for(Scope s : scopes) {
					scopeIds.add(s.getId());
				}
				criteria.createCriteria("scopes").add(Restrictions.in("id", scopeIds));
			}

			if(geographicalScopes != null && !geographicalScopes.isEmpty()) {
				Set<Integer> geoScopeIds = new HashSet<Integer>();
				for(NamedArea n : geographicalScopes) {
					geoScopeIds.add(n.getId());
				}
				criteria.createCriteria("geoScopes").add(Restrictions.in("id", geoScopeIds));
			}
			
			addMarkerTypesCriterion(markerTypes, criteria);

			if(pageSize != null) {
				criteria.setMaxResults(pageSize);
				if(pageNumber != null) {
					criteria.setFirstResult(pageNumber * pageSize);
				}
			}

			List<TaxonDescription> results = (List<TaxonDescription>)criteria.list();

			defaultBeanInitializer.initializeAll(results, propertyPaths);

			return results;
		} else {
			if((scopes == null || scopes.isEmpty())&& (geographicalScopes == null || geographicalScopes.isEmpty())&& (markerTypes == null || markerTypes.isEmpty())) {
				AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonDescription.class,auditEvent.getRevisionNumber());
				if(taxon != null) {
				    query.add(AuditEntity.relatedId("taxon").eq(taxon.getId()));
				}

				if(pageSize != null) {
			        query.setMaxResults(pageSize);
			        if(pageNumber != null) {
			            query.setFirstResult(pageNumber * pageSize);
			        } else {
			    	    query.setFirstResult(0);
			        }
			    }
				
				List<TaxonDescription> results = (List<TaxonDescription>)query.getResultList();
				defaultBeanInitializer.initializeAll(results, propertyPaths);
				return results;
			} else {
				throw new OperationNotSupportedInPriorViewException("countTaxonDescriptions(Taxon taxon, Set<Scope> scopes,Set<NamedArea> geographicalScopes)");
			}
		}
	}
	
    public List<TaxonNameDescription> getTaxonNameDescriptions(TaxonNameBase name, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        AuditEvent auditEvent = getAuditEventFromContext();
	    if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
	        Criteria criteria = getSession().createCriteria(TaxonNameDescription.class);
	  
	      if(name != null) {
		      criteria.add(Restrictions.eq("taxonName", name));
	      }
	  
	      if(pageSize != null) {
			  criteria.setMaxResults(pageSize);
		      if(pageNumber != null) {
		    	  criteria.setFirstResult(pageNumber * pageSize);
		      }
	      }
	  
	      List<TaxonNameDescription> results = (List<TaxonNameDescription>)criteria.list();
	      
	      defaultBeanInitializer.initializeAll(results, propertyPaths);
	      
	      return results; 
	    } else {
	    	AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonNameDescription.class,auditEvent.getRevisionNumber());
	    	
	    	if(name != null) {
			    query.add(AuditEntity.relatedId("taxonName").eq(name.getId()));
		    }
	    	
	    	if(pageSize != null) {
				  query.setMaxResults(pageSize);
			      if(pageNumber != null) {
			    	  query.setFirstResult(pageNumber * pageSize);
			      }
		    }
	    	
	    	List<TaxonNameDescription> results = (List<TaxonNameDescription>)query.getResultList();
	    	
	    	defaultBeanInitializer.initializeAll(results, propertyPaths);
	    	
	    	return results;
	    }
	  
    }
	
	public int countTaxonNameDescriptions(TaxonNameBase name) {
		AuditEvent auditEvent = getAuditEventFromContext();
	    if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Criteria criteria = getSession().createCriteria(TaxonNameDescription.class);
		  
		    if(name != null) {
			    criteria.add(Restrictions.eq("taxonName", name));
		    }
		  
		    criteria.setProjection(Projections.rowCount());
		  
		    return (Integer)criteria.uniqueResult();
	    } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonNameDescription.class,auditEvent.getRevisionNumber());
	    	
	    	if(name != null) {
			    query.add(AuditEntity.relatedId("taxonName").eq(name.getId()));
		    }
	    	
	    	query.addProjection(AuditEntity.id().count("id"));
	    	return ((Long)query.getSingleResult()).intValue();
	    }
	}

	/**
	 * Should use a DetachedCriteria & subquery, but HHH-158 prevents this, for now.
	 * 
	 * e.g. DetachedCriteria inner = DestachedCriteria.forClass(type);
	 * 
	 * outer.add(Subqueries.propertyIn("id", inner));
	 */
	public List<DescriptionBase> listDescriptions(Class<? extends DescriptionBase> clazz, Boolean hasImages, Boolean hasText,	Set<Feature> features, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		checkNotInPriorView("DescriptionDaoImpl.listDescriptions(Class<TYPE> type, Boolean hasImages, Boolean hasText,	Set<Feature> features, Integer pageSize, Integer pageNumber)");
		Criteria inner = null;
		
		if(clazz == null) {
			inner = getSession().createCriteria(type);
		} else {
			inner = getSession().createCriteria(clazz);
		}
		
		Criteria elementsCriteria = inner.createCriteria("descriptionElements");
		if(hasText != null) {
			if(hasText) {
				elementsCriteria.add(Restrictions.isNotEmpty("multilanguageText"));
			} else {
				elementsCriteria.add(Restrictions.isEmpty("multilanguageText"));
			}
		}
		
		if(hasImages != null) {
			if(hasImages) {
				elementsCriteria.add(Restrictions.isNotEmpty("media"));
			} else {
				elementsCriteria.add(Restrictions.isEmpty("media"));
			}
		}
		
		if(features != null && !features.isEmpty()) {
			elementsCriteria.add(Restrictions.in("feature", features));
		}
		
		inner.setProjection(Projections.distinct(Projections.id()));
		
		List<Object> intermediateResult = (List<Object>)inner.list();
		
		if(intermediateResult.isEmpty()) {
			return new ArrayList<DescriptionBase>();
		}
		
		Integer[] resultIds = new Integer[intermediateResult.size()];
		for(int i = 0; i < resultIds.length; i++) {	
				resultIds[i] = (Integer)intermediateResult.get(i);
		}
		
		Criteria outer = null;
		
		if(clazz == null) {
			outer = getSession().createCriteria(type);
		} else {
			outer = getSession().createCriteria(clazz);
		}
		
		outer.add(Restrictions.in("id", resultIds));
		addOrder(outer, orderHints);
		
		if(pageSize != null) {
			outer.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	outer.setFirstResult(pageNumber * pageSize);
		    }
		}
		
		List<DescriptionBase> results = (List<DescriptionBase>)outer.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

	public List<TaxonDescription> searchDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		checkNotInPriorView("DescriptionDaoImpl.searchDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)");
        
        Criteria criteria = getSession().createCriteria(TaxonDescription.class);
        Criteria elements = criteria.createCriteria("descriptionElements", "descriptionElement", Criteria.LEFT_JOIN);
		elements.add(Restrictions.in("area", namedAreas.toArray()));
		
		if(status != null) {
			elements.add(Restrictions.eq("status", status));
		}
		
		ProjectionList projectionList = Projections.projectionList().add(Projections.id());
		
		if(orderHints != null && !orderHints.isEmpty()) {
		    for(OrderHint orderHint : orderHints) {
			    projectionList = projectionList.add(Projections.property(orderHint.getPropertyName()));
		    }
		}
		
		criteria.setProjection(Projections.distinct(projectionList));	
		
		if(pageSize != null) {
			criteria.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	criteria.setFirstResult(pageNumber * pageSize);
		    }
		}
		
		addOrder(criteria,orderHints);
		
		List<Object> intermediateResult = (List<Object>)criteria.list();
		
		if(intermediateResult.isEmpty())
			return new ArrayList<TaxonDescription>();
		
		Integer[] resultIds = new Integer[intermediateResult.size()];
		for(int i = 0; i < resultIds.length; i++) {
			if(orderHints == null || orderHints.isEmpty()) {
				resultIds[i] = (Integer)intermediateResult.get(i);
			} else {
			  resultIds[i] = (Integer)((Object[])intermediateResult.get(i))[0];
			}
		}
		
		criteria = getSession().createCriteria(TaxonDescription.class);
		criteria.add(Restrictions.in("id", resultIds));
		addOrder(criteria,orderHints);
		
		List<TaxonDescription> results = (List<TaxonDescription>)criteria.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}
	
	public List<CommonTaxonName> searchDescriptionByCommonName(String queryString, MatchMode matchMode, Integer pageSize, Integer pageNumber) {
		
		Criteria crit = getSession().createCriteria(CommonTaxonName.class); 
		if (matchMode == MatchMode.EXACT) { 
			crit.add(Restrictions.eq("name", matchMode.queryStringFrom(queryString))); 
		} else { 
			crit.add(Restrictions.ilike("name", matchMode.queryStringFrom(queryString))); 
		} 

		if(pageSize != null) {
			crit.setMaxResults(pageSize); 
			if(pageNumber != null) {
				crit.setFirstResult(pageNumber * pageSize);
			}
		}
		List<CommonTaxonName> results = (List<CommonTaxonName>)crit.list();
		return results;
	}

	public Integer countDescriptionByCommonName(String queryString, MatchMode matchMode) {
		//TODO inprove performance
		List<CommonTaxonName> results =  searchDescriptionByCommonName(queryString, matchMode, null, null);
		return results.size();
	}
	
	@Override
	public DescriptionBase find(LSID lsid) {
		DescriptionBase descriptionBase = super.find(lsid);
		if(descriptionBase != null) {
			List<String> propertyPaths = new ArrayList<String>();
			propertyPaths.add("createdBy");
			propertyPaths.add("updatedBy");
			propertyPaths.add("taxon");
			propertyPaths.add("taxonName");
			propertyPaths.add("descriptionElements");
			propertyPaths.add("descriptionElements.createdBy");
			propertyPaths.add("descriptionElements.updatedBy");
			propertyPaths.add("descriptionElements.feature");
			propertyPaths.add("descriptionElements.multilanguageText");
			propertyPaths.add("descriptionElements.multilanguageText.language");
			propertyPaths.add("descriptionElements.area");
			propertyPaths.add("descriptionElements.status");
			propertyPaths.add("descriptionElements.modifyingText");
			propertyPaths.add("descriptionElementsmodifyingText.language");
			propertyPaths.add("descriptionElements.modifiers");
			
			defaultBeanInitializer.initialize(descriptionBase, propertyPaths);
		}
		return descriptionBase;
	}


	public <T extends DescriptionElementBase> List<T> getDescriptionElementForTaxon(
			Taxon taxon, Set<Feature> features,
			Class<T> type, Integer pageSize,
			Integer pageNumber, List<String> propertyPaths) {
			
		String queryString = "select de" +
			" from TaxonDescription as td" +
			" left join td.descriptionElements as de" +
			" where td.taxon = :taxon ";
		
		if(type != null){
			queryString += " and de.class = :type"; 
		}
		if (features != null && features.size() > 0){
			queryString += " and de.feature in (:features) "; 
		}
//		System.out.println(queryString);
		Query query = getSession().createQuery(queryString);
		
		query.setParameter("taxon", taxon);
		if(type != null){
			query.setParameter("type", type.getSimpleName());
		}
		if(features != null && features.size() > 0){
			query.setParameterList("features", features) ;
		}
		
	    if(pageSize != null) {
	    	query.setMaxResults(pageSize);
	        if(pageNumber != null) {
	        	query.setFirstResult(pageNumber * pageSize);
	        }
	    }
		    
	    List<T> results = (List<T>) query.list();
	    defaultBeanInitializer.initializeAll(results, propertyPaths);
	
    	return results; 
	}
}
