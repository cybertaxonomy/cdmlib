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

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.groups.Default;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.validation.constraint.CorrectRanksForCodeValidator;

/**
 * NOTE: In this test, the words "valid" and "invalid", loaded though
 * these terms are when applied to taxonomic names, only mean "passes the
 * rules of this validator" or not and should not be confused with the strict
 * nomenclatural and taxonomic sense of these words.
 *
 * @author a.mueller
 * @since 2021-10-15
 */
public class CorrectRanksForCodeTest extends ValidationTestBase {

	@SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(CorrectRanksForCodeTest.class);

	private IBotanicalName name;

	@Before
	public void setUp() {
		name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name.setNameCache("Aus aus");
		name.setAuthorshipCache("L.");
		name.setFullTitleCache("Aus aus L.");
		name.setTitleCache("Aus aus L.", true);

    }

/****************** TESTS *****************************/

	@Test
	public void testCultivars() {
	    name.setNameType(NomenclaturalCode.ICNCP);
		name.setRank(Rank.CULTIVAR());
        name.setGenusOrUninomial("Aus");
        name.setSpecificEpithet("aus");
        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Level2.class, Default.class);
        assertTrue("There should be no constraint violations as this name has a rank according to the code", constraintViolations.isEmpty());
        name.setNameType(NomenclaturalCode.ICNAFP);
        constraintViolations  = validator.validate(name, Level2.class, Default.class);
        assertFalse("There should be a constraint violation as this name of rank cultivar has code ICNafp", constraintViolations.isEmpty());
        assertHasConstraintOnValidator((Set)constraintViolations, CorrectRanksForCodeValidator.class);

        name.setNameType(NomenclaturalCode.ICNCP);
        name.setRank(Rank.SPECIES());
        assertFalse("There should be a constraint violation as this name of rank cultivar has code ICNafp", constraintViolations.isEmpty());
        assertHasConstraintOnValidator((Set)constraintViolations, CorrectRanksForCodeValidator.class);
	}

	@Test
	public void testZoological() {
        name.setNameType(NomenclaturalCode.ICZN);
        name.setRank(Rank.SECTION_ZOOLOGY());
        name.setGenusOrUninomial("Aus");
        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Level2.class, Default.class);
        assertTrue("There should be no constraint violations as this name has a rank according to the code", constraintViolations.isEmpty());
        name.setNameType(NomenclaturalCode.ICNAFP);
        constraintViolations  = validator.validate(name, Level2.class, Default.class);
        assertFalse("There should be a constraint violation as this name has a zoological rank but code ICNafp", constraintViolations.isEmpty());
        assertHasConstraintOnValidator((Set)constraintViolations, CorrectRanksForCodeValidator.class);
	}

    @Test
    public void testBotanical() {
        name.setNameType(NomenclaturalCode.ICNAFP);
        name.setRank(Rank.SECTION_BOTANY());
        name.setGenusOrUninomial("Aus");
        name.setInfraGenericEpithet("bus");
        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Level2.class, Default.class);
        assertTrue("There should be no constraint violations as this name has a rank according to the code", constraintViolations.isEmpty());
        name.setNameType(NomenclaturalCode.ICZN);
        constraintViolations  = validator.validate(name, Level2.class, Default.class);
        assertFalse("There should be a constraint violation as this name has a botanical rank but code ICZN", constraintViolations.isEmpty());
        assertHasConstraintOnValidator((Set)constraintViolations, CorrectRanksForCodeValidator.class);
    }
}
