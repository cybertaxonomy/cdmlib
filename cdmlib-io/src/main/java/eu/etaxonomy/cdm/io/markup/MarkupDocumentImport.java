/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.markup;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeCacheStrategy;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Figure;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NameTypeParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.SpecimenTypeParser;
import eu.etaxonomy.cdm.strategy.parser.SpecimenTypeParser.TypeInfo;


/**
 * @author a.mueller
 *
 */
@Component
public class MarkupDocumentImport  extends MarkupImportBase implements ICdmIO<MarkupImportState> {
	private static final Logger logger = Logger.getLogger(MarkupDocumentImport.class);


	
	private static final boolean CREATE_NEW = true;
	private static final boolean IS_IMAGE_GALLERY = true;
	private static final boolean NO_IMAGE_GALLERY = false;

	private static final String ACCEPTED = "accepted";
	private static final String ACCEPTED_NAME = "acceptedName";
	private static final String ADDENDA = "addenda";
	private static final String ALTERNATEPUBTITLE = "alternatepubtitle";
	private static final String ALTERNATIVE_COLLECTION_TYPE_STATUS = "alternativeCollectionTypeStatus";
	private static final String ALTERNATIVE_COLLECTOR = "alternativeCollector";
	private static final String ALTERNATIVE_FIELD_NUM = "alternativeFieldNum";
	private static final String ALTITUDE = "altitude";
	private static final String ANNOTATION = "annotation";
	private static final String AUTHOR = "author";
	private static final String BIOGRAPHIES = "biographies";
	private static final String BOLD = "bold";
	private static final String BR = "br";
	private static final String CHAR = "char";
	private static final String CITATION = "citation";
	private static final String COLLECTION = "collection";
	private static final String COLLECTION_TYPE_STATUS = "collectionTypeStatus";
	private static final String COLLECTOR = "collector";
	private static final String COORDINATES = "coordinates";
	private static final String DATES = "dates";
	private static final String DEFAULT_MEDIA_URL = "defaultMediaUrl";
	private static final String DESTROYED = "destroyed";
	private static final String DETAILS = "details";
	private static final String DISTRIBUTION_LIST = "distributionList";
	private static final String EDITION = "edition";
	private static final String EDITORS = "editors";
	private static final String FEATURE = "feature";
	private static final String FIGURE = "figure";
	private static final String FIGURE_LEGEND = "figureLegend";
	private static final String FIGURE_PART = "figurePart";
	private static final String FIGURE_REF = "figureRef";
	private static final String FIGURE_TITLE = "figureTitle";
	private static final String FOOTNOTE = "footnote";
	private static final String FOOTNOTE_REF = "footnoteRef";
	private static final String FOOTNOTE_STRING = "footnoteString";
	private static final String FIELD_NUM = "fieldNum";
	private static final String FULL_NAME = "fullName";
	private static final String FULL_TYPE = "fullType";
	private static final String HEADING = "heading";
	private static final String HABITAT_LIST = "habitatList";
	private static final String HOMONYM = "homonym";
	private static final String HOMOTYPES = "homotypes";
	private static final String ID = "id";
	private static final String INFRANK = "infrank";
	private static final String INFRAUT = "infraut";
	private static final String INFRPARAUT = "infrparaut";
	private static final String ISSUE = "issue";
	private static final String ITALICS = "italics";
	private static final String KEY = "key";
	private static final String LOCALITY = "locality";
	private static final String LOST = "lost";
	private static final String META_DATA = "metaData";
	private static final String NAME = "name";
	private static final String NAME_TYPE = "nameType";
	private static final String NOM = "nom";
	private static final String NOMENCLATURE = "nomenclature";
	private static final String NOT_FOUND = "notFound";
	private static final String NOT_SEEN = "notSeen";
	private static final String NOTES = "notes";
	private static final String NUM = "num";
	private static final String ORIGINAL_DETERMINATION = "originalDetermination";
	private static final String PARAUT = "paraut";
	private static final String PUBFULLNAME = "pubfullname";
	private static final String PUBLICATION = "publication";
	private static final String PUBNAME = "pubname";
	private static final String PUBTITLE = "pubtitle";
	private static final String PUBTYPE = "pubtype";
	private static final String RANK = "rank";
	private static final String REF = "ref";
	private static final String REF_PART = "refPart";
	private static final String REFERENCES = "references";
	private static final String TAXON = "taxon";
	private static final String TAXONTITLE = "taxontitle";
	private static final String TEXT_SECTION = "textSection";
	private static final String TYPE = "type";
	private static final String TYPE_STATUS = "typeStatus";
	private static final String TREATMENT = "treatment";
	private static final String SPECIMEN_TYPE = "specimenType";
	private static final String STATUS = "status";
	private static final String STRING = "string";
	private static final String SUB_HEADING = "subHeading";
	private static final String SUB_COLLECTION = "subCollection";
	private static final String SYNONYM = "synonym";
	private static final String UNKNOWN = "unknown";
	private static final String URL = "url";
	private static final String USAGE = "usage";
	private static final String VOLUME = "volume";
	private static final String WRITER = "writer";
	private static final String YEAR = "year";

		
	private NonViralNameParserImpl parser = new NonViralNameParserImpl();
	
	//TODO make part of state, but state is renewed when invoking the import a second time 
	private UnmatchedLeads unmatchedLeads;

	
	//TODO remove preliminary
	@Autowired
	private AuthenticationManager authenticationManager;
	private Authentication authentication;
	private PermissionEvaluator permissionEvaluator;
	
	public MarkupDocumentImport(){
		super();
		System.out.println("TODO remove preliminary authentication");
//		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("admin", "0000");
//		authentication = authenticationManager.authenticate(token);
//		SecurityContext context = SecurityContextHolder.getContext();
//		context.setAuthentication(authentication);
//		permissionEvaluator = new CdmPermissionEvaluator();
	}
	
	
	@Override
	public boolean doCheck(MarkupImportState state){
		state.setCheck(true);
		doInvoke(state);
		state.setCheck(false);
		return state.isSuccess();
	}
	
	@Override
	public void doInvoke(MarkupImportState state){
		fireProgressEvent("Start import markup document", "Before start of document");
		fireWarningEvent("Test a warning", "At start", 17);
		
		Queue<CdmBase> outputStream = new LinkedList<CdmBase>();
		
		//FIXME reset state
		doAllTheOldOtherStuff(state);

		//START
		try {
			//StAX
			XMLEventReader reader = getStaxReader(state); 
			state.setReader(reader);
			//start document
			if (! validateStartOfDocument(reader)){
				state.setUnsuccessfull();
				return;
			}
			
			//publication
			String elName = PUBLICATION;
			boolean hasPublication = false;
			while (reader.hasNext()) {
				XMLEvent nextEvent = reader.nextEvent();
				if (isStartingElement(nextEvent, elName)){
					handlePublication(state, reader, nextEvent, elName);
					hasPublication = true;
				}else if (nextEvent.isEndDocument()){
					if (!hasPublication){
						String message = "No publication root element found";
						fireWarningEvent(message, nextEvent, 8);
					}
					//done
				}else{
					fireSchemaConflictEventExpectedStartTag(elName, reader);
				}
			}
			
//			//SAX
//			ImportHandlerBase handler= new PublicationHandler(this);
//			parseSAX(state, handler);
			
		} catch (FactoryConfigurationError e1) {
			fireWarningEvent("Some error occurred while setting up xml factory. Data can't be imported", "Start", 16);
			state.setUnsuccessfull();
		} catch (XMLStreamException e1) {
			fireWarningEvent("An XMLStreamException occurred while parsing. Data can't be imported", "Start", 16);
			state.setUnsuccessfull();
//		} catch (ParserConfigurationException e) {
//			fireWarningEvent("A ParserConfigurationException occurred while parsing. Data can't be imported", "Start", 16);
//		} catch (SAXException e) {
//			fireWarningEvent("A SAXException occurred while parsing. Data can't be imported", "Start", 16);
//		} catch (IOException e) {
//			fireWarningEvent("An IO exception occurred while parsing. Data can't be imported", "Start", 16);

		}
		 
		
		
		return;
		
	}

	


