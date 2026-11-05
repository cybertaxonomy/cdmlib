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

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.validation.constraint.CorrectEpithetsForRankValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.groups.Default;

/**
 * NOTE: In this test, the words "valid" and "invalid", loaded though
 * these terms are when applied to taxonomic names, only mean "passes the
 * rules of this validator" or not and should not be confused with the strict
 * nomenclatural and taxonomic sense of these words.
 *
 * @author ben.clark
 */
public class CorrectEpithetsForRankTest extends ValidationTestBase {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private IBotanicalName name;

	@BeforeEach
	public void setUp() {
		name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name.setNameCache("Aus aus");
		name.setAuthorshipCache("L.");
		name.setFullTitleCache("Aus aus L.");
		name.setTitleCache("Aus aus L.", true);
	}

/****************** TESTS *****************************/

	@Test
	public void testValidSpecificName() {
		name.setGenusOrUninomial("Aus");
		name.setSpecificEpithet("aus");
        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Level2.class, Default.class);
        assertTrue(constraintViolations.isEmpty(), "There should be no constraint violations as this name has the correct epithets for its rank");
	}

	@Test
	public void testInValidSpecificName() {
		name.setGenusOrUninomial("Aus");
		name.setSpecificEpithet(null); // at the default level, this property can be null
        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertFalse(constraintViolations.isEmpty(), "There should be a constraint violation as this name does not have a specific epithet");
        assertHasConstraintOnValidator((Set)constraintViolations, CorrectEpithetsForRankValidator.class);
	}

	@Test
	public void testValidFamilyGroupName() {
		name.setGenusOrUninomial("Aus");
		name.setRank(Rank.FAMILY());
        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertTrue(constraintViolations.isEmpty(), "There should be no constraint violations as this name has the correct epithets for it rank");
	}

	@Test
	public void testInValidFamilyGroupName() {
		name.setGenusOrUninomial("Aus");
		name.setRank(Rank.FAMILY()); // at the default level, this property can be null
		name.setSpecificEpithet("aus");
        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertFalse(constraintViolations.isEmpty(), "There should be a constraint violation as this name does not have a specific epithet");
	}

	@Test
	public void testValidGenusGroupName() {
		name.setGenusOrUninomial("Aus");
		name.setInfraGenericEpithet("bus");
		name.setRank(Rank.SUBGENUS());
        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertTrue(constraintViolations.isEmpty(), "There should be no constraint violations as this name has the correct epithets for it rank");
	}

	@Test
	public void testInValidGenusGroupName() {
		name.setGenusOrUninomial("Aus");
		name.setRank(Rank.SUBGENUS()); // at the default level, this property can be null
		name.setSpecificEpithet("aus");
        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertFalse(constraintViolations.isEmpty(), "There should be a constraint violation as this name does not have a specific epithet");
	}

	@Test
	public void testValidInfraSpecificName() {
		name.setGenusOrUninomial("Aus");
		name.setSpecificEpithet("aus");
		name.setInfraSpecificEpithet("ceus");
		name.setRank(Rank.SUBSPECIES());
        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertTrue(constraintViolations.isEmpty(), "There should be no constraint violations as this name has the correct epithets for it rank");
	}

	@Test
	public void testInValidInfraSpecificName() {
		name.setGenusOrUninomial("Aus");
		name.setRank(Rank.SUBGENUS()); // at the default level, this property can be null
		name.setSpecificEpithet("aus");
        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertFalse(constraintViolations.isEmpty(), "There should be a constraint violation as this name does not have a specific epithet");
	}
}
