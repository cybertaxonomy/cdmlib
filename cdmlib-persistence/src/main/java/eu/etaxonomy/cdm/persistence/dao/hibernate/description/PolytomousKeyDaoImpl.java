/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.persistence.dao.description.IPolytomousKeyDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;

/**
 * @author a.mueller
 * @created 08.11.2010
 * @version 1.0
 */
@Repository
public class PolytomousKeyDaoImpl extends IdentifiableDaoBase<PolytomousKey> implements IPolytomousKeyDao {
	private static final Logger logger = Logger.getLogger(PolytomousKeyDaoImpl.class);

	public PolytomousKeyDaoImpl() {
		super(PolytomousKey.class); 
//		indexedClasses = new Class[1];
//		indexedClasses[0] = PolytomousKey.class;
	}
	

	@Override
	public List<PolytomousKey> list() {
		Criteria crit = getSession().createCriteria(type); 
		return crit.list(); 
	}

	
	//FIXME rewrite as node has a key attribute now
	public void loadNodes(PolytomousKeyNode root, List<String> nodePaths) {
		for(PolytomousKeyNode child : root.getChildren()) {
			defaultBeanInitializer.initialize(child, nodePaths);
			loadNodes(child,nodePaths);
		}
	}
	
}
