/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.sdd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xerces.internal.dom.ElementImpl;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;
import com.sun.org.apache.xml.internal.serialize.DOMSerializer;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import eu.etaxonomy.cdm.io.jaxb.CdmMarshallerListener;

/**
 * Writes the SDD XML file. 
 * 
 * @author h.fradin
 * 10.12.2008
 */

public class SDDDocumentBuilder {
	
	private DocumentImpl document;
	private XMLSerializer xmlserializer;
	private Writer writer;
	private DOMSerializer domi;
	
	private String AGENT = "Agent";
	private String AGENTS = "Agents";
	private String CATEGORICAL = "Categorical";
	private String CATEGORICAL_CHARACTER = "CategoricalCharacter";
	private String CHARACTER = "Character";
	private String CHARACTERS = "Characters";
	private String CHARACTER_TREE = "CharacterTree";
	private String CHARACTER_TREES = "CharacterTrees";
	private String CHAR_NODE = "CharNode";
	private String CODED_DESCRIPTION = "CodedDescription";
	private String CODED_DESCRIPTIONS = "CodedDescriptions";
	private String CREATORS = "Creators";
	private String DATASET = "Dataset";
	private String DATASETS = "Datasets";
	private String DATE_CREATED = "DateCreated";
	private String DATE_MODIFIED = "DateModified";
	private String DEPENDENCY_RULES = "DependencyRules";
	private String DESCRIPTIVE_CONCEPT = "DescriptiveConcept";
	private String DESCRIPTIVE_CONCEPTS = "DescriptiveConcepts";
	private String DETAIL = "Detail";
	private String GENERATOR = "Generator";
	private String ID = "id";
	private String IMAGE = "Image";
	private String INAPPLICABLE_IF = "InapplicableIf";
	private String IPR_STATEMENT = "IPRStatement";
	private String IPR_STATEMENTS = "IPRStatements";
	private String LABEL = "Label";
	private String MEDIA_OBJECT = "MediaObject";
	private String MEDIA_OBJECTS = "MediaObjects";
	private String NODE = "Node";
	private String NODES = "Nodes";
	private String NOTE = "Note";
	private String PARENT = "Parent";
	private String REF = "ref";
	private String REPRESENTATION = "Representation";
	private String REVISION_DATA = "RevisionData";
	private String SHOULD_CONTAIN_ALL_CHARACTERS = "ShouldContainAllCharacters";
	private String SOURCE = "Source";
	private String STATE = "State";
	private String STATE_DEFINITION = "StateDefinition";
	private String STATES = "States";
	private String STATUS = "Status";
	private String SUMMARY_DATA = "SummaryData";
	private String TECHNICAL_METADATA = "TechnicalMetadata";
	private String TEXT = "Text";
	private String TYPE = "Type";
	
	private static final Logger logger = Logger.getLogger(SDDDocumentBuilder.class);
	
	// private SDDContext sddContext;
		                                        
	public SDDDocumentBuilder() throws SAXException, IOException {
		
		document = new DocumentImpl();
					
		// sddContext = SDDContext.newInstance(new Class[] {SDDDataSet.class});
		// logger.debug(sddContext.toString());

	}
	
	public void marshal(SDDDataSet dataSet, File sddDestination) throws IOException {
		
		Marshaller marshaller;
		// marshaller = sddContext.createMarshaller();
		
		// For test purposes insert newlines to make the XML output readable
		// marshaller.setProperty(Marshaller.SDD_FORMATTED_OUTPUT, Boolean.TRUE);
		
		// marshaller.setProperty(Marshaller.SDD_ENCODING, "UTF-8");
		
		CdmMarshallerListener marshallerListener = new CdmMarshallerListener();
		// marshaller.setListener(marshallerListener);
		
		// validate with explicit schema
		//marshaller.setSchema(cdmSchema);
		
		// marshaller.setEventHandler(new DefaultValidationEventHandler());

		logger.info("Start marshalling");
		// marshaller.marshal(dataSet, writer);
		
		writeCDMtoSDD(dataSet, sddDestination);
		
	}

	public SDDDataSet unmarshal(SDDDataSet dataSet, File file) {
		
		Unmarshaller unmarshaller;
		// unmarshaller = sddContext.createUnmarshaller();
		
		// DefaultValidationEventHandler implementation is part of the API and convenient for trouble-shooting.
		// It prints errors to System.out.
		//unmarshaller.setEventHandler(new DefaultValidationEventHandler());

		logger.info("Start unmarshalling");
		// dataSet = (SDDDataSet) unmarshaller.unmarshal(file);
		return dataSet;
		
	}

	/**Write the DOM document.
	 * @param base
	 * @throws IOException
	 */
	public void writeCDMtoSDD(SDDDataSet cdmSource, File sddDestination) throws IOException {
		
		try {
			buildDocument();
		} catch (ParseException e) {
			System.out.println("Problem with SDD export located in the buildDocument() method ...");
			e.printStackTrace();
		}
	
		OutputFormat format = new OutputFormat(document, "UTF-8", true);
	
		FileOutputStream fos = new FileOutputStream(sddDestination);
		
		writer = new OutputStreamWriter(fos, "UTF-8");
		
		xmlserializer = new XMLSerializer(writer, format);
		domi = xmlserializer.asDOMSerializer(); // As a DOM Serializer
	
		domi.serialize(document.getDocumentElement());
	
		writer.close();
	}

	//	#############
	//	# BUILD DOM	#
	//	#############	
	
		/**
		 * Builds the whole document.
		 * @param base the Base
		 * @throws ParseException 
		 */
		public void buildDocument() throws ParseException {
	
			//create <Datasets> = root node
			ElementImpl baselement = new ElementImpl(document, DATASETS);
	
			baselement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			baselement.setAttribute("xmlns", "http://rs.tdwg.org/UBIF/2006/");
			baselement.setAttribute("xsi:schemaLocation", "http://rs.tdwg.org/UBIF/2006 http://rs.tdwg.org/UBIF/2006/Schema/1.1/SDD.xsd");
			baselement.setAttribute("xmlns:xper", "http://lis.snv.jussieu.fr/xper/XML");
	
	//		root.setAttributeNS("xmlns", "xsi", "http://www.w3.org/2001/XMLSchema-instance");
	//		root.setAttributeNS("xmlns", "schemaLocation", "http://lis.snv.jussieu.fr/xper/XML XPer2_Generated.xsd");
	
			//base content part
			// buildBase(baselement);
	
			//append the root element to the DOM document
			document.appendChild(baselement);
		}

//  can only be used with JAXB 2.1
//	public void writeFile(DataSet dataSet, File file) throws JAXBException {
//		marshaller.marshal(dataSet, file);
//	}

}

 
