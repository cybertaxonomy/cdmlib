// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.application.CdmApplicationDefaultConfiguration;
import eu.etaxonomy.cdm.api.service.description.TransmissionEngineOccurrence;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.kohlbecker
 * @date Feb 26, 2013
 *
 */
public class TransmissionEngineOccurrenceTest extends CdmTransactionalIntegrationTest {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(DescriptionServiceImplTest.class);

    private static final UUID T_LAPSANA_UUID = UUID.fromString("f65d47bd-4f49-4ab1-bc4a-bc4551eaa1a8");

    private static final UUID T_LAPSANA_COMMUNIS_UUID = UUID.fromString("2a5ceebb-4830-4524-b330-78461bf8cb6b");

    @SpringBeanByType
    private IDescriptionService descriptionService;

    @SpringBeanByType
    private ITermService termService;

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private IClassificationService classificationService;

    @SpringBeanByType
    private IReferenceService referenceService;

    @SpringBeanByType
    private CdmApplicationDefaultConfiguration repo;

    @SpringBeanByType
    private TransmissionEngineOccurrence engine;


    @Before
    public void setUp() {
        List<NamedArea> superAreas = Arrays.asList(new NamedArea[]{
                TdwgArea.getAreaByTdwgAbbreviation("YUG")
        });
        Rank lowerRank = Rank.SPECIES();
        Rank upperRank = Rank.GENUS();
        engine.updatePriorities();
    }

//    @Test
    @DataSet
    public void testPriorities() {
        Set extensions = termService.load(PresenceTerm.CULTIVATED().getUuid()).getExtensions();
        Assert.assertEquals(TransmissionEngineOccurrence.EXTENSION_VALUE_PREFIX + "45", ((Extension)extensions.iterator().next()).getValue());
    }

    @Test
    public void createTestData() throws FileNotFoundException {

        // --- References --- //
        Reference sec = ReferenceFactory.newDatabase();
        sec.setTitleCache("Test", true);
        Reference nomRef = ReferenceFactory.newBook();
        sec.setTitleCache("Sp.Pl.", true);

        referenceService.save(sec);
        referenceService.save(nomRef);

        // --- Taxa --- //
        //  Lapsana
        //        L. communis
        //            L. communis subsp. communis
        //            L. communis subsp. adenophora
        //            L. communis subsp. alpina
        //  Sonchella
        //        S. dentata
        //        S. stenoma
        BotanicalName n_lapsana = BotanicalName.NewInstance(Rank.GENUS());
        n_lapsana.setTitleCache("Lapsana", true);
        Taxon t_lapsana = Taxon.NewInstance(n_lapsana, sec);
        t_lapsana.setUuid(T_LAPSANA_UUID);
        taxonService.saveOrUpdate(t_lapsana);

        BotanicalName n_lapsana_communis = BotanicalName.NewInstance(Rank.SPECIES());
        n_lapsana.setTitleCache("L. communis", true);
        Taxon t_lapsana_communis = Taxon.NewInstance(n_lapsana, sec);
        t_lapsana.setUuid(T_LAPSANA_COMMUNIS_UUID);
        taxonService.saveOrUpdate(t_lapsana_communis);

        BotanicalName n_lapsana_communis_communis = BotanicalName.NewInstance(Rank.SUBSPECIES());
        n_lapsana.setTitleCache("L. communis subsp. communis", true);
        Taxon t_lapsana_communis_communis = Taxon.NewInstance(n_lapsana, sec);
        taxonService.saveOrUpdate(t_lapsana_communis_communis);

        BotanicalName n_lapsana_communis_adenophora = BotanicalName.NewInstance(Rank.SUBSPECIES());
        n_lapsana.setTitleCache("L. communis subsp. adenophora", true);
        Taxon t_lapsana_communis_adenophora = Taxon.NewInstance(n_lapsana, sec);
        taxonService.saveOrUpdate(t_lapsana_communis_adenophora);

        BotanicalName n_lapsana_communis_alpina = BotanicalName.NewInstance(Rank.SUBSPECIES());
        n_lapsana.setTitleCache("L. communis subsp. alpina", true);
        Taxon t_lapsana_communis_alpina = Taxon.NewInstance(n_lapsana, sec);
        taxonService.saveOrUpdate(t_lapsana_communis_alpina);

        // --- Classification --- //
        Classification classification = Classification.NewInstance("TestClassification");
        classificationService.save(classification);
        TaxonNode node_lapsana = classification.addChildTaxon(t_lapsana, sec, null, null);
        TaxonNode node_lapsana_communis = node_lapsana.addChildTaxon(t_lapsana_communis, sec, null, null);
        node_lapsana_communis.addChildTaxon(t_lapsana_communis_communis, sec, null, null);
        node_lapsana_communis.addChildTaxon(t_lapsana_communis_adenophora, sec, null, null);
        node_lapsana_communis.addChildTaxon(t_lapsana_communis_alpina, sec, null, null);
        classificationService.save(classification);

        // --- Distributions --- //
        // tdwg3 level YUG :  Yugoslavia
        // contains tdwg4 level areas :
        //   YUG-BH	Bosnia-Herzegovina
        //   YUG-CR	Croatia
        //   YUG-KO	Kosovo
        //   YUG-MA	Macedonia
        //   YUG-MN	Montenegro

        // assigning distribution information to taxa
        // expectations regarding the aggregation can be found in the comments below
//        TaxonDescription d_lapsana_communis_communis = TaxonDescription.NewInstance(t_lapsana_communis_communis);
//        d_lapsana_communis_communis.addElement(Distribution.NewInstance(
//                    TdwgArea.getAreaByTdwgAbbreviation("YUG-MN"),
//                    PresenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA() // should be ignored
//                    );

        setComplete();
        endTransaction();

        printDataSet(new FileOutputStream(this.getClass().getSimpleName() + ".xml"), new String[] {
            "TAXONBASE", "TAXONNAMEBASE",
            "REFERENCE", "DESCRIPTIONELEMENTBASE", "DESCRIPTIONBASE",
            "AGENTBASE", "CLASSIFICATION", "CLASSIFICATION_TAXONNODE", "TAXONNODE",
            "LANGUAGESTRING"});

    }

}
