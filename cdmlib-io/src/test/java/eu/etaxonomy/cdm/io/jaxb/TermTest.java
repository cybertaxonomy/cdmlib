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
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.net.URI;

import org.junit.Test;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;

public class TermTest {
		
	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/TermTest.xml";
	    
	    @Test
	    public void testUnmarshalTerm() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
			
			NamedArea africa = (NamedArea)dataSet.getTerms().get(2);	
			assertNotNull("DefinedTermBase must not be null",africa);
			assertNotNull("DefinedTermBase.includes must not be null",africa.getIncludes());
			assertFalse("DefinedTermBase.includes must contain NamedArea instances",africa.getIncludes().isEmpty());
			assertEquals("DefinedTermBase.includes must contain eight NamedArea instances",8,africa.getIncludes().size());
			NamedArea northAfrica = (NamedArea)dataSet.getTerms().get(4);
			assertNotNull("DefinedTermBase must not be null",northAfrica);
			assertTrue("Africa must include North Africa", africa.getIncludes().contains(northAfrica));
			assertNotNull("DefinedTermBase.partOf must not be null",northAfrica.getPartOf());
			assertEquals("North Africa must be part of Africa",africa, northAfrica.getPartOf());
			
			Feature discussion = (Feature)dataSet.getTerms().get(13);	
			assertNotNull("DefinedTermBase must not be null",discussion);
			assertNotNull("DefinedTermBase.generalizationOf must not be null",discussion.getGeneralizationOf());
			assertFalse("DefinedTermBase.generalizationOf must contain Feature instances",discussion.getGeneralizationOf().isEmpty());
			assertEquals("DefinedTermBase.generalizationOf must contain two Feature instances",2,discussion.getGeneralizationOf().size());
			Feature taxonomicDiscussion = (Feature)dataSet.getTerms().get(14);
			assertNotNull("DefinedTermBase must not be null",taxonomicDiscussion);
			assertTrue("Discussion must be the generalization of Taxonomic Discussion", discussion.getGeneralizationOf().contains(taxonomicDiscussion));
			assertNotNull("DefinedTermBase.kindOf must not be null",taxonomicDiscussion.getKindOf());
			assertEquals("Taxonomic Discussion must be a kind of Discussion",discussion, taxonomicDiscussion.getKindOf());
	    }
}
