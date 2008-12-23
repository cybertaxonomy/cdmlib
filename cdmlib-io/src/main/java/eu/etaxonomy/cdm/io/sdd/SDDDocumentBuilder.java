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
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xerces.internal.dom.ElementImpl;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;
import com.sun.org.apache.xml.internal.serialize.DOMSerializer;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import eu.etaxonomy.cdm.io.jaxb.CdmMarshallerListener;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

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
	private SDDDataSet cdmSource;

	private Map<Person,String> agents = new HashMap<Person,String>();
	private int agentsCount = 0;

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
	private String ROLE = "role";
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

	public void marshal(SDDDataSet cdmSource, File sddDestination) throws IOException {

		this.cdmSource = cdmSource;
		Marshaller marshaller;		
		CdmMarshallerListener marshallerListener = new CdmMarshallerListener();
		logger.info("Start marshalling");
		writeCDMtoSDD(sddDestination);

	}

	/**Write the DOM document.
	 * @param base
	 * @throws IOException
	 */
	public void writeCDMtoSDD(File sddDestination) throws IOException {

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

		buildTechnicalMetadata(baselement);

		List<ReferenceBase> references = cdmSource.getReferences();
		Iterator<ReferenceBase> iterator = references.iterator();
		Database d = Database.NewInstance();
		while (iterator.hasNext()) {
			ReferenceBase reference = (ReferenceBase) iterator.next();
			if (reference instanceof Database) {
				buildDataset(baselement, (Database) reference);
			}
		}

		// for datasets with no Database ReferenceBase
		// buildDataset(baselement, cdmSource, null);

		//append the root element to the DOM document
		document.appendChild(baselement);
	}

	//	#############
	//	# BUILD DOM	#
	//	#############	

	/**
	 * Builds TechnicalMetadata associated with the SDD file
	 */
	public void buildTechnicalMetadata(ElementImpl baselement) throws ParseException {
		//create TechnicalMetadata
		ElementImpl technicalMetadata = new ElementImpl(document, TECHNICAL_METADATA);
		//select different databases associated to different descriptions TODO
		List<ReferenceBase> references = cdmSource.getReferences();
		Iterator<ReferenceBase> iterator = references.iterator();
		boolean database = false;
		Database d = Database.NewInstance();
		while ((iterator.hasNext()) && (!database)) {
			ReferenceBase reference = (ReferenceBase) iterator.next();
			if (reference instanceof Database) {
				d = (Database) reference;
			}
		}
		DateTime dt = d.getCreated();
		String date = dt.toString().substring(0, 19);
		technicalMetadata.setAttribute("created", date);

		ElementImpl generator = new ElementImpl(document, GENERATOR);
		generator.setAttribute("name", "EDIT CDM");
		generator.setAttribute("version", "v1");
		generator.setAttribute("notes","This SDD file has been generated by the SDD export functionality of the EDIT platform for Cybertaxonomy - Copyright (c) 2008");
		technicalMetadata.appendChild(generator);

		baselement.appendChild(technicalMetadata);
	}

	// Builds the information associated with a dataset
	public void buildDataset(ElementImpl baselement, Database reference) throws ParseException {
		// create Dataset and language
		ElementImpl dataset = new ElementImpl(document, DATASET);
		// no default language associated with a dataset in the CDM
		// dataset.setAttribute("xml:lang", datasetLanguage);
		baselement.appendChild(dataset);
		buildRepresentation(dataset, reference);
		buildRevisionData(dataset, reference);

	}

	/**
	 * Builds a Representation element
	 */
	public void buildRepresentation(ElementImpl element, ReferenceBase reference) throws ParseException {

		//			create <Representation> element
		ElementImpl representation = new ElementImpl(document, REPRESENTATION);
		element.appendChild(representation);
		buildLabel(representation, reference.getTitleCache());

		Set<Annotation> annotations = reference.getAnnotations();
		Iterator iterator = annotations.iterator();
		String detailText = null;
		if (iterator.hasNext()) {
			Annotation annotation = (Annotation) iterator.next();
			detailText = annotation.getText();
		}

		if (detailText != null && !detailText.equals("")) {
			ElementImpl detail = new ElementImpl(document, DETAIL);
			detail.appendChild(document.createTextNode(detailText));
			representation.appendChild(detail);
		}

		Set<Media> medias = reference.getMedia();

		if (medias != null && medias.size() > 0) {
			ElementImpl mediaObject;

			for (int i = 0; i < medias.size(); i++) {
				mediaObject = new ElementImpl(document, MEDIA_OBJECT);
				mediaObject.setAttribute(REF, "m1");
				/* Object key = mediaObjects[i];

					if (referencesMediaObjects.containsKey((BaseObjectResource)key)) {
						mediaObject.setAttribute(REF, (String)referencesMediaObjects.get(key));
						if (((BaseObjectResource)key).getFocus() == 1)
							mediaObject.setAttribute("role", "Primary");
						else
							mediaObject.setAttribute("role", "Secondary");
					}
					else 
						if (referencesLinks.containsKey((String)key)) {
							mediaObject.setAttribute(REF, (String)key);
							mediaObject.setAttribute("role", "Secondary");
						}
				 */
				representation.appendChild(mediaObject);
			}
		}

	}

	//	################
	//	# GENERIC BRICKS       #
	//	################

	/**
	 * Creates a Label element 
	 * @param base
	 * @param element
	 */
	public void buildLabel(ElementImpl element, String text) {
		//		create <Label> element
		ElementImpl label = new ElementImpl(document, LABEL);

		label.appendChild(document.createTextNode(text));
		element.appendChild(label);
	}


	/**
	 * Builds RevisionData associated with the Dataset
	 */
	public void buildRevisionData(ElementImpl dataset, Database database) throws ParseException {

		// <RevisionData>
		//  <Creators>
		//    <Agent role="aut" ref="a1"/>
		//    <Agent role="aut" ref="a2"/>
		//    <Agent role="edt" ref="a3"/>
		//  </Creators>
		//  <DateModified>2006-04-08T00:00:00</DateModified>
		// </RevisionData>

		ElementImpl revisionData = new ElementImpl(document, REVISION_DATA);

		// authors
		TeamOrPersonBase authors = database.getAuthorTeam();
		TeamOrPersonBase editors = database.getUpdatedBy();

		if ((authors != null) || (editors != null)) {
			ElementImpl creators = new ElementImpl(document, CREATORS);
			if (authors != null) {
				buildRefAgent(creators, authors, "aut");
			}
			if (editors != null) {
				buildRefAgent(creators, editors, "edt");
			}
			revisionData.appendChild(creators);
		}

		dataset.appendChild(revisionData);
	}

	/**
	 * Builds an element Agent referring to Agent defined later in the SDD file
	 */
	public void buildRefAgent(ElementImpl element, TeamOrPersonBase ag, String role) throws ParseException {
		if (ag instanceof Person) {
			Person p = (Person) ag;
			ElementImpl agent = new ElementImpl(document, AGENT);
			agent.setAttribute(ROLE, role);
			if (p.getSources() != null) {
				OriginalSource os = (OriginalSource) p.getSources().toArray()[0];
				String id = os.getIdInSource();
				if (id != null) {
					if (!id.equals("")) {
						if (!agents.containsValue(id)) {
							agent.setAttribute(REF, id);
						} else if (!agents.containsValue("a" + (agentsCount+1))) {
							agent.setAttribute(REF, "a" + (agentsCount+1));
							agentsCount++;
						} else {
							agent.setAttribute(REF, id + (agentsCount+1));
							agentsCount++;
						}
					} else {
						agent.setAttribute(REF, "a" + (agentsCount+1));
						agentsCount++;
					}
				} else {
					agent.setAttribute(REF, "a" + (agentsCount+1));
					agentsCount++;
				}
				agents.put(p, agent.getAttribute(REF));
			}
			element.appendChild(agent);
		}

		if (ag instanceof Team) {
			Team team = (Team) ag;
			for (int i = 0; i < team.getTeamMembers().size(); i++) {
				ElementImpl agent = new ElementImpl(document, AGENT);
				agent.setAttribute(ROLE, role);
				Person author = team.getTeamMembers().get(i);
				if (author.getSources() != null) {
					OriginalSource os = (OriginalSource) author.getSources().toArray()[0];
					String id = os.getIdInSource();
					if (id != null) {
						if (!id.equals("")) {
							if (!agents.containsValue(id)) {
								agent.setAttribute(REF, id);
							} else if (!agents.containsValue("a" + (agentsCount+1))) {
								agent.setAttribute(REF, "a" + (agentsCount+1));
								agentsCount++;
							} else {
								agent.setAttribute(REF, id + (agentsCount+1));
								agentsCount++;
							}
						} else {
							agent.setAttribute(REF, "a" + (agentsCount+1));
							agentsCount++;
						}
					} else {
						agent.setAttribute(REF, "a" + (agentsCount+1));
						agentsCount++;
					}
					agents.put(author, agent.getAttribute(REF));
				}
				element.appendChild(agent);
			}
		}
	}


	/**
	 * Build the Hashtable for references of the elements, 
	 * then used in the different building elements methods
	 */
	/*
	public void buildReferences() {

		// <Agent> references
		for (int i = 0; i < cdmSource.getAgents().size(); i++) {
			OriginalSource os = (OriginalSource) cdmSource.getAgents().get(i).getSources().toArray()[i];
			String id = os.getIdNamespace();
			if (id != null) {
				if (!id.equals("")) {
					if (!references.containsValue(id)) {
						references.put(cdmSource.getAgents().get(i), id);
					} else if (!references.containsValue("a" + (i+1))) {
						references.put(cdmSource.getAgents().get(i), "a" + (i+1));
					} else {
						references.put(cdmSource.getAgents().get(i), id + (i+1));
					}
				} else {
					references.put(cdmSource.getAgents().get(i), "a" + (i+1));
				}
			} else {
				references.put(cdmSource.getAgents().get(i), "a" + (i+1));
			}
		}

		// <Character> references
		for (int i = 0; i < cdmSource.getFeatureData().size(); i++) {
			references.put(cdmSource.getFeatureData().get(i), "c" + (i+1));
		}

		/* no groups so far in CDM TODO
		// <DescriptiveConcept> and <Node> references
		for (int i = 0; i < base.getNbGroups(); i++) {
			references.put(base.getGroupAt(i), "dc" + (i+1));
		}
	 */

	// <State> references
	/*
		for (int i = 0; i < cdmSource.get(); i++) {
			variable = base.getVariableAt(i);
			for (int j = 0; j < variable.getNbModes(); j++) {
				references.put(variable.getModeAt(j), "s" + statesCounter);
				statesCounter++;
			}
		}

		// <CodedDescription> references
		for (int i = 0; i < base.getNbIndividuals(); i++) {
			references.put(base.getIndividualAt(i), "D" + (i+1));
		}

		// <MediaObject> references
		// TODO
		ArrayList al = base.getAllResources();
		for (int i = 0; i < al.size(); i++) {
			BaseObjectResource bor = (BaseObjectResource) al.get(i);

			if (!referencesMediaObjects.containsKey(bor)) {
				referencesMediaObjects.put(bor, "m" + mediaObjectsCounter);
				mediaObjectsCounter++;
			}
		}

		// base.images
		// BaseObjectResource bor = base.getResource();
		//bor.getName();
		//bor.getDescription();
		//bor.getFullFilename();
		/*
				if (!referencesMediaObjects.containsKey(bor)) {
					referencesMediaObjects.put(bor, "m" + mediaObjectsCounter);
					mediaObjectsCounter++;
				}
	 */	
	// group.images
	/*
				for (int i = 0; i < base.getNbGroups(); i++) {
					Object[] tab = ((Group) base.getGroupAt(i)).getAllResources();

					for (int j = 0; j < tab.length; j++) {
						bor = (BaseObjectResource) tab[j];

						if (!referencesMediaObjects.containsKey(bor)) {
							referencesMediaObjects.put(bor, "m" + mediaObjectsCounter);
							mediaObjectsCounter++;
						}
					}
				}
	 */

	/*
				int nbGroups = base.getNbGroups();
				ArrayList mObjArrayList;
				if (nbGroups > 0) {
					for (int i = 0; i < nbGroups; i++) {
						mObjArrayList = ((Group) base.getGroupAt(i)).getImages();
						for (int j = 0; j < mObjArrayList.size(); j++) {
							String temp = (String) mObjArrayList.get(j);
							if (!referencesMediaObjects.containsKey(temp)) {
								referencesMediaObjects.put(temp, "m" + mediaObjectsCounter);
								mediaObjectsCounter++;
							}
						}
					}
				}
	 */

	// individual.images
	/*
				for (int i = 0; i < base.getNbIndividuals(); i++) {
					Object[] tab = ((Individual) base.getIndividualAt(i)).getAllResources();

					for (int j = 0; j < tab.length; j++) {
						bor = (BaseObjectResource) tab[j];

						if (!referencesMediaObjects.containsKey(bor)) {
							referencesMediaObjects.put(bor, "m" + mediaObjectsCounter);
							mediaObjectsCounter++;
						}
					}
				}
	 */

	/*
				int nbIndividuals = base.getNbIndividuals();
				if (nbIndividuals > 0) {
					for (int i = 0; i < nbIndividuals; i++) {
						mObjArrayList = ((Individual) base.getIndividualAt(i)).getImages();
						for (int j = 0; j < mObjArrayList.size(); j++) {
							String temp = (String) mObjArrayList.get(j);
							if (!referencesMediaObjects.containsKey(temp)) {
								referencesMediaObjects.put(temp, "m" + mediaObjectsCounter);
								mediaObjectsCounter++;
							}
						}
					}
				}
	 */

	// variable.images
	/*
				int nbVariables = base.getNbVariables();
				if (nbVariables > 0) {
					for (int i = 0; i < nbVariables; i++) {
						mObjArrayList = ((Variable) base.getVariableAt(i)).getImages();
						for (int j = 0; j < mObjArrayList.size(); j++) {
							String temp = (String) mObjArrayList.get(j);
							if (!referencesMediaObjects.containsKey(temp)) {
								referencesMediaObjects.put(temp, "m" + mediaObjectsCounter);
								mediaObjectsCounter++;
							}
						}
					}
				}
	 */

	// mode.images
	/*
				int nbModesTotal = base.getNbModes();
				int nbModes;
				if (nbModesTotal > 0) {
					for (int i = 0; i < nbVariables; i++) {
						variable = (Variable) base.getVariableAt(i);
						nbModes = variable.getNbModes();
						for (int j = 0; j < nbModes; j++) {
							mObjArrayList = variable.getModeAt(j).getImages();
							for (int k = 0; k < mObjArrayList.size(); k++) {
								String temp = (String) mObjArrayList.get(k);
								if (!referencesMediaObjects.containsKey(temp)) {
									referencesMediaObjects.put(temp, "m" + mediaObjectsCounter);
									mediaObjectsCounter++;
								}
							}
						}
					}
				}
	 */
	/*
		for (int i = 0; i < base.getLinks().size(); i++) {
			referencesLinks.put(base.getLinkAt(i), "m" + mediaObjectsCounter);
			mediaObjectsCounter++;
		}

	}
	 */

}


