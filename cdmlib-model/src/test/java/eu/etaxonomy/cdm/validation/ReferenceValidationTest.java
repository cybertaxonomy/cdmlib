/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URISyntaxException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.validation.constraint.InReferenceValidator;
import eu.etaxonomy.cdm.validation.constraint.NoRecursiveInReferenceValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.groups.Default;

/**
 * @author ben.clark
 */
@SuppressWarnings("unused")
public class ReferenceValidationTest extends ValidationTestBase {

    private static final Logger logger = LogManager.getLogger();

    private IBook book;

	@BeforeEach
	public void setUp() {
		book = ReferenceFactory.newBook();
		book.setTitleCache("Lorem ipsum", true);
		book.setIsbn("1-919795-99-5");
	}

//****************** TESTS *****************************/

	/**
	 * Test validation at the second level with a valid reference
	 */
	@Test
	public final void testLevel2ValidationWithValidBook() {
        Set<ConstraintViolation<IBook>> constraintViolations  = validator.validate(book, Level2.class, Default.class);
        assertTrue(constraintViolations.isEmpty(), "There should be no constraint violations as this book is valid at level 2");
	}

	@Test
	public final void testLevel2ValidationWithValidISBN() {
        Set<ConstraintViolation<IBook>> constraintViolations  = validator.validate(book, Level2.class);
        assertTrue(constraintViolations.isEmpty(), "There should be no constraint violations as this book is valid at level 2");
	}

	@Test
	public final void testLevel2ValidationWithInValidISBN() {
		book.setIsbn("1-9197954-99-5");
        Set<ConstraintViolation<IBook>> constraintViolations  = validator.validate(book, Level2.class);
        assertFalse(constraintViolations.isEmpty(), "There should be a constraint violation as this book has an invalid ISBN number");
	}

	@Test
	public final void testLevel2ValidationWithValidUri() {
		try {
			book.setUri(new URI("http://www.e-taxonomy.eu"));
		} catch (URISyntaxException e) {
			fail("URI is not valid");
		}
        Set<ConstraintViolation<IBook>> constraintViolations  = validator.validate(book, Level2.class);
        assertTrue(constraintViolations.isEmpty(), "There should be no constraint violations as this book is valid at level 2");
	}

   @Test
    public final void testLevel3ValidationWithInValidInReference() {

        IBookSection bookSection = ReferenceFactory.newBookSection();
        bookSection.setTitleCache("test", true);
        bookSection.setTitle("");
        bookSection.setInReference((Reference)book);
        Set<ConstraintViolation<IBookSection>> constraintViolations  = validator.validate(bookSection, Level3.class);
        assertTrue(constraintViolations.size() == 0, "There should be one constraint violation as this book has a valid Ref");

        Reference article = ReferenceFactory.newArticle();
        article.setTitleCache("article", true);
        bookSection.setInReference(article);
        constraintViolations  = validator.validate(bookSection, Level3.class);
//        assertTrue(constraintViolations.size() == 1, "There should be a constraint violation as this book has an invalid inReference");
        assertHasConstraintOnValidator((Set)constraintViolations, InReferenceValidator.class);
    }

	@Test
	public final void testValidationAfterCasting(){
		((Reference)book).castReferenceToArticle();
		Set<ConstraintViolation<IBook>> constraintViolations  = validator.validate(book, Level2.class);
        assertFalse(constraintViolations.isEmpty(), "There should be one constraint violations as this article is not valid at level 2 (has an isbn)");
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
