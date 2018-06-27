/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dto.ClassificationLookupDTO;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.kohlbecker
 * @since Jun 15, 2015
 */
public class ClassificationDaoHibernateImplTest extends CdmTransactionalIntegrationTest {


    @SpringBeanByType
    private ITaxonDao taxonDao;
    @SpringBeanByType
    private IClassificationDao classificationDao;
    @SpringBeanByType
    private IReferenceDao referenceDao;

    private boolean includeUnpublished;

    private static final String CLASSIFICATION_UUID = "2a5ceebb-4830-4524-b330-78461bf8cb6b";
    private static final String CLASSIFICATION_FULL_UUID = "a71467a6-74dc-4148-9530-484628a5ab0e";
    private static final UUID UUID_ABIES = UUID.fromString("19f560d9-a555-4883-9c54-39d04872307c");
    private static final UUID UUID_PINACEAE = UUID.fromString("74216ed8-5f04-439e-87e0-500738f5e7fc");


    @Before
    public void setUp() {
        includeUnpublished = true;
    }

    /**
     * see http://dev.e-taxonomy.eu/trac/ticket/2778
     * Classification/{uuid}/childNodesAt/{rank-uuid} fails if only species in database
     */
    @Test
    @DataSet(value="ClassificationDaoHibernateImplTest.listRankSpecificRootNodes.xml")
    public void testListRankSpecificRootNodesFlatHierarchie() {

        checkPreconditions();

        Classification classification = classificationDao.load(UUID.fromString(CLASSIFICATION_UUID));

        includeUnpublished = true;
        // test for the bug in http://dev.e-taxonomy.eu/trac/ticket/2778
        Rank rank = Rank.GENUS();
        // run both queries in dao method since rank != null
        List<TaxonNode> rootNodes = classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished,
                null, null, null, 0);
        rootNodes.addAll(classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished,
                null, null, null, 1));
        assertEquals(3, rootNodes.size());

        rank = null;
        // run only fist query in dao method since rank == null
        rootNodes = classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished,
                null, null, null, 0);
        assertEquals("The absolut root nodes should be returned", 3, rootNodes.size());

        //no unpublished
        includeUnpublished = false;

        rank = Rank.GENUS();
        // run both queries in dao method since rank != null
        rootNodes = classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished,
                null, null, null, 0);
        rootNodes.addAll(classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished,
                null, null, null, 1));
        assertEquals(2, rootNodes.size());  //5002 in unpublished

        rank = null;
        rootNodes = classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished,
                null, null, null, 0);
        assertEquals("The absolut root nodes should be returned", 2, rootNodes.size());
    }


    /**
     * Test listRankSpecificRootNode with an existing classification
     */
    @Test
    @DataSet(value="ClassificationDaoHibernateImplTest.listRankSpecificRootNodes.xml")
    public void testListRankSpecificRootNodesWithHierarchie() {

        // check preconditions
    	checkPreconditions();

        Classification classification = classificationDao.load(UUID.fromString(CLASSIFICATION_FULL_UUID));
        includeUnpublished = false;
        Rank rank = Rank.GENUS();
        // run both queries in dao method since rank != null
        List<TaxonNode> rootNodes = classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished, null, null, null, 0);
        rootNodes.addAll(classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished, null, null, null, 1));
        assertEquals("Only the genus should come back", 1, rootNodes.size());
        assertEquals(Rank.GENUS(), rootNodes.get(0).getTaxon().getName().getRank());
        assertEquals(UUID_ABIES, rootNodes.get(0).getTaxon().getUuid());

        rank = Rank.SUBGENUS();
        // run both queries in dao method since rank != null
        includeUnpublished = true;
        rootNodes = classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished, null, null, null, 0);
        rootNodes.addAll(classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished, null, null, null, 1));
        assertEquals("Only the 2 species should come back", 2, rootNodes.size());
        for (TaxonNode tn : rootNodes){
        	assertEquals(Rank.SPECIES(), tn.getTaxon().getName().getRank());
        }
        // run both queries in dao method since rank != null
        includeUnpublished = false;
        rootNodes = classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished, null, null, null, 0);
        rootNodes.addAll(classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished, null, null, null, 1));
        assertEquals("Only the 1 published species should come back", 1, rootNodes.size());
        for (TaxonNode tn : rootNodes){
            assertEquals(Rank.SPECIES(), tn.getTaxon().getName().getRank());
        }

        rank = Rank.SUBFAMILY();
        // run both queries in dao method since rank != null
        rootNodes = classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished, null, null, null, 0);
        rootNodes.addAll(classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished, null, null, null, 1));
        assertEquals("Only the genus should come back", 1, rootNodes.size());
        assertEquals(Rank.GENUS(), rootNodes.get(0).getTaxon().getName().getRank());
        assertEquals(UUID_ABIES, rootNodes.get(0).getTaxon().getUuid());

        rank = Rank.FAMILY();
        // run both queries in dao method since rank != null
        rootNodes = classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished, null, null, null, 0);
        rootNodes.addAll(classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished, null, null, null, 1));
        assertEquals("Only the family should come back", 1, rootNodes.size());
        assertEquals(Rank.FAMILY(), rootNodes.get(0).getTaxon().getName().getRank());
        assertEquals(UUID_PINACEAE, rootNodes.get(0).getTaxon().getUuid());

        rank = null;
        // run only fist query in dao method since rank == null
        rootNodes = classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished, null, null, null, 0);
        assertEquals("Only the family as the absolut root node should come back", 1, rootNodes.size());
        assertEquals(Rank.FAMILY(), rootNodes.get(0).getTaxon().getName().getRank());
        assertEquals(UUID_PINACEAE, rootNodes.get(0).getTaxon().getUuid());

    }

    /**
     * Test listRankSpecificRootNode with all classifications
     */
    @Test
    @DataSet(value="ClassificationDaoHibernateImplTest.listRankSpecificRootNodes.xml")
    public void testListRankSpecificRootNodesWithNoClassification() {
    	Classification classification = null;

    	Rank rank = Rank.GENUS();
        // run both queries in dao method since rank != null
        List<TaxonNode> rootNodes = classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished, null, null, null, 0);
        rootNodes.addAll(classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished, null, null, null, 1));
        assertEquals("3 Species from no hierarchie and 1 genus from hierarchie should return", 4, rootNodes.size());

        rank = null;
        // run only fist query in dao method since rank == null
        rootNodes = classificationDao.listRankSpecificRootNodes(classification, rank, includeUnpublished, null, null, null, 0);
        assertEquals("4 taxa should return (3 species from no hierarchie, 1 family, from hierarchie classification", 4, rootNodes.size());
    }

    private void checkPreconditions() {
		// check preconditions
        List<TaxonBase> taxa = taxonDao.list(null, null);
        assertEquals(5, taxa.size());

//        for(TaxonBase t : taxa) {
//            assertEquals(Rank.SPECIES().getUuid(), t.getName().getRank().getUuid());
//        }
	}

    @Test
    @DataSet(value="ClassificationDaoHibernateImplTest.listRankSpecificRootNodes.xml")
    public void testClassificationLookup() {

        Classification classification = classificationDao.load(UUID.fromString(CLASSIFICATION_FULL_UUID));
        ClassificationLookupDTO classificationLookupDto = classificationDao.classificationLookup(classification);
        assertEquals(4, classificationLookupDto.getTaxonIds().size());
    }


    /**
     * At the moment the data created is special to the issue http://dev.e-taxonomy.eu/trac/ticket/2778
     * ClassificationDaoHibernateImplTest.issue2778.xml
     *
     * {@inheritDoc}
     */
    @Override
