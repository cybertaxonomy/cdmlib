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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.persistence.dao.common.IOriginalSourceDao;

/**
 * @author a.mueller
 * @created 17.07.2008
 * @version 1.0
 */
public class OriginalSourceDaoImpl extends CdmEntityDaoBase<OriginalSource> implements	IOriginalSourceDao {
	private static final Logger logger = Logger.getLogger(OriginalSourceDaoImpl.class);

	public OriginalSourceDaoImpl() {
		super(OriginalSource.class); 
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.IOriginalSourceDao#getOriginalSourceById(java.lang.String, java.lang.String)
	 */
	public OriginalSource getOriginalSourceById(String idInSource, String idNamespace) {
		Session session = getSession();
		Criteria crit = session.createCriteria(type);
		crit.add(Restrictions.eq("idInSource", idInSource));
		crit.add(Restrictions.eq("idNamespace", idNamespace));
		crit.addOrder(Order.desc("created"));
		List<OriginalSource> results = crit.list();
		if (results.isEmpty()){
			return null;
		}else{
			return results.get(0);			
		}
	}

}
