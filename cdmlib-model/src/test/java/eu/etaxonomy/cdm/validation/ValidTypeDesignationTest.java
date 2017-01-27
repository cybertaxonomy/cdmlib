/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.validation;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.validation.constraint.TypeDesignationValidator;



/**
 * NOTE: In this test, the words "valid" and "invalid", loaded though
 * these terms are when applied to taxonomic names, only mean "passes the
 * rules of this validator" or not and should not be confused with the strict
 * nomenclatural and taxonomic sense of these words.
 *
 * @author a.mueller
 *
 *
 */
public class ValidTypeDesignationTest extends ValidationTestBase {
	@SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ValidTypeDesignationTest.class);

    static Class validatorClass = TypeDesignationValidator.class;
    static Class group = Level2.class;

    private NameTypeDesignation nameDesignation;
    private SpecimenTypeDesignation specimenDesignation;
    private BotanicalName name1;
    private BotanicalName name2;
    private DerivedUnit specimen;


	@Before
	public void setUp() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();



		name1 = TaxonNameBase.NewBotanicalInstance(Rank.SPECIES());
		name1.setNameCache("Aus aus");

	    name2 = TaxonNameBase.NewBotanicalInstance(Rank.SPECIES());
	    name2.setNameCache("Aus bus");

	    specimen = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);


	}


/****************** TESTS *****************************/

    @Test
    public void testNameTypeDesignations() {
        nameDesignation = NameTypeDesignation.NewInstance();
        validateHasConstraint(nameDesignation, validatorClass, group);

        name1.addTypeDesignation(nameDesignation, false);
        nameDesignation.setTypeName(name2);
        validateHasNoConstraint(nameDesignation, validatorClass, group);

        nameDesignation.setTypeName(null);
        validateHasConstraint(nameDesignation, validatorClass, group);

        nameDesignation.setNotDesignated(true);
        validateHasNoConstraint(nameDesignation, validatorClass, group);

    }

    @Test
    public void testSpecimenTypeDesignations() {
        specimenDesignation = SpecimenTypeDesignation.NewInstance();
        validateHasConstraint(specimenDesignation, validatorClass, group);

        name1.addTypeDesignation(specimenDesignation, false);
        specimenDesignation.setTypeSpecimen(specimen);
        validateHasNoConstraint(specimenDesignation, validatorClass, group);

        specimenDesignation.setTypeSpecimen(null);
        validateHasConstraint(specimenDesignation, validatorClass, group);

        specimenDesignation.setNotDesignated(true);
        validateHasNoConstraint(specimenDesignation, validatorClass, group);

    }

}
