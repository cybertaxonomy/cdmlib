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
import javax.validation.constraints.Pattern;
import javax.validation.groups.Default;

import org.apache.log4j.Logger;
import org.hibernate.validator.internal.constraintvalidators.bv.PatternValidator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;


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
public class NameValidCharactersTest extends ValidationTestBase  {
	private static final Logger logger = Logger.getLogger(NameValidCharactersTest.class);

	private IBotanicalName name;

	@Before
	public void setUp() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
		name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
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


        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Default.class);
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

        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Default.class);
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

        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Default.class);
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

        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Default.class);
        assertTrue("There should not be a constraint violation as this name is valid at the default level",constraintViolations.isEmpty());

        constraintViolations  = validator.validate(name, Default.class,Level2.class);
        assertFalse("There should be a constraint violation as this name is valid at the default level, the second letter of the genus part is capitalized",constraintViolations.isEmpty());
	}

	/**
     * Test validation at level2 with an invalid name - this should pass as the genus part
     * does not have a capitalized first letter
     */
    @Test
    public final void testAuthorship() {
        Set<ConstraintViolation<IBotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertNoConstraintOnValidator((Set)constraintViolations, Pattern.class);

        name.setAuthorshipCache("", true);
        constraintViolations  = validator.validate(name, Level2.class);
        assertHasConstraintOnValidator((Set)constraintViolations, PatternValidator.class);

        name.setAuthorshipCache(null, true);
        constraintViolations  = validator.validate(name, Level2.class);
        assertNoConstraintOnValidator((Set)constraintViolations, PatternValidator.class);

        name.setAuthorshipCache("L\\u05EB", true);
        constraintViolations  = validator.validate(name, Level2.class);
        assertHasConstraintOnValidator((Set)constraintViolations, PatternValidator.class);

    }
}
