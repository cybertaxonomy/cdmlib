// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;

/**
 * @author a.mueller
 * @created 05.08.2009
 * @version 1.0
 */
public class MediaTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MediaTest.class);

	private Media media1; 
	private Team team1;
	private MediaRepresentation mediaRepresentation1;
	private MediaRepresentation mediaRepresentation2;
	private MediaRepresentationPart mediaRepresentationPart1;
	private MediaRepresentationPart mediaRepresentationPart2;
	private LanguageString languageString1;
	private final String germanDescription = "media1Desc2";
	private Rights rights1;
	private LanguageString languageString2;
	private String uriString1 = "Path to image 1";
	private String uriString2 = "Path to image 2";
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DefaultTermInitializer termInitializer = new DefaultTermInitializer();
		termInitializer.initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		media1 = Media.NewInstance();
		team1 = Team.NewInstance();
		media1.setArtist(team1);
		media1.setMediaCreated(new DateTime(2002, 1,1,0,0,0,0));
		languageString1 = LanguageString.NewInstance("media1Desc", Language.DEFAULT());
		media1.addDescription(languageString1);
		media1.addDescription("media1Desc2", Language.GERMAN());
		mediaRepresentation1 = MediaRepresentation.NewInstance();
		mediaRepresentation2 = MediaRepresentation.NewInstance();
		mediaRepresentation1.setMimeType("MimeType1");
		mediaRepresentation2.setMimeType("MimeType1");
		mediaRepresentationPart1 = ImageFile.NewInstance(uriString1, 100);
		mediaRepresentationPart2 = ImageFile.NewInstance(uriString2, 1000);
		((ImageFile) mediaRepresentationPart1).setHeight(100);
		((ImageFile) mediaRepresentationPart1).setWidth(100);
		
		((ImageFile) mediaRepresentationPart2).setHeight(100);
		((ImageFile) mediaRepresentationPart2).setWidth(100);
		
		mediaRepresentation1.addRepresentationPart(mediaRepresentationPart1);
		mediaRepresentation2.addRepresentationPart(mediaRepresentationPart2);
		media1.addRepresentation(mediaRepresentation1);
		media1.addRepresentation(mediaRepresentation2);
		rights1 = Rights.NewInstance();
		media1.addRights(rights1);
		
		languageString2 = LanguageString.NewInstance("media1Title", Language.DEFAULT());
		media1.addTitle(languageString2);
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

