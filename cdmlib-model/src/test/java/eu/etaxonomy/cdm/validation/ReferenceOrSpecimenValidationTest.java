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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @since 2022-12-19
 */
@SuppressWarnings("unused")
public class ReferenceOrSpecimenValidationTest extends ValidationTestBase {

    private static final Logger logger = LogManager.getLogger();

    private DescriptionElementSource source;
    private Reference book;
    private DerivedUnit specimen;

	@Before
	public void setUp() {
		book = ReferenceFactory.newBook();
		book.setTitleCache("Lorem ipsum", true);
		book.setIsbn("1-919795-99-5");
		specimen = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);
	}

//****************** TESTS *****************************/

	@Test
	public final void test() {
        DescriptionElementSource source = DescriptionElementSource.NewInstance(OriginalSourceType.PrimaryTaxonomicSource);

	    Set<ConstraintViolation<DescriptionElementSource>> constraintViolations  = validator.validate(source, Default.class);
        assertTrue("Source with neither reference nor specimen should not fail", constraintViolations.isEmpty());

        source.setCitation(book);
        constraintViolations  = validator.validate(source, Default.class);
        assertTrue("Source with only reference should not fail", constraintViolations.isEmpty());

        source.setSpecimen(specimen);
        constraintViolations  = validator.validate(source, Default.class);
        assertFalse("Source with both reference and specimen should fail", constraintViolations.isEmpty());

        source.setCitation(null);
        constraintViolations  = validator.validate(source, Default.class);
        assertTrue("Source with only specimen specimen should not fail", constraintViolations.isEmpty());

	}

}