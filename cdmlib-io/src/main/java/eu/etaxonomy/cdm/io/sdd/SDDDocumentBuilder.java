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

import org.apache.xerces.impl.xpath.regex.ParseException;
import org.apache.xml.serialize.DOMSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import eu.etaxonomy.cdm.io.jaxb.CdmMarshallerListener;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.AgentBase;
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
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.Modifier;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.IDatabase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

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
	private Map<ReferenceBase, String> articles = new HashMap<ReferenceBase, String>();
	private Map<VersionableEntity, String> featuretrees = new HashMap<VersionableEntity, String>();
	private Map<Modifier, String> modifiers = new HashMap<Modifier, String>();
	private Map<TaxonNode, String> taxonNodes = new HashMap<TaxonNode,String>();
	private Map<NamedArea, String> namedAreas = new HashMap<NamedArea,String>();
	private Map<Specimen, String> specimens = new HashMap<Specimen,String>();
	private ReferenceFactory refFactory = ReferenceFactory.newInstance();
	
	private Map<VersionableEntity,String> features = new HashMap<VersionableEntity,String>();
	private int agentsCount = 0;
	private int articlesCount = 0;
	private int codedDescriptionsCount = 0;
	private int taxonNamesCount = 0;
	private int charactersCount = 0;
	private int textcharactersCount = 0;
	private int mediasCount = 0;
	private int statesCount = 0;
	private int featuresCount = 0;
	private int chartreeCount = 0;
	private int charnodeCount = 0;
	private int taxonNodesCount = 0;
	private int namedAreasCount = 0;
	private int specimenCount = 0;
	
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
	private String PUBLICATIONS = "Publications";
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
	private String TEXT = "text";
	private String TEXT_CHAR = "TextChar";
	private String TEXT_CHARACTER = "TextCharacter";
	private String TYPE = "Type";
	private String URI = "uri";

	private Language defaultLanguage = Language.DEFAULT();

	private static final Logger logger = Logger.getLogger(SDDDocumentBuilder.class);
	
	private boolean natlang = true;
	private String NEWLINE = System.getProperty("line.separator");
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
		if (natlang) {
			buildNaturalLanguageDescription(baselement);
		}
		else {
		baselement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		baselement.setAttribute("xmlns", "http://rs.tdwg.org/UBIF/2006/");
		baselement.setAttribute("xsi:schemaLocation", "http://rs.tdwg.org/UBIF/2006 http://rs.tdwg.org/UBIF/2006/Schema/1.1/SDD.xsd");

		buildTechnicalMetadata(baselement);

		List<ReferenceBase> references = cdmSource.getReferences();
		Iterator<ReferenceBase> iterator = references.iterator();
		IDatabase d = refFactory.newDatabase();
		while (iterator.hasNext()) {
			ReferenceBase reference = (ReferenceBase) iterator.next();
			if (reference.getType().equals(ReferenceType.Database)) {
				buildDataset(baselement, reference);
			}
		}
		}

		// for datasets with no Database ReferenceBase
		// buildDataset(baselement, cdmSource, null);

		//append the root element to the DOM document
		document.appendChild(baselement);
	}
	
	public void buildNaturalLanguageDescription(ElementImpl dataset) {
		if (cdmSource.getTaxa() != null) {
			ElementImpl elNatLang = new ElementImpl(document, "NaturalLanguageDescription");
			StringBuilder natlangdesc = new StringBuilder();
			
			for (Iterator<? extends TaxonBase> tb = cdmSource.getTaxa().iterator() ; tb.hasNext() ;){
				Taxon taxon = (Taxon) tb.next();
				if (taxon.generateTitle().contains("Podosperm")) {
					Set<TaxonDescription> descriptions = taxon.getDescriptions();
					buildCharacterTreesDescr(natlangdesc, dataset,descriptions);
					
				}
			/*
			buildCharacterTreesDescr(natlangdesc, dataset);
			
			for (Iterator<? extends TaxonBase> tb = cdmSource.getTaxa().iterator() ; tb.hasNext() ;){
				Taxon taxon = (Taxon) tb.next();
				if (taxon.generateTitle().contains("Podosperm")) {
				natlangdesc.append(taxon.generateTitle());
				Set<TaxonDescription> descriptions = taxon.getDescriptions();
				for (Iterator<TaxonDescription> td = descriptions.iterator() ; td.hasNext() ;){
					StringBuilder taxondesc = new StringBuilder();
					TaxonDescription taxonDescription = td.next();
					Set<DescriptionElementBase> elements = taxonDescription.getElements();
					for (Iterator<DescriptionElementBase> deb = elements.iterator() ; deb.hasNext() ;){
						DescriptionElementBase descriptionElement = deb.next();
						if (descriptionElement instanceof CategoricalData) {
							CategoricalData categoricalData = (CategoricalData) descriptionElement;
							buildCategoricalDescr(taxondesc, categoricalData);
							taxondesc.append(NEWLINE);
						}
						if (descriptionElement instanceof QuantitativeData) {
							QuantitativeData quantitativeData = (QuantitativeData) descriptionElement;
							buildQuantitativeDescr(taxondesc, quantitativeData);
							taxondesc.append(NEWLINE);
						}
						if (descriptionElement instanceof TextData) {
							TextData textData = (TextData) descriptionElement;
							//buildTextChar(summaryData, textData);
						}
					}
					natlangdesc.append(taxondesc);
				}
				}*/
			}
			buildLabel(elNatLang,natlangdesc.toString());
			dataset.appendChild(elNatLang);
		}
		
		
	}
	
	public void buildCharacterTreesDescr(StringBuilder description, ElementImpl dataset, Set<TaxonDescription> descriptions) throws ParseException {

		if (cdmSource.getFeatureData() != null) {

			for (int i = 0; i < cdmSource.getFeatureData().size(); i++) {
				VersionableEntity featu = cdmSource.getFeatureData().get(i);
				if (featu instanceof FeatureTree){
					FeatureTree ft = (FeatureTree) featu;
					if (ft.getLabel().contains("natural language")) {
						description.append(ft.getLabel() + NEWLINE);
						List<FeatureNode> children = ft.getRootChildren();
						buildBranchesDescr(description, children,ft.getRoot(), descriptions);
						description.append(" (end of tree) ");
					}
				}
			}
		}
	}
	
	public void buildBranchesDescr(StringBuilder description, List<FeatureNode> children, FeatureNode parent, Set<TaxonDescription> descriptions) {
		StringBuilder branchDescription = new StringBuilder();
		if (!parent.isLeaf()){
			Feature fref = parent.getFeature();
			if (fref!=null) {
				branchDescription.append(" " + fref.getLabel());
			}
			else {
				branchDescription.append(" (nullFeature) ");
			}
			for (Iterator<FeatureNode> ifn = children.iterator() ; ifn.hasNext() ;){
				FeatureNode fn = ifn.next();
				buildBranchesDescr(branchDescription, fn.getChildren(),fn,descriptions);
				//branchDescription.append(" ; "+NEWLINE);
			}
		}
		else {
			Feature fref = parent.getFeature();
			if (fref!=null) {
				for (Iterator<TaxonDescription> td = descriptions.iterator() ; td.hasNext() ;){
					TaxonDescription taxonDescription = td.next();
					Set<DescriptionElementBase> elements = taxonDescription.getElements();
					for (Iterator<DescriptionElementBase> deb = elements.iterator() ; deb.hasNext() ;){
						DescriptionElementBase descriptionElement = deb.next();
						if (descriptionElement.getFeature().getLabel().equals(fref.getLabel())){
							if (descriptionElement instanceof CategoricalData) {
								CategoricalData categoricalData = (CategoricalData) descriptionElement;
								buildCategoricalDescr(branchDescription, categoricalData);
								branchDescription.append(NEWLINE);
							}
							if (descriptionElement instanceof QuantitativeData) {
								QuantitativeData quantitativeData = (QuantitativeData) descriptionElement;
								buildQuantitativeDescr(branchDescription, quantitativeData);
								branchDescription.append(NEWLINE);
							}
						}
					}
				}
			}
			/*if ((fref!=null)&&(fref.isSupportsCategoricalData())) {
				Set<TermVocabulary<State>> tvs = fref.getSupportedCategoricalEnumerations();
				for (Iterator<TermVocabulary<State>> tv = tvs.iterator() ; tv.hasNext() ;) {
					TermVocabulary<State> termVoc = tv.next();
					Set<State> states = termVoc.getTerms();
					for (Iterator<State> s = states.iterator() ; s.hasNext() ;) {
						State state = s.next() ;
						branchDescription.append(" " + (state.getLabel()) +", ");
					}
				}
			}
			else if (fref.isSupportsQuantitativeData()) {
				Set<StatisticalMeasure> sms = fref.getRecommendedStatisticalMeasures() ;
				for (Iterator<StatisticalMeasure> sm = sms.iterator() ; sm.hasNext() ;) {
					StatisticalMeasure statisticalMeasure = sm.next();
					Set<State> states = termVoc.getTerms();
					for (Iterator<State> s = states.iterator() ; s.hasNext() ;) {
						State state = s.next() ;
						branchDescription.append(" " + (state.getLabel()) +", ");
					}
				}
			}*/
			}
		description.append(branchDescription);
	}
	
	
	
	public void buildQuantitativeDescr (StringBuilder description, QuantitativeData quantitativeData) throws ParseException {

		boolean average = false;
		float averagevalue = new Float(0);
		boolean sd = false;
		float sdvalue = new Float(0);
		boolean min = false;
		float minvalue = new Float(0);
		boolean max = false;
		float maxvalue = new Float(0);
		boolean lowerb = false;
		float lowerbvalue = new Float(0);
		boolean upperb = false;
		float upperbvalue = new Float(0);
		
		StringBuilder QuantitativeDescription = new StringBuilder();
		Feature feature = quantitativeData.getFeature();
		QuantitativeDescription.append(" "+feature.getLabel());
		String unit = quantitativeData.getUnit().getLabel();
		Set<StatisticalMeasurementValue> statisticalValues = quantitativeData.getStatisticalValues();
		for (Iterator<StatisticalMeasurementValue> smv = statisticalValues.iterator() ; smv.hasNext() ;){
			StatisticalMeasurementValue statisticalValue = smv.next();
			StatisticalMeasure type = statisticalValue.getType();
			String label = type.getLabel();
			if (label.equals("Average")) {
				average = true;
				averagevalue = statisticalValue.getValue();
			} else if (label.equals("StandardDeviation")) {
				sd = true;
				sdvalue = statisticalValue.getValue();
			} else if (label.equals("Min")) {
				min = true;
				minvalue = statisticalValue.getValue();
			} else if (label.equals("Max")) {
				max = true;
				maxvalue = statisticalValue.getValue();
			} else if (label.equals("TypicalLowerBoundary")) {
				lowerb = true;
				lowerbvalue = statisticalValue.getValue();
			} else if (label.equals("TypicalUpperBoundary")) {
				upperb = true;
				upperbvalue = statisticalValue.getValue();
			}
		}
		if (max && min) {
			QuantitativeDescription.append(" from " + minvalue + " to " + maxvalue + unit);
		}
		else if (min) {
			QuantitativeDescription.append(" from " + minvalue + " " + unit);
		}
		else if (max) {
			QuantitativeDescription.append(" up to " + maxvalue + " " + unit);
		}
		if ((max||min)&&(lowerb||upperb)) {
			QuantitativeDescription.append(",");
		}
		if ((lowerb||upperb)&&(min||max)) {
			QuantitativeDescription.append(" most frequently");
		}
		if (upperb && lowerb) {
			QuantitativeDescription.append(" from " + lowerbvalue + " to " + upperbvalue + unit);
		}
		else if (lowerb) {
			QuantitativeDescription.append(" from " + lowerbvalue + " " + unit);
		}
		else if (upperb) {
			QuantitativeDescription.append(" up to " + upperbvalue + " " + unit);
		}
		if (((max||min)&&(average))||((lowerb||upperb)&&(average))) {
			QuantitativeDescription.append(",");
		}
		if (average) {
			QuantitativeDescription.append(" " + averagevalue + unit + " on average ");
			if (sd) {
				QuantitativeDescription.append("(+/- " + sdvalue + ")");
			}
		}
		description.append(QuantitativeDescription).append(" ; ");
	}
	
	public void buildCategoricalDescr(StringBuilder description, CategoricalData categoricalData) throws ParseException {

		Feature feature = categoricalData.getFeature();
		List<StateData> states = categoricalData.getStates();
		StringBuilder CategoricalDescription = new StringBuilder();
		CategoricalDescription.append(" "+feature.getLabel());
		for (Iterator<StateData> sd = states.iterator() ; sd.hasNext() ;){
			StateData stateData = sd.next();
			State s = stateData.getState();
			CategoricalDescription.append(" " + s.getLabel());
			//System.out.println(s.getLabel());
		}
		description.append(CategoricalDescription).append(" ; ");
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
		IDatabase d = refFactory.newDatabase();
		while ((iterator.hasNext()) && (!database)) {
			ReferenceBase reference = (ReferenceBase) iterator.next();
			if (reference.getType().equals(ReferenceType.Database)) {
				d = reference;
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
	public void buildDataset(ElementImpl baselement,IDatabase reference) throws ParseException {
		// create Dataset and language
		ElementImpl dataset = new ElementImpl(document, DATASET);
		// no default language associated with a dataset in the CDM
		dataset.setAttribute("xml:lang", Language.DEFAULT().getIso639_1());
		baselement.appendChild(dataset);
		buildRepresentation(dataset, reference);
		buildRevisionData(dataset, reference);
		buildIPRStatements(dataset, reference);
		buildTaxonNames(dataset);
		buildDescriptiveConcepts(dataset);
		buildCharacters(dataset);
		buildCodedDescriptions(dataset);
		buildAgents(dataset);
		buildPublications(dataset);
		buildMediaObjects(dataset);
		buildCharacterTrees(dataset);
		buildTaxonomicTrees(dataset);
		buildGeographicAreas(dataset);
		buildSpecimens(dataset);
	}

	/**
	 * Builds a Representation element using a ReferenceBase
	 */
	public void buildRepresentation(ElementImpl element, IDatabase reference) throws ParseException {

		//			create <Representation> element
		ElementImpl representation = new ElementImpl(document, REPRESENTATION);
		element.appendChild(representation);
		buildLabel(representation, reference.getTitleCache());

		Set<Annotation> annotations = ((ReferenceBase)reference).getAnnotations();
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

		Set<Media> rm = ((ReferenceBase)reference).getMedia();

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
		
		if (ie instanceof DefinedTermBase) {
			DefinedTermBase dtb = (DefinedTermBase) ie;
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
		//XIM
		if (ie instanceof IdentifiableMediaEntity) {
			IdentifiableMediaEntity ime = (IdentifiableMediaEntity) ie;
			Set<Media> medias = ime.getMedia();
			if (medias != null){
				ElementImpl elLinks = new ElementImpl(document, "Links");
				for (Iterator<Media> m = medias.iterator() ; m.hasNext() ;){
					Media media = m.next();
					Set<MediaRepresentation> smr = media.getRepresentations();
					for (Iterator<MediaRepresentation> mr = smr.iterator() ; mr.hasNext();){
						MediaRepresentation mediarep = mr.next();
						List<MediaRepresentationPart> lmrp = mediarep.getParts();
						for (Iterator<MediaRepresentationPart> mrp = lmrp.iterator();mrp.hasNext();){
							MediaRepresentationPart mediareppart = mrp.next();
							ElementImpl elLink = new ElementImpl(document, "Link");
							elLink.setAttribute("href",mediareppart.getUri());
							elLinks.appendChild(elLink);
						}
					}
				}
				element.appendChild(elLinks);
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

				taxonNamesCount = buildReference(tnb, taxonNames, ID, elTaxonName, "t", taxonNamesCount);
				//elTaxonName.setAttribute(URI,"http://www.google.com");

				buildRepresentation(elTaxonName, tnb);

				elTaxonNames.appendChild(elTaxonName);
			}

			dataset.appendChild(elTaxonNames);
		}

	}
	
	public void buildSpecimens(ElementImpl dataset) throws ParseException {

		if (cdmSource.getOccurrences() != null) {
			ElementImpl elSpecimens = new ElementImpl(document, "Specimens");

			for (int i = 0; i < cdmSource.getOccurrences().size(); i++) {
				ElementImpl elSpecimen = new ElementImpl(document, "Specimen");
				SpecimenOrObservationBase sob = cdmSource.getOccurrences().get(i);
				if (sob instanceof Specimen){
				specimenCount = buildReference(sob, specimens, ID, elSpecimen, "s", specimenCount);
				//elTaxonName.setAttribute(URI,"http://www.google.com");

				buildRepresentation(elSpecimen, sob);

				elSpecimens.appendChild(elSpecimen);
				}
			}

			dataset.appendChild(elSpecimens);
		}

	}
	
	
	public void buildGeographicAreas(ElementImpl dataset) {
			if (cdmSource.getTerms() != null) {
				ElementImpl elGeographicAreas = new ElementImpl(document, "GeographicAreas");

				int f = cdmSource.getTerms().size();
				for (int i = 0; i < f; i++) {
						if (cdmSource.getTerms().get(i) instanceof NamedArea) {
							NamedArea na = (NamedArea) cdmSource.getTerms().get(i);
							for (Iterator<Marker> mark = na.getMarkers().iterator() ; mark.hasNext();) {
								Marker marker = mark.next();
								if (marker.getMarkerType().getLabel().equals("SDDGeographicArea")) {
									ElementImpl elGeographicArea = new ElementImpl(document, "GeographicArea");
									namedAreasCount = buildReference(na, namedAreas, ID, elGeographicArea, "a", namedAreasCount);
									buildRepresentation(elGeographicArea,na);
									System.out.println(na.getDescription());
									elGeographicAreas.appendChild(elGeographicArea);
								}
							}
							
					}
				}
				dataset.appendChild(elGeographicAreas);
		}
	}
	
	
	

	/**
	 * Builds an element Agent referring to Agent defined later in the SDD file
	 */
	public void buildRefAgent(ElementImpl element, TeamOrPersonBase ag, String role) throws ParseException {
		if (ag instanceof Person) {
			Person p = (Person) ag;
			ElementImpl agent = new ElementImpl(document, AGENT);
			if (ag.getMarkers()!= null){
				Set<Marker> markers = ag.getMarkers();
				for(Iterator<Marker> m = markers.iterator() ; m.hasNext();){
					Marker marker = m.next();
					if (marker.getMarkerType().getLabel().equals("editor")){
						agent.setAttribute(ROLE, "edt");
					}
				}
			}
			else {
				agent.setAttribute(ROLE, role);
			}
			agentsCount = buildReference(p, agents, REF, agent, "a", agentsCount);
			element.appendChild(agent);
		}

		if (ag instanceof Team) {
			Team team = (Team) ag;
			for (int i = 0; i < team.getTeamMembers().size(); i++) {
				Person author = team.getTeamMembers().get(i);
				ElementImpl agent = new ElementImpl(document, AGENT);
				if (author.getMarkers()!= null){
					Set<Marker> markers = author.getMarkers();
					if (!markers.isEmpty()){
						for(Iterator<Marker> m = markers.iterator() ; m.hasNext();){
							Marker marker = m.next();
							if (marker.getMarkerType().getLabel().equals("editor")){
								agent.setAttribute(ROLE, "edt");
							}
						}
					}
						else {
							agent.setAttribute(ROLE, role);
						}
				}
				else {
					agent.setAttribute(ROLE, role);
				}
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
	public void buildDateModified(ElementImpl revisionData, IDatabase database) throws ParseException {

		//  <DateModified>2006-04-08T00:00:00</DateModified>

		if (((ReferenceBase)database).getUpdated() != null) {
			ElementImpl dateModified = new ElementImpl(document, DATE_MODIFIED);

			DateTime c = ((ReferenceBase)database).getUpdated();
			DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

			String date = fmt.print(c);
			dateModified.appendChild(document.createTextNode(date));

			revisionData.appendChild(dateModified);
		}

	}

	/**
	 * Builds IPRStatements associated with the Dataset
	 */
	public void buildIPRStatements(ElementImpl dataset, IDatabase database) throws ParseException {

		// <IPRStatements>
		//  <IPRStatement role="Copyright">
		//    <Label xml:lang="en-au">(c) 2003-2006 Centre for Occasional Botany.</Label>
		//  </IPRStatement>
		// </IPRStatements>

		if (database.getRights() != null) {
			//create IPRStatements
			ElementImpl iprStatements = new ElementImpl(document, IPR_STATEMENTS);
			dataset.appendChild(iprStatements);

			//mapping between IPRStatement Copyright (SDD) and first Right in the list of Rights
			ElementImpl iprStatement = new ElementImpl(document, IPR_STATEMENT);
			iprStatement.setAttribute("role", "Copyright");
			iprStatements.appendChild(iprStatement);
			Set<Rights> rish = database.getRights();
			if (!database.getRights().isEmpty()) {
				buildLabel(iprStatement, ((Rights) database.getRights().toArray()[0]).getText());
				}
		}

	}

	/**
	 * Builds RevisionData associated with the Dataset
	 */
	public void buildRevisionData(ElementImpl dataset, IDatabase database) throws ParseException {

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
		//TeamOrPersonBase editors = database.getUpdatedBy();

		if ((authors != null)) { // || (editors != null)) {
			ElementImpl creators = new ElementImpl(document, CREATORS);
			if (authors != null) {
				buildRefAgent(creators, authors, "aut");
			}
//			if (editors != null) {
//						buildRefAgent(creators, editors, "edt");
//			}
			revisionData.appendChild(creators);
		}

		buildDateModified(revisionData, database);

		dataset.appendChild(revisionData);
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
					if (character.isSupportsQuantitativeData()) {
						ElementImpl elQuantitativeCharacter = new ElementImpl(document, QUANTITATIVE_CHARACTER);
						charactersCount = buildReference(character, characters, ID, elQuantitativeCharacter, "c", charactersCount);
						// TODO if the character also supports text, add to the label a short tag to distinguish
						// it as the quantitative version and create a unique label
						buildRepresentation(elQuantitativeCharacter, character);
						// TODO <MeasurementUnit> and <Default>
						elCharacters.appendChild(elQuantitativeCharacter);
					}

					if (character.isSupportsCategoricalData()) {
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
					if (character.isSupportsTextData()) {
						ElementImpl elTextCharacter = new ElementImpl(document, TEXT_CHARACTER);
						textcharactersCount = buildReference(character, characters, ID, elTextCharacter, TEXT, textcharactersCount);
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
	 * Builds Agents associated with the Dataset
	 */
	public void buildAgents(ElementImpl dataset) throws ParseException {

		if (cdmSource.getAgents() != null) {
			ElementImpl elAgents = new ElementImpl(document, AGENTS);

			for (int i = 0; i < cdmSource.getAgents().size(); i++) {
				ElementImpl elAgent = new ElementImpl(document, AGENT);
				AgentBase personagent = (AgentBase)cdmSource.getAgents().get(i);
				if (personagent  instanceof Person){
					if (personagent.getMarkers()!= null){
						Set<Marker> markers = personagent.getMarkers();
						for(Iterator<Marker> m = markers.iterator() ; m.hasNext();){
							Marker marker = m.next();
							if (marker.getMarkerType().getLabel() == "editor"){
								agentsCount = buildReference(personagent, agents, ID, elAgent, "a", agentsCount);
							}
						}
					}
				agentsCount = buildReference(personagent, agents, ID, elAgent, "a", agentsCount);
				//elTaxonName.setAttribute(URI,"http://www.google.com");

				buildRepresentation(elAgent, personagent);
				/*XIMSet<Media> medias = personagent.getMedia();
				if (medias != null){
					ElementImpl elLinks = new ElementImpl(document, "Links");
					for (Iterator<Media> m = medias.iterator() ; m.hasNext() ;){
						Media media = m.next();
						Set<MediaRepresentation> smr = media.getRepresentations();
						for (Iterator<MediaRepresentation> mr = smr.iterator() ; mr.hasNext();){
							MediaRepresentation mediarep = mr.next();
							List<MediaRepresentationPart> lmrp = mediarep.getParts();
							for (Iterator<MediaRepresentationPart> mrp = lmrp.iterator();mrp.hasNext();){
								MediaRepresentationPart mediareppart = mrp.next();
								ElementImpl elLink = new ElementImpl(document, "Link");
								elLink.setAttribute("href",mediareppart.getUri());
								elLinks.appendChild(elLink);
							}
						}
					}
					elAgent.appendChild(elLinks);
				}*/

				elAgents.appendChild(elAgent);
				}
			}

			dataset.appendChild(elAgents);
		}
	}
	
	/**
	 * XIM Pas traité si ne dépend pas d'un arbre ; Builds CharacterTrees associated with the Dataset
	 */
	public void buildCharacterTrees(ElementImpl dataset) throws ParseException {

		if (cdmSource.getFeatureData() != null) {
			ElementImpl elChartrees = new ElementImpl(document, CHARACTER_TREES);

			for (int i = 0; i < cdmSource.getFeatureData().size(); i++) {
				VersionableEntity featu = cdmSource.getFeatureData().get(i);
				if (featu instanceof FeatureTree){
					FeatureTree ft = (FeatureTree) featu;
					ElementImpl elChartree = new ElementImpl(document, CHARACTER_TREE);
					chartreeCount = buildReference(featu, featuretrees, ID, elChartree, "ct", chartreeCount);
					elChartrees.appendChild(elChartree);
					ElementImpl elNodes = new ElementImpl(document, NODES);
					elChartree.appendChild(elNodes);
					List<FeatureNode> children = ft.getRootChildren();
					buildBranches(children,ft.getRoot(),elNodes);					
				}
			}
			dataset.appendChild(elChartrees);
		}
	}
	
	public void buildTaxonomicTrees(ElementImpl dataset) throws ParseException {

		if (cdmSource.getTaxa() != null) {
			ElementImpl elTaxonHierarchies = new ElementImpl(document, "TaxonHierarchies");
			ElementImpl elTaxonHierarchy = new ElementImpl(document, "TaxonHierarchy");
			for (Iterator<? extends TaxonBase> tb = cdmSource.getTaxa().iterator() ; tb.hasNext() ;){
				Taxon taxon = (Taxon) tb.next();
				if (taxon.getTaxonNodes()!=null){
					for (Iterator<TaxonNode> tn = taxon.getTaxonNodes().iterator() ; tn.hasNext() ;){
						TaxonNode taxonnode = tn.next();
							if (taxonnode.isTopmostNode()) {
								ElementImpl elNode = new ElementImpl(document, "Node");
								taxonNodesCount = buildReference(taxonnode, taxonNodes, ID, elNode, "tn", taxonNodesCount);
								ElementImpl elTaxonName = new ElementImpl(document, TAXON_NAME);
								taxonNamesCount = buildReference(taxonnode.getTaxon().getName(), taxonNames, REF, elTaxonName, "t", taxonNamesCount);
								elNode.appendChild(elTaxonName);
								elTaxonHierarchy.appendChild(elNode);
								if (taxonnode.hasChildNodes()){
									buildTaxonBranches(taxonnode.getChildNodes(),taxonnode, elTaxonHierarchy);
								}
							}
					}
				}
			}
			elTaxonHierarchies.appendChild(elTaxonHierarchy);
			dataset.appendChild(elTaxonHierarchies);
		}
	}

	private void buildTaxonBranches(Set<TaxonNode> children, TaxonNode parent, ElementImpl elTaxonHierarchy){
		if (children != null){
			for (Iterator<TaxonNode> tn = children.iterator() ; tn.hasNext();){
				TaxonNode taxonnode = tn.next();
				ElementImpl elNode = new ElementImpl(document, "Node");
				ElementImpl elParent = new ElementImpl(document, PARENT);
				ElementImpl elTaxonName = new ElementImpl(document, TAXON_NAME);
				if (taxonnode.hasChildNodes()){
					buildTaxonBranches(taxonnode.getChildNodes(),taxonnode, elTaxonHierarchy);
				}
				taxonNodesCount = buildReference(taxonnode, taxonNodes, ID, elNode, "tn", taxonNodesCount);
				taxonNodesCount = buildReference(parent, taxonNodes, REF, elParent, "tn", taxonNodesCount);
				taxonNamesCount = buildReference(taxonnode.getTaxon().getName(), taxonNames, REF, elTaxonName, "t", taxonNamesCount);
				elNode.appendChild(elParent);
				elNode.appendChild(elTaxonName);
				elTaxonHierarchy.appendChild(elNode);
			}
		}
	}
	
	public void buildBranches(List<FeatureNode> children, FeatureNode parent, ElementImpl element) {
		if (!parent.isLeaf()){
			ElementImpl elCharNode = new ElementImpl(document, NODE);
			charnodeCount = buildReference(parent, featuretrees, ID, elCharNode, "cn", charnodeCount);
			FeatureNode grandparent = parent.getParent();
			if (grandparent !=null)	{
				ElementImpl elParent = new ElementImpl(document, PARENT);
				charnodeCount = buildReference(grandparent, featuretrees, REF, elParent, "cn", charnodeCount);
			}
			ElementImpl elDescriptiveConcept = new ElementImpl(document, DESCRIPTIVE_CONCEPT);
			Feature fref = parent.getFeature();
			featuresCount = buildReference(fref, characters, REF, elDescriptiveConcept, "dc", featuresCount);
			elCharNode.appendChild(elDescriptiveConcept);
			element.appendChild(elCharNode);
			for (Iterator<FeatureNode> ifn = children.iterator() ; ifn.hasNext() ;){
				FeatureNode fn = ifn.next();
				buildBranches(fn.getChildren(),fn,element);
			}
		}
		else {
			ElementImpl elCharNode = new ElementImpl(document, CHAR_NODE);
			ElementImpl elParent = new ElementImpl(document, PARENT);
			charnodeCount = buildReference(parent, featuretrees, REF, elParent, "cn", charnodeCount);
			ElementImpl elCharacter = new ElementImpl(document, CHARACTER);
			Feature fref = parent.getFeature();
			charactersCount = buildReference(fref, characters, REF, elCharacter, "c", charactersCount);
			elCharNode.appendChild(elCharacter);
			elCharNode.appendChild(elParent);
			element.appendChild(elCharNode);
			}
	}
	
	/**
	 * XIMBuilds DescriptiveConcepts associated with the Dataset
	 */
	public void buildDescriptiveConcepts(ElementImpl dataset) throws ParseException {

		if (cdmSource.getFeatureData() != null) {
			ElementImpl elFeatures = new ElementImpl(document, DESCRIPTIVE_CONCEPTS);
			
			for (int i = 0; i < cdmSource.getFeatureData().size(); i++) {
				ElementImpl elFeat = new ElementImpl(document, DESCRIPTIVE_CONCEPT);
				VersionableEntity featu = cdmSource.getFeatureData().get(i);
				if (featu instanceof FeatureNode)
				{
					FeatureNode fitou = (FeatureNode) featu;
					Feature fitounette = fitou.getFeature();
					featuresCount = buildReference(featu, characters, ID, elFeat, "dc", featuresCount);
					if (fitounette != null){
						buildRepresentation(elFeat, fitounette);
					}
					if (!fitou.isLeaf() && fitou.getChildCount() > 0){
						Set<TermVocabulary<Modifier>> stm = null;
						stm = fitounette.getRecommendedModifierEnumeration();
						if (stm != null){
							for (Iterator<TermVocabulary<Modifier>> m = stm.iterator() ; m.hasNext() ;){
								TermVocabulary<Modifier> tv = m.next();
								Set<Modifier> setmod = tv.getTerms();
								for (Iterator<Modifier> mm = setmod.iterator() ; mm.hasNext();){
									Modifier modi = mm.next();
									ElementImpl elModi = new ElementImpl(document,"modifier");
									featuresCount = buildReference(modi, modifiers, ID, elModi, "mod", featuresCount);
									elFeat.appendChild(elModi);
								}
							}
						}							
					}
					elFeatures.appendChild(elFeat);
				}
			}

			dataset.appendChild(elFeatures);
		}
	}
	
	
	
	/**
	 * XIMBuilds MediaObjects associated with the Dataset
	 */
	public void buildMediaObjects(ElementImpl dataset) throws ParseException {

		// <TaxonNames>
		//  <TaxonName id="t1" uri="urn:lsid:authority:namespace:my-own-id">
		//    <Representation>
		//      <Label xml:lang="la">Viola hederacea Labill.</Label>
		//    </Representation>
		//  </TaxonName>
		// </TaxonNames>

		if (cdmSource.getMedia() != null) {
			ElementImpl elMediaObjects = new ElementImpl(document, MEDIA_OBJECTS);

			for (int i = 0; i < cdmSource.getMedia().size(); i++) {
				ElementImpl elMediaObject = new ElementImpl(document, MEDIA_OBJECT);
				Media mediobj = (Media) cdmSource.getMedia().get(i);

				mediasCount = buildReference(mediobj, medias, ID, elMediaObject, "t", mediasCount);
				//elTaxonName.setAttribute(URI,"http://www.google.com");

				buildRepresentation(elMediaObject, mediobj);
				Set<MediaRepresentation> smr = mediobj.getRepresentations();
					for (Iterator<MediaRepresentation> mr = smr.iterator() ; mr.hasNext();){
						MediaRepresentation mediarep = mr.next();
						ElementImpl elType = new ElementImpl(document, "Type");
						elType.appendChild(document.createTextNode(mediarep.getMimeType()));
						elMediaObject.appendChild(elType);
						List<MediaRepresentationPart> lmrp = mediarep.getParts();
						for (Iterator<MediaRepresentationPart> mrp = lmrp.iterator();mrp.hasNext();){
								MediaRepresentationPart mediareppart = mrp.next();
								ElementImpl elSource = new ElementImpl(document, "Source");
								elSource.setAttribute("href",mediareppart.getUri());
								elMediaObject.appendChild(elSource);
							}
					}

				elMediaObjects.appendChild(elMediaObject);
			}

			dataset.appendChild(elMediaObjects);
		}

	}
	
	/**
	 * Builds Publications associated with the Dataset
	 */
	public void buildPublications(ElementImpl dataset) throws ParseException {

		if (cdmSource.getReferences() != null) {
			ElementImpl elPublications = new ElementImpl(document, PUBLICATIONS);
			boolean editorial = false;
			for (int i = 0; i < cdmSource.getReferences().size(); i++) {
				ElementImpl elPublication = new ElementImpl(document, "Publication");
				ReferenceBase publication = cdmSource.getReferences().get(i);
				//if (publication  instanceof Article){
					Set<Annotation> annotations = publication.getAnnotations();
					for (Iterator<Annotation> a = annotations.iterator() ; a.hasNext() ;){
						Annotation annotation = a.next();
						AnnotationType annotationType = annotation.getAnnotationType();
						if (annotationType == AnnotationType.EDITORIAL()) {
							editorial = true;
						}
						else {
							editorial = false;
						}
					}
					if (!editorial){
				articlesCount = buildReference(publication, articles, ID, elPublication, "p", articlesCount);
				buildRepresentation(elPublication, (IDatabase) publication);//XIM
				elPublications.appendChild(elPublication);
					}
				
				//}
			}
			dataset.appendChild(elPublications);
		}
	}

	
	
	/**
	 * Builds a Reference
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
					String uri = os.getCitationMicroReference();
					if (uri != null) {element.setAttribute(URI, uri);}
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
			if (descriptionSource.getType().equals(ReferenceType.Article)) {
				
				ElementImpl citation = new ElementImpl(document, CITATION);
				articlesCount = buildReference(descriptionSource, articles, REF, citation, "p", articlesCount);

				Set<Annotation> annotations = descriptionSource.getAnnotations();
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