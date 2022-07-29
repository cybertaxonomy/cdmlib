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
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

public class PolytomousKeyNodeServiceTest extends CdmTransactionalIntegrationTest {

    String[] tableNames = new String[]{"POLYTOMOUSKEY","POLYTOMOUSKEYNODE","POLYTOMOUSKEYNODE_AUD"};

	@SpringBeanByType
	private IPolytomousKeyNodeService service;

	@SpringBeanByType
	private IPolytomousKeyService keyService;

	/****************** TESTS *****************************/

	@Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
	public final void testSetDao() {
		Assert.assertNotNull(service);
		Assert.assertNotNull(keyService);
	}

	@Test
    public final void testDelete(){

	    //create key
        PolytomousKey key = PolytomousKey.NewTitledInstance("TestPolytomousKey");
        keyService.save(key);
        PolytomousKeyNode root1 = PolytomousKeyNode.NewInstance("Test statement");
        key.setRoot(root1);
        key.setStartNumber(0);

        PolytomousKeyNode child2 = PolytomousKeyNode.NewInstance("Test statement Nr 2");
        UUID uuidChild2 = child2.getUuid();
        root1.addChild(child2,0);
        service.save(root1);

        PolytomousKeyNode child3 = PolytomousKeyNode.NewInstance("Test statement Nr 3");
        UUID uuidChild3 = child3.getUuid();
        child2.addChild(child3, 0);
        service.save(child2).getUuid();

        PolytomousKeyNode child4 = PolytomousKeyNode.NewInstance("Test statement Nr 4");
        UUID uuidChild4 = child4.getUuid();
        child3.addChild(child4, 0);
        service.save(child3).getUuid();

        //assert key
        PolytomousKeyNode loaded3 = service.load(uuidChild3);
        assertNotNull(loaded3);

        //assert delete without children
        service.delete(uuidChild3, false);
        loaded3 = service.load(uuidChild3);
        assertNull(loaded3);
        PolytomousKeyNode loaded4 = service.load(uuidChild4);
        assertNotNull("Child4 should be moved to "
                + "parent child2 but not removed completely", loaded4);

        //assert delete with children
        PolytomousKeyNode loaded2 = service.load(uuidChild2);
        assertNotNull(loaded2);
        service.delete(uuidChild2, true);
        loaded2 = service.load(uuidChild2);
        assertNull(loaded2);
        loaded4 = service.load(uuidChild4);
        assertNull("Child4 should be deleted with child3 this time", loaded4);
    }

