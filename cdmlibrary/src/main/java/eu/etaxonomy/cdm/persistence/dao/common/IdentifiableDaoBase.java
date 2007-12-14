package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

public class IdentifiableDaoBase<T extends IdentifiableEntity> extends CdmEntityDaoBase<T> implements IIdentifiableDao<T>{
	static Logger logger = Logger.getLogger(IdentifiableDaoBase.class);


	public IdentifiableDaoBase(Class<T> type) {
		super(type);
	}

	public List<T> findByTitle(String queryString) {
		Criteria crit = getSession().createCriteria(type);
		crit.add(Restrictions.ilike("titleCache", queryString));
		List<T> results = crit.list();
		return results;
	}

}
