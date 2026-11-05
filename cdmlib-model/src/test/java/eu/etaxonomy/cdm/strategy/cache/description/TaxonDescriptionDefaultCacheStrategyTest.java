/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.description;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 */
public class TaxonDescriptionDefaultCacheStrategyTest {

	@BeforeEach
	public void setUp() throws Exception {
	}


//*********************** TESTS ****************************************************/

	@Test
	public void testGetTitleCache(){

		IBotanicalName botName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		botName.setGenusOrUninomial("Genus");
		botName.setSpecificEpithet("species");

		Reference sec = ReferenceFactory.newGeneric();
		sec.setTitleCache("My sec", true);
		Taxon taxon = Taxon.NewInstance(botName, sec);
		TaxonDescription taxonDescription = TaxonDescription.NewInstance(taxon);
		assertEquals("Factual data set for Genus species", taxonDescription.generateTitle(), "Wrong title cache for description");
		taxonDescription.setImageGallery(true);
		assertEquals("Image gallery for Genus species", taxonDescription.generateTitle(), "Wrong title cache for description");
		taxonDescription = TaxonDescription.NewInstance();
		assertEquals("Factual data set", taxonDescription.generateTitle(), "Wrong title cache for description");
		taxonDescription.setImageGallery(true);
		assertEquals("Image gallery", taxonDescription.generateTitle(), "Wrong title cache for description");
	}
}