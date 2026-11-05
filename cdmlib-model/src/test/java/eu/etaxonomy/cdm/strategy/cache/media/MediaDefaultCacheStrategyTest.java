/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

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
			assertEquals("My best media", media.getTitleCache(), "Wrong title cache for media");

			media = Media.NewInstance();
			assertTrue(media.getTitleCache().startsWith("- empty"), "Wrong title cache for empty media");

			Person person = Person.NewTitledInstance("Artist");
			media.setArtist(person);
			assertEquals("Artist", media.getTitleCache(), "Wrong title cache for media with artist");

			MediaRepresentation representation = MediaRepresentation.NewInstance(new URI("www.abc.de/myFileName.jpg&mo=file"), null, null, 0, null);
			media.addRepresentation(representation);
			assertEquals("Artist", media.getTitleCache(), "Wrong title cache for media with artist");

			media.setArtist(null);
			assertEquals("myFileName", media.getTitleCache(), "Wrong title cache for media");
			media.removeRepresentation(representation);

			representation = MediaRepresentation.NewInstance(new URI("www.abc.de/"), null, null, 0, null);
			media.addRepresentation(representation);
			assertEquals("www.abc.de/", media.getTitleCache(), "Wrong title cache for media");
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail("URI syntax exception");
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
