/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.taxon;

import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.IdentifiableDaoBase;

/**
 * @author a.mueller
 *
 */
@Repository
public class TaxonDaoHibernateImpl extends IdentifiableDaoBase<TaxonBase> implements ITaxonDao {
	static Logger logger = Logger.getLogger(TaxonDaoHibernateImpl.class);

	public TaxonDaoHibernateImpl() {
		super(TaxonBase.class);
	}

	public List<Taxon> getRootTaxa(ReferenceBase sec) {
		// TODO add reference filter
		Criteria crit = getSession().createCriteria(Taxon.class);
		crit.add(Restrictions.isNull("taxonomicParentCache"));
		List<Taxon> results = crit.list();
		return results;
	}

	public List<TaxonBase> getTaxaByName(String name, ReferenceBase sec) {
		// TODO add reference filter
		return this.findByTitle(name);
	}
	
}