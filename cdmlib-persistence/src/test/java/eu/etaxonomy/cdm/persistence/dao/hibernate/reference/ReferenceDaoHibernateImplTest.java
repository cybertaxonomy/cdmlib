/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.dao.hibernate.reference;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class ReferenceDaoHibernateImplTest extends CdmIntegrationTest {
	
	@SpringBeanByType
	IReferenceDao referenceDao;
	
	private UUID firstBookUuid;
	private UUID firstJournalUuid;
	private UUID genericUuid;
	private String firstPublisherName ="First Publisher";
	private String secondPublisherName ="Second Publisher";
	private String thirdPublisherName ="Third Publisher";
	private String fourthPublisherName ="Fourth Publisher";
	
	
	@Before
	public void setUp() {
		firstBookUuid = UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f2");
	    firstJournalUuid = UUID.fromString("ad4322b7-4b05-48af-be70-f113e46c545e");
	    genericUuid = UUID.fromString("bd4822b7-4b05-4eaf-be70-f113446c585e");
	}

	@Test
	public void testDummy() {
	}
	
	@Test
	public void testGetPublishers() {
		IJournal firstJournal = (IJournal)referenceDao.findByUuid(firstJournalUuid);
		assert firstJournal!= null : "journal must exist";
		
//		List<Publisher> publishers = firstJournal.getPublishers();
//		
//		assertNotNull("getPublishers should return a list", publishers);
//		assertFalse("the list should not be empty", publishers.isEmpty());
//		assertEquals("getPublishers should return 4 Publisher instances",4,publishers.size());
//		assertEquals("first publisher should come first",firstPublisherName,publishers.get(0).getPublisherName());
//		assertEquals("second publisher should come second",secondPublisherName,publishers.get(1).getPublisherName());
//		assertEquals("third publisher should come third",thirdPublisherName,publishers.get(2).getPublisherName());
//		assertEquals("fourth publisher should come fourth",fourthPublisherName,publishers.get(3).getPublisherName());
		
		
	}


}
