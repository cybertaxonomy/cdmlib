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

import eu.etaxonomy.cdm.model.name.TaxonName;

/**
 * @author a.mueller
 *
 */
public class TaxonNameDaoHibernateImpl 
			extends DaoBase<TaxonName, Integer> implements ITaxonNameDao {
	static Logger logger = Logger.getLogger(TaxonNameDaoHibernateImpl.class);

	/**
	 * 
	 */
	public TaxonNameDaoHibernateImpl() {
		super(TaxonName.class); 
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.ITaxonNameDAO#getRelatedNames(java.lang.Integer)
	 */
	public List<TaxonName> getRelatedNames(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public List<TaxonName> getAllNames() {
		List<TaxonName> list = ht().find("from TaxonName");
		return list;
	}
	
	
	public List<TaxonName> getNamesByName(String name) {
		List<TaxonName> list = ht().find("from TaxonName tn where tn.name=?", name);
		return list;
	}

	//TODO test
	public TaxonName getDataSource(){
		return null;
	}

	//TODO test
	public void setDataSource(TaxonName tn){
		
	}
    
	private HibernateTemplate ht(){
		return getHibernateTemplate();
	}
	
}