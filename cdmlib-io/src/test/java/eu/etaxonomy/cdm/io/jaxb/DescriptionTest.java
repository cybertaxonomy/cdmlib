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

import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class DescriptionTest {
		
	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/DescriptionTest.xml";
	    
	    @Test
	    public void testUnmarshalDescription() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
			
			Taxon taxon = (Taxon)dataSet.getTaxonBases().get(0);	
			assertNotNull("Taxon must not be null",taxon);
			assertNotNull("Taxon.descriptions must not be null",taxon.getDescriptions());
			assertFalse("Taxon.descriptions must not be empty",taxon.getDescriptions().isEmpty());
			assertEquals("Taxon.descriptions must contain one description",1,taxon.getDescriptions().size());
			
			TaxonDescription taxonDescription = taxon.getDescriptions().iterator().next();
			
			assertNotNull("TaxonDescription.descriptionElements must not be null",taxonDescription.getElements());
			assertFalse("TaxonDescription.descriptionElements must not be empty",taxonDescription.getElements().isEmpty());
			assertEquals("TaxonDescription.descriptionElements should contain one DescriptionElement",1,taxonDescription.getElements().size());
			
			TextData textData = (TextData)taxonDescription.getElements().iterator().next();
			assertEquals("TaxonDescription should equal TextData.inDescription",taxonDescription,textData.getInDescription());	
	    }
}
