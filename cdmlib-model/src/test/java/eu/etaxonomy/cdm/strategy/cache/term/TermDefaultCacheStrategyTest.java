/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.term;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 19.05.2010
 */
public class TermDefaultCacheStrategyTest extends TermTestBase {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private TermDefaultCacheStrategy<?> strategy;

//************************* SET UP ****************************************

	@BeforeEach
	public void setUp() throws Exception {
		strategy = TermDefaultCacheStrategy.NewInstance(null);
	}

//******************************* TEST **********************************************

	@Test
	public void testGetUuid() {
		String uuid = "9cdf52c1-bac4-4b6c-a7f9-1a87401bd8f9";
		assertEquals(uuid, strategy.getUuid().toString(), "UUID should be equal");
	}

	@Test
	public void testGetTitleCache() {
		assertEquals("Genus", Rank.GENUS().getTitleCache(), "Genus title cache should be 'Genus'");
		assertEquals("Illegitimate", NomenclaturalStatusType.ILLEGITIMATE().getTitleCache(), "Nom. Illeg. title cache should be 'Illegitimate'");
	}

	@Test
	public void testGetTitleCacheNoRepresentations() {
		final String newTermUuid = "e3a6e29d-314a-4e06-be70-cbfe093842ec";
		NamedArea newTerm = NamedArea.NewInstance();
		newTerm.setUuid(UUID.fromString(newTermUuid));

		assertEquals("NamedArea<e3a6e29d-314a-4e06-be70-cbfe093842ec>", newTerm.getTitleCache(), "Term with no representation must return a title that makes some sense");
	}
}
