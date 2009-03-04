/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;


public class IdentifiableDaoBase<T extends IdentifiableEntity> extends AnnotatableDaoImpl<T> implements IIdentifiableDao<T>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IdentifiableDaoBase.class);


	public IdentifiableDaoBase(Class<T> type) {
		super(type);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ITitledDao#findByTitle(java.lang.String)
	 */
	public List<T> findByTitle(String queryString) {
		return findByTitle(queryString, null);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ITitledDao#findByTitle(java.lang.String)
	 */
	public List<T> findByTitle(String queryString, CdmBase sessionObject) {
		Session session = getSession();
		if ( sessionObject != null ) {
			session.update(sessionObject);
		}
		Criteria crit = session.createCriteria(type);
		crit.add(Restrictions.ilike("persistentTitleCache", queryString));
		List<T> results = crit.list();
		return results;
	}
	
	public List<T> findByTitleAndClass(String queryString, Class<T> clazz) {
		Session session = getSession();
		Criteria crit = session.createCriteria(clazz);
		crit.add(Restrictions.ilike("persistentTitleCache", queryString));
		List<T> results = crit.list();
		return results;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ITitledDao#findByTitle(java.lang.String, boolean, int, int, java.util.List)
	 */
	public List<T> findByTitle(String queryString, MatchMode matchmode, int page, int pagesize, List<Criterion> criteria) {

		Criteria crit = getSession().createCriteria(type);
		if (matchmode == MatchMode.EXACT) {
			crit.add(Restrictions.eq("persistentTitleCache", matchmode.queryStringFrom(queryString)));
		} else {
			crit.add(Restrictions.ilike("persistentTitleCache", matchmode.queryStringFrom(queryString)));
		}
		if (pagesize >= 0) {
			crit.setMaxResults(pagesize);
		}
		if(criteria != null){
			for (Criterion criterion : criteria) {
				crit.add(criterion);
			}
		}
		crit.addOrder(Order.asc("persistentTitleCache"));
		int firstItem = (page - 1) * pagesize;
		crit.setFirstResult(firstItem);
		List<T> results = crit.list();
		return results;
	}

	public int countRights(T identifiableEntity) {
		Query query = getSession().createQuery("select count(rights) from " + type.getSimpleName() + " identifiableEntity join identifiableEntity.rights rights where identifiableEntity = :identifiableEntity");
		query.setParameter("identifiableEntity",identifiableEntity);
		return ((Long)query.uniqueResult()).intValue();
	}

	public int countSources(T identifiableEntity) {
		Query query = getSession().createQuery("select count(source) from OriginalSource source where source.sourcedObj = :identifiableEntity");
		query.setParameter("identifiableEntity",identifiableEntity);
		return ((Long)query.uniqueResult()).intValue();
	}

	public List<Rights> getRights(T identifiableEntity, Integer pageSize, Integer pageNumber) {
		Query query = getSession().createQuery("select rights from " + type.getSimpleName() + " identifiableEntity join identifiableEntity.rights rights where identifiableEntity = :identifiableEntity");
		query.setParameter("identifiableEntity",identifiableEntity);
		
		if(pageSize != null) {
	    	query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
		
		return (List<Rights>)query.list();
	}

	public List<OriginalSource> getSources(T identifiableEntity, Integer pageSize, Integer pageNumber) {
		Query query = getSession().createQuery("select source from OriginalSource source where source.sourcedObj = :identifiableEntity");
		query.setParameter("identifiableEntity",identifiableEntity);
		
		if(pageSize != null) {
	    	query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
		
		return (List<OriginalSource>)query.list();
	}

	public List<T> findOriginalSourceByIdInSource(String idInSource, String idNamespace) {
		Session session = getSession();
		Query q = session.createQuery(
                "Select c from " + type.getSimpleName() + " as c " +
                "inner join c.sources as source " +
                "where source.idInSource = :idInSource " + 
                	" AND source.idNamespace = :idNamespace"
            );
		q.setString("idInSource", idInSource);
		q.setString("idNamespace", idNamespace);
		//TODO integrate reference in where 
		List<T> results = (List<T>)q.list();
		
		return results;
	}
}
