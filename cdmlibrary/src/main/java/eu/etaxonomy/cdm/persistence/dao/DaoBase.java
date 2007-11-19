/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.orm.hibernate3.HibernateTemplate;


/**
 * @author a.mueller
 *
 */
public abstract class DaoBase<T, ID extends Serializable> 
		extends HibernateDaoSupport implements IDao<T, ID> {

	static Logger logger = Logger.getLogger(DaoBase.class);

	protected Class<T> type;
	
	public DaoBase(Class<T> type){
		this.type = type;
	}
	

	public void saveOrUpdate(T transientObject) throws DataAccessException  {
		HibernateTemplate ht = getHibernateTemplate();
		ht.saveOrUpdate(transientObject);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#save(java.lang.Object)
	 */
	public ID save(T newInstance) throws DataAccessException  {
		HibernateTemplate ht = getHibernateTemplate();
		return (ID)ht.save(newInstance);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#update(java.lang.Object)
	 */
	public void update(T transientObject) throws DataAccessException {
		//TODO update kommt in einem O/R-Mapping eigentlich gar nich vor!!
		getHibernateTemplate().update(transientObject);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#delete(java.lang.Object)
	 */
	public void delete(T persistentObject) throws DataAccessException {
		getHibernateTemplate().delete(persistentObject);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#findById(java.io.Serializable)
	 */
	public T findById(ID id) throws DataAccessException {
		return (T)getHibernateTemplate().get(type,id);
	}

//********************************************//	
	
	
	public abstract List<T> find(String queryString);



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#exists(java.io.Serializable)
	 */
	public Boolean exists(ID id) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<T> list100() {
		HibernateTemplate ht = getHibernateTemplate();
		ht.setMaxResults(100);
		return ht.loadAll(type); 
	}

}
