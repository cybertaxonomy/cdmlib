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
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
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

    static Class validatorClass = ValidTaxonomicYearValidator.class;
    static Class group = Level3.class;


	private BotanicalName name;

	private Reference beforeLineeRef;
    private Reference afterLineeRef;


	@Before
	public void setUp() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();

		name = TaxonNameBase.NewBotanicalInstance(Rank.SPECIES());
		name.setNameCache("Aus aus");
		name.setAuthorshipCache("L.");
		name.setFullTitleCache("Aus aus L.");
		name.setTitleCache("Aus aus L.", true);

		beforeLineeRef = ReferenceFactory.newBook();
		beforeLineeRef.setDatePublished(TimePeriodParser.parseString("1752"));

	    afterLineeRef = ReferenceFactory.newBook();
	    afterLineeRef.setDatePublished(TimePeriodParser.parseString("1754"));

	}


/****************** TESTS *****************************/

	@Test
	public void testNotBeforeLinee() {
        name.setNomenclaturalReference(beforeLineeRef);
        validateHasConstraint(name, validatorClass, group);
	}

	@Test
    public void testAfterLinee() {
        name.setNomenclaturalReference(afterLineeRef);
        validateHasNoConstraint(name, validatorClass, group);
    }

    @Test
    public void testNoNomRef() {
        name.setNomenclaturalReference(null);
        validateHasNoConstraint(name, validatorClass, group);
    }

    @Test
    public void testZooName() {

        ZoologicalName zooName = TaxonNameBase.NewZoologicalInstance(Rank.SPECIES());

        zooName.setNomenclaturalReference(null);
        zooName.setPublicationYear(1788);
        validateHasNoConstraint(zooName, validatorClass, group);

        zooName.setPublicationYear(1730);
        //currently we do not use the publication year for zooName.getYear, but this may change in future.
        //we may adapt this test then
        validateHasNoConstraint(zooName, validatorClass, group);

        zooName.setNomenclaturalReference(beforeLineeRef);
        validateHasConstraint(zooName, validatorClass, group);


    }


}
