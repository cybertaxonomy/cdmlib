// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.fail;
import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author n.hoffmann
 * @created Sep 22, 2009
 * @version 1.0
 */
public class TaxonTreeServiceImplTest extends CdmIntegrationTest{
	private static final Logger logger = Logger
			.getLogger(TaxonTreeServiceImplTest.class);

	@SpringBeanByType
	ITaxonTreeService service;
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#setTaxonNodeComparator(eu.etaxonomy.cdm.api.service.ITaxonNodeComparator)}.
	 */
	@Test
	public final void testSetTaxonNodeComparator() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#loadTaxonNodeByTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, java.util.UUID, java.util.List)}.
	 */
	@Test
	public final void testLoadTaxonNodeByTaxon() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#loadTaxonNode(eu.etaxonomy.cdm.model.taxon.TaxonNode, java.util.List)}.
	 */
	@Test
	public final void testLoadTaxonNode() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#loadRankSpecificRootNodes(eu.etaxonomy.cdm.model.taxon.TaxonomicTree, eu.etaxonomy.cdm.model.name.Rank, java.util.List)}.
	 */
	@Test
	public final void testLoadRankSpecificRootNodes() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#loadTreeBranch(eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.name.Rank, java.util.List)}.
	 */
	@Test
	public final void testLoadTreeBranch() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#loadTreeBranchToTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonomicTree, eu.etaxonomy.cdm.model.name.Rank, java.util.List)}.
	 */
	@Test
	public final void testLoadTreeBranchToTaxon() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#loadChildNodesOfTaxonNode(eu.etaxonomy.cdm.model.taxon.TaxonNode, java.util.List)}.
	 */
	@Test
	public final void testLoadChildNodesOfTaxonNode() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#loadChildNodesOfTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonomicTree, java.util.List)}.
	 */
	@Test
	public final void testLoadChildNodesOfTaxon() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#getTaxonNodeByUuid(java.util.UUID)}.
	 */
	@Test
	public final void testGetTaxonNodeByUuid() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#getTreeNodeByUuid(java.util.UUID)}.
	 */
	@Test
	public final void testGetTreeNodeByUuid() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#listTaxonomicTrees(java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)}.
	 */
	@Test
	public final void testListTaxonomicTrees() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#getTaxonomicTreeByUuid(java.util.UUID)}.
	 */
	@Test
	public final void testGetTaxonomicTreeByUuid() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#removeTaxonNode(eu.etaxonomy.cdm.model.taxon.TaxonNode)}.
	 */
	@Test
	public final void testRemoveTaxonNode() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#removeTreeNode(eu.etaxonomy.cdm.model.taxon.ITreeNode)}.
	 */
	@Test
	public final void testRemoveTreeNode() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#saveTaxonNode(eu.etaxonomy.cdm.model.taxon.TaxonNode)}.
	 */
	@Test
	public final void testSaveTaxonNode() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#saveTaxonNodeAll(java.util.Collection)}.
	 */
	@Test
	public final void testSaveTaxonNodeAll() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#saveTreeNode(eu.etaxonomy.cdm.model.taxon.ITreeNode)}.
	 */
	@Test
	public final void testSaveTreeNode() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByTaxonomicTree(eu.etaxonomy.cdm.model.taxon.TaxonomicTree)}.
	 */
	@Test
	public final void testGetTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByTaxonomicTree() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#getUuidAndTitleCache()}.
	 */
	@Test
	public final void testGetUuidAndTitleCache() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#getAllMediaForChildNodes(eu.etaxonomy.cdm.model.taxon.TaxonNode, java.util.List, int, int, int, java.lang.String[])}.
	 */
	@Test
	public final void testGetAllMediaForChildNodesTaxonNodeListOfStringIntIntIntStringArray() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#getAllMediaForChildNodes(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonomicTree, java.util.List, int, int, int, java.lang.String[])}.
	 */
	@Test
	public final void testGetAllMediaForChildNodesTaxonTaxonomicTreeListOfStringIntIntIntStringArray() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#setDao(eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonomicTreeDao)}.
	 */
	@Test
	public final void testSetDaoITaxonomicTreeDao() {
		Assert.assertNotNull(service);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonTreeServiceImpl#generateTitleCache()}.
	 */
	@Test
	public final void testGenerateTitleCache() {
//		fail("Not yet implemented");
	}
}
