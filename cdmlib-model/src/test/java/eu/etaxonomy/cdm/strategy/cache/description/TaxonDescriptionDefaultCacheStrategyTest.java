/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.description;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 *
 */
public class TaxonDescriptionDefaultCacheStrategyTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}


//*********************** TESTS ****************************************************/

	@Test
	public void testGetTitleCache(){

		BotanicalName botName = BotanicalName.NewInstance(Rank.SPECIES());
		botName.setGenusOrUninomial("Genus");
		botName.setSpecificEpithet("species");

		Reference<?> sec = ReferenceFactory.newGeneric();
		sec.setTitleCache("My sec", true);
		Taxon taxon = Taxon.NewInstance(botName, sec);
		TaxonDescription taxonDescription = TaxonDescription.NewInstance(taxon);
		Assert.assertEquals("Wrong title cache for description", "Factual data for Genus species", taxonDescription.getTitleCache());
		taxonDescription.setImageGallery(true);
		Assert.assertEquals("Wrong title cache for description", "Image gallery for Genus species", taxonDescription.getTitleCache());
		taxonDescription = TaxonDescription.NewInstance();
		Assert.assertEquals("Wrong title cache for description", "Factual data", taxonDescription.getTitleCache());
		taxonDescription.setImageGallery(true);
		Assert.assertEquals("Wrong title cache for description", "Image gallery", taxonDescription.getTitleCache());

	}

}
