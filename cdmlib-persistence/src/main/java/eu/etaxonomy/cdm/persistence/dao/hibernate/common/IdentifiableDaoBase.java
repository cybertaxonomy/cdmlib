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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Credit;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
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
	public List<T> findByTitle(String queryString, MatchMode matchmode, int page, int pagesize, List<Criterion> criteria) {
		checkNotInPriorView("IdentifiableDaoBase.findByTitle(String queryString, MATCH_MODE matchmode, int page, int pagesize, List<Criterion> criteria)");
		Criteria crit = getSession().createCriteria(type);
		if (matchmode == MatchMode.EXACT) {
			crit.add(Restrictions.eq("titleCache", matchmode.queryStringFrom(queryString)));
		} else {
//			crit.add(Restrictions.ilike("titleCache", matchmode.queryStringFrom(queryString)));
			crit.add(Restrictions.like("titleCache", matchmode.queryStringFrom(queryString)));
		}
		if (pagesize >= 0) {
			crit.setMaxResults(pagesize);
		}
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

	public List<Rights> getRights(T identifiableEntity, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		checkNotInPriorView("IdentifiableDaoBase.getRights(T identifiableEntity, Integer pageSize, Integer pageNumber, List<String> propertyPaths)");
		Query query = getSession().createQuery("select rights from " + type.getSimpleName() + " identifiableEntity join identifiableEntity.rights rights where identifiableEntity = :identifiableEntity");
		query.setParameter("identifiableEntity",identifiableEntity);
		setPagingParameter(query, pageSize, pageNumber);
		List<Rights> results = (List<Rights>)query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}
	
	public List<Credit> getCredits(T identifiableEntity, Integer pageSize, Integer pageNumber) {
		checkNotInPriorView("IdentifiableDaoBase.getCredits(T identifiableEntity, Integer pageSize, Integer pageNumber)");
		Query query = getSession().createQuery("select credits from " + type.getSimpleName() + " identifiableEntity join identifiableEntity.credits credits where identifiableEntity = :identifiableEntity");
		query.setParameter("identifiableEntity",identifiableEntity);
		setPagingParameter(query, pageSize, pageNumber);
		return (List<Credit>)query.list();
	}

	public List<IdentifiableSource> getSources(T identifiableEntity, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		checkNotInPriorView("IdentifiableDaoBase.getSources(T identifiableEntity, Integer pageSize, Integer pageNumber)");
		Query query = getSession().createQuery("select source from OriginalSourceBase source where source.sourcedObj.id = :id and source.sourcedObj.class = :class");
		query.setParameter("id",identifiableEntity.getId());
		query.setParameter("class",identifiableEntity.getClass().getName());
		setPagingParameter(query, pageSize, pageNumber);
		List<IdentifiableSource> results = (List<IdentifiableSource>)query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
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
	
	public List<UuidAndTitleCache<T>> getUuidAndTitleCache(){
		List<UuidAndTitleCache<T>> list = new ArrayList<UuidAndTitleCache<T>>();
		Session session = getSession();
		
		Query query = session.createQuery("select uuid, titleCache from " + type.getSimpleName());
		
		List<Object[]> result = query.list();
		
		for(Object[] object : result){
			list.add(new UuidAndTitleCache<T>(type, (UUID) object[0], (String) object[1]));
		}
		
		return list;
	}
}
