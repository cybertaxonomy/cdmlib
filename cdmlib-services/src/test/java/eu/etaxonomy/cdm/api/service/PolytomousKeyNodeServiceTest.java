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

	@SpringBeanByType
	private IPolytomousKeyNodeService service;

	@SpringBeanByType
	private IPolytomousKeyService keyService;

	/****************** TESTS *****************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#setDao(eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao)}.
	 */
	@Test
	public final void testSetDao() {
		Assert.assertNotNull(service);
		Assert.assertNotNull(keyService);
	}

	@Test
    public final void testDelete(){

        PolytomousKey key = PolytomousKey.NewTitledInstance("TestPolytomousKey");
        keyService.save(key);
        PolytomousKeyNode node = PolytomousKeyNode.NewInstance("Test statement");
        key.setRoot(node);
        key.setStartNumber(0);

        PolytomousKeyNode child = PolytomousKeyNode.NewInstance("Test statement Nr 2");
        //child.setKey(key);

        node.addChild(child,0);
        service.save(node);

        PolytomousKeyNode child1 = PolytomousKeyNode.NewInstance("Test statement Nr 3");
        //child.setKey(key);

        child.addChild(child1,0);
        UUID uuidChild = service.save(child).getUuid();

        PolytomousKeyNode child2 = PolytomousKeyNode.NewInstance("Test statement Nr 4");
        //child.setKey(key);

        child1.addChild(child2,0);
        UUID uuidChild1 = service.save(child1).getUuid();

        node = service.load(uuidChild1);
        UUID uuidChild2 = node.getChildAt(0).getUuid();
        assertNotNull(node);
        service.delete(uuidChild1, false);
        node = service.load(uuidChild1);
        assertNull(node);
        node = service.load(uuidChild2);
        assertNotNull(node);

        node = service.load(uuidChild);

        assertNotNull(node);
        service.delete(uuidChild, true);
        node = service.load(uuidChild);
        assertNull(node);
        node = service.load(uuidChild2);
        assertNull(node);
    }

    @Test  //8127  //5536
    public final void testMerge(){
        String[] tableNames = new String[] {"POLYTOMOUSKEY","POLYTOMOUSKEYNODE","POLYTOMOUSKEYNODE_AUD"};
        logger.warn(tableNames);

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
        System.out.println("NEXT");

        //test result
        rootNode = service.find(key.getRoot().getUuid());
        rootNode.getChildren();
        Assert.assertEquals(2, rootNode.getChildren().size());
        Assert.assertEquals(child2.getUuid(), rootNode.getChildren().get(0).getUuid());
        Assert.assertEquals(child3.getUuid(), rootNode.getChildren().get(1).getUuid());
        commitAndStartNewTransaction();

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