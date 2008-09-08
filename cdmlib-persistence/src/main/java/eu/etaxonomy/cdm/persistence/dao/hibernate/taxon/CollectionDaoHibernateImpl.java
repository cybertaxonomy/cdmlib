/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.collection;

import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.persistence.dao.collection.ICollectionDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;

/**
 * @author p.kelbert
 *
 */
@Repository
public class CollectionDaoHibernateImpl extends IdentifiableDaoBase<Collection> implements ICollectionDao {
	static Logger logger = Logger.getLogger(CollectionDaoHibernateImpl.class);

	public CollectionDaoHibernateImpl() {
		super(Collection.class);
	}

	
	

	public List<Collection> getCollectionByCode(String code) {
		Criteria crit = getSession().createCriteria(Collection.class);
		crit.createCriteria("code").add(Restrictions.eq("code", code));
		List<Collection> results = crit.list();
		return results;
	}

	

	


	
}