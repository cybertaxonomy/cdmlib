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
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;

/**
 * @author a.kohlbecker
 * @created 29.05.2008
 * @version 1.0
 */
@Repository
public class DefinedTermDaoImpl extends CdmEntityDaoBase<DefinedTermBase> implements IDefinedTermDao{
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
		Session session = getSession();
		if ( sessionObject != null ) {
			session.update(sessionObject);
		}
		Query query = session.createQuery("select term from DefinedTermBase term join fetch term.representations representation where representation.label = :label");
		query.setParameter("label", queryString);
		return (List<DefinedTermBase>) query.list();

	}

	public List<DefinedTermBase> findByTitleAndClass(String queryString, Class<DefinedTermBase> clazz) {
		Session session = getSession();
		Criteria crit = session.createCriteria(clazz);
		crit.add(Restrictions.ilike("persistentTitleCache", queryString));
		List<DefinedTermBase> results = crit.list();
		return results;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ITitledDao#findByTitle(java.lang.String, eu.etaxonomy.cdm.persistence.dao.common.ITitledDao.MATCH_MODE, int, int, java.util.List)
	 */
	public List<DefinedTermBase> findByTitle(String queryString, ITitledDao.MATCH_MODE matchMode, int page, int pagesize, List<Criterion> criteria) {
		//FXIME is query parametrised?
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
		
		Query query = getSession().createQuery("from WaterbodyOrCountry where iso3166_A2 = :isoCode"); 
		query.setParameter("isoCode", iso639);
		return (WaterbodyOrCountry) query.uniqueResult();
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
		Query query = getSession().createQuery("from Language where " + isoStandart + "= :isoCode"); 
		query.setParameter("isoCode", iso639);
		return (Language) query.uniqueResult();
	}
	
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
		Criteria criteria = getSession().createCriteria(NamedArea.class);
		
		if(level != null) {
			criteria.add(Restrictions.eq("level",level));
		}
		
		if(type != null) {
			criteria.add(Restrictions.eq("type", type));
		}
		
		criteria.setProjection(Projections.rowCount());
		
		return (Integer)criteria.uniqueResult();
	}

	public int countMedia(DefinedTermBase definedTerm) {
		Query query = getSession().createQuery("select count(media) from DefinedTermBase definedTerm join definedTerm.media media where definedTerm = :definedTerm");
	    query.setParameter("definedTerm", definedTerm);
	    
		return ((Long)query.uniqueResult()).intValue();
	}

	public List<Media> getMedia(DefinedTermBase definedTerm, Integer pageSize,	Integer pageNumber) {
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
	}

	public <T extends DefinedTermBase> int countGeneralizationOf(T definedTerm) {
		Query query = getSession().createQuery("select count(specialization) from DefinedTermBase generalization join generalization.generalizationOf specialization where generalization = :generalization");
		query.setParameter("generalization", definedTerm);
		return ((Long)query.uniqueResult()).intValue();
	}

	public <T extends DefinedTermBase> int countIncludes(Set<T> definedTerms) {
		Query query = getSession().createQuery("select count(included) from DefinedTermBase definedTerm join definedTerm.includes included where definedTerm in (:definedTerms)");
		query.setParameterList("definedTerms", definedTerms);
		return ((Long)query.uniqueResult()).intValue();
	}

	public <T extends DefinedTermBase> List<T> getGeneralizationOf(T definedTerm, Integer pageSize, Integer pageNumber) {
		Query query = getSession().createQuery("select specialization from DefinedTermBase generalization join generalization.generalizationOf specialization where generalization = :generalization");
		query.setParameter("generalization", definedTerm);
		
		if(pageSize != null) {
			query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    }
		}
		
		return (List<T>)query.list();
	}

	public <T extends DefinedTermBase> List<T> getIncludes(Set<T> definedTerms,	Integer pageSize, Integer pageNumber) {
		Query query = getSession().createQuery("select included from DefinedTermBase definedTerm join definedTerm.includes included where definedTerm in (:definedTerms)");
		query.setParameterList("definedTerms", definedTerms);
		
		if(pageSize != null) {
			query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    }
		}
		
		return (List<T>)query.list();
	}
	
	public <T extends DefinedTermBase> int countPartOf(Set<T> definedTerms) {
		Query query = getSession().createQuery("select count(distinct partOf) from DefinedTermBase definedTerm join definedTerm.partOf partOf where definedTerm in (:definedTerms)");
		query.setParameterList("definedTerms", definedTerms);
		return ((Long)query.uniqueResult()).intValue();
	}

	public <T extends DefinedTermBase> List<T> getPartOf(Set<T> definedTerms, Integer pageSize, Integer pageNumber) {
		Query query = getSession().createQuery("select distinct partOf from DefinedTermBase definedTerm join definedTerm.partOf partOf where definedTerm in (:definedTerms)");
		query.setParameterList("definedTerms", definedTerms);
		
		if(pageSize != null) {
			query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    }
		}
		
		return (List<T>)query.list();
	}

	public DefinedTermBase findByUri(String uri) {
		Query query = getSession().createQuery("select term from DefinedTermBase term where term.uri = :uri");
		query.setParameter("uri", uri);
		return (DefinedTermBase)query.uniqueResult();
	}


//	@Override
//	public List<DefinedTermBase> list(int limit, int start) {
//		Query query = getSession().createQuery("select term from DefinedTermBase term join fetch term.representations representation ");
//		query.setMaxResults(limit);
//		query.setFirstResult(start);
//		return (List<DefinedTermBase>) query.list();
//	}

}
