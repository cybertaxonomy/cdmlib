/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import eu.etaxonomy.cdm.model.name.NonViralName;

/**
 * @author a.mueller
 *
 */
public class NonViralNameDaoHibernateImpl 
			extends DaoBase<NonViralName, Integer> implements INonViralNameDao {
	static Logger logger = Logger.getLogger(NonViralNameDaoHibernateImpl.class);

	/**
	 * 
	 */
	public NonViralNameDaoHibernateImpl() {
		super(NonViralName.class); 
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.ITaxonNameDAO#getRelatedNames(java.lang.Integer)
	 */
	public List<NonViralName> getRelatedNames(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public List<NonViralName> getAllNames() {
		List<NonViralName> list = ht().find("from NonViralName");
		return list;
	}
	
	
	public List<NonViralName> getNamesByName(String name) {
		List<NonViralName> list = ht().find("from NonViralName tn where tn.name=?", name);
		return list;
	}

	//TODO test
	public NonViralName getDataSource(){
		return null;
	}

	//TODO test
	public void setDataSource(NonViralName tn){
		
	}
    
	private HibernateTemplate ht(){
		return getHibernateTemplate();
	}
	
}