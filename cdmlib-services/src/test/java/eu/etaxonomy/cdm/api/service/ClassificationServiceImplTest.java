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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.dto.GroupedTaxonDTO;
import eu.etaxonomy.cdm.compare.taxon.TaxonNodeByNameComparator;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author n.hoffmann
 * @since Sep 22, 2009
 */
public class ClassificationServiceImplTest extends CdmTransactionalIntegrationTest{

    private static final Logger logger = LogManager.getLogger();

    @SpringBeanByType
    private IClassificationService service;

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private ITaxonNodeService taxonNodeService;

    @SpringBeanByType
    private IClassificationService classificationService;

    @SpringBeanByType
    private IClassificationDao classificationDao;

    @SpringBeanByType
    private IReferenceDao referenceDao;

    private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
            "childNodes",
            "childNodes.taxon",
            "childNodes.taxon.name",
            "taxon.secSource.citation",
            "taxon.name.*"
            });

    public static final UUID CLASSIFICATION_UUID = UUID.fromString("6c2bc8d9-ee62-4222-be89-4a8e31770878");

    private Comparator<? super TaxonNode> taxonNodeComparator;

    private boolean includeUnpublished;

    @Before
    public void setUp() {
        includeUnpublished = true;
    }

    @Test
    public final void testLoadTaxonNodeByTaxon() {
		logger.warn("testLoadTaxonNodeByTaxon not yet implemented");
    }

    @Test
    public final void testLoadTaxonNode() {
		logger.warn("testLoadTaxonNode not yet implemented");
    }

    @Test
    @DataSet
    public final void testTaxonNodeByNameComparator() {
        taxonNodeComparator = new TaxonNodeByNameComparator();

        List<TaxonNode> nodes = service.getAllNodes();
        Collections.sort(nodes, taxonNodeComparator);

//        logger.setLevel(Level.DEBUG);
        if(logger.isDebugEnabled()){
            logger.debug("-------------");
	        for (TaxonNode node: nodes){
	        	if (node!= null && node.getTaxon() != null && node.getTaxon().getName()!= null){
	                logger.debug(node.getTaxon().getName().getTitleCache() + " [" + node.getTaxon().getName().getRank() + "]");
	        	}
	        }
        }

        Assert.assertEquals("Acacia N.Jacobsen, Bastm. & Yuji Sasaki", nodes.get(1).getTaxon().getName().getTitleCache());
        Assert.assertEquals("Acacia subgen. Aculeiferum Pedley", nodes.get(2).getTaxon().getName().getTitleCache());
        Assert.assertEquals("Acacia sect. Botrycephalae Yuji Sasaki", nodes.get(3).getTaxon().getName().getTitleCache());
        Assert.assertEquals("Acacia subgen. Phyllodineae N.Jacobsen, Bastm. & Yuji Sasaki", nodes.get(4).getTaxon().getName().getTitleCache());
        Assert.assertEquals("Acacia acicularis Willd.", nodes.get(5).getTaxon().getName().getTitleCache());
        Assert.assertEquals("×Acacia acicularis Willd. subsp. acicularis", nodes.get(6).getTaxon().getName().getTitleCache());
        Assert.assertEquals("Acacia cuspidifolia Maslin", nodes.get(7).getTaxon().getName().getTitleCache());
        Assert.assertEquals("Acacia mearnsii Benth", nodes.get(8).getTaxon().getName().getTitleCache());

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

    @Test
    @DataSet
    public final void testListRankSpecificRootNodes(){
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
        //             |-- ×Acacia acicularis Willd. subsp. acicularis         [Subspecies]
        //
        // for more historic Acacia taxonomy see http://lexikon.freenet.de/Akazien

        List<TaxonNode> taxonNodes = service.listRankSpecificRootNodes(null, null, null, includeUnpublished, null, null, NODE_INIT_STRATEGY);
        Assert.assertEquals(2, taxonNodes.size());

        taxonNodes = service.listRankSpecificRootNodes(classification, null, null, includeUnpublished, null, null, NODE_INIT_STRATEGY);
        Assert.assertEquals(2, taxonNodes.size());

        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SECTION_BOTANY(), includeUnpublished, null, null, NODE_INIT_STRATEGY);
        Assert.assertEquals(4, taxonNodes.size());

        // also test if the pager works
        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SECTION_BOTANY(), includeUnpublished, 10, 0, NODE_INIT_STRATEGY);
        Assert.assertEquals(4, taxonNodes.size());
        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SECTION_BOTANY(), includeUnpublished, 2, 0, NODE_INIT_STRATEGY);
        Assert.assertEquals(2, taxonNodes.size());
        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SECTION_BOTANY(), includeUnpublished, 2, 1, NODE_INIT_STRATEGY);
        Assert.assertEquals(2, taxonNodes.size());
        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SECTION_BOTANY(), includeUnpublished, 2, 2, NODE_INIT_STRATEGY);
        Assert.assertEquals(0, taxonNodes.size());

        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SPECIES(), includeUnpublished, null, null, NODE_INIT_STRATEGY);
        Assert.assertEquals(3, taxonNodes.size());

        // also test if the pager works
        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SPECIES(), includeUnpublished, 10, 0, NODE_INIT_STRATEGY);
        Assert.assertEquals(3, taxonNodes.size());
        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SPECIES(), includeUnpublished, 2, 1, NODE_INIT_STRATEGY);
        Assert.assertEquals(1, taxonNodes.size());
    }

    @Test
    @DataSet
    public final void testListRankSpecificRootNodes_withSubtree(){
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
        //             |-- ×Acacia acicularis Willd. subsp. acicularis         [Subspecies]
        //
        // for more historic Acacia taxonomy see http://lexikon.freenet.de/Akazien

        TaxonNode subtree = taxonNodeService.find(UUID.fromString("bcdf945f-1f02-423e-883d-fe89e0af93e4"));


        List<TaxonNode> taxonNodes = service.listRankSpecificRootNodes(null, subtree, null, includeUnpublished, null, null, NODE_INIT_STRATEGY);
        Assert.assertEquals(2, taxonNodes.size());

        taxonNodes = service.listRankSpecificRootNodes(classification, null, null, includeUnpublished, null, null, NODE_INIT_STRATEGY);
        Assert.assertEquals(2, taxonNodes.size());

        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SECTION_BOTANY(), includeUnpublished, null, null, NODE_INIT_STRATEGY);
        Assert.assertEquals(4, taxonNodes.size());

        // also test if the pager works
        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SECTION_BOTANY(), includeUnpublished, 10, 0, NODE_INIT_STRATEGY);
        Assert.assertEquals(4, taxonNodes.size());
        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SECTION_BOTANY(), includeUnpublished, 2, 0, NODE_INIT_STRATEGY);
        Assert.assertEquals(2, taxonNodes.size());
        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SECTION_BOTANY(), includeUnpublished, 2, 1, NODE_INIT_STRATEGY);
        Assert.assertEquals(2, taxonNodes.size());
        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SECTION_BOTANY(), includeUnpublished, 2, 2, NODE_INIT_STRATEGY);
        Assert.assertEquals(0, taxonNodes.size());

        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SPECIES(), includeUnpublished, null, null, NODE_INIT_STRATEGY);
        Assert.assertEquals(3, taxonNodes.size());

        // also test if the pager works
        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SPECIES(), includeUnpublished, 10, 0, NODE_INIT_STRATEGY);
        Assert.assertEquals(3, taxonNodes.size());
        taxonNodes = service.listRankSpecificRootNodes(classification, null, Rank.SPECIES(), includeUnpublished, 2, 1, NODE_INIT_STRATEGY);
        Assert.assertEquals(1, taxonNodes.size());
    }

    @Test
    public final void testSetDaoIClassificationDao() {
        Assert.assertNotNull(service);
    }

    @Test
    @DataSet
    public final void testGroupTaxaByHigherTaxon(){

        Rank minRank = Rank.GENUS();
        Rank maxRank = Rank.KINGDOM();
        List<UUID> taxonUuids = new ArrayList<>();
        taxonUuids.add(acacia_acicularis_uuid);
        taxonUuids.add(acacia_cuspidifolia_uuid);
        taxonUuids.add(acacia_sect_botrycephalae_uuid);

        List<GroupedTaxonDTO> result = this.service.groupTaxaByHigherTaxon(taxonUuids, CLASSIFICATION_UUID, minRank, maxRank);
        System.out.println(result);
        Assert.assertEquals(3, result.size());
        //acacia_acicularis_uuid  //is a root taxon with no parents
        Assert.assertEquals(acacia_acicularis_uuid, result.get(0).getTaxonUuid());
        Assert.assertNull(result.get(0).getGroupTaxonUuid());
        Assert.assertTrue(StringUtils.isBlank(result.get(0).getGroupTaxonName()));
        //acacia_cuspidifolia_uuid
        Assert.assertEquals(acacia_cuspidifolia_uuid, result.get(1).getTaxonUuid());
        Assert.assertNotNull(result.get(1).getGroupTaxonUuid());
        Assert.assertFalse(StringUtils.isBlank(result.get(1).getGroupTaxonName()));
        //acacia_sect_botrycephalae_uuid
        Assert.assertEquals(acacia_sect_botrycephalae_uuid, result.get(2).getTaxonUuid());
        Assert.assertNotNull(result.get(2).getGroupTaxonUuid());
        Assert.assertFalse(StringUtils.isBlank(result.get(2).getGroupTaxonName()));
    }

    private UUID acacia_acicularis_uuid  = UUID.fromString("90ad2d8f-19a9-4a10-bab3-7d1de5ce1968");
    private UUID acacia_cuspidifolia_uuid = UUID.fromString("94123e4d-da49-4ed0-9d59-f52a9f7a3618");
    private UUID acacia_sect_botrycephalae_uuid = UUID.fromString("2c73a166-35d1-483d-b8e8-209214cb6193");

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
        //             |-- ×Acacia acicularis Willd. subsp. acicularis         [Subspecies]
        //
        // for more historic Acacia taxonomy see http://lexikon.freenet.de/Akazien

        // 1. create the entities   and save them
        Classification classification = Classification.NewInstance("Acacia Classification");
        classification.setUuid(CLASSIFICATION_UUID);
        classificationDao.save(classification);

        Reference sec = ReferenceFactory.newBook();
        sec.setTitleCache("Sp. Pl.", true);
        referenceDao.save(sec);

        IBotanicalName acacia_n = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS(), "Acacia", null, null, null, null, sec, null, null);
        acacia_n.setAuthorshipCache("N.Jacobsen, Bastm. & Yuji Sasaki", true);
        Taxon acacia_t = Taxon.NewInstance(acacia_n, sec);
        acacia_t.setUuid(UUID.fromString("2fc779ee-7a9d-4586-92ba-1cd774ac77f0"));

        IBotanicalName acacia_subg_aculeiferum_n = TaxonNameFactory.NewBotanicalInstance(Rank.SUBGENUS(), "Acacia", "Aculeiferum", null, null, null, sec, null, null);
        acacia_subg_aculeiferum_n.setAuthorshipCache("Pedley", true);
        Taxon acacia_subg_aculeiferum_t = Taxon.NewInstance(acacia_subg_aculeiferum_n, sec);
        acacia_subg_aculeiferum_t.setUuid(UUID.fromString("169fea08-6b7a-4315-b111-a774c7fafe30"));

        IBotanicalName acacia_subg_phyllodineae_n = TaxonNameFactory.NewBotanicalInstance(Rank.SUBGENUS(), "Acacia", "Phyllodineae", null, null, null, sec, null, null);
        acacia_subg_phyllodineae_n.setAuthorshipCache("N.Jacobsen, Bastm. & Yuji Sasaki", true);
        Taxon acacia_subg_phyllodineae_t = Taxon.NewInstance(acacia_subg_phyllodineae_n, sec);
        acacia_subg_phyllodineae_t.setUuid(UUID.fromString("a9da5d43-517e-4ca5-a490-b6a5cd637e9e"));

        IBotanicalName acacia_setc_botrycephalae_n = TaxonNameFactory.NewBotanicalInstance(Rank.SECTION_BOTANY(), "Acacia", "Botrycephalae", null, null, null, sec, null, null);
        acacia_setc_botrycephalae_n.setAuthorshipCache("Yuji Sasaki", true);
        Taxon acacia_sect_botrycephalae_t = Taxon.NewInstance(acacia_setc_botrycephalae_n, sec);
        acacia_sect_botrycephalae_t.setUuid(acacia_sect_botrycephalae_uuid);

        IBotanicalName acacia_cuspidifolia_n = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Acacia", null,"cuspidifolia", null, null, sec, null, null);
        acacia_cuspidifolia_n.setAuthorshipCache("Maslin", true);
        Taxon acacia_cuspidifolia_t = Taxon.NewInstance(acacia_cuspidifolia_n, sec);
        acacia_cuspidifolia_t.setUuid(acacia_cuspidifolia_uuid);

        IBotanicalName acacia_mearnsii_n = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Acacia", null,"mearnsii", null, null, sec, null, null);
        acacia_mearnsii_n.setAuthorshipCache("Benth", true);
        Taxon acacia_mearnsii_t = Taxon.NewInstance(acacia_mearnsii_n, sec);
        acacia_mearnsii_t.setUuid(UUID.fromString("2e55dc01-71f5-4d42-9bb0-a2448e46dd18"));

        IBotanicalName acacia_acicularis_n = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Acacia", null,"acicularis", null, null, sec, null, null);
        acacia_acicularis_n.setAuthorshipCache("Willd.", true);
        Taxon acacia_acicularis_t = Taxon.NewInstance(acacia_acicularis_n, sec);
        acacia_acicularis_t.setUuid(acacia_acicularis_uuid);

        IBotanicalName xacacia_acicularis_n = TaxonNameFactory.NewBotanicalInstance(Rank.SUBSPECIES(), "Acacia", null,"acicularis", "acicularis", null, sec, null, null);
        xacacia_acicularis_n.setAuthorshipCache("Willd.", true);
        xacacia_acicularis_n.setMonomHybrid(true);
        System.out.println(xacacia_acicularis_n.getTitleCache());
        Taxon xacacia_acicularis_t = Taxon.NewInstance(xacacia_acicularis_n, sec);
        xacacia_acicularis_t.setUuid(UUID.fromString("04d125a6-6adf-4900-97ff-82729618086a"));

        TaxonNode acacia_tn = classification.addChildTaxon(acacia_t, sec, null);
        TaxonNode acacia_subg_phyllodineae_tn = acacia_tn.addChildTaxon(acacia_subg_phyllodineae_t, sec, null);
        acacia_subg_phyllodineae_tn.addChildTaxon(acacia_sect_botrycephalae_t, sec, null);
        acacia_tn.addChildTaxon(acacia_subg_aculeiferum_t, sec, null);
        acacia_tn.addChildTaxon(acacia_mearnsii_t, sec, null).addChildTaxon(xacacia_acicularis_t, sec, null);
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
            "TAXONBASE", "TAXONNAME",
            "REFERENCE",
            "CLASSIFICATION", "TAXONNODE",
            "LANGUAGESTRING", "HOMOTYPICALGROUP",
            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
            },
            fileNameAppendix, true );
    }
}
