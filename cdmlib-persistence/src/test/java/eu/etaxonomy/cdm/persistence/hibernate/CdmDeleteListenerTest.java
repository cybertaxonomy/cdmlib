// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.hibernate;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 * @created 17.09.2009
 * @version 1.0
 */
public class CdmDeleteListenerTest extends CdmIntegrationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmDeleteListenerTest.class);

	@SpringBeanByType
	private ITaxonNameDao taxonNameDao;

	private UUID name1Uuid = UUID.fromString("a49a3963-c4ea-4047-8588-2f8f15352730");
	
	@Ignore
	@Test
	@DataSet("CdmDeleteListenerTest.xml")
	public void testTaxonNameDao() throws Exception {
		assertNotNull("taxonNameDao should exist",taxonNameDao);
	}
	
	@Test
	@DataSet("CdmDeleteListenerTest.xml")
	public void testPrintDataset(){
		BotanicalName botName1 = BotanicalName.NewInstance(Rank.SPECIES());
		BotanicalName botName2 = BotanicalName.NewInstance(Rank.SPECIES());
		//botName1.addRelationshipToName(botName2, NameRelationshipType.ORTHOGRAPHIC_VARIANT(), null);
		botName1.addHybridChild(botName2, HybridRelationshipType.FEMALE_PARENT(), null);
		taxonNameDao.saveOrUpdate(botName1);
		//taxonNameDao.flush();
		try {
			File file = new File("testXXX.txt");
			if (!file.exists()){
				file.createNewFile();
			}
			//printDataSet(new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.hibernate.CdmDeleteListener#onDelete(org.hibernate.event.DeleteEvent, java.util.Set)}.
	 */
	@Test
	@DataSet("CdmDeleteListenerTest.xml")
	@ExpectedDataSet
	public void testOnDelete() {
		NonViralName name1 = (NonViralName)taxonNameDao.findByUuid(name1Uuid);
		assertNotNull(name1);
		Set<NameRelationship> relations = name1.getNameRelations();
		Assert.assertEquals("There must be 1 name relationship", 1, relations.size());
		name1.removeNameRelationship(relations.iterator().next());
		
		Set<HybridRelationship> hybridRels = name1.getParentRelationships();
		Assert.assertEquals("There must be 1 parent relationship", 1, hybridRels.size());
		
		taxonNameDao.saveOrUpdate(name1);
	}
}
