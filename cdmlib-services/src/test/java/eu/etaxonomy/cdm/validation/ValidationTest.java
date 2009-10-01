/**
 * 
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
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author ben.clark
 *
 */
@SuppressWarnings("unused")
public class ValidationTest extends CdmIntegrationTest {
	private static final Logger logger = Logger.getLogger(ValidationTest.class);
	
	@SpringBeanByType
	private Validator validator;
	
	private BotanicalName name;
	
	@Before
	public void setUp() {
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
	 * Test validation at the "default" level with an invalid name
	 */
	@Test
	@DataSet
	public final void testLevel2ValidationWithValidName() {
		name.setGenusOrUninomial("Abies");
		name.setSpecificEpithet("balsamea");
		name.setNameCache("Abies balsamea");
		name.setAuthorshipCache("L.");
		name.setTitleCache("Abies balsamea L.");
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Default.class,Level2.class,Level3.class);
        for(ConstraintViolation<BotanicalName> constraintViolation : constraintViolations) {
        	System.out.println(constraintViolation.getPropertyPath() + " " + constraintViolation.getMessage());
        }
        //assertFalse("There should be a constraint violation as this name is invalid at the default level",constraintViolations.isEmpty());
	}
	
	
}
