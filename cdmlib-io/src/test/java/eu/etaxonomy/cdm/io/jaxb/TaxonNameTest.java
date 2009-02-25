package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.net.URI;

import org.junit.Test;

import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;

public class TaxonNameTest {
		
	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/TaxonNameTest.xml";
	    
	    @Test
	    public void testUnmarshalName() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = null;
	        dataSet = cdmDocumentBuilder.unmarshal(dataSet, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
			
			BotanicalName botanicalName = (BotanicalName)dataSet.getTaxonomicNames().get(0);	
			assertNotNull("BotanicalName must not be null",botanicalName);
			Specimen specimen = (Specimen)dataSet.getOccurrences().get(0);
			
			assertNotNull("BotanicalName.typeDesignations must not be null",botanicalName.getTypeDesignations());
			assertFalse("BotanicalName.typeDesignations must contain TypeDesignation entities", botanicalName.getTypeDesignations().isEmpty());
			
			SpecimenTypeDesignation specimenTypeDesignation = (SpecimenTypeDesignation)botanicalName.getTypeDesignations().iterator().next();
			assertNotNull("SpecimenTypeDesignation.typifiedNames must not be null",specimenTypeDesignation.getTypifiedNames());
			assertFalse("SpecimenTypeDesignation.typifiedNames must not be empty",specimenTypeDesignation.getTypifiedNames().isEmpty());
			assertTrue("SpecimenTypeDesignation.typifiedNames must contain BotanicalName",specimenTypeDesignation.getTypifiedNames().contains(botanicalName));
			
			assertNotNull("SpecimenTypeDesignation.typeSpecimen must not be null",specimenTypeDesignation.getTypeSpecimen());
			assertEquals("SpecimenTypeDesignation.typeSpecimen must equal Specimen",specimen,specimenTypeDesignation.getTypeSpecimen());
			
			ZoologicalName zoologicalName = (ZoologicalName)dataSet.getTaxonomicNames().get(5);	
			assertNotNull("ZoologicalName must not be null",zoologicalName);
			
			NameTypeDesignation nameTypeDesignation = (NameTypeDesignation)zoologicalName.getTypeDesignations().iterator().next();
			assertNotNull("NameTypeDesignation.typeName must not be null",nameTypeDesignation.getTypeName());
			assertEquals("NameTypeDesignation.typeName must equal ZoologicalName",dataSet.getTaxonomicNames().get(6),nameTypeDesignation.getTypeName());
			
	    }
}
