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

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.validation.constraint.NameMustFollowCodeValidator;



/**
 * Test class for {@link NameMustFollowCodeValidator}
 *
 * @author a.mueller
 \* @since 11.03.2017
 */
public class NameMustFollowCodeTest extends ValidationTestBase {
	@SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(NameMustFollowCodeTest.class);

	private TaxonName nonViralName;
    private TaxonName viralName;
    private TaxonName bacterialName;
    private TaxonName zoologicalName;
    private TaxonName cultivarName;

	@Before
	public void setUp() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
		nonViralName = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
		viralName = TaxonNameFactory.NewViralInstance(Rank.SPECIES());
		zoologicalName = TaxonNameFactory.NewZoologicalInstance(Rank.SPECIES());
		bacterialName = TaxonNameFactory.NewBacterialInstance(Rank.SPECIES());
	    cultivarName = TaxonNameFactory.NewCultivarInstance(Rank.SPECIES());
	}


/****************** TESTS *****************************/

    @Test
    public void testValidEmptyNames() {
        Set<ConstraintViolation<TaxonName>> constraintViolations
                            = validator.validate(cultivarName);
        assertTrue("There should be no constraint violations as this name has data set and therefore no unvalid attributes set", constraintViolations.isEmpty());

        constraintViolations  = validator.validate(nonViralName);
        assertTrue("There should be no constraint violations as this name has data set and therefore no unvalid attributes set", constraintViolations.isEmpty());

        constraintViolations  = validator.validate(viralName);
        assertTrue("There should be no constraint violations as this name has data set and therefore no unvalid attributes set", constraintViolations.isEmpty());

        constraintViolations  = validator.validate(zoologicalName);
        assertTrue("There should be no constraint violations as this name has data set and therefore no unvalid attributes set", constraintViolations.isEmpty());

        constraintViolations  = validator.validate(bacterialName);
        assertTrue("There should be no constraint violations as this name has data set and therefore no unvalid attributes set", constraintViolations.isEmpty());
    }

    @Test
    public void testMessage() {
        nonViralName.setAcronym("acronym");
        Set<ConstraintViolation<TaxonName>> constraintViolations  = validator.validate(nonViralName);
        assertFalse("There should be a constraint violation as a nonViralName must not have an acronym", constraintViolations.isEmpty());
        String message = constraintViolations.iterator().next().getMessage();
        String expected = "Taxon name must only have attributes set that are available according to their code. E.g. 'acronym name' should only be available for viral names.";
        Assert.assertEquals(expected, message);
    }

	@Test
	public void testValidNonViralName() {
	    nonViralName.setAcronym("acronym");
	    Set<ConstraintViolation<TaxonName>> constraintViolations  = validator.validate(nonViralName);
        assertFalse("There should be a constraint violation as a nonViralName must not have an acronym", constraintViolations.isEmpty());

        nonViralName = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
        nonViralName.setBreed("Breed");
        constraintViolations  = validator.validate(nonViralName);
        assertFalse("There should be a constraint violation as pure NonViralNames must not have a breed", constraintViolations.isEmpty());

        nonViralName = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
        nonViralName.setOriginalPublicationYear(1987);
        constraintViolations  = validator.validate(nonViralName);
        assertFalse("There should be a constraint violation as pure NonViralNames must not have an original publication year", constraintViolations.isEmpty());

        nonViralName = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
        nonViralName.setPublicationYear(2001);
        constraintViolations  = validator.validate(nonViralName);
        assertFalse("There should be a constraint violation as pure NonViralNames must not have a publication year", constraintViolations.isEmpty());

        nonViralName = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
        nonViralName.setSubGenusAuthorship("SubGenusAuthor");
        constraintViolations  = validator.validate(nonViralName);
        assertFalse("There should be a constraint violation as pure NonViralNames must not have a subgenus author", constraintViolations.isEmpty());

        nonViralName = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
        nonViralName.setNameApprobation("Name approbation");
        constraintViolations  = validator.validate(nonViralName);
        assertFalse("There should be a constraint violation as pure NonViralNames must not have a name approbation", constraintViolations.isEmpty());

        //Valid
        nonViralName = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
        nonViralName.setMonomHybrid(true);
        constraintViolations  = validator.validate(nonViralName);
        assertTrue("There should be no constraint violation as NonViralNames may have a hybrid flag set", constraintViolations.isEmpty());

        nonViralName = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
        nonViralName.setGenusOrUninomial("Genus");
        constraintViolations  = validator.validate(nonViralName);
        assertTrue("There should be no constraint violation as NonViralNames may have a genus name set", constraintViolations.isEmpty());

        nonViralName = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
        nonViralName.setNameCache("NameCache");
        constraintViolations  = validator.validate(nonViralName);
        assertTrue("There should be no constraint violation as NonViralNames may have the name cache set", constraintViolations.isEmpty());

        nonViralName = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
        TaxonName childName = TaxonNameFactory.NewViralInstance(Rank.SPECIES());
        nonViralName.addHybridChild(childName, HybridRelationshipType.FIRST_PARENT(), null);
        constraintViolations  = validator.validate(nonViralName);
        assertTrue("There should be no constraint violation as NonViralNames may have a hybrid child", constraintViolations.isEmpty());

        //TBC
	}

    @Test
    public void testValidViralName() {
        viralName.setAcronym("acronym");
        Set<ConstraintViolation<TaxonName>> constraintViolations  = validator.validate(viralName);
        assertTrue("There should be no constraint violation as a viral name may have acronym set", constraintViolations.isEmpty());

        //Invalid
        viralName = TaxonNameFactory.NewViralInstance(Rank.SPECIES());
        viralName.setMonomHybrid(true);
        constraintViolations  = validator.validate(viralName);
        assertFalse("There should be a constraint violation as a ViralName must not have a hybrid flag set", constraintViolations.isEmpty());

        viralName = TaxonNameFactory.NewViralInstance(Rank.SPECIES());
        viralName.setGenusOrUninomial("Genus");
        constraintViolations  = validator.validate(viralName);
        assertFalse("There should be a constraint violation as a ViralName must not have the genus name set", constraintViolations.isEmpty());

        viralName = TaxonNameFactory.NewViralInstance(Rank.SPECIES());
        viralName.setNameCache("NameCache");
        constraintViolations  = validator.validate(viralName);
        assertFalse("There should be a constraint violation as a ViralName must not have the nameCache set", constraintViolations.isEmpty());

        viralName = TaxonNameFactory.NewViralInstance(Rank.SPECIES());
        TaxonName childName = TaxonNameFactory.NewViralInstance(Rank.SPECIES());
        viralName.addHybridChild(childName, HybridRelationshipType.FIRST_PARENT(), null);
        constraintViolations  = validator.validate(viralName);
        assertFalse("There should be a constraint violation as a ViralName must not have hybrid child", constraintViolations.isEmpty());

        //TBC

    }

    @Test
    public void testValidZoologicalName() {
        zoologicalName.setBreed("Breed");
        zoologicalName.setOriginalPublicationYear(1987);
        zoologicalName.setPublicationYear(2001);

        Set<ConstraintViolation<TaxonName>> constraintViolations  = validator.validate(zoologicalName);
        assertTrue("There should be no constraint violation as a zoological name may have breed and years set", constraintViolations.isEmpty());
    }

    @Test
    public void testValidBacterialName() {
        bacterialName.setSubGenusAuthorship("Subgenus author");
        bacterialName.setNameApprobation("Name approbation");

        Set<ConstraintViolation<TaxonName>> constraintViolations  = validator.validate(bacterialName);
        assertTrue("There should be no constraint violation as a bacterial name may have subgenus authorship or name approbation set", constraintViolations.isEmpty());
    }

}
