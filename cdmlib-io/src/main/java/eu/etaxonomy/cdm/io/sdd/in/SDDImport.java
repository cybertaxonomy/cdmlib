/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.sdd.in;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.common.media.ImageInfo;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.ICdmImport;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.XmlImportBase;
import eu.etaxonomy.cdm.io.sdd.SDDTransformer;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.WorkingSet;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author h.fradin
 * @created 24.10.2008
 */
@Component("sddImport")
public class SDDImport extends XmlImportBase<SDDImportConfigurator, SDDImportState> implements ICdmImport<SDDImportConfigurator, SDDImportState> {
    private static final long serialVersionUID = 5492939941309574059L;

    private static final Logger logger = Logger.getLogger(SDDImport.class);

	private static int modCount = 1000;

    private Map<String,Person> authors = new HashMap<>();
    private Map<String,String> citations = new HashMap<>();
    private Map<String,String> defaultUnitPrefixes = new HashMap<>();
    private Map<String,Person> editors = new HashMap<>();
    private Map<String,FeatureNode> featureNodes = new HashMap<>();
    private Map<String,Feature> features = new HashMap<>();
    private Map<String,String> locations = new HashMap<>();
    private Map<String,List<CdmBase>> mediaObject_ListCdmBase = new HashMap<>();
    private Map<String,String> mediaObject_Role = new HashMap<>();
    private Map<String,Reference> publications = new HashMap<>();
    private Map<String,State> states = new HashMap<>();
    private Map<String,TaxonDescription> taxonDescriptions = new HashMap<>();
    private Map<String,NonViralName<?>> taxonNameBases = new HashMap<>();
    private Map<String,MeasurementUnit> units = new HashMap<>();
    private Map<String,TaxonNode> taxonNodes = new HashMap<>();
    private Map<String,NamedArea> namedAreas = new HashMap<>();
    private Map<String,DerivedUnit> specimens = new HashMap<>();
    private Map<String,DefinedTerm> modifiers = new HashMap<>();

	private Set<MarkerType> markerTypes = new HashSet<>();
	private Set<TermVocabulary<?>> vocabularies = new HashSet<>();

	private Set<Feature> descriptiveConcepts = new HashSet<>();
	private Set<AnnotationType> annotationTypes = new HashSet<>();
//	private Set<Feature> featureSet = new HashSet<Feature>();
	private Set<Reference> sources = new HashSet<>();
	private Reference sec = ReferenceFactory.newDatabase();
	private Reference sourceReference = null;

	private Language datasetLanguage = null;
	private WorkingSet workingSet = null;

	private final Namespace xmlNamespace = Namespace.getNamespace("xml","http://www.w3.org/XML/1998/namespace");

	private String generatorName = "";
	private String generatorVersion = "";


	private Set<StatisticalMeasure> statisticalMeasures = new HashSet<>();
	private Set<VersionableEntity> featureData = new HashSet<>();
	private Set<FeatureTree> featureTrees = new HashSet<>();
	private Set<Classification> classifications = new HashSet<>();

	private final UUID uuidAnnotationTypeLocation = UUID.fromString("a3737e07-72e3-46d2-986d-fa4cf5de0b63");
    private Rank defaultRank = Rank.UNKNOWN_RANK();  //TODO handle by configurator, better null?

	private Rights copyright = null;

	private int taxonNamesCount = 0;

	public SDDImport(){
		super();
	}

	private void init() {
	    authors = new HashMap<>();
	    citations = new HashMap<>();
	    defaultUnitPrefixes = new HashMap<>();
	    editors = new HashMap<>();
	    featureNodes = new HashMap<>();
	    features = new HashMap<>();
	    locations = new HashMap<>();
	    mediaObject_ListCdmBase = new HashMap<>();
	    mediaObject_Role = new HashMap<>();
	    publications = new HashMap<>();
	    states = new HashMap<>();
	    taxonDescriptions = new HashMap<>();
	    taxonNameBases = new HashMap<>();
	    units = new HashMap<>();
	    taxonNodes = new HashMap<>();
	    namedAreas = new HashMap<>();
	    specimens = new HashMap<>();
	    modifiers = new HashMap<>();

	    markerTypes = new HashSet<>();
	    vocabularies = new HashSet<>();

	    descriptiveConcepts = new HashSet<>();
	    annotationTypes = new HashSet<>();
	    sources = new HashSet<>();
	    statisticalMeasures = new HashSet<>();
	    featureData = new HashSet<>();
	    featureTrees = new HashSet<>();
	    classifications = new HashSet<>();
	}

	@Override
	public boolean doCheck(SDDImportState state){
		boolean result = true;
		logger.warn("No check implemented for SDD");
		return result;
	}

	@Override
	public void doInvoke(SDDImportState state){
	    init();
		TransactionStatus ts = startTransaction();
		SDDImportConfigurator sddConfig = state.getConfig();
		IProgressMonitor progressMonitor = sddConfig.getProgressMonitor();

		logger.info("start Datasets ...");

		// <Datasets xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://rs.tdwg.org/UBIF/2006/" xsi:schemaLocation="http://rs.tdwg.org/UBIF/2006/ ../SDD.xsd">
		Element root = sddConfig.getSourceRoot();
		Namespace sddNamespace = sddConfig.getSddNamespace();

		logger.info("start TechnicalMetadata ...");
		// <TechnicalMetadata created="2006-04-20T10:00:00">
		importTechnicalMetadata(root, sddNamespace, sddConfig);
		List<Element> elDatasets = root.getChildren("Dataset",sddNamespace);
//		int i = 0;

		//for each Dataset
		logger.info("start Dataset ...");
		progressMonitor.beginTask("Importing SDD data", elDatasets.size());
		for (Element elDataset : elDatasets){
			importDataset(elDataset, sddNamespace, state);
//			if ((++i % modCount) == 0){ logger.info("dataset(s) handled: " + i);}
//			logger.info(i + " dataset(s) handled");
			progressMonitor.worked(1);
		}
		commitTransaction(ts);
		progressMonitor.done();
		logger.info("End of transaction");
		return;
	}

	@Override
    protected boolean isIgnore(SDDImportState state){
		return false;
	}


	// associates the reference of a media object in SDD with a CdmBase Object
	protected void associateImageWithCdmBase(String refMO, CdmBase cb){
		if ((refMO != null) && (cb!=null)) {
			if (! refMO.equals("")) {
				if (! mediaObject_ListCdmBase.containsKey(refMO)) {
					List<CdmBase> lcb = new ArrayList<CdmBase>();
					lcb.add(cb);
					mediaObject_ListCdmBase.put(refMO,lcb);
				} else {
					List<CdmBase> lcb = mediaObject_ListCdmBase.get(refMO);
					lcb.add(cb);
					mediaObject_ListCdmBase.put(refMO,lcb);
				}
			}
		}
	}

	// imports information about the Dataset
	protected void importDatasetRepresentation(Element parent, Namespace sddNamespace){
		logger.info("start Representation ...");
		/* <Representation>
			<Label>The Genus Viola</Label>
			<Detail>This is an example for a very simple SDD file, representing a single description with categorical, quantitative, and text character. Compare also the "Fragment*" examples, which contain more complex examples in the form of document fragments. Intended for version="SDD 1.1".</Detail>
	       </Representation>
		 */



		Element elRepresentation = parent.getChild("Representation",sddNamespace);
		String label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);
		String detail = (String)ImportHelper.getXmlInputValue(elRepresentation, "Detail",sddNamespace);

		//new
		Representation representation = Representation.NewInstance(detail, label, null, datasetLanguage);
		workingSet.addRepresentation(representation);


		//old
//		sec.setTitleCache(label, true);
//
//		if (detail != null) {
//			Annotation annotation = Annotation.NewInstance(detail, datasetLanguage);
//			annotation.setAnnotationType(AnnotationType.EDITORIAL());
//			sec.addAnnotation(annotation);
//		}


		List<Element> listMediaObjects = elRepresentation.getChildren("MediaObject",sddNamespace);

