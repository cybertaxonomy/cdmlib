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

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.media.Media;

public class LanguageStringTest {
		
	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/LanguageStringTest.xml";
	    
	    @Test
	    public void testUnmarshalLanguageString() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
			
			Media media = (Media)dataSet.getMedia().get(0);	
			assertNotNull("Media must not be null",media);
			/* TODO: Does not work because the term loading does not work in test cases...
			assertNotNull("Media.title must not be null", media.getTitle(Language.ENGLISH()));
			assertFalse("Media.title must contain LanguageString elements",media.getAllTitles().isEmpty());
			LanguageString languageString = media.getAllTitles().values().iterator().next();
			assertNotNull("LanguageString.text must not be null", languageString.getText());
			assertEquals("LanguageString.text must contain the expected value","<i xmlns=\"http://www.w3.org/1999/xhtml\">English</i> Title",languageString.getText());
	   		*/
	    }
}
