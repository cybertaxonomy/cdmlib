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
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeByNameComparator;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author n.hoffmann
 * @created Sep 22, 2009
 * @version 1.0
 */
public class ClassificationServiceImplTest extends CdmTransactionalIntegrationTest{

    private static final Logger logger = Logger.getLogger(ClassificationServiceImplTest.class);

    @SpringBeanByType
    IClassificationService service;

    @SpringBeanByType
    ITaxonNodeService taxonNodeService;

    @SpringBeanByType
    IClassificationDao classificationDao;

    @SpringBeanByType
    IReferenceDao referenceDao;

    private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
            "childNodes",
            "childNodes.taxon",
            "childNodes.taxon.name",
            "taxon.sec",
            "taxon.name.*"
            });

    private static final String CLASSIFICATION_UUID = "6c2bc8d9-ee62-4222-be89-4a8e31770878";

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
        Classification classification = service.find(UUID.fromString(CLASSIFICATION_UUID));

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

        Collections.sort(nodes, taxonNodeComparator);


        logger.setLevel(Level.DEBUG);
        if(logger.isDebugEnabled()){
            logger.debug("-------------");
	        for (TaxonNode node: nodes){
	        	if (node!= null && node.getTaxon() != null && node.getTaxon().getName()!= null){
	                logger.debug(node.getTaxon().getName().getTitleCache() + " [" + node.getTaxon().getName().getRank() + "]");
	        	}
	            /*for (TaxonNode child : node.getChildNodes()){
	                    logger.debug(child.getTaxon().getName().getTitleCache());
	            }*/
	        }
        }

        Assert.assertEquals("Acacia N.Jacobsen, Bastm. & Yuji Sasaki", nodes.get(1).getTaxon().getName().getTitleCache());
        Assert.assertEquals("Acacia subgen. Aculeiferum Pedley", nodes.get(2).getTaxon().getName().getTitleCache());
        Assert.assertEquals("Acacia sect. Botrycephalae Yuji Sasaki", nodes.get(3).getTaxon().getName().getTitleCache());
        Assert.assertEquals("Acacia subgen. Phyllodineae N.Jacobsen, Bastm. & Yuji Sasaki", nodes.get(4).getTaxon().getName().getTitleCache());
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

        // classification, see  createTestDataSet()

        // ClassificationRoot
        // |- Acacia N.Jacobsen, Bastm. & Yuji Sasaki                          [Genus]
        // |  |-- Acacia subg. Aculeiferum Pedley                              [Subgenus]
        // |  |-- Acacia subg. Phyllodineae N.Jacobsen, Bastm. & Yuji Sasaki   [Subgenus]
        // |  |  |-- Acacia sect. Botrycephalae Yuji Sasaki                    [Section (Botany)]
        // |  |------- Acacia cuspidifolia Maslin                              [Species]
        // |  |------- Acacia mearnsii Benth                                   [Species]
        // |---------- Acacia acicularis Willd.                                [Species]
        //
        // for more historic Acacia taxonomy see http://lexikon.freenet.de/Akazien

        List<TaxonNode> taxonNodes = service.listRankSpecificRootNodes(null, null, null, null, NODE_INIT_STRATEGY);
        Assert.assertEquals(2, taxonNodes.size());

        taxonNodes = service.listRankSpecificRootNodes(classification, null, null, null, NODE_INIT_STRATEGY);
        Assert.assertEquals(2, taxonNodes.size());

        taxonNodes = service.listRankSpecificRootNodes(classification, Rank.SECTION_BOTANY(), null, null, NODE_INIT_STRATEGY);
        Assert.assertEquals(4, taxonNodes.size());

        // also test if the pager works
        taxonNodes = service.listRankSpecificRootNodes(classification, Rank.SECTION_BOTANY(), 10, 0, NODE_INIT_STRATEGY);
        Assert.assertEquals(4, taxonNodes.size());

        taxonNodes = service.listRankSpecificRootNodes(classification, Rank.SPECIES(), null, null, NODE_INIT_STRATEGY);
        Assert.assertEquals(3, taxonNodes.size());

        // also test if the pager works
        taxonNodes = service.listRankSpecificRootNodes(classification, Rank.SPECIES(), 10, 0, NODE_INIT_STRATEGY);
        Assert.assertEquals(3, taxonNodes.size());

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


    /**
     * {@inheritDoc}
     */
    @Override
