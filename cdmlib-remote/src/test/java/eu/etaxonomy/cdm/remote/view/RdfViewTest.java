package eu.etaxonomy.cdm.remote.view;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.hibernate.envers.event.AuditEventListener;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.springframework.oxm.Marshaller;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.remote.dto.dc.Relation;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.PublicationCitation;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.Relationship;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonConcept;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonName;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonRelationshipTerm;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.Team;
import eu.etaxonomy.remote.dto.rdf.Rdf;

@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-test.xml")
public class RdfViewTest extends UnitilsJUnit4 {
	
	@SpringBeanByType
	private Marshaller marshaller;
	
	private Rdf rdf;
	
	@Before
	public void setUp() throws Exception {
		XMLUnit.setControlParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
	    XMLUnit.setTestParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
	    XMLUnit.setSAXParserFactory("org.apache.xerces.jaxp.SAXParserFactoryImpl");
	    XMLUnit.setIgnoreWhitespace(true);
	    
		TaxonConcept taxonConcept = new TaxonConcept();

		TaxonName taxonName = new TaxonName();
		taxonName.setAuthorship("authorship");
		taxonName.setNameComplete("Aus aus");
		taxonName.setIdentifier(new URI("urn:lsid:example.org:names:1"));
		taxonConcept.setHasName(taxonName);
		taxonConcept.setIdentifier(new URI("urn:lsid:example.org:taxonconcepts:1"));
		taxonConcept.setTitle("Lorem ipsum");
		taxonConcept.setCreated(new DateTime(2004, 12, 25, 12, 0, 0, 0, DateTimeZone.UTC));
		
		Set<Relation> relations = new HashSet<Relation>();
		Relation relation = new Relation();
		relation.setResource(new URI("http://www.example.org/"));
		relations.add(relation);
		taxonConcept.setRelations(relations);
		PublicationCitation publicationCitation = new PublicationCitation();
		taxonConcept.setPublishedIn("Lorem ipsum dolor");
		
		Team team = new Team();
		team.setTitle("team name");
		taxonConcept.setAccordingTo(team);
		
		TaxonRelationshipTerm taxonRelationshipTerm = new TaxonRelationshipTerm();
		taxonRelationshipTerm.setIdentifier(new URI("http://rs.e-taxonomy.eu/voc/TaxonRelationshipTerm.rdf"));
		Set<Relationship> relationships = new HashSet<Relationship>();	
		Relationship relationship = new Relationship();
		relationship.setRelationshipCategory(taxonRelationshipTerm);
		TaxonConcept t = new TaxonConcept();
		t.setIdentifier(new URI("urn:lsid:example.org:taxonconcepts:2"));
		relationship.setToTaxon(t);
		relationship.setFromTaxon(taxonConcept);
		relationships.add(relationship);
		
		taxonConcept.setHasRelationship(relationships);
		
		rdf = new Rdf();
		rdf.addThing(taxonConcept);
	}
	
	@Test
	public void testMarshalRdf() throws Exception {	
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8" ));
		marshaller.marshal(rdf, new StreamResult(writer));
		writer.close();
		
		String resource = "/eu/etaxonomy/cdm/remote/view/RdfViewTest.rdf";	
		XMLAssert.assertXMLEqual(new InputStreamReader(this.getClass().getResourceAsStream(resource)),new StringReader(new String(outputStream.toByteArray())));
	}

}
