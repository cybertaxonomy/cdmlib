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
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class PolytomousKeyNodeServiceTest extends CdmTransactionalIntegrationTest {

    String[] tableNames = new String[]{"POLYTOMOUSKEY","POLYTOMOUSKEYNODE","POLYTOMOUSKEYNODE_AUD"};

	@SpringBeanByType
	private IPolytomousKeyNodeService service;

	@SpringBeanByType
	private IPolytomousKeyService keyService;

	/****************** TESTS *****************************/

	@Test
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

    @Test  //8127  //5536
    public final void testMerge(){

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
        PolytomousKeyNode child1b = rootNode.getChildren().get(0);
        rootNode.getChildren().remove(child1b);
        PolytomousKeyNode child3 = PolytomousKeyNode.NewInstance("Test statement child3");
        rootNode.addChild(child3);
        service.merge(rootNode);

        commitAndStartNewTransaction(tableNames);

        //test result
        rootNode = service.find(key.getRoot().getUuid());
        rootNode.getChildren();
        Assert.assertEquals(2, rootNode.getChildren().size());
        Assert.assertEquals(child2.getUuid(), rootNode.getChildren().get(0).getUuid());
        Assert.assertEquals(child3.getUuid(), rootNode.getChildren().get(1).getUuid());
        commitAndStartNewTransaction();

        System.out.println("NEXT");
        //same with key
        //load root node and make it detached
        PolytomousKey keyLoaded = keyService.find(key.getUuid());
        rootNode = keyLoaded.getRoot();
        rootNode.getChildren().get(0);  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        rootNode.getChildren().remove(rootNode.getChildren().get(0));
        PolytomousKeyNode child4 = PolytomousKeyNode.NewInstance("Test statement child4");
        rootNode.addChild(child4);

        @SuppressWarnings("unused")
        PolytomousKey mergedKey = keyService.merge(keyLoaded);

        //NOTE: for historical reasons interesting to know, that if not using orphan removal
        //      resorting the index does not take place if not touching the children list somehow.
        //      The sortindex starts than at some number > 0 and may contain nulls.
        //      If touching the list like below the index starts at 0 but
        //      mergedKey.getRoot().getChildren().size();

        commitAndStartNewTransaction(tableNames);

        rootNode = service.find(key.getRoot().getUuid());
        rootNode.getChildren();
        Assert.assertEquals(2, rootNode.getChildren().size());
        Assert.assertEquals(child3.getUuid(), rootNode.getChildren().get(0).getUuid());
        Assert.assertEquals(child4.getUuid(), rootNode.getChildren().get(1).getUuid());

    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}