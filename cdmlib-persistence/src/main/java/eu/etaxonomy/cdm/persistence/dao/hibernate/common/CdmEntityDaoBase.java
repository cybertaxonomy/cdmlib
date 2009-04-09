/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


/**
 * @author a.mueller
 * FIXME CdmEntityDaoBase is abstract, can it be annotated with @Repository?
 */
@Repository
public abstract class CdmEntityDaoBase<T extends CdmBase> extends DaoBase implements ICdmEntityDao<T> {
	private static final Logger logger = Logger.getLogger(CdmEntityDaoBase.class);

	int flushAfterNo = 1000; //large numbers may cause synchronisation errors when commiting the session !!
	protected Class<T> type;
	
	@Autowired
	@Qualifier("defaultBeanInitializer")
	protected BeanInitializer defaultBeanInitializer;
	
	
	public CdmEntityDaoBase(Class<T> type){
		this.type = type;
		logger.debug("Creating DAO of type [" + type.getSimpleName() + "]");
	}
	
	//TODO this method should be moved to a concrete class (not typed)
	public UUID saveCdmObj(CdmBase cdmObj) throws DataAccessException  {
		getSession().saveOrUpdate(cdmObj);
		return cdmObj.getUuid();
	}
	
    //TODO: Replace saveCdmObj() by saveCdmObject_
	private UUID saveCdmObject_(T cdmObj){
		getSession().saveOrUpdate(cdmObj);
		return cdmObj.getUuid();
	}
	
    //TODO: Use everywhere CdmEntityDaoBase.saveAll() instead of ServiceBase.saveCdmObjectAll()?
	public Map<UUID, T> saveAll(Collection<T> cdmObjCollection){
		int types = cdmObjCollection.getClass().getTypeParameters().length;
		if (types > 0){
			if (logger.isDebugEnabled()){logger.debug("ClassType: + " + cdmObjCollection.getClass().getTypeParameters()[0]);}
		}

		Map<UUID, T> resultMap = new HashMap<UUID, T>();
		Iterator<T> iterator = cdmObjCollection.iterator();
		int i = 0;
		while(iterator.hasNext()){
			if ( ( (i % 2000) == 0) && (i > 0)   ){logger.debug("Saved " + i + " objects" );}
			T cdmObj = iterator.next();
			UUID uuid = saveCdmObject_(cdmObj);
			if (logger.isDebugEnabled()){logger.debug("Save cdmObj: " + (cdmObj == null? null: cdmObj.toString()));}
			resultMap.put(uuid, cdmObj);
			i++;
			if ( (i % flushAfterNo) == 0){
				try{
					logger.debug("flush");
					flush();
				}catch(Exception e){
					logger.error("UUUIIIII");
					e.printStackTrace();
				}
			}
		}

		if ( logger.isInfoEnabled() ){logger.info("Saved " + i + " objects" );}
		return resultMap;
	}

	
	public UUID saveOrUpdate(T transientObject) throws DataAccessException  {
		try {
			if (logger.isDebugEnabled()){logger.debug("dao saveOrUpdate start...");}
			if (logger.isDebugEnabled()){logger.debug("transientObject(" + transientObject.getClass().getSimpleName() + ") ID:" + transientObject.getId() + ", UUID: " + transientObject.getUuid()) ;}
			Session session = getSession();
			session.saveOrUpdate(transientObject);
			if (logger.isDebugEnabled()){logger.debug("dao saveOrUpdate end");}
			return transientObject.getUuid();
		} catch (NonUniqueObjectException e) {
			logger.error("Error in CdmEntityDaoBase.saveOrUpdate(obj)");
			logger.error(e.getIdentifier());
			logger.error(e.getEntityName());
			logger.error(e.getMessage());
			e.printStackTrace();
			throw e;
		} catch (HibernateException e) {
			
			e.printStackTrace();
			throw e;
		}
	}

	public UUID save(T newInstance) throws DataAccessException {
		getSession().save(newInstance);
		return newInstance.getUuid();
	}
	
