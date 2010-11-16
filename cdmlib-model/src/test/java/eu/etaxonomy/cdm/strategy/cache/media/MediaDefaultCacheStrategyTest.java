/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.strategy.cache.media;


import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 *
 */
public class MediaDefaultCacheStrategyTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		new DefaultTermInitializer().initialize();
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	
//*********************** TESTS ****************************************************/
	
	@Test
	public void testGetTitleCache(){
		
		Media media = Media.NewInstance();
		media.addTitle("My best media", Language.DEFAULT());
		Assert.assertEquals("Wrong title cache for media", "My best media", media.getTitleCache());
		
		media = Media.NewInstance();
		Assert.assertTrue("Wrong title cache for media", media.getTitleCache().startsWith("- empty"));
		
		MediaRepresentation representation = MediaRepresentation.NewInstance(null, null, "www.abc.de/myFileName.jpg", 0);
		media.addRepresentation(representation);
		Assert.assertEquals("Wrong title cache for media", "myFileName.jpg", media.getTitleCache());
		media.removeRepresentation(representation);
		
		representation = MediaRepresentation.NewInstance(null, null, "www.abc.de/", 0);
		media.addRepresentation(representation);
		Assert.assertEquals("Wrong title cache for media", "www.abc.de/", media.getTitleCache());
		
		
		
	}
	
}
