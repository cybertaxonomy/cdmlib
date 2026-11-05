/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 * @since 05.08.2009
 */
public class MediaTest extends EntityTestBase {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private Media media1;
    private Team team1;
    private MediaRepresentation mediaRepresentation1;
    private MediaRepresentation mediaRepresentation2;
    private LanguageString languageString1;
    private final String germanDescription = "media1Desc2";
    private Rights rights1;
    private LanguageString languageString2;
    private static URI uriString1;
    private static URI uriString2;

    {
        try {
            uriString1 = new URI("http://Pathtoimage1/");
            uriString2 = new URI("http://Path_to_image_2/");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error in URI syntax");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        media1 = Media.NewInstance();
        team1 = Team.NewInstance();
        media1.setArtist(team1);
        media1.setMediaCreated(TimePeriod.NewInstance(2002));
        languageString1 = LanguageString.NewInstance("media1Desc", Language.DEFAULT());
        media1.putDescription(languageString1);
        media1.putDescription(Language.GERMAN(), "media1Desc2");
        mediaRepresentation1 = ImageFile.NewInstance(uriString1, 100, 100, 100);
        mediaRepresentation2 = ImageFile.NewInstance(uriString2, 1000, 100, 100);
        mediaRepresentation1.setMimeType("MimeType1");
        mediaRepresentation2.setMimeType("MimeType1");
        media1.addRepresentation(mediaRepresentation1);
        media1.addRepresentation(mediaRepresentation2);
        rights1 = Rights.NewInstance();
        media1.addRights(rights1);

        languageString2 = LanguageString.NewInstance("media1Title", Language.DEFAULT());
        media1.putTitle(languageString2);

    }

//***************** TESTS *********************************/

    @Test
    public void testClone() {
        try {
            Media mediaClone = media1.clone();
            assertSame(team1, mediaClone.getArtist(), "Artist must be the same");
            assertTrue(mediaClone.getAllDescriptions().containsKey(Language.DEFAULT()), "Clone must have a default language description");
            assertEquals(languageString1.getText(), mediaClone.getAllDescriptions().get(Language.DEFAULT()).getText(), "Description1.text must be the equal");
            assertSame(languageString1.getLanguage(), mediaClone.getAllDescriptions().get(Language.DEFAULT()).getLanguage(), "Description1.language must be the same");
            assertTrue(mediaClone.getAllDescriptions().containsKey(Language.GERMAN()), "Clone must have a german description");
            assertEquals(germanDescription, mediaClone.getAllDescriptions().get(Language.GERMAN()).getText(), "German description must equal media1Desc2");

            assertEquals("2002", mediaClone.getMediaCreated().getYear(), "Media created year must be 2002");
            assertEquals(2, mediaClone.getRepresentations().size(), "Number of media representations must be 2");
            assertNotSame(mediaRepresentation1, mediaClone.getRepresentations().iterator().next(), "Only media representation must not be mediaRepresentation1");
            assertEquals(mediaRepresentation1.getMimeType(), mediaClone.getRepresentations().iterator().next().getMimeType(), "Only meda representation must have same MimeType as mediaRepresentation1");

//            #5762
//            assertTrue(mediaClone.getRights().contains(rights1), "Rights must contain rights1");
            assertTrue(mediaClone.getRights().size() == 1, "Rights must contain 1 rights object");
            assertTrue(mediaClone.getRights().iterator().next().equals(rights1), "Rights must not be cloned");

            assertTrue(mediaClone.getAllTitles().containsKey(Language.DEFAULT()), "Clone must have a default language title");
            assertSame(languageString2.getLanguage(), mediaClone.getAllTitles().get(Language.DEFAULT()).getLanguage(), "Title.language must be the same");
            assertEquals(languageString2.getText(), mediaClone.getAllTitles().get(Language.DEFAULT()).getText(), "Title.text must be equal");

        } catch (CloneNotSupportedException e) {
            fail("Media must be cloneable");
        }

    }

    @Test
    public void testAddRepresentation() {
        assertTrue(media1.getRepresentations().contains(mediaRepresentation1), "Representations must contain mediaRepresentation1");
    }

    @Test
    public void testRemoveRepresentation() {
        assertTrue(media1.getRepresentations().contains(mediaRepresentation1), "Representations must contain mediaRepresentation1");
        media1.removeRepresentation(mediaRepresentation1);
        assertFalse(media1.getRepresentations().contains(mediaRepresentation1), "Representations must not contain mediaRepresentation1");
        assertEquals(1, media1.getRepresentations().size(), "Number of representations must be 1");
        media1.removeRepresentation(mediaRepresentation2);
        assertFalse(media1.getRepresentations().contains(mediaRepresentation2), "Representations must not contain mediaRepresentation2");
        assertEquals(0, media1.getRepresentations().size(), "Number of representations must be 0");
    }

    @Test
    public void testAddRights() {
        assertTrue(media1.getRights().contains(rights1), "Rights must contain rights1");
    }

    @Test
    public void testRemoveRights() {
        assertTrue(media1.getRights().contains(rights1), "Rights must contain rights1");
        media1.removeRights(rights1);
        assertFalse(media1.getRights().contains(rights1), "Rights must not contain rights1");
        assertEquals(0, media1.getRights().size(), "Number of rights must be 0");
    }

    @Test
    public void testAddTitle() {
        assertSame(languageString2, media1.getAllTitles().get(Language.DEFAULT()), "Title must be the same");
    }

    @Test
    public void testRemoveTitle() {
        assertSame(languageString2, media1.getAllTitles().get(Language.DEFAULT()), "Title must be the same");
        media1.removeTitle(Language.GERMAN());
        assertEquals(1, media1.getAllTitles().size(), "Number of titles must be 1");
        media1.removeTitle(Language.DEFAULT());
        assertEquals(0, media1.getAllTitles().size(), "Number of titles must be 0");
        assertFalse(languageString2.equals(media1.getAllTitles().get(Language.DEFAULT())), "Title must not contain languageString2");
    }

    @Test
    public void testGetMediaCreated() {
        assertEquals("2002", media1.getMediaCreated().getYear(), "Media created year must be 2002");
    }

    @Test
    public void testGetDescription() {
        assertTrue(media1.getAllDescriptions().containsKey(Language.DEFAULT()), "Clone must have a default language description");
        assertSame(languageString1, media1.getAllDescriptions().get(Language.DEFAULT()), "Description1 must be the same");
    }

    @Test
    public void testAddDescriptionLanguageString() {
        assertEquals(2, media1.getAllDescriptions().size(), "Number of descriptions must be 2");
        assertTrue(media1.getAllDescriptions().containsKey(Language.DEFAULT()), "Clone must have a default language description");
        assertSame(languageString1, media1.getAllDescriptions().get(Language.DEFAULT()), "Description1 must be the same");
        media1.putDescription(languageString2);
        assertEquals(2, media1.getAllDescriptions().size(), "Number of descriptions must be 2");
        assertEquals(languageString2, media1.getAllDescriptions().get(Language.DEFAULT()), "Default language description must be languageString2");
    }

    @Test
    public void testAddDescriptionStringLanguage() {
        assertTrue(media1.getAllDescriptions().containsKey(Language.GERMAN()), "Clone must have a german language description");
        assertSame("media1Desc2", media1.getAllDescriptions().get(Language.GERMAN()).getText(), "Description1 must be the same");
        media1.putDescription(Language.DEFAULT(), "testDesc");
        assertEquals(2, media1.getAllDescriptions().size(), "Number of descriptions must be 2");
        media1.putDescription(Language.DEFAULT(), "testDesc2");
        assertEquals(2, media1.getAllDescriptions().size(), "Number of descriptions must be 2");
        assertSame("testDesc2", media1.getAllDescriptions().get(Language.DEFAULT()).getText(), "Default language description must be 'testDesc2'");
    }

    @Test
    public void testRemoveDescription() {
        assertEquals(2, media1.getAllDescriptions().size(), "Number of descriptions must be 2");
        assertTrue(media1.getAllDescriptions().containsKey(Language.DEFAULT()), "Clone must have a default language description");
        assertSame(languageString1, media1.getAllDescriptions().get(Language.DEFAULT()), "Description1 must be the same");
        media1.removeDescription(Language.JAPANESE());
        assertEquals(2, media1.getAllDescriptions().size(), "Number of descriptions must be 2");
        media1.removeDescription(Language.DEFAULT());
        assertEquals(1, media1.getAllDescriptions().size(), "Number of descriptions must be 1");
        media1.removeDescription(Language.DEFAULT());
        assertEquals(1, media1.getAllDescriptions().size(), "Number of descriptions must be 1");
        media1.removeDescription(Language.GERMAN());
        assertEquals(0, media1.getAllDescriptions().size(), "Number of descriptions must be 0");
    }
}