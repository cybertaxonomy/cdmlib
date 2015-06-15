// $Id$
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

import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.kohlbecker
 * @date Jun 15, 2015
 *
 */
public class ClassificationDaoHibernateImplTest extends CdmTransactionalIntegrationTest {


    @SpringBeanByType
    private ITaxonDao taxonDao;
    @SpringBeanByType
    private IClassificationDao classificationDao;
    @SpringBeanByType
    private IReferenceDao referenceDao;


    private static final String CLASSIFICATION_UUID = "2a5ceebb-4830-4524-b330-78461bf8cb6b";


    /**
     * see http://dev.e-taxonomy.eu/trac/ticket/2778
     */
    @Test
    @DataSet(value="ClassificationDaoHibernateImplTest.issue2778.xml")
    public void testIssue2778() {

        // check preconditions
        List<TaxonBase> taxa = taxonDao.list(null, null);
        assertEquals(4, taxa.size());

        for(TaxonBase t : taxa) {
            assertEquals(Rank.SPECIES().getUuid(), t.getName().getRank().getUuid());
        }

        Classification classification = classificationDao.load(UUID.fromString(CLASSIFICATION_UUID));

        // test for the bug in http://dev.e-taxonomy.eu/trac/ticket/2778
        List<TaxonNode> rootNodes = classificationDao.listRankSpecificRootNodes(classification, Rank.GENUS(), null, null, null);
        assertEquals(4, rootNodes.size());

    }

    /**
     * At the moment the data created is special to the issue http://dev.e-taxonomy.eu/trac/ticket/2778
     * ClassificationDaoHibernateImplTest.issue2778.xml
     *
     * {@inheritDoc}
     */
    @Override
    @Test // uncomment to write out the test data xml file for this test class
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public final void createTestDataSet() throws FileNotFoundException {

    // 1. create the entities   and save them
    Classification europeanAbiesClassification = Classification.NewInstance("European Abies");
    europeanAbiesClassification.setUuid(UUID.fromString(CLASSIFICATION_UUID));
    classificationDao.save(europeanAbiesClassification);

    Reference<?> sec = ReferenceFactory.newBook();
    sec.setTitleCache("Kohlbecker, A., Testcase standart views, 2013", true);
    Reference<?> sec_sensu = ReferenceFactory.newBook();
    sec_sensu.setTitleCache("Komarov, V. L., Flora SSSR 29", true);
    referenceDao.save(sec);
    referenceDao.save(sec_sensu);


    BotanicalName n_abies_alba = BotanicalName.NewInstance(Rank.SPECIES());
    n_abies_alba.setNameCache("Abies alba", true);
    Taxon t_abies_alba = Taxon.NewInstance(n_abies_alba, sec);
    taxonDao.save(t_abies_alba);

    BotanicalName n_abies_grandis = BotanicalName.NewInstance(Rank.SPECIES());
    n_abies_grandis.setNameCache("Abies grandis", true);
    Taxon t_abies_grandis = Taxon.NewInstance(n_abies_grandis, sec);
    taxonDao.save(t_abies_grandis);

    BotanicalName n_abies_kawakamii = BotanicalName.NewInstance(Rank.SPECIES());
    n_abies_kawakamii.setNameCache("Abies kawakamii", true);
    Taxon t_abies_kawakamii = Taxon.NewInstance(n_abies_kawakamii, sec);
    taxonDao.save(t_abies_kawakamii);

    BotanicalName n_abies_lasiocarpa = BotanicalName.NewInstance(Rank.SPECIES());
    n_abies_lasiocarpa.setNameCache("Abies lasiocarpa", true);
    Taxon t_abies_lasiocarpa = Taxon.NewInstance(n_abies_lasiocarpa, sec);
    taxonDao.save(t_abies_lasiocarpa);

    // add taxa to classifications
    europeanAbiesClassification.addChildTaxon(t_abies_alba, null, null);
    europeanAbiesClassification.addChildTaxon(t_abies_grandis, null, null);
    europeanAbiesClassification.addChildTaxon(t_abies_kawakamii, null, null);
    europeanAbiesClassification.addChildTaxon(t_abies_lasiocarpa, null, null);
    classificationDao.saveOrUpdate(europeanAbiesClassification);

    // 2. end the transaction so that all data is actually written to the db
    setComplete();
    endTransaction();

    // use the fileNameAppendix if you are creating a data set file which need to be named differently
    // from the standard name. Fir example if a single test method needs different data then the other
    // methods the test class you may want to set the fileNameAppendix when creating the data for this method.
    String fileNameAppendix = "issue2778";

    // 3.
    writeDbUnitDataSetFile(new String[] {
        "TAXONBASE", "TAXONNAMEBASE",
        "REFERENCE",
        "AGENTBASE","HOMOTYPICALGROUP",
        "CLASSIFICATION", "TAXONNODE",
        "HIBERNATE_SEQUENCES" // IMPORTANT!!!
        },
        fileNameAppendix );
  }

}
