/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.markup;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
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

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;


/**
 * @author a.mueller
 *
 */
@Component
public class MarkupDocumentImport  extends MarkupImportBase implements ICdmIO<MarkupImportState> {
	private static final String ORIGINAL_DETERMINATION = "originalDetermination";

	private static final String COLLECTION = "collection";

	private static final String FULL_TYPE = "fullType";

	private static final String NOT_FOUND = "notFound";

	private static final Logger logger = Logger.getLogger(MarkupDocumentImport.class);

	private static final boolean CREATE_NEW = true;
	private static final boolean IS_IMAGE_GALLERY = true;
	private static final boolean NO_IMAGE_GALLERY = false;

	private static final String ACCEPTED = "accepted";
	private static final String ADDENDA = "addenda";
	private static final String ALTERNATEPUBTITLE = "alternatepubtitle";
	private static final String ANNOTATION = "annotation";
	private static final String AUTHOR = "author";
	private static final String BIOGRAPHIES = "biographies";
	private static final String BOLD = "bold";
	private static final String CHAR = "char";
	private static final String CITATION = "citation";
	private static final String DESTROYED = "destroyed";
	private static final String DETAILS = "details";
	private static final String DISTRIBUTION_LIST = "distributionList";
	private static final String EDITION = "edition";
	private static final String EDITORS = "editors";
	private static final String EXAUT = "exaut";
	private static final String FEATURE = "feature";
	private static final String FIGURE = "figure";
	private static final String FOOTNOTE = "footnote";
	private static final String FULL_NAME = "fullName";
	private static final String HEADING = "heading";
	private static final String HABITAT_LIST = "habitatList";
	private static final String HOMONYM = "homonym";
	private static final String HOMOTYPES = "homotypes";
	private static final String INFRANK = "infrank";
	private static final String INFRAUT = "infraut";
	private static final String INFREX = "infrex";
	private static final String INFRPARAUT = "infrparaut";
	private static final String INFRPAREX = "infrparex";
	private static final String ISSUE = "issue";
	private static final String ITALICS = "italics";
	private static final String KEY = "key";
	private static final String LOST = "lost";
	private static final String META_DATA = "metaData";
	private static final String NAME = "name";
	private static final String NAME_TYPE = "nameType";
	private static final String NOM = "nom";
	private static final String NOMENCLATURE = "nomenclature";
	private static final String NOT_SEEN = "notSeen";
	private static final String NOTES = "notes";
	private static final String NUM = "num";
	private static final String PARAUT = "paraut";
	private static final String PUBFULLNAME = "pubfullname";
	private static final String PUBLICATION = "publication";
	private static final String PUBNAME = "pubname";
	private static final String PUBTITLE = "pubtitle";
	private static final String PUBTYPE = "pubtype";
	private static final String RANK = "rank";
	private static final String REFERENCES = "references";
	private static final String REF_PART = "refPart";
	private static final String TAXON = "taxon";
	private static final String TAXONTITLE = "taxontitle";
	private static final String TEXT_SECTION = "textSection";
	private static final String TYPE = "type";
	private static final String TYPE_STATUS = "typeStatus";
	private static final String TREATMENT = "treatment";
	private static final String SPECIMEN_TYPE = "specimenType";
	private static final String STRING = "string";
	private static final String SYNONYM = "synonym";
	private static final String UNKNOWN = "unknown";
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
						fireWarningEvent(message, makeLocationStr(nextEvent.getLocation()), 8);
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
					if(isEndingElement(event, META_DATA)){
						//NOT YET IMPLEMENTED
						popUnimplemented(event.asEndElement());
					}else if(isStartingElement(event, TREATMENT)){
						//NOT YET IMPLEMENTED
						popUnimplemented(event.asEndElement());
					}else if(isStartingElement(event, BIOGRAPHIES)){
						//NOT YET IMPLEMENTED
						popUnimplemented(event.asEndElement());
					}else if(isStartingElement(event, REFERENCES)){
						//NOT YET IMPLEMENTED
						popUnimplemented(event.asEndElement());
					}else if(isStartingElement(event, TEXT_SECTION)){
						//NOT YET IMPLEMENTED
						popUnimplemented(event.asEndElement());
					}else if(isStartingElement(event, ADDENDA)){
						//NOT YET IMPLEMENTED
						popUnimplemented(event.asEndElement());
					}else{
						handleUnexpectedElement(event);
					}
				}
			}else if (event.isStartElement()){
				if(isStartingElement(event, META_DATA)){
					handleNotYetImplementedElement(event);
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
		return;
	}

	private void handleTreatment(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		checkNoAttributes(parentEvent);
		Taxon lastTaxon = null;
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (isStartingElement(next, TAXON)){
				Taxon thisTaxon = handleTaxon(state, reader, next.asStartElement(), lastTaxon);
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


	private Taxon handleTaxon(MarkupImportState state, XMLEventReader reader, StartElement parentEvent, Taxon lastTaxon) throws XMLStreamException {
		//TODO progress monitoring
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		Taxon taxon = createTaxonAndName(state, attributes);
		state.setCurrentTaxon(taxon);
		
		boolean hasTitle = false;
		boolean hasNomenclature = false;
		String taxonTitle = null;
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
					}else if(isEndingElement(next, FIGURE)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, FOOTNOTE)){
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
					Extension writerExtension = handleWriter(state, reader, next);
					
					
					taxon.addExtension(writerExtension);
				}else if(isStartingElement(next, TEXT_SECTION)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, KEY)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, NOMENCLATURE)){
					handleNomenclature(state, reader, next, taxon);
					hasNomenclature = true;
				}else if(isStartingElement(next, FEATURE)){
					handleFeature(state, reader, next);
				}else if(isStartingElement(next, NOTES)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, REFERENCES)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, FIGURE)){
					handleNotYetImplementedElement(next);
				}else if(isStartingElement(next, FOOTNOTE)){
					handleNotYetImplementedElement(next);
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
		if (checkAndRemoveAttributeValue(attributes, CLASS, "cultivated")){
			name = CultivarPlantName.NewInstance(rank);
		}else{
			NomenclaturalCode nc = makeNomenclaturalCode(state);
			name = (NonViralName)nc.getNewTaxonNameInstance(rank);
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
	
	private Extension handleWriter(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
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

	private void handleNomenclature(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, Taxon taxon) throws XMLStreamException {
		checkNoAttributes(parentEvent);
		
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (isStartingElement(next, HOMOTYPES)){
				handleHomotypes(state, reader, next.asStartElement(), taxon);
			}else if(isMyEndingElement(next, parentEvent)){
				return;
			}else{
				fireSchemaConflictEventExpectedStartTag(HOMOTYPES, reader);
				state.setUnsuccessfull();
			}
		}
		return;
	}



	private void handleHomotypes(MarkupImportState state, XMLEventReader reader, StartElement parentEvent, Taxon taxon) throws XMLStreamException {
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
				if(isStartingElement(next, NOM)){
					homotypicalGroup = handleNom(state, reader, next, homotypicalGroup);
					hasNom = true;
				}else if(isStartingElement(next, NAME_TYPE)){
					state.setNameType(true);
					handleNotYetImplementedElement(next);
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
		
		//elements
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()){
				if (isMyEndingElement(next, parentEvent)){
					return;
				}else{
					if(isEndingElement(next, FULL_TYPE)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next,TYPE_STATUS)){
						//NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					}else if(isEndingElement(next, COLLECTION)){
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
					handleNotYetImplementedElement(next);
//					handleName(state, reader, next, name);
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
			}else{
				handleUnexpectedElement(next);
			}
		}
		//TODO handle missing end element
		throw new IllegalStateException("Nom has no closing tag");		// TODO Auto-generated method stub
		
	}


	private HomotypicalGroup handleNom(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, 
				HomotypicalGroup homotypicalGroup) throws XMLStreamException {
		HomotypicalGroup resultHomGroup = null;
		boolean isSynonym = false;
		boolean isNameType = state.isNameType();
		//attributes
		String classValue = getClassOnlyAttribute(parentEvent);
		if (! isNameType && ACCEPTED.equalsIgnoreCase(classValue)){
			isSynonym = false;
		}else if (! isNameType && SYNONYM.equalsIgnoreCase(classValue)){
			isSynonym = true;
		}else if (isNameType && NAME_TYPE.equalsIgnoreCase(classValue)){
			handleNotYetImplementedAttributeValue(parentEvent, CLASS, classValue);
		}else{
			//TODO Not yet implemented
			fireUnexpectedAttributeValue(parentEvent, CLASS, classValue);
		}
		NonViralName name = makeName(state.getCurrentTaxon(), homotypicalGroup, isSynonym);
		
		
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()){
				if (isMyEndingElement(next, parentEvent)){
//					logger.warn(name.getTitleCache());
					return resultHomGroup;
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
					handleName(state, reader, next, name);
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

	/**
	 * Returns the (empty) name with the correct homotypical group depending on the taxon status
	 * @param taxon
	 * @param homotypicalGroup
	 * @param isSynonym
	 * @return
	 */
	private NonViralName makeName(Taxon taxon,HomotypicalGroup homotypicalGroup, boolean isSynonym) {
		NonViralName name;
		if (isSynonym){
			Rank defaultRank = Rank.SPECIES();  //can be any
			name = BotanicalName.NewInstance(defaultRank, homotypicalGroup);
			SynonymRelationshipType synonymType = SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
			if (taxon.getHomotypicGroup().equals(homotypicalGroup)){
				synonymType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
			}
			taxon.addSynonymName(name, synonymType);
		}else{
			name = (NonViralName)taxon.getName();
		}
		return name;
	}
	
	private void handleName(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, NonViralName name) throws XMLStreamException {
		//attributes
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String classValue = getAndRemoveRequiredAttributeValue(parentEvent, attributes, CLASS);
		checkNoAttributes(attributes, parentEvent);
		
		//Ranks: family, subfamily, tribus, genus, subgenus, section, subsection, species, subspecies, variety, subvariety, forma
		//infrank, exaut, paraut, author, infrparaut, infraut, infrex, status, notes
		
		//ranks
		Rank rank = makeRank(state, classValue, false);
		
		String text = "";
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if (isStartingElement(next, ANNOTATION)){
				handleNotYetImplementedElement(next);
//				handleAnnotation(state, reader, next);
			}else if(isMyEndingElement(next, parentEvent)){
				doNameEnd(name, rank, text, classValue, next, state);
				return;
			}else if (next.isCharacters()){
				text += next.asCharacters().getData();
			}else if (next.isEndElement()){
				if(isEndingElement(next, ANNOTATION)){
					//NOT YET IMPLEMENTED
					popUnimplemented(next.asEndElement());
				}else{
					handleUnexpectedEndElement(next.asEndElement());
				}
			}else{
				fireSchemaConflictEventExpectedStartTag("annotation", reader);
				state.setUnsuccessfull();
			}
		}
		return;
		
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
	
	private void doNameEnd(NonViralName name, Rank rank, String text, String classValue, XMLEvent xmlEvent, MarkupImportState state) {
		Location location = xmlEvent.getLocation();
		if (rank != null){
			name.setRank(rank);
			if (rank.isSupraGeneric() || rank.isGenus()){
				name.setGenusOrUninomial(text);
			}else if (rank.isInfraGeneric()){
				name.setInfraGenericEpithet(text);
			}else if (rank.isSpecies()){
				name.setSpecificEpithet(text);
			}else if (rank.isInfraSpecific()){
				name.setInfraSpecificEpithet(text);
			}else{
				String message = "Invalid rank '%s'. Can't decide which epithet to fill with '%s'";
				message = String.format(message, rank.getTitleCache(), text);
				fireWarningEvent(message, makeLocationStr(location), 4);
			}
		}else{
			//infrank, infrparaut, infraut, infrex, notes
			//DONE: status,  exaut, paraut, author,
			
			//STATUS
			if (classValue.equalsIgnoreCase("status")){
				try {
					NomenclaturalStatusType nomStatusType = NomenclaturalStatusType.getNomenclaturalStatusTypeByAbbreviation(text);
					name.addStatus(NomenclaturalStatus.NewInstance(nomStatusType));
				} catch (UnknownCdmTypeException e) {
					String message = "Nom. status could not be recognized in '%s'";
					message = String.format(message, text);
					fireWarningEvent(message, makeLocationStr(location), 2);
				}
			//NOTES
			}else if(classValue.equalsIgnoreCase(NOTES)){
				handleNotYetImplementedAttributeValue(xmlEvent, CLASS, classValue);
			
			//Authors
			}else if(classValue.equalsIgnoreCase(AUTHOR)){
				//TODO parse
				TeamOrPersonBase author = Team.NewTitledInstance(text, text);
				name.setCombinationAuthorTeam(author);
			}else if(classValue.equalsIgnoreCase(EXAUT)){
				//TODO parse
				TeamOrPersonBase exAuthor = Team.NewTitledInstance(text, text);
				name.setExCombinationAuthorTeam(exAuthor);
			}else if(classValue.equalsIgnoreCase(PARAUT)){
				//TODO parse
				TeamOrPersonBase parAuthor = Team.NewTitledInstance(text, text);
				name.setBasionymAuthorTeam(parAuthor);
			}else if(classValue.equalsIgnoreCase("parexaut")){
				//TODO parse
				TeamOrPersonBase parExAuthor = Team.NewTitledInstance(text, text);
				name.setExBasionymAuthorTeam(parExAuthor);
			}else if(classValue.equalsIgnoreCase(INFRPARAUT)){
				//TODO infrparaut <-> paraut
				//TODO parse
				TeamOrPersonBase parAuthor = Team.NewTitledInstance(text, text);
				name.setBasionymAuthorTeam(parAuthor);
			}else if(classValue.equalsIgnoreCase(INFRAUT)){
				//TODO infraut <-> author
				//TODO parse
				TeamOrPersonBase author = Team.NewTitledInstance(text, text);
				name.setCombinationAuthorTeam(author);
			}else if(classValue.equalsIgnoreCase(INFREX)){
				//TODO infrex <-> exaut
				//TODO parse
				TeamOrPersonBase exAuthor = Team.NewTitledInstance(text, text);
				name.setExCombinationAuthorTeam(exAuthor);
			}else if(classValue.equalsIgnoreCase(INFRPAREX)){
				//TODO infrparex <-> parexaut
				//TODO parse
				TeamOrPersonBase exParAuthor = Team.NewTitledInstance(text, text);
				name.setExBasionymAuthorTeam(exParAuthor);
			}else if(classValue.equalsIgnoreCase(INFRANK)){
				if (classValue.equalsIgnoreCase(INFRANK)){
					rank = makeRank(state, text, true);
					if (rank == null){
						//TODO make event
						logger.warn("Rank not recognized: " + text);
					}
				}
				
			}else{
				String message = "Nom.class attribute has unhandled value '%s'";
				message = String.format(message, classValue);
				fireWarningEvent(message, makeLocationStr(location), 2);
			}
		}
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
		
		String type = getAndRemoveRefPart(refMap, PUBTYPE);
		String authorStr = getAndRemoveRefPart(refMap, AUTHOR);
		String titleStr = getAndRemoveRefPart(refMap, PUBTITLE);
		String titleCache = getAndRemoveRefPart(refMap, PUBFULLNAME);
		String volume = getAndRemoveRefPart(refMap, VOLUME);
		String edition = getAndRemoveRefPart(refMap, EDITION);
		String editors = getAndRemoveRefPart(refMap, EDITORS);
		String year = getAndRemoveRefPart(refMap, YEAR);
		String pubName = getAndRemoveRefPart(refMap, PUBNAME);
		
		
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


	private String getAndRemoveRefPart(Map<String, String> refMap, String key){
		String result = refMap.get(key);
		refMap.remove(key);
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
					}else if(isEndingElement(next, FIGURE)){
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
							fireWarningEvent(message, makeLocationStr(next.getLocation()), 1);
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
					TaxonDescription td = getTaxonDescription(taxon, state.getConfig().getSourceReference(), NO_IMAGE_GALLERY, CREATE_NEW);
					td.addElement(textData);
				}else if(isStartingElement(next, STRING)){
					String string = handleString(state, reader, next);
					TextData textData = TextData.NewInstance(feature);
					textData.putText(Language.DEFAULT(), string);
					TaxonDescription td = getTaxonDescription(taxon, state.getConfig().getSourceReference(), NO_IMAGE_GALLERY, CREATE_NEW);
					td.addElement(textData);
				}else if(isStartingElement(next, FIGURE)){
					handleNotYetImplementedElement(next);
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
		
		String text = "";
		while (reader.hasNext()){
			XMLEvent next = readNoWhitespace(reader);
			if(isMyEndingElement(next, parentEvent)){
				return text;
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
		throw new IllegalStateException("<String> has no closing tag");
		
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
			fireWarningEvent(message, makeLocationStr(parentEvent.getLocation()), 4);
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
