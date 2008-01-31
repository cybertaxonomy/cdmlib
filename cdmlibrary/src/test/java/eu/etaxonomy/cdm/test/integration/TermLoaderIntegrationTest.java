/**
 * 
 */
package eu.etaxonomy.cdm.test.integration;

import static org.junit.Assert.*;

import java.util.UUID;
import org.junit.Test;

import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * @author a.mueller
 *
 */
public class TermLoaderIntegrationTest {


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.ConceptRelationshipType#TAXONOMICALLY_INCLUDED_IN()}.
	 */
	@Test
	public final void testTermsAreLoaded() {
		TaxonRelationshipType rel = TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN();
		assertNotNull(rel);
		UUID expectedUuid = UUID.fromString("d13fecdf-eb44-4dd7-9244-26679c05df1c");
		assertEquals(expectedUuid, rel.getUuid());
	}

}
