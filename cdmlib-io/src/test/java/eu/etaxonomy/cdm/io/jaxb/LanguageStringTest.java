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
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class LanguageStringTest {
		
	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/LanguageStringTest.xml";
	    
	    @Test
	    public void testUnmarshalLanguageString() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = null;
	        dataSet = cdmDocumentBuilder.unmarshal(dataSet, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
			
			Media media = (Media)dataSet.getMedia().get(0);	
			assertNotNull("Media must not be null",media);
			assertNotNull("Media.title must not be null", media.getTitle());
			assertFalse("Media.title must contain LanguageString elements",media.getTitle().isEmpty());
			LanguageString languageString = media.getTitle().values().iterator().next();
			assertNotNull("LanguageString.text must not be null", languageString.getText());
			assertEquals("LanguageString.text must contain the expected value","<i xmlns=\"http://www.w3.org/1999/xhtml\">English</i> Title",languageString.getText());
	    }
}
