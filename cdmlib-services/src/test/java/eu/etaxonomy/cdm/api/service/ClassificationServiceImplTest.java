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
public class ClassificationServiceImplTest extends CdmIntegrationTest{
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ClassificationServiceImplTest.class);

    @SpringBeanByType
    IClassificationService service;

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#setTaxonNodeComparator(eu.etaxonomy.cdm.model.taxon.ITaxonNodeComparator)}.
     */
    @Test
    public final void testSetTaxonNodeComparator() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#loadTaxonNodeByTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, java.util.UUID, java.util.List)}.
     */
    @Test
    public final void testLoadTaxonNodeByTaxon() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#loadTaxonNode(eu.etaxonomy.cdm.model.taxon.TaxonNode, java.util.List)}.
     */
    @Test
    public final void testLoadTaxonNode() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#loadRankSpecificRootNodes(eu.etaxonomy.cdm.model.taxon.Classification, eu.etaxonomy.cdm.model.name.Rank, java.util.List)}.
     */
    @Test
    public final void testLoadRankSpecificRootNodes() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#loadTreeBranch(eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.name.Rank, java.util.List)}.
     */
    @Test
    public final void testLoadTreeBranch() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#loadTreeBranchToTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.Classification, eu.etaxonomy.cdm.model.name.Rank, java.util.List)}.
     */
    @Test
    public final void testLoadTreeBranchToTaxon() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#loadChildNodesOfTaxonNode(eu.etaxonomy.cdm.model.taxon.TaxonNode, java.util.List)}.
     */
    @Test
    public final void testLoadChildNodesOfTaxonNode() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#loadChildNodesOfTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.Classification, java.util.List)}.
     */
    @Test
    public final void testLoadChildNodesOfTaxon() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#getTaxonNodeByUuid(java.util.UUID)}.
     */
    @Test
    public final void testGetTaxonNodeByUuid() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#getTreeNodeByUuid(java.util.UUID)}.
     */
    @Test
    public final void testGetTreeNodeByUuid() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#listClassifications(java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)}.
     */
    @Test
    public final void testListClassifications() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#getClassificationByUuid(java.util.UUID)}.
     */
    @Test
    public final void testGetClassificationByUuid() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#removeTaxonNode(eu.etaxonomy.cdm.model.taxon.TaxonNode)}.
     */
    @Test
    public final void testRemoveTaxonNode() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#removeTreeNode(eu.etaxonomy.cdm.model.taxon.ITreeNode)}.
     */
    @Test
    public final void testRemoveTreeNode() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#saveTaxonNode(eu.etaxonomy.cdm.model.taxon.TaxonNode)}.
     */
    @Test
    public final void testSaveTaxonNode() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#saveTaxonNodeAll(java.util.Collection)}.
     */
    @Test
    public final void testSaveTaxonNodeAll() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#saveTreeNode(eu.etaxonomy.cdm.model.taxon.ITreeNode)}.
     */
    @Test
    public final void testSaveTreeNode() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(eu.etaxonomy.cdm.model.taxon.Classification)}.
     */
    @Test
    public final void testGetTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#getUuidAndTitleCache()}.
     */
    @Test
    public final void testGetUuidAndTitleCache() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#getAllMediaForChildNodes(eu.etaxonomy.cdm.model.taxon.TaxonNode, java.util.List, int, int, int, java.lang.String[])}.
     */
    @Test
    public final void testGetAllMediaForChildNodesTaxonNodeListOfStringIntIntIntStringArray() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#getAllMediaForChildNodes(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.Classification, java.util.List, int, int, int, java.lang.String[])}.
     */
    @Test
    public final void testGetAllMediaForChildNodesTaxonClassificationListOfStringIntIntIntStringArray() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#setDao(eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao)}.
     */
    @Test
    public final void testSetDaoIClassificationDao() {
        Assert.assertNotNull(service);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#generateTitleCache()}.
     */
    @Test
    public final void testGenerateTitleCache() {
//		fail("Not yet implemented");
    }
}
