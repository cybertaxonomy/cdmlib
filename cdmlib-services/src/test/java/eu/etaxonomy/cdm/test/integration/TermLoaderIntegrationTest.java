/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesSpringIntegrationTest;

/**
 * @author a.mueller
 */
public class TermLoaderIntegrationTest extends CdmServicesSpringIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private UUID misappliedNameFroUuid = UUID.fromString("1ed87175-59dd-437e-959e-0d71583d8417");

	@Test
	public final void testTermsAreLoaded() {
		assertNotNull(TaxonRelationshipType.MISAPPLIED_NAME_FOR(), "TaxonRelationshipType.IS_MISAPPLIED_NAME_FOR should have been initialized");
		assertEquals(misappliedNameFroUuid, TaxonRelationshipType.uuidMisappliedNameFor, "TaxonRelationshipType.IS_MISAPPLIED_NAME_FOR should have a uuid of " + misappliedNameFroUuid);
	}

	@Test
	public void testUnlazyStatusTerms() {
			assertNotNull(NomenclaturalStatusType.SUPERFLUOUS(), "NomenclaturalStatusType.SUPERFLUOUS should exist");
			assertNotNull(NomenclaturalStatusType.NUDUM(), "NomenclaturalStatusType.NUDUM should exist");
			assertNotNull(NomenclaturalStatusType.ILLEGITIMATE(), "NomenclaturalStatusType.ILLEGITIMATE should exist");
			assertNotNull(NomenclaturalStatusType.INVALID(), "NomenclaturalStatusType.INVALID should exist");
			assertNotNull(NomenclaturalStatusType.CONSERVED(), "NomenclaturalStatusType.CONSERVED should exist");
			assertNotNull(NomenclaturalStatusType.ALTERNATIVE(), "NomenclaturalStatusType.ALTERNATIVE should exist");
			assertNotNull(NomenclaturalStatusType.REJECTED(), "NomenclaturalStatusType.REJECTED should exist");
			assertNotNull(NomenclaturalStatusType.REJECTED_PROP(), "NomenclaturalStatusType.REJECTED_PROP should exist");
			assertNotNull(NomenclaturalStatusType.PROVISIONAL(), "NomenclaturalStatusType.PROVISIONAL should exist");
			assertNotNull(NomenclaturalStatusType.SUBNUDUM(), "NomenclaturalStatusType.SUBNUDUM should exist");
			assertNotNull(NomenclaturalStatusType.OPUS_UTIQUE_OPPR(), "NomenclaturalStatusType.OPUS_UTIQUE_OPPR should exist");
			assertNotNull(NomenclaturalStatusType.VALID(), "NomenclaturalStatusType.VALID should exist");
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
