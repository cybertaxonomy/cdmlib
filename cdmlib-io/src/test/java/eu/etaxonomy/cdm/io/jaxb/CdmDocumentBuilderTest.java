package eu.etaxonomy.cdm.io.jaxb;

import java.io.InputStreamReader;
import java.net.URI;

import junit.framework.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;

public class CdmDocumentBuilderTest {
		
	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/CdmDocumentBuilderTest.xml";
	    
	    @Test
	    public void testXInclude() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
			
			Person person = (Person)dataSet.getAgents().get(0);
			Assert.assertNotNull(person);
	    }
}
