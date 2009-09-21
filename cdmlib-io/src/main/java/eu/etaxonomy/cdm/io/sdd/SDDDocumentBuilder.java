/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
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
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.ElementImpl;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;
import com.sun.org.apache.xml.internal.serialize.DOMSerializer;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import eu.etaxonomy.cdm.io.jaxb.CdmMarshallerListener;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * Writes the SDD XML file. 
 * 
 * @author h.fradin
 * @created 10.12.2008
 * @version 1.0
 */

public class SDDDocumentBuilder {

	private DocumentImpl document;
	private XMLSerializer xmlserializer;
	private Writer writer;
	private DOMSerializer domi;
	private SDDDataSet cdmSource;

	private Map<Person,String> agents = new HashMap<Person,String>();
	private Map<TaxonNameBase,String> taxonNames = new HashMap<TaxonNameBase,String>();
	private Map<Feature,String> characters = new HashMap<Feature,String>();
	private Map<TaxonDescription,String> codedDescriptions = new HashMap<TaxonDescription,String>();
	private Map<Media,String> medias = new HashMap<Media,String>();
	private Map<State,String> states = new HashMap<State,String>();
	private Map<Article, String> articles = new HashMap<Article, String>();
	private int agentsCount = 0;
	private int articlesCount = 0;
	private int codedDescriptionsCount = 0;
	private int taxonNamesCount = 0;
	private int charactersCount = 0;
	private int mediasCount = 0;
	private int statesCount = 0;

	private String AGENT = "Agent";
	private String AGENTS = "Agents";
	private String CATEGORICAL = "Categorical";
	private String CATEGORICAL_CHARACTER = "CategoricalCharacter";
	private String CHARACTER = "Character";
	private String CHARACTERS = "Characters";
	private String CHARACTER_TREE = "CharacterTree";
	private String CHARACTER_TREES = "CharacterTrees";
	private String CHAR_NODE = "CharNode";
	private String CITATION = "Citation";
	private String CODED_DESCRIPTION = "CodedDescription";
	private String CODED_DESCRIPTIONS = "CodedDescriptions";
	private String CONTENT = "Content";
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
	private String MEASURE = "Measure";
	private String MEDIA_OBJECT = "MediaObject";
	private String MEDIA_OBJECTS = "MediaObjects";
	private String NODE = "Node";
	private String NODES = "Nodes";
	private String NOTE = "Note";
	private String PARENT = "Parent";
	private String QUANTITATIVE = "Quantitative";
	private String QUANTITATIVE_CHARACTER = "QuantitativeCharacter";
	private String REF = "ref";
	private String REPRESENTATION = "Representation";
	private String REVISION_DATA = "RevisionData";
	private String ROLE = "role";
	private String SCOPE = "Scope";
	private String SHOULD_CONTAIN_ALL_CHARACTERS = "ShouldContainAllCharacters";
	private String SOURCE = "Source";
	private String STATE = "State";
	private String STATE_DEFINITION = "StateDefinition";
	private String STATES = "States";
	private String STATUS = "Status";
	private String SUMMARY_DATA = "SummaryData";
	private String TAXON_NAME = "TaxonName";
	private String TAXON_NAMES = "TaxonNames";
	private String TECHNICAL_METADATA = "TechnicalMetadata";
	private String TEXT = "Text";
	private String TEXT_CHAR = "TextChar";
	private String TEXT_CHARACTER = "TextCharacter";
	private String TYPE = "Type";
	private String URI = "uri";