//    @Test
    public void createTestDataSet() throws FileNotFoundException {

        // ClassificationRoot
        // |- Acacia N.Jacobsen, Bastm. & Yuji Sasaki                          [Genus]
        // |  |-- Acacia subg. Aculeiferum Pedley                              [Subgenus]
        // |  |-- Acacia subg. Phyllodineae N.Jacobsen, Bastm. & Yuji Sasaki   [Subgenus]
        // |  |  |-- Acacia sect. Botrycephalae Yuji Sasaki                    [Section (Botany)]
        // |  |------- Acacia cuspidifolia Maslin                              [Species]
        // |  |------- Acacia mearnsii Benth                                   [Species]
        // |---------- Acacia acicularis Willd.                                [Species]
        //
        // for more historic Acacia taxonomy see http://lexikon.freenet.de/Akazien

        // 1. create the entities   and save them
        Classification classification = Classification.NewInstance("Acacia Classification");
        classification.setUuid(UUID.fromString(CLASSIFICATION_UUID));
        classificationDao.save(classification);

        Reference<?> sec = ReferenceFactory.newBook();
        sec.setTitleCache("Sp. Pl.", true);
        referenceDao.save(sec);

        BotanicalName acacia_n = BotanicalName.NewInstance(Rank.GENUS(), "Acacia", null, null, null, null, sec, null, null);
        acacia_n.setAuthorshipCache("N.Jacobsen, Bastm. & Yuji Sasaki", true);
        Taxon acacia_t = Taxon.NewInstance(acacia_n, sec);

        BotanicalName acacia_subg_aculeiferum_n = BotanicalName.NewInstance(Rank.SUBGENUS(), "Acacia", "Aculeiferum", null, null, null, sec, null, null);
        acacia_subg_aculeiferum_n.setAuthorshipCache("Pedley", true);
        Taxon acacia_subg_aculeiferum_t = Taxon.NewInstance(acacia_subg_aculeiferum_n, sec);

        BotanicalName acacia_subg_phyllodineae_n = BotanicalName.NewInstance(Rank.SUBGENUS(), "Acacia", "Phyllodineae", null, null, null, sec, null, null);
        acacia_subg_phyllodineae_n.setAuthorshipCache("N.Jacobsen, Bastm. & Yuji Sasaki", true);
        Taxon acacia_subg_phyllodineae_t = Taxon.NewInstance(acacia_subg_phyllodineae_n, sec);

        BotanicalName acacia_setc_botrycephalae_n = BotanicalName.NewInstance(Rank.SECTION_BOTANY(), "Acacia", "Botrycephalae", null, null, null, sec, null, null);
        acacia_setc_botrycephalae_n.setAuthorshipCache("Yuji Sasaki", true);
        Taxon acacia_setc_botrycephalae_t = Taxon.NewInstance(acacia_setc_botrycephalae_n, sec);

        BotanicalName acacia_cuspidifolia_n = BotanicalName.NewInstance(Rank.SPECIES(), "Acacia", null,"cuspidifolia", null, null, sec, null, null);
        acacia_cuspidifolia_n.setAuthorshipCache("Maslin", true);
        Taxon acacia_cuspidifolia_t = Taxon.NewInstance(acacia_cuspidifolia_n, sec);

        BotanicalName acacia_mearnsii_n = BotanicalName.NewInstance(Rank.SPECIES(), "Acacia", null,"mearnsii", null, null, sec, null, null);
        acacia_mearnsii_n.setAuthorshipCache("Benth", true);
        Taxon acacia_mearnsii_t = Taxon.NewInstance(acacia_mearnsii_n, sec);

        BotanicalName acacia_acicularis_n = BotanicalName.NewInstance(Rank.SPECIES(), "Acacia", null,"acicularis", null, null, sec, null, null);
        acacia_acicularis_n.setAuthorshipCache("Willd.", true);
        Taxon acacia_acicularis_t = Taxon.NewInstance(acacia_acicularis_n, sec);

        TaxonNode acacia_tn = classification.addChildTaxon(acacia_t, sec, null);
        TaxonNode acacia_subg_phyllodineae_tn = acacia_tn.addChildTaxon(acacia_subg_phyllodineae_t, sec, null);
        acacia_subg_phyllodineae_tn.addChildTaxon(acacia_setc_botrycephalae_t, sec, null);
        acacia_tn.addChildTaxon(acacia_subg_aculeiferum_t, sec, null);
        acacia_tn.addChildTaxon(acacia_mearnsii_t, sec, null);
        acacia_tn.addChildTaxon(acacia_cuspidifolia_t, sec, null);
        classification.addChildTaxon(acacia_acicularis_t, sec, null);

        classificationDao.save(classification);

        // 2. end the transaction so that all data is actually written to the db
        setComplete();
        endTransaction();

        // use the fileNameAppendix if you are creating a data set file which need to be named differently
        // from the standard name. For example if a single test method needs different data then the other
        // methods the test class you may want to set the fileNameAppendix when creating the data for this method.
        String fileNameAppendix = null;

        // 3.
        writeDbUnitDataSetFile(new String[] {
            "TAXONBASE", "TAXONNAMEBASE",
            "REFERENCE",
            "CLASSIFICATION", "TAXONNODE",
            "LANGUAGESTRING", "HOMOTYPICALGROUP",
            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
            },
            fileNameAppendix );

    }
}
