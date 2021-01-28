/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.term;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
	private static final Logger logger = Logger.getLogger(TermDefaultCacheStrategyTest.class);

	private TermDefaultCacheStrategy<?> strategy;

//************************* SET UP ****************************************

	@Before
	public void setUp() throws Exception {
		strategy = new TermDefaultCacheStrategy<>();
	}

//******************************* TEST **********************************************

	@Test
	public void testGetUuid() {
		String uuid = "9cdf52c1-bac4-4b6c-a7f9-1a87401bd8f9";
		Assert.assertEquals("UUID should be equal", uuid, strategy.getUuid().toString());
	}

	@Test
	public void testGetTitleCache() {
		Assert.assertEquals("Genus title cache should be 'Genus'", "Genus", Rank.GENUS().getTitleCache());
		Assert.assertEquals("Nom. Illeg. title cache should be 'Illegitimate'", "Illegitimate", NomenclaturalStatusType.ILLEGITIMATE().getTitleCache());
	}

	@Test
	public void testGetTitleCacheNoRepresentations() {
		final String newTermUuid = "e3a6e29d-314a-4e06-be70-cbfe093842ec";
		NamedArea newTerm = NamedArea.NewInstance();
		newTerm.setUuid(UUID.fromString(newTermUuid));

		Assert.assertEquals("Term with no representation must return a title that makes some sense", "NamedArea<e3a6e29d-314a-4e06-be70-cbfe093842ec>", newTerm.getTitleCache());
	}
}