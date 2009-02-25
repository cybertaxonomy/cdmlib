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
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class ReferenceTest {
		
	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/ReferenceTest.xml";
	    
	    @Test
	    public void testUnmarshalReference() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = null;
	        dataSet = cdmDocumentBuilder.unmarshal(dataSet, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
			
			Article article = (Article)dataSet.getReferences().get(0);	
			assertNotNull("Article must not be null",article);
			
			Journal journal = (Journal)dataSet.getReferences().get(1);
			assertNotNull("Journal must not be null", journal);
			assertEquals("Journal must equal Article.inJournal",journal,article.getInJournal());
	    }
}