    @Test  //8127  //5536 //10101
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public final void testMergeDetached(){

        //create key with 2 child nodes
        PolytomousKey key = PolytomousKey.NewTitledInstance("TestPolytomousKey");
        PolytomousKeyNode child1 = PolytomousKeyNode.NewInstance("Test statement child1");
        PolytomousKeyNode child2 = PolytomousKeyNode.NewInstance("Test statement child2");
        key.getRoot().addChild(child1);
        key.getRoot().addChild(child2);
        keyService.merge(key);
        commitAndStartNewTransaction();

        //load root node and make it detached
        PolytomousKeyNode rootNode = service.find(key.getRoot().getUuid());
        rootNode.getChildren().get(0);  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        PolytomousKeyNode childToRemove = rootNode.getChildren().get(0);
        rootNode.removeChild(childToRemove);
        PolytomousKeyNode child3 = PolytomousKeyNode.NewInstance("Test statement child3");
        rootNode.addChild(child3);
        service.merge(rootNode, childToRemove);
        commitAndStartNewTransaction(tableNames);

        //test result
        rootNode = service.find(key.getRoot().getUuid());
        rootNode.getChildren();
        Assert.assertEquals(2, rootNode.getChildren().size());
        Assert.assertEquals(child2.getUuid(), rootNode.getChildren().get(0).getUuid());
        Assert.assertEquals(child3.getUuid(), rootNode.getChildren().get(1).getUuid());
        Assert.assertEquals("Should be root + 2 children", 3, service.count(PolytomousKeyNode.class));
        commitAndStartNewTransaction();

//        System.out.println("NEXT");
        //same with key
        //load root node and make it detached
        PolytomousKey keyLoaded = keyService.find(key.getUuid());
        rootNode = keyLoaded.getRoot();
        rootNode.getChildren().get(0);  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        childToRemove = rootNode.getChildren().get(0);
        rootNode.removeChild(childToRemove);
        PolytomousKeyNode child4 = PolytomousKeyNode.NewInstance("Test statement child4");
        rootNode.addChild(child4);

        @SuppressWarnings("unused")
        PolytomousKey mergedKey = keyService.merge(keyLoaded, childToRemove);

        //NOTE: maybe interesting to know, that if not using orphan removal
        //      resorting the index does not take place if not touching the children list somehow.
        //      The sortindex starts than at some number > 0 and may contain nulls.
        //      If touching the list like below the index starts at 0. This is now
        //      automatically handled in PostMergeEntityListener.
        //      mergedKey.getRoot().getChildren().size();

        commitAndStartNewTransaction(tableNames);

        rootNode = service.find(key.getRoot().getUuid());
        rootNode.getChildren();
        Assert.assertEquals(2, rootNode.getChildren().size());
        Assert.assertEquals(child3.getUuid(), rootNode.getChildren().get(0).getUuid());
        Assert.assertEquals(child4.getUuid(), rootNode.getChildren().get(1).getUuid());
        Assert.assertEquals("Should be root + 2 children", 3, service.count(PolytomousKeyNode.class));
    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void testMergeDetachedWithMove() {

        //create key with 2 child nodes
        PolytomousKey key = PolytomousKey.NewTitledInstance("Move test PolytomousKey");
        PolytomousKeyNode child1 = PolytomousKeyNode.NewInstance("Test statement child1");
        PolytomousKeyNode child2 = PolytomousKeyNode.NewInstance("Test statement child2");
        key.getRoot().addChild(child1);
        key.getRoot().addChild(child2);
        keyService.merge(key);
        commitAndStartNewTransaction();

        //load root node and make it detached
        PolytomousKey keyLoaded = keyService.find(key.getUuid());
        PolytomousKeyNode rootNode = keyLoaded.getRoot();
        rootNode.getChildren().get(1).getChildren().size();  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        PolytomousKeyNode childMove = rootNode.getChildren().get(0);
        PolytomousKeyNode newParentNode = rootNode.getChildren().get(1);
        newParentNode.addChild(childMove);
        PolytomousKeyNode child4 = PolytomousKeyNode.NewInstance("Test statement child3");
        rootNode.addChild(child4);
        @SuppressWarnings("unused")
        //no removed child to delete here
        PolytomousKey mergedKey = keyService.merge(keyLoaded, new CdmBase[]{});

        commitAndStartNewTransaction(tableNames);

        rootNode = service.find(key.getRoot().getUuid());
        rootNode.getChildren();
        Assert.assertEquals(2, rootNode.getChildren().size());
        PolytomousKeyNode firstChild = rootNode.getChildren().get(0);
        Assert.assertEquals(child2.getUuid(), firstChild.getUuid());
        Assert.assertEquals(child4.getUuid(), rootNode.getChildren().get(1).getUuid());
        Assert.assertEquals(1, firstChild.getChildren().size());
        Assert.assertEquals(child1.getUuid(), firstChild.getChildren().get(0).getUuid());
        Assert.assertEquals("Should be root + 2 children + 1 grandchild", 4, service.count(PolytomousKeyNode.class));
    }

    @Test //10101 same as testMerge but with saveOrUpdate
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public final void testSaveDetached(){

        //create key with 2 child nodes
        PolytomousKey key = PolytomousKey.NewTitledInstance("TestPolytomousKey");
        PolytomousKeyNode child1 = PolytomousKeyNode.NewInstance("Test statement child1");
        PolytomousKeyNode child2 = PolytomousKeyNode.NewInstance("Test statement child2");
        key.getRoot().addChild(child1);
        key.getRoot().addChild(child2);
        keyService.save(key);
        commitAndStartNewTransaction();

        //load root node and make it detached
        PolytomousKeyNode rootNode = service.find(key.getRoot().getUuid());
        rootNode.getChildren().get(0);  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        PolytomousKeyNode childToRemove = rootNode.getChildren().get(0);
        rootNode.removeChild(childToRemove);
        PolytomousKeyNode child3 = PolytomousKeyNode.NewInstance("Test statement child3");
        rootNode.addChild(child3);
        service.saveOrUpdate(rootNode);   //TODO childToRemove
        service.delete(childToRemove.getUuid(), false);
        commitAndStartNewTransaction(tableNames);

        //test result
        rootNode = service.find(key.getRoot().getUuid());
        rootNode.getChildren();
        Assert.assertEquals(2, rootNode.getChildren().size());
        Assert.assertEquals(child2.getUuid(), rootNode.getChildren().get(0).getUuid());
        Assert.assertEquals(child3.getUuid(), rootNode.getChildren().get(1).getUuid());
        Assert.assertEquals("Should be root + 2 children", 3, service.count(PolytomousKeyNode.class));
        commitAndStartNewTransaction();

//        System.out.println("NEXT");
        //same with key
        //load root node and make it detached
        PolytomousKey keyLoaded = keyService.find(key.getUuid());
        rootNode = keyLoaded.getRoot();
        rootNode.getChildren().get(0);  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        childToRemove = rootNode.getChildren().get(0);
        rootNode.removeChild(childToRemove);
        PolytomousKeyNode child4 = PolytomousKeyNode.NewInstance("Test statement child4");
        rootNode.addChild(child4);

        @SuppressWarnings("unused")
        UUID mergedKey = keyService.saveOrUpdate(keyLoaded);
        service.delete(childToRemove.getUuid(), false);

        commitAndStartNewTransaction(tableNames);

        rootNode = service.find(key.getRoot().getUuid());
        rootNode.getChildren();
        Assert.assertEquals(2, rootNode.getChildren().size());
        Assert.assertEquals(child3.getUuid(), rootNode.getChildren().get(0).getUuid());
        Assert.assertEquals(child4.getUuid(), rootNode.getChildren().get(1).getUuid());
        Assert.assertEquals("Should be root + 2 children", 3, service.count(PolytomousKeyNode.class));
    }


    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void testSaveDetachedWithMove() {

        //create key with 2 child nodes
        PolytomousKey key = PolytomousKey.NewTitledInstance("Move test PolytomousKey");
        PolytomousKeyNode child1 = PolytomousKeyNode.NewInstance("Test statement child1");
        PolytomousKeyNode child2 = PolytomousKeyNode.NewInstance("Test statement child2");
        key.getRoot().addChild(child1);
        key.getRoot().addChild(child2);
        keyService.save(key);
        commitAndStartNewTransaction();

        //load root node and make it detached
        PolytomousKey keyLoaded = keyService.find(key.getUuid());
        PolytomousKeyNode rootNode = keyLoaded.getRoot();
        rootNode.getChildren().get(1).getChildren().size();  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        PolytomousKeyNode childMove = rootNode.getChildren().get(0);
        PolytomousKeyNode newParentNode = rootNode.getChildren().get(1);
        newParentNode.addChild(childMove);
        PolytomousKeyNode child4 = PolytomousKeyNode.NewInstance("Test statement child3");
        rootNode.addChild(child4);
        //no removed child to delete here
        keyService.saveOrUpdate(keyLoaded) ;

        commitAndStartNewTransaction(tableNames);

        rootNode = service.find(key.getRoot().getUuid());
        rootNode.getChildren();
        Assert.assertEquals(2, rootNode.getChildren().size());
        PolytomousKeyNode firstChild = rootNode.getChildren().get(0);
        Assert.assertEquals(child2.getUuid(), firstChild.getUuid());
        Assert.assertEquals(child4.getUuid(), rootNode.getChildren().get(1).getUuid());
        Assert.assertEquals(1, firstChild.getChildren().size());
        Assert.assertEquals(child1.getUuid(), firstChild.getChildren().get(0).getUuid());
        Assert.assertEquals("Should be root + 2 children + 1 grandchild", 4, service.count(PolytomousKeyNode.class));
    }


    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}