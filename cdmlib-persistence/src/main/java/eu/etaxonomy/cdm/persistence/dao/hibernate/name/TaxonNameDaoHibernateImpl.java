/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;

/**
 * @author a.mueller
 *
 */
@Repository
public class TaxonNameDaoHibernateImpl 
			extends IdentifiableDaoBase<TaxonNameBase> implements ITaxonNameDao {
	static Logger logger = Logger.getLogger(TaxonNameDaoHibernateImpl.class);

	public TaxonNameDaoHibernateImpl() {
		super(TaxonNameBase.class); 
	}

}