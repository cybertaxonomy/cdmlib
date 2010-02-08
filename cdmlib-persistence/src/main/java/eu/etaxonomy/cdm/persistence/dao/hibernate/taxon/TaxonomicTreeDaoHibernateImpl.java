// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonomicTreeDao;

/**
 * @author a.mueller
 * @created 16.06.2009
 * @version 1.0
 */
@Repository
@Qualifier("taxonomicTreeDaoHibernateImpl")
public class TaxonomicTreeDaoHibernateImpl extends IdentifiableDaoBase<TaxonomicTree>
		implements ITaxonomicTreeDao {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonomicTreeDaoHibernateImpl.class);
	
	public TaxonomicTreeDaoHibernateImpl() {
		super(TaxonomicTree.class);
		indexedClasses = new Class[1];
		indexedClasses[0] = TaxonomicTree.class;
	}
	
	@SuppressWarnings("unchecked")
	public List<TaxonNode> loadRankSpecificRootNodes(TaxonomicTree taxonomicTree, Rank rank, List<String> propertyPaths){
		String hql = "SELECT DISTINCT tn FROM TaxonNode tn LEFT JOIN tn.childNodes as ctn" +
				" WHERE tn.taxonomicTree = :tree  AND (" +
				" tn.taxon.name.rank = :rank" +
				" OR (tn.taxon.name.rank < :rank AND tn.parent = null)" +
				" OR (tn.taxon.name.rank > :rank AND ctn.taxon.name.rank < :rank)" +
				" )";
		Query query = getSession().createQuery(hql);
		query.setParameter("rank", rank);
		query.setParameter("tree", taxonomicTree);
		List<TaxonNode> results = query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}
	
	
}
