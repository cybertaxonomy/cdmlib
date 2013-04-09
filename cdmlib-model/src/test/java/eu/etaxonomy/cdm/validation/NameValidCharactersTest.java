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

import org.junit.Assert;

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


/**
 * NOTE: In this test, the words "valid" and "invalid", loaded though 
 * these terms are when applied to taxonomic names, only mean "passes the
 * rules of this validator" or not and should not be confused with the strict
 * nomenclatural and taxonomic sense of these words.
 * 
 * @author ben.clark
 *
 */
//@Ignore //FIXME ignoring only for merging 8.6.2010 a.kohlbecker
@SuppressWarnings("unused")
public class NameValidCharactersTest  {
	private static final Logger logger = Logger.getLogger(NameValidCharactersTest.class);
	

	private Validator validator;
	
	private BotanicalName name;
	
	@Before
	public void setUp() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
		name = BotanicalName.NewInstance(Rank.SPECIES());
		name.setGenusOrUninomial("Abies");
		name.setSpecificEpithet("balsamea");
		name.setNameCache("Abies balsamea");
		name.setAuthorshipCache("L.");
		name.setTitleCache("Abies balsamea L.", true);
		name.setFullTitleCache("Abies balsamea L.");
	}
	
	
/****************** TESTS *****************************/
	
	/**
	 * Test validation at level2 with an invalid name - this should pass as there
	 * are international characters that are not allowed - grave and acute are forbidden
	 */
	@Test
	public final void testForbiddenAccents() {
		name.setSpecificEpithet("bals�me�");
		
		
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Default.class);
        assertTrue("There should not be a constraint violation as this name is valid at the default level",constraintViolations.isEmpty());
       
        constraintViolations  = validator.validate(name, Default.class,Level2.class);
        assertFalse("There should be a constraint violation as this name is valid at the default level, but contains a letter with a grave and an acute",constraintViolations.isEmpty());
	}
	
	/**
	 * Test validation at level2 with an valid name - this should pass the
	 * diaeresis is allowed under the botanical code.
	 */
	@Test
	@Ignore // setting this to ignore because the character is not showsn correctly in mac os.
	public final void testAllowedAccents() {
		name.setSpecificEpithet("bals�mea");
				
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Default.class);
        assertTrue("There should not be a constraint violation as this name is valid at the default level",constraintViolations.isEmpty());
       
        constraintViolations  = validator.validate(name, Default.class,Level2.class);
        assertTrue("There should not be a constraint violation as this name is valid at both levels, despite containing a diaeresis",constraintViolations.isEmpty());
	}
	
	/**
	 * Test validation at level2 with an invalid name - this should pass as the genus part 
	 * does not have a capitalized first letter
	 */
	@Test
	public final void testWithoutCapitalizedUninomial() {
		name.setGenusOrUninomial("abies");		
		
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Default.class);
        assertTrue("There should not be a constraint violation as this name is valid at the default level",constraintViolations.isEmpty());
       
        constraintViolations  = validator.validate(name, Default.class,Level2.class);
        assertFalse("There should be a constraint violation as this name is valid at the default level, the first letter of the genus part is not capitalized",constraintViolations.isEmpty());
	}
	
	/**
	 * Test validation at level2 with an invalid name - this should pass as the genus part 
	 * does not have a capitalized first letter
	 */
	@Test
	public final void testWithCapitalizedNonFirstLetterInUninomial() {
		name.setGenusOrUninomial("ABies");		
		
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Default.class);
        assertTrue("There should not be a constraint violation as this name is valid at the default level",constraintViolations.isEmpty());
       
        constraintViolations  = validator.validate(name, Default.class,Level2.class);
        assertFalse("There should be a constraint violation as this name is valid at the default level, the second letter of the genus part is capitalized",constraintViolations.isEmpty());
	}
}