//    @Test // uncomment to write out the test data xml file for this test class
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public final void createTestDataSet() throws FileNotFoundException {

	    // 1. create the entities   and save them
	    Classification flatHierarchieClassification = Classification.NewInstance("European Abies");
	    flatHierarchieClassification.setUuid(UUID.fromString(CLASSIFICATION_UUID));
	    classificationDao.save(flatHierarchieClassification);

	    Reference sec = ReferenceFactory.newBook();
	    sec.setTitleCache("Kohlbecker, A., Testcase standart views, 2013", true);
	    Reference sec_sensu = ReferenceFactory.newBook();
	    sec_sensu.setTitleCache("Komarov, V. L., Flora SSSR 29", true);
	    referenceDao.save(sec);
	    referenceDao.save(sec_sensu);


	    IBotanicalName n_abies_alba = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
	    n_abies_alba.setNameCache("Abies alba", true);
	    Taxon t_abies_alba = Taxon.NewInstance(n_abies_alba, sec);
	    taxonDao.save(t_abies_alba);

	    IBotanicalName n_abies_grandis = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
	    n_abies_grandis.setNameCache("Abies grandis", true);
	    Taxon t_abies_grandis = Taxon.NewInstance(n_abies_grandis, sec);
	    taxonDao.save(t_abies_grandis);

	    IBotanicalName n_abies_kawakamii = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
	    n_abies_kawakamii.setNameCache("Abies kawakamii", true);
	    Taxon t_abies_kawakamii = Taxon.NewInstance(n_abies_kawakamii, sec);
	    taxonDao.save(t_abies_kawakamii);

//	    BotanicalName n_abies_lasiocarpa = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
//	    n_abies_lasiocarpa.setNameCache("Abies lasiocarpa", true);
//	    Taxon t_abies_lasiocarpa = Taxon.NewInstance(n_abies_lasiocarpa, sec);
//	    taxonDao.save(t_abies_lasiocarpa);

	    IBotanicalName n_abies = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
	    n_abies.setNameCache("Abies", true);
	    Taxon t_abies = Taxon.NewInstance(n_abies, sec);
	    t_abies.setUuid(UUID_ABIES);
	    taxonDao.save(t_abies);

	    IBotanicalName n_pinaceae = TaxonNameFactory.NewBotanicalInstance(Rank.FAMILY());
	    n_pinaceae.setNameCache("Pinaceae", true);
	    Taxon t_pinaceae = Taxon.NewInstance(n_pinaceae, sec);
	    t_pinaceae.setUuid(UUID_PINACEAE);
	    taxonDao.save(t_pinaceae);


	    // add taxa to classifications
	    flatHierarchieClassification.addChildTaxon(t_abies_alba, null, null);
	    flatHierarchieClassification.addChildTaxon(t_abies_grandis, null, null);
	    flatHierarchieClassification.addChildTaxon(t_abies_kawakamii, null, null);
//	    flatHierarchieClassification.addChildTaxon(t_abies_lasiocarpa, null, null);
	    classificationDao.saveOrUpdate(flatHierarchieClassification);

	    // 1. create the entities   and save them
	    Classification fullHierarchieClassification = Classification.NewInstance("European Abies full hierarchie");
	    fullHierarchieClassification.setUuid(UUID.fromString(CLASSIFICATION_FULL_UUID));
	    classificationDao.save(fullHierarchieClassification);

	    fullHierarchieClassification.addParentChild(t_pinaceae, t_abies, null, null);
	    fullHierarchieClassification.addParentChild(t_abies, t_abies_alba, null, null);
	    fullHierarchieClassification.addParentChild(t_abies, t_abies_grandis, null, null);

	    classificationDao.saveOrUpdate(fullHierarchieClassification);



	    // 2. end the transaction so that all data is actually written to the db
	    setComplete();
	    endTransaction();

	    // use the fileNameAppendix if you are creating a data set file which need to be named differently
	    // from the standard name. Fir example if a single test method needs different data then the other
	    // methods the test class you may want to set the fileNameAppendix when creating the data for this method.
	    String fileNameAppendix = "listRankSpecificRootNodes";

	    // 3.
	    writeDbUnitDataSetFile(new String[] {
	        "TAXONBASE", "TAXONNAME",
	        "REFERENCE",
	        "AGENTBASE","HOMOTYPICALGROUP",
	        "CLASSIFICATION", "TAXONNODE",
	        "LANGUAGESTRING",
	        "HIBERNATE_SEQUENCES" // IMPORTANT!!!
	        },
	        fileNameAppendix );
  }

}