	private void handlePublication(MarkupImportState state, XMLEventReader reader, XMLEvent currentEvent, String elName) throws XMLStreamException {
			
		//attributes
		StartElement element = currentEvent.asStartElement();
		Map<String, Attribute> attributes = getAttributes(element);
		handleUnexpectedAttributes(element.getLocation(), attributes, "noNamespaceSchemaLocation");
		
		while (reader.hasNext()){
			XMLEvent event = readNoWhitespace(reader);
			//TODO cardinality of alternative
			if (event.isEndElement()){
				if (isEndingElement(event, elName)){
					return;
				}else{
					if(isEndingElement(event, BIOGRAPHIES)){
						//NOT YET IMPLEMENTED
						popUnimplemented(event.asEndElement());
					}else if(isEndingElement(event, REFERENCES)){
						//NOT YET IMPLEMENTED
						popUnimplemented(event.asEndElement());
					}else if(isEndingElement(event, TEXT_SECTION)){
						//NOT YET IMPLEMENTED
						popUnimplemented(event.asEndElement());
					}else if(isEndingElement(event, ADDENDA)){
						//NOT YET IMPLEMENTED
						popUnimplemented(event.asEndElement());
					}else{
						handleUnexpectedElement(event);
					}
				}
			}else if (event.isStartElement()){
				if(isStartingElement(event, META_DATA)){
					handleMetaData(state, reader, event);
				}else if(isStartingElement(event, TREATMENT)){
					handleTreatment(state, reader, event);
				}else if(isStartingElement(event, BIOGRAPHIES)){
					handleNotYetImplementedElement(event);
				}else if(isStartingElement(event, REFERENCES)){
					handleNotYetImplementedElement(event);
				}else if(isStartingElement(event, TEXT_SECTION)){
					handleNotYetImplementedElement(event);
				}else if(isStartingElement(event, ADDENDA)){
					handleNotYetImplementedElement(event);
				}else{
					handleUnexpectedStartElement(event);
				}
			}else{
				handleUnexpectedElement(event);
			}
		}
		throw new IllegalStateException("Publication has no ending element");
	}

