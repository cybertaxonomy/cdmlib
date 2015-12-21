//// $Id$
///**
//* Copyright (C) 2015 EDIT
//* European Distributed Institute of Taxonomy
//* http://www.e-taxonomy.eu
//*
//* The contents of this file are subject to the Mozilla Public License Version 1.1
//* See LICENSE.TXT at the top of this package for the full license terms.
//*/
//package eu.etaxonomy.cdm.test.integration;
//
//import java.io.FileNotFoundException;
//import java.util.UUID;
//
//import org.unitils.dbunit.annotation.DataSet;
//import org.unitils.spring.annotation.SpringBeanByType;
//
//import eu.etaxonomy.cdm.model.reference.Reference;
//import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
//import eu.etaxonomy.cdm.model.taxon.Classification;
//import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
//import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
//import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
//import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;
//
///**
// * This is only an example for am implementation of the {@link CdmTransactionalIntegrationTest}
// * which is never meant to be executed.
// *
// * @author a.kohlbecker
// * @date Jun 15, 2015
// *
// */
//public class CdmTransactionalIntegrationTestExample extends CdmTransactionalIntegrationTest {
//
//    @SpringBeanByType
//    private ITaxonDao taxonDao;
//    @SpringBeanByType
//    private IClassificationDao classificationDao;
//    @SpringBeanByType
//    private IReferenceDao referenceDao;
//
//    private static final String CLASSIFICATION_UUID = "2a5ceebb-4830-4524-b330-78461bf8cb6b";
//
//    /**
//     * This is an example implementation for {@link CdmTransactionalIntegrationTest#createTestDataSet()}:
//     *
//     * {@inheritDoc}
//     */
//    @Override
//    // @Test // uncomment to write out the test data xml file for this test class
//    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
//    public final void createTestDataSet() throws FileNotFoundException {
//
//        // 1. create the entities   and save them
//        Classification europeanAbiesClassification = Classification.NewInstance("European Abies");
//        europeanAbiesClassification.setUuid(UUID.fromString(CLASSIFICATION_UUID));
//        classificationDao.save(europeanAbiesClassification);
//
//         Reference<?> sec = ReferenceFactory.newBook();
//        sec.setTitleCache("Kohlbecker, A., Testcase standart views, 2013", true);
//        Reference<?> sec_sensu = ReferenceFactory.newBook();
//        sec_sensu.setTitleCache("Komarov, V. L., Flora SSSR 29", true);
//        referenceDao.save(sec);
//        referenceDao.save(sec_sensu);
//
//        // 2. end the transaction so that all data is actually written to the db
//        setComplete();
//        endTransaction();
//
//        // use the fileNameAppendix if you are creating a data set file which need to be named differently
//        // from the standard name. For example if a single test method needs different data then the other
//        // methods the test class you may want to set the fileNameAppendix when creating the data for this method.
//        String fileNameAppendix = null;
//
//        // 3.
//        writeDbUnitDataSetFile(new String[] {
//            "TAXONBASE", "TAXONNAMEBASE",
//            "SYNONYMRELATIONSHIP", "TAXONRELATIONSHIP",
//            "REFERENCE",
//            "AGENTBASE", "HOMOTYPICALGROUP",
//            "CLASSIFICATION", "TAXONNODE",
//            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
//            },
//            fileNameAppendix );
//  }
//
//}
