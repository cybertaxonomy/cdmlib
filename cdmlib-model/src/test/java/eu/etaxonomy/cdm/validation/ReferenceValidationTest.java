/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.groups.Default;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.validation.constraint.InReferenceValidator;
import eu.etaxonomy.cdm.validation.constraint.NoRecursiveInReferenceValidator;


/**
 *
 * @author ben.clark
 *
 */
@SuppressWarnings("unused")
public class ReferenceValidationTest extends ValidationTestBase {
	private static final Logger logger = Logger.getLogger(ReferenceValidationTest.class);

	private IBook book;

	@Before
	public void setUp() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
		book = ReferenceFactory.newBook();
		book.setTitleCache("Lorem ipsum", true);
		book.setIsbn("1-919795-99-5");
	}


/****************** TESTS *****************************/

	/**
	 * Test validation at the second level with a valid reference
	 */
	@Test
	public final void testLevel2ValidationWithValidBook() {
        Set<ConstraintViolation<IBook>> constraintViolations  = validator.validate(book, Level2.class, Default.class);
        assertTrue("There should be no constraint violations as this book is valid at level 2",constraintViolations.isEmpty());
	}

	@Test
	public final void testLevel2ValidationWithValidISBN() {

        Set<ConstraintViolation<IBook>> constraintViolations  = validator.validate(book, Level2.class);
        assertTrue("There should be no constraint violations as this book is valid at level 2",constraintViolations.isEmpty());
	}

	@Test
	public final void testLevel2ValidationWithInValidISBN() {
		book.setIsbn("1-9197954-99-5");
        Set<ConstraintViolation<IBook>> constraintViolations  = validator.validate(book, Level2.class);
        assertFalse("There should be a constraint violation as this book has an invalid ISBN number",constraintViolations.isEmpty());
	}

	@Test
	public final void testLevel2ValidationWithValidUri() {
		try {
			book.setUri(new URI("http://www.e-taxonomy.eu"));
		} catch (URISyntaxException e) {
			Assert.fail("URI is not valid");
		}
        Set<ConstraintViolation<IBook>> constraintViolations  = validator.validate(book, Level2.class);
        assertTrue("There should be no constraint violations as this book is valid at level 2",constraintViolations.isEmpty());
	}


   @Test
    public final void testLevel3ValidationWithInValidInReference() {

        IBookSection bookSection = ReferenceFactory.newBookSection();
        bookSection.setTitleCache("test", true);
        bookSection.setTitle("");
        bookSection.setInReference((Reference)book);
        Set<ConstraintViolation<IBookSection>> constraintViolations  = validator.validate(bookSection, Level3.class);
        assertTrue("There should be one constraint violation as this book has a valid Ref",constraintViolations.size() == 0);

        Reference article = ReferenceFactory.newArticle();
        article.setTitleCache("article", true);
        bookSection.setInReference(article);
        constraintViolations  = validator.validate(bookSection, Level3.class);
//        assertTrue("There should be a constraint violation as this book has an invalid inReference",constraintViolations.size() == 1);
        assertHasConstraintOnValidator((Set)constraintViolations, InReferenceValidator.class);
    }


	@Test
	public final void testValidationAfterCasting(){
		((Reference)book).castReferenceToArticle();
		Set<ConstraintViolation<IBook>> constraintViolations  = validator.validate(book, Level2.class);
        assertFalse("There should be one constraint violations as this article is not valid at level 2 (has an isbn)", constraintViolations.isEmpty());
	}

	@Test
	public final void testNoRecursiveInReference(){
	    Reference myRef = ReferenceFactory.newBookSection();
        myRef.setInReference(myRef);
        myRef.setTitle("My book section");
        assertHasConstraintOnValidator((Set)validator.validate(myRef, Level3.class), NoRecursiveInReferenceValidator.class);

        myRef.setInBook(book);
        assertNoConstraintOnValidator((Set)validator.validate(myRef, Level3.class), NoRecursiveInReferenceValidator.class);

	}


}