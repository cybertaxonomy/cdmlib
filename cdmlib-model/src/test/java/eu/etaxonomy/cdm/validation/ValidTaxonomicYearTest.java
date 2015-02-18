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
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.validation.constraint.ValidTaxonomicYearValidator;



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
public class ValidTaxonomicYearTest extends ValidationTestBase {
	@SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ValidTaxonomicYearTest.class);



	private BotanicalName name;

	private Reference<?> beforeLineRef;
    private Reference<?> afterLineRef;


	@Before
	public void setUp() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();

		name = BotanicalName.NewInstance(Rank.SPECIES());
		name.setNameCache("Aus aus");
		name.setAuthorshipCache("L.");
		name.setFullTitleCache("Aus aus L.");
		name.setTitleCache("Aus aus L.", true);

		beforeLineRef = ReferenceFactory.newBook();
		beforeLineRef.setDatePublished(TimePeriodParser.parseString("1752"));

	    afterLineRef = ReferenceFactory.newBook();
	    afterLineRef.setDatePublished(TimePeriodParser.parseString("1754"));

	}


/****************** TESTS *****************************/

	@Test
	public void testNotBeforeLine() {
        name.setNomenclaturalReference(beforeLineRef);
	    Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Level3.class);
        assertHasConstraintOnValidator((Set)constraintViolations, ValidTaxonomicYearValidator.class);
	}

	@Test
    public void testAfterLine() {
        name.setNomenclaturalReference(afterLineRef);
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Level3.class);
        assertNoConstraintOnValidator((Set)constraintViolations, ValidTaxonomicYearValidator.class);
    }

    @Test
    public void testNoNomRef() {
        name.setNomenclaturalReference(null);
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Level3.class);
        assertNoConstraintOnValidator((Set)constraintViolations, ValidTaxonomicYearValidator.class);
    }

    @Test
    public void testZooName() {

        ZoologicalName zooName = ZoologicalName.NewInstance(Rank.SPECIES());

        zooName.setNomenclaturalReference(null);
        zooName.setPublicationYear(1788);
        Set<ConstraintViolation<ZoologicalName>> constraintViolations  = validator.validate(zooName, Level3.class);
        assertNoConstraintOnValidator((Set)constraintViolations, ValidTaxonomicYearValidator.class);

        zooName.setPublicationYear(1730);
        constraintViolations  = validator.validate(zooName, Level3.class);
        //currently we do not use the publication year for zooName.getYear, but this may change in future.
        //we may adapt this test then
        assertNoConstraintOnValidator((Set)constraintViolations, ValidTaxonomicYearValidator.class);

        zooName.setNomenclaturalReference(beforeLineRef);
        constraintViolations  = validator.validate(zooName, Level3.class);
        assertHasConstraintOnValidator((Set)constraintViolations, ValidTaxonomicYearValidator.class);


    }


}
