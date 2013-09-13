/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * @author a.mueller
 *
 */
public class TermLoaderIntegrationTest extends CdmIntegrationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermLoaderIntegrationTest.class);

	private UUID taxonomicallyIncludedInUuid;
	
    @Before
	public void setUp() {
    	taxonomicallyIncludedInUuid = UUID.fromString("d13fecdf-eb44-4dd7-9244-26679c05df1c");
    }
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.ConceptRelationshipType#TAXONOMICALLY_INCLUDED_IN()}.
	 */
	@Test
	public final void testTermsAreLoaded() {
		assertNotNull("TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN should have been initialized",TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN());
		assertEquals("TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN should have a uuid of " + taxonomicallyIncludedInUuid.toString(),taxonomicallyIncludedInUuid, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN().getUuid());
	}
	
	@Test
	public void testUnlazyStatusTerms() {
			assertNotNull("NomenclaturalStatusType.SUPERFLUOUS should exist",NomenclaturalStatusType.SUPERFLUOUS());
			assertNotNull("NomenclaturalStatusType.NUDUM should exist",NomenclaturalStatusType.NUDUM());
			assertNotNull("NomenclaturalStatusType.ILLEGITIMATE should exist",NomenclaturalStatusType.ILLEGITIMATE());
			assertNotNull("NomenclaturalStatusType.INVALID should exist",NomenclaturalStatusType.INVALID());
			assertNotNull("NomenclaturalStatusType.CONSERVED should exist",NomenclaturalStatusType.CONSERVED());
			assertNotNull("NomenclaturalStatusType.ALTERNATIVE should exist",NomenclaturalStatusType.ALTERNATIVE());
			assertNotNull("NomenclaturalStatusType.REJECTED should exist",NomenclaturalStatusType.REJECTED());
			assertNotNull("NomenclaturalStatusType.REJECTED_PROP should exist",NomenclaturalStatusType.REJECTED_PROP());
			assertNotNull("NomenclaturalStatusType.PROVISIONAL should exist",NomenclaturalStatusType.PROVISIONAL());
			assertNotNull("NomenclaturalStatusType.SUBNUDUM should exist",NomenclaturalStatusType.SUBNUDUM());
			assertNotNull("NomenclaturalStatusType.OPUS_UTIQUE_OPPR should exist",NomenclaturalStatusType.OPUS_UTIQUE_OPPR());
			assertNotNull("NomenclaturalStatusType.VALID should exist",NomenclaturalStatusType.VALID());
	}
}
