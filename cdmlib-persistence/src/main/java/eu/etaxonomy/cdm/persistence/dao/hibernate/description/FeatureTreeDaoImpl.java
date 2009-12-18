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

import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureTreeDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;

/**
 * @author a.mueller
 * @created 10.07.2008
 * @version 1.0
 */
@Repository
public class FeatureTreeDaoImpl extends IdentifiableDaoBase<FeatureTree> implements IFeatureTreeDao{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FeatureTreeDaoImpl.class);

	public FeatureTreeDaoImpl() {
		super(FeatureTree.class); 
		indexedClasses = new Class[2];
		indexedClasses[0] = FeatureTree.class;
		indexedClasses[1] = PolytomousKey.class;
	}
	
	public List<FeatureTree> list() {
		Criteria crit = getSession().createCriteria(type); 
		return crit.list(); 
	}

	public void loadNodes(FeatureNode root, List<String> nodePaths) {
		for(FeatureNode child : root.getChildren()) {
			defaultBeanInitializer.initialize(child, nodePaths);
			loadNodes(child,nodePaths);
		}
	}

}
