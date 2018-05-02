/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.persistence.dao.common.IOriginalSourceDao;

/**
 * @author a.mueller
 * @since 17.07.2008
 * @version 1.0
 */
@Repository
public class OriginalSourceDaoImpl extends CdmEntityDaoBase<OriginalSourceBase> implements	IOriginalSourceDao {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OriginalSourceDaoImpl.class);

	public OriginalSourceDaoImpl() {
		super(OriginalSourceBase.class); 
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.IOriginalSourceDao#findOriginalSourcesByIdInSource(java.lang.Class, java.util.List, java.lang.String)
	 */
	public Map<String, ISourceable> findOriginalSourcesByIdInSource(Class clazz, Set<String> idInSourceSet, String idNamespace) {
		Session session = getSession();
		String idInSourceString = "";
		for (String idInSource : idInSourceSet){
			idInSourceString = CdmUtils.concat("','", idInSourceString, idInSource);
		}
		idInSourceString = "'"+ idInSourceString + "'";

		Query q = session.createQuery(
                "SELECT source.idInSource, c FROM " + clazz.getSimpleName() + " AS c " +
                "INNER JOIN c.sources AS source " +
                "WHERE source.idInSource IN ( " + idInSourceString + " )" + 
                	" AND source.idNamespace = :idNamespace"
            );
		q.setString("idNamespace", idNamespace);
		//TODO integrate reference in where 
		
		Map<String, ISourceable> result = new HashMap<String, ISourceable>();
		
		List<Object[]> list = q.list();
		for (Object[] pair : list){
			result.put((String)pair[0], (ISourceable)pair[1]);
		}
		
		return result;
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
	public List<OriginalSourceBase> findOriginalSourceByIdInSource(String idInSource, String idNamespace) {
		Session session = getSession();
		Criteria crit = session.createCriteria(type);
		crit.add(Restrictions.eq("idInSource", idInSource));
		if (idNamespace == null){
			crit.add(Restrictions.isNull("idNamespace"));
		}else{
			crit.add(Restrictions.eq("idNamespace", idNamespace));
		}
		crit.addOrder(Order.desc("created"));
		List<OriginalSourceBase> results = crit.list();
		
		return results;
	}


}
