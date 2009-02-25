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
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.impl.AbstractQueryImpl;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;


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
		/**
		 *  FIXME why do we need to call update in a find* method? I don't know for sure 
		 *  that this is a good idea . . . 
		 */
		Session session = getSession();
		if ( sessionObject != null ) {
			session.update(sessionObject);
		}
		checkNotInPriorView("IdentifiableDaoBase.findByTitle(String queryString, CdmBase sessionObject)");
		Criteria crit = session.createCriteria(type);
		crit.add(Restrictions.ilike("titleCache", queryString));
		List<T> results = crit.list();
		return results;
	}
	
	public List<T> findByTitleAndClass(String queryString, Class<T> clazz) {
		checkNotInPriorView("IdentifiableDaoBase.findByTitleAndClass(String queryString, Class<T> clazz)");
		Criteria crit = getSession().createCriteria(clazz);
		crit.add(Restrictions.ilike("titleCache", queryString));
		List<T> results = crit.list();
		return results;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ITitledDao#findByTitle(java.lang.String, boolean, int, int, java.util.List)
	 */
	public List<T> findByTitle(String queryString, MATCH_MODE matchmode, int page, int pagesize, List<Criterion> criteria) {
		checkNotInPriorView("IdentifiableDaoBase.findByTitle(String queryString, MATCH_MODE matchmode, int page, int pagesize, List<Criterion> criteria)");
		Criteria crit = getSession().createCriteria(type);
		crit.add(Restrictions.ilike("titleCache", matchmode.queryStringFrom(queryString)));
		crit.setMaxResults(pagesize);
		if(criteria != null){
			for (Criterion criterion : criteria) {
				crit.add(criterion);
			}
		}
		crit.addOrder(Order.asc("titleCache"));
		int firstItem = (page - 1) * pagesize;
		crit.setFirstResult(firstItem);
		List<T> results = crit.list();
		return results;
	}

	public int countRights(T identifiableEntity) {
		checkNotInPriorView("IdentifiableDaoBase.countRights(T identifiableEntity)");
		Query query = getSession().createQuery("select count(rights) from " + type.getSimpleName() + " identifiableEntity join identifiableEntity.rights rights where identifiableEntity = :identifiableEntity");
		query.setParameter("identifiableEntity",identifiableEntity);
		return ((Long)query.uniqueResult()).intValue();
	}

	public int countSources(T identifiableEntity) {
		checkNotInPriorView("IdentifiableDaoBase.countSources(T identifiableEntity)");
		Query query = getSession().createQuery("select count(source) from OriginalSource source where source.sourcedObj = :identifiableEntity");
		query.setParameter("identifiableEntity",identifiableEntity);
		return ((Long)query.uniqueResult()).intValue();
	}

	public List<Rights> getRights(T identifiableEntity, Integer pageSize, Integer pageNumber) {
		checkNotInPriorView("IdentifiableDaoBase.getRights(T identifiableEntity, Integer pageSize, Integer pageNumber)");
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
		checkNotInPriorView("IdentifiableDaoBase.getSources(T identifiableEntity, Integer pageSize, Integer pageNumber)");
		Query query = getSession().createQuery("select source from OriginalSource source where source.sourcedObj.id = :id and source.sourcedObj.class = :class");
		query.setParameter("id",identifiableEntity.getId());
		query.setParameter("class",identifiableEntity.getClass().getName());
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
		checkNotInPriorView("IdentifiableDaoBase.findOriginalSourceByIdInSource(String idInSource, String idNamespace)");
		Query query = getSession().createQuery(
                "Select c from " + type.getSimpleName() + " as c " +
                "inner join c.sources as source " +
                "where source.idInSource = :idInSource " + 
                	" AND source.idNamespace = :idNamespace"
            );
		query.setString("idInSource", idInSource);
		query.setString("idNamespace", idNamespace);
		//TODO integrate reference in where		
		return (List<T>)query.list();
	}

	public T find(LSID lsid) {
		checkNotInPriorView("IdentifiableDaoBase.find(LSID lsid)");
		Criteria criteria = getSession().createCriteria(type);
		criteria.add(Restrictions.eq("lsid.authority", lsid.getAuthority()));
		criteria.add(Restrictions.eq("lsid.namespace", lsid.getNamespace()));
		criteria.add(Restrictions.eq("lsid.object", lsid.getObject()));
		
		if(lsid.getRevision() != null) {
			criteria.add(Restrictions.eq("lsid.revision", lsid.getRevision()));
		}
		
		T object = (T)criteria.uniqueResult();
		if(object != null) {
			return object;
		} else {
			AuditQuery query = getAuditReader().createQuery().forRevisionsOfEntity(type, false, true);
			query.add(AuditEntity.property("lsid_authority").eq(lsid.getAuthority()));
			query.add(AuditEntity.property("lsid_namespace").eq(lsid.getNamespace()));
			query.add(AuditEntity.property("lsid_object").eq(lsid.getObject()));
			
			if(lsid.getRevision() != null) {
				query.add(AuditEntity.property("lsid_revision").eq(lsid.getRevision()));
			}
			
			query.addOrder(AuditEntity.revisionNumber().asc());
			query.setMaxResults(1);
			query.setFirstResult(0);
			List<Object[]> objs = (List<Object[]>)query.getResultList();
			if(objs.isEmpty()) {
				return null;
			} else {
				return (T)objs.get(0)[0];
			}
		}
	}
}
