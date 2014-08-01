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
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;



/**
 * TODO 
 * This test was originally designed to test if Taxon name parts use the correct case.
 * As this is already covered by the Pattern validations on the according fields it is not longer required and can be used
 * for any other validation.
 * 
 * @author a.mueller
 *
 */
public class NamePartsCaseTest  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NamePartsCaseTest.class);
	
	private Validator validator;
	
	private NonViralName name;
	
	@Before
	public void setUp() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
		name = BotanicalName.NewInstance(Rank.SPECIES());
		name.setGenusOrUninomial("Aus");
		name.setSpecificEpithet("aus");
		name.setAuthorshipCache("L.");
		name.getNameCache();
		
	}
	
	
/****************** TESTS *****************************/
	
	@Test
	public void testValidSpecificName() {
        Set<ConstraintViolation<NonViralName>> constraintViolations  = validator.validate(name, Level2.class);  
        assertTrue("There should be no constraint violations as this name has the correct cases", constraintViolations.isEmpty());
	}

	@Test
	@Ignore   //does not work as also Pattern validation matches (see class documentation)
	public void testInvalidSpecificName() {
		name.setGenusOrUninomial("aus");
        Set<ConstraintViolation<NonViralName>> constraintViolations  = validator.validate(name, Level2.class);  
        assertFalse("There should be no constraint violations as this name has the correct cases", constraintViolations.isEmpty());
        assertEquals("There should be exactly 1 violation", 1, constraintViolations.size());
        ConstraintViolation<NonViralName> violation = constraintViolations.iterator().next();
        assertEquals("", violation.getMessage());
	}

	
}
