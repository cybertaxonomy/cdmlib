package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.InputStreamReader;
import java.net.URI;

import org.joda.time.DateTimeFieldType;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class DescriptionTest {
		
	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/DescriptionTest.xml";
	    
	    @Test
	    public void testUnmarshalDescription() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = null;
	        dataSet = cdmDocumentBuilder.unmarshal(dataSet, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
			
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
