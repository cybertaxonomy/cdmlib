/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.validation.constraint.BasionymsMustShareEpithetsAndAuthorsValidator;



/**
 * NOTE: In this test, the words "valid" and "invalid", loaded though
 * these terms are when applied to taxonomic names, only mean "passes the
 * rules of this validator" or not and should not be confused with the strict
 * nomenclatural and taxonomic sense of these words.
 *
 * @author ben.clark
 *
 *
 */
public class BasionymsMustShareEpithetsAndAuthorsTest extends ValidationTestBase {
	@SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(BasionymsMustShareEpithetsAndAuthorsTest.class);

	private IBotanicalName name;
	private TaxonNameBase basionymName;
	private Person author1;
	private Person author2;



	@Before
	public void setUp() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
		name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name.setGenusOrUninomial("Aus");
		name.setSpecificEpithet("aus");
		author1 = Person.NewTitledInstance("Person");
		name.setBasionymAuthorship(author1);

		author2 = Person.NewTitledInstance("Person2");
		basionymName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		basionymName.setGenusOrUninomial("Aus");
		basionymName.setSpecificEpithet("aus");
        basionymName.setCombinationAuthorship(author1);

        name.addBasionym(basionymName);
	}

/****************** TESTS *****************************/

	@Test
	public void testBasionymHasSameAuthorship() {
        Assert.assertEquals(1, name.getNameRelations().size());
        NameRelationship basRel = name.getNameRelations().iterator().next();
	    Set<ConstraintViolation<NameRelationship>> constraintViolations  = validator.validate(basRel, Level3.class);
        assertNoConstraintOnValidator((Set)constraintViolations, BasionymsMustShareEpithetsAndAuthorsValidator.class);

        basionymName.setCombinationAuthorship(author2);
        Assert.assertEquals(1, name.getNameRelations().size());
        basRel = name.getNameRelations().iterator().next();
        constraintViolations  = validator.validate(basRel, Level3.class);
        assertHasConstraintOnValidator((Set)constraintViolations, BasionymsMustShareEpithetsAndAuthorsValidator.class);
	}

	@Test
    public void testSameSpecificLastEpithet() {
	    Assert.assertEquals(1, name.getNameRelations().size());
       NameRelationship basRel = name.getNameRelations().iterator().next();
       Set<ConstraintViolation<NameRelationship>> constraintViolations  = validator.validate(basRel, Level3.class);
       assertNoConstraintOnValidator((Set)constraintViolations, BasionymsMustShareEpithetsAndAuthorsValidator.class);

       basionymName.setSpecificEpithet("bus");
       Assert.assertEquals(1, name.getNameRelations().size());
       basRel = name.getNameRelations().iterator().next();
       constraintViolations  = validator.validate(basRel, Level3.class);
       assertHasConstraintOnValidator((Set)constraintViolations, BasionymsMustShareEpithetsAndAuthorsValidator.class);
    }

    @Test
    public void testSameInfraSpecificEpithet() {
       name.setInfraSpecificEpithet("bus");
       basionymName.setInfraSpecificEpithet("bus");
       Assert.assertEquals(1, name.getNameRelations().size());
       NameRelationship basRel = name.getNameRelations().iterator().next();
       Set<ConstraintViolation<NameRelationship>> constraintViolations  = validator.validate(basRel, Level3.class);
       assertNoConstraintOnValidator((Set)constraintViolations, BasionymsMustShareEpithetsAndAuthorsValidator.class);

       basionymName.setInfraSpecificEpithet("heptodi");
       Assert.assertEquals(1, name.getNameRelations().size());
       basRel = name.getNameRelations().iterator().next();
       constraintViolations  = validator.validate(basRel, Level3.class);
       assertHasConstraintOnValidator((Set)constraintViolations, BasionymsMustShareEpithetsAndAuthorsValidator.class);
    }

    @Test
    public void testSameLastEpithetSpecificInfraSpecific() {
       name.setInfraSpecificEpithet("bus");
       basionymName.setSpecificEpithet("bus");
       Assert.assertEquals(1, name.getNameRelations().size());
       NameRelationship basRel = name.getNameRelations().iterator().next();
       Set<ConstraintViolation<NameRelationship>> constraintViolations  = validator.validate(basRel, Level3.class);
       assertNoConstraintOnValidator((Set)constraintViolations, BasionymsMustShareEpithetsAndAuthorsValidator.class);

       basionymName.setSpecificEpithet("heptodi");
       Assert.assertEquals(1, name.getNameRelations().size());
       basRel = name.getNameRelations().iterator().next();
       constraintViolations  = validator.validate(basRel, Level3.class);
       assertHasConstraintOnValidator((Set)constraintViolations, BasionymsMustShareEpithetsAndAuthorsValidator.class);
    }

    @Test
    public void testSameLastEpithetInfraSpecificSpecific() {
       name.setSpecificEpithet("bus");
       basionymName.setInfraSpecificEpithet("bus");
       Assert.assertEquals(1, name.getNameRelations().size());
       NameRelationship basRel = name.getNameRelations().iterator().next();
       Set<ConstraintViolation<NameRelationship>> constraintViolations  = validator.validate(basRel, Level3.class);
       assertNoConstraintOnValidator((Set)constraintViolations, BasionymsMustShareEpithetsAndAuthorsValidator.class);

       basionymName.setInfraSpecificEpithet("heptodi");
       Assert.assertEquals(1, name.getNameRelations().size());
       basRel = name.getNameRelations().iterator().next();
       constraintViolations  = validator.validate(basRel, Level3.class);
       assertHasConstraintOnValidator((Set)constraintViolations, BasionymsMustShareEpithetsAndAuthorsValidator.class);
    }

    @Test
    public void testZoologicalReference() {
       Reference nomRef = ReferenceFactory.newBook();
       Reference nomRef2 = ReferenceFactory.newBook();

       IZoologicalName zooName = TaxonNameFactory.NewZoologicalInstance(Rank.SPECIES());
       zooName.setGenusOrUninomial("Aus");
       zooName.setSpecificEpithet("aus");
       zooName.setBasionymAuthorship(author1);
       zooName.setNomenclaturalReference(nomRef);
       IZoologicalName originalCombination = TaxonNameFactory.NewZoologicalInstance(Rank.SPECIES());
       originalCombination.setGenusOrUninomial("Aus");
       originalCombination.setSpecificEpithet("aus");
       originalCombination.setCombinationAuthorship(author1);
       originalCombination.setNomenclaturalReference(nomRef);
       zooName.addBasionym(TaxonNameBase.castAndDeproxy(originalCombination));


       Assert.assertEquals(1, zooName.getNameRelations().size());
       NameRelationship basRel = zooName.getNameRelations().iterator().next();
       Set<ConstraintViolation<NameRelationship>> constraintViolations  = validator.validate(basRel, Level3.class);
       assertNoConstraintOnValidator((Set)constraintViolations, BasionymsMustShareEpithetsAndAuthorsValidator.class);

       originalCombination.setNomenclaturalReference(nomRef2);
       Assert.assertEquals(1, zooName.getNameRelations().size());
       basRel = zooName.getNameRelations().iterator().next();
       constraintViolations  = validator.validate(basRel, Level3.class);
       assertHasConstraintOnValidator((Set)constraintViolations, BasionymsMustShareEpithetsAndAuthorsValidator.class);

       //reset
       originalCombination.setNomenclaturalReference(nomRef);
       Assert.assertEquals(1, zooName.getNameRelations().size());
       basRel = zooName.getNameRelations().iterator().next();
       constraintViolations  = validator.validate(basRel, Level3.class);
       assertNoConstraintOnValidator((Set)constraintViolations, BasionymsMustShareEpithetsAndAuthorsValidator.class);

       zooName.setNomenclaturalMicroReference("A");
       originalCombination.setNomenclaturalMicroReference("B");
       Assert.assertEquals(1, zooName.getNameRelations().size());
       basRel = zooName.getNameRelations().iterator().next();
       constraintViolations  = validator.validate(basRel, Level3.class);
       assertHasConstraintOnValidator((Set)constraintViolations, BasionymsMustShareEpithetsAndAuthorsValidator.class);

    }



}
