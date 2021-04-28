/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.media;

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 */
public class MediaDefaultCacheStrategyTest extends TermTestBase {

//*********************** TESTS ****************************************************/

	@Test
	public void testGetTitleCache(){

		try {
			Media media = Media.NewInstance();
			media.putTitle(Language.DEFAULT(), "My best media");
			Assert.assertEquals("Wrong title cache for media", "My best media", media.getTitleCache());

			media = Media.NewInstance();
			Assert.assertTrue("Wrong title cache for empty media", media.getTitleCache().startsWith("- empty"));

			Person person = Person.NewTitledInstance("Artist");
			media.setArtist(person);
			Assert.assertEquals("Wrong title cache for media with artist", "Artist", media.getTitleCache());

			MediaRepresentation representation = MediaRepresentation.NewInstance(null, null, new URI("www.abc.de/myFileName.jpg"), 0, null);
			media.addRepresentation(representation);
			Assert.assertEquals("Wrong title cache for media with artist", "Artist", media.getTitleCache());

			media.setArtist(null);
			Assert.assertEquals("Wrong title cache for media", "myFileName.jpg", media.getTitleCache());
			media.removeRepresentation(representation);

			representation = MediaRepresentation.NewInstance(null, null, new URI("www.abc.de/"), 0, null);
			media.addRepresentation(representation);
			Assert.assertEquals("Wrong title cache for media", "www.abc.de/", media.getTitleCache());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail("URI syntax exception");
		}
	}

	@Test
	public void testHandleEmptyUri(){
		Media media = Media.NewInstance();
		MediaRepresentation representation;
		representation = MediaRepresentation.NewInstance(null, null, null, 0, null);
		media.addRepresentation(representation);
	}
}
