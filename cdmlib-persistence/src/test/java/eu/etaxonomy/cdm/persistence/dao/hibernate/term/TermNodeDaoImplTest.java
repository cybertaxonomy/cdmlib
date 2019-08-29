/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.term;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureState;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.term.ITermNodeDao;
import eu.etaxonomy.cdm.persistence.dao.term.ITermTreeDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @since 2019-08-16
 */
public class TermNodeDaoImplTest extends CdmTransactionalIntegrationTest {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(TermNodeDaoImplTest.class);

	@SpringBeanByType
	private ITermTreeDao treeDao;

	   @SpringBeanByType
	    private ITermNodeDao nodeDao;

	   @SpringBeanByType
	    private IDefinedTermDao termDao;

	@Before
	public void setUp() {}

    @Test
    @DataSet(value="TermNodeDaoImplTest.xml")
    public void testLoadInapplicable() {
//        List<TermNode> list = nodeDao.list();
        @SuppressWarnings({"unchecked" })
        TermNode<Feature> node = nodeDao.load(UUID.fromString("78c9277c-af28-42f8-b3a9-02f717d9250f"), null);
        Assert.assertNotNull(node);
        Assert.assertEquals(1, node.getOnlyApplicableIf().size());
	}

    @Override
//    @Test
    public void createTestDataSet() throws FileNotFoundException {
        // 1. create the entities   and save them
        TermTree<Feature> featureTree = TermTree.NewFeatureInstance();
        TermNode<Feature> node = featureTree.getRoot().addChild(Feature.NewInstance());
        FeatureState applicable = node.addApplicableState(Feature.NewInstance(), State.NewInstance());
        FeatureState inApplicable = node.addInapplicableState(Feature.NewInstance(), State.NewInstance());

        treeDao.save(featureTree);
        termDao.save(node.getTerm());
        termDao.save(applicable.getFeature());
        termDao.save(inApplicable.getFeature());
        termDao.save(applicable.getState());
        termDao.save(inApplicable.getState());


        // 2. end the transaction so that all data is actually written to the db
        setComplete();
        endTransaction();

        // use the fileNameAppendix if you are creating a data set file which need to be named differently
        // from the standard name. For example if a single test method needs different data then the other
        // methods the test class you may want to set the fileNameAppendix when creating the data for this method.
        String fileNameAppendix = null;

        // 3.
        writeDbUnitDataSetFile(new String[] {
            "TERMCOLLECTION",
            "TERMRELATION",
            "FEATURESTATE",
            "TERMNODE_INAPPLICABLEIF",
            "TERMNODE_ONLYAPPLICABLEIF",
            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
            },
            fileNameAppendix, true );
    }
}
