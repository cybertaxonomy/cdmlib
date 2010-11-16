/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * 
 * @author ben.clark
 *
 */
@SuppressWarnings("unused")
@Ignore //FIXME ignoring only for merging 8.6.2010 a.kohlbecker
public class ReferenceValidationTest extends CdmIntegrationTest {
	private static final Logger logger = Logger.getLogger(ReferenceValidationTest.class);
	
	@SpringBeanByType
	private Validator validator;
	
	private IBook book;
	ReferenceFactory refFactory;
	@Before
	public void setUp() {
		refFactory = ReferenceFactory.newInstance();
		book = refFactory.newBook();
		book.setTitleCache("Lorem ipsum", true);
	}
	
	
/****************** TESTS *****************************/

	/**
	 * Test validation at the second level with a valid reference
	 */
	@Test
	public final void testLevel2ValidationWithValidBook() {
        Set<ConstraintViolation<IBook>> constraintViolations  = validator.validate(book, Level2.class);
        assertTrue("There should be no constraint violations as this book is valid at level 2",constraintViolations.isEmpty());
	}
	
	@Test
	public final void testLevel2ValidationWithValidISBN() {
		book.setIsbn("1-919795-99-5");
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
		book.setUri("http://www.e-taxonomy.eu");
        Set<ConstraintViolation<IBook>> constraintViolations  = validator.validate(book, Level2.class);
        assertTrue("There should be no constraint violations as this book is valid at level 2",constraintViolations.isEmpty());
	}
	
	@Test
	public final void testLevel2ValidationWithInValidUri() {
		book.setUri("http://www.e-\taxonomy.eu");
        Set<ConstraintViolation<IBook>> constraintViolations  = validator.validate(book, Level2.class);
        assertFalse("There should be a constraint violation as this book has an invalid URI",constraintViolations.isEmpty());
	}
}