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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeByNameComparator;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author n.hoffmann
 * @created Sep 22, 2009
 * @version 1.0
 */
public class ClassificationServiceImplTest extends CdmIntegrationTest{

    private static final Logger logger = Logger.getLogger(ClassificationServiceImplTest.class);

    @SpringBeanByType
    IClassificationService service;

    @SpringBeanByType
    ITaxonNodeService taxonNodeService;
    private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
            "childNodes",
            "childNodes.taxon",
            "childNodes.taxon.name",
            "taxon.sec",
            "taxon.name.*"
            });

    private Comparator<? super TaxonNode> taxonNodeComparator;


    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#setTaxonNodeComparator(eu.etaxonomy.cdm.model.taxon.ITaxonNodeComparator)}.
     */
//    @Test
    public final void testSetTaxonNodeComparator() {


//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#loadTaxonNodeByTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, java.util.UUID, java.util.List)}.
     */
//    @Test
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


    @Test
    @DataSet
    public final void testTaxonNodeByNameComparator() {
        taxonNodeComparator = new TaxonNodeByNameComparator();
        Classification classification = service.find(UUID.fromString("6c2bc8d9-ee62-4222-be89-4a8e31770878"));

//    	try {
//			printDataSet(new FileOutputStream(new File("testTaxonNodeByNameComparator-dump.xml")), new String[] {"DefinedTermBase"});
//		} catch (FileNotFoundException e) {
//			/* IGNORE */
//		}

        //List<TaxonNode> taxonNodes = service.loadRankSpecificRootNodes(classification, Rank.GENUS(), NODE_INIT_STRATEGY);

        List<TaxonNode> taxonNodes = service.getAllNodes();
        List<TaxonNode> nodes = new ArrayList<TaxonNode>();
        for (TaxonNode node: taxonNodes){
            TaxonNode nodeDeproxy = HibernateProxyHelper.deproxy(node, TaxonNode.class);
            nodes.add(taxonNodeService.load(nodeDeproxy.getUuid(), NODE_INIT_STRATEGY));

        }
       // TaxonNode nodeGenus = taxonNodeService.find(UUID.fromString("19a4fce2-8be5-4ec7-a6a7-f3974047ba5f"));
       // int index = taxonNodes.indexOf(nodeGenus);
      //  taxonNodes.remove(index);
        Collections.sort(nodes, taxonNodeComparator);

        /**
         * expected order is:
         *  Acacia subg. Aculeiferum Pedley
         *  Acacia sect. Botrycephalae Yuji Sasaki
         *  Acacia subg. Phyllodineae N.Jacobsen, Bastm. & Yuji Sasaki
         *  Acacia cuspidifolia Maslin
         *  Acacia mearnsii Benth
         */

        logger.setLevel(Level.DEBUG);
        if(logger.isDebugEnabled()){
            logger.debug("-------------");
	        for (TaxonNode node: nodes){
	        	if (node!= null && node.getTaxon() != null && node.getTaxon().getName()!= null){
	                logger.debug(node.getTaxon().getName().getTitleCache() );
	        	}
	            /*for (TaxonNode child : node.getChildNodes()){
	                    logger.debug(child.getTaxon().getName().getTitleCache());
	            }*/
	        }
        }

        Assert.assertEquals("Acacia N.Jacobsen, Bastm. & Yuji Sasaki", nodes.get(1).getTaxon().getName().getTitleCache());
        Assert.assertEquals("Acacia subg. Aculeiferum Pedley", nodes.get(2).getTaxon().getName().getTitleCache());
        Assert.assertEquals("Acacia sect. Botrycephalae Yuji Sasaki", nodes.get(3).getTaxon().getName().getTitleCache());
        Assert.assertEquals("Acacia subg. Phyllodineae N.Jacobsen, Bastm. & Yuji Sasaki", nodes.get(4).getTaxon().getName().getTitleCache());
        Assert.assertEquals("Acacia cuspidifolia Maslin", nodes.get(5).getTaxon().getName().getTitleCache());
        Assert.assertEquals("Acacia mearnsii Benth", nodes.get(6).getTaxon().getName().getTitleCache());



        /*
        ((TaxonNodeByNameComparator)taxonNodeComparator).setSortInfraGenericFirst(false);
        Collections.sort(taxonNodes, taxonNodeComparator);

        Assert.assertEquals("Acacia cuspidifolia Maslin", taxonNodes.get(0).getTaxon().getName().getTitleCache());
        System.err.println();
        for (TaxonNode node: taxonNodes){
            System.err.println(node.getTaxon().getName().getTitleCache() );
            /*for (TaxonNode child : node.getChildNodes()){
                System.err.println(child.getTaxon().getName().getTitleCache());
            }
        }*/
//		fail("Not yet implemented");

    }


    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#loadRankSpecificRootNodes(eu.etaxonomy.cdm.model.taxon.Classification, eu.etaxonomy.cdm.model.name.Rank, java.util.List)}.
     */
    @Test
    @DataSet
    public final void testlistRankSpecificRootNodes(){
        Classification classification = service.find(UUID.fromString("6c2bc8d9-ee62-4222-be89-4a8e31770878"));

        List<TaxonNode> taxonNodes = service.listRankSpecificRootNodes(null, null, null, null, NODE_INIT_STRATEGY);
        Assert.assertEquals(1, taxonNodes.size());

        taxonNodes = service.listRankSpecificRootNodes(classification, null, null, null, NODE_INIT_STRATEGY);
        Assert.assertEquals(1, taxonNodes.size());

        taxonNodes = service.listRankSpecificRootNodes(classification, Rank.SECTION_BOTANY(), null, null, NODE_INIT_STRATEGY);
        Assert.assertEquals(2, taxonNodes.size());

    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#loadTreeBranch(eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.name.Rank, java.util.List)}.
     */
//    @Test
    public final void testLoadTreeBranch() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#loadTreeBranchToTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.Classification, eu.etaxonomy.cdm.model.name.Rank, java.util.List)}.
     */
//    @Test
    public final void testLoadTreeBranchToTaxon() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#loadChildNodesOfTaxonNode(eu.etaxonomy.cdm.model.taxon.TaxonNode, java.util.List)}.
     */
//    @Test
    public final void testLoadChildNodesOfTaxonNode() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#loadChildNodesOfTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.Classification, java.util.List)}.
     */
//    @Test
    public final void testLoadChildNodesOfTaxon() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#getTaxonNodeByUuid(java.util.UUID)}.
     */
//    @Test
    public final void testGetTaxonNodeByUuid() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#getTreeNodeByUuid(java.util.UUID)}.
     */
//    @Test
    public final void testGetTreeNodeByUuid() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#listClassifications(java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)}.
     */
//    @Test
    public final void testListClassifications() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#getClassificationByUuid(java.util.UUID)}.
     */
//    @Test
    public final void testGetClassificationByUuid() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#removeTaxonNode(eu.etaxonomy.cdm.model.taxon.TaxonNode)}.
     */
//    @Test
    public final void testRemoveTaxonNode() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#removeTreeNode(eu.etaxonomy.cdm.model.taxon.ITreeNode)}.
     */
//    @Test
    public final void testRemoveTreeNode() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#saveTaxonNode(eu.etaxonomy.cdm.model.taxon.TaxonNode)}.
     */
//    @Test
    public final void testSaveTaxonNode() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#saveTaxonNodeAll(java.util.Collection)}.
     */
//    @Test
    public final void testSaveTaxonNodeAll() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#saveTreeNode(eu.etaxonomy.cdm.model.taxon.ITreeNode)}.
     */
//    @Test
    public final void testSaveTreeNode() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(eu.etaxonomy.cdm.model.taxon.Classification)}.
     */
//    @Test
    public final void testGetTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#getUuidAndTitleCache()}.
     */
//    @Test
    public final void testGetUuidAndTitleCache() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#getAllMediaForChildNodes(eu.etaxonomy.cdm.model.taxon.TaxonNode, java.util.List, int, int, int, java.lang.String[])}.
     */
//    @Test
    public final void testGetAllMediaForChildNodesTaxonNodeListOfStringIntIntIntStringArray() {
//		fail("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.ClassificationServiceImpl#getAllMediaForChildNodes(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.Classification, java.util.List, int, int, int, java.lang.String[])}.
     */
//    @Test
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
//    @Test
    public final void testGenerateTitleCache() {
//		fail("Not yet implemented");
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
}