	private void handleMetaData(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		checkNoAttributes(parentEvent);
		
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)){
				return;
			}else if(isStartingElement(next, DEFAULT_MEDIA_URL)){
				String baseUrl = getCData(state, reader, next);
				try {
					new URL(baseUrl);
					state.setBaseMediaUrl(baseUrl);
				} catch (MalformedURLException e) {
					String message = "defaultMediaUrl '%s' is not a valid URL";
					message = String.format(message, baseUrl);
					fireWarningEvent(message, next, 8);
				} 
			}else{
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("MetaData has no ending element");
		
	}


	private void handleTreatment(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		checkNoAttributes(parentEvent);
		Taxon lastTaxon = null;
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (isStartingElement(next, TAXON)){
				Taxon thisTaxon = handleTaxon(state, reader, next.asStartElement());
				doTaxonRelation(state, thisTaxon, lastTaxon, parentEvent.getLocation());
				lastTaxon = thisTaxon;
				//TODO for imports spanning multiple documents ?? Still needed?
				state.getConfig().setLastTaxonUuid(lastTaxon.getUuid());
			}else if(isMyEndingElement(next, parentEvent)){
				return;
			}else{
				fireSchemaConflictEventExpectedStartTag(TAXON, reader);
				state.setUnsuccessfull();
			}
		}
		return;
	}
	
	/**
	 * @param taxon
	 * @param lastTaxon
	 */
	private void doTaxonRelation(MarkupImportState state, Taxon taxon, Taxon lastTaxon, Location dataLocation) {
		
		Classification tree = makeTree(state);
		if (lastTaxon == null){
			tree.addChildTaxon(taxon, null, null, null);
			return;
		}
		Rank thisRank = taxon.getName().getRank();
		Rank lastRank = lastTaxon.getName().getRank();
		if (lastTaxon.getTaxonNodes().size() > 0){
			TaxonNode lastNode = lastTaxon.getTaxonNodes().iterator().next();
			if (thisRank.isLower(lastRank )  ){
				lastNode.addChildTaxon(taxon, null, null, null);
				fillMissingEpithetsForTaxa(lastTaxon, taxon);
			}else if (thisRank.equals(lastRank)){
				TaxonNode parent = lastNode.getParent();
				if (parent != null){
					parent.addChildTaxon(taxon, null, null, null);
					fillMissingEpithetsForTaxa(parent.getTaxon(), taxon);
				}else{
					tree.addChildTaxon(taxon, null, null, null);
				}
			}else if (thisRank.isHigher(lastRank)){
				doTaxonRelation(state, taxon, lastNode.getParent().getTaxon(), dataLocation);
//				TaxonNode parentNode = handleTaxonRelation(state, taxon, lastNode.getParent().getTaxon());
//				parentNode.addChildTaxon(taxon, null, null, null);
			}
		}else{
			
			String message = "Last taxon has no node";
			fireWarningEvent(message, makeLocationStr(dataLocation), 6);
		}
	}
	

	/**
	 * @param state
	 * @return 
	 */
	private Classification makeTree(MarkupImportState state) {
		Classification result = state.getTree(null);
		if (result == null){
			UUID uuid = state.getConfig().getClassificationUuid();
			if (uuid == null){
				logger.warn("No classification uuid is defined");
				result = createNewClassification(state);
			}else{
				result = getClassificationService().find(uuid);
				if (result == null){
					result = createNewClassification(state);
					result.setUuid(uuid);
				}
			}
			state.putTree(null, result);
		}
		save(result, state);
		return result;
	}
	

	private Classification createNewClassification(MarkupImportState state) {
		Classification result;
		result = Classification.NewInstance(state.getConfig().getClassificationTitle());
		state.putTree(null, result);
		return result;
	}


	private Taxon handleTaxon(MarkupImportState state, XMLEventReader reader, StartElement parentEvent) throws XMLStreamException {
		//TODO progress monitoring
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		Taxon taxon = createTaxonAndName(state, attributes);
		state.setCurrentTaxon(taxon);
		
		boolean hasTitle = false;
		boolean hasNomenclature = false;
		String taxonTitle = null;
		Extension writerExtension = null;
		
		
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()){
				if (isMyEndingElement(next, parentEvent)){
					
					checkMandatoryElement(hasTitle, parentEvent, TAXONTITLE);
					checkMandatoryElement(hasNomenclature, parentEvent, NOMENCLATURE);
					handleUnexpectedAttributes(parentEvent.getLocation(), attributes);
					
					state.setCurrentTaxon(null);
					save(taxon, state);
					return taxon;
				}else{
					if(isEndingElement(next, HEADING)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, TEXT_SECTION)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, KEY)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, NOTES)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, REFERENCES)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else{
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			}else if (next.isStartElement()){
				if(isStartingElement(next, HEADING)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, TAXONTITLE)){
					taxonTitle = handleTaxonTitle(state, reader, next);
					hasTitle = true;
				}else if(isStartingElement(next, WRITER)){
					List<FootnoteDataHolder> footNotes = new ArrayList<FootnoteDataHolder>();
					writerExtension = handleWriter(state, reader, next, footNotes);
					taxon.addExtension(writerExtension);
					//TODO what if taxonTitle comes later
					if (StringUtils.isNotBlank(taxonTitle) && writerExtension != null){
						String writer = writerExtension.getValue();
						Reference sec = ReferenceFactory.newBookSection();
						sec.setTitle(taxonTitle);
						TeamOrPersonBase author = createAuthor(writer);
						sec.setAuthorTeam(author);
						sec.setInReference(state.getConfig().getSourceReference());
						taxon.setSec(sec);
						registerFootnotes(state, sec, footNotes);
					}else{
						String message = "No taxontitle exists for writer";
						fireWarningEvent(message, next, 6);
					}
					
				}else if(isStartingElement(next, TEXT_SECTION)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, KEY)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, NOMENCLATURE)){
					handleNomenclature(state, reader, next);
					hasNomenclature = true;
				}else if(isStartingElement(next, FEATURE)){
					handleFeature(state, reader, next);
				}else if(isStartingElement(next, NOTES)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, REFERENCES)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, FIGURE)){
					handleFigure(state, reader, next);
				}else if(isStartingElement(next, FOOTNOTE)){
					FootnoteDataHolder footnote = handleFootnote(state, reader, next);
					if (footnote.isRef()){
						String message = "Ref footnote not implemented here";
						fireWarningEvent(message, next, 4);
					}else{
						registerGivenFootnote(state, footnote);
					}
				}else{
					handleUnexpectedStartElement(next);
				}
			}else{
				handleUnexpectedElement(next);
			}
		}
		//TODO handle missing end element
		throw new IllegalStateException("Taxon has no closing tag");
	}


	/**
	 * @param state
	 * @param attributes 
	 */
	private Taxon createTaxonAndName(MarkupImportState state, Map<String, Attribute> attributes) {
		NonViralName name;
		Rank rank = Rank.SPECIES(); //default
		boolean isCultivar = checkAndRemoveAttributeValue(attributes, CLASS, "cultivated");
		if (isCultivar){
			name = CultivarPlantName.NewInstance(rank);
		}else{
			name = createNameByCode(state, rank);
		}
		Taxon taxon = Taxon.NewInstance(name, state.getConfig().getSourceReference());
		if (checkAndRemoveAttributeValue(attributes, CLASS, "dubious")){
			taxon.setDoubtful(true);
		}else if (checkAndRemoveAttributeValue(attributes, CLASS, "excluded")){
			taxon.setExcluded(true);
		}
		//TODO insufficient, new, expected
		handleNotYetImplementedAttribute(attributes, CLASS);
		//From old version
//		MarkerType markerType = getMarkerType(state, attrValue);
//		if (markerType == null){
//			logger.warn("Class attribute value for taxon not yet supported: " + attrValue);
//		}else{
//			taxon.addMarker(Marker.NewInstance(markerType, true));
//		}
		
//		save(name, state);
//		save(taxon, state);
		return taxon;
	}


	/**
	 * @param state
	 * @param rank
	 * @return
	 */
	private NonViralName createNameByCode(MarkupImportState state, Rank rank) {
		NonViralName name;
		NomenclaturalCode nc = makeNomenclaturalCode(state);
		name = (NonViralName)nc.getNewTaxonNameInstance(rank);
		return name;
	}


	/**
	 * @param state
	 * @return
	 */
	private NomenclaturalCode makeNomenclaturalCode(MarkupImportState state) {
		NomenclaturalCode nc = state.getConfig().getNomenclaturalCode();
		if (nc == null){
			nc = NomenclaturalCode.ICBN;  //default;
		}
		return nc;
	}

	private String handleTaxonTitle(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		String text = "";
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String rankAttr = getAndRemoveAttributeValue(attributes, RANK);
		Rank rank = makeRank(state, rankAttr, false);
		//TODO
//		String numAttr = getAndRemoveAttributeValue(attributes, NUM);
		handleNotYetImplementedAttribute(attributes, NUM);
		checkNoAttributes(attributes, parentEvent);
		
		//TODO handle attributes
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()){
				if (isMyEndingElement(next, parentEvent)){
					Taxon taxon = state.getCurrentTaxon();
					String titleText = null;
					if (checkMandatoryText(text, parentEvent)){
						titleText = normalize(text);
						UUID uuidTitle = MarkupTransformer.uuidTaxonTitle;
						ExtensionType titleExtension = this.getExtensionType(state, uuidTitle, "Taxon Title ", "taxon title", "title");
						taxon.addExtension(titleText, titleExtension);
					}
					taxon.getName().setRank(rank);
					//TODO check title exists
					return titleText;
				}else{
					if(isEndingElement(next, FOOTNOTE)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else{
						handleUnexpectedEndElement(next.asEndElement());
						state.setUnsuccessfull();
					}
				}
			}else if (next.isStartElement()){
				if(isStartingElement(next, FOOTNOTE)){
					handleNotYetImplementedElement(next);
				}else{
					handleUnexpectedStartElement(next);
					state.setUnsuccessfull();
				}
			}else if (next.isCharacters()){
				text +=next.asCharacters().getData();
			
			}else{
				handleUnexpectedElement(next);
				state.setUnsuccessfull();
			}
		}
		return null;
		
		
	}
	
	private Extension handleWriter(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, List<FootnoteDataHolder> footNotes) throws XMLStreamException {
		String text = "";
		checkNoAttributes(parentEvent);
		
		//TODO handle attributes
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()){
				if (isMyEndingElement(next, parentEvent)){
					if (checkMandatoryText(text, parentEvent)){
						UUID uuidWriter = MarkupTransformer.uuidWriter;
						ExtensionType titleExtensionType = this.getExtensionType(state, uuidWriter, "Writer", "writer", "writer");
						Extension extension = Extension.NewInstance();
						extension.setType(titleExtensionType);
						extension.setValue(normalize(text));
						return extension;
					}else{
						return null;
					}
				}else{
					if(isEndingElement(next, FOOTNOTE)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else{
						handleUnexpectedEndElement(next.asEndElement());
						state.setUnsuccessfull();
					}
				}
			}else if (next.isStartElement()){
				if(isStartingElement(next, FOOTNOTE_REF)){
					FootnoteDataHolder footNote = handleFootnoteRef(state, reader, next);
					if (footNote.isRef()){
						footNotes.add(footNote);
					}else{
						logger.warn ("Non ref footnotes not yet impelemnted");
					}
				}else{
					handleUnexpectedStartElement(next);
					state.setUnsuccessfull();
				}
			}else if (next.isCharacters()){
				text +=next.asCharacters().getData();
			
			}else{
				handleUnexpectedElement(next);
				state.setUnsuccessfull();
			}
		}
		return null;
	}

	private void registerFootnotes(MarkupImportState state, AnnotatableEntity entity, List<FootnoteDataHolder> footnotes) {
		for (FootnoteDataHolder footNote : footnotes){
			registerFootnoteDemand(state, entity, footNote);
		}
	}

	private void registerGivenFootnote(MarkupImportState state, FootnoteDataHolder footnote) {
		state.registerFootnote(footnote);
		Set<AnnotatableEntity> demands = state.getFootnoteDemands(footnote.id);
		if (demands != null){
			for (AnnotatableEntity entity : demands){
				attachFootnote(state, entity, footnote);
			}
		}
	}


	private void registerGivenFigure(MarkupImportState state, String id, Media figure) {
		state.registerFigure(id, figure);
		Set<AnnotatableEntity> demands = state.getFigureDemands(id);
		if (demands != null){
			for (AnnotatableEntity entity : demands){
				attachFigure(state, entity, figure);
			}
		}
	}
	
	private void registerFootnoteDemand(MarkupImportState state, AnnotatableEntity entity, FootnoteDataHolder footnote) {
		FootnoteDataHolder existingFootnote = state.getFootnote(footnote.ref);
		if (existingFootnote != null ){
			attachFootnote(state, entity, existingFootnote);
		}else{
			Set<AnnotatableEntity> demands = state.getFootnoteDemands(footnote.ref);
			if (demands == null){
				demands = new HashSet<AnnotatableEntity>();
				state.putFootnoteDemands(footnote.ref, demands);
			}
			demands.add(entity);
		}
	}
	
	private void registerFigureDemand(MarkupImportState state, AnnotatableEntity entity, String figureRef) {
		Media existingFigure = state.getFigure(figureRef);
		if (existingFigure != null ){
			attachFigure(state, entity, existingFigure);
		}else{
			Set<AnnotatableEntity> demands = state.getFigureDemands(figureRef);
			if (demands == null){
				demands = new HashSet<AnnotatableEntity>();
				state.putFigureDemands(figureRef, demands);
			}
			demands.add(entity);
		}
	}


	private void attachFootnote(MarkupImportState state, AnnotatableEntity entity, FootnoteDataHolder footnote) {
		AnnotationType annotationType = this.getAnnotationType(state, MarkupTransformer.uuidFootnote, "Footnote", "An e-flora footnote", "fn", null);
		Annotation annotation = Annotation.NewInstance(footnote.string, annotationType, Language.DEFAULT());
		// TODO transient objects
		entity.addAnnotation(annotation);
		save(entity,state);
	}
	

	private void attachFigure(MarkupImportState state, AnnotatableEntity entity, Media figure) {
		//IdentifiableEntity<?> toSave;
		if (entity.isInstanceOf(TextData.class)){
			TextData deb = CdmBase.deproxy(entity, TextData.class);
			deb.addMedia(figure);
			//toSave = ((TaxonDescription)deb.getInDescription()).getTaxon();
		}else if (entity.isInstanceOf(IdentifiableMediaEntity.class)){
			IdentifiableMediaEntity<?> ime = CdmBase.deproxy(entity, IdentifiableMediaEntity.class);
			ime.addMedia(figure);
			//toSave = ime;
		}else{
			String message = "Unsupported entity to attach media: %s";
			message = String.format(message, entity.getClass().getName());
			//toSave = null;
		}
		save(entity,state);
	}
	

	private void handleFigure(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
//		FigureDataHolder result = new FigureDataHolder();

		
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String id = getAndRemoveAttributeValue(attributes, ID);
		String type = getAndRemoveAttributeValue(attributes, TYPE);
		checkNoAttributes(attributes, parentEvent);
		
		String urlString = null;
		String legendString = null;
		String titleString = null;
		String numString = null;
		String text = null;
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if(isMyEndingElement(next, parentEvent)){
				Media media = null;
				try {
					URL url = new URL(urlString);
					if ("lineart".equals(type)){
						media = Figure.NewInstance(url.toURI(), null, null, null);
					}else if (type == null || "photo".equals(type) || "signature".equals(type) || "others".equals(type) ){
						media = Media.NewInstance(url.toURI(), null, null, null);
					}else{
						String message = "Unknown figure type '%s'";
						message = String.format(message, type);
						//TODO location
						fireWarningEvent(message, "-", 2);
						media = Media.NewInstance(url.toURI(), null, null, null);
					}
					//title
					if (StringUtils.isNotBlank(titleString)){
						media.putTitle(Language.DEFAULT(), titleString);
					}
					//legend
					if (StringUtils.isNotBlank(legendString)){
						media.addDescription(legendString, Language.DEFAULT());
					}
					if (StringUtils.isNotBlank(numString)){
						//TODO use concrete source (e.g. DAPHNIPHYLLACEAE in FM vol.13)
						Reference citation = state.getConfig().getSourceReference();
						media.addSource(numString, "num", citation, null);
						//TODO name used in source if available
					}
					//TODO which citation
					if (StringUtils.isNotBlank(id)){
						media.addSource(id, null, state.getConfig().getSourceReference(), null);
					}else{
						String message = "Figure id should never be empty or null";
						fireWarningEvent(message, next, 6);
					}
					//text
					//do nothing

				} catch (MalformedURLException e) {
					String message = "Media uri has incorrect syntax: %s";
					message = String.format(message, urlString);
					fireWarningEvent(message, next, 4);
				} catch (URISyntaxException e) {
					String message = "Media uri has incorrect syntax: %s";
					message = String.format(message, urlString);
					fireWarningEvent(message, next, 4);
				}
				
				registerGivenFigure(state, id, media);
				return;
			}else if (isStartingElement(next, FIGURE_LEGEND)){
				//TODO same as figurestring ?
				legendString = handleFootnoteString(state, reader, next);
			}else if (isStartingElement(next, FIGURE_TITLE)){
				titleString = getCData(state, reader, next);
			}else if (isStartingElement(next, URL)){
				String localUrl = getCData(state, reader, next);
				urlString = CdmUtils.Nz(state.getBaseMediaUrl()) +  localUrl;
			}else if (isStartingElement(next, NUM)){
				numString = getCData(state, reader, next);
			}else if (next.isCharacters()){
				text +=next.asCharacters().getData();
			}else {
				fireUnexpectedEvent(next, 0);
			}
		}
		throw new IllegalStateException("<figure> has no end tag");		
	}
	
	private FigureDataHolder handleFigureRef(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		FigureDataHolder result = new FigureDataHolder();
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		result.ref = getAndRemoveAttributeValue(attributes, REF);
		checkNoAttributes(attributes, parentEvent);
		
		//text is not handled, needed only for debugging purposes
		String text = "";
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if(isMyEndingElement(next, parentEvent)){
				return result;
			}else if (isStartingElement(next, NUM)){
				String num = getCData(state, reader, next);
				result.num = num;  //num is not handled during import
			}else if (isStartingElement(next, FIGURE_PART)){
				result.figurePart = getCData(state, reader, next);
			}else if (next.isCharacters()){
				text +=next.asCharacters().getData();
			}else{
				fireUnexpectedEvent(next, 0);
			}
		}
		throw new IllegalStateException("<figureRef> has no end tag");		
	}


	private FootnoteDataHolder handleFootnote(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		FootnoteDataHolder result = new FootnoteDataHolder();
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		result.id = getAndRemoveAttributeValue(attributes, ID);
//		result.ref = getAndRemoveAttributeValue(attributes, REF);
		checkNoAttributes(attributes, parentEvent);
		
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (isStartingElement(next, FOOTNOTE_STRING)){
				String string = handleFootnoteString(state, reader, next);
				result.string = string;
			}else if(isMyEndingElement(next, parentEvent)){
				return result;
			}else{
				fireUnexpectedEvent(next, 0);
			}
		}
		return result;
	}
	
	private FootnoteDataHolder handleFootnoteRef(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		FootnoteDataHolder result = new FootnoteDataHolder();
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		result.ref = getAndRemoveAttributeValue(attributes, REF);
		checkNoAttributes(attributes, parentEvent);
		
		//text is not handled, needed only for debugging purposes
		String text = "";
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
//			if (isStartingElement(next, FOOTNOTE_STRING)){
//				String string = handleFootnoteString(state, reader, next);
//				result.string = string;
//			}else 
				if(isMyEndingElement(next, parentEvent)){
				return result;
			}else if (next.isCharacters()){
				text +=next.asCharacters().getData();
			
			}else{
				fireUnexpectedEvent(next, 0);
			}
		}
		return result;
	}


	private void handleNomenclature(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		checkNoAttributes(parentEvent);
		
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (isStartingElement(next, HOMOTYPES)){
				handleHomotypes(state, reader, next.asStartElement());
			}else if(isMyEndingElement(next, parentEvent)){
				return;
			}else{
				fireSchemaConflictEventExpectedStartTag(HOMOTYPES, reader);
				state.setUnsuccessfull();
			}
		}
		return;
	}

	private String handleFootnoteString(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		boolean isTextMode = true;
		String text = "";
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if(isMyEndingElement(next, parentEvent)){
				return text;
			}else if (next.isEndElement()){
				if (isEndingElement(next, FULL_NAME)){
					popUnimplemented(next.asEndElement());
				}else if (isEndingElement(next, COLLECTION)){
					popUnimplemented(next.asEndElement());
				}else if (isEndingElement(next, REFERENCES)){
					popUnimplemented(next.asEndElement());
				}else if (isEndingElement(next, BR)){
					isTextMode = true;
				}else if (isHtml(next)){
					text += "</" + getHtmlTag(next) + ">";
				}else {
					handleUnexpectedEndElement(next.asEndElement());
				}
			}else if (next.isStartElement()){
				if (isStartingElement(next, FULL_NAME)){
					handleNotYetImplementedElement(next);
				}else if (isStartingElement(next, COLLECTION)){
					handleNotYetImplementedElement(next);
				}else if (isStartingElement(next, REFERENCES)){
					handleNotYetImplementedElement(next);
				}else if (isStartingElement(next, BR)){
					text += "<br/>";
					isTextMode = false;
				}else if (isHtml(next)){
					text += "<" + getHtmlTag(next) + ">";
				}else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()){
				if (!isTextMode){
					String message = "footnoteString is not in text mode";
					fireWarningEvent(message, next, 6);
				}else{
					text += next.asCharacters().getData();
				}
			}else {
				handleUnexpectedEndElement(next.asEndElement());
			}
		}
		throw new IllegalStateException("<footnoteString> has no closing tag");

	}
	

	private void handleHomotypes(MarkupImportState state, XMLEventReader reader, StartElement parentEvent) throws XMLStreamException {
		checkNoAttributes(parentEvent);
		
		HomotypicalGroup homotypicalGroup = null;
		
		boolean hasNom = false;
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()){
				if (isMyEndingElement(next, parentEvent)){
					checkMandatoryElement(hasNom, parentEvent, NOM);
					return;
				}else{
					if(isEndingElement(next, NAME_TYPE)){
						state.setNameType(false);
					}else if(isEndingElement(next, NOTES)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else{
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			}else if (next.isStartElement()){
				if(isStartingElement(next, NOM)){
					NonViralName<?> name = handleNom(state, reader, next, homotypicalGroup);
					homotypicalGroup = name.getHomotypicalGroup();
					hasNom = true;
				}else if(isStartingElement(next, NAME_TYPE)){
					state.setNameType(true);
					handleNameType(state, reader, next);
				}else if(isStartingElement(next, SPECIMEN_TYPE)){
					handleSpecimenType(state, reader, next, homotypicalGroup);
				}else if(isStartingElement(next, NOTES)){
					handleNotYetImplementedElement(next);
				}else{
					handleUnexpectedStartElement(next);
				}
			}else{
				handleUnexpectedElement(next);
			}
		}
		//TODO handle missing end element
		throw new IllegalStateException("Homotypes has no closing tag");
		
	}


	private void handleNameType(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String typeStatus = getAndRemoveAttributeValue(attributes, TYPE_STATUS);
		checkNoAttributes(attributes, parentEvent);
		
		NameTypeDesignationStatus status;
		try {
			status = NameTypeParser.parseNameTypeStatus(typeStatus);
		} catch (UnknownCdmTypeException e) {
			String message = "Type status could not be recognized: %s";
			message = String.format(message, typeStatus);
			fireWarningEvent(message, parentEvent, 4);
		}
		
		boolean hasNom = false;
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()){
				if (isMyEndingElement(next, parentEvent)){
					checkMandatoryElement(hasNom, parentEvent.asStartElement(), NOM);
					return;
				}else{
					if(isEndingElement(next, ACCEPTED_NAME)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else{
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			}else if (next.isStartElement()){
				if(isStartingElement(next, NOM)){
					NonViralName name = handleNom(state, reader, next, null);
					hasNom = true;
				}else if(isStartingElement(next, ACCEPTED_NAME)){
					handleNotYetImplementedElement(next);
				}else{
					handleUnexpectedStartElement(next);
				}
			}else{
				handleUnexpectedElement(next);
			}
		}
		//TODO handle missing end element
		throw new IllegalStateException("Homotypes has no closing tag");
		
		
	}


	private void handleSpecimenType(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, HomotypicalGroup homotypicalGroup) throws XMLStreamException {
		//attributes
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String typeStatus = getAndRemoveAttributeValue(attributes, TYPE_STATUS);
		String notSeen = getAndRemoveAttributeValue(attributes, NOT_SEEN);
		String unknown = getAndRemoveAttributeValue(attributes, UNKNOWN);
		String notFound = getAndRemoveAttributeValue(attributes, NOT_FOUND);
		String destroyed = getAndRemoveAttributeValue(attributes, DESTROYED);
		String lost = getAndRemoveAttributeValue(attributes, LOST);
		checkNoAttributes(attributes, parentEvent);
		if (StringUtils.isNotEmpty(typeStatus)){
			//TODO
			//currently not needed
		} else if (StringUtils.isNotEmpty(notSeen)){
			handleNotYetImplementedAttribute(attributes, NOT_SEEN);
		}else if (StringUtils.isNotEmpty(unknown)){
			handleNotYetImplementedAttribute(attributes, UNKNOWN);
		}else if (StringUtils.isNotEmpty(notFound)){
			handleNotYetImplementedAttribute(attributes, NOT_FOUND);
		}else if (StringUtils.isNotEmpty(destroyed)){
			handleNotYetImplementedAttribute(attributes, DESTROYED);
		}else if (StringUtils.isNotEmpty(lost)){
			handleNotYetImplementedAttribute(attributes, LOST);
		}
		
		NonViralName firstName = null;
		Set<TaxonNameBase> names = homotypicalGroup.getTypifiedNames();
		if (names.isEmpty()){
			String message = "There is no name in a homotypical group. Can't create the specimen type";
			fireWarningEvent(message, parentEvent, 8);
		}else{
			firstName = CdmBase.deproxy(names.iterator().next(), NonViralName.class);
		}
		
		DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(DerivedUnitType.Specimen);
		String text = "";
		//elements
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()){
				if (isMyEndingElement(next, parentEvent)){
					makeSpecimenType(state, facade, text, firstName, parentEvent);
					return;
				}else{
					if(isEndingElement(next, FULL_TYPE)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next,TYPE_STATUS)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, ORIGINAL_DETERMINATION)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, SPECIMEN_TYPE)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, CITATION)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, NOTES)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, ANNOTATION)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else{
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			}else if (next.isStartElement()){
				if(isStartingElement(next, FULL_TYPE)){
					handleNotYetImplementedElement(next);
//					homotypicalGroup = handleNom(state, reader, next, taxon, homotypicalGroup);
				}else if(isStartingElement(next, TYPE_STATUS)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, COLLECTION)){
					handleCollection(state, reader, next, homotypicalGroup, facade);
				}else if(isStartingElement(next, ORIGINAL_DETERMINATION)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, SPECIMEN_TYPE)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, NOTES)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, ANNOTATION)){
					handleNotYetImplementedElement(next);
				}else{
					handleUnexpectedStartElement(next);
				}
			}else if (next.isCharacters()){
					text += next.asCharacters().getData();
			}else{
				handleUnexpectedElement(next);
			}
		}
		//TODO handle missing end element
		throw new IllegalStateException("Specimen type has no closing tag");		// TODO Auto-generated method stub
		
	}


	private void makeSpecimenType(MarkupImportState state, DerivedUnitFacade facade, String text, NonViralName name, XMLEvent parentEvent) {
		text = text.trim();
		//remove brackets
		if (text.matches("^\\(.*\\)\\.?$")){
			text = text.replaceAll("\\.", "");
			text = text.substring(1, text.length() - 1);
		}
		String[] split = text.split("[;,]");
		for (String str : split){
			str = str.trim();
			boolean addToAllNamesInGroup = true;
			TypeInfo typeInfo = makeSpecimenTypeTypeInfo(str, parentEvent);
			SpecimenTypeDesignationStatus typeStatus = typeInfo.status;
			Collection collection = createCollection(typeInfo.collectionString);
			
			//TODO improve cache strategy handling
			DerivedUnitBase typeSpecimen = facade.addDuplicate(collection, null, null, null, null);
			typeSpecimen.setCacheStrategy(new DerivedUnitFacadeCacheStrategy());
			name.addSpecimenTypeDesignation((Specimen)typeSpecimen, typeStatus, null, null, null, false, addToAllNamesInGroup);
		}
		
		
		
	}

	private Collection createCollection(String code) {
		//TODO deduplicate
		//TODO code <-> name
		Collection result = Collection.NewInstance();
		result.setCode(code);
		return result;
	}
	
	private TypeInfo makeSpecimenTypeTypeInfo(String originalString, XMLEvent event) {
		TypeInfo result = new TypeInfo();
		String[] split = originalString.split("\\s+");
		for (String str : split){
			if (str.matches(SpecimenTypeParser.typeTypePattern)){
				SpecimenTypeDesignationStatus status;
				try {
					status = SpecimenTypeParser.makeSpecimentTypeStatus(str);
				} catch (UnknownCdmTypeException e) {
					String message = "Specimen type status '%s' not recognized by parser";
					message = String.format(message, str);
					fireWarningEvent(message, event, 4);
					status = null;
				}
				result.status = status;
			}else if(str.matches( SpecimenTypeParser.collectionPattern)){
				result.collectionString = str;
			}else{
				String message = "Type part '%s' could not be recognized";
				message = String.format(message, str);
				fireWarningEvent(message, event, 2);
			}
		}
		
		return result;
	}


	private void handleCollection(MarkupImportState state,XMLEventReader reader, XMLEvent parentEvent, HomotypicalGroup homotypicalGroup, DerivedUnitFacade facade) throws XMLStreamException {
		checkNoAttributes(parentEvent);
		boolean hasCollector = false;
		boolean hasFieldNum = false;
		
		//elements
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()){
				if (isMyEndingElement(next, parentEvent)){
					checkMandatoryElement(hasCollector, parentEvent.asStartElement(), COLLECTOR);
					checkMandatoryElement(hasFieldNum, parentEvent.asStartElement(), FIELD_NUM);
					return;
				}else{
					if(isEndingElement(next,ALTERNATIVE_COLLECTOR)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, ALTERNATIVE_FIELD_NUM)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, COLLECTION_TYPE_STATUS)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, ALTERNATIVE_COLLECTION_TYPE_STATUS)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, SUB_COLLECTION)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, DATES)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, NOTES)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else{
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			}else if (next.isStartElement()){
				if(isStartingElement(next, COLLECTOR)){
					hasCollector = true;
					String collectorStr = getCData(state, reader, next);
					AgentBase<?> collector = createCollector(collectorStr);
					facade.setCollector(collector);
				}else if(isStartingElement(next, ALTERNATIVE_COLLECTOR)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, FIELD_NUM)){
					hasFieldNum = true;
					String fieldNumStr = getCData(state, reader, next);
					facade.setFieldNumber(fieldNumStr);
				}else if(isStartingElement(next, ALTERNATIVE_FIELD_NUM)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, COLLECTION_TYPE_STATUS)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, ALTERNATIVE_COLLECTION_TYPE_STATUS)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, SUB_COLLECTION)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, LOCALITY)){
					handleLocality(state, reader, next, facade);
				}else if(isStartingElement(next, DATES)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, NOTES)){
					handleNotYetImplementedElement(next);
				}else{
					handleUnexpectedStartElement(next);
				}
			}else{
				handleUnexpectedElement(next);
			}
		}
		//TODO handle missing end element
		throw new IllegalStateException("Collection has no closing tag");
		
	}


	private void handleLocality(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, DerivedUnitFacade facade) throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);
		boolean isLocality = false;
		NamedAreaLevel areaLevel = null;
		if ("locality".equalsIgnoreCase(classValue)){
			isLocality = true;
		}else{
			try {
				areaLevel = state.getTransformer().getNamedAreaLevelByKey(classValue);
			} catch (UndefinedTransformerMethodException e) {
				//do nothing
			}
			if (areaLevel == null){
				String message = "Named area level '%s' not yet implemented.";
				message = String.format(message, classValue);
				fireWarningEvent(message, parentEvent, 6);
			}
		}
		
		String text = "";
		//elements
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()){
				if (StringUtils.isNotBlank(text)){
					if (isMyEndingElement(next, parentEvent)){
						if (isLocality){
							facade.setLocality(text);
						}else{
							NamedArea area = createArea(text, areaLevel, state);
							facade.addCollectingArea(area);
						}
					}
					//TODO
					return;
				}else{
					if(isEndingElement(next, ALTITUDE)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next,COORDINATES)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, ANNOTATION)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else{
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			}else if (next.isStartElement()){
				if(isStartingElement(next, ALTITUDE)){
					handleNotYetImplementedElement(next);
//					homotypicalGroup = handleNom(state, reader, next, taxon, homotypicalGroup);
				}else if(isStartingElement(next, COORDINATES)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, ANNOTATION)){
					handleNotYetImplementedElement(next);
				}else{
					handleUnexpectedStartElement(next);
				}
			}else if (next.isCharacters()){
					text += next.asCharacters().getData();
			}else{
				handleUnexpectedElement(next);
			}
		}
		//TODO handle missing end element
		throw new IllegalStateException("Specimen type has no closing tag");		// TODO Auto-generated method stub
		
	}


	private NamedArea createArea(String text, NamedAreaLevel areaLevel, MarkupImportState state) {
		NamedArea area = NamedArea.NewInstance(text, text, null);
		area.setLevel(areaLevel);
		save(area, state);
		return area;
	}


	private AgentBase<?> createCollector(String collectorStr) {
		return createAuthor(collectorStr);
	}


	private String getCData(MarkupImportState state, XMLEventReader reader, XMLEvent next) throws XMLStreamException {
		checkNoAttributes(next);
		
		String text = "";
		while (reader.hasNext()){
			XMLEvent myNext = readNoWhitespace(reader);
			if(isMyEndingElement(myNext, next)){
				return text;
			}else if (myNext.isCharacters()){
				text += myNext.asCharacters().getData();
			}else {
				handleUnexpectedElement(myNext);
			}
		}
		throw new IllegalStateException("Event has no closing tag");
		
	}


	/**
	 * Creates the name defined by a nom tag. Adds it to the given homotypical group (if not null). 
	 * @param state
	 * @param reader
	 * @param parentEvent
	 * @param homotypicalGroup
	 * @return
	 * @throws XMLStreamException
	 */
	private NonViralName<?> handleNom(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, 
				HomotypicalGroup homotypicalGroup) throws XMLStreamException {
		boolean isSynonym = false;
		boolean isNameType = state.isNameType();
		//attributes
		String classValue = getClassOnlyAttribute(parentEvent);
		NonViralName<?> name;
		if (! isNameType && ACCEPTED.equalsIgnoreCase(classValue)){
			isSynonym = false;
			name = createName(state, homotypicalGroup, isSynonym);
		}else if (! isNameType && SYNONYM.equalsIgnoreCase(classValue)){
			isSynonym = true;
			name = createName(state, homotypicalGroup, isSynonym);
		}else if (isNameType && NAME_TYPE.equalsIgnoreCase(classValue)){
			//TODO do we need to define the rank here?
			name = createNameByCode(state, null);
		}else{
			fireUnexpectedAttributeValue(parentEvent, CLASS, classValue);
			name = createNameByCode(state, null);
		}
		
		Map<String, String> nameMap = new HashMap<String, String>();
		
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()){
				if (isMyEndingElement(next, parentEvent)){
					//fill the name with all data gathered
					fillName(state, nameMap, name, next);
					return name;
				}else{
					if(isEndingElement(next, FULL_NAME)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, NUM)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, HOMONYM)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, NOTES)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, ANNOTATION)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else{
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			}else if (next.isStartElement()){
				if(isStartingElement(next, FULL_NAME)){
					handleNotYetImplementedElement(next);
//					homotypicalGroup = handleNom(state, reader, next, taxon, homotypicalGroup);
				}else if(isStartingElement(next, NUM)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, NAME)){
					handleName(state, reader, next, nameMap);
				}else if(isStartingElement(next, CITATION)){
					handleCitation(state, reader, next, name);
				}else if(isStartingElement(next, HOMONYM)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, NOTES)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, ANNOTATION)){
					handleNotYetImplementedElement(next);
				}else{
					handleUnexpectedStartElement(next);
				}
			}else{
				handleUnexpectedElement(next);
			}
		}
		//TODO handle missing end element
		throw new IllegalStateException("Nom has no closing tag");
		
	}

	private void fillName(MarkupImportState state, Map<String, String> nameMap, NonViralName name, XMLEvent event) {
		
		//Ranks: family, subfamily, tribus, genus, subgenus, section, subsection, species, subspecies, variety, subvariety, forma
		//infrank, paraut, author, infrparaut, infraut, status, notes
		
		
		String infrank = getAndRemoveMapKey(nameMap, INFRANK);
		String authorStr = getAndRemoveMapKey(nameMap, AUTHOR);
		String paraut = getAndRemoveMapKey(nameMap, PARAUT);
		
		String infrParAut = getAndRemoveMapKey(nameMap, INFRPARAUT);
		String infrAut = getAndRemoveMapKey(nameMap, INFRAUT);
		
		String statusStr = getAndRemoveMapKey(nameMap, STATUS);
		String notes = getAndRemoveMapKey(nameMap, NOTES);
		
		makeRankDecision(state, nameMap, name, event, infrank);
		
		//test consistency of rank and authors
		testRankAuthorConsistency(name, event, authorStr, paraut, infrParAut, infrAut);

		//authors
		makeNomenclaturalAuthors(name, event, authorStr, paraut, infrParAut, infrAut);
		
		//status
		//TODO handle pro parte, pro syn. etc.
		if (StringUtils.isNotBlank(statusStr)){
			String proPartePattern = "(pro parte|p.p.)";
			if (statusStr.matches(proPartePattern)){
				state.setProParte(true);
			}
			try {
				//TODO handle trim earlier
				statusStr = statusStr.trim();
				NomenclaturalStatusType nomStatusType = NomenclaturalStatusType.getNomenclaturalStatusTypeByAbbreviation(statusStr);
				name.addStatus(NomenclaturalStatus.NewInstance(nomStatusType));
			} catch (UnknownCdmTypeException e) {
				String message = "Status '%s' could not be recognized";
				message = String.format(message, statusStr);
				fireWarningEvent(message, event, 4);
			}
		}	
	
		//notes
		if (StringUtils.isNotBlank(notes)){
			handleNotYetImplementedAttributeValue(event, CLASS, NOTES);
		}
		
		return;
	}


	/**
	 * @param state
	 * @param nameMap
	 * @param name
	 * @param event
	 * @param infrank
	 */
	private void makeRankDecision(MarkupImportState state, Map<String, String> nameMap,
			NonViralName name, XMLEvent event, String infrank) {
		//TODO ranks
		for (String key : nameMap.keySet()){
			Rank rank = makeRank(state, key, false);
			if (rank == null){
				handleNotYetImplementedAttributeValue(event, CLASS, key);
			}else{
				if (name.getRank() == null || rank.isLower(name.getRank())){
					name.setRank(rank);
				}
				String value = nameMap.get(key);
				if (rank.isSupraGeneric() || rank.isGenus()){
					name.setGenusOrUninomial(value);
				}else if (rank.isInfraGeneric()){
					name.setInfraGenericEpithet(value);
				}else if (rank.isSpecies()){
					name.setSpecificEpithet(value);
				}else if (rank.isInfraSpecific()){
					name.setInfraSpecificEpithet(value);
				}else{
					String message = "Invalid rank '%s'. Can't decide which epithet to fill with '%s'";
					message = String.format(message, rank.getTitleCache(), value);
					fireWarningEvent(message, event, 4);
				}
			}
			
		}
		if (StringUtils.isNotBlank(infrank)){
			Rank rank = makeRank(state, infrank, true);
			
			if (rank == null){
				String message = "Infrank '%s' rank not recognized"; 
				message = String.format(message, infrank);
				fireWarningEvent(message, event, 4);
			}else{
				if (name.getRank() == null || rank.isLower(name.getRank())){
					name.setRank(rank);
				}else{
					String message = "InfRank '%s' is not lower than existing rank ";
					message = String.format(message, infrank);
					fireWarningEvent(message, event, 2);
				}
			}
		}
	}


	/**
	 * @param name
	 * @param event
	 * @param authorStr
	 * @param paraut
	 * @param infrParAut
	 * @param infrAut
	 */
	private void makeNomenclaturalAuthors(NonViralName name, XMLEvent event,
			String authorStr, String paraut, String infrParAut, String infrAut) {
		if (name.getRank().isInfraSpecific()){
			if(StringUtils.isNotBlank(infrAut)){
				INomenclaturalAuthor[] authorAndEx = authorAndEx(infrAut, event);
				name.setCombinationAuthorTeam(authorAndEx[0]);
				name.setExCombinationAuthorTeam(authorAndEx[1]);
			}
			if(StringUtils.isNotBlank(infrParAut)){
				INomenclaturalAuthor[] authorAndEx = authorAndEx(infrParAut, event);
				name.setBasionymAuthorTeam(authorAndEx[0]);
				name.setExBasionymAuthorTeam(authorAndEx[1]);
			}
		}else{
			if(StringUtils.isNotBlank(authorStr)){
				INomenclaturalAuthor[] authorAndEx = authorAndEx(authorStr, event);
				name.setCombinationAuthorTeam(authorAndEx[0]);
				name.setExCombinationAuthorTeam(authorAndEx[1]);
			}
			if(StringUtils.isNotBlank(paraut)){
				INomenclaturalAuthor[] authorAndEx = authorAndEx(paraut, event);
				name.setBasionymAuthorTeam(authorAndEx[0]);
				name.setExBasionymAuthorTeam(authorAndEx[1]);
			}
		}
	}


	private TeamOrPersonBase[] authorAndEx(String authorAndEx, XMLEvent xmlEvent) {
		authorAndEx = authorAndEx.trim();
		TeamOrPersonBase[] result = new TeamOrPersonBase[2];
		
		String[] split = authorAndEx.split("\\sex\\s");
		if (split.length > 2){
			String message = "There is more then 1 ' ex ' in author string. Can't separate author and ex-author";
			fireWarningEvent(message, xmlEvent, 4);
			result[0] = createAuthor(authorAndEx);
		}else if (split.length ==2){
			result[0] = createAuthor(split[1]);
			result[1] = createAuthor(split[0]);
		}else{
			result[0] = createAuthor(split[0]);
		}
		return result;
	}


	/**
	 * @param name
	 * @param event
	 * @param authorStr
	 * @param paraut
	 * @param infrParAut
	 * @param infrAut
	 */
	private void testRankAuthorConsistency(NonViralName name, XMLEvent event,
			String authorStr, String paraut, String infrParAut, String infrAut) {
		if (name.getRank().isInfraSpecific()){
			if (StringUtils.isBlank(infrParAut) && StringUtils.isNotBlank(infrAut) &&
					(StringUtils.isNotBlank(paraut) || StringUtils.isNotBlank(authorStr))){
				String message = "Rank is infraspecicific but has only specific or higher author(s)";
				fireWarningEvent(message, event, 4);
			}
		}else{
			//is not infraspecific
			if (StringUtils.isNotBlank(infrParAut) || StringUtils.isNotBlank(infrAut)){
				String message = "Rank is not infraspecicific but name has infra author(s)";
				fireWarningEvent(message, event, 4);
			}
		}
	}


	/**
	 * Returns the (empty) name with the correct homotypical group depending on the taxon status.
	 * Throws NPE if no currentTaxon is set in state.
	 * @param state
	 * @param homotypicalGroup
	 * @param isSynonym
	 * @return
	 */
	private NonViralName<?> createName(MarkupImportState state,HomotypicalGroup homotypicalGroup, boolean isSynonym) {
		NonViralName<?> name;
		Taxon taxon = state.getCurrentTaxon();
		if (isSynonym){
			Rank defaultRank = Rank.SPECIES();  //can be any
			name = createNameByCode(state, defaultRank);
			name.setHomotypicalGroup(homotypicalGroup);
			SynonymRelationshipType synonymType = SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
			if (taxon.getHomotypicGroup().equals(homotypicalGroup)){
				synonymType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
			}
			taxon.addSynonymName(name, synonymType);
		}else{
			name = CdmBase.deproxy(taxon.getName(), NonViralName.class);
		}
		return name;
	}
	
	private void handleName(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, Map<String, String> nameMap) throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);
		
		String text = "";
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if(isMyEndingElement(next, parentEvent)){
				nameMap.put(classValue, text);
				return;
			}else if (next.isStartElement()){
				if (isStartingElement(next, ANNOTATION)){
					handleNotYetImplementedElement(next);
				}else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()){
				text += next.asCharacters().getData();
			}else {
				handleUnexpectedEndElement(next.asEndElement());
			}
		}
		throw new IllegalStateException("name has no closing tag");
		
	}


	/**
	 * @param state
	 * @param classValue
	 * @param byAbbrev 
	 * @return
	 */
	private Rank makeRank(MarkupImportState state, String value, boolean byAbbrev) {
		Rank rank = null;
		if (StringUtils.isBlank(value)){
			return null;
		}
		try {
			boolean useUnknown = true;
			NomenclaturalCode nc = makeNomenclaturalCode(state);
			if (byAbbrev){
				rank = Rank.getRankByAbbreviation(value, nc, useUnknown);
			}else{	
				rank = Rank.getRankByEnglishName(value, nc, useUnknown);
			}
			if (rank.equals(Rank.UNKNOWN_RANK())){
				rank = null;
			}
		} catch (UnknownCdmTypeException e) {
			//doNothing
		}
		return rank;
	}
	

