/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

@DataSet
public class PolytomousKeyDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByType
	PolytomousKeyDaoImpl polytomousKeyDao;

	@SpringBeanByType
    PolytomousKeyNodeDaoImpl polytomousKeyNodeDao;

	@Before
	public void setUp() {

	}

	@Test
	@DataSet("PolytomousKeyDaoHibernateImplTest.xml")
//	@ExpectedDataSet  //for some reason this result in an infinite waiting of the connection pool
	public void testSavePolytomousKey() {
		PolytomousKey existingKey = polytomousKeyDao.findByUuid(UUID.fromString("bab66772-2c83-428a-bb6d-655d12ac6097"));
		Assert.assertNotNull("",existingKey);
		PolytomousKeyNode root = existingKey.getRoot();
		Assert.assertNotNull("Root should not be null",root);
		Assert.assertEquals(2, root.childCount());

		//new key
		PolytomousKey newKey = PolytomousKey.NewInstance();
		PolytomousKeyNode newRoot = newKey.getRoot();
		PolytomousKeyNode child1 = PolytomousKeyNode.NewInstance();
		child1.addQuestionText("Question1", null);
		child1.addQuestionText("Question1German", Language.GERMAN());
		child1.addStatementText("Statement1", null);
		PolytomousKeyNode child2 = PolytomousKeyNode.NewInstance();
		child2.addStatementText("Statement2", null);
		child2.addQuestionText("Question2German", Language.DEFAULT());
		PolytomousKeyNode child3 = PolytomousKeyNode.NewInstance();
		child3.addStatementText("Statement3", null);
		child3.addStatementText("Statement3German", Language.GERMAN());

		newRoot.addChild(child1);
		newRoot.addChild(child3);
		newRoot.addChild(child2, 1);

		newKey = polytomousKeyDao.save(newKey);

		//doesn't make sense as long as there is no new session
		PolytomousKey newKeyFromDb = polytomousKeyDao.findByUuid(newKey.getUuid());
//		List<PolytomousKeyNode> children = newKeyFromDb.getRoot().getChildren();
//		Assert.assertEquals(child1.getUuid(), children.get(0).getUuid());
//		Assert.assertNotSame(child1.getUuid(), children.get(0).getUuid());

//		printDataSet(System.out, new String[]{"PolytomousKeyNode", "KeyStatement", "KeyStatement_LanguageString", "LanguageString"});
		System.out.println("End test1");
	}

	@Test
	public void testDeletePolyotomousKey(){
		UUID uuid = UUID.fromString("bab66772-2c83-428a-bb6d-655d12ac6097");
		PolytomousKey existingKey = polytomousKeyDao.findByUuid(uuid);
		Assert.assertNotNull("",existingKey);
		PolytomousKeyNode node = existingKey.getRoot();
		polytomousKeyDao.delete(existingKey);

		commitAndStartNewTransaction(null);

		try {if (true){printDataSet(System.out, new String[]{"POLYTOMOUSKEY", "POLYTOMOUSKEYNODE"});}
		} catch(Exception e) { logger.warn(e);}

		PolytomousKey nonExistingKey = polytomousKeyDao.findByUuid(uuid);
		Assert.assertNull("", nonExistingKey);
		node = polytomousKeyNodeDao.findByUuid(node.getUuid());
		Assert.assertNull("", node);
	}

	@Test
	public void testNothing(){
		//maybe deleted once testSavePolytomousKey() works correctly
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
}
