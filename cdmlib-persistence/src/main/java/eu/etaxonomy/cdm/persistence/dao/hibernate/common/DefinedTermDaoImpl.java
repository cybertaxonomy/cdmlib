/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.kohlbecker
 * @created 29.05.2008
 * @version 1.0
 */
@Repository
public class DefinedTermDaoImpl extends VersionableDaoBase<DefinedTermBase> implements IDefinedTermDao{
	private static final Logger logger = Logger.getLogger(DefinedTermDaoImpl.class);

	public DefinedTermDaoImpl() {
		super(DefinedTermBase.class);
	}

	public List<DefinedTermBase> findByTitle(String queryString) {
		return findByTitle(queryString, null);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ITitledDao#findByTitle(java.lang.String, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public List<DefinedTermBase> findByTitle(String queryString, CdmBase sessionObject) {
		checkNotInPriorView("DefinedTermDaoImpl.findByTitle(String queryString, CdmBase sessionObject)");
		Session session = getSession();
		if ( sessionObject != null ) {// FIXME is this needed?
			session.update(sessionObject);
		}
		Query query = session.createQuery("select term from DefinedTermBase term join fetch term.representations representation where representation.label = :label");
		query.setParameter("label", queryString);
		return (List<DefinedTermBase>) query.list();

	}

	public List<DefinedTermBase> findByTitleAndClass(String queryString, Class<DefinedTermBase> clazz) {
		checkNotInPriorView("DefinedTermDaoImpl.findByTitleAndClass(String queryString, Class<DefinedTermBase> clazz)");
		Session session = getSession();
		Criteria crit = session.createCriteria(clazz);
		crit.add(Restrictions.ilike("persistentTitleCache", queryString));
		List<DefinedTermBase> results = crit.list();
		return results;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ITitledDao#findByTitle(java.lang.String, eu.etaxonomy.cdm.persistence.dao.common.ITitledDao.MATCH_MODE, int, int, java.util.List)
	 */
	public List<DefinedTermBase> findByTitle(String queryString, MatchMode matchMode, int page, int pagesize, List<Criterion> criteria) {
		//FIXME is query parametrised?
		checkNotInPriorView("DefinedTermDaoImpl.findByTitle(String queryString, ITitledDao.MATCH_MODE matchMode, int page, int pagesize, List<Criterion> criteria)");
		Criteria crit = getSession().createCriteria(type);
		crit.add(Restrictions.ilike("titleCache", matchMode.queryStringFrom(queryString)));
		crit.setMaxResults(pagesize);
		int firstItem = (page - 1) * pagesize + 1;
		crit.setFirstResult(firstItem);
		List<DefinedTermBase> results = crit.list();
		return results;
	}
	

	public WaterbodyOrCountry getCountryByIso(String iso639) {
		// If iso639 = "" query returns non-unique result. We prevent this here:
		if (iso639.equals("") ) { return null; }
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		  Query query = getSession().createQuery("from WaterbodyOrCountry where iso3166_A2 = :isoCode"); 
		  query.setParameter("isoCode", iso639);
		  return (WaterbodyOrCountry) query.uniqueResult();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(WaterbodyOrCountry.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("iso3166_A2").eq(iso639));
			return (WaterbodyOrCountry) query.getSingleResult();
		}
	}
	
