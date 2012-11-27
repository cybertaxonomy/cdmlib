/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.media;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.hibernate.TypeMismatchException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaRepresentationPartDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;
//import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

@DataSet
public class MediaRepresentationPartDaoImplTest extends /*CdmTransactionalIntegrationTest*/CdmIntegrationTest {

	@SpringBeanByType
	IMediaRepresentationPartDao dao;
	
	@SpringBeanByType
	IDefinedTermDao definedTermDao;
	
	
	@Before
	public void setUp() {
	}
	
	@Test
	public void testCount() {
		int numberOfMediaRepresentationParts = dao.count();
		assertEquals("count should return 3",3,numberOfMediaRepresentationParts);
	}
	
	@Test
	public void testFindByUuid() {
		MediaRepresentationPart part = dao.findByUuid(UUID.fromString("e06a0f29-ef82-4ce3-8a94-dd98feae8f9e"));
		Assert.assertNotNull("part should not be null", part);
		try {
			Assert.assertEquals("part URI should be http://www.test.de", new URI("http://www.test.de"), part.getUri());
		} catch (URISyntaxException e) {
			Assert.fail("No URISyntaxException should occurr");
		}
		
		MediaRepresentationPart part2 = dao.findByUuid(UUID.fromString("a07a0f29-ef82-3ce3-8a94-dd98feae8f9e"));
		Assert.assertNotNull("part2 should not be null", part2);
		try {
			Assert.assertEquals("part URI should be http://www.test2.de", new URI("http://www.test2.de"), part2.getUri());
		} catch (URISyntaxException e) {
			Assert.fail("No URISyntaxException should occurr");
		}
		
		try {
			MediaRepresentationPart part3 = dao.findByUuid(UUID.fromString("b48a0f26-2f42-3ce3-8a04-dd98feaa8e98"));
//			Assert.assertEquals("part URI should be http://www.test2.de", new URI("http://www.test2.de"), part3.getUri());
			Assert.fail("A TypeMismatchException should occurr for http://www.fail.de");
		} catch (Exception e) {
			if (e instanceof TypeMismatchException){
				//OK
			}else{
				Assert.fail("Only an URISyntaxException should occurr but exception was of type " + e.getClass().getName());
			}
		}

	}
	
	@Test
	@ExpectedDataSet
	public void testSave() {
		try {
			URI uri = new URI("http://www.test3.de");
			
			MediaRepresentationPart part1 = MediaRepresentationPart.NewInstance(uri, 22);
			//a representation is needed otherwise a not nullable exception is thrown (due to lucene index (?))
			MediaRepresentation representation = MediaRepresentation.NewInstance();
			representation.addRepresentationPart(part1);
			dao.save(part1);
		
//			TaxonNameBase name = nameService.find(UUID.fromString("5d74500b-9fd5-4d18-b9cd-cc1c8a372fec"));
//			System.out.println(name.getRank().getLabel());
//			setComplete();
//			endTransaction();
//			try {
//				printDataSet(System.out, new String[]{"MediaRepresentationPart"});
//			} catch(Exception e) { 
//				logger.warn(e);
//			} 
			
			
			
		} catch (URISyntaxException e) {
			Assert.fail("URI should be ok");
		}
		
	}

}