//	public void handleNameNotRank(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, String classValue, NonViralName name) throws XMLStreamException {
//		if (ACCEPTED.equalsIgnoreCase(classValue)){
//		}else if (SYNONYM.equalsIgnoreCase(classValue)){
//		}else{
//			//TODO Not yet implemented
//			handleNotYetImplementedAttributeValue(parentEvent, CLASS, classValue);
//		}
//	}
	

	private void handleCitation(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, NonViralName name) throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);

		state.setCitation(true);
		boolean hasRefPart = false;
		Reference reference = ReferenceFactory.newGeneric();
		Map<String, String> refMap = new HashMap<String, String>();
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)){
				checkMandatoryElement(hasRefPart, parentEvent.asStartElement(), REF_PART);
				reference = createReference(state, refMap, next);
				String microReference = refMap.get(DETAILS);
				doCitation(state, name, classValue, reference, microReference, parentEvent);
				state.setCitation(false);
				return;
			}else if(isStartingElement(next, REF_PART)){
				handleRefPart(state, reader, next, refMap);
				hasRefPart = true;
			}else{
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("Citation has no closing tag");
		
		
	}

	private void handleRefPart(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, Map<String, String> refMap) throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);
		
		String text = "";
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if(isMyEndingElement(next, parentEvent)){
				refMap.put(classValue, text);
				return;
			}else if (next.isStartElement()){
				if (isStartingElement(next, ANNOTATION)){
					handleNotYetImplementedElement(next);
				}else if (isStartingElement(next, ITALICS)){
					handleNotYetImplementedElement(next);
				}else if (isStartingElement(next, BOLD)){
					handleNotYetImplementedElement(next);
				}else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()){
				text += next.asCharacters().getData();
			}else {
				handleUnexpectedEndElement(next.asEndElement());
			}
		}
		throw new IllegalStateException("RefPart has no closing tag");
		
	}
	

	private Reference createReference(MarkupImportState state, Map<String, String> refMap, XMLEvent parentEvent) {
		//TODO
		Reference reference;
		
		String type = getAndRemoveMapKey(refMap, PUBTYPE);
		String authorStr = getAndRemoveMapKey(refMap, AUTHOR);
		String titleStr = getAndRemoveMapKey(refMap, PUBTITLE);
		String titleCache = getAndRemoveMapKey(refMap, PUBFULLNAME);
		String volume = getAndRemoveMapKey(refMap, VOLUME);
		String edition = getAndRemoveMapKey(refMap, EDITION);
		String editors = getAndRemoveMapKey(refMap, EDITORS);
		String year = getAndRemoveMapKey(refMap, YEAR);
		String pubName = getAndRemoveMapKey(refMap, PUBNAME);
		
		
		if (state.isCitation()){
			if (volume != null || "journal".equalsIgnoreCase(type)){
				IArticle article = ReferenceFactory.newArticle();
				if (pubName != null){
					IJournal journal = ReferenceFactory.newJournal();
					journal.setTitle(pubName);
					article.setInJournal(journal);
				}
				reference = (Reference)article; 
				
			}else{
				Reference bookOrPartOf = ReferenceFactory.newGeneric();
				reference = bookOrPartOf;
			}
			//TODO use existing author from name or before
			TeamOrPersonBase author = createAuthor(authorStr);
			reference.setAuthorTeam(author);
			
			
		}else{
			reference = ReferenceFactory.newGeneric();
			//TODO type
			TeamOrPersonBase author = createAuthor(authorStr);
			reference.setAuthorTeam(author);
			
			reference.setTitle(titleStr);
			if (StringUtils.isNotBlank(titleCache)){
				reference.setTitleCache(titleCache, true);
			}
			reference.setEdition(edition);
			reference.setEditor(editors);
			
			if (pubName != null){
				Reference inReference = ReferenceFactory.newGeneric();
				inReference.setTitle(pubName);
				reference.setInReference(inReference);
			}
		}
		reference.setVolume(volume);
		reference.setDatePublished(TimePeriod.parseString(year));
		
		
		//TODO
//		ALTERNATEPUBTITLE
//		ISSUE
//		NOTES
		for (String key : refMap.keySet()){
			if (! DETAILS.equalsIgnoreCase(key)){
				this.fireUnexpectedAttributeValue(parentEvent, CLASS, key);
			}
		}


		
		return reference;
	}

	private TeamOrPersonBase createAuthor(String authorTitle) {
		//TODO atomize and also use by name creation
		TeamOrPersonBase result = Team.NewTitledInstance(authorTitle, authorTitle);
		return result;
	}


	private String getAndRemoveMapKey(Map<String, String> map, String key){
		String result = map.get(key);
		map.remove(key);
		if (result != null){
			result = normalize(result);
		}
		return StringUtils.stripToNull(result);
	}

	private void doCitation(MarkupImportState state, NonViralName name, String classValue, Reference reference, String microCitation, XMLEvent parentEvent) {
		if (PUBLICATION.equalsIgnoreCase(classValue)){
			name.setNomenclaturalReference(reference);
			name.setNomenclaturalMicroReference(microCitation);
		}else if (USAGE.equalsIgnoreCase(classValue)){
			Taxon taxon = state.getCurrentTaxon();
			TaxonDescription td = this.getTaxonDescription(taxon, state.getConfig().getSourceReference(), false, true);
			TextData citation = TextData.NewInstance(Feature.CITATION());
			//TODO name used in source
			citation.addSource(null, null, reference, microCitation);
			td.addElement(citation);
		}else if (TYPE.equalsIgnoreCase(classValue)){
			handleNotYetImplementedAttributeValue(parentEvent, CLASS, classValue);
		}else{
			//TODO Not yet implemented
			handleNotYetImplementedAttributeValue(parentEvent, CLASS, classValue);
		}
	}
	

	private void handleFeature(MarkupImportState state, XMLEventReader reader,XMLEvent parentEvent) throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);
		Feature feature = makeFeature(classValue, state, parentEvent);
		Taxon taxon = state.getCurrentTaxon();
		TaxonDescription taxonDescription = getTaxonDescription(taxon, state.getConfig().getSourceReference(), NO_IMAGE_GALLERY, CREATE_NEW);
