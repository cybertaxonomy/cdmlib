package eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.occurrence.ICollectionDao;

@Repository
public class CollectionDaoHibernateImpl extends IdentifiableDaoBase<Collection> implements
		ICollectionDao {
		
	public CollectionDaoHibernateImpl() {
		super(Collection.class);
	}

	public List<Collection> getCollectionByCode(String code) {
		Criteria crit = getSession().createCriteria(Collection.class);

		crit.add(Restrictions.eq("code", code));
		List<Collection> results = crit.list();
		return results;
	}
}
