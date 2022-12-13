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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

/**
 * Originally tested the PublicationBase class.
 * Now this class was merged into {@link Reference}
 * therefore this class is a special test for
 * {@link Reference}.
 * @author a.mueller
 * @since 23.03.2009
 */
public class PublicationBaseTest {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private IBook reference;
	private IArticle reference2;
	private String publisher1;
	private String publisher2;
	private String place1;
	private String place2;

	@Before
	public void setUp() throws Exception {
		publisher1 = "publisher1";
		publisher2 = "publisher2";
		place1 = "place1";
		place2 = "place2";
		reference = ReferenceFactory.newBook();
		reference2 = ReferenceFactory.newArticle();
	}

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