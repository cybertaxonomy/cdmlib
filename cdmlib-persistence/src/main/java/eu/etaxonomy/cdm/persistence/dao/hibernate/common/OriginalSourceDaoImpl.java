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
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.persistence.dao.common.IOriginalSourceDao;

/**
 * @author a.mueller
 * @created 17.07.2008
 * @version 1.0
 */
@Repository
public class OriginalSourceDaoImpl extends CdmEntityDaoBase<OriginalSource> implements	IOriginalSourceDao {
	private static final Logger logger = Logger.getLogger(OriginalSourceDaoImpl.class);

	public OriginalSourceDaoImpl() {
		super(OriginalSource.class); 
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.IOriginalSourceDao#findOriginalSourceByIdInSource(java.lang.Class, java.lang.String, java.lang.String)
	 */
	public List<IdentifiableEntity> findOriginalSourceByIdInSource(Class clazz, String idInSource, String idNamespace) {
		Session session = getSession();
		Query q = session.createQuery(
                "Select c from " + clazz.getSimpleName() + " as c " +
                "inner join c.sources as source " +
                "where source.idInSource = :idInSource " + 
                	" AND source.idNamespace = :idNamespace"
            );
		q.setString("idInSource", idInSource);
		q.setString("idNamespace", idNamespace);
		//TODO integrate reference in where 
		List<IdentifiableEntity> results = q.list();
		
		return results;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.IOriginalSourceDao#findOriginalSourceByIdInSource(java.lang.String, java.lang.String)
	 */
	public List<OriginalSource> findOriginalSourceByIdInSource(String idInSource, String idNamespace) {
		Session session = getSession();
		Criteria crit = session.createCriteria(type);
		crit.add(Restrictions.eq("idInSource", idInSource));
		if (idNamespace == null){
			crit.add(Restrictions.isNull("idNamespace"));
		}else{
			crit.add(Restrictions.eq("idNamespace", idNamespace));
		}
		crit.addOrder(Order.desc("created"));
		List<OriginalSource> results = crit.list();
		
		return results;
	}


}
