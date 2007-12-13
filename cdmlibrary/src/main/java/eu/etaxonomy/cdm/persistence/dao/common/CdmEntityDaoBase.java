/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.common;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;


/**
 * @author a.mueller
 *
 */
public abstract class CdmEntityDaoBase<T extends CdmBase> extends DaoBase implements ICdmEntityDao<T> {

	static Logger logger = Logger.getLogger(CdmEntityDaoBase.class);

	protected Class<T> type;
	
	public CdmEntityDaoBase(Class<T> type){
		this.type = type;
		logger.debug("Creating DAO of type [" + type.getSimpleName() + "]");
	}
	
	
	
	public String saveCdmObj(CdmBase cdmObj) throws DataAccessException  {
		getSession().saveOrUpdate(cdmObj);
		return cdmObj.getUuid();
	}

	public String saveOrUpdate(T transientObject) throws DataAccessException  {
		getSession().saveOrUpdate(transientObject);
		return transientObject.getUuid();
	}

	public String save(T newInstance) throws DataAccessException {
		getSession().save(newInstance);
		return newInstance.getUuid();
	}
	
	public String update(T transientObject) throws DataAccessException {
		getSession().update(transientObject);
		return transientObject.getUuid();
	}
	
	public String delete(T persistentObject) throws DataAccessException {
		getSession().delete(persistentObject);
		return persistentObject.getUuid();
	}

	public T findById(int id) throws DataAccessException {
		return (T) getSession().get(type, id);
	}

	public T findByUuid(String uuid) throws DataAccessException{
		Session session = getSession();
//	logger.debug("BEGIN TRANSACTION");
//	session.beginTransaction();
	Transaction tx = session.getTransaction();
	
		Criteria crit = session.createCriteria(type);
		crit.add(Restrictions.eq("uuid", uuid));
		crit.addOrder(Order.desc("created"));
		List<T> results = crit.list();
//	logger.debug("COMMIT TRANSACTION");
//	tx.commit();
	Object o = tx;
		if (results.isEmpty()){
			return null;
		}else{
			return results.get(0);			
		}
	}
	
	public Boolean exists(String uuid) {
		if (findByUuid(uuid)==null){
			return false;
		}
		return true;
	}

	public List<T> list(int limit, int start) {
		Criteria crit = getSession().createCriteria(type); 
		crit.setFirstResult(start);
		crit.setMaxResults(limit);
		return crit.list(); 
	}

	public List<Object> executeHsql(String hsql){
		Query q = getSession().createQuery(hsql);
		return q.list();
	}
	
}
