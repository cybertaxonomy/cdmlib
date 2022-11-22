/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.owl.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITermTreeService;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultExport;
import eu.etaxonomy.cdm.io.common.ExportDataWrapper;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.TARGET;
import eu.etaxonomy.cdm.io.descriptive.owl.out.StructureTreeOwlExportConfigurator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 * @since 25.06.2017
 */
public class OwlExportTest  extends CdmTransactionalIntegrationTest{

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @SpringBeanByName
    private CdmApplicationAwareDefaultExport<StructureTreeOwlExportConfigurator> defaultExport;

    @SpringBeanByType
    private IClassificationService classificationService;

    @SpringBeanByType
    private ITermTreeService termTreeService;

    @SpringBeanByType
    private ITaxonNodeService taxonNodeService;


    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void testEmptyData(){
        File destinationFolder = null;
        StructureTreeOwlExportConfigurator config = StructureTreeOwlExportConfigurator.NewInstance();
        config.setFeatureTreeUuids(createFeatureTree());
        config.setVocabularyUuids(Collections.EMPTY_LIST);
        config.setDestination(destinationFolder);
        config.setTarget(TARGET.EXPORT_DATA);
        ExportResult result = defaultExport.invoke(config);
        System.out.println(result.createReport());
        ExportDataWrapper<?> exportData = result.getExportData();
    }

    public List<UUID> createFeatureTree() {
        TermTree<Feature> tree = TermTree.NewFeatureInstance();
        TermVocabulary<Feature> voc = TermVocabulary.NewInstance(TermType.Feature, Feature.class,
                "voc description", "vocabulary", "voc", URI.create("http://test.voc"));

        Feature featureA = Feature.NewInstance("A", "A", "A");
        voc.addTerm(featureA);
        TermNode<Feature> nodeA = tree.getRoot().addChild(featureA);

        Feature featureA1 = Feature.NewInstance("A1", "A1", "A1");
        voc.addTerm(featureA1);
        TermNode<Feature> nodeA1 = nodeA.addChild(featureA1);

        Feature featureA2 = Feature.NewInstance("A2", "A2", "A2");
        voc.addTerm(featureA2);
        TermNode<Feature> nodeA2 = nodeA.addChild(featureA2);

        Feature featureB = Feature.NewInstance("B", "B", "B");
        voc.addTerm(featureB);
        TermNode<Feature> nodeB = tree.getRoot().addChild(featureB);

        Feature featureB1 = Feature.NewInstance("B", "B1", "B1");
        voc.addTerm(featureB1);
        TermNode<Feature> nodeB1 = nodeB.addChild(featureB1);

        Feature featureC = Feature.NewInstance("C", "C", "C");
        voc.addTerm(featureC);
        TermNode<Feature> nodeC = tree.getRoot().addChild(featureC);

        termTreeService.save(tree);
        return Collections.singletonList(tree.getUuid());
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        //      try {
        //      writeDbUnitDataSetFile(new String[] {
        //              "Classification",
        //      }, "testAttachDnaSampleToDerivedUnit");
        //  } catch (FileNotFoundException e) {
        //      e.printStackTrace();
        //  }
    }


    private void setUuid(CdmBase cdmBase, String uuidStr) {
        cdmBase.setUuid(UUID.fromString(uuidStr));
    }

}
