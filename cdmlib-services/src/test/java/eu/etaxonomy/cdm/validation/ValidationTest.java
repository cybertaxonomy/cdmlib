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
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * NOTE: In this test, the words "valid" and "invalid", loaded though 
 * these terms are when applied to taxonomic names, only mean "passes the
 * rules of this validator" or not and should not be confused with the strict
 * nomenclatural and taxonomic sense of these words.
 * 
 * @author ben.clark
 *
 */
@SuppressWarnings("unused")
public class ValidationTest extends CdmIntegrationTest {
	private static final Logger logger = Logger.getLogger(ValidationTest.class);
	
	@SpringBeanByType
	private Validator validator;
	
	@SpringBeanByType
	private ITermService termService;

	private BotanicalName name;
	
	@Before
	public void setUp() {
		
		
		//Rank speciesRank = (Rank)termService.find(Rank.uuidSpecies);
		name = BotanicalName.NewInstance(Rank.SPECIES());
	}
	
	
/****************** TESTS *****************************/
	
	/**
	 * Test validation factory initialization and autowiring 
	 * into an instance of javax.valdation.Validator
	 */
	@Test
	@DataSet
	public final void testValidatorAutowire() {
        assertNotNull("the validator should not be null", validator);
	}

	/**
	 * Test validation at the "default" level with a valid name
	 */
	@Test
	@DataSet
	public final void testDefaultValidationWithValidName() {
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name);
        assertTrue("There should be no constraint violations as this name is valid at the default level",constraintViolations.isEmpty());
	}
	
	/**
	 * Test validation at the "default" level with an invalid name
	 */
	@Test
	@DataSet
	public final void testDefaultValidationWithInValidName() {
		name.setGenusOrUninomial("");
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name);
        assertFalse("There should be a constraint violation as this name is invalid at the default level",constraintViolations.isEmpty());
	}
	
	/**
	 * Test validation at level2 with a valid name
	 */
	@Test
	@DataSet
	public final void testLevel2ValidationWithValidName() {
		name.setGenusOrUninomial("Abies");
		name.setSpecificEpithet("balsamea");
		name.setNameCache("Abies balsamea");
		name.setAuthorshipCache("L.");
		name.setTitleCache("Abies balsamea L.", true);
		name.setFullTitleCache("Abies balsamea L.");
		
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Default.class,Level2.class);
        assertTrue("There should not be a constraint violation as this name is valid at the default and at the second level",constraintViolations.isEmpty());
	}
	
	/**
	 * Test validation at level2 with an invalid name
	 */
	@Test
	@DataSet
	public final void testLevel2ValidationWithInValidName() {
		name.setGenusOrUninomial("Abies");
		name.setSpecificEpithet("balsamea");
		
		
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Default.class);
        assertTrue("There should not be a constraint violation as this name is valid at the default level",constraintViolations.isEmpty());
       
        constraintViolations  = validator.validate(name, Default.class,Level2.class);
        assertFalse("There should be a constraint violation as this name is valid at the default level, but invalid at the second level",constraintViolations.isEmpty());
	}
	
	/**
	 * Test validation at level3 with a valid name
	 */
	@Test
	@DataSet
	public final void testLevel3ValidationWithValidName() {
		name.setGenusOrUninomial("Abies");
		name.setSpecificEpithet("balsamea");
		name.setNameCache("Abies balsamea");
		name.setAuthorshipCache("L.");
		name.setTitleCache("Abies balsamea L.", true);
		name.setFullTitleCache("Abies balsamea L.");
		
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Default.class,Level2.class, Level3.class);
        assertTrue("There should not be a constraint violation as this name is valid at all levels",constraintViolations.isEmpty());
	}
	
	/**
	 * Test validation at the level3 with an invalid name
	 */
	@Test
	@DataSet
	public final void testLevel3ValidationWithInValidName() {
		name.setGenusOrUninomial("Abies");
		name.setSpecificEpithet("alba");
		name.setNameCache("Abies alba");
		name.setAuthorshipCache("Mill.");
		name.setTitleCache("Abies alba Mill.", true);
		name.setFullTitleCache("Abies alba Mill.");
		name.setNomenclaturalReference(null);
		//name.setNomenclaturalMicroReference(" ");
		
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Default.class, Level2.class);
        assertTrue("There should not be a constraint violation as this name is valid at the default and second level",constraintViolations.isEmpty());
        constraintViolations  = validator.validate(name, Default.class,Level2.class, Level3.class);
        assertFalse("There should be a constraint violation as this name is valid at the default and second level, but invalid at the third level",constraintViolations.isEmpty());
        
	}
}
