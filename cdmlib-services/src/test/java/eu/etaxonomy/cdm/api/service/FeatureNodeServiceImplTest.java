/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.FeatureNodeDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.NodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.ITreeNode;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @created 2013-09-28
 */
public class FeatureNodeServiceImplTest extends CdmTransactionalIntegrationTest{

	private static final String sep = ITreeNode.separator;
	private static final String pref = ITreeNode.treePrefix;

	@SpringBeanByType
	private IFeatureNodeService featureNodeService;

	@SpringBeanByType
	private IFeatureTreeService featureTreeService;

	@SpringBeanByType
	private ITermService termService;


//	private static final UUID t2Uuid = UUID.fromString("55c3e41a-c629-40e6-aa6a-ff274ac6ddb1");
//	private static final UUID t3Uuid = UUID.fromString("2659a7e0-ff35-4ee4-8493-b453756ab955");
	private static final UUID featureTreeUuid = UUID.fromString("6c2bc8d9-ee62-4222-be89-4a8e31770878");
	private static final UUID featureTree2Uuid = UUID.fromString("43d67247-936f-42a3-a739-bbcde372e334");
	private static final UUID node2Uuid= UUID.fromString("484a1a77-689c-44be-8e65-347d835f47e8");
	private static final UUID node3Uuid = UUID.fromString("2d41f0c2-b785-4f73-a436-cc2d5e93cc5b");
	private static final UUID node4Uuid = UUID.fromString("fdaec4bd-c78e-44df-ae87-28f18110968c");
	private static final UUID node5Uuid = UUID.fromString("c4d5170a-7967-4dac-ab76-ae2019eefde5");
	private static final UUID node6Uuid = UUID.fromString("b419ba5e-9c8b-449c-ad86-7abfca9a7340");

	private Feature t1;
	private Feature t2;
//	private Synonym s1;
	private FeatureNode node3;
	private FeatureNode node2;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testIndexCreatRoot() {
		FeatureTree featureTree = FeatureTree.NewInstance();
		featureTreeService.save(featureTree);

		Feature feature = (Feature)termService.find(914);
		FeatureNode newNode = FeatureNode.NewInstance(feature);
		featureTree.getRoot().addChild(newNode);

		featureTreeService.save(featureTree);

		featureNodeService.saveOrUpdate(newNode);

		commitAndStartNewTransaction(new String[]{"FeatureNode"});
		newNode = featureNodeService.load(newNode.getUuid());
		Assert.assertEquals("", sep + pref+featureTree.getId()+sep + featureTree.getRoot().getId()+ sep  + newNode.getId() + sep, newNode.treeIndex());
	}


	@Test
	@DataSet(value="FeatureNodeServiceImplTest-indexing.xml")
	public final void testIndexCreateNode() {
		Feature feature = (Feature)termService.find(914);

		node2 = featureNodeService.load(node2Uuid);
		String oldTreeIndex = node2.treeIndex();

		FeatureNode newNode = FeatureNode.NewInstance(feature);
		node2.addChild(newNode);
		featureNodeService.saveOrUpdate(node2);

		commitAndStartNewTransaction(new String[]{"FeatureNode"});
		newNode = featureNodeService.load(newNode.getUuid());
		Assert.assertEquals("", oldTreeIndex + newNode.getId() + sep, newNode.treeIndex());
	}


	@Test
	@DataSet(value="FeatureNodeServiceImplTest-indexing.xml")
	public final void testIndexMoveNode() {
		//in feature tree
		FeatureTree featureTree = featureTreeService.load(featureTreeUuid);
		node2 = featureNodeService.load(node2Uuid);
		node3 = featureNodeService.load(node3Uuid);
		node3.addChild(node2);
		featureNodeService.saveOrUpdate(node2);
		commitAndStartNewTransaction(new String[]{"FeatureNode"});
		FeatureNode node6 = featureNodeService.load(node6Uuid);
		Assert.assertEquals("Node6 treeindex is not correct", node3.treeIndex() + "2#4#6#", node6.treeIndex());

		//root of new feature tree
		FeatureTree featureTree2 = featureTreeService.load(featureTree2Uuid);
		node2 = featureNodeService.load(node2Uuid);
		featureTree2.getRoot().addChild(node2);
		featureNodeService.saveOrUpdate(node2);
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		node2 = featureNodeService.load(node2Uuid);
		Assert.assertEquals("Node2 treeindex is not correct", "#t2#7#2#", node2.treeIndex());
		node6 = featureNodeService.load(node6Uuid);
		Assert.assertEquals("Node6 treeindex is not correct", "#t2#7#2#4#6#", node6.treeIndex());

		//into new classification
		node3 = featureNodeService.load(node3Uuid);
		FeatureNode node5 = featureNodeService.load(node5Uuid);
		node5.addChild(node3);
		featureNodeService.saveOrUpdate(node5);
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		node3 = featureNodeService.load(node3Uuid);
		Assert.assertEquals("Node3 treeindex is not correct", "#t2#7#2#5#3#", node3.treeIndex());

	}

	@Test  //here we may have a test for testing delete of a node and attaching the children
	//to its parents, however this depends on the way delete is implemented and therefore needs
	//to wait until this is finally done
	@DataSet(value="FeatureNodeServiceImplTest-indexing.xml")
	public final void testIndexDeleteNode() {
		node2 = featureNodeService.load(node2Uuid);
		FeatureNode root = node2.getParent();
		FeatureNodeDeletionConfigurator config = new FeatureNodeDeletionConfigurator();
		config.setDeleteElement(false);
        config.setChildHandling(ChildHandling.MOVE_TO_PARENT);
        DeleteResult result = featureNodeService.deleteFeatureNode(node2Uuid, config);
        commitAndStartNewTransaction(new String[]{"TaxonNode"});
        FeatureTree tree1 = featureTreeService.load(featureTreeUuid);
        assertNotNull(tree1);
        node2 = featureNodeService.load(node2Uuid);
        assertNull(node2);
        node3 = featureNodeService.load(node3Uuid);
        assertNotNull(node3);
        FeatureNode node4 = featureNodeService.load(node4Uuid);
        assertNotNull(node4);
		config.setDeleteElement(false);
		config.setChildHandling(ChildHandling.DELETE);
		result = featureNodeService.deleteFeatureNode(node4Uuid, config);
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		tree1 = featureTreeService.load(featureTreeUuid);
		node4 = featureNodeService.load(node4Uuid);
        assertNull(node4);
        FeatureNode node6 = featureNodeService.load(node6Uuid);
        assertNull(node6);

		HibernateProxyHelper.deproxy(tree1, FeatureTree.class);
		FeatureNode rootNode = HibernateProxyHelper.deproxy(tree1.getRoot(), FeatureNode.class);
		assertNotNull(tree1);
		featureTreeService.delete(tree1.getUuid());
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		tree1 = featureTreeService.load(featureTreeUuid);
		assertNull(tree1);

	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub
    }





}
