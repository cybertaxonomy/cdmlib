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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

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
	}

}