//		TextData figureHolderTextData = null;   //for use with one TextData for all figure only
		
		boolean isDescription = feature.equals(Feature.DESCRIPTION());
		DescriptionElementBase lastDescriptionElement = null;
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()){
				if (isMyEndingElement(next, parentEvent)){
					return;
				}else{
					if(isEndingElement(next, WRITER)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, DISTRIBUTION_LIST)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, HABITAT_LIST)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, REFERENCES)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else{
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			}else if (next.isStartElement()){
				if(isStartingElement(next, HEADING)){
					String heading = handleHeading(state, reader, next);
					if (StringUtils.isNotBlank(heading)){
						if (! heading.equalsIgnoreCase(classValue)){
							String message = "Feature heading '%s' differs from feature class '%s'";
							message = String.format(message, heading, classValue);
							fireWarningEvent(message, next, 1);
						}else{
							//do nothing
						}
					}
				}else if(isStartingElement(next, WRITER)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, DISTRIBUTION_LIST)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, HABITAT_LIST)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, CHAR)){
					TextData textData = handleChar (state,reader, next);
					taxonDescription.addElement(textData);
				}else if(isStartingElement(next, STRING)){
					String string = handleString(state, reader, next);
					TextData textData = TextData.NewInstance(feature);
					textData.putText(Language.DEFAULT(), string);
					taxonDescription.addElement(textData);
					lastDescriptionElement = textData;
				}else if(isStartingElement(next, FIGURE_REF)){
					FigureDataHolder figureHolder = handleFigureRef(state, reader, next);
					Feature figureFeature  = getFeature(state, MarkupTransformer.uuidFigures, "Figures", "Figures", "Fig.", null);
					if (isDescription){
						TextData figureHolderTextData = null;
//						if (figureHolderTextData == null){
						figureHolderTextData = TextData.NewInstance(figureFeature);
						if (StringUtils.isNotBlank(figureHolder.num)){
							String annotationText = "<num>" + figureHolder.num.trim() + "</num>";
							Annotation annotation = Annotation.NewInstance(annotationText, AnnotationType.TECHNICAL(), Language.DEFAULT());
							figureHolderTextData.addAnnotation(annotation);
						}
						if (StringUtils.isNotBlank(figureHolder.figurePart)){
							String annotationText = "<figurePart>" + figureHolder.figurePart.trim() + "</figurePart>";
							Annotation annotation = Annotation.NewInstance(annotationText, AnnotationType.EDITORIAL(), Language.DEFAULT());
							figureHolderTextData.addAnnotation(annotation);
						}
//						if (StringUtils.isNotBlank(figureText)){
//							figureHolderTextData.putText(Language.DEFAULT(), figureText);
//						}
						taxonDescription.addElement(figureHolderTextData);
//						}
						registerFigureDemand(state, figureHolderTextData, figureHolder.ref);
					}else{	
						if (lastDescriptionElement == null){
							String message = "No description element created yet that can be referred by figure. Create new TextData instead";
							fireWarningEvent(message, next, 4);
							lastDescriptionElement = TextData.NewInstance(figureFeature);
							taxonDescription.addElement(lastDescriptionElement);
						}
						registerFigureDemand(state, lastDescriptionElement, figureHolder.ref);		
					}
				}else if(isStartingElement(next, REFERENCES)){
					handleNotYetImplementedElement(next);
				}else{
					handleUnexpectedStartElement(next);
				}
			}else{
				handleUnexpectedElement(next);
			}
		}
		//TODO handle missing end element
		throw new IllegalStateException("Feature has no closing tag");
	}
	



	private String handleHeading(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		checkNoAttributes(parentEvent);
		
		String text = "";
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if(isMyEndingElement(next, parentEvent)){
				return text;
			}else if (next.isStartElement()){
				if (isStartingElement(next, FOOTNOTE)){
					handleNotYetImplementedElement(next);
				}else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()){
				text += next.asCharacters().getData();
			}else {
				handleUnexpectedEndElement(next.asEndElement());
			}
		}
		throw new IllegalStateException("<String> has no closing tag");
		
	}


	private String handleString(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
//		String classValue = getClassOnlyAttribute(parentEvent);
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		checkNoAttributes(attributes, parentEvent);
		
		boolean isTextMode = true;
		String text = "";
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if(isMyEndingElement(next, parentEvent)){
				return text;
			}else if (next.isEndElement()){
				if (isEndingElement(next, SUB_HEADING)){
					popUnimplemented(next.asEndElement());
				}else if (isEndingElement(next, ANNOTATION)){
					popUnimplemented(next.asEndElement());
				}else if (isEndingElement(next, BR)){
					isTextMode = true;
				}else if (isHtml(next)){
					text += "</" + getHtmlTag(next) + ">";
				}else {
					handleUnexpectedEndElement(next.asEndElement());
				}
			}else if (next.isStartElement()){
				if (isStartingElement(next, SUB_HEADING)){
					handleNotYetImplementedElement(next);
				}else if (isStartingElement(next, ANNOTATION)){
					handleNotYetImplementedElement(next);
				}else if (isStartingElement(next, BR)){
					text += "<br/>";
					isTextMode = false;
				}else if (isHtml(next)){
					text += "<" + getHtmlTag(next) + ">";
				}else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()){
				if (!isTextMode){
					String message = "String is not in text mode";
					fireWarningEvent(message, next, 6);
				}else{
					text += next.asCharacters().getData();
				}
			}else {
				handleUnexpectedEndElement(next.asEndElement());
			}
		}
		throw new IllegalStateException("<String> has no closing tag");
		
	}

	private String getHtmlTag(XMLEvent event) {
		if (event.isStartElement() ){
			String tag = event.asStartElement().getName().getLocalPart();
			return tag;
		}else if (event.isEndElement() ){
			String tag = event.asEndElement().getName().getLocalPart();
			return tag;
		}else{
			String message = "Only start or end elements are allowed as Html tags";
			throw new IllegalStateException(message);
		}
	}

	protected static final List<String> htmlList = Arrays.asList(
			"sub", "sup", "ol", "ul", "i", "b", "table", "br");
	
	
	private boolean isHtml(XMLEvent event) {
		if (event.isStartElement() ){
			String tag = event.asStartElement().getName().getLocalPart();
			return htmlList.contains(tag);
		}else if (event.isEndElement() ){
			String tag = event.asEndElement().getName().getLocalPart();
			return htmlList.contains(tag);
		}else{
			return false;
		}
			
	}

	private TextData handleChar(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);
		Feature feature = makeFeature(classValue, state, parentEvent);
		
		String text = "";
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if(isMyEndingElement(next, parentEvent)){
				TextData textData = TextData.NewInstance(feature);
				textData.putText(Language.DEFAULT(), text);
				return textData;
			}else if (next.isStartElement()){
				if (isStartingElement(next, ANNOTATION)){
					handleNotYetImplementedElement(next);
				}else if (isStartingElement(next, ITALICS)){
					handleNotYetImplementedElement(next);
				}else if (isStartingElement(next, BOLD)){
					handleNotYetImplementedElement(next);
				}else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()){
				text += next.asCharacters().getData();
			}else {
				handleUnexpectedEndElement(next.asEndElement());
			}
		}
		throw new IllegalStateException("RefPart has no closing tag");
	}


	/**
	 * @param classValue
	 * @param state 
	 * @param parentEvent 
	 * @return
	 * @throws UndefinedTransformerMethodException 
	 */
	private Feature makeFeature(String classValue, MarkupImportState state, XMLEvent parentEvent) {
		UUID uuid;
		try {
			Feature feature = state.getTransformer().getFeatureByKey(classValue);
			if (feature != null){
				return feature;
			}
			uuid = state.getTransformer().getFeatureUuid(classValue);
			if (uuid == null){
				logger.info("Uuid is null for " + classValue);
			}
			String featureText = StringUtils.capitalize(classValue);
			
			//TODO eFlora vocabulary
			TermVocabulary<Feature> voc = null;
			feature = getFeature(state, uuid, featureText, featureText, classValue, voc);
			if (feature == null){
				throw new NullPointerException(classValue + " not recognized as a feature");
			}
			return feature;
		} catch (Exception e) {
			String message = "Could not create feature for %s: %s" ;
			message = String.format(message, classValue, e.getMessage());
			fireWarningEvent(message, parentEvent, 4);
			return Feature.UNKNOWN();
		}
	}
		


	/**
	 * This comes from the old version, needs to be checked on need
	 * @param state
	 */
	private void doAllTheOldOtherStuff(MarkupImportState state) {
		state.putTree(null, null);
		if (unmatchedLeads == null){
			unmatchedLeads = UnmatchedLeads.NewInstance();
		}
		state.setUnmatchedLeads(unmatchedLeads);
		
//		TransactionStatus tx = startTransaction();
		unmatchedLeads.saveToSession(getPolytomousKeyNodeService());
		
		
		//TODO generally do not store the reference object in the config
		Reference sourceReference = state.getConfig().getSourceReference();
		getReferenceService().saveOrUpdate(sourceReference);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(MarkupImportState state){
		return ! state.getConfig().isDoTaxa();
	}




}
