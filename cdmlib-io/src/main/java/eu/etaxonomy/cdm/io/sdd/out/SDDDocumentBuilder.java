/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.sdd.out;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureState;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.IDatabase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermBase;
import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;

/**
 * Writes the SDD XML file.
 *
 * @author h.fradin
 * @since 10.12.2008
 */
public class SDDDocumentBuilder {

	private final Document document;
	private SDDDataSet cdmSource;

	private final Map<Person, String> agents = new HashMap<>();
	private final Map<TaxonName, String> taxonNames = new HashMap<>();
	private final Map<Feature, String> characters = new HashMap<>();
	private final Map<Feature, String> descriptiveConcepts = new HashMap<>();
	private final Map<TaxonDescription, String> codedDescriptions = new HashMap<>();
	private final Map<Media, String> medias = new HashMap<>();
	private final Map<State, String> states = new HashMap<>();
	private final Map<Reference, String> articles = new HashMap<>();
	private final Map<VersionableEntity, String> featuretrees = new HashMap<>();
	private final Map<DefinedTerm, String> modifiers = new HashMap<>();
	private final Map<TaxonNode, String> taxonNodes = new HashMap<>();
	private final Map<NamedArea, String> namedAreas = new HashMap<>();
	private final Map<DerivedUnit, String> specimens = new HashMap<>();

	private int agentsCount = 0;
	private int articlesCount = 0;
	private int codedDescriptionsCount = 0;
	private int taxonNamesCount = 0;
	private int charactersCount = 0;
	private int textcharactersCount = 0;
	private int mediasCount = 0;
	private int statesCount = 0;
	private int chartreeCount = 0;
	private int charnodeCount = 0;
	private int taxonNodesCount = 0;
	private int namedAreasCount = 0;
	private int specimenCount = 0;
	private int descriptiveConceptCount = 0;
	private int modifiersCount = 0;

	private final String AGENT = "Agent";
	private final String AGENTS = "Agents";
	private final String CATEGORICAL = "Categorical";
	private final String CATEGORICAL_CHARACTER = "CategoricalCharacter";
	private final String CHARACTER = "Character";
	private final String CHARACTERS = "Characters";
	private final String CHARACTER_TREE = "CharacterTree";
	private final String CHARACTER_TREES = "CharacterTrees";
	private final String CHAR_NODE = "CharNode";
	private final String CITATION = "Citation";
	private final String CODED_DESCRIPTION = "CodedDescription";
	private final String CODED_DESCRIPTIONS = "CodedDescriptions";
	private final String CONTENT = "Content";
	private final String CREATORS = "Creators";
	private final String DATASET = "Dataset";
	private final String DATASETS = "Datasets";
	private final String DATE_CREATED = "DateCreated";
	private final String DATE_MODIFIED = "DateModified";
	private final String DEPENDENCY_RULES = "DependencyRules";
	private final String DESCRIPTIVE_CONCEPT = "DescriptiveConcept";
	private final String DESCRIPTIVE_CONCEPTS = "DescriptiveConcepts";
	private final String DETAIL = "Detail";
	private final String GENERATOR = "Generator";
	private final String ID = "id";
	private final String IMAGE = "Image";
	private final String INAPPLICABLE_IF = "InapplicableIf";
	private final String IPR_STATEMENT = "IPRStatement";
	private final String IPR_STATEMENTS = "IPRStatements";
	private final String LABEL = "Label";
	private final String MEASURE = "Measure";
	private final String MEDIA_OBJECT = "MediaObject";
	private final String MEDIA_OBJECTS = "MediaObjects";
	private final String NODE = "Node";
	private final String NODES = "Nodes";
	private final String NOTE = "Note";
	private final String PARENT = "Parent";
	private final String PUBLICATIONS = "Publications";
	private final String QUANTITATIVE = "Quantitative";
	private final String QUANTITATIVE_CHARACTER = "QuantitativeCharacter";
	private final String REF = "ref";
	private final String REPRESENTATION = "Representation";
	private final String REVISION_DATA = "RevisionData";
	private final String ROLE = "role";
	private final String SCOPE = "Scope";
	private final String SHOULD_CONTAIN_ALL_CHARACTERS = "ShouldContainAllCharacters";
	private final String SOURCE = "Source";
	private final String STATE = "State";
	private final String STATE_DEFINITION = "StateDefinition";
	private final String STATES = "States";
	private final String STATUS = "Status";
	private final String SUMMARY_DATA = "SummaryData";
	private final String TAXON_NAME = "TaxonName";
	private final String TAXON_NAMES = "TaxonNames";
	private final String TECHNICAL_METADATA = "TechnicalMetadata";
	private final String TEXT = "text";
	private final String TEXT_CHAR = "TextChar";
	private final String TEXT_CHARACTER = "TextCharacter";
	private final String TYPE = "Type";
	private final String URI = "uri";

	private final Language defaultLanguage = Language.DEFAULT();

	private static final Logger logger = LogManager.getLogger();

	public SDDDocumentBuilder() throws ParserConfigurationException {
	    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		document = builder.newDocument();
	}

	public void marshal(SDDDataSet cdmSource, File sddDestination)
			throws IOException, TransformerException {

		this.cdmSource = cdmSource;
		logger.info("Start marshalling");
		writeCDMtoSDD(sddDestination);
	}

	public void marshal(SDDDataSet cdmSource, String sddDestinationFileName)
			throws IOException, TransformerException {

		this.cdmSource = cdmSource;
		logger.info("Start marshalling");
		writeCDMtoSDD(ResourceUtils.getFile(sddDestinationFileName));
	}