//***************** TESTS *********************************/	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.media.Media#clone()}.
	 */
	@Test
	public void testClone() {
		try {
			Media mediaClone = (Media)media1.clone();
			Assert.assertSame("Artist must be the same", team1, mediaClone.getArtist());
			Assert.assertTrue("Clone must have a default language description", mediaClone.getDescription().containsKey(Language.DEFAULT()));
			Assert.assertSame("Description1 must be the same", languageString1, mediaClone.getDescription().get(Language.DEFAULT()) );
			Assert.assertTrue("Clone must have a german description", mediaClone.getDescription().containsKey(Language.GERMAN()));
			Assert.assertEquals("German description must equal media1Desc2", germanDescription, mediaClone.getDescription().get(Language.GERMAN()).getText() );
			
			Assert.assertEquals("Media created year must be 2002", 2002, mediaClone.getMediaCreated().getYear());
			Assert.assertEquals("Number of media representations must be 2", 2, mediaClone.getRepresentations().size());
			Assert.assertNotSame("Only media representation must not be mediaRepresentation1", mediaRepresentation1, mediaClone.getRepresentations().iterator().next());
			Assert.assertEquals("Only meda representation must have same MimeType as mediaRepresentation1", mediaRepresentation1.getMimeType(), mediaClone.getRepresentations().iterator().next().getMimeType());
			Assert.assertTrue("Rights must contain rights1", mediaClone.getRights().contains(rights1));
		
			Assert.assertTrue("Clone must have a default language title", mediaClone.getTitle().containsKey(Language.DEFAULT()));
			Assert.assertSame("Title must be the same", languageString2, mediaClone.getTitle().get(Language.DEFAULT()) );
			
		} catch (CloneNotSupportedException e) {
			Assert.fail("Media must be cloneable");
		}
		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.media.Media#addRepresentation(eu.etaxonomy.cdm.model.media.MediaRepresentation)}.
	 */
	@Test
	public void testAddRepresentation() {
		Assert.assertTrue("Representations must contain mediaRepresentation1", media1.getRepresentations().contains(mediaRepresentation1));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.media.Media#removeRepresentation(eu.etaxonomy.cdm.model.media.MediaRepresentation)}.
	 */
	@Test
	public void testRemoveRepresentation() {
		Assert.assertTrue("Representations must contain mediaRepresentation1", media1.getRepresentations().contains(mediaRepresentation1));
		media1.removeRepresentation(mediaRepresentation1);
		Assert.assertFalse("Representations must not contain mediaRepresentation1", media1.getRepresentations().contains(mediaRepresentation1));
		Assert.assertEquals("Number of representations must be 1", 1, media1.getRepresentations().size());
		media1.removeRepresentation(mediaRepresentation2);
		Assert.assertFalse("Representations must not contain mediaRepresentation2", media1.getRepresentations().contains(mediaRepresentation2));
		Assert.assertEquals("Number of representations must be 0", 0, media1.getRepresentations().size());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.media.Media#addRights(eu.etaxonomy.cdm.model.media.Rights)}.
	 */
	@Test
	public void testAddRights() {
		Assert.assertTrue("Rights must contain rights1", media1.getRights().contains(rights1));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.media.Media#removeRights(eu.etaxonomy.cdm.model.media.Rights)}.
	 */
	@Test
	public void testRemoveRights() {
		Assert.assertTrue("Rights must contain rights1", media1.getRights().contains(rights1));
		media1.removeRights(rights1);
		Assert.assertFalse("Rights must not contain rights1", media1.getRights().contains(rights1));
		Assert.assertEquals("Number of rights must be 0", 0, media1.getRights().size());	
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.media.Media#addTitle(eu.etaxonomy.cdm.model.common.LanguageString)}.
	 */
	@Test
	public void testAddTitle() {
		Assert.assertSame("Title must be the same", languageString2, media1.getTitle().get(Language.DEFAULT()) );
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.media.Media#removeTitle(eu.etaxonomy.cdm.model.common.Language)}.
	 */
	@Test
	public void testRemoveTitle() {
		Assert.assertSame("Title must be the same", languageString2, media1.getTitle().get(Language.DEFAULT()) );
		media1.removeTitle(Language.GERMAN());
		Assert.assertEquals("Number of titles must be 1", 1, media1.getTitle().size());
		media1.removeTitle(Language.DEFAULT());
		Assert.assertEquals("Number of titles must be 0", 0, media1.getTitle().size());
		Assert.assertFalse("Title must not contain languageString2", languageString2.equals(media1.getTitle().get(Language.DEFAULT())));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.media.Media#getMediaCreated()}.
	 */
	@Test
	public void testGetMediaCreated() {
		Assert.assertEquals("Media created year must be 2002", 2002, media1.getMediaCreated().getYear());
		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.media.Media#getDescription()}.
	 */
	@Test
	public void testGetDescription() {
		Assert.assertTrue("Clone must have a default language description", media1.getDescription().containsKey(Language.DEFAULT()));
		Assert.assertSame("Description1 must be the same", languageString1, media1.getDescription().get(Language.DEFAULT()) );
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.media.Media#addDescription(eu.etaxonomy.cdm.model.common.LanguageString)}.
	 */
	@Test
	public void testAddDescriptionLanguageString() {
		Assert.assertEquals("Number of descriptions must be 2", 2, media1.getDescription().size() );
		Assert.assertTrue("Clone must have a default language description", media1.getDescription().containsKey(Language.DEFAULT()));
		Assert.assertSame("Description1 must be the same", languageString1, media1.getDescription().get(Language.DEFAULT()) );
		media1.addDescription(languageString2);
		Assert.assertEquals("Number of descriptions must be 2", 2, media1.getDescription().size() );
		Assert.assertEquals("Default language description must be languageString2", languageString2, media1.getDescription().get(Language.DEFAULT()) );
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.media.Media#addDescription(java.lang.String, eu.etaxonomy.cdm.model.common.Language)}.
	 */
	@Test
	public void testAddDescriptionStringLanguage() {
		Assert.assertTrue("Clone must have a german language description", media1.getDescription().containsKey(Language.GERMAN()));
		Assert.assertSame("Description1 must be the same", "media1Desc2", media1.getDescription().get(Language.GERMAN()).getText() );
		media1.addDescription("testDesc", Language.DEFAULT());
		Assert.assertEquals("Number of descriptions must be 2", 2, media1.getDescription().size() );
		media1.addDescription("testDesc2", Language.DEFAULT());
		Assert.assertEquals("Number of descriptions must be 2", 2, media1.getDescription().size() );
		Assert.assertSame("Default language description must be 'testDesc2'", "testDesc2", media1.getDescription().get(Language.DEFAULT()).getText() );
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.media.Media#removeDescription(eu.etaxonomy.cdm.model.common.Language)}.
	 */
	@Test
	public void testRemoveDescription() {
		Assert.assertEquals("Number of descriptions must be 2", 2, media1.getDescription().size() );
		Assert.assertTrue("Clone must have a default language description", media1.getDescription().containsKey(Language.DEFAULT()));
		Assert.assertSame("Description1 must be the same", languageString1, media1.getDescription().get(Language.DEFAULT()) );
		media1.removeDescription(Language.JAPANESE());
		Assert.assertEquals("Number of descriptions must be 2", 2, media1.getDescription().size() );
		media1.removeDescription(Language.DEFAULT());
		Assert.assertEquals("Number of descriptions must be 1", 1, media1.getDescription().size() );
		media1.removeDescription(Language.DEFAULT());
		Assert.assertEquals("Number of descriptions must be 1", 1, media1.getDescription().size() );
		media1.removeDescription(Language.GERMAN());
		Assert.assertEquals("Number of descriptions must be 0", 0, media1.getDescription().size() );
	}
	
	@Test
	public void testfindBestMatchingRepresentation() {
		String[] mimetypes = {".*"};
		Assert.assertEquals(mediaRepresentation1, media1.findBestMatchingRepresentation(100, 100, 100, mimetypes));
		Assert.assertEquals(mediaRepresentation2, media1.findBestMatchingRepresentation(1000, 100, 100, mimetypes));
	
	}
}
