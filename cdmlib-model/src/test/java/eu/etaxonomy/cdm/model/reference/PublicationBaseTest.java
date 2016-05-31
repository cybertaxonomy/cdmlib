/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

/**
 * @author a.mueller
 * @created 23.03.2009
 */
//@Ignore
public class PublicationBaseTest {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(PublicationBaseTest.class);

	private IBook reference;
	private IArticle reference2;
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
		reference = ReferenceFactory.newBook();
		reference2 = ReferenceFactory.newArticle();

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
		assertEquals("No publisher is set", null, reference.getPublisher());
		reference.setPublisher(publisher1, place1);
		assertEquals("The publishers is publisher1", publisher1, reference.getPublisher());
		assertEquals("The place is place1", place1, reference.getPlacePublished());
		reference.setPublisher(publisher2, place2);
		assertEquals("Second publisher must be publisher2", publisher2, reference.getPublisher());

		assertEquals("Second publication place must be place2", place2, reference.getPlacePublished());
	}

	@Test
	public void testInReferenceValidation(){
		IJournal journal = ReferenceFactory.newJournal();
		reference2.setInJournal(journal);
		//TODO: to validate it, the object has to be saved to the db
		IBookSection booksection = ((Reference)reference2).castReferenceToBookSection();

	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.reference.Reference#clone()}.
	 * This test was originally designed for the case when publisher was still
	 * a subclass holding publishing information. The current model is simplified
	 * and therefore this test is more or less obsolet
	 */
	@Test
	public void testClone() {
		reference.setPublisher(publisher1, place1);
//		publicationBase.addPublisher(publisher2, place2);
		Reference clone = (Reference)reference.clone();
		assertEquals("Publisher place must be equal in original publication and cloned publication", place1, clone.getPlacePublished());
		Assert.assertSame(place1, reference.getPublisher(), clone.getPublisher());
	}

    @Test
    public void beanTests(){
//      #5307 Test that BeanUtils does not fail
        BeanUtils.getPropertyDescriptors(Reference.class);
    }
}
