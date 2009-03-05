package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.Assert.assertNotNull;

import java.io.InputStreamReader;
import java.net.URI;

import org.junit.Test;

import eu.etaxonomy.cdm.model.location.NamedArea;

public class NamedAreaTest {
		
	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/NamedAreaTest.xml";
	    
	    @Test
	    public void testUnmarshalNamedArea() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
			
			NamedArea namedArea = (NamedArea)dataSet.getTerms().get(1);	
			assertNotNull("NamedArea must not be null",namedArea);
	    }
}
