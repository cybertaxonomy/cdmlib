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
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
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
		ReferenceFactory refFactory = ReferenceFactory.newInstance();
		
		BotanicalName botName = BotanicalName.NewInstance(Rank.SPECIES());
		botName.setGenusOrUninomial("Genus");
		botName.setSpecificEpithet("species");
		
		ReferenceBase sec = refFactory.newGeneric();
		sec.setTitleCache("My sec");
		Taxon taxon = Taxon.NewInstance(botName, sec);
		TaxonDescription taxonDescription = TaxonDescription.NewInstance(taxon);
		Assert.assertEquals("Wrong title cache for description", "Taxon description for Genus species", taxonDescription.getTitleCache());
		taxonDescription.setImageGallery(true);
		Assert.assertEquals("Wrong title cache for description", "Image galery for Genus species", taxonDescription.getTitleCache());
		taxonDescription = TaxonDescription.NewInstance();
		Assert.assertEquals("Wrong title cache for description", "Taxon description", taxonDescription.getTitleCache());
		taxonDescription.setImageGallery(true);
		Assert.assertEquals("Wrong title cache for description", "Image galery", taxonDescription.getTitleCache());

	}
	
}
