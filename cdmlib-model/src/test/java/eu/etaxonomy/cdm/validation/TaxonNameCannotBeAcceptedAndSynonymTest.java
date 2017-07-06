/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.validation;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
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
public class TaxonNameCannotBeAcceptedAndSynonymTest extends ValidationTestBase{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonNameCannotBeAcceptedAndSynonymTest.class);

	private TaxonName name1;
	private IBotanicalName name2;
	private IBotanicalName name3;
    private Taxon taxon1;
    private Taxon taxon2;
    private Synonym synonym;
    private Reference sec1;
    private Reference sec2;

	@Before
	public void setUp() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
		name1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name3 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());

		sec1 = ReferenceFactory.newGeneric();
		sec2 = ReferenceFactory.newGeneric();

		taxon1 = Taxon.NewInstance(name1, sec1);
		taxon1.setTitleCache("Aus aus", true);
		taxon2 = Taxon.NewInstance(name2, sec1);
		taxon2.setTitleCache("Aus bus", true);
		synonym = Synonym.NewInstance(name3, sec1);
		synonym.setTitleCache("Aus ceus", true);
	}


/****************** TESTS *****************************/

	@Test
	public void testValidTaxon() {
		assert taxon1.getName().getTaxonBases().size() == 1;
        Set<ConstraintViolation<Taxon>> constraintViolations  = validator.validate(taxon1, Level3.class);
        assertTrue("There should be no constraint violations as this taxon does not have the same name as any other taxa",constraintViolations.isEmpty());
	}

	@Test
	public void testTwoAcceptedTaxaWithSameNameSameSec() {
		taxon2.setName(name1);
		assert taxon1.getName().getTaxonBases().size() == 2;
        Set<ConstraintViolation<Taxon>> constraintViolations  = validator.validate(taxon1, Level3.class);
        assertTrue("There should be a single constraint violation as this taxon shares the same name as taxon2 and is according to the same authority, sec1",!constraintViolations.isEmpty());
	}

	@Test
	public void testTwoAcceptedTaxaWithSameNameDifferentSec() {
		taxon2.setName(name1);
		taxon2.setSec(sec2);
		assert taxon1.getName().getTaxonBases().size() == 2;
        Set<ConstraintViolation<Taxon>> constraintViolations  = validator.validate(taxon1, Level3.class);
        assertTrue("There should not be any constraint violations despite both accepted taxa sharing the same name as the sec reference is different",constraintViolations.isEmpty());
	}

	@Test
	public void testTaxonAndSynonymWithSameNameSameSec() {
		synonym.setName(name1);
		assert taxon1.getName().getTaxonBases().size() == 2;
        Set<ConstraintViolation<Taxon>> constraintViolations  = validator.validate(taxon1, Level3.class);
        assertTrue("There should be a single constraint violation as this taxon shares the same name as synonym and is according to the same authority, sec1",!constraintViolations.isEmpty());
	}
}
