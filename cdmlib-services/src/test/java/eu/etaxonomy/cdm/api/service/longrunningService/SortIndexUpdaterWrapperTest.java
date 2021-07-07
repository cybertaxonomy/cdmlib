/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.longrunningService;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.ITermNodeService;
import eu.etaxonomy.cdm.api.service.ITermTreeService;
import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.api.service.config.SortIndexUpdaterConfigurator;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.model.common.ITreeNode;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @since 18.12.2019
 */
@Ignore  //ignore ignored because it currently sometimes influences other tests due to test data
public class SortIndexUpdaterWrapperTest extends CdmTransactionalIntegrationTest {

    private static final UUID uuidTermTree = UUID.fromString("dd24b972-3364-4902-a345-310b8c771e00");

    @SpringBeanByType
    private ITermTreeService termTreeService;

    @SpringBeanByType
    private ITermNodeService termNodeService;

    @SpringBeanByType
    private SortIndexUpdaterWrapper sortIndexUpdater;

    @Test
    @DataSet
    public void test_TaxonNode() {
        //for now only to avoid first dataset loading bug when test is running standalone
    }

    @Test
    @DataSet
    public void testTermNode() {
        try {
            Field sortIndexField = TermNode.class.getDeclaredField("sortIndex");
            sortIndexField.setAccessible(true);

            SortIndexUpdaterConfigurator config = SortIndexUpdaterConfigurator
                    .NewInstance(false, true, false);
            config.setMonitor(DefaultProgressMonitor.NewInstance());
            UpdateResult result = sortIndexUpdater.doInvoke(config);

            Assert.assertEquals("No exception should be thrown during sortindex update", 0, result.getExceptions().size());

            @SuppressWarnings("unchecked")
            TermTree<Feature> termTree = termTreeService.find(uuidTermTree);
            Assert.assertNotNull(termTree);
            TermNode<Feature> rootNode = termTree.getRoot();
            testTreeRecursive(sortIndexField, rootNode);

        } catch (NoSuchFieldException | SecurityException e1) {
            Assert.fail("sortIndex field not found");
        }
    }

    private <T extends ITreeNode<T>> void testTreeRecursive(Field sortIndexField, ITreeNode<T> rootNode) {
        @SuppressWarnings("deprecation")
        List<T> children = rootNode.getChildNodes();
        for (int i = 0 ; i < children.size(); i++){
            try {
                T child = children.get(i);
                Integer sortIndex = (Integer)sortIndexField.get(child);
                Assert.assertEquals((Integer)i, sortIndex);
                testTreeRecursive(sortIndexField, child);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                Assert.fail("sortIndex field not accessible");
            }
        }
    }

    @Test
//    @DataSet
    public void testPolytomousKeyNode() {
        //TODO
    }

    @Test
    @DataSet
    public void testMonitor() {
        SortIndexUpdaterConfigurator config = SortIndexUpdaterConfigurator
                .NewInstance(false, true, false);
        try {
            UpdateResult result = sortIndexUpdater.doInvoke(config);
            Assert.assertEquals("Missing monitor creates exception", 0, result.getExceptions().size());
        } catch (Exception e) {
            Assert.fail("Missing monitor throws exception");
        }
    }

//    @Test
    @Override
    public void createTestDataSet() throws FileNotFoundException {

        List<Feature> featureList = new ArrayList<>();
        featureList.add(Feature.ANATOMY());
        featureList.add(Feature.DESCRIPTION());
        featureList.add(Feature.CONSERVATION());
        TermTree<Feature> tree = TermTree.NewFeatureInstance(featureList);
        tree.setUuid(uuidTermTree);
        TermNode<Feature> descFeature = tree.getRootChildren().get(1);
        descFeature.addChild(Feature.CULTIVATION());
        descFeature.addChild(Feature.DISCUSSION());
        descFeature.addChild(Feature.ECOLOGY());

        termTreeService.save(tree);

        // 2. end the transaction so that all data is actually written to the db
        setComplete();
        endTransaction();

        // use the fileNameAppendix if you are creating a data set file which need to be named differently
        // from the standard name. For example if a single test method needs different data then the other
        // methods the test class you may want to set the fileNameAppendix when creating the data for this method.
        String fileNameAppendix = null;

        // 3.
        writeDbUnitDataSetFile(new String[] {
            "TERMRELATION", "TERMCOLLECTION"
//            ,"HIBERNATE_SEQUENCES" // IMPORTANT!!!
            },
            fileNameAppendix, true );
    }

}