	private Language defaultLanguage = Language.DEFAULT();

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
		dataset.setAttribute("xml:lang", Language.DEFAULT().getIso639_1());
		baselement.appendChild(dataset);
		buildRepresentation(dataset, reference);
		buildRevisionData(dataset, reference);
		buildIPRStatements(dataset, reference);
		buildTaxonNames(dataset);
		buildCharacters(dataset);
		buildCodedDescriptions(dataset);

	}

	/**
	 * Builds a Representation element using a ReferenceBase
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

		Set<Media> rm = reference.getMedia();

		if (rm != null && rm.size() > 0) {
			ElementImpl mediaObject;

			for (int i = 0; i < rm.size(); i++) {
				mediaObject = new ElementImpl(document, MEDIA_OBJECT);
				//mediaObject = org.apache.xerces.dom.ElementImpl(document, MEDIA_OBJECT);
				mediasCount = buildReference((Media) rm.toArray()[i], medias, REF, mediaObject, "m", mediasCount);
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

		// if language different from language dataset, indicate it TODO, but need to deal with a database language
		label.appendChild(document.createTextNode(text));
		element.appendChild(label);
	}


	/**
	 * Builds TaxonNames associated with the Dataset
	 */
	public void buildTaxonNames(ElementImpl dataset) throws ParseException {

		// <TaxonNames>
		//  <TaxonName id="t1" uri="urn:lsid:authority:namespace:my-own-id">
		//    <Representation>
		//      <Label xml:lang="la">Viola hederacea Labill.</Label>
		//    </Representation>
		//  </TaxonName>
		// </TaxonNames>

		if (cdmSource.getTaxonomicNames() != null) {
			ElementImpl elTaxonNames = new ElementImpl(document, TAXON_NAMES);

			for (int i = 0; i < cdmSource.getTaxonomicNames().size(); i++) {
				ElementImpl elTaxonName = new ElementImpl(document, TAXON_NAME);
				TaxonNameBase tnb = cdmSource.getTaxonomicNames().get(i);

				taxonNamesCount = buildReference(tnb, taxonNames, REF, elTaxonName, "t", taxonNamesCount);

				buildRepresentation(elTaxonName, tnb);

				elTaxonNames.appendChild(elTaxonName);
			}

			dataset.appendChild(elTaxonNames);
		}

	}

	/**
	 * Builds an element Agent referring to Agent defined later in the SDD file
	 */
	public void buildRefAgent(ElementImpl element, TeamOrPersonBase ag, String role) throws ParseException {
		if (ag instanceof Person) {
			Person p = (Person) ag;
			ElementImpl agent = new ElementImpl(document, AGENT);
			agent.setAttribute(ROLE, role);
			agentsCount = buildReference(p, agents, REF, agent, "a", agentsCount);
			element.appendChild(agent);
		}

		if (ag instanceof Team) {
			Team team = (Team) ag;
			for (int i = 0; i < team.getTeamMembers().size(); i++) {
				ElementImpl agent = new ElementImpl(document, AGENT);
				agent.setAttribute(ROLE, role);
				Person author = team.getTeamMembers().get(i);
				if (author.getSources() != null) {
					IdentifiableSource os = (IdentifiableSource) author.getSources().toArray()[0];
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
				} else {
					agent.setAttribute(REF, "a" + (agentsCount+1));
					agentsCount++;
				}
				agents.put(author, agent.getAttribute(REF));
				element.appendChild(agent);
			}
		}
	}

	/**
	 * Builds ModifiedDate associated with RevisionData
	 */
	public void buildDateModified(ElementImpl revisionData, Database database) throws ParseException {

		//  <DateModified>2006-04-08T00:00:00</DateModified>

		if (database.getUpdated() != null) {
			ElementImpl dateModified = new ElementImpl(document, DATE_MODIFIED);

			DateTime c = database.getUpdated();
			DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

			String date = fmt.print(c);
			dateModified.appendChild(document.createTextNode(date));

			revisionData.appendChild(dateModified);
		}

	}

	/**
	 * Builds IPRStatements associated with the Dataset
	 */
	public void buildIPRStatements(ElementImpl dataset, Database database) throws ParseException {

		// <IPRStatements>
		//  <IPRStatement role="Copyright">
		//    <Label xml:lang="en-au">(c) 2003-2006 Centre for Occasional Botany.</Label>
		//  </IPRStatement>
		// </IPRStatements>

		if (database.getRights() != null) {
			//			create IPRStatements
			ElementImpl iprStatements = new ElementImpl(document, IPR_STATEMENTS);
			dataset.appendChild(iprStatements);

			//mapping between IPRStatement Copyright (SDD) and first Right in the list of Rights
			ElementImpl iprStatement = new ElementImpl(document, IPR_STATEMENT);
			iprStatement.setAttribute("role", "Copyright");
			iprStatements.appendChild(iprStatement);
			buildLabel(iprStatement, ((Rights) database.getRights().toArray()[0]).getText());
		}

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
		//		TeamOrPersonBase editors = database.getUpdatedBy();

		if ((authors != null)) { // || (editors != null)) {
			ElementImpl creators = new ElementImpl(document, CREATORS);
			if (authors != null) {
				buildRefAgent(creators, authors, "aut");
			}
			//			if (editors != null) {
			//				buildRefAgent(creators, editors, "edt");
			//			}
			revisionData.appendChild(creators);
		}

		buildDateModified(revisionData, database);

		dataset.appendChild(revisionData);
	}

	/**
	 * Builds a Representation element using an IdentifiableEntity 
	 */
	public void buildRepresentation(ElementImpl element, IdentifiableEntity ie) throws ParseException {

		//			create <Representation> element
		ElementImpl representation = new ElementImpl(document, REPRESENTATION);
		element.appendChild(representation);
		buildLabel(representation, ie.getTitleCache());

		Set<Annotation> annotations = ie.getAnnotations();
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

	}

	/**
	 * Builds Characters associated with the Dataset
	 */
	public void buildCharacters(ElementImpl dataset) throws ParseException {

		if (cdmSource.getTerms() != null) {
			ElementImpl elCharacters = new ElementImpl(document, CHARACTERS);

			//TODO Boucle infinie
			int f = cdmSource.getTerms().size();
			for (int i = 0; i < f; i++) {
				if (cdmSource.getTerms().get(i) instanceof Feature) {
					Feature character = (Feature) cdmSource.getTerms().get(i);
					if (character.supportsQuantitativeData()) {
						ElementImpl elQuantitativeCharacter = new ElementImpl(document, QUANTITATIVE_CHARACTER);
						charactersCount = buildReference(character, characters, ID, elQuantitativeCharacter, "c", charactersCount);
						// TODO if the character also supports text, add to the label a short tag to distinguish
						// it as the quantitative version and create a unique label
						buildRepresentation(elQuantitativeCharacter, character);
						// TODO <MeasurementUnit> and <Default>
						elCharacters.appendChild(elQuantitativeCharacter);
					}

					if (character.supportsCategoricalData()) {
						ElementImpl elCategoricalCharacter = new ElementImpl(document, CATEGORICAL_CHARACTER);
						charactersCount = buildReference(character, characters, ID, elCategoricalCharacter, "c", charactersCount);
						buildRepresentation(elCategoricalCharacter, character);

						Set<TermVocabulary<State>> enumerations = character.getSupportedCategoricalEnumerations();
						if (enumerations != null) {
							if (enumerations.size()>0) {
								ElementImpl elStates = new ElementImpl(document, STATES);
								TermVocabulary tv = (TermVocabulary) enumerations.toArray()[0];
								Set<State> stateList = tv.getTerms();
								for (int j = 0; j < stateList.size(); j++) {
									ElementImpl elStateDefinition = new ElementImpl(document, STATE_DEFINITION);
									State state = (State) stateList.toArray()[j];
									statesCount = buildReference(state, states, ID, elStateDefinition, "s", statesCount);
									buildRepresentation(elStateDefinition, state);
									elStates.appendChild(elStateDefinition);
								}
								elCategoricalCharacter.appendChild(elStates);
								elCharacters.appendChild(elCategoricalCharacter);
							}
						}
					}
					if (character.supportsTextData()) {
						ElementImpl elTextCharacter = new ElementImpl(document, TEXT_CHARACTER);
						charactersCount = buildReference(character, characters, ID, elTextCharacter, "c", charactersCount);
						buildRepresentation(elTextCharacter, character);
						// TODO <MeasurementUnit> and <Default>
						elCharacters.appendChild(elTextCharacter);
					}
				}
			}

			dataset.appendChild(elCharacters);
		}

	}

	/**
	 * Builds an element Agent referring to Agent defined later in the SDD file
	 */
	public int buildReference(VersionableEntity ve, Map references, String refOrId, ElementImpl element, String prefix, int count) throws ParseException {
		if (references.containsKey(ve)) {
			element.setAttribute(refOrId,(String) references.get(ve));
		} else {
			if (ve instanceof IdentifiableEntity) {
				IdentifiableEntity ie = (IdentifiableEntity) ve;
				if (ie.getSources().size() > 0) {
					IdentifiableSource os = (IdentifiableSource) ie.getSources().toArray()[0];
					String id = os.getIdInSource();
					if (id != null) {
						if (!id.equals("")) {
							if (!references.containsValue(id)) {
								element.setAttribute(refOrId, id);
							} else while (element.getAttribute(refOrId).equals("")) {
								if (!references.containsValue(prefix + (count+1))) {
									element.setAttribute(refOrId, prefix + (count+1));
								}
								count++;
							}
						} else while (element.getAttribute(refOrId).equals("")) {
							if (!references.containsValue(prefix + (count+1))) {
								element.setAttribute(refOrId, prefix + (count+1));
							}
							count++;
						}
					} else while (element.getAttribute(refOrId).equals("")) {
						if (!references.containsValue(prefix + (count+1))) {
							element.setAttribute(refOrId, prefix + (count+1));
						}
						count++;
					}
				} else while (element.getAttribute(refOrId).equals("")) {
					if (!references.containsValue(prefix + (count+1))) {
						element.setAttribute(refOrId, prefix + (count+1));
					}
					count++;
				}
			} else while (element.getAttribute(refOrId).equals("")) {
				if (!references.containsValue(prefix + (count+1))) {
					element.setAttribute(refOrId, prefix + (count+1));
				}
				count++;
			}
			references.put(ve, element.getAttribute(refOrId));
		}
		return count;
	}

	/**
	 * Builds a Representation element using a Feature
	 */
	public void buildRepresentation(ElementImpl element, TermBase tb) throws ParseException {

		//			create <Representation> element
		ElementImpl representation = new ElementImpl(document, REPRESENTATION);
		element.appendChild(representation);

		Set<Representation> representations = tb.getRepresentations();
		if (representations != null) {
			if (!representations.isEmpty()) {
				String label = ((Representation) representations.toArray()[0]).getLabel();
				buildLabel(representation, label);
				String detailText = tb.getDescription();

				if (detailText != null && !detailText.equals("")) {
					if (!detailText.equals(label)) {
						ElementImpl detail = new ElementImpl(document, DETAIL);
						detail.appendChild(document.createTextNode(detailText));
						representation.appendChild(detail);
					}
				}

			}
		}		

		if (tb instanceof DefinedTermBase) {
			DefinedTermBase dtb = (DefinedTermBase) tb;
			Set<Media> rm = dtb.getMedia();

			if (rm != null && rm.size() > 0) {
				ElementImpl mediaObject;

				for (int i = 0; i < rm.size(); i++) {
					mediaObject = new ElementImpl(document, MEDIA_OBJECT);
					mediasCount = buildReference((Media) rm.toArray()[i], medias, REF, mediaObject, "m", mediasCount);
					representation.appendChild(mediaObject);
				}
			}
		}

	}

	//	/**
	//	 * Builds Coded Descriptions associated with the Dataset
	//	 */
	public void buildCodedDescriptions(ElementImpl dataset) throws ParseException {

		if (cdmSource.getTaxa() != null) {
			ElementImpl elCodedDescriptions = new ElementImpl(document, CODED_DESCRIPTIONS);

			for (Iterator<? extends TaxonBase> tb = cdmSource.getTaxa().iterator() ; tb.hasNext() ;){
				Taxon taxon = (Taxon) tb.next();
				Set<TaxonDescription> descriptions = taxon.getDescriptions();
				for (Iterator<TaxonDescription> td = descriptions.iterator() ; td.hasNext() ;){
					TaxonDescription taxonDescription = td.next();
					ElementImpl elCodedDescription = new ElementImpl(document, CODED_DESCRIPTION);
					codedDescriptionsCount = buildReference(taxonDescription, codedDescriptions, ID, elCodedDescription, "D", codedDescriptionsCount);
					buildRepresentation(elCodedDescription, taxonDescription);
					buildScope(elCodedDescription, taxonDescription);
					buildSummaryData(elCodedDescription, taxonDescription);
					elCodedDescriptions.appendChild(elCodedDescription);
				}
			}

			dataset.appendChild(elCodedDescriptions);
		}

	}

	/**
	 * Builds Scope associated with a CodedDescription
	 */
	public void buildScope(ElementImpl element, TaxonDescription taxonDescription) throws ParseException {

		//		  <Scope>
		//         <TaxonName ref="t1"/>
		//         <Citation ref="p1" location="p. 30"/>
		//        </Scope>

		ElementImpl scope = new ElementImpl(document, SCOPE);

		Taxon taxon = taxonDescription.getTaxon();
		if (taxon != null) {
			TaxonNameBase taxonNameBase = taxon.getName();
			if (taxonNameBase != null) {
				String ref = taxonNames.get(taxonNameBase);
				if (!ref.equals("")) {
					ElementImpl taxonName = new ElementImpl(document, TAXON_NAME);
					taxonName.setAttribute(REF, ref);
					scope.appendChild(taxonName);
				}
			}
		}

		Set<ReferenceBase> descriptionSources = taxonDescription.getDescriptionSources();
		for (Iterator<ReferenceBase> rb = descriptionSources.iterator() ; rb.hasNext() ;){
			ReferenceBase descriptionSource = rb.next();
			if (descriptionSource instanceof Article) {
				Article article = (Article) descriptionSource;
				ElementImpl citation = new ElementImpl(document, CITATION);
				articlesCount = buildReference(article, articles, REF, citation, "p", articlesCount);

				Set<Annotation> annotations = article.getAnnotations();
				for (Iterator<Annotation> a = annotations.iterator() ; a.hasNext() ;){
					Annotation annotation = a.next();
					AnnotationType annotationType = annotation.getAnnotationType();
					if (annotationType != null) {
						String type = annotationType.getLabel();
						if (type.equals("location")) {
							citation.setAttribute("location", annotation.getText());
						}
					}
				}

				scope.appendChild(citation);
			}
		}

		element.appendChild(scope);
	}

	/**
	 * Builds SummaryData associated with a CodedDescription
	 */
	public void buildSummaryData(ElementImpl element, TaxonDescription taxonDescription) throws ParseException {

		//			<SummaryData>
		//	          <Categorical ref="c4">
		//	            <State ref="s3"/>
		//	            <State ref="s4"/>
		//	          </Categorical>

		ElementImpl summaryData = new ElementImpl(document, SUMMARY_DATA);
		Set<DescriptionElementBase> elements = taxonDescription.getElements();
		for (Iterator<DescriptionElementBase> deb = elements.iterator() ; deb.hasNext() ;){
			DescriptionElementBase descriptionElement = deb.next();
			if (descriptionElement instanceof CategoricalData) {
				CategoricalData categoricalData = (CategoricalData) descriptionElement;
				buildCategorical(summaryData, categoricalData);
			}
			if (descriptionElement instanceof QuantitativeData) {
				QuantitativeData quantitativeData = (QuantitativeData) descriptionElement;
				buildQuantitative(summaryData, quantitativeData);
			}
			if (descriptionElement instanceof TextData) {
				TextData textData = (TextData) descriptionElement;
				buildTextChar(summaryData, textData);
			}
		}
		element.appendChild(summaryData);
	}

	/**
	 * Builds Categorical associated with a SummaryData
	 */
	public void buildCategorical(ElementImpl element, CategoricalData categoricalData) throws ParseException {

		//			<SummaryData>
		//	          <Categorical ref="c4">
		//	            <State ref="s3"/>
		//	            <State ref="s4"/>
		//	          </Categorical>

		ElementImpl categorical = new ElementImpl(document, CATEGORICAL);
		Feature feature = categoricalData.getFeature();
		buildReference(feature, characters, REF, categorical, "c", charactersCount);
		List<StateData> states = categoricalData.getStates();
		for (Iterator<StateData> sd = states.iterator() ; sd.hasNext() ;){
			StateData stateData = sd.next();
			State s = stateData.getState();
			buildState(categorical, s);
		}
		element.appendChild(categorical);
	}

	/**
	 * Builds State associated with a Categorical
	 */
	public void buildState(ElementImpl element, State s) throws ParseException {

		//			<SummaryData>
		//	          <Categorical ref="c4">
		//	            <State ref="s3"/>
		//	            <State ref="s4"/>
		//	          </Categorical>

		ElementImpl state = new ElementImpl(document, STATE);
		buildReference(s, states, REF, state, "s", statesCount);
		element.appendChild(state);
	}

	/**
	 * Builds Quantitative associated with a SummaryData
	 */
	public void buildQuantitative(ElementImpl element, QuantitativeData quantitativeData) throws ParseException {

		//		<Quantitative ref="c2">
		//        <Measure type="Min" value="2.3"></Measure>
		//        <Measure type="Mean" value="5.1"/>
		//        <Measure type="Max" value="7.9"/>
		//        <Measure type="SD" value="1.3"/>
		//        <Measure type="N" value="20"/>
		//      </Quantitative>

		ElementImpl quantitative = new ElementImpl(document, QUANTITATIVE);
		Feature feature = quantitativeData.getFeature();
		buildReference(feature, characters, REF, quantitative, "c", charactersCount);
		Set<StatisticalMeasurementValue> statisticalValues = quantitativeData.getStatisticalValues();
		for (Iterator<StatisticalMeasurementValue> smv = statisticalValues.iterator() ; smv.hasNext() ;){
			StatisticalMeasurementValue statisticalValue = smv.next();
			buildMeasure(quantitative, statisticalValue);
		}
		element.appendChild(quantitative);
	}

	/**
	 * Builds Measure associated with a Quantitative
	 */
	public void buildMeasure(ElementImpl element, StatisticalMeasurementValue statisticalValue) throws ParseException {

		//		<Quantitative ref="c2">
		//        <Measure type="Min" value="2.3"></Measure>
		//        <Measure type="Mean" value="5.1"/>
		//        <Measure type="Max" value="7.9"/>
		//        <Measure type="SD" value="1.3"/>
		//        <Measure type="N" value="20"/>
		//      </Quantitative>

		ElementImpl measure = new ElementImpl(document, MEASURE);
		StatisticalMeasure type = statisticalValue.getType();
		String label = type.getLabel();
		if (label.equals("Average")) {
			measure.setAttribute("type", "Mean");
		} else if (label.equals("StandardDeviation")) {
			measure.setAttribute("type", "SD");
		} else if (label.equals("SampleSize")) {
			measure.setAttribute("type", "N");
		} else {
			measure.setAttribute("type", label);
		}
		float value = statisticalValue.getValue();
		measure.setAttribute("value", String.valueOf(value));
		element.appendChild(measure);
	}

	/**
	 * Builds TextChar associated with a SummaryData
	 */
	public void buildTextChar(ElementImpl element, TextData textData) throws ParseException {

		//			<TextChar ref="c3">
		//            <Content>Free form text</Content>
		//          </TextChar>

		ElementImpl textChar = new ElementImpl(document, TEXT_CHAR);
		Feature feature = textData.getFeature();
		buildReference(feature, characters, REF, textChar, "c", charactersCount);
		Map<Language,LanguageString> multilanguageText = textData.getMultilanguageText();
		for (Iterator<Language> l = multilanguageText.keySet().iterator() ; l.hasNext() ;){
			Language language = l.next();
			LanguageString languageString = multilanguageText.get(language);
			buildContent(textChar,languageString);
		}
		element.appendChild(textChar);
	}

	/**
	 * Builds Content associated with a TextChar
	 */
	public void buildContent(ElementImpl element, LanguageString languageString) throws ParseException {

		//			<TextChar ref="c3">
		//            <Content>Free form text</Content>
		//          </TextChar>

		ElementImpl content = new ElementImpl(document, CONTENT);
		Language language = languageString.getLanguage();
		String text = languageString.getText();
		if (!language.getIso639_1().equals(defaultLanguage.getIso639_1())) {
			content.setAttribute("xml:lang", language.getIso639_1());
		}
		content.setTextContent(text);
		element.appendChild(content);
	}

	//	/**
	//	 * Build Hashtables with the references for the different elements that build the dataset, 
	//	 * and that are then used in the different building elements methods
	//	 */
	//
	//	public void buildReferences() {
	//
	//		// <TaxonNames> references
	//		for (int i = 0; i < cdmSource.getTaxonomicNames().size(); i++) {
	//			OriginalSource os = (OriginalSource) cdmSource.getAgents().get(i).getSources().toArray()[i];
	//			String id = os.getIdNamespace();
	//			if (id != null) {
	//				if (!id.equals("")) {
	//					if (!references.containsValue(id)) {
	//						references.put(cdmSource.getAgents().get(i), id);
	//					} else if (!references.containsValue("a" + (i+1))) {
	//						references.put(cdmSource.getAgents().get(i), "a" + (i+1));
	//					} else {
	//						references.put(cdmSource.getAgents().get(i), id + (i+1));
	//					}
	//				} else {
	//					references.put(cdmSource.getAgents().get(i), "a" + (i+1));
	//				}
	//			} else {
	//				references.put(cdmSource.getAgents().get(i), "a" + (i+1));
	//			}
	//		}
	//
	//		// <Character> references
	//		for (int i = 0; i < cdmSource.getFeatureData().size(); i++) {
	//			references.put(cdmSource.getFeatureData().get(i), "c" + (i+1));
	//		}
	//
	//		/* no groups so far in CDM TODO
	//		// <DescriptiveConcept> and <Node> references
	//		for (int i = 0; i < base.getNbGroups(); i++) {
	//			references.put(base.getGroupAt(i), "dc" + (i+1));
	//		}
	//		 */
	//
	//		// <State> references
	//
	//		for (int i = 0; i < cdmSource.get(); i++) {
	//			variable = base.getVariableAt(i);
	//			for (int j = 0; j < variable.getNbModes(); j++) {
	//				references.put(variable.getModeAt(j), "s" + statesCounter);
	//				statesCounter++;
	//			}
	//		}
	//
	//		// <CodedDescription> references
	//		for (int i = 0; i < base.getNbIndividuals(); i++) {
	//			references.put(base.getIndividualAt(i), "D" + (i+1));
	//		}
	//
	//		// <MediaObject> references
	//		// TODO
	//		ArrayList al = base.getAllResources();
	//		for (int i = 0; i < al.size(); i++) {
	//			BaseObjectResource bor = (BaseObjectResource) al.get(i);
	//
	//			if (!referencesMediaObjects.containsKey(bor)) {
	//				referencesMediaObjects.put(bor, "m" + mediaObjectsCounter);
	//				mediaObjectsCounter++;
	//			}
	//		}
	//
	//		// base.images
	//		// BaseObjectResource bor = base.getResource();
	//		//bor.getName();
	//		//bor.getDescription();
	//		//bor.getFullFilename();
	//
	//		if (!referencesMediaObjects.containsKey(bor)) {
	//			referencesMediaObjects.put(bor, "m" + mediaObjectsCounter);
	//			mediaObjectsCounter++;
	//		}
	//
	//		// group.images
	//
	//		for (int i = 0; i < base.getNbGroups(); i++) {
	//			Object[] tab = ((Group) base.getGroupAt(i)).getAllResources();
	//
	//			for (int j = 0; j < tab.length; j++) {
	//				bor = (BaseObjectResource) tab[j];
	//
	//				if (!referencesMediaObjects.containsKey(bor)) {
	//					referencesMediaObjects.put(bor, "m" + mediaObjectsCounter);
	//					mediaObjectsCounter++;
	//				}
	//			}
	//		}
	//
	//		int nbGroups = base.getNbGroups();
	//		ArrayList mObjArrayList;
	//		if (nbGroups > 0) {
	//			for (int i = 0; i < nbGroups; i++) {
	//				mObjArrayList = ((Group) base.getGroupAt(i)).getImages();
	//				for (int j = 0; j < mObjArrayList.size(); j++) {
	//					String temp = (String) mObjArrayList.get(j);
	//					if (!referencesMediaObjects.containsKey(temp)) {
	//						referencesMediaObjects.put(temp, "m" + mediaObjectsCounter);
	//						mediaObjectsCounter++;
	//					}
	//				}
	//			}
	//		}
	//
	//		// individual.images
	//		for (int i = 0; i < base.getNbIndividuals(); i++) {
	//			Object[] tab = ((Individual) base.getIndividualAt(i)).getAllResources();
	//
	//			for (int j = 0; j < tab.length; j++) {
	//				bor = (BaseObjectResource) tab[j];
	//
	//				if (!referencesMediaObjects.containsKey(bor)) {
	//					referencesMediaObjects.put(bor, "m" + mediaObjectsCounter);
	//					mediaObjectsCounter++;
	//				}
	//			}
	//		}
	//
	//		int nbIndividuals = base.getNbIndividuals();
	//		if (nbIndividuals > 0) {
	//			for (int i = 0; i < nbIndividuals; i++) {
	//				mObjArrayList = ((Individual) base.getIndividualAt(i)).getImages();
	//				for (int j = 0; j < mObjArrayList.size(); j++) {
	//					String temp = (String) mObjArrayList.get(j);
	//					if (!referencesMediaObjects.containsKey(temp)) {
	//						referencesMediaObjects.put(temp, "m" + mediaObjectsCounter);
	//						mediaObjectsCounter++;
	//					}
	//				}
	//			}
	//		}
	//
	//		// variable.images
	//
	//		int nbVariables = base.getNbVariables();
	//		if (nbVariables > 0) {
	//			for (int i = 0; i < nbVariables; i++) {
	//				mObjArrayList = ((Variable) base.getVariableAt(i)).getImages();
	//				for (int j = 0; j < mObjArrayList.size(); j++) {
	//					String temp = (String) mObjArrayList.get(j);
	//					if (!referencesMediaObjects.containsKey(temp)) {
	//						referencesMediaObjects.put(temp, "m" + mediaObjectsCounter);
	//						mediaObjectsCounter++;
	//					}
	//				}
	//			}
	//		}
	//
	//		// mode.images
	//
	//		int nbModesTotal = base.getNbModes();
	//		int nbModes;
	//		if (nbModesTotal > 0) {
	//			for (int i = 0; i < nbVariables; i++) {
	//				variable = (Variable) base.getVariableAt(i);
	//				nbModes = variable.getNbModes();
	//				for (int j = 0; j < nbModes; j++) {
	//					mObjArrayList = variable.getModeAt(j).getImages();
	//					for (int k = 0; k < mObjArrayList.size(); k++) {
	//						String temp = (String) mObjArrayList.get(k);
	//						if (!referencesMediaObjects.containsKey(temp)) {
	//							referencesMediaObjects.put(temp, "m" + mediaObjectsCounter);
	//							mediaObjectsCounter++;
	//						}
	//					}
	//				}
	//			}
	//		}
	//
	//		for (int i = 0; i < base.getLinks().size(); i++) {
	//			referencesLinks.put(base.getLinkAt(i), "m" + mediaObjectsCounter);
	//			mediaObjectsCounter++;
	//		}
	//
	//	}
	
}