	public <T extends DefinedTermBase> List<T> getDefinedTermByRepresentationText(String text, Class<T> clazz ) {
		Query query = getSession().createQuery("from "+ clazz.getName()+" as wc where wc.representations.text like '"+text+"'"); 
		return (List<T>) query.list();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao#getLangaugeByIso(java.lang.String)
	 */
	public Language getLanguageByIso(String iso639) {
		if (iso639.length() < 2 || iso639.length() > 3) {
			logger.warn("Invalid length " + iso639.length() + " of ISO code. Length must be 2 or 3.");
			return null;
		}
		String isoStandart = "iso639_" + (iso639.length() - 1);
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("from Language where " + isoStandart + "= :isoCode"); 
		    query.setParameter("isoCode", iso639);
		    return (Language) query.uniqueResult();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Language.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property(isoStandart).eq(iso639));
			return (Language)query.getSingleResult();
		}
	}
	
	/**
	 *  FIXME this will result in a query per language - could you, given that iso codes
	 *  are unique, use from Language where iso639_1 in (:isoCode) or iso639_2 in (:isoCode)
	 */
	public List<Language> getLanguagesByIso(List<String> iso639List) {
		List<Language> languages = new ArrayList<Language>(iso639List.size());
		for (String iso639 : iso639List) {
			languages.add(getLanguageByIso(iso639));
		}
		return languages;
	}
	
	public List<Language> getLanguagesByLocale(Enumeration<Locale> locales) {
		List<Language> languages = new ArrayList<Language>();
		while(locales.hasMoreElements()) {
			Locale locale = locales.nextElement();
			languages.add(getLanguageByIso(locale.getLanguage()));		
		}
		return languages;
	}

	public int count(NamedAreaLevel level, NamedAreaType type) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Criteria criteria = getSession().createCriteria(NamedArea.class);
		
		    if(level != null) {
			    criteria.add(Restrictions.eq("level",level));
		    }
		
		    if(type != null) {
			    criteria.add(Restrictions.eq("type", type));
		    }
		
		    criteria.setProjection(Projections.rowCount());
		
		    return (Integer)criteria.uniqueResult();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(NamedArea.class,auditEvent.getRevisionNumber());
			
			if(level != null) {
				query.add(AuditEntity.relatedId("level").eq(level.getId()));
		    }
		
		    if(type != null) {
		    	query.add(AuditEntity.relatedId("type").eq(type.getId()));
		    }
		    query.addProjection(AuditEntity.id().count("id"));
		    return ((Long)query.getSingleResult()).intValue();
		}
	}

	public int countMedia(DefinedTermBase definedTerm) {
		checkNotInPriorView("DefinedTermDaoImpl.countMedia(DefinedTermBase definedTerm)");
		Query query = getSession().createQuery("select count(media) from DefinedTermBase definedTerm join definedTerm.media media where definedTerm = :definedTerm");
	    query.setParameter("definedTerm", definedTerm);
	    
		return ((Long)query.uniqueResult()).intValue();
	}

	public List<Media> getMedia(DefinedTermBase definedTerm, Integer pageSize,	Integer pageNumber) {
		checkNotInPriorView("DefinedTermDaoImpl.getMedia(DefinedTermBase definedTerm, Integer pageSize,	Integer pageNumber)");
		Query query = getSession().createQuery("select media from DefinedTermBase definedTerm join definedTerm.media media where definedTerm = :definedTerm");
		query.setParameter("definedTerm", definedTerm);
		
		if(pageSize != null) {
			query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    }
		}
		
		return (List<Media>)query.list();
	}

	public List<NamedArea> list(NamedAreaLevel level, NamedAreaType type, Integer pageSize, Integer pageNumber) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(NamedArea.class);
		
		    if(level != null) {
			    criteria.add(Restrictions.eq("level",level));
		    }
		
		    if(type != null) {
			    criteria.add(Restrictions.eq("type", type));
		    }
		
		    if(pageSize != null) {
			    criteria.setMaxResults(pageSize);
		        if(pageNumber != null) {
		    	    criteria.setFirstResult(pageNumber * pageSize);
		        }
		    }
		
		    return (List<NamedArea>)criteria.list();
		} else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(NamedArea.class,auditEvent.getRevisionNumber());
			
			if(level != null) {
				query.add(AuditEntity.relatedId("level").eq(level.getId()));
		    }
		
		    if(type != null) {
		    	query.add(AuditEntity.relatedId("type").eq(type.getId()));
		    }
		  
		    return (List<NamedArea>)query.getResultList();
		}
	}

	public <T extends DefinedTermBase> int countGeneralizationOf(T kindOf) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("select count(term) from DefinedTermBase term where term.kindOf = :kindOf");
		    query.setParameter("kindOf", kindOf);
		    return ((Long)query.uniqueResult()).intValue();
		} else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(DefinedTermBase.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("kindOf").eq(kindOf.getId()));
		    query.addProjection(AuditEntity.id().count("id"));
		    return ((Long)query.getSingleResult()).intValue();
		}
	}

	public <T extends DefinedTermBase> int countIncludes(Set<T> partOf) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
    		Query query = getSession().createQuery("select count(term) from DefinedTermBase term where term.partOf in (:partOf)");
	    	query.setParameterList("partOf", partOf);
		    return ((Long)query.uniqueResult()).intValue();
		} else {
			Integer count = 0;
			for(T t : partOf) {
				AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(DefinedTermBase.class,auditEvent.getRevisionNumber());
				query.add(AuditEntity.relatedId("partOf").eq(t.getId()));
			    query.addProjection(AuditEntity.id().count("id"));
			    count += ((Long)query.getSingleResult()).intValue();
			}
			return count;
		}
	}

	public <T extends DefinedTermBase> List<T> getGeneralizationOf(T kindOf, Integer pageSize, Integer pageNumber) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("select term from DefinedTermBase term where term.kindOf = :kindOf");
		    query.setParameter("kindOf", kindOf);
		
		    if(pageSize != null) {
			    query.setMaxResults(pageSize);
		        if(pageNumber != null) {
		    	    query.setFirstResult(pageNumber * pageSize);
		        }
		    }
		
	        return (List<T>)query.list();
		} else {
			 AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(DefinedTermBase.class,auditEvent.getRevisionNumber());
			 query.add(AuditEntity.relatedId("kindOf").eq(kindOf.getId()));
			
			 if(pageSize != null) {
			    query.setMaxResults(pageSize);
			     if(pageNumber != null) {
			  	    query.setFirstResult(pageNumber * pageSize);
			     }
			 }
			 
			 return (List<T>)query.getResultList();
		}
	}

	public <T extends DefinedTermBase> List<T> getIncludes(Set<T> partOf,	Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
    		Query query = getSession().createQuery("select term from DefinedTermBase term where term.partOf in (:partOf)");
	    	query.setParameterList("partOf", partOf);
		 
		    if(pageSize != null) {
			    query.setMaxResults(pageSize);
		        if(pageNumber != null) {
		    	    query.setFirstResult(pageNumber * pageSize);
		        }
		    }
		    
		    List<T> results = (List<T>)query.list();
		    defaultBeanInitializer.initializeAll(results, propertyPaths);
		    return results; 
		} else {
			List<T> result = new ArrayList<T>();
			for(T t : partOf) {
				AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(DefinedTermBase.class,auditEvent.getRevisionNumber());
				query.add(AuditEntity.relatedId("partOf").eq(t.getId()));
				if(pageSize != null) {
				    query.setMaxResults(pageSize);
			        if(pageNumber != null) {
			    	    query.setFirstResult(pageNumber * pageSize);
			        }
			    }			   

			    result.addAll((List<T>)query.getResultList());
			}
			defaultBeanInitializer.initializeAll(result, propertyPaths);
			return result;
		}
	}
	
	public <T extends DefinedTermBase> int countPartOf(Set<T> definedTerms) {
		checkNotInPriorView("DefinedTermDaoImpl.countPartOf(Set<T> definedTerms)");
		Query query = getSession().createQuery("select count(distinct definedTerm) from DefinedTermBase definedTerm join definedTerm.includes included where included in (:definedTerms)");
		query.setParameterList("definedTerms", definedTerms);
		return ((Long)query.uniqueResult()).intValue();
	}

	public <T extends DefinedTermBase> List<T> getPartOf(Set<T> definedTerms, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		checkNotInPriorView("DefinedTermDaoImpl.getPartOf(Set<T> definedTerms, Integer pageSize, Integer pageNumber)");
		Query query = getSession().createQuery("select distinct definedTerm from DefinedTermBase definedTerm join definedTerm.includes included where included in (:definedTerms)");
		query.setParameterList("definedTerms", definedTerms);
		
		if(pageSize != null) {
			query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    }
		}
		List<T> r = (List<T>)query.list();
		/**
		 * For some weird reason, hibernate returns proxies (extending the superclass), not the actual class on this, 
		 * despite querying the damn database and returning the discriminator along with the rest of the object properties!
		 * 
		 * Probably a bug in hibernate, but we'll manually deproxy for now since the objects are initialized anyway, the 
		 * performance implications are small (we're swapping one array of references for another, not hitting the db or 
		 * cache). 
		 */
		List<T> results = new ArrayList<T>();
		if(!definedTerms.isEmpty()) {
		    Class<T> type = (Class<T>)definedTerms.iterator().next().getClass();
		    for(T t : r) {
			    results.add(CdmBase.deproxy(t, type));
		    }
		    defaultBeanInitializer.initializeAll(results, propertyPaths);
		}
		return results;
	}

	public DefinedTermBase findByUri(String uri) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("select term from DefinedTermBase term where term.uri = :uri");
		    query.setParameter("uri", uri);
		    return (DefinedTermBase)query.uniqueResult();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(DefinedTermBase.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("uri").eq(uri));
		    return (DefinedTermBase)query.getSingleResult();
		}
	}


//	@Override
//	public List<DefinedTermBase> list(int limit, int start) {
//		Query query = getSession().createQuery("select term from DefinedTermBase term join fetch term.representations representation ");
//		query.setMaxResults(limit);
//		query.setFirstResult(start);
//		return (List<DefinedTermBase>) query.list();
//	}

}
