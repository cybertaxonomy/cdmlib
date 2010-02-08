/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.InputStreamReader;
import java.net.URI;

import org.junit.Test;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;

public class MediaTest {
		
	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/MediaTest.xml";
	    
	    @Test
	    public void testUnmarshalMedia() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
			
			Media media = (Media)dataSet.getMedia().get(0);	
			assertNotNull("Media must not be null",media);
			assertNotNull("Media.mediaRepresentations must not be null",media.getRepresentations());
			assertFalse("Media.mediaRepresentations must not be empty",media.getRepresentations().isEmpty());
			
			MediaRepresentation mediaRepresentation = media.getRepresentations().iterator().next();
			assertEquals("Media must equal MediaRepresentation.media",media,mediaRepresentation.getMedia());
			
			assertNotNull("MediaRepresentation.mediaRepresentationParts must not be null",mediaRepresentation.getParts());
			assertFalse("MediaRepresentation.mediaRepresentationParts must not be empty",mediaRepresentation.getParts().isEmpty());
			MediaRepresentationPart mediaRepresentationPart = mediaRepresentation.getParts().get(0);
			assertEquals("MediaRepresentationPart.mediaRepresentation must equal MediaRepresentation",mediaRepresentation,mediaRepresentationPart.getMediaRepresentation());
	    }
}
