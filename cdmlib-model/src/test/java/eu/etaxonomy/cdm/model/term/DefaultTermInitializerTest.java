/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.term.init.TermLoader;

/**
 * @author a.mueller
 * @since 02.03.2009
 */
public class DefaultTermInitializerTest {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @BeforeAll
    public static void setUpBeforeClass() {
        TermLoader termLoader = new TermLoader();
        termLoader.unloadAllTerms();
    }

	@Test
	public void testInitialize(){
		assertNull(Language.DEFAULT(), "At the beginning of the initialization test the default language should still be null but is not");
		DefaultTermInitializer initalizer = new DefaultTermInitializer();
		initalizer.initialize();
		assertNotNull(Language.DEFAULT(), "Default language should be english but is null");
		TermVocabulary<Language> voc = Language.DEFAULT().getVocabulary();
		assertNotNull(voc.getRepresentation(Language.DEFAULT()), "language for language vocabulary representation was null but must be default language");

		Set<MeasurementUnit> units = Feature.ALTITUDE().getRecommendedMeasurementUnits();
		assertEquals(1, units.size(), "Recommended measurement unit should exist");
		assertEquals(MeasurementUnit.uuidMeter, units.iterator().next().getUuid());

		IdentifierType idTypeIF = IdentifierType.IDENTIFIER_NAME_IF();
		assertEquals("https://www.indexfungorum.org/names/NamesRecord.asp?RecordID={@ID}", idTypeIF.getUrlPattern());
	}
}