    public void marshal(SDDDataSet dataSet, OutputStream stream) throws TransformerException {
        this.cdmSource = dataSet;
        logger.info("Start marshalling");
        try {
            buildDocument();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();

        // Formatting
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        transformer.transform(new DOMSource(document), new StreamResult(stream));
    }

	/**
	 * Write the DOM document.
	 *
	 * @param base
	 * @throws IOException
	 * @throws TransformerException
	 */
	public void writeCDMtoSDD(File sddDestination) throws IOException, TransformerException {

		try {
			buildDocument();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		FileOutputStream fos = new FileOutputStream(sddDestination);
		Writer writer = new OutputStreamWriter(fos, "UTF-8");

		try {
		    Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");  // = pretty print (true)
		    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		    transformer.transform(
		        new DOMSource(document.getDocumentElement()),
		        new StreamResult(writer)
		    );
		}finally {
		    writer.close();
		}

//
//
//		OutputFormat format = new OutputFormat(document, "UTF-8", true);
//
//
//		writer = new OutputStreamWriter(fos, "UTF-8");
//
//		xmlserializer = new XMLSerializer(writer, format);
//		domi = xmlserializer.asDOMSerializer(); // As a DOM Serializer
//
//		domi.serialize(document.getDocumentElement());
//
//		writer.close();
	}

	// #############
	// # BUILD DOM #
	// #############

	/**
	 * Builds the whole document.
	 *
	 * @param base
	 *            the Base
	 * @throws ParseException
	 */
	public void buildDocument() throws ParseException {

		// create <Datasets> = root node
		Element baselement = document.createElement(DATASETS);
		baselement.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		baselement.setAttribute("xmlns", "http://rs.tdwg.org/UBIF/2006/");
		baselement
				.setAttribute("xsi:schemaLocation",
						"http://rs.tdwg.org/UBIF/2006 http://rs.tdwg.org/UBIF/2006/Schema/1.1/SDD.xsd");

		buildTechnicalMetadata(baselement);

		List<Reference> references = cdmSource.getReferences();
		Iterator<Reference> iterator = references.iterator();
		IDatabase d = ReferenceFactory.newDatabase();
		while (iterator.hasNext()) {
			Reference reference = iterator.next();
			if (reference.getType().equals(ReferenceType.Database)) {
				buildDataset(baselement, reference);
			}
		}
		document.appendChild(baselement);
	}

	// #############
	// # BUILD DOM #
	// #############

	/**
	 * Builds TechnicalMetadata associated with the SDD file
	 */
	public void buildTechnicalMetadata(Element baselement)
			throws ParseException {
		// create TechnicalMetadata
		Element technicalMetadata = document.createElement(TECHNICAL_METADATA);
		// select different databases associated to different descriptions TODO
		List<Reference> references = cdmSource.getReferences();
		Iterator<Reference> iterator = references.iterator();
		boolean database = false;
		IDatabase d = ReferenceFactory.newDatabase();
		while ((iterator.hasNext()) && (!database)) {
			Reference reference = iterator.next();
			if (reference.getType().equals(ReferenceType.Database)) {
				d = reference;
			}
		}
		DateTime dt = d.getCreated();
		String date = dt.toString().substring(0, 19);
		technicalMetadata.setAttribute("created", date);

		Element generator = document.createElement(GENERATOR);
		generator.setAttribute("name", "EDIT CDM");
		generator.setAttribute("version", "v1");
		generator
				.setAttribute(
						"notes",
						"This SDD file has been generated by the SDD export functionality of the EDIT platform for Cybertaxonomy - Copyright (c) 2008");
		technicalMetadata.appendChild(generator);

		baselement.appendChild(technicalMetadata);
	}

	// Builds the information associated with a dataset
	public void buildDataset(Element baselement, IDatabase reference)
			throws ParseException {
		// create Dataset and language
		Element dataset = document.createElement(DATASET);
		// no default language associated with a dataset in the CDM
		dataset.setAttribute("xml:lang", Language.DEFAULT().getIso639_1());
		baselement.appendChild(dataset);
		buildRepresentation(dataset, reference);
		buildRevisionData(dataset, reference);
		buildTaxonNames(dataset);
		buildDescriptiveConcepts(dataset);
		buildCharacters(dataset);
		buildCodedDescriptions(dataset);
		buildAgents(dataset);
		buildPublications(dataset);
		buildMediaObjects(dataset);
		buildCharacterTrees(dataset);
		buildClassifications(dataset);
		buildGeographicAreas(dataset);
		buildSpecimens(dataset);
	}

	/**
	 * Builds a Representation element using a Reference
	 */
	public void buildRepresentation(Element element, IDatabase reference){

		// create <Representation> element
		Element representation = document.createElement(REPRESENTATION);
		element.appendChild(representation);
		buildLabel(representation, reference.getTitleCache());

		Set<Annotation> annotations = ((Reference) reference).getAnnotations();
		Iterator<Annotation> iterator = annotations.iterator();
		String detailText = null;
		if (iterator.hasNext()) {
			Annotation annotation = iterator.next();
			detailText = annotation.getText();
		}

		if (detailText != null && !detailText.equals("")) {
			Element detail = document.createElement(DETAIL);
			detail.appendChild(document.createTextNode(detailText));
			representation.appendChild(detail);
		}

		Set<Media> rm = ((Reference) reference).getMedia();

		if (rm != null && rm.size() > 0) {
			Element mediaObject;

			for (int i = 0; i < rm.size(); i++) {
				mediaObject = document.createElement(MEDIA_OBJECT);
				mediasCount = buildReference((Media) rm.toArray()[i], medias,
						REF, mediaObject, "m", mediasCount);
				representation.appendChild(mediaObject);
			}
		}

	}

	/**
	 * Builds a Representation element using a Feature
	 */
	public void buildRepresentation(Element element, TermBase tb){

		// create <Representation> element
		Element representation = document.createElement(REPRESENTATION);
		element.appendChild(representation);

		Set<Representation> representations = tb.getRepresentations();
		if (representations != null) {
			if (!representations.isEmpty()) {
				String label = ((Representation) representations.toArray()[0])
						.getLabel();
				buildLabel(representation, label);
				String detailText = tb.getDescription();

				if (detailText != null && !detailText.equals("")) {
					if (!detailText.equals(label)) {
					    Element detail = document.createElement(DETAIL);
						detail.appendChild(document.createTextNode(detailText));
						representation.appendChild(detail);
					}
				}

			}
		}

		if (tb instanceof DefinedTermBase) {
			DefinedTermBase<?> dtb = (DefinedTermBase<?>) tb;
			Set<Media> rm = dtb.getMedia();

			if (rm != null && rm.size() > 0) {
				Element mediaObject;

				for (int i = 0; i < rm.size(); i++) {
					mediaObject = document.createElement(MEDIA_OBJECT);
					mediasCount = buildReference((Media) rm.toArray()[i],
							medias, REF, mediaObject, "m", mediasCount);
					representation.appendChild(mediaObject);
				}
			}
		}
	}

	/**
	 * Builds a Representation element using an IdentifiableEntity
	 */
	private void buildRepresentation(Element element, IdentifiableEntity<?> ie){

		// create <Representation> element
		Element representation = document.createElement(REPRESENTATION);
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
			Element detail = document.createElement(DETAIL);
			detail.appendChild(document.createTextNode(detailText));
			representation.appendChild(detail);
		}

		if (ie instanceof DefinedTermBase) {
			DefinedTermBase<?> dtb = (DefinedTermBase<?>) ie;
			Set<Media> rm = dtb.getMedia();

			if (rm != null && rm.size() > 0) {
				Element mediaObject;

				for (int i = 0; i < rm.size(); i++) {
					mediaObject = document.createElement(MEDIA_OBJECT);
					mediasCount = buildReference((Media) rm.toArray()[i],
							medias, REF, mediaObject, "m", mediasCount);
					representation.appendChild(mediaObject);
				}
			}
		}
		if (ie instanceof IdentifiableMediaEntity) {
			IdentifiableMediaEntity<?> ime = (IdentifiableMediaEntity<?>) ie;
			Set<Media> medias = ime.getMedia();
			if (medias != null) {
				Element elLinks = document.createElement("Links");
				for (Iterator<Media> m = medias.iterator(); m.hasNext();) {
					Media media = m.next();
					Set<MediaRepresentation> smr = media.getRepresentations();
					for (Iterator<MediaRepresentation> mr = smr.iterator(); mr
							.hasNext();) {
						MediaRepresentation mediarep = mr.next();
						List<MediaRepresentationPart> lmrp = mediarep
								.getParts();
						for (Iterator<MediaRepresentationPart> mrp = lmrp
								.iterator(); mrp.hasNext();) {
							MediaRepresentationPart mediareppart = mrp.next();
							Element elLink = document.createElement("Link");
							elLink.setAttribute("href", mediareppart.getUri()
									.toString());
							elLinks.appendChild(elLink);
						}
					}
				}
				element.appendChild(elLinks);
			}
		}
	}

	/**
	 * Builds RevisionData associated with the Dataset
	 */
	public void buildRevisionData(Element dataset, IDatabase database)
			throws ParseException {

		// <RevisionData>
		// <Creators>
		// <Agent role="aut" ref="a1"/>
		// <Agent role="aut" ref="a2"/>
		// <Agent role="edt" ref="a3"/>
		// </Creators>
		// <DateModified>2006-04-08T00:00:00</DateModified>
		// </RevisionData>

		Element revisionData = document.createElement(REVISION_DATA);

		// authors
		TeamOrPersonBase<?> authors = database.getAuthorship();
		// TeamOrPersonBase editors = database.getUpdatedBy();

		if ((authors != null)) { // || (editors != null)) {
			Element creators = document.createElement(CREATORS);
			buildRefAgent(creators, authors, "aut");
			// if (editors != null) {
			// buildRefAgent(creators, editors, "edt");
			// }
			revisionData.appendChild(creators);
		}

		buildDateModified(revisionData, database);

		dataset.appendChild(revisionData);
	}

	/**
	 * Builds ModifiedDate associated with RevisionData
	 */
	public void buildDateModified(Element revisionData, IDatabase database) {

		// <DateModified>2006-04-08T00:00:00</DateModified>

		if (((Reference) database).getUpdated() != null) {
			Element dateModified = document.createElement(DATE_MODIFIED);

			DateTime c = ((Reference) database).getUpdated();
			DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

			String date = fmt.print(c);
			dateModified.appendChild(document.createTextNode(date));

			revisionData.appendChild(dateModified);
		}
	}

	/**
	 * Creates a Label element
	 *
	 * @param base
	 * @param element
	 */
	public void buildLabel(Element element, String text) {
		Element label = document.createElement(LABEL);
		label.appendChild(document.createTextNode(text));
		element.appendChild(label);
	}

	// ################
	// # GENERIC BRICKS #
	// ################

	/**
	 * Builds TaxonNames associated with the Dataset
	 */
	public void buildTaxonNames(Element dataset) throws ParseException {

		// <TaxonNames>
		// <TaxonName id="t1" uri="urn:lsid:authority:namespace:my-own-id">
		// <Representation>
		// <Label xml:lang="la">Viola hederacea Labill.</Label>
		// </Representation>
		// </TaxonName>
		// </TaxonNames>

		if (cdmSource.getTaxonomicNames() != null) {
			Element elTaxonNames = document.createElement(TAXON_NAMES);

			for (int i = 0; i < cdmSource.getTaxonomicNames().size(); i++) {
				Element elTaxonName = document.createElement(TAXON_NAME);
				TaxonName tnb = cdmSource.getTaxonomicNames().get(i);

				taxonNamesCount = buildReference(tnb, taxonNames, ID,
						elTaxonName, "t", taxonNamesCount);

				buildRepresentation(elTaxonName, tnb);

				elTaxonNames.appendChild(elTaxonName);
			}

			dataset.appendChild(elTaxonNames);
		}

	}

	public void buildDescriptiveConcepts(Element dataset)
			throws ParseException {

		if (cdmSource.getFeatureData() != null) {
			Element elFeatures = document.createElement(DESCRIPTIVE_CONCEPTS);
			int f = cdmSource.getTerms().size();
			for (int i = 0; i < f; i++) {
				DefinedTermBase<?> dtb = cdmSource.getTerms().get(i);
				if (dtb instanceof Feature) {
					Element elFeat = document.createElement(DESCRIPTIVE_CONCEPT);
					Feature feature = (Feature) dtb;
					if (feature.getMarkers() != null) {
						Set<Marker> markers = feature.getMarkers();
						for (Iterator<Marker> m = markers.iterator(); m
								.hasNext();) {
							Marker marker = m.next();
							if (marker.getMarkerType().getLabel()
									.equals("DescriptiveConcept")) {
								descriptiveConceptCount = buildReference(dtb,
										descriptiveConcepts, ID, elFeat, "dc",
										descriptiveConceptCount);
								buildRepresentation(elFeat, feature);
								if (!feature
										.getRecommendedModifierEnumeration()
										.isEmpty()) {
									Element elModifiers = document.createElement("Modifiers");
									for (Iterator<TermCollection<DefinedTerm,?>> menum = feature
											.getRecommendedModifierEnumeration()
											.iterator(); menum.hasNext();) {
									    TermCollection<DefinedTerm,?> termVoc = menum.next();
										Set<DefinedTerm> sm = termVoc.getDistinctTerms();
										for (Iterator<DefinedTerm> modif = sm.iterator(); modif.hasNext();) {
											DefinedTerm modifier = modif.next();
											Element elModifier = document.createElement("Modifier");
											modifiersCount = buildReference(
													modifier, modifiers, ID,
													elModifier, "mod",
													modifiersCount);
											buildRepresentation(elModifier,
													modifier);
											elModifiers.appendChild(elModifier);
										}
									}
									elFeat.appendChild(elModifiers);
								}
								elFeatures.appendChild(elFeat);
							}
						}
					}
				}
			}
			dataset.appendChild(elFeatures);
		}
	}

	/**
	 * Builds Characters associated with the Dataset
	 */
	public void buildCharacters(Element dataset) throws ParseException {

		if (cdmSource.getTerms() != null) {
			Element elCharacters = document.createElement(CHARACTERS);

			int f = cdmSource.getTerms().size();
			for (int i = 0; i < f; i++) {
				if (cdmSource.getTerms().get(i) instanceof Feature) {
					Feature character = (Feature) cdmSource.getTerms().get(i);
					if (character.isSupportsQuantitativeData()) {
						Element elQuantitativeCharacter = document.createElement(
						        QUANTITATIVE_CHARACTER);
						charactersCount = buildReference(character, characters,
								ID, elQuantitativeCharacter, "c",
								charactersCount);
						buildRepresentation(elQuantitativeCharacter, character);
						elCharacters.appendChild(elQuantitativeCharacter);
					}

					if (character.isSupportsCategoricalData()) {
						Element elCategoricalCharacter = document.createElement(
						        CATEGORICAL_CHARACTER);
						charactersCount = buildReference(character, characters,
								ID, elCategoricalCharacter, "c",
								charactersCount);
						buildRepresentation(elCategoricalCharacter, character);

						Set<TermCollection<?,?>> enumerations = character
								.getSupportedCategoricalEnumerations();
						if (enumerations != null) {
							if (enumerations.size() > 0) {
								Element elStates = document.createElement(STATES);
								TermCollection<?,?> tv = enumerations.iterator().next();
								Set<?> stateList = tv.getDistinctTerms();
								for (int j = 0; j < stateList.size(); j++) {
									Element elStateDefinition = document.createElement(
									        STATE_DEFINITION);
									State state = (State) stateList.toArray()[j];
									statesCount = buildReference(state, states,
											ID, elStateDefinition, "s",
											statesCount);
									buildRepresentation(elStateDefinition,
											state);
									elStates.appendChild(elStateDefinition);
								}
								elCategoricalCharacter.appendChild(elStates);
								elCharacters
										.appendChild(elCategoricalCharacter);
							}
						}
					}
					if (character.isSupportsTextData()) {
						Element elTextCharacter = document.createElement(
								TEXT_CHARACTER);
						textcharactersCount = buildReference(character,
								characters, ID, elTextCharacter, TEXT,
								textcharactersCount);
						buildRepresentation(elTextCharacter, character);
						elCharacters.appendChild(elTextCharacter);
					}
				}
			}

			dataset.appendChild(elCharacters);
		}

	}

	public void buildCodedDescriptions(Element dataset)
			throws ParseException {

		if (cdmSource.getTaxa() != null) {
			Element elCodedDescriptions = document.createElement(
					CODED_DESCRIPTIONS);

			for (Iterator<? extends TaxonBase> tb = cdmSource.getTaxa()
					.iterator(); tb.hasNext();) {
				Taxon taxon = (Taxon) tb.next();
				Set<TaxonDescription> descriptions = taxon.getDescriptions();
				for (Iterator<TaxonDescription> td = descriptions.iterator(); td
						.hasNext();) {
					TaxonDescription taxonDescription = td.next();
					Element elCodedDescription = document.createElement(
							CODED_DESCRIPTION);
					codedDescriptionsCount = buildReference(taxonDescription,
							codedDescriptions, ID, elCodedDescription, "D",
							codedDescriptionsCount);
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
	public void buildScope(Element element,
			TaxonDescription taxonDescription) throws ParseException {

		// <Scope>
		// <TaxonName ref="t1"/>
		// <Citation ref="p1" location="p. 30"/>
		// </Scope>

		Element scope = document.createElement(SCOPE);

		Taxon taxon = taxonDescription.getTaxon();
		if (taxon != null) {
			TaxonName taxonName = taxon.getName();
			if (taxonName != null) {
				String ref = taxonNames.get(taxonName);
				if (!ref.equals("")) {
					Element taxonNameEl = document.createElement(
							TAXON_NAME);
					taxonNameEl.setAttribute(REF, ref);
					scope.appendChild(taxonNameEl);
				}
			}
		}

		Set<Reference> descriptionSources = new HashSet<Reference>();
		for (IdentifiableSource source : taxonDescription.getSources()){
			descriptionSources.add(source.getCitation());
		}
		for (Iterator<Reference> rb = descriptionSources.iterator(); rb.hasNext();) {
			Reference descriptionSource = rb.next();
			if (descriptionSource.getType().equals(ReferenceType.Article)) {

				Element citation = document.createElement(CITATION);
				articlesCount = buildReference(descriptionSource, articles,
						REF, citation, "p", articlesCount);

				Set<Annotation> annotations = descriptionSource.getAnnotations();
				for (Iterator<Annotation> a = annotations.iterator(); a.hasNext();) {
					Annotation annotation = a.next();
					AnnotationType annotationType = annotation.getAnnotationType();
					if (annotationType != null) {
						String type = annotationType.getLabel();
						if (type.equals("location")) {
							citation.setAttribute("location",annotation.getText());
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
	public void buildSummaryData(Element element,
			TaxonDescription taxonDescription) throws ParseException {

		// <SummaryData>
		// <Categorical ref="c4">
		// <State ref="s3"/>
		// <State ref="s4"/>
		// </Categorical>

		Element summaryData = document.createElement(SUMMARY_DATA);
		Set<DescriptionElementBase> elements = taxonDescription.getElements();
		for (Iterator<DescriptionElementBase> deb = elements.iterator(); deb
				.hasNext();) {
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
	public void buildCategorical(Element element,
			CategoricalData categoricalData) throws ParseException {

		// <SummaryData>
		// <Categorical ref="c4">
		// <State ref="s3"/>
		// <State ref="s4"/>
		// </Categorical>

		Element categorical = document.createElement(CATEGORICAL);
		Feature feature = categoricalData.getFeature();
		buildReference(feature, characters, REF, categorical, "c",
				charactersCount);
		List<StateData> states = categoricalData.getStateData();
		for (Iterator<StateData> sd = states.iterator(); sd.hasNext();) {
			StateData stateData = sd.next();
			DefinedTermBase<?> s = stateData.getState();
			buildState(categorical, s);
		}
		element.appendChild(categorical);
	}

	/**
	 * Builds State associated with a Categorical
	 */
	public void buildState(Element element, DefinedTermBase<?> s) throws ParseException {

		// <SummaryData>
		// <Categorical ref="c4">
		// <State ref="s3"/>
		// <State ref="s4"/>
		// </Categorical>

		Element state = document.createElement(STATE);
		buildReference(s, states, REF, state, "s", statesCount);
		element.appendChild(state);
	}

	/**
	 * Builds Quantitative associated with a SummaryData
	 */
	public void buildQuantitative(Element element,
			QuantitativeData quantitativeData) throws ParseException {

		// <Quantitative ref="c2">
		// <Measure type="Min" value="2.3"></Measure>
		// <Measure type="Mean" value="5.1"/>
		// <Measure type="Max" value="7.9"/>
		// <Measure type="SD" value="1.3"/>
		// <Measure type="N" value="20"/>
		// </Quantitative>

		Element quantitative = document.createElement(QUANTITATIVE);
		Feature feature = quantitativeData.getFeature();
		buildReference(feature, characters, REF, quantitative, "c",
				charactersCount);
		Set<StatisticalMeasurementValue> statisticalValues = quantitativeData
				.getStatisticalValues();
		for (Iterator<StatisticalMeasurementValue> smv = statisticalValues
				.iterator(); smv.hasNext();) {
			StatisticalMeasurementValue statisticalValue = smv.next();
			buildMeasure(quantitative, statisticalValue);
		}
		element.appendChild(quantitative);
	}

	/**
	 * Builds Measure associated with a Quantitative
	 */
	public void buildMeasure(Element element,
			StatisticalMeasurementValue statisticalValue) {

		// <Quantitative ref="c2">
		// <Measure type="Min" value="2.3"></Measure>
		// <Measure type="Mean" value="5.1"/>
		// <Measure type="Max" value="7.9"/>
		// <Measure type="SD" value="1.3"/>
		// <Measure type="N" value="20"/>
		// </Quantitative>

		Element measure = document.createElement(MEASURE);
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
		BigDecimal value = statisticalValue.getValue();
		measure.setAttribute("value", String.valueOf(value));
		element.appendChild(measure);
	}

	/**
	 * Builds TextChar associated with a SummaryData
	 */
	public void buildTextChar(Element element, TextData textData)
			throws ParseException {

		// <TextChar ref="c3">
		// <Content>Free form text</Content>
		// </TextChar>

		Element textChar = document.createElement(TEXT_CHAR);
		Feature feature = textData.getFeature();
		buildReference(feature, characters, REF, textChar, "c", charactersCount);
		Map<Language, LanguageString> multilanguageText = textData
				.getMultilanguageText();
		for (Language language : multilanguageText.keySet()) {
			LanguageString languageString = multilanguageText.get(language);
			buildContent(textChar, languageString);
		}
		element.appendChild(textChar);
	}

	/**
	 * Builds Content associated with a TextChar
	 */
	public void buildContent(Element element, LanguageString languageString){

		// <TextChar ref="c3">
		// <Content>Free form text</Content>
		// </TextChar>

		Element content = document.createElement(CONTENT);
		Language language = languageString.getLanguage();
		String text = languageString.getText();
		if (!language.getIso639_1().equals(defaultLanguage.getIso639_1())) {
			content.setAttribute("xml:lang", language.getIso639_1());
		}
		content.setTextContent(text);
		element.appendChild(content);
	}

	/**
	 * Builds an element Agent referring to Agent defined later in the SDD file
	 */
	private void buildRefAgent(Element element, TeamOrPersonBase<?> ag,
			String role) throws ParseException {
		if (ag instanceof Person) {
			Person p = (Person) ag;
			Element agent = document.createElement(AGENT);
			if (ag.getMarkers() != null) {
				Set<Marker> markers = ag.getMarkers();
				for (Iterator<Marker> m = markers.iterator(); m.hasNext();) {
					Marker marker = m.next();
					if (marker.getMarkerType().getLabel().equals("editor")) {
						agent.setAttribute(ROLE, "edt");
					}
				}
			} else {
				agent.setAttribute(ROLE, role);
			}
			agentsCount = buildReference(p, agents, REF, agent, "a",
					agentsCount);
			element.appendChild(agent);
		}

		if (ag instanceof Team) {
			Team team = (Team) ag;
			for (int i = 0; i < team.getTeamMembers().size(); i++) {
				Person author = team.getTeamMembers().get(i);
				Element agent = document.createElement(AGENT);
				if (author.getMarkers() != null) {
					Set<Marker> markers = author.getMarkers();
					if (!markers.isEmpty()) {
						for (Iterator<Marker> m = markers.iterator(); m
								.hasNext();) {
							Marker marker = m.next();
							if (marker.getMarkerType().getLabel()
									.equals("editor")) {
								agent.setAttribute(ROLE, "edt");
							}
						}
					} else {
						agent.setAttribute(ROLE, role);
					}
				} else {
					agent.setAttribute(ROLE, role);
				}
				if (author.getSources() != null) {
					IdentifiableSource os = (IdentifiableSource) author
							.getSources().toArray()[0];
					String id = os.getIdInSource();
					if (id != null) {
						if (!id.equals("")) {
							if (!agents.containsValue(id)) {
								agent.setAttribute(REF, id);
							} else if (!agents.containsValue("a"
									+ (agentsCount + 1))) {
								agent.setAttribute(REF, "a" + (agentsCount + 1));
								agentsCount++;
							} else {
								agent.setAttribute(REF, id + (agentsCount + 1));
								agentsCount++;
							}
						} else {
							agent.setAttribute(REF, "a" + (agentsCount + 1));
							agentsCount++;
						}
					} else {
						agent.setAttribute(REF, "a" + (agentsCount + 1));
						agentsCount++;
					}
				} else {
					agent.setAttribute(REF, "a" + (agentsCount + 1));
					agentsCount++;
				}
				agents.put(author, agent.getAttribute(REF));
				element.appendChild(agent);
			}
		}
	}

	/**
	 * Builds Agents associated with the Dataset
	 */
	public void buildAgents(Element dataset) {

		if (cdmSource.getAgents() != null) {
			Element elAgents = document.createElement(AGENTS);

			for (int i = 0; i < cdmSource.getAgents().size(); i++) {
				Element elAgent = document.createElement(AGENT);
				AgentBase<?> personagent = cdmSource.getAgents().get(i);
				if (personagent instanceof Person) {
					if (personagent.getMarkers() != null) {
						Set<Marker> markers = personagent.getMarkers();
						for (Iterator<Marker> m = markers.iterator(); m
								.hasNext();) {
							Marker marker = m.next();
							if (marker.getMarkerType().getLabel()
									.equals("editor")) {
								agentsCount = buildReference(personagent,
										agents, ID, elAgent, "a", agentsCount);
							}
						}
					}
					agentsCount = buildReference(personagent, agents, ID,
							elAgent, "a", agentsCount);
					buildRepresentation(elAgent, personagent);
					elAgents.appendChild(elAgent);
				}
			}

			dataset.appendChild(elAgents);
		}
	}

	public void buildCharacterTrees(Element dataset) throws ParseException {

		if (cdmSource.getFeatureData() != null) {
			Element elChartrees = document.createElement(CHARACTER_TREES);

			for (int i = 0; i < cdmSource.getFeatureData().size(); i++) {
				VersionableEntity featu = cdmSource.getFeatureData().get(i);
				if (featu instanceof TermTree) {
					TermTree ft = (TermTree<?>) featu;
					Element elChartree = document.createElement(
							CHARACTER_TREE);
					chartreeCount = buildReference(featu, featuretrees, ID,
							elChartree, "ct", chartreeCount);
					buildRepresentation(elChartree, ft);
					elChartrees.appendChild(elChartree);
					Element elNodes = document.createElement(NODES);
					elChartree.appendChild(elNodes);
					List<TermNode> roots = ft.getRootChildren();
					for (Iterator<TermNode> fn = roots.iterator(); fn
							.hasNext();) {
						TermNode featureNode = fn.next();
						buildBranches(featureNode, elNodes, true);
					}
				}
			}
			dataset.appendChild(elChartrees);
		}
	}

	public void buildClassifications(Element dataset) throws ParseException {

		if (cdmSource.getTaxa() != null) {
			Element elTaxonHierarchies = document.createElement(
					"TaxonHierarchies");
			Element elTaxonHierarchy = document.createElement(
					"TaxonHierarchy");
			for (Iterator<? extends TaxonBase> tb = cdmSource.getTaxa()
					.iterator(); tb.hasNext();) {
				Taxon taxon = (Taxon) tb.next();
				if (taxon.getTaxonNodes() != null) {
					for (Iterator<TaxonNode> tn = taxon.getTaxonNodes()
							.iterator(); tn.hasNext();) {
						TaxonNode taxonnode = tn.next();
						if (taxonnode.isTopmostNode()) {
							Element elNode = document.createElement("Node");
							taxonNodesCount = buildReference(taxonnode,
									taxonNodes, ID, elNode, "tn", taxonNodesCount);
							Element elTaxonName = document.createElement(TAXON_NAME);
							taxonNamesCount = buildReference(taxonnode.getTaxon().getName(),
									taxonNames, REF, elTaxonName, "t", taxonNamesCount);
							elNode.appendChild(elTaxonName);
							elTaxonHierarchy.appendChild(elNode);
							if (taxonnode.hasChildNodes()) {buildTaxonBranches(
									taxonnode.getChildNodes(), taxonnode, elTaxonHierarchy);
							}
						}
					}
				}
			}
			elTaxonHierarchies.appendChild(elTaxonHierarchy);
			dataset.appendChild(elTaxonHierarchies);
		}
	}

	private void buildTaxonBranches(List<TaxonNode> children, TaxonNode parent,
			Element elTaxonHierarchy) throws ParseException {
		if (children != null) {
			for (Iterator<TaxonNode> tn = children.iterator(); tn.hasNext();) {
				TaxonNode taxonnode = tn.next();
				Element elNode = document.createElement("Node");
				Element elParent = document.createElement(PARENT);
				Element elTaxonName = document.createElement(TAXON_NAME);
				if (taxonnode.hasChildNodes()) {
					buildTaxonBranches(taxonnode.getChildNodes(), taxonnode,
							elTaxonHierarchy);
				}
				taxonNodesCount = buildReference(taxonnode, taxonNodes, ID,
						elNode, "tn", taxonNodesCount);
				taxonNodesCount = buildReference(parent, taxonNodes, REF,
						elParent, "tn", taxonNodesCount);
				taxonNamesCount = buildReference(
						taxonnode.getTaxon().getName(), taxonNames, REF,
						elTaxonName, "t", taxonNamesCount);
				elNode.appendChild(elParent);
				elNode.appendChild(elTaxonName);
				elTaxonHierarchy.appendChild(elNode);
			}
		}
	}

	public void buildBranches(TermNode<Feature> parent, Element element,
			boolean isRoot) throws ParseException {

	    List<TermNode<Feature>> children = parent.getChildNodes();
		if (!parent.isLeaf()) {
			Element elCharNode = document.createElement(NODE);
			charnodeCount = buildReference(parent, featuretrees, ID,
					elCharNode, "cn", charnodeCount);
			TermNode<Feature> grandparent = parent.getParent();
			if ((grandparent != null) && (!isRoot)) {
				Element elParent = document.createElement(PARENT);
				charnodeCount = buildReference(grandparent, featuretrees, REF,
						elParent, "cn", charnodeCount);
				elCharNode.appendChild(elParent);
			}
			Element elDescriptiveConcept = document.createElement(
					DESCRIPTIVE_CONCEPT);
			Feature fref = parent.getTerm();
			descriptiveConceptCount = buildReference(fref, descriptiveConcepts,
					REF, elDescriptiveConcept, "dc", descriptiveConceptCount);
			elCharNode.appendChild(elDescriptiveConcept);
			element.appendChild(elCharNode);
			for (Iterator<TermNode<Feature>> ifn = children.iterator(); ifn.hasNext();) {
				TermNode<Feature> fn = ifn.next();
				buildBranches(fn, element, false);
			}
		} else {
			Element elCharNode = document.createElement(CHAR_NODE);
			Element elParent = document.createElement(PARENT);
			TermNode<?> grandparent = parent.getParent();
			charnodeCount = buildReference(grandparent, featuretrees, REF,
					elParent, "cn", charnodeCount);
			charnodeCount = buildReference(parent, featuretrees, ID,
					elCharNode, "cn", charnodeCount);
			Element elCharacter = document.createElement(CHARACTER);
			Feature fref = parent.getTerm();
			boolean dependencies = false;
			Element elDependecyRules = document.createElement(
					"DependecyRules");
			if (parent.getInapplicableIf() != null) {
				Set<FeatureState> innaplicableIf = parent.getInapplicableIf();
				Element elInnaplicableIf = document.createElement("InapplicableIf");
				for (FeatureState featureState : innaplicableIf) {
				    DefinedTermBase<?> state = featureState.getState();
					Element elState = document.createElement(STATE);
					buildReference(CdmBase.deproxy(state, DefinedTermBase.class), states, REF, elState, "State",
							statesCount);
					elInnaplicableIf.appendChild(elState);
				}
				elDependecyRules.appendChild(elInnaplicableIf);
				dependencies = true;
			}
			if (parent.getOnlyApplicableIf() != null) {
				Set<FeatureState> onlyApplicableIf = parent.getOnlyApplicableIf();
				Element elOnlyApplicableIf = document.createElement(
						"OnlyApplicableIf");
				for (FeatureState featureState : onlyApplicableIf) {
					Element elState = document.createElement(STATE);
					DefinedTermBase<?> state = featureState.getState();
                    buildReference(CdmBase.deproxy(state, DefinedTermBase.class), states, REF, elState, "State",
							statesCount);
					elOnlyApplicableIf.appendChild(elState);
				}
				elDependecyRules.appendChild(elOnlyApplicableIf);
				dependencies = true;
			}
			if (dependencies == true) {
                elCharNode.appendChild(elDependecyRules);
            }
			charactersCount = buildReference(fref, characters, REF,
					elCharacter, "c", charactersCount);
			elCharNode.appendChild(elCharacter);
			elCharNode.appendChild(elParent);
			element.appendChild(elCharNode);
		}
	}

	public void buildMediaObjects(Element dataset) throws ParseException {

		if (cdmSource.getMedia() != null) {
			Element elMediaObjects = document.createElement(
					MEDIA_OBJECTS);

			for (int i = 0; i < cdmSource.getMedia().size(); i++) {
				Element elMediaObject = document.createElement(
						MEDIA_OBJECT);
				Media mediobj = (Media) cdmSource.getMedia().get(i);
				mediasCount = buildReference(mediobj, medias, ID,
						elMediaObject, "t", mediasCount);
				buildRepresentation(elMediaObject, mediobj);
				Set<MediaRepresentation> smr = mediobj.getRepresentations();
				for (Iterator<MediaRepresentation> mr = smr.iterator(); mr
						.hasNext();) {
					MediaRepresentation mediarep = mr.next();
					Element elType = document.createElement("Type");
					elType.appendChild(document.createTextNode(mediarep
							.getMimeType()));
					elMediaObject.appendChild(elType);
					List<MediaRepresentationPart> lmrp = mediarep.getParts();
					for (Iterator<MediaRepresentationPart> mrp = lmrp
							.iterator(); mrp.hasNext();) {
						MediaRepresentationPart mediareppart = mrp.next();
						Element elSource = document.createElement(
								"Source");
						elSource.setAttribute("href", mediareppart.getUri()
								.toString());
						elMediaObject.appendChild(elSource);
					}
				}
				elMediaObjects.appendChild(elMediaObject);
			}
			dataset.appendChild(elMediaObjects);
		}
	}

	public void buildPublications(Element dataset) throws ParseException {

		if (cdmSource.getReferences() != null) {
			Element elPublications = document.createElement(PUBLICATIONS);
			boolean editorial = false;
			for (int i = 0; i < cdmSource.getReferences().size(); i++) {
				Element elPublication = document.createElement(
						"Publication");
				Reference publication = cdmSource.getReferences().get(i);
				Set<Annotation> annotations = publication.getAnnotations();
				for (Iterator<Annotation> a = annotations.iterator(); a
						.hasNext();) {
					Annotation annotation = a.next();
					AnnotationType annotationType = annotation
							.getAnnotationType();
					if (annotationType != null && annotationType.equals(AnnotationType.EDITORIAL())) {
						editorial = true;
					} else {
						editorial = false;
					}
				}
				if (!editorial) {
					articlesCount = buildReference(publication, articles, ID,
							elPublication, "p", articlesCount);
					buildRepresentation(elPublication, (IDatabase) publication);
					elPublications.appendChild(elPublication);
				}
			}
			dataset.appendChild(elPublications);
		}
	}

	public int buildReference(VersionableEntity ve, Map references,
			String refOrId, Element element, String prefix, int count) {

	    if (references.containsKey(ve)) {
			element.setAttribute(refOrId, (String) references.get(ve));
		} else {
			if (ve instanceof IdentifiableEntity) {
				IdentifiableEntity<?> ie = (IdentifiableEntity<?>) ve;
				if (ie.getSources().size() > 0) {
					IdentifiableSource os = (IdentifiableSource) ie
							.getSources().toArray()[0];
					String id = os.getIdInSource();
					String uri = os.getCitationMicroReference();
					if (uri != null) {
						element.setAttribute(URI, uri);
					}
					if (id != null) {
						if (!id.equals("")) {
							if (!references.containsValue(id)) {
								element.setAttribute(refOrId, id);
							} else {
                                while (element.getAttribute(refOrId).equals("")) {
									if (!references.containsValue(prefix
											+ (count + 1))) {
										element.setAttribute(refOrId, prefix
												+ (count + 1));
									}
									count++;
								}
                            }
						} else {
                            while (element.getAttribute(refOrId).equals("")) {
								if (!references.containsValue(prefix
										+ (count + 1))) {
									element.setAttribute(refOrId, prefix
											+ (count + 1));
								}
								count++;
							}
                        }
					} else {
                        while (element.getAttribute(refOrId).equals("")) {
							if (!references.containsValue(prefix + (count + 1))) {
								element.setAttribute(refOrId, prefix
										+ (count + 1));
							}
							count++;
						}
                    }
				} else {
                    while (element.getAttribute(refOrId).equals("")) {
						if (!references.containsValue(prefix + (count + 1))) {
							element.setAttribute(refOrId, prefix + (count + 1));
						}
						count++;
					}
                }
			} else {
                while (element.getAttribute(refOrId).equals("")) {
					if (!references.containsValue(prefix + (count + 1))) {
						element.setAttribute(refOrId, prefix + (count + 1));
					}
					count++;
				}
            }
			references.put(ve, element.getAttribute(refOrId));
		}
		return count;
	}

	public void buildGeographicAreas(Element dataset) throws ParseException {
		if (cdmSource.getTerms() != null) {
			Element elGeographicAreas = document.createElement(
					"GeographicAreas");

			int f = cdmSource.getTerms().size();
			for (int i = 0; i < f; i++) {
				if (cdmSource.getTerms().get(i) instanceof NamedArea) {
					NamedArea na = (NamedArea) cdmSource.getTerms().get(i);
					for (Iterator<Marker> mark = na.getMarkers().iterator(); mark
							.hasNext();) {
						Marker marker = mark.next();
						if (marker.getMarkerType().getLabel()
								.equals("SDDGeographicArea")) {
							Element elGeographicArea = document.createElement(
							        "GeographicArea");
							namedAreasCount = buildReference(na, namedAreas,
									ID, elGeographicArea, "a", namedAreasCount);
							buildRepresentation(elGeographicArea, na);
							elGeographicAreas.appendChild(elGeographicArea);
						}
					}
				}
			}
			dataset.appendChild(elGeographicAreas);
		}
	}

	public void buildSpecimens(Element dataset) throws ParseException {

		if (cdmSource.getOccurrences() != null) {
			Element elSpecimens = document.createElement("Specimens");

			for (int i = 0; i < cdmSource.getOccurrences().size(); i++) {
				Element elSpecimen = document.createElement("Specimen");
				SpecimenOrObservationBase<?> sob = cdmSource.getOccurrences().get(i);
				if (sob.getRecordBasis().isPreservedSpecimen()) {
					specimenCount = buildReference(sob, specimens, ID, elSpecimen, "s", specimenCount);
					buildRepresentation(elSpecimen, sob);
					elSpecimens.appendChild(elSpecimen);
				}
			}
			dataset.appendChild(elSpecimens);
		}

	}

}
