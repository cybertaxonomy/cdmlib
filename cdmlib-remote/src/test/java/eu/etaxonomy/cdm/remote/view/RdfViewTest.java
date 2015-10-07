package eu.etaxonomy.cdm.remote.view;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.dozer.Mapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.oxm.Marshaller;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.remote.dto.dc.Relation;
import eu.etaxonomy.cdm.remote.dto.namecatalogue.NameInformation;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.InfoItem;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.PublicationCitation;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.Relationship;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.SpeciesProfileModel;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.StringType;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonConcept;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonName;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonRelationshipTerm;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.Team;
import eu.etaxonomy.remote.dto.rdf.Rdf;

@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-test.xml")
public class RdfViewTest extends UnitilsJUnit4 {

	@SpringBeanByName
	private Marshaller marshaller;

	@SpringBeanByName
	private Marshaller rdfMarshaller;

    @SpringBeanByType
    private Mapper mapper;

	private Rdf rdf;
	private TaxonConcept taxonConcept;
	private SpeciesProfileModel speciesProfileModel;

	@Before
	public void setUp() throws Exception {
		XMLUnit.setControlParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
	    XMLUnit.setTestParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
	    XMLUnit.setSAXParserFactory("org.apache.xerces.jaxp.SAXParserFactoryImpl");
	    XMLUnit.setIgnoreWhitespace(true);

		taxonConcept = new TaxonConcept();

		TaxonName taxonName = new TaxonName();
		taxonName.setAuthorship("authorship");
		taxonName.setNameComplete("Aus aus");
		taxonName.setIdentifier(new URI("urn:lsid:example.org:names:1"));
		taxonConcept.setHasName(taxonName);
		taxonConcept.setIdentifier(new URI("urn:lsid:example.org:taxonconcepts:1"));
		taxonConcept.setTitle("Lorem ipsum");
		taxonConcept.setCreated(new DateTime(2004, 12, 25, 12, 0, 0, 0,DateTimeZone.UTC));


		Relation relation = new Relation();
		relation.setResource(new URI("http://www.example.org/"));
		taxonConcept.setRelation(relation);
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
		t.setTitle("Dolor sic amet");
		relationship.setToTaxon(t);
		//relationship.setFromTaxon(taxonConcept);
		relationships.add(relationship);

		taxonConcept.setHasRelationship(relationships);

		SpeciesProfileModel speciesProfileModel1 = new SpeciesProfileModel();
		speciesProfileModel1.setIdentifier(new URI("urn:lsid:example.org:descriptions:1"));
		speciesProfileModel1.setTitle("Description of Aus aus");

		Set<SpeciesProfileModel> speciesProfileModels = new HashSet<SpeciesProfileModel>();
		speciesProfileModels.add(speciesProfileModel1);
		taxonConcept.setDescribedBy(speciesProfileModels);
		rdf = new Rdf();

		speciesProfileModel = new SpeciesProfileModel();
		InfoItem infoItem = new InfoItem();
		StringType englishDescription = new StringType();
		englishDescription.setLang("en");
		englishDescription.setValue("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras tincidunt pretium quam, id tristique sem iaculis vitae. Vestibulum pharetra eros in ligula rutrum imperdiet. In lorem dui, cursus a suscipit in, pulvinar eget nisl. Phasellus ut nunc eu mauris adipiscing luctus non vel lorem. Suspendisse volutpat faucibus ante, nec bibendum libero consectetur sed. Nullam non posuere neque. Nulla egestas ullamcorper mauris nec tincidunt. Duis id nibh justo. Mauris vel felis et mi eleifend auctor a ac dui. Morbi in urna leo, eu varius lorem. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi mollis nunc eget purus ullamcorper eget pulvinar sapien bibendum. Donec et velit augue, eu pretium mauris. Maecenas lorem leo, malesuada vitae tempor a, gravida quis dolor. Pellentesque lacus velit, sagittis quis posuere ac, rutrum a lacus. Cras dolor ligula, hendrerit at porta sed, posuere euismod mi. Sed sit amet velit turpis.");
		Map<Object,StringType> hasContent = new HashMap<Object,StringType>();
		hasContent.put(englishDescription.getValue(),englishDescription);
		infoItem.setHasContent(hasContent);
		Set<InfoItem> infoItems = new HashSet<InfoItem>();
		infoItems.add(infoItem);
		speciesProfileModel.setHasInformation(infoItems);
	}

	@Test
	public void testMarshalRdf() throws Exception {
		rdf.addThing(taxonConcept);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8" ));
		marshaller.marshal(rdf, new StreamResult(writer));
		writer.close();

		String resource = "/eu/etaxonomy/cdm/remote/view/RdfViewTest.rdf";
		System.out.println(new String(outputStream.toByteArray()));
		XMLAssert.assertXMLEqual(new InputStreamReader(this.getClass().getResourceAsStream(resource)),new StringReader(new String(outputStream.toByteArray())));
	}

	@Test
	public void testMarshalSPM() throws Exception {
		rdf.addThing(speciesProfileModel);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8" ));
		marshaller.marshal(rdf, new StreamResult(writer));
		writer.close();

//		String resource = "/eu/etaxonomy/cdm/remote/view/RdfViewTest.rdf";
//		System.out.println(new String(outputStream.toByteArray()));
//		XMLAssert.assertXMLEqual(new InputStreamReader(this.getClass().getResourceAsStream(resource)),new StringReader(new String(outputStream.toByteArray())));
	}

	@Ignore
	@Test
	public void testNameInformationRdf() throws Exception {

		Map model = new HashMap<String,List<NameInformation>>();
		List niList = new ArrayList();
		NameInformation ni = new NameInformation();
		ni.setRequest("64cf8cf8-f56a-4411-8f49-c3dc95ea257a");
		ni.setResponse("Platalea leucorodia Linnaeus, 1758",
				"Platalea leucorodia",
				"Species",
				new HashSet(),
				null,
				new HashSet(),
				new HashSet(),
				new HashSet());
		ni.getResponse().addToTaxonUuids("1a5bcb42-146f-42e5-9136-1b21d170163e");
		niList.add(ni);
		model.put("64cf8cf8-f56a-4411-8f49-c3dc95ea257a", niList);
		RdfView rdfView = new RdfView();
		rdfView.setMapper(mapper);
		rdfView.setRdfMarshaller(marshaller);
		rdf = rdfView.buildRdf(model);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8" ));
		rdfMarshaller.marshal(rdf, new StreamResult(writer));
		writer.close();
		System.out.println(new String(outputStream.toByteArray()));

		String resource = "/eu/etaxonomy/cdm/remote/view/NameInformationTest.rdf";
		XMLAssert.assertXMLEqual(new InputStreamReader(this.getClass().getResourceAsStream(resource)),new StringReader(new String(outputStream.toByteArray())));
	}



}
