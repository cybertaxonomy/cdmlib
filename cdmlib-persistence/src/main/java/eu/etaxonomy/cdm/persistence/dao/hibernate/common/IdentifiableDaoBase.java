/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import static eu.etaxonomy.cdm.persistence.dao.common.ITitledDao.MATCH_MODE.*;


public class IdentifiableDaoBase<T extends IdentifiableEntity> extends CdmEntityDaoBase<T> implements IIdentifiableDao<T>{
	private static final Logger logger = Logger.getLogger(IdentifiableDaoBase.class);


	public IdentifiableDaoBase(Class<T> type) {
		super(type);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ITitledDao#findByTitle(java.lang.String)
	 */
	public List<T> findByTitle(String queryString) {
		return findByTitle(queryString, null);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ITitledDao#findByTitle(java.lang.String)
	 */
	public List<T> findByTitle(String queryString, CdmBase sessionObject) {
		Session session = getSession();
		if ( sessionObject != null ) {
			session.update(sessionObject);
		}
		Criteria crit = session.createCriteria(type);
		crit.add(Restrictions.ilike("persistentTitleCache", queryString));
		List<T> results = crit.list();
		return results;
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ITitledDao#findByTitle(java.lang.String, boolean, int, int, java.util.List)
	 */
	public List<T> findByTitle(String queryString, MATCH_MODE matchmode, int page, int pagesize, List<Criterion> criteria) {

		Criteria crit = getSession().createCriteria(type);
		crit.add(Restrictions.ilike("titleCache", matchmode.queryStringFrom(queryString)));
		crit.setMaxResults(pagesize);
		if(criteria != null){
			for (Criterion criterion : criteria) {
				crit.add(criterion);
			}
		}
		crit.addOrder(Order.asc("titleCache"));
		int firstItem = (page - 1) * pagesize;
		crit.setFirstResult(firstItem);
		List<T> results = crit.list();
		return results;
	}
	
	

}
