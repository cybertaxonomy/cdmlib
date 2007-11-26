/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao;

import java.io.Serializable;
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
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 *
 */
@Repository
public class TaxonDaoHibernateImpl extends DaoBase<TaxonBase> implements ITaxonDao {
	static Logger logger = Logger.getLogger(TaxonDaoHibernateImpl.class);

	public TaxonDaoHibernateImpl() {
		super(TaxonBase.class);
	}

	@Override
	public List<TaxonBase> find(String queryString) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Taxon> getRootTaxa(ReferenceBase sec) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<TaxonBase> getTaxaByName(String name, ReferenceBase sec) {
		// TODO add reference filter
		return this.find(name);
	}
	
}