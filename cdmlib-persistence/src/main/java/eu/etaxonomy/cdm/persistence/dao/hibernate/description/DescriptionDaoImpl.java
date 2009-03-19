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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.Scope;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

@Repository 
@Qualifier("descriptionDaoImpl")
public class DescriptionDaoImpl extends IdentifiableDaoBase<DescriptionBase> implements IDescriptionDao{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DescriptionDaoImpl.class);

	public DescriptionDaoImpl() {
		super(DescriptionBase.class); 
	}

	public int countDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase status) {
		checkNotInPriorView("DescriptionDaoImpl.countDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase status)");
		Query query = null;
		
		if(status == null) {
			query = getSession().createQuery("select count(distinct description) from TaxonDescription description left join description.elements element join element.area area where area in (:namedAreas)");
		} else {
			query = getSession().createQuery("select count(distinct description) from TaxonDescription description left join description.elements element join element.area area  join element.status status where area in (:namedAreas) and status = :status");
			query.setParameter("status", status);
		}
		query.setParameterList("namedAreas", namedAreas);
		
		return ((Long)query.uniqueResult()).intValue();
	}

	public <TYPE extends DescriptionElementBase> int countDescriptionElements(DescriptionBase description, Set<Feature> features, Class<TYPE> type) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Criteria criteria = getSession().createCriteria(type);
		
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
			        AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			    
			        if(description != null) {
			    	    query.add(AuditEntity.relatedId("inDescription").eq(description.getId()));
			        }
			     
			        query.add(AuditEntity.relatedId("feature").eq(f.getId()));
			        query.addProjection(AuditEntity.id().count("id"));
			        count += ((Long)query.getSingleResult()).intValue();
			    }
			    
			    return count;
			} else {
				AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			    
		        if(description != null) {
		    	    query.add(AuditEntity.relatedId("inDescription").eq(description.getId()));
		        }
		        query.addProjection(AuditEntity.id().count("id"));
		        return ((Long)query.getSingleResult()).intValue();
			}
		}
	}

	public <TYPE extends DescriptionBase> int countDescriptions(Class<TYPE> type, Boolean hasImages, Boolean hasText, Set<Feature> features) {
		checkNotInPriorView("DescriptionDaoImpl.countDescriptions(Class<TYPE> type, Boolean hasImages, Boolean hasText, Set<Feature> features)");
		Criteria inner = getSession().createCriteria(type);
		Criteria elementsCriteria = inner.createCriteria("elements");
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

	public int countTaxonDescriptions(Taxon taxon, Set<Scope> scopes,Set<NamedArea> geographicalScopes) {
		checkNotInPriorView("DescriptionDaoImpl.countTaxonDescriptions(Taxon taxon, Set<Scope> scopes,Set<NamedArea> geographicalScopes)");
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
		
		criteria.setProjection(Projections.rowCount());
		
		return (Integer)criteria.uniqueResult();
	}

	public <TYPE extends DescriptionElementBase> List<TYPE> getDescriptionElements(DescriptionBase description, Set<Feature> features,	Class<TYPE> type, Integer pageSize, Integer pageNumber) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(type);
		
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
		
	    	return (List<TYPE>)criteria.list();
		} else {
			if(features != null && !features.isEmpty()) {
				List<TYPE> result = new ArrayList<TYPE>();
			    for(Feature f : features) {
			        AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			    
			        if(description != null) {
			    	    query.add(AuditEntity.relatedId("inDescription").eq(description.getId()));
			        }
			     
			        query.add(AuditEntity.relatedId("feature").eq(f.getId()));
			        result.addAll((List<TYPE>)query.getResultList());
			    }
			    
			    return result;
			} else {
				AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			    
		        if(description != null) {
		    	    query.add(AuditEntity.relatedId("inDescription").eq(description.getId()));
		        }
		        
		        return (List<TYPE>)query.getResultList();
			}
		}
	}

	public List<TaxonDescription> getTaxonDescriptions(Taxon taxon,	Set<Scope> scopes, Set<NamedArea> geographicalScopes,Integer pageSize, Integer pageNumber) {
		checkNotInPriorView("DescriptionDaoImpl.getTaxonDescriptions(Taxon taxon, Set<Scope> scopes, Set<NamedArea> geographicalScopes,Integer pageSize, Integer pageNumber)");
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
		
		if(pageSize != null) {
			criteria.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	criteria.setFirstResult(pageNumber * pageSize);
		    }
		}
		
		return (List<TaxonDescription>)criteria.list();
	}
	
    public List<TaxonNameDescription> getTaxonNameDescriptions(TaxonNameBase name, Integer pageSize, Integer pageNumber) {
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
	  
	      return (List<TaxonNameDescription>)criteria.list();
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
	    	return (List<TaxonNameDescription>)query.getResultList();
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
	public <TYPE extends DescriptionBase> List<TYPE> listDescriptions(Class<TYPE> type, Boolean hasImages, Boolean hasText,	Set<Feature> features, Integer pageSize, Integer pageNumber) {
		checkNotInPriorView("DescriptionDaoImpl.listDescriptions(Class<TYPE> type, Boolean hasImages, Boolean hasText,	Set<Feature> features, Integer pageSize, Integer pageNumber)");
		Criteria inner = getSession().createCriteria(type);
		Criteria elementsCriteria = inner.createCriteria("elements");
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
		
		List<Integer> ids = (List<Integer>)inner.list();
		
		if(ids.isEmpty()) {
			return new ArrayList<TYPE>();
		}
		
		Criteria outer = getSession().createCriteria(type);
		outer.add(Restrictions.in("id", ids));
		
		if(pageSize != null) {
			outer.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	outer.setFirstResult(pageNumber * pageSize);
		    }
		}
		
		return (List<TYPE>)outer.list();
	}

	public List<TaxonDescription> searchDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase status, Integer pageSize, Integer pageNumber) {
		checkNotInPriorView("DescriptionDaoImpl.searchDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase status, Integer pageSize, Integer pageNumber)");
        Query query = null;
		
		if(status == null) {
			query = getSession().createQuery("select distinct description from TaxonDescription description left join description.elements element join element.area area where area in (:namedAreas)");
		} else {
			query = getSession().createQuery("select distinct description from TaxonDescription description left join description.elements element join element.area area  join element.status status where area in (:namedAreas) and status = :status");
			query.setParameter("status", status);
		}
		query.setParameterList("namedAreas", namedAreas);
		
		if(pageSize != null) {
			query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    }
		}
		
		return (List<TaxonDescription>)query.list();
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
}
