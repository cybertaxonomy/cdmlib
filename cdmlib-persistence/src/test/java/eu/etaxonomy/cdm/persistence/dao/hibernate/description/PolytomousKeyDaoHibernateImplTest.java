/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;

@CdmDataSet
public class PolytomousKeyDaoHibernateImplTest extends CdmTransactionalSpringIntegrationTest {

	@Autowired
	private PolytomousKeyDaoImpl polytomousKeyDao;

	@Autowired
	private PolytomousKeyNodeDaoImpl polytomousKeyNodeDao;

	@BeforeEach
	public void setUp() {}

	@Test
	@CdmDataSet("PolytomousKeyDaoHibernateImplTest.xml")
//	@CdmExpectedDataSet  //for some reason this result in an infinite waiting of the connection pool
	public void testSavePolytomousKey() {
		PolytomousKey existingKey = polytomousKeyDao.findByUuid(UUID.fromString("bab66772-2c83-428a-bb6d-655d12ac6097"));
		assertNotNull(existingKey, "");
		PolytomousKeyNode root = existingKey.getRoot();
		assertNotNull(root, "Root should not be null");
		assertEquals(2, root.childCount());

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
//		assertEquals(child1.getUuid(), children.get(0).getUuid());
//		assertNotSame(child1.getUuid(), children.get(0).getUuid());

//		printDataSet(System.out, new String[]{"PolytomousKeyNode", "KeyStatement", "KeyStatement_LanguageString", "LanguageString"});
	}

	@Test
	public void testDeletePolyotomousKey(){
		UUID uuid = UUID.fromString("bab66772-2c83-428a-bb6d-655d12ac6097");
		PolytomousKey existingKey = polytomousKeyDao.findByUuid(uuid);
		assertNotNull(existingKey, "");
		PolytomousKeyNode node = existingKey.getRoot();
		polytomousKeyDao.delete(existingKey);

		commitAndStartNewTransaction(null);

//		try {if (true){printDataSet(System.out, new String[]{"POLYTOMOUSKEY", "POLYTOMOUSKEYNODE"});}
//		} catch(Exception e) { logger.warn(e);}

		PolytomousKey nonExistingKey = polytomousKeyDao.findByUuid(uuid);
		assertNull(nonExistingKey, "");
		node = polytomousKeyNodeDao.findByUuid(node.getUuid());
		assertNull(node, "");
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
