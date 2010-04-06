/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author a.mueller
 * @created 23.03.2009
 * @version 1.0
 */
//@Ignore
public class PublicationBaseTest {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(PublicationBaseTest.class);

	private IBook publicationBase;
	private IArticle publicationBase2;
	private String publisher1;
	private String publisher2;
	private String place1;
	private String place2;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		publisher1 = "publisher1";
		publisher2 = "publisher2";
		place1 = "place1";
		place2 = "place2";
		
		ReferenceFactory refFactory = ReferenceFactory.newInstance();
		publicationBase = refFactory.newBook();
		
		publicationBase2 = refFactory.newArticle();
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.reference.PublicationBase#addPublisher(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testAddPublisherStringString() {
		assertEquals("No publisher is set", null, publicationBase.getPublisher());
		publicationBase.setPublisher(publisher1, place1);
		assertEquals("The publishers is publisher1", publisher1, publicationBase.getPublisher());
		assertEquals("The place is place1", place1, publicationBase.getPlacePublished());
		publicationBase.setPublisher(publisher2, place2);
		assertEquals("Second publisher must be publisher2", publisher2, publicationBase.getPublisher());

		assertEquals("Second publication place must be place2", place2, publicationBase.getPlacePublished());
	}

	
//	/**
//	 * Test method for {@link eu.etaxonomy.cdm.model.reference.PublicationBase#addPublisher(java.lang.String, java.lang.String, int)}.
//	 */
//	@Test(expected=IndexOutOfBoundsException.class)
//	public void testAddPublisherStringStringInt() {
//		publicationBase.addPublisher(publisher1, place1);
//		publicationBase.addPublisher(publisher2, place2);
//		assertEquals("Publishers list must contains exactly 2 entry", 2, publicationBase.getPublishers().size());
//		String indexPublisher = "indexPublisher";
//		String indexPlace = "indexPlace";
//		publicationBase.addPublisher(indexPublisher, indexPlace, 1);
//		assertEquals("Publisher at position 1 (starting at 0) should be 'indexPublisher'", indexPublisher, publicationBase.getPublishers().get(1).getPublisherName());
//		publicationBase.addPublisher(indexPublisher, indexPlace, 5);
//	}


//	/**
//	 * Test method for {@link eu.etaxonomy.cdm.model.reference.PublicationBase#getPublisher(java.lang.int}.
//	 */
//	@Test(expected=IndexOutOfBoundsException.class)
//	public void testGetPublisherInt() {
//		publicationBase.addPublisher(publisher1, place1);
//		publicationBase.addPublisher(publisher2, place2);
//		assertEquals("Publishers list must contains exactly 2 entry", 2, publicationBase.getPublishers().size());
//		assertEquals("First publisher must be publisher1", publisher1, publicationBase.getPublisher(0).getPublisherName());
//		publicationBase.getPublisher(2);
//		publicationBase.getPublisher(-1);
//	}

//	/**
//	 * Test method for {@link eu.etaxonomy.cdm.model.reference.PublicationBase#removePublisher(eu.etaxonomy.cdm.model.reference.Publisher)}.
//	 */
//	@Test
//	public void testRemovePublisher() {
//		publicationBase.addPublisher(publisher1, place1);
//		publicationBase.addPublisher(publisher2, place2);
//		assertEquals("Publishers list must contains exactly 2 entry", 2, publicationBase.getPublishers().size());
//		publicationBase.removePublisher(publicationBase.getPublishers().get(0));
//		assertEquals("Publishers list must contains exactly 1 entry", 1, publicationBase.getPublishers().size());
//		List<Publisher> publishers = publicationBase.getPublishers();
//		assertEquals("Only publisher must be publisher2", publisher2, publishers.get(0).getPublisherName());
//		assertEquals("only publication place  must be place2", place2, publishers.get(0).getPlace());
//	}

//	/**
//	 * Test method for {@link eu.etaxonomy.cdm.model.reference.PublicationBase#clone()}.
//	 */
//	@Test
//	public void testClone() {
//		publicationBase.addPublisher(publisher1, place1);
//		publicationBase.addPublisher(publisher2, place2);
//		assertEquals("Publishers list must contains exactly 2 entry", 2, publicationBase.getPublishers().size());
//		CdDvd clone = (CdDvd)publicationBase.clone();
//		assertEquals("Publisher place must be equal in original publication and cloned publication", place1, clone.getPublisher(0).getPlace());
//		assertNotSame(place1, publicationBase.getPublisher(0), clone.getPublisher(0));	
//	}
}
