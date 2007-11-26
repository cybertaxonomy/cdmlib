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
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * @author a.mueller
 *
 */
@Repository
public class TaxonNameDaoHibernateImpl 
			extends DaoBase<TaxonNameBase> implements ITaxonNameDao {
	static Logger logger = Logger.getLogger(TaxonNameDaoHibernateImpl.class);

	public TaxonNameDaoHibernateImpl() {
		super(TaxonNameBase.class); 
	}

	
	@Override
	public List<TaxonNameBase> find(String queryString) {
		// TODO Auto-generated method stub
		return null;
	}
	
}