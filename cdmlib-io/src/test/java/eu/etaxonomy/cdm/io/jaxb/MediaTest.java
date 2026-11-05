/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;

public class MediaTest {

    private String resource = "/eu/etaxonomy/cdm/io/jaxb/MediaTest.xml";

    @Test
    public void testUnmarshalMedia() throws Exception {
        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());

		Media media = dataSet.getMedia().get(0);
		assertNotNull(media, "Media must not be null");
		assertNotNull(media.getRepresentations(), "Media.mediaRepresentations must not be null");
		assertFalse(media.getRepresentations().isEmpty(), "Media.mediaRepresentations must not be empty");

		MediaRepresentation mediaRepresentation = media.getRepresentations().iterator().next();
		assertEquals(media, mediaRepresentation.getMedia(), "Media must equal MediaRepresentation.media");
		assertNotNull(mediaRepresentation.getUri(), "MediaRepresentation.uri must not be null");
		assertEquals(ImageFile.class, mediaRepresentation.getClass(), "Representation must be ImageFile");
		assertEquals(Integer.valueOf(1200), ((ImageFile) mediaRepresentation).getHeight());
		assertEquals(Integer.valueOf(1600), ((ImageFile) mediaRepresentation).getWidth());
    }
}