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

import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.term.DefaultTermInitializer;
import eu.etaxonomy.cdm.validation.constraint.LectotypeSourceValidator;



/**
 * NOTE: In this test, the words "valid" and "invalid", loaded though
 * these terms are when applied to taxonomic names, only mean "passes the
 * rules of this validator" or not and should not be confused with the strict
 * nomenclatural and taxonomic sense of these words.
 *
 * @author a.mueller
 */
public class ValidLectotypeSourceTest extends ValidationTestBase {
	@SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ValidLectotypeSourceTest.class);

    static Class<?> validatorClass = LectotypeSourceValidator.class;
    static Class<?> group = Level2.class;

    private NameTypeDesignation nameDesignation;
    private SpecimenTypeDesignation specimenDesignation;
    private IBotanicalName name1;
    private TaxonName name2;
    private DerivedUnit specimen;
    private Reference lectotypeSource;


	@Before
	public void setUp() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();

		name1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name1.setNameCache("Aus aus");

	    name2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
	    name2.setNameCache("Aus bus");

	    specimen = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);

	    lectotypeSource = ReferenceFactory.newGeneric();
	}


/****************** TESTS *****************************/

    @Test
    public void testNameTypeDesignations() {

        //without source  => never invalid
        nameDesignation = NameTypeDesignation.NewInstance();
        validateHasNoConstraint(nameDesignation, validatorClass, group);

        nameDesignation.setTypeStatus(NameTypeDesignationStatus.AUTOMATIC());
        validateHasNoConstraint(nameDesignation, validatorClass, group);

        nameDesignation.setTypeStatus(NameTypeDesignationStatus.LECTOTYPE());
        validateHasNoConstraint(nameDesignation, validatorClass, group);

        //with source
        nameDesignation.setCitation(lectotypeSource);
        nameDesignation.setTypeStatus(null);
        validateHasConstraint(nameDesignation, validatorClass, group);

        nameDesignation.setTypeStatus(NameTypeDesignationStatus.AUTOMATIC());
        validateHasConstraint(nameDesignation, validatorClass, group);

        nameDesignation.setTypeStatus(NameTypeDesignationStatus.LECTOTYPE());
        validateHasNoConstraint(nameDesignation, validatorClass, group);

    }

    @Test
    public void testSpecimenTypeDesignations() {

        //without source  => never invalid
        specimenDesignation = SpecimenTypeDesignation.NewInstance();
        validateHasNoConstraint(specimenDesignation, validatorClass, group);

        specimenDesignation.setTypeStatus(SpecimenTypeDesignationStatus.HOLOTYPE());
        validateHasNoConstraint(specimenDesignation, validatorClass, group);

        specimenDesignation.setTypeStatus(SpecimenTypeDesignationStatus.LECTOTYPE());
        validateHasNoConstraint(specimenDesignation, validatorClass, group);

        //with source
        specimenDesignation.setCitation(lectotypeSource);
        specimenDesignation.setTypeStatus(null);
        validateHasConstraint(specimenDesignation, validatorClass, group);

        specimenDesignation.setTypeStatus(SpecimenTypeDesignationStatus.HOLOTYPE());
        validateHasConstraint(specimenDesignation, validatorClass, group);

        specimenDesignation.setTypeStatus(SpecimenTypeDesignationStatus.LECTOTYPE());
        validateHasNoConstraint(specimenDesignation, validatorClass, group);

    }

}
