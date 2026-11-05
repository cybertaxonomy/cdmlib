/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.media.Media;

public class LanguageStringTest {

    private String resource = "/eu/etaxonomy/cdm/io/jaxb/LanguageStringTest.xml";

    @Test
    public void testUnmarshalLanguageString() throws Exception {
        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());

		Media media = dataSet.getMedia().get(0);
		assertNotNull(media, "Media must not be null");
		/* TODO: Does not work because the term loading does not work in test cases...
		assertNotNull(media.getTitle(Language.ENGLISH()), "Media.title must not be null");
		assertFalse(media.getAllTitles().isEmpty(), "Media.title must contain LanguageString elements");
		LanguageString languageString = media.getAllTitles().values().iterator().next();
		assertNotNull(languageString.getText(), "LanguageString.text must not be null");
		assertEquals("<i xmlns=\"http://www.w3.org/1999/xhtml\">English</i> Title", languageString.getText(), "LanguageString.text must contain the expected value");
   		*/
    }
}