	public UUID update(T transientObject) throws DataAccessException {
		getSession().update(transientObject);
		return transientObject.getUuid();
	}
	
	public UUID refresh(T persistentObject) throws DataAccessException {
		getSession().refresh(persistentObject);
		return persistentObject.getUuid();
	}
	
	public UUID delete(T persistentObject) throws DataAccessException {
		getSession().delete(persistentObject);
		return persistentObject.getUuid();
	}

	public T findById(int id) throws DataAccessException {
		return (T) getSession().get(type, id);
	}

	public T findByUuid(UUID uuid) throws DataAccessException{
		Session session = getSession();
		Criteria crit = session.createCriteria(type);
		crit.add(Restrictions.eq("uuid", uuid));
		crit.addOrder(Order.desc("created"));
		List<T> results = crit.list();
		if (results.isEmpty()){
			return null;
		}else{
			return results.get(0);			
		}
	}
	
	public T load(UUID uuid) {
		T bean = findByUuid(uuid);
		if(bean == null) 
			return null;
		defaultBeanInitializer.load(bean);
		
		return bean;
	}
	

	public T load(UUID uuid, List<String> propertyPaths){
		T bean = findByUuid(uuid);
		if(bean == null) 
			return bean;
		
		defaultBeanInitializer.initialize(bean, propertyPaths);
		
		return bean;
	}
	
	public Boolean exists(UUID uuid) {
		if (findByUuid(uuid)==null){
			return false;
		}
		return true;
	}
	
	public int count() {
		return count(type);
	}
	
	public <TYPE extends T> int count(Class<TYPE> type) {
		Session session = getSession();
		Criteria crit = session.createCriteria(type);
		crit.setProjection(Projections.projectionList().add(Projections.rowCount()));
		Integer nbrRows = (Integer) crit.uniqueResult();
		return nbrRows.intValue();
	}

	public List<T> list(Integer limit, Integer start) {
		return list(limit, start, null); 
	}
	
	public <TYPE extends T> List<TYPE> list(Integer limit, Integer start, List<OrderHint> orderHints) {
		
		Criteria crit = getSession().createCriteria(type); 
		if(limit != null) {
		    crit.setFirstResult(start);
		    crit.setMaxResults(limit);
		}
		if(orderHints != null){
			for(OrderHint orderHint : orderHints){
				Order order;
				String assocObj = null, propname;
				int pos;
				if((pos = orderHint.getPropertyName().indexOf('.', 0)) >= 0){
					assocObj = orderHint.getPropertyName().substring(0, pos);
					propname = orderHint.getPropertyName().substring(pos + 1);
				} else {
					propname = orderHint.getPropertyName();
				}
				if(orderHint.isAscending()){
					order = Order.asc(propname);					
				} else {
					order = Order.desc(propname);
				}
				if(assocObj != null){
					crit.createCriteria(assocObj).addOrder(order);
				} else {
					crit.addOrder(order);				
				}
			}
		}
		return crit.list(); 
	}
	
	
	
	public List<T> list(Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
		List<T> entities = list(limit, start, orderHints);
		defaultBeanInitializer.initializeAll(entities, propertyPaths);
		return entities;
	}

	public <TYPE extends T> List<TYPE> list(Class<TYPE> type, Integer limit, Integer start) {
		Criteria crit = getSession().createCriteria(type); 
		if(limit != null) {
		    crit.setFirstResult(start);
		    crit.setMaxResults(limit);
		}
		return crit.list(); 
	}
	
	public List<T> rows(String tableName, int limit, int start) {
		Query query = getSession().createQuery("from " + tableName + " order by uuid");
		query.setFirstResult(start);
		query.setMaxResults(limit);
		List<T> result = query.list();
		return result;
	}
	
	public Class<T> getType() {
		return type;
	}
	
	protected void setPagingParameter(Query query, Integer pageSize, Integer pageNumber){
		if(pageSize != null) {
	    	query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
	}
	
	protected void setPagingParameter(AuditQuery query, Integer pageSize, Integer pageNumber){
		if(pageSize != null) {
	    	query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
	}
}
