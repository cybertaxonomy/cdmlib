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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;

/**
 * NOTE: In this test, the words "valid" and "invalid", loaded though
 * these terms are when applied to taxonomic names, only mean "passes the
 * rules of this validator" or not and should not be confused with the strict
 * nomenclatural and taxonomic sense of these words.
 *
 * @author ben.clark
 */
@SuppressWarnings("unused")
public class ValidationTest extends CdmServicesTransactionalSpringIntegrationTest {

    private static final Logger logger = LogManager.getLogger();

	@Autowired
	private Validator validator;

	@Autowired
	private ITermService termService;

	private IBotanicalName name;

	@BeforeEach
	public void setUp() {

		//Rank speciesRank = (Rank)termService.find(Rank.uuidSpecies);
		name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
	}

/****************** TESTS *****************************/

	/**
	 * Test validation factory initialization and autowiring
	 * into an instance of javax.valdation.Validator
	 */
	@Test
	@CdmDataSet
	public final void testValidatorAutowire() {
        assertNotNull(validator, "the validator should not be null");
	}

	/**
	 * Test validation at the "default" level with a valid name
	 */
	@Test
	@CdmDataSet
	public final void testDefaultValidationWithValidName() {
        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name);
        assertTrue(constraintViolations.isEmpty(), "There should be no constraint violations as this name is valid at the default level");
	}

	/**
	 * Test validation at the "default" level with an invalid name
	 */
	@Test
	@CdmDataSet
	public final void testDefaultValidationWithInValidName() {
		name.setGenusOrUninomial("");
        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name);
        assertTrue(constraintViolations.isEmpty(), "There should not be a constraint violation as this name is invalid at the default level because the setter checks for the empty string");
	}

	/**
	 * Test validation at level2 with a valid name
	 */
	@Test
	@CdmDataSet
	public final void testLevel2ValidationWithValidName() {
		name.setGenusOrUninomial("Abies");
		name.setSpecificEpithet("balsamea");
		name.setNameCache("Abies balsamea");
		name.setAuthorshipCache("L.");
		name.setTitleCache("Abies balsamea L.", true);
		name.setFullTitleCache("Abies balsamea L.");

        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Default.class,Level2.class);
        assertTrue(constraintViolations.isEmpty(), "There should not be a constraint violation as this name is valid at the default and at the second level");
	}

	/**
	 * Test validation at level2 with an invalid name
	 */
	@Test
	@CdmDataSet
	public final void testLevel2ValidationWithInValidName() {
		name.setGenusOrUninomial("Abies");
		name.setSpecificEpithet("balsamea");

        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Default.class);
        assertTrue(constraintViolations.isEmpty(), "There should not be a constraint violation as this name is valid at the default level");

        constraintViolations  = validator.validate(name, Default.class,Level2.class);
        assertFalse(constraintViolations.isEmpty(), "There should be a constraint violation as this name is valid at the default level, but invalid at the second level");
	}

	/**
	 * Test validation at level3 with a valid name
	 */
	@Test
	@CdmDataSet
	public final void testLevel3ValidationWithValidName() {
		name.setGenusOrUninomial("Abies");
		name.setSpecificEpithet("balsamea");
		name.setNameCache("Abies balsamea");
		name.setAuthorshipCache("L.");
		name.setTitleCache("Abies balsamea L.", true);
		name.setFullTitleCache("Abies balsamea L.");

        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Default.class, Level2.class/*, Level3.class*/);
        assertTrue(constraintViolations.isEmpty(), "There should not be a constraint violation as this name is valid at all levels");
	}

	/**
	 * Test validation at the level3 with an invalid name
	 */
	@Test
	@CdmDataSet
	public final void testLevel3ValidationWithInValidName() {
		name.setGenusOrUninomial("Abies");
		name.setSpecificEpithet("alba");
		name.setNameCache("Abies alba");
		name.setAuthorshipCache("Mill.");
		name.setTitleCache("Abies alba Mill.", true);
		name.setFullTitleCache("Abies alba Mill.");
		name.setNomenclaturalReference(null);
		//name.setNomenclaturalMicroReference(" ");

        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Default.class, Level2.class);
        assertTrue(constraintViolations.isEmpty(), "There should not be a constraint violation as this name is valid at the default and second level");
        constraintViolations  = validator.validate(name, Default.class,Level2.class, Level3.class);
        assertFalse(constraintViolations.isEmpty(), "There should be a constraint violation as this name is valid at the default and second level, but invalid at the third level as duplicates exist");
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