		for (Element elMediaObject : listMediaObjects) {
			String ref = null;
			String role = null;
			if (elMediaObject != null) {
				ref = elMediaObject.getAttributeValue("ref");
				role = elMediaObject.getAttributeValue("role");
			}
			if (ref != null) {
				if (!ref.equals("")) {
					this.associateImageWithCdmBase(ref,sourceReference);
					this.associateImageWithCdmBase(ref,sec);
					mediaObject_Role.put(ref,role);
				}
			}
		}
	}

	// imports the representation (label, detail, lang) of a particular SDD element
	protected void importRepresentation(Element parent, Namespace sddNamespace, VersionableEntity ve, String id, SDDImportState state){
		Element elRepresentation = parent.getChild("Representation",sddNamespace);

		Map<Language,List<String>> langLabDet = new HashMap<Language,List<String>>();

		handleRepresentationLabels(sddNamespace, elRepresentation, langLabDet);
		handleRepresentationDetails(sddNamespace, elRepresentation, langLabDet);

		if (ve instanceof TermBase) {
			makeRepresentationForTerms((TermBase)ve, langLabDet);
		}else if (ve instanceof Media) {
			makeRepresentationForMedia((Media)ve, langLabDet);
		}else if (ve instanceof IdentifiableEntity<?>) {
			IdentifiableEntity<?> ie = (IdentifiableEntity<?>)ve;
			makeRepresentationForIdentifiableEntity(sddNamespace, ie, elRepresentation, langLabDet);
			if (ve instanceof IdentifiableMediaEntity<?>){
				makeRepresentationForIdentifiableMediaEntity(parent, sddNamespace, (IdentifiableMediaEntity<?>)ve);
			}
		}

		makeRepresentationMediaObjects(sddNamespace, ve, elRepresentation);//FIXME

	}


	/**
	 * Handles the "Detail" children of representations. Adds the result to the langLabDet.
	 * @param sddNamespace
	 * @param elRepresentation
	 * @param langLabDet
	 */
	private void handleRepresentationDetails(Namespace sddNamespace,
			Element elRepresentation, Map<Language, List<String>> langLabDet) {
		List<Element> listDetails = elRepresentation.getChildren("Detail",sddNamespace);
		for (Element elDetail : listDetails){
			Language language = getLanguage(elDetail);
			String role = elDetail.getAttributeValue("role");
			String detail = elDetail.getText();
			List<String> labDet = langLabDet.get(language);
			labDet.add(detail);
			labDet.add(role);
			langLabDet.put(language, labDet);
		}
	}

	/**
	 * Handles the "Label" children of representations. Adds the result to the langLabDet.
	 * @param sddNamespace
	 * @param elRepresentation
	 * @param langLabDet
	 */
	private void handleRepresentationLabels(Namespace sddNamespace,
				Element elRepresentation, Map<Language, List<String>> langLabDet) {
		// <Label xml:lang="la">Viola hederacea Labill.</Label>
		List<Element> listLabels = elRepresentation.getChildren("Label",sddNamespace);
		for (Element elLabel : listLabels){
			Language language = getLanguage(elLabel);
			String label = elLabel.getText();
			List<String> labDet = new ArrayList<String>(3);
			labDet.add(label);
			langLabDet.put(language, labDet);
		}
	}

	/**
	 *
	 * @param media
	 * @param langLabDet
	 */
	private void makeRepresentationForMedia(Media media, Map<Language, List<String>> langLabDet) {
		for (Language lang : langLabDet.keySet()){
			List<String> labDet = langLabDet.get(lang);
			if (labDet.get(0) != null){
				media.putTitle(LanguageString.NewInstance(labDet.get(0), lang));
			}
			if (labDet.size()>1) {
				media.putDescription(lang, labDet.get(1));
			}
		}
	}

	/**
	 * Handles representations for terms. Adds one representation per language in langLabDet.
	 *
	 * @param ve
	 * @param langLabDet
	 */
	private void makeRepresentationForTerms(TermBase tb, Map<Language, List<String>> langLabDet) {
			for (Language lang : langLabDet.keySet()){
				List<String> labDet = langLabDet.get(lang);
				if (labDet.size()>0){
					if (labDet.size()>1) {
						tb.addRepresentation(Representation.NewInstance(labDet.get(1), labDet.get(0), labDet.get(0), lang));
					} else {
						tb.addRepresentation(Representation.NewInstance(labDet.get(0), labDet.get(0), labDet.get(0), lang));
					}
				}
			}
	}


	/**
	 * Handles the "MediaObject" children of representations.
	 * @param sddNamespace
	 * @param ve
	 * @param elRepresentation
	 */
	private void makeRepresentationMediaObjects(Namespace sddNamespace,	VersionableEntity ve, Element elRepresentation) {
		List <Element> listMediaObjects = elRepresentation.getChildren("MediaObject", sddNamespace);
		for (Element elMediaObject : listMediaObjects) {
			String ref = null;
			//TODO
			String role = null;
			if (elMediaObject != null) {
				ref = elMediaObject.getAttributeValue("ref");
				role = elMediaObject.getAttributeValue("role");
			}
			if (StringUtils.isNotBlank(ref)) {
				if (ve instanceof TaxonDescription) {
					TaxonDescription td = (TaxonDescription) ve;
					if (td.getSources().size() > 0) {
						this.associateImageWithCdmBase(ref, td.getSources().iterator().next().getCitation());
					} else {
						Reference descriptionSource = ReferenceFactory.newGeneric();
						sources.add(descriptionSource);
						//TODO type
						td.addSource(OriginalSourceType.Unknown, null, null, descriptionSource, null);
						this.associateImageWithCdmBase(ref,descriptionSource);
					}
				} else {
					this.associateImageWithCdmBase(ref,ve);
				}
			}
		}
	}

	/**
	 * Handles the "Links" element
	 * @param parent
	 * @param sddNamespace
	 * @param ve
	 */
	private void makeRepresentationForIdentifiableMediaEntity(Element parent,
			Namespace sddNamespace, IdentifiableMediaEntity ime) {
		Element elLinks = parent.getChild("Links",sddNamespace);

		if (elLinks != null) {

			//  <Link rel="Alternate" href="http://www.diversitycampus.net/people/hagedorn"/>
			List<Element> listLinks = elLinks.getChildren("Link", sddNamespace);
			Media link = Media.NewInstance();
			MediaRepresentation mr = MediaRepresentation.NewInstance();
			int k = 0;
			//for each Link
			for (Element elLink : listLinks){

				try {
					//TODO
					String rel = elLink.getAttributeValue("rel");
					String href = elLink.getAttributeValue("href");
					URI uri = new URI(href);
					mr.addRepresentationPart(MediaRepresentationPart.NewInstance(uri, null));
					link.addRepresentation(mr);
					ime.addMedia(link);

				} catch (Exception e) {
					//FIXME
					logger.warn("Import of Link " + k + " failed.");
				}

				if ((++k % modCount) == 0){ logger.info("Links handled: " + k);}

			}
		}
	}

	/**
	 * @param sddNamespace
	 * @param ve
	 * @param elRepresentation
	 * @param langLabDet
	 * @return
	 */
	private void makeRepresentationForIdentifiableEntity(Namespace sddNamespace, IdentifiableEntity<?> ie,
					Element elRepresentation, Map<Language, List<String>> langLabDet) {
		List<String> labDet = null;

		if (ie instanceof TaxonNameBase) {
			if (langLabDet.keySet().contains(getTermService().getLanguageByIso("la"))) {
				labDet = langLabDet.get(getTermService().getLanguageByIso("la"));
			} else if (langLabDet.keySet().contains(datasetLanguage)) {
				labDet = langLabDet.get(datasetLanguage);
				logger.info("TaxonName " + (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace) + " is not specified as a latin name.");
			} else {
				labDet = langLabDet.get(langLabDet.keySet().iterator().next());
				logger.info("TaxonName " + (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace) + " is not specified as a latin name.");
			}
		} else {
			labDet = langLabDet.get(langLabDet.keySet().iterator().next());
		}

		//FIXME labDet is != null only for TaxonNameBase
		ie.setTitleCache(labDet.get(0), true);

		if (labDet.size()>1) {
			Annotation annotation = null;
			if (labDet.get(1) != null) {
				if (labDet.get(2) != null) {
					annotation = Annotation.NewInstance(labDet.get(2) + " - " + labDet.get(1), datasetLanguage);
				} else {
					annotation = Annotation.NewInstance(labDet.get(1), datasetLanguage);
				}
			}
			ie.addAnnotation(annotation);
		}
		return;
	}

	/**
	 * @param elLabel
	 * @return
	 */
	private Language getLanguage(Element elLanguage) {
		String lang = elLanguage.getAttributeValue("lang",xmlNamespace);
		Language language = null;
		if (StringUtils.isNotBlank(lang)) {
			language = getTermService().getLanguageByIso(lang.substring(0, 2));
		} else {
			language = datasetLanguage;
		}
		return language;
	}


	// imports the representation (label, detail, lang) of a particular SDD element
	protected void importTechnicalMetadata(Element root, Namespace sddNamespace, SDDImportConfigurator sddConfig){
		Element elTechnicalMetadata = root.getChild("TechnicalMetadata", sddNamespace);
		String nameCreated = elTechnicalMetadata.getAttributeValue("created");
		sourceReference = sddConfig.getSourceReference();

		if (nameCreated != null) {
			if (!nameCreated.equals("")) {
				int year = Integer.parseInt(nameCreated.substring(0,4));
				int monthOfYear = Integer.parseInt(nameCreated.substring(5,7));
				int dayOfMonth = Integer.parseInt(nameCreated.substring(8,10));
				int hourOfDay = Integer.parseInt(nameCreated.substring(11,13));
				int minuteOfHour = Integer.parseInt(nameCreated.substring(14,16));
				int secondOfMinute = Integer.parseInt(nameCreated.substring(17,19));
				DateTime created = new DateTime(year,monthOfYear,dayOfMonth,hourOfDay,minuteOfHour,secondOfMinute,0);
				sourceReference.setCreated(created);
				sec.setCreated(created);
			}
		}

		// <Generator name="n/a, handcrafted instance document" version="n/a"/>
		Element elGenerator = elTechnicalMetadata.getChild("Generator", sddNamespace);
		generatorName = elGenerator.getAttributeValue("name");
		generatorVersion = elGenerator.getAttributeValue("version");

		sec.addAnnotation(Annotation.NewDefaultLanguageInstance(generatorName + " - " + generatorVersion));
		sourceReference.addAnnotation(Annotation.NewDefaultLanguageInstance(generatorName + " - " + generatorVersion));

	}

	// imports the complete dataset information
	protected void importDataset(Element elDataset, Namespace sddNamespace, SDDImportState state){			// <Dataset xml:lang="en-us">

		workingSet = WorkingSet.NewInstance();
		importDatasetLanguage(elDataset,state);
		importDatasetRepresentation(elDataset, sddNamespace);
		importRevisionData(elDataset, sddNamespace);
		importIPRStatements(elDataset, sddNamespace, state);
		importTaxonNames(elDataset, sddNamespace, state);

		importDescriptiveConcepts(elDataset, sddNamespace, state);
		importCharacters(elDataset, sddNamespace, state);
		importCharacterTrees(elDataset, sddNamespace, state);

		MarkerType editorMarkerType = getMarkerType(state, SDDTransformer.uuidMarkerEditor, "editor", "Editor", "edt");
		MarkerType geographicAreaMarkerType = getMarkerType(state, SDDTransformer.uuidMarkerSDDGeographicArea, "SDDGeographicArea", "SDDGeographicArea", "ga");
		MarkerType descriptiveConceptMarkerType = getMarkerType(state, SDDTransformer.uuidMarkerDescriptiveConcept, "DescriptiveConcept", "Descriptive Concept", "DC");
		markerTypes.add(editorMarkerType);
		markerTypes.add(geographicAreaMarkerType);
		markerTypes.add(descriptiveConceptMarkerType);

		//saving of all imported data into the CDM db
		saveVocabularies();
		saveFeatures();
		saveModifiers();
		saveStates();
		saveMarkerType();
		saveAreas(geographicAreaMarkerType);
		saveUnits();
		saveStatisticalMeasure();
		saveAnnotationType();

		importCodedDescriptions(elDataset, sddNamespace, state);
		importAgents(elDataset, sddNamespace, state);
		importPublications(elDataset, sddNamespace, state);
		importMediaObjects(elDataset, sddNamespace, state);
		importTaxonHierarchies(elDataset, sddNamespace, state);
		importGeographicAreas(elDataset, sddNamespace, state);
		importSpecimens(elDataset,sddNamespace, state);


		if ((authors != null)||(editors != null)) {
			Team team = Team.NewInstance();
			if (authors != null) {
				for (Person author : authors.values()){
					team.addTeamMember(author);
				}
			}
			if (editors != null) {
				Marker marker = Marker.NewInstance();
				marker.setMarkerType(editorMarkerType);
				for (Person editor : editors.values()){
					Person edit = editor;
					edit.addMarker(marker);
					team.addTeamMember(edit);
				}
			}
			sec.setAuthorship(team);
			sourceReference.setAuthorship(team);
		}

		if (copyright != null) {
			sourceReference.addRights(copyright);
			sec.addRights(copyright);
		}

		// Returns a CdmApplicationController created by the values of this configuration.
		IDescriptionService descriptionService = getDescriptionService();

		for (TaxonDescription taxonDescription : taxonDescriptions.values()){
			// Persists a Description
			descriptionService.save(taxonDescription);
		}

		for (String ref : taxonDescriptions.keySet()){
			TaxonDescription td = taxonDescriptions.get(ref);
			if (citations.containsKey(ref)) {
				Reference publication = publications.get(citations.get(ref));
				if (locations.containsKey(ref)) {
					Annotation location = Annotation.NewInstance(locations.get(ref), datasetLanguage);
					//TODO move to a generic place (implemented in hurry therefore dirty)
					AnnotationType annotationType = getAnnotationType(state, uuidAnnotationTypeLocation, "location", "location", "location", null);
//					annotationTypes.add(annotationType);  TODO necessary??
					location.setAnnotationType(annotationType);
					(publication).addAnnotation(location);
				}
				//TODO type
				td.addSource(OriginalSourceType.Unknown, null, null, publication, null);
			}
		}
		logger.info("end makeTaxonDescriptions ...");

		if (descriptiveConcepts != null) {
			for (Feature feature : descriptiveConcepts) {
				Marker marker = Marker.NewInstance();
				marker.setMarkerType(descriptiveConceptMarkerType);
				feature.addMarker(marker);
			}
		}
		saveFeatures();

		for (Reference publication : publications.values()){
			getReferenceService().save(publication);
		}

		for (Reference source : sources){
			getReferenceService().save(source);
		}

		for (FeatureTree featureTree : featureTrees) {
			getFeatureTreeService().save(featureTree);
		}
		getWorkingSetService().save(workingSet);
		for (Classification classification : classifications) {
			getClassificationService().save(classification);
		}
		for (DerivedUnit specimen : specimens.values()) {
			getOccurrenceService().save(specimen);
		}
		logger.info("end of persistence ...");

		return;
	}

	/**
	 *
	 */
	private void saveVocabularies() {
		for (TermVocabulary<?> vocabulary : vocabularies ){
			getVocabularyService().save(vocabulary);
		}

	}

	private void saveAnnotationType() {
		for (AnnotationType annotationType: annotationTypes){
		    getTermService().saveOrUpdate(annotationType);
		}
	}

	private void saveStatisticalMeasure() {
		for (StatisticalMeasure sm : statisticalMeasures){
			getTermService().save(sm);
		}
	}

	private void saveUnits() {
		if (units != null) {
			for (MeasurementUnit unit : units.values()){
				if (unit != null) {
					getTermService().save(unit);
				}
			}
		}
	}

	private void saveAreas(MarkerType geographicAreaMarkerType) {
		for (NamedArea area : namedAreas.values() ){
			Marker marker = Marker.NewInstance();
			marker.setMarkerType(geographicAreaMarkerType);
			area.addMarker(marker);
			getTermService().save(area);
		}
	}

	private void saveStates() {
		for (State state : states.values() ){
			getTermService().save(state);
		}
	}

	private void saveMarkerType() {
		for (MarkerType markerType : markerTypes){
			getTermService().save(markerType);
		}
	}

	private void saveModifiers() {
		for (DefinedTerm modifier : modifiers.values() ){
			getTermService().save(modifier);
		}
	}

	private void saveFeatures() {
		for (Feature feature : features.values() ){
			getTermService().save(feature);
		}
	}

	// imports the default language of the dataset
	protected void importDatasetLanguage(Element elDataset, SDDImportState state){
		String nameLang = elDataset.getAttributeValue("lang",xmlNamespace);

		if (StringUtils.isNotBlank(nameLang)) {
			String iso = nameLang.substring(0, 2);
			datasetLanguage = getTermService().getLanguageByIso(iso);
		} else {
			datasetLanguage = Language.DEFAULT();
		}
		if (datasetLanguage == null) {
			datasetLanguage = Language.DEFAULT();
		}
	}

	// imports the specimens
	protected void importSpecimens(Element elDataset, Namespace sddNamespace, SDDImportState cdmState) {
		logger.info("start Specimens ...");
		/*	<Specimens>
        		<Specimen id="sp1">
           			<Representation>
              			<Label>TJM45337</Label>
           			</Representation>
        		</Specimen>
     		</Specimens>
		 */
		Element elSpecimens = elDataset.getChild("Specimens",sddNamespace);
		if (elSpecimens != null){
			List<Element> listSpecimens = elSpecimens.getChildren("Specimen", sddNamespace);
			for (Element elSpecimen : listSpecimens) {
				String id = elSpecimen.getAttributeValue("id");
				DerivedUnit specimen = null;
				if (!id.equals("")) {
					specimen = DerivedUnit.NewPreservedSpecimenInstance();
					specimens.put(id,specimen);
					importRepresentation(elSpecimen, sddNamespace, specimen, id, cdmState);
				}
			}

		}
	}

	// imports the revision data associated with the Dataset (authors, modifications)
	protected void importRevisionData(Element elDataset, Namespace sddNamespace){
		// <RevisionData>
		logger.info("start RevisionData ...");
		Element elRevisionData = elDataset.getChild("RevisionData",sddNamespace);
		if (elRevisionData != null){
			// <Creators>
			Element elCreators = elRevisionData.getChild("Creators",sddNamespace);

			// <Agent role="aut" ref="a1"/>
			List<Element> listAgents = elCreators.getChildren("Agent", sddNamespace);

			int j = 0;
			//for each Agent
			for (Element elAgent : listAgents){

				String role = elAgent.getAttributeValue("role");
				String ref = elAgent.getAttributeValue("ref");
				if (role.equals("aut")) {
					if(!ref.equals("")) {
						authors.put(ref, null);
					}
				}
				if (role.equals("edt")) {
					if(!ref.equals("")) {
						editors.put(ref, null);
					}
				}
				if ((++j % modCount) == 0){ logger.info("Agents handled: " + j);}

			}

			// <DateModified>2006-04-08T00:00:00</DateModified>
			String stringDateModified = (String)ImportHelper.getXmlInputValue(elRevisionData, "DateModified",sddNamespace);

			if (stringDateModified != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
				Date d = null;
				try {
					d = sdf.parse(stringDateModified);
				} catch(Exception e) {
					System.err.println("Exception :");
					e.printStackTrace();
				}

				DateTime updated = null;
				if (d != null) {
					updated = new DateTime(d);
					sourceReference.setUpdated(updated);
					sec.setUpdated(updated);
				}
			}
		}
	}

	// imports ipr statements associated with a dataset
	protected void importIPRStatements(Element elDataset, Namespace sddNamespace, SDDImportState state){
		// <IPRStatements>
		logger.info("start IPRStatements ...");
		Element elIPRStatements = elDataset.getChild("IPRStatements",sddNamespace);
		// <IPRStatement role="Copyright">
		if (elIPRStatements != null) {
			List<Element> listIPRStatements = elIPRStatements.getChildren("IPRStatement", sddNamespace);
			int j = 0;
			//for each IPRStatement

			for (Element elIPRStatement : listIPRStatements){

				String role = elIPRStatement.getAttributeValue("role");
				// <Label xml:lang="en-au">(c) 2003-2006 Centre for Occasional Botany.</Label>
				Element elLabel = elIPRStatement.getChild("Label",sddNamespace);
				String lang = "";
				if (elLabel != null) {
					lang = elLabel.getAttributeValue("lang",xmlNamespace);
				}
				String label = (String)ImportHelper.getXmlInputValue(elIPRStatement, "Label",sddNamespace);

				if (role.equals("Copyright")) {
					Language iprLanguage = null;
					if (lang != null) {
						if (!lang.equals("")) {
							iprLanguage = getTermService().getLanguageByIso(lang.substring(0, 2));
						} else {
							iprLanguage = datasetLanguage;
						}
					}
					if (iprLanguage == null) {
						iprLanguage = datasetLanguage;
					}
					copyright = Rights.NewInstance(label, iprLanguage);
				}

				if (copyright != null) {
					sourceReference.addRights(copyright);
					sec.addRights(copyright);
				}

				if ((++j % modCount) == 0){ logger.info("IPRStatements handled: " + j);}

			}
		}
	}

	// imports the taxon names
	protected void importTaxonNames(Element elDataset, Namespace sddNamespace, SDDImportState state){
		// <TaxonNames>
		logger.info("start TaxonNames ...");
		Element elTaxonNames = elDataset.getChild("TaxonNames",sddNamespace);
		// <TaxonName id="t1" uri="urn:lsid:authority:namespace:my-own-id">
		if (elTaxonNames != null) {
			List<Element> listTaxonNames = elTaxonNames.getChildren("TaxonName", sddNamespace);
			int j = 0;
			//for each TaxonName
			for (Element elTaxonName : listTaxonNames){

				String id = elTaxonName.getAttributeValue("id");
				String uri = elTaxonName.getAttributeValue("uri");

				NonViralName<?> tnb = null;
				if (!id.equals("")) {
					tnb = NonViralName.NewInstance(defaultRank);
					IdentifiableSource source = null;
					if (isNotBlank(uri)) {
						//TODO type
						source = IdentifiableSource.NewInstance(OriginalSourceType.Unknown, id, "TaxonName", ReferenceFactory.newGeneric(), uri);
					} else {
						source = IdentifiableSource.NewDataImportInstance(id, "TaxonName");
					}
					tnb.addSource(source);
					taxonNameBases.put(id,tnb);
				}

				// <Representation>
				// <Label xml:lang="la">Viola hederacea Labill.</Label>
				importRepresentation(elTaxonName, sddNamespace, tnb, id, state);

				if ((++j % modCount) == 0){ logger.info("TaxonNames handled: " + j);}

			}
		}
	}

	// imports the characters (categorical, quantitative and text ; sequence characters not supported) which correspond to CDM Features
	protected void importCharacters(Element elDataset, Namespace sddNamespace, SDDImportState cdmState){
		// <Characters>
		logger.info("start Characters ...");
		Element elCharacters = elDataset.getChild("Characters", sddNamespace);

		// <CategoricalCharacter id="c1">
		if (elCharacters != null) {
			handleCategoricalData(sddNamespace, cdmState, elCharacters);
			handleQuantitativeData(sddNamespace, cdmState, elCharacters);
			handleTextCharacters(sddNamespace, cdmState, elCharacters);
		}

		/*for (Iterator<Feature> f = features.values().iterator() ; f.hasNext() ;){
			featureSet.add(f.next()); //XIM Why this line ?
		}*/

		return;

	}

	/**
	 * @param sddNamespace
	 * @param cdmState
	 * @param elCharacters
	 * @return
	 */
	private void handleCategoricalData(Namespace sddNamespace, SDDImportState cdmState, Element elCharacters) {
		List<Element> elCategoricalCharacters = elCharacters.getChildren("CategoricalCharacter", sddNamespace);
		int j = 0;
		for (Element elCategoricalCharacter : elCategoricalCharacters){
			try {

				String idCC = elCategoricalCharacter.getAttributeValue("id");
				Feature categoricalCharacter = Feature.NewInstance();
				categoricalCharacter.setKindOf(Feature.DESCRIPTION());
				importRepresentation(elCategoricalCharacter, sddNamespace, categoricalCharacter, idCC, cdmState);
				categoricalCharacter.setSupportsCategoricalData(true);

				// <States>
				Element elStates = elCategoricalCharacter.getChild("States",sddNamespace);

				// <StateDefinition id="s1">
				List<Element> elStateDefinitions = elStates.getChildren("StateDefinition",sddNamespace);
				TermVocabulary<State> termVocabularyState = TermVocabulary.NewInstance(TermType.State, null, null, null, null);

				vocabularies.add(termVocabularyState);

				int k = 0;
				//for each StateDefinition
				for (Element elStateDefinition : elStateDefinitions){

					if ((++k % modCount) == 0){ logger.info("StateDefinitions handled: " + (k-1));}

					String idS = elStateDefinition.getAttributeValue("id");
					State state = states.get(idS);
					if (state == null){
						state = State.NewInstance();
					}else{
						logger.debug("State duplicate found");
					}
					importRepresentation(elStateDefinition, sddNamespace, state, idS, cdmState);

					termVocabularyState.addTerm(state);
					states.put(idS,state);
				}
				categoricalCharacter.addSupportedCategoricalEnumeration(termVocabularyState);
				features.put(idCC, categoricalCharacter);

			} catch (Exception e) {
				logger.warn("Import of CategoricalCharacter " + j + " failed.");
				cdmState.setUnsuccessfull();
			}

			if ((++j % modCount) == 0){ logger.info("CategoricalCharacters handled: " + j);}

		}
		return;
	}

	/**
	 * @param sddNamespace
	 * @param sddConfig
	 * @param elCharacters
	 */
	private void handleQuantitativeData(Namespace sddNamespace,	SDDImportState cdmState, Element elCharacters) {
		int j;
		// <QuantitativeCharacter id="c2">
		List<Element> elQuantitativeCharacters = elCharacters.getChildren("QuantitativeCharacter", sddNamespace);
		j = 0;
		//for each QuantitativeCharacter
		for (Element elQuantitativeCharacter : elQuantitativeCharacters){

			try {

				String idQC = elQuantitativeCharacter.getAttributeValue("id");

				// <Representation>
				//  <Label>Leaf length</Label>
				// </Representation>
				Feature quantitativeCharacter = Feature.NewInstance();
				quantitativeCharacter.setKindOf(Feature.DESCRIPTION());
				importRepresentation(elQuantitativeCharacter, sddNamespace, quantitativeCharacter, idQC, cdmState);

				quantitativeCharacter.setSupportsQuantitativeData(true);

				// <MeasurementUnit>
				//  <Label role="Abbrev">m</Label>
				// </MeasurementUnit>
				Element elMeasurementUnit = elQuantitativeCharacter.getChild("MeasurementUnit",sddNamespace);
				String label = "";
				String role = "";
				if (elMeasurementUnit != null) {
					Element elLabel = elMeasurementUnit.getChild("Label",sddNamespace);
					role = elLabel.getAttributeValue("role");
					label = (String)ImportHelper.getXmlInputValue(elMeasurementUnit, "Label",sddNamespace);
				}

				MeasurementUnit unit = null;
				if (!label.equals("")){
					if (role != null) {
						if (role.equals("Abbrev")){
							unit = MeasurementUnit.NewInstance(label,label,label);
						}
					} else {
						unit = MeasurementUnit.NewInstance(label,label,label);
					}
				}

				if (unit != null) {
					units.put(idQC, unit);
				}

				//<Default>
				//  <MeasurementUnitPrefix>milli</MeasurementUnitPrefix>
				//</Default>
				Element elDefault = elQuantitativeCharacter.getChild("Default",sddNamespace);
				if (elDefault != null) {
					String measurementUnitPrefix = (String)ImportHelper.getXmlInputValue(elDefault, "MeasurementUnitPrefix",sddNamespace);
					if (! measurementUnitPrefix.equals("")){
						defaultUnitPrefixes.put(idQC, measurementUnitPrefix);
					}
				}

				features.put(idQC, quantitativeCharacter);

			} catch (Exception e) {
				//FIXME
				logger.warn("Import of QuantitativeCharacter " + j + " failed.");
				cdmState.setUnsuccessfull();
			}

			if ((++j % modCount) == 0){ logger.info("QuantitativeCharacters handled: " + j);}

		}
		return;
	}

	private void handleTextCharacters(Namespace sddNamespace, SDDImportState cdmState, Element elCharacters) {
		int j;
		// <TextCharacter id="c3">
		List<Element> elTextCharacters = elCharacters.getChildren("TextCharacter", sddNamespace);
		j = 0;
		//for each TextCharacter
		for (Element elTextCharacter : elTextCharacters){

			try {

				String idTC = elTextCharacter.getAttributeValue("id");

				// <Representation>
				//  <Label xml:lang="en">Leaf features not covered by other characters</Label>
				// </Representation>
				Feature textCharacter = Feature.NewInstance();
				textCharacter.setKindOf(Feature.DESCRIPTION());
				importRepresentation(elTextCharacter, sddNamespace, textCharacter, idTC, cdmState);

				textCharacter.setSupportsTextData(true);

				features.put(idTC, textCharacter);

			} catch (Exception e) {
				//FIXME
				logger.warn("Import of TextCharacter " + j + " failed.");
				cdmState.setUnsuccessfull();
			}

			if ((++j % modCount) == 0){ logger.info("TextCharacters handled: " + j);}

		}
		return;
	}

	// imports the descriptions of taxa
	protected void importCodedDescriptions(Element elDataset, Namespace sddNamespace, SDDImportState cdmState){

		// <CodedDescriptions>
		logger.info("start CodedDescriptions ...");
		Element elCodedDescriptions = elDataset.getChild("CodedDescriptions",sddNamespace);

		// <CodedDescription id="D101">
		if (elCodedDescriptions != null) {
			List<Element> listCodedDescriptions = elCodedDescriptions.getChildren("CodedDescription", sddNamespace);
			int j = 0;
			//for each CodedDescription
			for (Element elCodedDescription : listCodedDescriptions){
				handleCodedDescription(sddNamespace, cdmState, elCodedDescription, j);
				if ((++j % modCount) == 0){ logger.info("CodedDescriptions handled: " + j);}
			}
		}
		return;
	}

	/**
	 * @param sddNamespace
	 * @param sddConfig
	 * @param j
	 * @param elCodedDescription
	 * @return
	 */
	private void handleCodedDescription(Namespace sddNamespace, SDDImportState cdmState, Element elCodedDescription, int j) {
		try {

			String idCD = elCodedDescription.getAttributeValue("id");

			// <Representation>
			//  <Label>&lt;i&gt;Viola hederacea&lt;/i&gt; Labill. as revised by R. Morris April 8, 2006</Label>
			// </Representation>
			TaxonDescription taxonDescription = TaxonDescription.NewInstance();
			if (!generatorName.isEmpty()){
				Annotation annotation = Annotation.NewInstance(generatorName, AnnotationType.TECHNICAL(),Language.DEFAULT());
				taxonDescription.addAnnotation(annotation);
			}
			importRepresentation(elCodedDescription, sddNamespace, taxonDescription, idCD, cdmState);

			// <Scope>
			//  <TaxonName ref="t1"/>
			//  <Citation ref="p1" location="p. 30"/>
			// </Scope>
			Element elScope = elCodedDescription.getChild("Scope", sddNamespace);
			Taxon taxon;
			if (elScope != null) {
				taxon = handleCDScope(sddNamespace, cdmState, idCD, elScope);
			} else {//in case no taxon is linked to the description, a new one is created
				taxon = handleCDNoScope(sddNamespace, cdmState, elCodedDescription);
			}

			// <SummaryData>
			Element elSummaryData = elCodedDescription.getChild("SummaryData",sddNamespace);
			if (elSummaryData != null) {
				handleSummaryCategoricalData(sddNamespace, taxonDescription, elSummaryData);
				handleSummaryQuantitativeData(sddNamespace, taxonDescription, elSummaryData);
				handleSummaryTextData(sddNamespace, taxonDescription, elSummaryData);
			}

			if (taxon != null) {
				taxon.addDescription(taxonDescription);
			}
//
			workingSet.addDescription(taxonDescription);

//OLD			taxonDescription.setDescriptiveSystem(featureSet);

			taxonDescriptions.put(idCD, taxonDescription);//FIXME

		} catch (Exception e) {
			//FIXME
			logger.warn("Import of CodedDescription " + j + " failed.", e);
			cdmState.setUnsuccessfull();
		}
		return;
	}

	/**
	 * @param sddNamespace
	 * @param sddConfig
	 * @param elCodedDescription
	 * @param taxon
	 * @return
	 */
	private Taxon handleCDNoScope(Namespace sddNamespace,
	        SDDImportState cdmState, Element elCodedDescription	) {
		Taxon taxon = null;
		NonViralName<?> nonViralName = NonViralName.NewInstance(defaultRank);
		String id = new String("" + taxonNamesCount);
		IdentifiableSource source = IdentifiableSource.NewDataImportInstance(id, "TaxonName");
		importRepresentation(elCodedDescription, sddNamespace, nonViralName, id, cdmState);

		if(cdmState.getConfig().isReuseExistingTaxaWhenPossible()){
			taxon = getTaxonService().findBestMatchingTaxon(nonViralName.getTitleCache());
		}

		if(taxon != null){
			nonViralName = HibernateProxyHelper.deproxy(taxon.getName(), NonViralName.class);
//							taxonNameBases.put(id ,tnb);
//							taxonNamesCount++;
			logger.info("using existing Taxon " + taxon.getTitleCache());
		} else {
			nonViralName.addSource(source);
			taxonNameBases.put(id ,nonViralName);
			taxonNamesCount++;
			logger.info("creating new Taxon from TaxonName " + nonViralName.getTitleCache());
			taxon = Taxon.NewInstance(nonViralName, sec);
		}
		return taxon;
	}

	/**
	 * @param sddNamespace
	 * @param sddConfig
	 * @param idCD
	 * @param elScope
	 * @param taxon
	 * @return
	 */
	private Taxon handleCDScope(Namespace sddNamespace, SDDImportState cdmState,
			String idCD, Element elScope) {
		Taxon taxon = null;
		Element elTaxonName = elScope.getChild("TaxonName", sddNamespace);
		String ref = elTaxonName.getAttributeValue("ref");
		INonViralName nonViralName = taxonNameBases.get(ref);

		if(cdmState.getConfig().isReuseExistingTaxaWhenPossible()){
			taxon = getTaxonService().findBestMatchingTaxon(nonViralName.getTitleCache());
		}

		if(taxon != null){
			logger.info("using existing Taxon" + taxon.getTitleCache());
			if(!nonViralName.getUuid().equals(taxon.getName().getUuid())){
				logger.warn("TaxonNameBase entity of existing taxon does not match Name in list -> replacing Name in list");
				nonViralName = taxon.getName();
			}
		} else {
			logger.info("creating new Taxon from TaxonName '" + nonViralName.getTitleCache()+"'");
			taxon = Taxon.NewInstance(nonViralName, sec);
		}

		//citation
		Element elCitation = elScope.getChild("Citation",sddNamespace);
		if (elCitation != null) {
			String refCitation = elCitation.getAttributeValue("ref");
			if (! refCitation.equals("")){
				citations.put(idCD, refCitation);
			}
			String location = elCitation.getAttributeValue("location");
			if (! location.equals("")){
				locations.put(idCD, location);
			}
		}
		return taxon;
	}

	/**
	 * @param sddNamespace
	 * @param taxonDescription
	 * @param elSummaryData
	 */
	private void handleSummaryTextData(Namespace sddNamespace,
			TaxonDescription taxonDescription, Element elSummaryData) {
		String ref;
		int k;
		// <TextChar ref="c3">
		List<Element> elTextChars = elSummaryData.getChildren("TextChar", sddNamespace);
		k = 0;
		//for each TextChar
		for (Element elTextChar : elTextChars){
			if ((++k % modCount) == 0){ logger.info("TextChar handled: " + (k-1));}
			ref = elTextChar.getAttributeValue("ref");
			Feature feature = features.get(ref);
			TextData textData = TextData.NewInstance();
			textData.setFeature(feature);

			// <Content>Free form text</Content>
			String content = (String)ImportHelper.getXmlInputValue(elTextChar, "Content",sddNamespace);
			textData.putText(datasetLanguage, content);
			taxonDescription.addElement(textData);
		}
	}

	/**
	 * @param sddNamespace
	 * @param taxonDescription
	 * @param elSummaryData
	 */
	private void handleSummaryQuantitativeData(Namespace sddNamespace,
			TaxonDescription taxonDescription, Element elSummaryData) {
		String ref;
		int k;
		// <Quantitative ref="c2">
		List<Element> elQuantitatives = elSummaryData.getChildren("Quantitative", sddNamespace);
		k = 0;
		//for each Quantitative
		for (Element elQuantitative : elQuantitatives){
			if ((++k % modCount) == 0){ logger.warn("Quantitative handled: " + (k-1));}
			ref = elQuantitative.getAttributeValue("ref");
			Feature feature = features.get(ref);
			QuantitativeData quantitativeData = QuantitativeData.NewInstance();
			quantitativeData.setFeature(feature);

			MeasurementUnit unit = units.get(ref);
			String prefix = defaultUnitPrefixes.get(ref);
			if (unit != null) {
				String u = unit.getLabel();
				if (prefix != null) {
					u = prefix + u;
				}
				unit.setLabel(u);
				quantitativeData.setUnit(unit);
			}

			// <Measure type="Min" value="2.3"/>
			List<Element> elMeasures = elQuantitative.getChildren("Measure", sddNamespace);
			int l = 0;

			//for each State
			for (Element elMeasure : elMeasures){
				if ((++l % modCount) == 0){ logger.info("States handled: " + (l-1));}
				String type = elMeasure.getAttributeValue("type");
				String value = elMeasure.getAttributeValue("value");
				if (value.contains(",")) {
					value = value.replace(',', '.');
				}
				Float v = Float.parseFloat(value);
				//Float v = new Float(0);
				StatisticalMeasure t = null;
				if (type.equals("Min")) {
					t = StatisticalMeasure.MIN();
				} else if (type.equals("Mean")) {
					t = StatisticalMeasure.AVERAGE();
				} else if (type.equals("Max")) {
					t = StatisticalMeasure.MAX();
				} else if (type.equals("SD")) {
					t = StatisticalMeasure.STANDARD_DEVIATION();
				} else if (type.equals("N")) {
					t = StatisticalMeasure.SAMPLE_SIZE();
				} else if (type.equals("UMethLower")) {
					t = StatisticalMeasure.TYPICAL_LOWER_BOUNDARY();
				} else if (type.equals("UMethUpper")) {
					t = StatisticalMeasure.TYPICAL_UPPER_BOUNDARY();
				} else if (type.equals("Var")) {
					t = StatisticalMeasure.VARIANCE();
				} else {
					t = StatisticalMeasure.NewInstance(type,type,type);
					statisticalMeasures.add(t);
				}

				StatisticalMeasurementValue statisticalValue = StatisticalMeasurementValue.NewInstance();
				statisticalValue.setValue(v);
				statisticalValue.setType(t);
				quantitativeData.addStatisticalValue(statisticalValue);
				featureData.add(statisticalValue);
			}
			taxonDescription.addElement(quantitativeData);
		}
	}

	/**
	 * @param sddNamespace
	 * @param taxonDescription
	 * @param elSummaryData
	 */
	private void handleSummaryCategoricalData(Namespace sddNamespace,
			TaxonDescription taxonDescription, Element elSummaryData) {
		String ref;
		// <Categorical ref="c4">
		List<Element> elCategoricals = elSummaryData.getChildren("Categorical", sddNamespace);
		int k = 0;
		//for each Categorical
		for (Element elCategorical : elCategoricals){
			if ((++k % modCount) == 0){ logger.warn("Categorical handled: " + (k-1));}
			ref = elCategorical.getAttributeValue("ref");
			Feature feature = features.get(ref);
			CategoricalData categoricalData = CategoricalData.NewInstance();
			categoricalData.setFeature(feature);

			// <State ref="s3"/>
			List<Element> elStates = elCategorical.getChildren("State", sddNamespace);
			int l = 0;

			//for each State
			for (Element elState : elStates){
				if ((++l % modCount) == 0){ logger.info("States handled: " + (l-1));}
				ref = elState.getAttributeValue("ref");
				State state = states.get(ref);
				if (state != null) {
					StateData stateData = StateData.NewInstance();
					stateData.setState(state);
					List<Element> elModifiers = elState.getChildren("Modifier", sddNamespace);
					for (Element elModifier : elModifiers){
						ref = elModifier.getAttributeValue("ref");
						DefinedTerm modifier = modifiers.get(ref);
						if (modifier != null) {
							stateData.addModifier(modifier);
						}
					}
					categoricalData.addStateData(stateData);
				}
				taxonDescription.addElement(categoricalData);
			}
		}
	}

	// imports the persons associated with the dataset creation, modification, related publications
	protected void importAgents(Element elDataset, Namespace sddNamespace, SDDImportState cdmState){
		// <Agents>
		logger.info("start Agents ...");
		Element elAgents = elDataset.getChild("Agents",sddNamespace);
		if (elAgents != null) {
			// <Agent id="a1">
			List <Element> listAgents = elAgents.getChildren("Agent", sddNamespace);
			int j = 0;
			//for each Agent
			for (Element elAgent : listAgents){

				try {

					String idA = elAgent.getAttributeValue("id");

					//  <Representation>
					//   <Label>Kevin Thiele</Label>
					//   <Detail role="Description">Ali Baba is also known as r.a.m.</Detail>
					//  </Representation>
					Person person = Person.NewInstance();
					importRepresentation(elAgent, sddNamespace, person, idA, cdmState);
					person.addSource(IdentifiableSource.NewDataImportInstance(idA, "Agent"));

					/*XIM <Links>
					Element elLinks = elAgent.getChild("Links",sddNamespace);

					if (elLinks != null) {

						//  <Link rel="Alternate" href="http://www.diversitycampus.net/people/hagedorn"/>
						List<Element> listLinks = elLinks.getChildren("Link", sddNamespace);
						int k = 0;
						//for each Link
						for (Element elLink : listLinks){

							try {

								String rel = elLink.getAttributeValue("rel");
								String href = elLink.getAttributeValue("href");

								Media link = Media.NewInstance();
								MediaRepresentation mr = MediaRepresentation.NewInstance();
								mr.addRepresentationPart(MediaRepresentationPart.NewInstance(href, null));
								link.addRepresentation(mr);
								person.addMedia(link);

							} catch (Exception e) {
								//FIXME
								logger.warn("Import of Link " + k + " failed.");
								success = false;
							}

							if ((++k % modCount) == 0){ logger.info("Links handled: " + k);}

						}
					}
					*/
					if (authors.containsKey(idA)) {
						authors.put(idA,person);
					}

					if (editors.containsKey(idA)) {
						editors.put(idA, person);
					}

				} catch (Exception e) {
					//FIXME
					logger.warn("Import of Agent " + j + " failed.");
					cdmState.setUnsuccessfull();
				}

				if ((++j % modCount) == 0){ logger.info("Agents handled: " + j);}

			}
		}
	}

	// imports publications related with the data set
	protected void importPublications(Element elDataset, Namespace sddNamespace, SDDImportState cdmState){
		/* <Publications>
			  <Publication id="p112">
			    <Representation>
			      <Label>Gee, X. & Haa, Y. (2003). How to be happy in five minutes. Instant Gratifications, Palm Beach.</Label>
			    </Representation>
			    <Links>
			    <Link rel="BasedOn" href="doi:10.1992/32311"/>
			    <Link rel="Alternate" href="http://some.service.net/providing/bibliographic.data"/>
			    </Links>
			</Publications>
*/
		logger.info("start Publications ...");
		Element elPublications = elDataset.getChild("Publications",sddNamespace);

		if (elPublications != null) {
			List<Element> listPublications = elPublications.getChildren("Publication", sddNamespace);
			int j = 0;
			for (Element elPublication : listPublications){

				try {

					String idP = elPublication.getAttributeValue("id");
					Reference publication = ReferenceFactory.newArticle();
					importRepresentation(elPublication, sddNamespace, publication, idP, cdmState);

					publications.put(idP,publication);

				} catch (Exception e) {
					logger.warn("Import of Publication " + j + " failed.");
					cdmState.setUnsuccessfull();
				}

				if ((++j % modCount) == 0){ logger.info("Publications handled: " + j);}

			}
		}
	}

	// imports media objects such as images //FIXME check mediaobj
	protected void importMediaObjects(Element elDataset, Namespace sddNamespace, SDDImportState cdmState){
		// <MediaObjects>
		logger.info("start MediaObjects ...");
		Element elMediaObjects = elDataset.getChild("MediaObjects",sddNamespace);

		if (elMediaObjects != null) {
			// <MediaObject id="m1">
			List<Element> listMediaObjects = elMediaObjects.getChildren("MediaObject", sddNamespace);
			int j = 0;
			for (Element elMO : listMediaObjects){

				String id = "";

				try {
					String idMO = elMO.getAttributeValue("id");
					id = idMO;

					//  <Representation>
					//   <Label>Image description, e.g. to be used for alt-attribute in html.</Label>
					//  </Representation>
					Media media = Media.NewInstance();
					importRepresentation(elMO, sddNamespace, media, idMO, cdmState);

					// <Type>Image</Type>
					// <Source href="http://test.edu/test.jpg"/>
					String type = (String)ImportHelper.getXmlInputValue(elMO,"Type",sddNamespace);

					if ((type != null) && (type.equals("Image"))) {
						Element elSource = elMO.getChild("Source",sddNamespace);
						String href = elSource.getAttributeValue("href");

						ImageInfo imageMetaData = null;
						ImageFile image = null;

						if (href.substring(0,7).equals("http://")) {
							try{
								URL url = new URL(href);

								imageMetaData = ImageInfo.NewInstance(url.toURI(), 0);
								image = ImageFile.NewInstance(url.toURI(), null, imageMetaData);
							} catch (MalformedURLException e) {
								logger.error("Malformed URL", e);
							} catch (IOException ioe) {
							    logger.error("(IO ex: " + id + "): " + ioe.getMessage());
							}
						} else {
							String sns = cdmState.getConfig().getSourceNameString();
							File f = new File(sns);
							File parent = f.getParentFile();
							String fi = parent.toString() + File.separator + href;
							File file = new File(fi);
							imageMetaData = ImageInfo.NewInstance(new URI(fi), 0); //file
							image = ImageFile.NewInstance(file.toURI(), null, imageMetaData);
						}
						MediaRepresentation representation = MediaRepresentation.NewInstance(imageMetaData.getMimeType(), null);
						representation.addRepresentationPart(image);

						media.addRepresentation(representation);

						ArrayList<CdmBase> lcb = (ArrayList<CdmBase>) mediaObject_ListCdmBase.get(idMO);
						if (lcb != null) {
							for (int k = 0; k < lcb.size(); k++) {
								if (lcb.get(k) instanceof DefinedTermBase) {
									DefinedTermBase<?> dtb = (DefinedTermBase<?>) lcb.get(k);
									// if (lcb.get(0) instanceof DefinedTermBase) {
									// DefinedTermBase dtb = (DefinedTermBase) lcb.get(0);
									//									if (dtb!=null) {
									//										if (k == 0) {
									dtb.addMedia(media);
									//System.out.println(dtb.getLabel());
									//										} else {
									//											Media me = (Media) media.clone();
									//											dtb.addMedia(me);
									//										}
									//									}
								} else if (lcb.get(k) instanceof Reference) {
									Reference rb = (Reference) lcb.get(k);
									//} else if (lcb.get(0) instanceof Reference) {
									//Reference rb = (Reference) lcb.get(0);
									// rb.setTitleCache(label);
									//									if (rb!=null) {
									//										if (k == 0) {
									rb.addMedia(media);
									//System.out.println(rb.getTitle());
									//										} else {
									//											Media me = (Media) media.clone();
									//											rb.addMedia(me);
									//										}
									//									}
//								 else if (lcb.get(k) instanceof TaxonNameBase){
//									TaxonNameBase tb = (TaxonNameBase) lcb.get(k);
//									tb.addMedia(media);
								} else {
									logger.warn("Can't handle associated media for " + lcb.get(k).getId() + "(" +  lcb.get(k).getClass().getSimpleName()+")"  );
								}
							}
						}
					}

				} catch (Exception e) {
					//FIXME
				    logger.warn("Could not attach MediaObject " + j + "(SDD: " + id + ") to several objects: " + e.getMessage());
				    cdmState.setUnsuccessfull();
				}

				if ((++j % modCount) == 0){ logger.info("MediaObjects handled: " + j);

				}
			}
		}
	}

	// imports the <DescriptiveConcepts> block ; DescriptiveConcepts are used as nodes in CharacterTrees and Characters as leaves
	// but since Modifiers can be linked to DescriptiveConcepts they are stored as features with a particular Marker
	protected void importDescriptiveConcepts(Element elDataset, Namespace sddNamespace, SDDImportState cdmState){
		/* <DescriptiveConcepts>
		      <DescriptiveConcept id="dc0">
			        <Representation>
			          <Label>Fixed set of modifiers supported in Lucid3</Label>
			        </Representation>
			        <Modifiers>
			          <Modifier id="mod1">
			            <Representation>
			              <Label>rarely</Label>
			            </Representation>
			            <ModifierClass>Frequency</ModifierClass>
			            <ProportionRange lowerestimate="0.0" upperestimate="0.25"/>
			          </Modifier>
		          </Modifiers>
		        </DescriptiveConcept>
	         </DescriptiveConcepts>
		 */
		logger.info("start DescriptiveConcepts ...");
		Element elDescriptiveConcepts = elDataset.getChild("DescriptiveConcepts",sddNamespace);
		if (elDescriptiveConcepts != null) {
			List<Element> listDescriptiveConcepts = elDescriptiveConcepts.getChildren("DescriptiveConcept", sddNamespace);
			int j = 0;

			for (Element elDescriptiveConcept : listDescriptiveConcepts){
				try {
				String id = elDescriptiveConcept.getAttributeValue("id");
					Feature feature = Feature.NewInstance();
					feature.setKindOf(Feature.DESCRIPTION());
					if (!id.equals("")) {
					    //	 <Representation>
    					//       <Label>Body</Label>
    					importRepresentation(elDescriptiveConcept, sddNamespace, feature, id, cdmState);
    						features.put(id, feature);
    						getTermService().save(feature);//XIM
    						descriptiveConcepts.add(feature);
    						// imports the modifiers
    						Element elModifiers = elDescriptiveConcept.getChild("Modifiers", sddNamespace);
    					if (elModifiers !=null){
    						List<Element> listModifiers = elModifiers.getChildren("Modifier", sddNamespace);
    							TermVocabulary<DefinedTerm> termVocabularyState = TermVocabulary.NewInstance(TermType.Modifier, null, null, null, null);
    						for (Element elModifier : listModifiers) {
    							DefinedTerm modif = DefinedTerm.NewModifierInstance(null, null, null);
    							String idmod = elModifier.getAttributeValue("id");
    							importRepresentation(elModifier, sddNamespace, modif, idmod, cdmState);
    							termVocabularyState.addTerm(modif);
    							//termVocabularyStates.add(termVocabularyState);
    							getVocabularyService().save(termVocabularyState);//XIM
    							modifiers.put(idmod, modif);
    						}
    						feature.addRecommendedModifierEnumeration(termVocabularyState);
    					}
					}
				}
				catch (Exception e) {
					logger.warn("Import of DescriptiveConcept " + j + " failed: " + e.getMessage());
				}
				if ((++j % modCount) == 0){ logger.info("DescriptiveConcepts handled: " + j);}

			}
		}
	}

	// imports the <CharacterTrees> block
	protected void importCharacterTrees(Element elDataset, Namespace sddNamespace, SDDImportState cdmState){
		// <CharacterTrees>
		logger.info("start CharacterTrees ...");
		Element elCharacterTrees = elDataset.getChild("CharacterTrees",sddNamespace);

		if (elCharacterTrees != null) {
			List<Element> listCharacterTrees = elCharacterTrees.getChildren("CharacterTree", sddNamespace);
			int j = 0;
			for (Element elCharacterTree : listCharacterTrees){
				try {
					Element elRepresentation = elCharacterTree.getChild("Representation",sddNamespace);
					String label = (String)ImportHelper.getXmlInputValue(elRepresentation,"Label",sddNamespace);
					//Element elDesignedFor = elCharacterTree.getChild("DesignedFor",sddNamespace);//TODO ?

					FeatureTree featureTree =  FeatureTree.NewInstance();
					importRepresentation(elCharacterTree, sddNamespace, featureTree, "", cdmState);
					FeatureNode root = featureTree.getRoot();
					List<Element> listeOfNodes = elCharacterTree.getChildren("Nodes", sddNamespace);

					//Nodes of CharacterTrees in SDD always refer to DescriptiveConcepts
					for (Element elNodes : listeOfNodes) {
						handleCharacterNodes(sddNamespace, root, elNodes);
					}
					featureTrees.add(featureTree);
					if (workingSet.getDescriptiveSystem() != null){
						//TODO how to handle multiple
						logger.warn("Multiple feature trees not yet supported");
					}else{
						workingSet.setDescriptiveSystem(featureTree);
					}
				}

				catch (Exception e) {
					logger.warn("Import of Character tree " + j + " failed.");
					cdmState.setUnsuccessfull();
				}
				if ((++j % modCount) == 0){ logger.info("CharacterTrees handled: " + j);}

			}

		}
	}

	/**
	 * @param sddNamespace
	 * @param root
	 * @param elNodes
	 */
	private void handleCharacterNodes(Namespace sddNamespace, FeatureNode root, Element elNodes) {
		List<Element> listNodes = elNodes.getChildren("Node", sddNamespace);
		if (listNodes != null) {
			for (Element elNode : listNodes){
				String idN = elNode.getAttributeValue("id");
				FeatureNode fn = null;
				Feature dc = null;
				if (idN!=null) {
					// DescriptiveConcepts are used as nodes in CharacterTrees
					Element elDescriptiveConcept = elNode.getChild("DescriptiveConcept", sddNamespace);
					if (elDescriptiveConcept != null){
						String refDC = elDescriptiveConcept.getAttributeValue("ref");
						dc = features.get(refDC);
						fn = FeatureNode.NewInstance(dc);
					}
					if (fn==null){
						fn = FeatureNode.NewInstance();
					}
					Element elParent = elNode.getChild("Parent", sddNamespace);
					// in SDD links between Nodes are referenced by the <Parent> tag
					if (elParent!=null){
						String refP = elParent.getAttributeValue("ref");
						if (refP!=null) {
							FeatureNode parent = featureNodes.get(refP);
							if (parent==null){
								root.addChild(fn); // if no parent found or the reference is broken, add the node to the root of the tree
							}
							else {
								parent.addChild(fn);
							}
						}
					}
					else {
						root.addChild(fn); // if no parent found or the reference is broken, add the node to the root of the tree
					}
				}
				featureNodes.put(idN, fn);
			}
		}

		// Leaves of CharacterTrees in SDD are always CharNodes (referring to Characters)
		List<Element> listCharNodes = elNodes.getChildren("CharNode", sddNamespace);
		if (listCharNodes != null) {
			for (Element elCharNode : listCharNodes){
				Element elParent = elCharNode.getChild("Parent", sddNamespace);
				Element elCharacter = elCharNode.getChild("Character", sddNamespace);
				Element elDependencyRules = elCharNode.getChild("DependencyRules", sddNamespace);
				FeatureNode fn = FeatureNode.NewInstance();

				if (elDependencyRules!=null){
					Element elInapplicableIf = elCharNode.getChild("InapplicableIf", sddNamespace);
					if (elInapplicableIf!=null){
						List<Element> listStates = elInapplicableIf.getChildren("State", sddNamespace);
						for (Element stateElement : listStates) {
							String refState = stateElement.getAttributeValue("ref");
							if ((refState!=null)&&(!refState.equals(""))) {
								State state = states.get(refState);
								fn.addInapplicableState(state);
							}
						}
					}
					Element elOnlyapplicableIf = elCharNode.getChild("OnlyApplicableIf", sddNamespace);
					if (elOnlyapplicableIf!=null){
						List<Element> listStates = elInapplicableIf.getChildren("State", sddNamespace);
						for (Element stateElement : listStates) {
							String refState = stateElement.getAttributeValue("ref");
							if ((refState!=null)&&(!refState.equals(""))) {
								State state = states.get(refState);
								fn.addApplicableState(state);
							}
						}
					}
				}

				if (elParent!=null){
					String refP = elParent.getAttributeValue("ref");
					if ((refP!=null)&&(!refP.equals(""))) {
					FeatureNode parent = featureNodes.get(refP);
						if (parent==null){
						parent = root; // if no parent found or the reference is broken, add the node to the root of the tree
						}
						parent.addChild(fn);
					}
				}
				String refC = elCharacter.getAttributeValue("ref");
				if ((refC!=null)&&(!refC.equals(""))){
					Feature character = features.get(refC);
					fn.setFeature(character);
					featureNodes.put(refC, fn);
				}
			}
		}
	}

	// imports the <TaxonHierarchies> block
	protected void importTaxonHierarchies(Element elDataset, Namespace sddNamespace, SDDImportState cdmState){

		logger.info("start TaxonHierarchies ...");
		Element elTaxonHierarchies = elDataset.getChild("TaxonHierarchies",sddNamespace);

		if (elTaxonHierarchies != null) {
			List<Element> listTaxonHierarchies = elTaxonHierarchies.getChildren("TaxonHierarchy", sddNamespace);
			int j = 0;
			for (Element elTaxonHierarchy : listTaxonHierarchies){
				try {
					Element elRepresentation = elTaxonHierarchy.getChild("Representation",sddNamespace);
					String label = (String)ImportHelper.getXmlInputValue(elRepresentation,"Label",sddNamespace);
						Classification classification =  Classification.NewInstance(label);
						importRepresentation(elTaxonHierarchy, sddNamespace, classification, "", cdmState);

						Element elNodes = elTaxonHierarchy.getChild("Nodes", sddNamespace); // There can be only one <Nodes> block for TaxonHierarchies
						List<Element> listNodes = elNodes.getChildren("Node", sddNamespace);

						for (Element elNode : listNodes){
							String idN = elNode.getAttributeValue("id");
							TaxonNameBase<?,?> tnb = null;
							if (!idN.equals("")) {
								Element elTaxonName = elNode.getChild("TaxonName", sddNamespace);
								String refTN = elTaxonName.getAttributeValue("ref");
								tnb = taxonNameBases.get(refTN);
								Taxon taxon = tnb.getTaxa().iterator().next() ;
								Element elParent = elNode.getChild("Parent", sddNamespace);
								if (elParent!=null){
									String refP = elParent.getAttributeValue("ref");
									if (!refP.equals("")) {
										TaxonNode parent = taxonNodes.get(refP);
										TaxonNode child = parent.addChildTaxon(taxon, sec, null);
										child.setSynonymToBeUsed( Synonym.NewInstance(tnb, sec)); //TODO is this required??
										taxonNodes.put(idN,child);
									}
								}
								else {
									TaxonNode tn = classification.addChildTaxon(taxon, sec, null); // if no parent found or the reference is broken, add the node to the root of the tree
									tn.setSynonymToBeUsed( Synonym.NewInstance(tnb, sec));  //TODO is this required??
									taxonNodes.put(idN,tn);
								}
							}
						}

						classifications.add(classification);
					}

				catch (Exception e) {
					//FIXME
					logger.warn("Import of Taxon Hierarchy " + j + " failed.");
					cdmState.setUnsuccessfull();
				}

				if ((++j % modCount) == 0){ logger.info("TaxonHierarchies handled: " + j);}

			}

		}
	}

	// imports the <GeographicAreas> block
	protected void importGeographicAreas(Element elDataset, Namespace sddNamespace, SDDImportState cdmState) {
		Element elGeographicAreas = elDataset.getChild("GeographicAreas",sddNamespace);
		if (elGeographicAreas != null) {
			List<Element> listGeographicAreas = elGeographicAreas.getChildren("GeographicArea", sddNamespace);
			int j = 0;

			for (Element elGeographicArea : listGeographicAreas){

				String id = elGeographicArea.getAttributeValue("id");
				NamedArea na = NamedArea.NewInstance();
				importRepresentation(elGeographicArea, sddNamespace, na, id, cdmState);
				namedAreas.put(id,na);
								}
			if ((++j % modCount) == 0){ logger.info("GeographicAreas handled: " + j);}
		}
	}
}
