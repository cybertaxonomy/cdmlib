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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultExport;
import eu.etaxonomy.cdm.io.common.ExportDataWrapper;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.TARGET;
import eu.etaxonomy.cdm.io.descriptive.owl.out.OwlExportConfigurator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 * @since 25.06.2017
 *
 */
public class OwlExportTest  extends CdmTransactionalIntegrationTest{
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(OwlExportTest.class);

    @SpringBeanByName
    private CdmApplicationAwareDefaultExport<OwlExportConfigurator> defaultExport;

    @SpringBeanByType
    private IClassificationService classificationService;

    @SpringBeanByType
    private ITaxonNodeService taxonNodeService;


    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
    public void testEmptyData(){
        File destinationFolder = null;
        OwlExportConfigurator config = OwlExportConfigurator.NewInstance(null, destinationFolder, createFeatureTree());
        config.setTarget(TARGET.EXPORT_DATA);
        ExportResult result = defaultExport.invoke(config);
        System.out.println(result.createReport());
        ExportDataWrapper<?> exportData = result.getExportData();
    }


    /**
     * {@inheritDoc}
     */
    public FeatureTree createFeatureTree() {
        FeatureTree tree = FeatureTree.NewInstance();
        tree.setRoot(FeatureNode.NewInstance());

        FeatureNode nodeA = FeatureNode.NewInstance(Feature.NewInstance("A", "A", "A"));
        FeatureNode nodeA1 = FeatureNode.NewInstance(Feature.NewInstance("A1", "A1", "A1"));
        FeatureNode nodeA2 = FeatureNode.NewInstance(Feature.NewInstance("A2", "A2", "A2"));
        FeatureNode nodeB = FeatureNode.NewInstance(Feature.NewInstance("B", "B", "B"));
        FeatureNode nodeB1 = FeatureNode.NewInstance(Feature.NewInstance("B", "B1", "B1"));
        FeatureNode nodeC = FeatureNode.NewInstance(Feature.NewInstance("C", "C", "C"));

        tree.getRoot().addChild(nodeA);
        nodeA.addChild(nodeA1);
        nodeA.addChild(nodeA2);
        tree.getRoot().addChild(nodeB);
        nodeB.addChild(nodeB1);
        tree.getRoot().addChild(nodeC);
        return tree;
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
