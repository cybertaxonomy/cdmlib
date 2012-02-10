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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeCacheStrategy;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.ext.geo.GeoServiceArea;
import eu.etaxonomy.cdm.ext.geo.IEditGeoService;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.markup.UnmatchedLeads.UnmatchedLeadsKey;
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
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.KeyStatement;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
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
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
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
public class MarkupDocumentImport extends MarkupImportBase implements ICdmIO<MarkupImportState> {
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
	private static final String BIBLIOGRAPHY = "bibliography";
	private static final String BIOGRAPHIES = "biographies";
	private static final String BOLD = "bold";
	private static final String BR = "br";
	private static final String CHAR = "char";
	private static final String CITATION = "citation";
	private static final String COLLECTION_AND_TYPE = "collectionAndType";
	private static final String COLLECTION_TYPE_STATUS = "collectionTypeStatus";
	private static final String COLLECTOR = "collector";
	private static final String COORDINATES = "coordinates";
	private static final String COUPLET = "couplet";
	private static final String DATES = "dates";
	private static final String DEDICATION = "dedication";
	private static final String DEFAULT_MEDIA_URL = "defaultMediaUrl";
	private static final String DESTROYED = "destroyed";
	private static final String DETAILS = "details";
	private static final String DISTRIBUTION_LIST = "distributionList";
	private static final String DISTRIBUTION_LOCALITY = "distributionLocality";
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
	private static final String FREQUENCY = "frequency";
	private static final String FULL_NAME = "fullName";
	private static final String FULL_TYPE = "fullType";
	private static final String GATHERING = "gathering";
	private static final String HEADING = "heading";
	private static final String HABITAT = "habitat";
	private static final String HABITAT_LIST = "habitatList";
	private static final String HOMONYM = "homonym";
	private static final String HOMOTYPES = "homotypes";
	private static final String ID = "id";
	private static final String INFRANK = "infrank";
	private static final String INFRAUT = "infraut";
	private static final String INFRPARAUT = "infrparaut";
	private static final String IS_SPOTCHARACTERS = "isSpotcharacters";
	private static final String ISSUE = "issue";
	private static final String ITALICS = "italics";
	private static final String KEY = "key";
	private static final String KEY_TITLE = "keyTitle";
	private static final String KEYNOTES = "keynotes";
	private static final String LIFE_CYCLE_PERIODS = "lifeCyclePeriods";
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
	private static final String PAGES = "pages";
	private static final String PARAUT = "paraut";
	private static final String PUBFULLNAME = "pubfullname";
	private static final String PUBLICATION = "publication";
	private static final String PUBNAME = "pubname";
	private static final String PUBTITLE = "pubtitle";
	private static final String PUBTYPE = "pubtype";
	private static final String QUESTION = "question";
	private static final String QUOTE = "quote";
	private static final String RANK = "rank";
	private static final String REF = "ref";
	private static final String REF_NUM = "refNum";
	private static final String REF_PART = "refPart";
	private static final String REFERENCE = "reference";
	private static final String REFERENCES = "references";
	private static final String TAXON = "taxon";
	private static final String TAXONTITLE = "taxontitle";
	private static final String TAXONTYPE = "taxontype";
	private static final String TEXT = "text";
	private static final String TEXT_SECTION = "textSection";
	private static final String TO_COUPLET = "toCouplet";
	private static final String TO_KEY = "toKey";
	private static final String TO_TAXON = "toTaxon";
	private static final String TYPE = "type";
	private static final String TYPE_STATUS = "typeStatus";
	private static final String TREATMENT = "treatment";
	private static final String SERIALS_ABBREVIATIONS = "serialsAbbreviations";
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

	// TODO make part of state, but state is renewed when invoking the import a
	// second time
	private UnmatchedLeads unmatchedLeads;

	@Autowired
	private IEditGeoService editGeoService;
	
	// TODO remove preliminary
	@Autowired
	private AuthenticationManager authenticationManager;
	private Authentication authentication;
	private PermissionEvaluator permissionEvaluator;

	public MarkupDocumentImport() {
		super();
		System.out.println("TODO remove preliminary authentication");
		// UsernamePasswordAuthenticationToken token = new
		// UsernamePasswordAuthenticationToken("admin", "0000");
		// authentication = authenticationManager.authenticate(token);
		// SecurityContext context = SecurityContextHolder.getContext();
		// context.setAuthentication(authentication);
		// permissionEvaluator = new CdmPermissionEvaluator();
	}

	@Override
	public boolean doCheck(MarkupImportState state) {
		state.setCheck(true);
		doInvoke(state);
		state.setCheck(false);
		return state.isSuccess();
	}

	@Override
	public void doInvoke(MarkupImportState state) { 
		fireProgressEvent("Start import markup document", "Before start of document");
		
		Queue<CdmBase> outputStream = new LinkedList<CdmBase>();

		TransactionStatus tx = startTransaction();
		// FIXME reset state
		doAllTheOldOtherStuff(state);

		// START
		try {
			// StAX
			XMLEventReader reader = getStaxReader(state);
			state.setReader(reader);
			// start document
			if (!validateStartOfDocument(reader)) {
				state.setUnsuccessfull();
				return;
			}

			// publication
			String elName = PUBLICATION;
			boolean hasPublication = false;
			
			while (reader.hasNext()) {
				XMLEvent nextEvent = reader.nextEvent();
				if (isStartingElement(nextEvent, elName)) {
					handlePublication(state, reader, nextEvent, elName);
					hasPublication = true;
				} else if (nextEvent.isEndDocument()) {
					if (!hasPublication) {
						String message = "No publication root element found";
						fireWarningEvent(message, nextEvent, 8);
					}
					// done
				} else {
					fireSchemaConflictEventExpectedStartTag(elName, reader);
				}
			}
			commitTransaction(tx);

			// //SAX
			// ImportHandlerBase handler= new PublicationHandler(this);
			// parseSAX(state, handler);

		} catch (FactoryConfigurationError e1) {
			fireWarningEvent("Some error occurred while setting up xml factory. Data can't be imported", "Start", 16);
			state.setUnsuccessfull();
		} catch (XMLStreamException e1) {
			fireWarningEvent("An XMLStreamException occurred while parsing. Data can't be imported", "Start", 16);
			state.setUnsuccessfull();
			// } catch (ParserConfigurationException e) {
			// fireWarningEvent("A ParserConfigurationException occurred while parsing. Data can't be imported",
			// "Start", 16);
			// } catch (SAXException e) {
			// fireWarningEvent("A SAXException occurred while parsing. Data can't be imported",
			// "Start", 16);
			// } catch (IOException e) {
			// fireWarningEvent("An IO exception occurred while parsing. Data can't be imported",
			// "Start", 16);

		}
		
		return;

	}

	private void handlePublication(MarkupImportState state, XMLEventReader reader, XMLEvent currentEvent, 
			String elName) throws XMLStreamException {

		// attributes
		StartElement element = currentEvent.asStartElement();
		Map<String, Attribute> attributes = getAttributes(element);
		String lang = getAndRemoveAttributeValue(attributes, "lang");
		if (lang != null){
			Language language = getTermService().getLanguageByIso(lang);
			state.setDefaultLanguage(language);
		}
		
		handleUnexpectedAttributes(element.getLocation(), attributes, "noNamespaceSchemaLocation");

		while (reader.hasNext()) {
			XMLEvent event = readNoWhitespace(reader);
			// TODO cardinality of alternative
			if (event.isEndElement()) {
				if (isEndingElement(event, elName)) {
					return;
				} else {
					if (isEndingElement(event, BIOGRAPHIES)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(event.asEndElement());
					} else if (isEndingElement(event, REFERENCES)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(event.asEndElement());
					} else if (isEndingElement(event, TEXT_SECTION)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(event.asEndElement());
					} else if (isEndingElement(event, ADDENDA)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(event.asEndElement());
					} else {
						handleUnexpectedElement(event);
					}
				}
			} else if (event.isStartElement()) {
				if (isStartingElement(event, META_DATA)) {
					handleMetaData(state, reader, event);
				} else if (isStartingElement(event, TREATMENT)) {
					handleTreatment(state, reader, event);
				} else if (isStartingElement(event, BIOGRAPHIES)) {
					handleNotYetImplementedElement(event);
				} else if (isStartingElement(event, REFERENCES)) {
					handleNotYetImplementedElement(event);
				} else if (isStartingElement(event, TEXT_SECTION)) {
					handleNotYetImplementedElement(event);
				} else if (isStartingElement(event, ADDENDA)) {
					handleNotYetImplementedElement(event);
				} else {
					handleUnexpectedStartElement(event);
				}
			} else {
				handleUnexpectedElement(event);
			}
		}
		throw new IllegalStateException("Publication has no ending element");
	}

	private void handleMetaData(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		checkNoAttributes(parentEvent);

		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				return;
			} else if (isStartingElement(next, DEFAULT_MEDIA_URL)) {
				String baseUrl = getCData(state, reader, next);
				try {
					new URL(baseUrl);
					state.setBaseMediaUrl(baseUrl);
				} catch (MalformedURLException e) {
					String message = "defaultMediaUrl '%s' is not a valid URL";
					message = String.format(message, baseUrl);
					fireWarningEvent(message, next, 8);
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("MetaData has no ending element");

	}

	private void handleTreatment(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		checkNoAttributes(parentEvent);
		Taxon lastTaxon = null;
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isStartingElement(next, TAXON)) {
				Taxon thisTaxon = handleTaxon(state, reader, next.asStartElement());
				doTaxonRelation(state, thisTaxon, lastTaxon, parentEvent.getLocation());
				lastTaxon = thisTaxon;
				// TODO for imports spanning multiple documents ?? Still needed?
				state.getConfig().setLastTaxonUuid(lastTaxon.getUuid());
			} else if (isMyEndingElement(next, parentEvent)) {
				Set<PolytomousKeyNode> keyNodesToSave = state.getPolytomousKeyNodesToSave();
				//better save the key then the nodes
				Set<PolytomousKey> keySet = new HashSet<PolytomousKey>();
				for (PolytomousKeyNode node : keyNodesToSave){
					PolytomousKey key = node.getKey();
					keySet.add(key);
				}
				save(keySet, state);
				//unmatched key leads
				UnmatchedLeads unmatched = state.getUnmatchedLeads();
				if (unmatched.size() > 0){
					String message = "The following key leads are unmatched: %s";
					message = String.format(message, state.getUnmatchedLeads().toString());
					fireWarningEvent(message, next, 6);
				}
//				save(keyNodesToSave, state);

				return;
			} else {
				handleUnexpectedElement(next);
			}
		}
		return;
	}

	/**
	 * @param taxon
	 * @param lastTaxon
	 */
	private void doTaxonRelation(MarkupImportState state, Taxon taxon,
			Taxon lastTaxon, Location dataLocation) {

		Classification tree = makeTree(state, dataLocation);
		if (lastTaxon == null) {
			tree.addChildTaxon(taxon, null, null, null);
			return;
		}
		Rank thisRank = taxon.getName().getRank();
		Rank lastRank = lastTaxon.getName().getRank();
		if (lastTaxon.getTaxonNodes().size() > 0) {
			TaxonNode lastNode = lastTaxon.getTaxonNodes().iterator().next();
			if (thisRank == null){
				String message = "rank is undefined for taxon '%s'. Can't create classification without rank.";
				message = String.format(message, taxon.getName().getTitleCache());
				fireWarningEvent(message, makeLocationStr(dataLocation), 6);
			}else if (thisRank.isLower(lastRank)) {
				lastNode.addChildTaxon(taxon, null, null, null);
				fillMissingEpithetsForTaxa(lastTaxon, taxon);
			} else if (thisRank.equals(lastRank)) {
				TaxonNode parent = lastNode.getParent();
				if (parent != null) {
					parent.addChildTaxon(taxon, null, null, null);
					fillMissingEpithetsForTaxa(parent.getTaxon(), taxon);
				} else {
					tree.addChildTaxon(taxon, null, null, null);
				}
			} else if (thisRank.isHigher(lastRank)) {
				doTaxonRelation(state, taxon, lastNode.getParent().getTaxon(),	dataLocation);
				// TaxonNode parentNode = handleTaxonRelation(state, taxon,
				// lastNode.getParent().getTaxon());
				// parentNode.addChildTaxon(taxon, null, null, null);
			}
		} else {

			String message = "Last taxon has no node";
			fireWarningEvent(message, makeLocationStr(dataLocation), 6);
		}
	}

	/**
	 * @param state
	 * @param dataLocation 
	 * @return
	 */
	private Classification makeTree(MarkupImportState state, Location dataLocation) {
		Classification result = state.getTree(null);
		if (result == null) {
			UUID uuid = state.getConfig().getClassificationUuid();
			if (uuid == null) {
				String message = "No classification uuid is defined";
				fireWarningEvent(message, makeLocationStr(dataLocation), 6);
				result = createNewClassification(state);
			} else {
				result = getClassificationService().find(uuid);
				if (result == null) {
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
		// TODO progress monitoring
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		Taxon taxon = createTaxonAndName(state, attributes);
		state.setCurrentTaxon(taxon);

		boolean hasTitle = false;
		boolean hasNomenclature = false;
		String taxonTitle = null;

		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()) {
				if (isMyEndingElement(next, parentEvent)) {
					checkMandatoryElement(hasTitle, parentEvent, TAXONTITLE);
					checkMandatoryElement(hasNomenclature, parentEvent,	NOMENCLATURE);
					handleUnexpectedAttributes(parentEvent.getLocation(),attributes);
					
					makeKeyNodes(state, parentEvent, taxonTitle);
					state.setCurrentTaxon(null);
					state.setCurrentTaxonNum(null);
					save(taxon, state);
					return taxon;
				} else {
					if (isEndingElement(next, HEADING)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, TEXT_SECTION)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, REFERENCES)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, FIGURE_REF)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else {
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, HEADING)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, TAXONTITLE)) {
					taxonTitle = handleTaxonTitle(state, reader, next);
					hasTitle = true;
				} else if (isStartingElement(next, WRITER)) {
					makeKeyWriter(state, reader, taxon, taxonTitle, next);
				} else if (isStartingElement(next, TEXT_SECTION)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, KEY)) {
					handleKey(state, reader, next);
				} else if (isStartingElement(next, NOMENCLATURE)) {
					handleNomenclature(state, reader, next);
					hasNomenclature = true;
				} else if (isStartingElement(next, FEATURE)) {
					handleFeature(state, reader, next);
				} else if (isStartingElement(next, NOTES)) {
					// TODO is this the correct way to handle notes?
					String note = handleNotes(state, reader, next);

					UUID notesUuid;
					try {
						notesUuid = state.getTransformer().getFeatureUuid(
								"notes");
						Feature feature = getFeature(state, notesUuid, "Notes",
								"Notes", "note", null);
						TextData textData = TextData.NewInstance(feature);
						textData.putText(Language.DEFAULT(), note);
						TaxonDescription description = getTaxonDescription(
								taxon, false, true);
						description.addElement(textData);
					} catch (UndefinedTransformerMethodException e) {
						String message = "getFeatureUuid method not yet implemented";
						fireWarningEvent(message, next, 8);
					}
				} else if (isStartingElement(next, REFERENCES)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, FIGURE_REF)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, FIGURE)) {
					handleFigure(state, reader, next);
				} else if (isStartingElement(next, FOOTNOTE)) {
					FootnoteDataHolder footnote = handleFootnote(state, reader,
							next);
					if (footnote.isRef()) {
						String message = "Ref footnote not implemented here";
						fireWarningEvent(message, next, 4);
					} else {
						registerGivenFootnote(state, footnote);
					}
				} else {
					handleUnexpectedStartElement(next);
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<Taxon> has no closing tag");
	}

	private void makeKeyNodes(MarkupImportState state, XMLEvent event, String taxonTitle) {
		Taxon taxon = state.getCurrentTaxon();
		String num = state.getCurrentTaxonNum();
		
		String nameString = CdmBase.deproxy(taxon.getName(), NonViralName.class).getNameCache();
//		String nameString = taxonTitle;
		
		//try to find matching lead nodes 
		UnmatchedLeadsKey leadsKey = UnmatchedLeadsKey.NewInstance(num, nameString);
		Set<PolytomousKeyNode> matchingNodes = handleMatchingNodes(state, taxon, leadsKey);
		
		if (num != null){//same without using the num
			UnmatchedLeadsKey noNumLeadsKey = UnmatchedLeadsKey.NewInstance("", nameString);
			Set<PolytomousKeyNode> noNumMatchingNodes = handleMatchingNodes(state, taxon, noNumLeadsKey);
			if(noNumMatchingNodes.size() > 0){
				String message ="Taxon matches additional key node when not considering <num> attribute in taxontitle. This may be correct but may also indicate an error.";
				fireWarningEvent(message, event, 1);
			}
		}
		//report missing match, if num exists
		if (matchingNodes.isEmpty() && num != null){
			String message = "Taxon has <num> attribute in taxontitle but no matching key nodes exist: %s, Key: %s";
			message = String.format(message, num, leadsKey.toString());
			fireWarningEvent(message, event, 1);
		}
		
	}
	
	private Set<PolytomousKeyNode> handleMatchingNodes(MarkupImportState state, Taxon taxon, UnmatchedLeadsKey leadsKey) {
		Set<PolytomousKeyNode> matchingNodes = state.getUnmatchedLeads().getNodes(leadsKey);
		for (PolytomousKeyNode matchingNode : matchingNodes){
			state.getUnmatchedLeads().removeNode(leadsKey, matchingNode);
			matchingNode.setTaxon(taxon);
			state.getPolytomousKeyNodesToSave().add(matchingNode);
		}
		return matchingNodes;
	}

	private void handleKey(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		// attributes
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String isSpotcharacters = getAndRemoveAttributeValue(attributes, IS_SPOTCHARACTERS);
		if (isNotBlank(isSpotcharacters) ) {
			//TODO isSpotcharacters
			String message = "Attribute isSpotcharacters not yet implemented for <key>";
			fireWarningEvent(message, parentEvent, 4);
		}
		
		PolytomousKey key = PolytomousKey.NewInstance();
		key.addTaxonomicScope(state.getCurrentTaxon());
		state.setCurrentKey(key);
		
		boolean isFirstCouplet = true;
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				save(key, state);
				state.setCurrentKey(null);
				return;
			} else if (isEndingElement(next, KEYNOTES)){
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, KEY_TITLE)) {
					handleKeyTitle(state, reader, next);
			} else if (isStartingElement(next, KEYNOTES)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, COUPLET)) {
				PolytomousKeyNode node = null;
				if (isFirstCouplet){
					node = key.getRoot();
					isFirstCouplet = false;
				}
				handleCouplet(state, reader, next, node);
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<key> has no closing tag");
	}

	/**
	 * @param state
	 * @param reader
	 * @param key
	 * @param next
	 * @throws XMLStreamException
	 */
	private void handleKeyTitle(MarkupImportState state, XMLEventReader reader, XMLEvent next) throws XMLStreamException {
		PolytomousKey key = state.getCurrentKey();
		String keyTitle = getCData(state, reader, next);
		String standardTitles = "(?i)(Key\\sto\\sthe\\s(genera|species|varieties|forms))";
		
		if (isNotBlank(keyTitle) ){
			if (!state.getConfig().isReplaceStandardKeyTitles() || ! keyTitle.matches(standardTitles)){
				key.setTitleCache(keyTitle, true);
			}
		}
	}

	private void handleCouplet(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, PolytomousKeyNode parentNode) throws XMLStreamException {
		String num = getOnlyAttribute(parentEvent, NUM, true);
		List<PolytomousKeyNode> childList = new ArrayList<PolytomousKeyNode>(); 
		
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				completeCouplet(state, parentEvent, parentNode, num, childList);
				return;
			} else if (isStartingElement(next, QUESTION)) {
				handleQuestion(state, reader, next, childList);
			} else if (isStartingElement(next, KEYNOTES)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, KEYNOTES)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<couplet> has no closing tag");
	}

	/**
	 * @param state
	 * @param parentEvent
	 * @param parentNode
	 * @param num
	 * @param childList
	 */
	private void completeCouplet(MarkupImportState state, XMLEvent parentEvent,
			PolytomousKeyNode parentNode, String num,
			List<PolytomousKeyNode> childList) {
		if (parentNode != null){
			for (PolytomousKeyNode childNode : childList){
				parentNode.addChild(childNode);
			}
		}else if (isNotBlank(num)){
			UnmatchedLeadsKey unmatchedKey = UnmatchedLeadsKey.NewInstance(state.getCurrentKey(), num);
			Set<PolytomousKeyNode> nodes = state.getUnmatchedLeads().getNodes(unmatchedKey);
			for(PolytomousKeyNode nodeToMatch: nodes){
				for (PolytomousKeyNode childNode : childList){
					nodeToMatch.addChild(childNode);
				}
				state.getUnmatchedLeads().removeNode(unmatchedKey, nodeToMatch);
			}
		}else{
			String message = "Parent num could not be matched. Please check if num (%s) is correct";
			message = String.format(message, num);
			fireWarningEvent(message, parentEvent, 6);
		}
	}

	private void handleQuestion(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, List<PolytomousKeyNode> nodesList) throws XMLStreamException {
		// attributes
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		//needed only for data lineage
		String questionNum = getAndRemoveRequiredAttributeValue(parentEvent, attributes, NUM);
		
		PolytomousKeyNode myNode = PolytomousKeyNode.NewInstance();
		myNode.setKey(state.getCurrentKey());  //to avoid NPE while computing num in PolytomousKeyNode in case this node is not matched correctly with a parent
		nodesList.add(myNode);
		
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				return;
			} else if (isStartingElement(next, TEXT)) {
				String text = getCData(state, reader, next);
				KeyStatement statement = KeyStatement.NewInstance(text);
				myNode.setStatement(statement);
			} else if (isStartingElement(next, COUPLET)) {
				//TODO test
				handleCouplet(state, reader, next, myNode);
			} else if (isStartingElement(next, TO_COUPLET)) {
				handleToCouplet(state, reader, next, myNode);
			} else if (isStartingElement(next, TO_TAXON)) {
				handleToTaxon(state, reader, next, myNode);
			
			} else if (isStartingElement(next, TO_KEY)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, TO_KEY)){
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, KEYNOTES)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, KEYNOTES)){
				//TODO
				popUnimplemented(next.asEndElement());
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<question> has no closing tag");
	}

	private void handleToCouplet(MarkupImportState state, XMLEventReader reader, XMLEvent next, PolytomousKeyNode node) throws XMLStreamException {
		String num = getOnlyAttribute(next, NUM, true);
		String cData = getCData(state, reader, next, false);
		if (isNotBlank(cData) && ! cData.equals(num)){
			String message = "CData ('%s') not handled in <toCouplet>";
			message = String.format(message, cData);
			fireWarningEvent(message, next, 4);
		}
		UnmatchedLeadsKey unmatched = UnmatchedLeadsKey.NewInstance(state.getCurrentKey(), num);
		state.getUnmatchedLeads().addKey(unmatched, node);
	}

	private void handleToTaxon(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, PolytomousKeyNode node) throws XMLStreamException {
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String num = getAndRemoveAttributeValue(attributes, NUM);
		String taxonStr = getCData(state, reader, parentEvent, false);
		//TODO ?
		taxonStr = makeTaxonKey(taxonStr, state.getCurrentTaxon());
		UnmatchedLeadsKey unmatched = UnmatchedLeadsKey.NewInstance(num, taxonStr);
		state.getUnmatchedLeads().addKey(unmatched, node);
		return;
	}
	
	private String makeTaxonKey(String strGoto, Taxon taxon) {
		String result = "";
		if (strGoto == null){
			return "";
		}
		
		NonViralName<?> name = CdmBase.deproxy(taxon.getName(), NonViralName.class);
		String strGenusName = name.getGenusOrUninomial();
		
		
		strGoto = strGoto.replaceAll("\\([^\\(\\)]*\\)", "");  //replace all brackets
		strGoto = strGoto.replaceAll("\\s+", " "); //replace multiple whitespaces by exactly one whitespace
		
		strGoto = strGoto.trim();  
		String[] split = strGoto.split("\\s");
		for (int i = 0; i<split.length; i++){
			String single = split[i];
			if (isGenusAbbrev(single, strGenusName)){
				split[i] = strGenusName;
			}
			if (isInfraSpecificMarker(single)){
				String strSpeciesEpi = name.getSpecificEpithet();
				if (isBlank(result)){
					result += strGenusName + " " + strSpeciesEpi;
				}
			}
			result = (result + " " + split[i]).trim();
		}
		return result;
	}
	

	private boolean isInfraSpecificMarker(String single) {
		try {
			if (Rank.getRankByAbbreviation(single).isInfraSpecific()){
				return true;
			}else{
				return false;
			}
		} catch (UnknownCdmTypeException e) {
			return false;
		}
	}
	
	private boolean isGenusAbbrev(String single, String strGenusName) {
		if (! single.matches("[A-Z]\\.?")) {
			return false;
		}else if (single.length() == 0 || strGenusName == null || strGenusName.length() == 0){
			return false; 
		}else{
			return single.charAt(0) == strGenusName.charAt(0);
		}
	}

	/**
	 * @param state
	 * @param reader
	 * @param taxon
	 * @param taxonTitle
	 * @param next
	 * @throws XMLStreamException
	 */
	private void makeKeyWriter(MarkupImportState state, XMLEventReader reader, Taxon taxon, String taxonTitle, XMLEvent next) throws XMLStreamException {
		WriterDataHolder writer = handleWriter(state, reader, next);
		taxon.addExtension(writer.extension);
		// TODO what if taxonTitle comes later
		if (StringUtils.isNotBlank(taxonTitle)
				&& writer.extension != null) {
			Reference<?> sec = ReferenceFactory.newBookSection();
			sec.setTitle(taxonTitle);
			TeamOrPersonBase<?> author = createAuthor(writer.writer);
			sec.setAuthorTeam(author);
			sec.setInReference(state.getConfig()
					.getSourceReference());
			taxon.setSec(sec);
			registerFootnotes(state, sec, writer.footnotes);
		} else {
			String message = "No taxontitle exists for writer";
			fireWarningEvent(message, next, 6);
		}
	}

	private String handleNotes(MarkupImportState state, XMLEventReader reader,
			XMLEvent parentEvent) throws XMLStreamException {
		checkNoAttributes(parentEvent);

		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				return text;
			} else if (next.isEndElement()) {
				if (isEndingElement(next, HEADING)) {
					popUnimplemented(next.asEndElement());
				} else if (isEndingElement(next, WRITER)) {
					popUnimplemented(next.asEndElement());
				} else if (isEndingElement(next, NUM)) {
					popUnimplemented(next.asEndElement());
				} else {
					handleUnexpectedEndElement(next.asEndElement());
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, HEADING)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, SUB_HEADING)) {
					String subheading = getCData(state, reader, next).trim();
					if (! isNoteHeading(subheading)) {
						fireNotYetImplementedElement(next.getLocation(), next.asStartElement().getName(), 0);
					}
				} else if (isStartingElement(next, WRITER)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, NUM)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, STRING)) {
					// TODO why multiple strings in schema?
					text = makeNotesString(state, reader, text, next);
				} else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<Notes> has no closing tag");
	}

	/**
	 * @param state
	 * @param reader
	 * @param text
	 * @param next
	 * @return
	 * @throws XMLStreamException
	 */
	private String makeNotesString(MarkupImportState state,	XMLEventReader reader, String text, XMLEvent next) throws XMLStreamException {
		Map<String, String> stringMap = handleString(state, reader,	next, null);
		if (stringMap.size() == 0){
			String message = "No text available in <notes>";
			fireWarningEvent(message, next, 4);
		}else if (stringMap.size() > 1){
			String message = "Subheadings not yet supported in <notes>";
			fireWarningEvent(message, next, 4);
		}else{
			String firstSubheading = stringMap.keySet().iterator().next();
			if ( firstSubheading != null && ! isNoteHeading (firstSubheading) )  {
				String message = "Subheadings not yet supported in <notes>";
				fireWarningEvent(message, next, 4);
			}
		}
		for (String subheading : stringMap.keySet()){
			text += subheading;
			text += stringMap.get(subheading);
		}
		return text;
	}

	private boolean isNoteHeading(String heading) {
		String excludePattern = "(i?)(Notes?):?";
		return heading.matches(excludePattern);
	}

	/**
	 * @param state
	 * @param attributes
	 */
	private Taxon createTaxonAndName(MarkupImportState state,
			Map<String, Attribute> attributes) {
		NonViralName<?> name;
		Rank rank = Rank.SPECIES(); // default
		boolean isCultivar = checkAndRemoveAttributeValue(attributes, CLASS,
				"cultivated");
		if (isCultivar) {
			name = CultivarPlantName.NewInstance(rank);
		} else {
			name = createNameByCode(state, rank);
		}
		Taxon taxon = Taxon.NewInstance(name, state.getConfig()
				.getSourceReference());
		if (checkAndRemoveAttributeValue(attributes, CLASS, "dubious")) {
			taxon.setDoubtful(true);
		} else if (checkAndRemoveAttributeValue(attributes, CLASS, "excluded")) {
			taxon.setExcluded(true);
		}
		// TODO insufficient, new, expected
		handleNotYetImplementedAttribute(attributes, CLASS);
		// From old version
		// MarkerType markerType = getMarkerType(state, attrValue);
		// if (markerType == null){
		// logger.warn("Class attribute value for taxon not yet supported: " +
		// attrValue);
		// }else{
		// taxon.addMarker(Marker.NewInstance(markerType, true));
		// }

		// save(name, state);
		// save(taxon, state);
		return taxon;
	}

	/**
	 * @param state
	 * @param rank
	 * @return
	 */
	private NonViralName<?> createNameByCode(MarkupImportState state, Rank rank) {
		NonViralName<?> name;
		NomenclaturalCode nc = makeNomenclaturalCode(state);
		name = (NonViralName<?>) nc.getNewTaxonNameInstance(rank);
		return name;
	}

	/**
	 * @param state
	 * @return
	 */
	private NomenclaturalCode makeNomenclaturalCode(MarkupImportState state) {
		NomenclaturalCode nc = state.getConfig().getNomenclaturalCode();
		if (nc == null) {
			nc = NomenclaturalCode.ICBN; // default;
		}
		return nc;
	}

	private String handleTaxonTitle(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		//attributes
		String text = "";
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String rankAttr = getAndRemoveAttributeValue(attributes, RANK);
		Rank rank = makeRank(state, rankAttr, false);
		String num = getAndRemoveAttributeValue(attributes, NUM);
		state.setCurrentTaxonNum(num);
		checkNoAttributes(attributes, parentEvent);

		// TODO handle attributes
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()) {
				if (isMyEndingElement(next, parentEvent)) {
					Taxon taxon = state.getCurrentTaxon();
					String titleText = null;
					if (checkMandatoryText(text, parentEvent)) {
						titleText = normalize(text);
						UUID uuidTitle = MarkupTransformer.uuidTaxonTitle;
						ExtensionType titleExtension = this.getExtensionType(state, uuidTitle, "Taxon Title ","taxon title", "title");
						taxon.addExtension(titleText, titleExtension);
					}
					taxon.getName().setRank(rank);
					// TODO check title exists
					return titleText;
				} else {
					if (isEndingElement(next, FOOTNOTE)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else {
						handleUnexpectedEndElement(next.asEndElement());
						state.setUnsuccessfull();
					}
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, FOOTNOTE)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next);
					state.setUnsuccessfull();
				}
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();

			} else {
				handleUnexpectedElement(next);
				state.setUnsuccessfull();
			}
		}
		return null;

	}

	private WriterDataHolder handleWriter(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		String text = "";
		checkNoAttributes(parentEvent);
		WriterDataHolder dataHolder = new WriterDataHolder();
		List<FootnoteDataHolder> footnotes = new ArrayList<FootnoteDataHolder>();

		// TODO handle attributes
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				text = CdmUtils.removeBrackets(text);
				if (checkMandatoryText(text, parentEvent)) {
					text = normalize(text);
					dataHolder.writer = text;
					dataHolder.footnotes = footnotes;

					// Extension
					UUID uuidWriterExtension = MarkupTransformer.uuidWriterExtension;
					ExtensionType writerExtensionType = this
							.getExtensionType(state, uuidWriterExtension,
									"Writer", "writer", "writer");
					Extension extension = Extension.NewInstance();
					extension.setType(writerExtensionType);
					extension.setValue(text);
					dataHolder.extension = extension;

					// Annotation
					UUID uuidWriterAnnotation = MarkupTransformer.uuidWriterAnnotation;
					AnnotationType writerAnnotationType = this.getAnnotationType(state, uuidWriterAnnotation, "Writer", "writer", "writer", null);
					Annotation annotation = Annotation.NewInstance(text, writerAnnotationType, Language.DEFAULT());
					dataHolder.annotation = annotation;

					return dataHolder;
				} else {
					return null;
				}
			} else if (isStartingElement(next, FOOTNOTE_REF)) {
				FootnoteDataHolder footNote = handleFootnoteRef(state, reader, next);
				if (footNote.isRef()) {
					footnotes.add(footNote);
				} else {
					logger.warn("Non ref footnotes not yet impelemnted");
				}
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();

			} else {
				handleUnexpectedElement(next);
				state.setUnsuccessfull();
			}
		}
		throw new IllegalStateException("<writer> has no end tag");
	}

	private void registerFootnotes(MarkupImportState state, AnnotatableEntity entity, List<FootnoteDataHolder> footnotes) {
		for (FootnoteDataHolder footNote : footnotes) {
			registerFootnoteDemand(state, entity, footNote);
		}
	}

	private void registerGivenFootnote(MarkupImportState state, FootnoteDataHolder footnote) {
		state.registerFootnote(footnote);
		Set<AnnotatableEntity> demands = state.getFootnoteDemands(footnote.id);
		if (demands != null) {
			for (AnnotatableEntity entity : demands) {
				attachFootnote(state, entity, footnote);
			}
		}
	}

	private void registerGivenFigure(MarkupImportState state, String id, Media figure) {
		state.registerFigure(id, figure);
		Set<AnnotatableEntity> demands = state.getFigureDemands(id);
		if (demands != null) {
			for (AnnotatableEntity entity : demands) {
				attachFigure(state, entity, figure);
			}
		}
	}

	private void registerFootnoteDemand(MarkupImportState state, AnnotatableEntity entity, FootnoteDataHolder footnote) {
		FootnoteDataHolder existingFootnote = state.getFootnote(footnote.ref);
		if (existingFootnote != null) {
			attachFootnote(state, entity, existingFootnote);
		} else {
			Set<AnnotatableEntity> demands = state.getFootnoteDemands(footnote.ref);
			if (demands == null) {
				demands = new HashSet<AnnotatableEntity>();
				state.putFootnoteDemands(footnote.ref, demands);
			}
			demands.add(entity);
		}
	}

	private void registerFigureDemand(MarkupImportState state, AnnotatableEntity entity, String figureRef) {
		Media existingFigure = state.getFigure(figureRef);
		if (existingFigure != null) {
			attachFigure(state, entity, existingFigure);
		} else {
			Set<AnnotatableEntity> demands = state.getFigureDemands(figureRef);
			if (demands == null) {
				demands = new HashSet<AnnotatableEntity>();
				state.putFigureDemands(figureRef, demands);
			}
			demands.add(entity);
		}
	}

	private void attachFootnote(MarkupImportState state, AnnotatableEntity entity, FootnoteDataHolder footnote) {
		AnnotationType annotationType = this.getAnnotationType(state, MarkupTransformer.uuidFootnote, "Footnote", "An e-flora footnote", "fn", null);
		Annotation annotation = Annotation.NewInstance(footnote.string,
				annotationType, Language.DEFAULT());
		// TODO transient objects
		entity.addAnnotation(annotation);
		save(entity, state);
	}

	private void attachFigure(MarkupImportState state,
			AnnotatableEntity entity, Media figure) {
		// IdentifiableEntity<?> toSave;
		if (entity.isInstanceOf(TextData.class)) {
			TextData deb = CdmBase.deproxy(entity, TextData.class);
			deb.addMedia(figure);
			// toSave = ((TaxonDescription)deb.getInDescription()).getTaxon();
		} else if (entity.isInstanceOf(IdentifiableMediaEntity.class)) {
			IdentifiableMediaEntity<?> ime = CdmBase.deproxy(entity,
					IdentifiableMediaEntity.class);
			ime.addMedia(figure);
			// toSave = ime;
		} else {
			String message = "Unsupported entity to attach media: %s";
			message = String.format(message, entity.getClass().getName());
			// toSave = null;
		}
		save(entity, state);
	}

	private void handleFigure(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		// FigureDataHolder result = new FigureDataHolder();

		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String id = getAndRemoveAttributeValue(attributes, ID);
		String type = getAndRemoveAttributeValue(attributes, TYPE);
		checkNoAttributes(attributes, parentEvent);

		String urlString = null;
		String legendString = null;
		String titleString = null;
		String numString = null;
		String text = null;
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				makeFigure(state, id, type, urlString, legendString, titleString, numString, next);
				return;
			} else if (isStartingElement(next, FIGURE_LEGEND)) {
				// TODO same as figurestring ?
				legendString = handleFootnoteString(state, reader, next);
			} else if (isStartingElement(next, FIGURE_TITLE)) {
				titleString = getCData(state, reader, next);
			} else if (isStartingElement(next, URL)) {
				String localUrl = getCData(state, reader, next);
				urlString = CdmUtils.Nz(state.getBaseMediaUrl()) + localUrl;
			} else if (isStartingElement(next, NUM)) {
				numString = getCData(state, reader, next);
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				fireUnexpectedEvent(next, 0);
			}
		}
		throw new IllegalStateException("<figure> has no end tag");
	}

	/**
	 * @param state
	 * @param id
	 * @param type
	 * @param urlString
	 * @param legendString
	 * @param titleString
	 * @param numString
	 * @param next
	 */
	private void makeFigure(MarkupImportState state, String id, String type, String urlString, 
						String legendString, String titleString, String numString, XMLEvent next) {
		Media media = null;
		boolean isFigure = false;
		try {
			//TODO maybe everything is a figure as it is all taken from a book
			if ("lineart".equals(type)) {
				isFigure = true;
//				media = Figure.NewInstance(url.toURI(), null, null,	null);
			} else if (type == null || "photo".equals(type)
					|| "signature".equals(type)
					|| "others".equals(type)) {
			} else {
				String message = "Unknown figure type '%s'";
				message = String.format(message, type);
				fireWarningEvent(message, next, 2);
			}
			media = getImageMedia(urlString, READ_MEDIA_DATA, isFigure);
			
			if (media != null){
				// title
				if (StringUtils.isNotBlank(titleString)) {
					media.putTitle(Language.DEFAULT(), titleString);
				}
				// legend
				if (StringUtils.isNotBlank(legendString)) {
					media.addDescription(legendString, Language.DEFAULT());
				}
				if (StringUtils.isNotBlank(numString)) {
					// TODO use concrete source (e.g. DAPHNIPHYLLACEAE in FM
					// vol.13)
					Reference<?> citation = state.getConfig().getSourceReference();
					media.addSource(numString, "num", citation, null);
					// TODO name used in source if available
				}
				// TODO which citation
				if (StringUtils.isNotBlank(id)) {
					media.addSource(id, null, state.getConfig().getSourceReference(), null);
				} else {
					String message = "Figure id should never be empty or null";
					fireWarningEvent(message, next, 6);
				}
				
				// text
				// do nothing

			}
		} catch (MalformedURLException e) {
			String message = "Media uri has incorrect syntax: %s";
			message = String.format(message, urlString);
			fireWarningEvent(message, next, 4);
//		} catch (URISyntaxException e) {
//			String message = "Media uri has incorrect syntax: %s";
//			message = String.format(message, urlString);
//			fireWarningEvent(message, next, 4);
		}

		registerGivenFigure(state, id, media);
	}

	private FigureDataHolder handleFigureRef(MarkupImportState state,
			XMLEventReader reader, XMLEvent parentEvent)
			throws XMLStreamException {
		FigureDataHolder result = new FigureDataHolder();
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		result.ref = getAndRemoveAttributeValue(attributes, REF);
		checkNoAttributes(attributes, parentEvent);

		// text is not handled, needed only for debugging purposes
		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				return result;
			} else if (isStartingElement(next, NUM)) {
				String num = getCData(state, reader, next);
				result.num = num; // num is not handled during import
			} else if (isStartingElement(next, FIGURE_PART)) {
				result.figurePart = getCData(state, reader, next);
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				fireUnexpectedEvent(next, 0);
			}
		}
		throw new IllegalStateException("<figureRef> has no end tag");
	}

	private FootnoteDataHolder handleFootnote(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		FootnoteDataHolder result = new FootnoteDataHolder();
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		result.id = getAndRemoveAttributeValue(attributes, ID);
		// result.ref = getAndRemoveAttributeValue(attributes, REF);
		checkNoAttributes(attributes, parentEvent);

		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isStartingElement(next, FOOTNOTE_STRING)) {
				String string = handleFootnoteString(state, reader, next);
				result.string = string;
			} else if (isMyEndingElement(next, parentEvent)) {
				return result;
			} else {
				fireUnexpectedEvent(next, 0);
			}
		}
		return result;
	}

	private FootnoteDataHolder handleFootnoteRef(MarkupImportState state,
			XMLEventReader reader, XMLEvent parentEvent)
			throws XMLStreamException {
		FootnoteDataHolder result = new FootnoteDataHolder();
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		result.ref = getAndRemoveAttributeValue(attributes, REF);
		checkNoAttributes(attributes, parentEvent);

		// text is not handled, needed only for debugging purposes
		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			// if (isStartingElement(next, FOOTNOTE_STRING)){
			// String string = handleFootnoteString(state, reader, next);
			// result.string = string;
			// }else
			if (isMyEndingElement(next, parentEvent)) {
				return result;
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();

			} else {
				fireUnexpectedEvent(next, 0);
			}
		}
		return result;
	}

	private void handleNomenclature(MarkupImportState state,
			XMLEventReader reader, XMLEvent parentEvent)
			throws XMLStreamException {
		checkNoAttributes(parentEvent);

		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isStartingElement(next, HOMOTYPES)) {
				handleHomotypes(state, reader, next.asStartElement());
			} else if (isMyEndingElement(next, parentEvent)) {
				return;
			} else {
				fireSchemaConflictEventExpectedStartTag(HOMOTYPES, reader);
				state.setUnsuccessfull();
			}
		}
		return;
	}

	private String handleFootnoteString(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		boolean isTextMode = true;
		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				return text;
			} else if (next.isEndElement()) {
				if (isEndingElement(next, FULL_NAME)) {
					popUnimplemented(next.asEndElement());
				} else if (isEndingElement(next, BR)) {
					isTextMode = true;
				} else if (isHtml(next)) {
					text += getXmlTag(next);
				} else {
					handleUnexpectedEndElement(next.asEndElement());
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, FULL_NAME)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, GATHERING)) {
					text += handleInLineGathering(state, reader, next);
				} else if (isStartingElement(next, REFERENCES)) {
					text += " " + handleInLineReferences(state, reader, next)+ " ";
				} else if (isStartingElement(next, BR)) {
					text += "<br/>";
					isTextMode = false;
				} else if (isHtml(next)) {
					text += getXmlTag(next);
				} else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()) {
				if (!isTextMode) {
					String message = "footnoteString is not in text mode";
					fireWarningEvent(message, next, 6);
				} else {
					text += next.asCharacters().getData().trim(); 
					// getCData(state, reader, next); does not work as we have inner tags like <references>
				}
			} else {
				handleUnexpectedEndElement(next.asEndElement());
			}
		}
		throw new IllegalStateException("<footnoteString> has no closing tag");

	}

	private String handleInLineGathering(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(DerivedUnitType.DerivedUnit.FieldObservation);
		handleGathering(state, reader, parentEvent, null, facade);
		FieldObservation fieldObservation = facade.innerFieldObservation();
		String result = "<cdm:specimen uuid='%s'>%s</specimen>";
		result = String.format(result, fieldObservation.getUuid(), fieldObservation.getTitleCache());
		save(fieldObservation, state);
		return result;	
	}

	private String handleInLineReferences(MarkupImportState state,XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		checkNoAttributes(parentEvent);

		boolean hasReference = false;
		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				checkMandatoryElement(hasReference, parentEvent.asStartElement(), REFERENCE);
				return text;
			} else if (isStartingElement(next, REFERENCE)) {
				text += handleInLineReference(state, reader, next);
				hasReference = true;
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<References> has no closing tag");
	}

	private String handleInLineReference(MarkupImportState state,XMLEventReader reader, XMLEvent parentEvent)throws XMLStreamException {
		Reference<?> reference = handleReference(state, reader, parentEvent);
		String result = "<cdm:ref uuid='%s'>%s</ref>";
		result = String.format(result, reference.getUuid(), reference.getTitleCache());
		save(reference, state);
		return result;
	}

	private Reference<?> handleReference(MarkupImportState state,XMLEventReader reader, XMLEvent parentEvent)throws XMLStreamException {
		checkNoAttributes(parentEvent);

		boolean hasRefPart = false;
		Map<String, String> refMap = new HashMap<String, String>();
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				checkMandatoryElement(hasRefPart, parentEvent.asStartElement(),
						REF_PART);
				Reference<?> reference = createReference(state, refMap, next);
				return reference;
			} else if (isStartingElement(next, REF_PART)) {
				handleRefPart(state, reader, next, refMap);
				hasRefPart = true;
			} else {
				handleUnexpectedElement(next);
			}
		}
		// TODO handle missing end element
		throw new IllegalStateException("<Reference> has no closing tag");
	}

	private void handleHomotypes(MarkupImportState state,
			XMLEventReader reader, StartElement parentEvent)
			throws XMLStreamException {
		checkNoAttributes(parentEvent);

		HomotypicalGroup homotypicalGroup = null;

		boolean hasNom = false;
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()) {
				if (isMyEndingElement(next, parentEvent)) {
					checkMandatoryElement(hasNom, parentEvent, NOM);
					return;
				} else {
					if (isEndingElement(next, NAME_TYPE)) {
						state.setNameType(false);
					} else if (isEndingElement(next, NOTES)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else {
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, NOM)) {
					NonViralName<?> name = handleNom(state, reader, next,
							homotypicalGroup);
					homotypicalGroup = name.getHomotypicalGroup();
					hasNom = true;
				} else if (isStartingElement(next, NAME_TYPE)) {
					state.setNameType(true);
					handleNameType(state, reader, next, homotypicalGroup);
				} else if (isStartingElement(next, SPECIMEN_TYPE)) {
					handleSpecimenType(state, reader, next, homotypicalGroup);
				} else if (isStartingElement(next, NOTES)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next);
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		// TODO handle missing end element
		throw new IllegalStateException("Homotypes has no closing tag");

	}

	private void handleNameType(MarkupImportState state, XMLEventReader reader,
			XMLEvent parentEvent, HomotypicalGroup homotypicalGroup)
			throws XMLStreamException {
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
			status = null;
		}

		boolean hasNom = false;
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()) {
				if (isMyEndingElement(next, parentEvent)) {
					checkMandatoryElement(hasNom, parentEvent.asStartElement(),
							NOM);
					state.setNameType(false);
					return;
				} else {
					if (isEndingElement(next, ACCEPTED_NAME)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else {
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, NOM)) {
					// TODO should we check if the type is always a species, is
					// this a rule?
					NonViralName<?> speciesName = handleNom(state, reader,
							next, null);
					for (TaxonNameBase<?, ?> name : homotypicalGroup
							.getTypifiedNames()) {
						name.addNameTypeDesignation(speciesName, null, null,
								null, status, false, false, false, false);
					}
					hasNom = true;
				} else if (isStartingElement(next, ACCEPTED_NAME)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next);
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		// TODO handle missing end element
		throw new IllegalStateException("Homotypes has no closing tag");

	}

	private void handleSpecimenType(MarkupImportState state,
			XMLEventReader reader, XMLEvent parentEvent,
			HomotypicalGroup homotypicalGroup) throws XMLStreamException {
		// attributes
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String typeStatus = getAndRemoveAttributeValue(attributes, TYPE_STATUS);
		String notSeen = getAndRemoveAttributeValue(attributes, NOT_SEEN);
		String unknown = getAndRemoveAttributeValue(attributes, UNKNOWN);
		String notFound = getAndRemoveAttributeValue(attributes, NOT_FOUND);
		String destroyed = getAndRemoveAttributeValue(attributes, DESTROYED);
		String lost = getAndRemoveAttributeValue(attributes, LOST);
		checkNoAttributes(attributes, parentEvent);
		if (StringUtils.isNotEmpty(typeStatus)) {
			// TODO
			// currently not needed
		} else if (StringUtils.isNotEmpty(notSeen)) {
			handleNotYetImplementedAttribute(attributes, NOT_SEEN);
		} else if (StringUtils.isNotEmpty(unknown)) {
			handleNotYetImplementedAttribute(attributes, UNKNOWN);
		} else if (StringUtils.isNotEmpty(notFound)) {
			handleNotYetImplementedAttribute(attributes, NOT_FOUND);
		} else if (StringUtils.isNotEmpty(destroyed)) {
			handleNotYetImplementedAttribute(attributes, DESTROYED);
		} else if (StringUtils.isNotEmpty(lost)) {
			handleNotYetImplementedAttribute(attributes, LOST);
		}

		NonViralName<?> firstName = null;
		Set<TaxonNameBase> names = homotypicalGroup.getTypifiedNames();
		if (names.isEmpty()) {
			String message = "There is no name in a homotypical group. Can't create the specimen type";
			fireWarningEvent(message, parentEvent, 8);
		} else {
			firstName = CdmBase.deproxy(names.iterator().next(),
					NonViralName.class);
		}

		DerivedUnitFacade facade = DerivedUnitFacade
				.NewInstance(DerivedUnitType.Specimen);
		String text = "";
		// elements
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()) {
				if (isMyEndingElement(next, parentEvent)) {
					makeSpecimenType(state, facade, text, firstName,
							parentEvent);
					return;
				} else {
					if (isEndingElement(next, FULL_TYPE)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, TYPE_STATUS)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, ORIGINAL_DETERMINATION)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, SPECIMEN_TYPE)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, COLLECTION_AND_TYPE)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, CITATION)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, NOTES)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, ANNOTATION)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else {
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, FULL_TYPE)) {
					handleNotYetImplementedElement(next);
					// homotypicalGroup = handleNom(state, reader, next, taxon,
					// homotypicalGroup);
				} else if (isStartingElement(next, TYPE_STATUS)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, GATHERING)) {
					handleGathering(state, reader, next, homotypicalGroup, facade);
				} else if (isStartingElement(next, ORIGINAL_DETERMINATION)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, SPECIMEN_TYPE)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, COLLECTION_AND_TYPE)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, NOTES)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, ANNOTATION)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next);
				}
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedElement(next);
			}
		}
		// TODO handle missing end element
		throw new IllegalStateException("Specimen type has no closing tag"); 
	}

	private void makeSpecimenType(MarkupImportState state,
			DerivedUnitFacade facade, String text, NonViralName name,
			XMLEvent parentEvent) {
		text = text.trim();
		// remove brackets
		if (text.matches("^\\(.*\\)\\.?$")) {
			text = text.replaceAll("\\.", "");
			text = text.substring(1, text.length() - 1);
		}
		String[] split = text.split("[;,]");
		for (String str : split) {
			str = str.trim();
			boolean addToAllNamesInGroup = true;
			TypeInfo typeInfo = makeSpecimenTypeTypeInfo(str, parentEvent);
			SpecimenTypeDesignationStatus typeStatus = typeInfo.status;
			Collection collection = createCollection(typeInfo.collectionString);

			// TODO improve cache strategy handling
			DerivedUnitBase typeSpecimen = facade.addDuplicate(collection,
					null, null, null, null);
			typeSpecimen.setCacheStrategy(new DerivedUnitFacadeCacheStrategy());
			name.addSpecimenTypeDesignation((Specimen) typeSpecimen, typeStatus, null, null, null, false, addToAllNamesInGroup);
		}
	}

	private Collection createCollection(String code) {
		// TODO deduplicate
		// TODO code <-> name
		Collection result = Collection.NewInstance();
		result.setCode(code);
		return result;
	}

	private TypeInfo makeSpecimenTypeTypeInfo(String originalString, XMLEvent event) {
		TypeInfo result = new TypeInfo();
		String[] split = originalString.split("\\s+");
		for (String str : split) {
			if (str.matches(SpecimenTypeParser.typeTypePattern)) {
				SpecimenTypeDesignationStatus status;
				try {
					status = SpecimenTypeParser.parseSpecimenTypeStatus(str);
				} catch (UnknownCdmTypeException e) {
					String message = "Specimen type status '%s' not recognized by parser";
					message = String.format(message, str);
					fireWarningEvent(message, event, 4);
					status = null;
				}
				result.status = status;
			} else if (str.matches(SpecimenTypeParser.collectionPattern)) {
				result.collectionString = str;
			} else {
				String message = "Type part '%s' could not be recognized";
				message = String.format(message, str);
				fireWarningEvent(message, event, 2);
			}
		}

		return result;
	}

	
	private void handleGathering(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, HomotypicalGroup homotypicalGroup, DerivedUnitFacade facade) throws XMLStreamException {
		checkNoAttributes(parentEvent);
		boolean hasCollector = false;
		boolean hasFieldNum = false;

		// elements
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()) {
				if (isMyEndingElement(next, parentEvent)) {
					checkMandatoryElement(hasCollector,parentEvent.asStartElement(), COLLECTOR);
					checkMandatoryElement(hasFieldNum,parentEvent.asStartElement(), FIELD_NUM);
					return;
				} else {
					if (isEndingElement(next, ALTERNATIVE_COLLECTOR)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, ALTERNATIVE_FIELD_NUM)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, COLLECTION_TYPE_STATUS)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next,
							ALTERNATIVE_COLLECTION_TYPE_STATUS)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, SUB_COLLECTION)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, DATES)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, NOTES)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else {
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, COLLECTOR)) {
					hasCollector = true;
					String collectorStr = getCData(state, reader, next);
					AgentBase<?> collector = createCollector(collectorStr);
					facade.setCollector(collector);
				} else if (isStartingElement(next, ALTERNATIVE_COLLECTOR)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, FIELD_NUM)) {
					hasFieldNum = true;
					String fieldNumStr = getCData(state, reader, next);
					facade.setFieldNumber(fieldNumStr);
				} else if (isStartingElement(next, ALTERNATIVE_FIELD_NUM)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, COLLECTION_TYPE_STATUS)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next,
						ALTERNATIVE_COLLECTION_TYPE_STATUS)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, SUB_COLLECTION)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, LOCALITY)) {
					handleLocality(state, reader, next, facade);
				} else if (isStartingElement(next, DATES)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, NOTES)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next);
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		// TODO handle missing end element
		throw new IllegalStateException("Collection has no closing tag");

	}

	private void handleLocality(MarkupImportState state, XMLEventReader reader,XMLEvent parentEvent, DerivedUnitFacade facade)throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);
		boolean isLocality = false;
		NamedAreaLevel areaLevel = null;
		if ("locality".equalsIgnoreCase(classValue)) {
			isLocality = true;
		} else {
			areaLevel = makeNamedAreaLevel(state, classValue, parentEvent);
		}

		String text = "";
		// elements
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()) {
				if (isMyEndingElement(next, parentEvent)) {
					if (StringUtils.isNotBlank(text)) {
						text = normalize(text);
						if (isLocality) {
							facade.setLocality(text);
						} else {
							text = CdmUtils.removeTrailingDot(text);
							NamedArea area = makeArea(state, text, areaLevel);
							facade.addCollectingArea(area);
						}
					}
					// TODO
					return;
				} else {
					if (isEndingElement(next, ALTITUDE)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, COORDINATES)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, ANNOTATION)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else {
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, ALTITUDE)) {
					handleNotYetImplementedElement(next);
					// homotypicalGroup = handleNom(state, reader, next, taxon,
					// homotypicalGroup);
				} else if (isStartingElement(next, COORDINATES)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, ANNOTATION)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next);
				}
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<SpecimenType> has no closing tag"); 
	}

//	private NamedArea createArea(String text, NamedAreaLevel areaLevel, MarkupImportState state) {
//		NamedArea area = NamedArea.NewInstance(text, text, null);
//		area.setLevel(areaLevel);
//		save(area, state);
//		return area;
//	}

	private AgentBase<?> createCollector(String collectorStr) {
		return createAuthor(collectorStr);
	}

	private String getCData(MarkupImportState state, XMLEventReader reader, XMLEvent next) throws XMLStreamException {
		return getCData(state, reader, next, true);
	}
		
	/**
	 * Reads character data. Any element other than character data or the ending
	 * tag will fire an unexpected element event.
	 * 
	 * @param state
	 * @param reader
	 * @param next
	 * @return
	 * @throws XMLStreamException
	 */
	private String getCData(MarkupImportState state, XMLEventReader reader, XMLEvent next,boolean checkAttributes) throws XMLStreamException {
		if (checkAttributes){
			checkNoAttributes(next);
		}

		String text = "";
		while (reader.hasNext()) {
			XMLEvent myNext = readNoWhitespace(reader);
			if (isMyEndingElement(myNext, next)) {
				return text;
			} else if (myNext.isCharacters()) {
				text += myNext.asCharacters().getData();
			} else {
				handleUnexpectedElement(myNext);
			}
		}
		throw new IllegalStateException("Event has no closing tag");

	}

	/**
	 * Creates the name defined by a nom tag. Adds it to the given homotypical
	 * group (if not null).
	 * 
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
		// attributes
		String classValue = getClassOnlyAttribute(parentEvent);
		NonViralName<?> name;
		if (!isNameType && ACCEPTED.equalsIgnoreCase(classValue)) {
			isSynonym = false;
			name = createName(state, homotypicalGroup, isSynonym);
		} else if (!isNameType && SYNONYM.equalsIgnoreCase(classValue)) {
			isSynonym = true;
			name = createName(state, homotypicalGroup, isSynonym);
		} else if (isNameType && NAME_TYPE.equalsIgnoreCase(classValue)) {
			// TODO do we need to define the rank here?
			name = createNameByCode(state, null);
		} else {
			fireUnexpectedAttributeValue(parentEvent, CLASS, classValue);
			name = createNameByCode(state, null);
		}

		Map<String, String> nameMap = new HashMap<String, String>();

		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()) {
				if (isMyEndingElement(next, parentEvent)) {
					// fill the name with all data gathered
					fillName(state, nameMap, name, next);
					return name;
				} else {
					if (isEndingElement(next, FULL_NAME)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, NUM)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, HOMONYM)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, NOTES)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, ANNOTATION)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else {
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, FULL_NAME)) {
					handleNotYetImplementedElement(next);
					// homotypicalGroup = handleNom(state, reader, next, taxon,
					// homotypicalGroup);
				} else if (isStartingElement(next, NUM)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, NAME)) {
					handleName(state, reader, next, nameMap);
				} else if (isStartingElement(next, CITATION)) {
					handleCitation(state, reader, next, name);
				} else if (isStartingElement(next, HOMONYM)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, NOTES)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, ANNOTATION)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next);
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		// TODO handle missing end element
		throw new IllegalStateException("Nom has no closing tag");

	}

	private void fillName(MarkupImportState state, Map<String, String> nameMap,
			NonViralName name, XMLEvent event) {

		// Ranks: family, subfamily, tribus, genus, subgenus, section,
		// subsection, species, subspecies, variety, subvariety, forma
		// infrank, paraut, author, infrparaut, infraut, status, notes

		String infrank = getAndRemoveMapKey(nameMap, INFRANK);
		String authorStr = getAndRemoveMapKey(nameMap, AUTHOR);
		String paraut = getAndRemoveMapKey(nameMap, PARAUT);

		String infrParAut = getAndRemoveMapKey(nameMap, INFRPARAUT);
		String infrAut = getAndRemoveMapKey(nameMap, INFRAUT);

		String statusStr = getAndRemoveMapKey(nameMap, STATUS);
		String notes = getAndRemoveMapKey(nameMap, NOTES);

		makeRankDecision(state, nameMap, name, event, infrank);

		// test consistency of rank and authors
		testRankAuthorConsistency(name, event, authorStr, paraut, infrParAut,infrAut);

		// authors
		makeNomenclaturalAuthors(name, event, authorStr, paraut, infrParAut,infrAut);

		// status
		// TODO handle pro parte, pro syn. etc.
		if (StringUtils.isNotBlank(statusStr)) {
			String proPartePattern = "(pro parte|p.p.)";
			if (statusStr.matches(proPartePattern)) {
				state.setProParte(true);
			}
			try {
				// TODO handle trim earlier
				statusStr = statusStr.trim();
				NomenclaturalStatusType nomStatusType = NomenclaturalStatusType
						.getNomenclaturalStatusTypeByAbbreviation(statusStr);
				name.addStatus(NomenclaturalStatus.NewInstance(nomStatusType));
			} catch (UnknownCdmTypeException e) {
				String message = "Status '%s' could not be recognized";
				message = String.format(message, statusStr);
				fireWarningEvent(message, event, 4);
			}
		}

		// notes
		if (StringUtils.isNotBlank(notes)) {
			handleNotYetImplementedAttributeValue(event, CLASS, NOTES);
		}

		return;
	}

	/**
	 * @param state
	 * @param nameMap
	 * @param name
	 * @param event
	 * @param infrankStr
	 */
	private void makeRankDecision(MarkupImportState state,
			Map<String, String> nameMap, NonViralName<?> name, XMLEvent event,
			String infrankStr) {
		// TODO ranks
		for (String key : nameMap.keySet()) {
			Rank rank = makeRank(state, key, false);
			if (rank == null) {
				handleNotYetImplementedAttributeValue(event, CLASS, key);
			} else {
				if (name.getRank() == null || rank.isLower(name.getRank())) {
					name.setRank(rank);
				}
				String value = nameMap.get(key);
				if (rank.isSupraGeneric() || rank.isGenus()) {
					name.setGenusOrUninomial(value);
				} else if (rank.isInfraGeneric()) {
					name.setInfraGenericEpithet(value);
				} else if (rank.isSpecies()) {
					name.setSpecificEpithet(value);
				} else if (rank.isInfraSpecific()) {
					name.setInfraSpecificEpithet(value);
				} else {
					String message = "Invalid rank '%s'. Can't decide which epithet to fill with '%s'";
					message = String.format(message, rank.getTitleCache(),value);
					fireWarningEvent(message, event, 4);
				}
			}

		}
		// handle given infrank marker
		if (StringUtils.isNotBlank(infrankStr)) {
			Rank infRank = makeRank(state, infrankStr, true);

			if (infRank == null) {
				String message = "Infrank '%s' rank not recognized";
				message = String.format(message, infrankStr);
				fireWarningEvent(message, event, 4);
			} else {
				if (name.getRank() == null) {
					name.setRank(infRank);
				} else if (infRank.isLower(name.getRank())) {
					String message = "InfRank '%s' is lower than existing rank ";
					message = String.format(message, infrankStr);
					fireWarningEvent(message, event, 2);
					name.setRank(infRank);
				} else if (infRank.equals(name.getRank())) {
					// nothing
				} else {
					String message = "InfRank '%s' is higher than existing rank ";
					message = String.format(message, infrankStr);
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
		if (name.getRank() != null && name.getRank().isInfraSpecific()) {
			if (StringUtils.isNotBlank(infrAut)) {
				INomenclaturalAuthor[] authorAndEx = authorAndEx(infrAut, event);
				name.setCombinationAuthorTeam(authorAndEx[0]);
				name.setExCombinationAuthorTeam(authorAndEx[1]);
			}
			if (StringUtils.isNotBlank(infrParAut)) {
				INomenclaturalAuthor[] authorAndEx = authorAndEx(infrParAut, event);
				name.setBasionymAuthorTeam(authorAndEx[0]);
				name.setExBasionymAuthorTeam(authorAndEx[1]);
			}
		} else {
			if (name.getRank() == null){
				String message = "No rank defined. Check correct usage of authors!";
				fireWarningEvent(message, event, 4);
				if (isNotBlank(infrParAut) || isNotBlank(infrAut)){
					authorStr = infrAut;
					paraut = infrParAut;
				}
			}
			if (StringUtils.isNotBlank(authorStr)) {
				INomenclaturalAuthor[] authorAndEx = authorAndEx(authorStr, event);
				name.setCombinationAuthorTeam(authorAndEx[0]);
				name.setExCombinationAuthorTeam(authorAndEx[1]);
			}
			if (StringUtils.isNotBlank(paraut)) {
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
		if (split.length > 2) {
			String message = "There is more then 1 ' ex ' in author string. Can't separate author and ex-author";
			fireWarningEvent(message, xmlEvent, 4);
			result[0] = createAuthor(authorAndEx);
		} else if (split.length == 2) {
			result[0] = createAuthor(split[1]);
			result[1] = createAuthor(split[0]);
		} else {
			result[0] = createAuthor(split[0]);
		}
		return result;
	}

	/**
	 * Tests if the names rank is consistent with the given author strings.
	 * @param name
	 * @param event
	 * @param authorStr
	 * @param paraut
	 * @param infrParAut
	 * @param infrAut
	 */
	private void testRankAuthorConsistency(NonViralName name, XMLEvent event,
					String authorStr, String paraut, String infrParAut, String infrAut) {
		if (name.getRank() == null){
			return;
		}
		if (name.getRank().isInfraSpecific()) {
			if (StringUtils.isBlank(infrParAut)
					&& StringUtils.isNotBlank(infrAut)
					&& (StringUtils.isNotBlank(paraut) || StringUtils.isNotBlank(authorStr))) {
				String message = "Rank is infraspecicific but has only specific or higher author(s)";
				fireWarningEvent(message, event, 4);
			}
		} else {
			// is not infraspecific
			if (StringUtils.isNotBlank(infrParAut) 	|| StringUtils.isNotBlank(infrAut)) {
				String message = "Rank is not infraspecicific but name has infra author(s)";
				fireWarningEvent(message, event, 4);
			}
		}
	}

	/**
	 * Returns the (empty) name with the correct homotypical group depending on
	 * the taxon status. Throws NPE if no currentTaxon is set in state.
	 * 
	 * @param state
	 * @param homotypicalGroup
	 * @param isSynonym
	 * @return
	 */
	private NonViralName<?> createName(MarkupImportState state,
			HomotypicalGroup homotypicalGroup, boolean isSynonym) {
		NonViralName<?> name;
		Taxon taxon = state.getCurrentTaxon();
		if (isSynonym) {
			Rank defaultRank = Rank.SPECIES(); // can be any
			name = createNameByCode(state, defaultRank);
			if (homotypicalGroup != null) {
				name.setHomotypicalGroup(homotypicalGroup);
			}
			SynonymRelationshipType synonymType = SynonymRelationshipType
					.HETEROTYPIC_SYNONYM_OF();
			if (taxon.getHomotypicGroup().equals(homotypicalGroup)) {
				synonymType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
			}
			taxon.addSynonymName(name, synonymType);
		} else {
			name = CdmBase.deproxy(taxon.getName(), NonViralName.class);
		}
		return name;
	}

	private void handleName(MarkupImportState state, XMLEventReader reader,
			XMLEvent parentEvent, Map<String, String> nameMap)
			throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);

		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				nameMap.put(classValue, text);
				return;
			} else if (next.isStartElement()) {
				if (isStartingElement(next, ANNOTATION)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
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
	private Rank makeRank(MarkupImportState state, String value,
			boolean byAbbrev) {
		Rank rank = null;
		if (StringUtils.isBlank(value)) {
			return null;
		}
		try {
			boolean useUnknown = true;
			NomenclaturalCode nc = makeNomenclaturalCode(state);
			if (byAbbrev) {
				rank = Rank.getRankByAbbreviation(value, nc, useUnknown);
			} else {
				rank = Rank.getRankByEnglishName(value, nc, useUnknown);
			}
			if (rank.equals(Rank.UNKNOWN_RANK())) {
				rank = null;
			}
		} catch (UnknownCdmTypeException e) {
			// doNothing
		}
		return rank;
	}

	// public void handleNameNotRank(MarkupImportState state, XMLEventReader
	// reader, XMLEvent parentEvent, String classValue, NonViralName name)
	// throws XMLStreamException {
	// if (ACCEPTED.equalsIgnoreCase(classValue)){
	// }else if (SYNONYM.equalsIgnoreCase(classValue)){
	// }else{
	// //TODO Not yet implemented
	// handleNotYetImplementedAttributeValue(parentEvent, CLASS, classValue);
	// }
	// }

	private void handleCitation(MarkupImportState state, XMLEventReader reader,	XMLEvent parentEvent, NonViralName name) throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);

		state.setCitation(true);
		boolean hasRefPart = false;
		Map<String, String> refMap = new HashMap<String, String>();
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				checkMandatoryElement(hasRefPart, parentEvent.asStartElement(),
						REF_PART);
				Reference<?> reference = createReference(state, refMap, next);
				String microReference = refMap.get(DETAILS);
				doCitation(state, name, classValue, reference, microReference,
						parentEvent);
				state.setCitation(false);
				return;
			} else if (isStartingElement(next, REF_PART)) {
				handleRefPart(state, reader, next, refMap);
				hasRefPart = true;
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("Citation has no closing tag");

	}

	private void handleRefPart(MarkupImportState state, XMLEventReader reader,XMLEvent parentEvent, Map<String, String> refMap) throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);

		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				refMap.put(classValue, text);
				return;
			} else if (next.isStartElement()) {
				if (isStartingElement(next, ANNOTATION)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, ITALICS)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, BOLD)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedEndElement(next.asEndElement());
			}
		}
		throw new IllegalStateException("RefPart has no closing tag");

	}

	private Reference<?> createReference(MarkupImportState state, Map<String, String> refMap, XMLEvent parentEvent) {
		// TODO
		Reference<?> reference;

		String type = getAndRemoveMapKey(refMap, PUBTYPE);
		String authorStr = getAndRemoveMapKey(refMap, AUTHOR);
		String titleStr = getAndRemoveMapKey(refMap, PUBTITLE);
		String titleCache = getAndRemoveMapKey(refMap, PUBFULLNAME);
		String volume = getAndRemoveMapKey(refMap, VOLUME);
		String edition = getAndRemoveMapKey(refMap, EDITION);
		String editors = getAndRemoveMapKey(refMap, EDITORS);
		String year = getAndRemoveMapKey(refMap, YEAR);
		String pubName = getAndRemoveMapKey(refMap, PUBNAME);
		String pages = getAndRemoveMapKey(refMap, PAGES);

		if (state.isCitation()) {
			if (volume != null || "journal".equalsIgnoreCase(type)) {
				IArticle article = ReferenceFactory.newArticle();
				if (pubName != null) {
					IJournal journal = ReferenceFactory.newJournal();
					journal.setTitle(pubName);
					article.setInJournal(journal);
				}
				reference = (Reference<?>) article;

			} else {
				// TODO
				if (pubName != null){
					reference  = ReferenceFactory.newBookSection();
				}else{
					reference = ReferenceFactory.newBook();
				}
			}
			// TODO use existing author from name or before
			TeamOrPersonBase<?> author = createAuthor(authorStr);
			reference.setAuthorTeam(author);

			reference.setTitle(titleStr);
			if (StringUtils.isNotBlank(titleCache)) {
				reference.setTitleCache(titleCache, true);
			}
			reference.setEdition(edition);
			reference.setEditor(editors);

			if (pubName != null) {
				Reference<?> inReference;
				if (reference.getType().equals(ReferenceType.Article)) {
					inReference = ReferenceFactory.newJournal();
				} else {
					inReference = ReferenceFactory.newGeneric();
				}
				inReference.setTitle(pubName);
				reference.setInReference(inReference);
			}

			
		} else {  //no citation
			if (volume != null || "journal".equalsIgnoreCase(type)) {
				IArticle article = ReferenceFactory.newArticle();
				if (pubName != null) {
					IJournal journal = ReferenceFactory.newJournal();
					journal.setTitle(pubName);
					article.setInJournal(journal);
				}
				reference = (Reference<?>) article;

			} else {
				Reference<?> bookOrPartOf = ReferenceFactory.newGeneric();
				reference = bookOrPartOf;
			}

			// TODO type
			TeamOrPersonBase<?> author = createAuthor(authorStr);
			reference.setAuthorTeam(author);

			reference.setTitle(titleStr);
			if (StringUtils.isNotBlank(titleCache)) {
				reference.setTitleCache(titleCache, true);
			}
			reference.setEdition(edition);
			reference.setEditor(editors);

			if (pubName != null) {
				Reference<?> inReference;
				if (reference.getType().equals(ReferenceType.Article)) {
					inReference = ReferenceFactory.newJournal();
				} else {
					inReference = ReferenceFactory.newGeneric();
				}
				inReference.setTitle(pubName);
				reference.setInReference(inReference);
			}
		}
		reference.setVolume(volume);
		reference.setDatePublished(TimePeriod.parseString(year));
		//TODO check if this is handled correctly in FM markup
		reference.setPages(pages);

		// TODO
		String[] unhandledList = new String[]{ALTERNATEPUBTITLE, ISSUE, NOTES, STATUS};
		for (String unhandled : unhandledList){
			String value = getAndRemoveMapKey(refMap, unhandled);
			if (isNotBlank(value)){
				this.handleNotYetImplementedAttributeValue(parentEvent, CLASS, unhandled);
			}
		}
		
		for (String key : refMap.keySet()) {
			if (!DETAILS.equalsIgnoreCase(key)) {
				this.fireUnexpectedAttributeValue(parentEvent, CLASS, key);
			}
		}

		return reference;
	}

	private TeamOrPersonBase createAuthor(String authorTitle) {
		// TODO atomize and also use by name creation
		TeamOrPersonBase result = Team.NewTitledInstance(authorTitle,
				authorTitle);
		return result;
	}

	private String getAndRemoveMapKey(Map<String, String> map, String key) {
		String result = map.get(key);
		map.remove(key);
		if (result != null) {
			result = normalize(result);
		}
		return StringUtils.stripToNull(result);
	}

	private void doCitation(MarkupImportState state, NonViralName name,
			String classValue, Reference reference, String microCitation,
			XMLEvent parentEvent) {
		if (PUBLICATION.equalsIgnoreCase(classValue)) {
			name.setNomenclaturalReference(reference);
			name.setNomenclaturalMicroReference(microCitation);
		} else if (USAGE.equalsIgnoreCase(classValue)) {
			Taxon taxon = state.getCurrentTaxon();
			TaxonDescription td = this.getTaxonDescription(taxon, state
					.getConfig().getSourceReference(), false, true);
			TextData citation = TextData.NewInstance(Feature.CITATION());
			// TODO name used in source
			citation.addSource(null, null, reference, microCitation);
			td.addElement(citation);
		} else if (TYPE.equalsIgnoreCase(classValue)) {
			handleNotYetImplementedAttributeValue(parentEvent, CLASS,
					classValue);
		} else {
			// TODO Not yet implemented
			handleNotYetImplementedAttributeValue(parentEvent, CLASS,
					classValue);
		}
	}

	private void handleFeature(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);
		Feature feature = makeFeature(classValue, state, parentEvent);
		Taxon taxon = state.getCurrentTaxon();
		TaxonDescription taxonDescription = getTaxonDescription(taxon, state.getConfig().getSourceReference(), NO_IMAGE_GALLERY, CREATE_NEW);
		// TextData figureHolderTextData = null; //for use with one TextData for
		// all figure only

		boolean isDescription = feature.equals(Feature.DESCRIPTION());
		DescriptionElementBase lastDescriptionElement = null;
		
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				return;
			} else if (isEndingElement(next, DISTRIBUTION_LIST) || isEndingElement(next, HABITAT_LIST)) { 
				// only handle list elements
			} else if (isStartingElement(next, HEADING)) {
				makeFeatureHeading(state, reader, classValue, feature, next);
			} else if (isStartingElement(next, WRITER)) {
				makeFeatureWriter(state, reader, feature, taxon, next);
//			} else if (isStartingElement(next, DISTRIBUTION_LOCALITY)) {
//				if (!feature.equals(Feature.DISTRIBUTION())) {
//					String message = "Distribution locality only allowed for feature of type 'distribution'";
//					fireWarningEvent(message, next, 4);
//				}
//				handleDistributionLocality(state, reader, next);
			} else if (isStartingElement(next, DISTRIBUTION_LIST) || isStartingElement(next, HABITAT_LIST)) {
				// only handle single list elements
			} else if (isStartingElement(next, HABITAT)) {
				if (!(feature.equals(Feature.HABITAT())
						|| feature.equals(Feature.HABITAT_ECOLOGY()) 
						|| feature.equals(Feature.ECOLOGY()))) {
					String message = "Habitat only allowed for feature of type 'habitat','habitat ecology' or 'ecology'";
					fireWarningEvent(message, next, 4);
				}
				handleHabitat(state, reader, next);
			} else if (isStartingElement(next, CHAR)) {
				TextData textData = handleChar(state, reader, next);
				taxonDescription.addElement(textData);
			} else if (isStartingElement(next, STRING)) {
				lastDescriptionElement = makeFeatureString(state, reader,feature, taxonDescription, lastDescriptionElement,next);
			} else if (isStartingElement(next, FIGURE_REF)) {
				lastDescriptionElement = makeFeatureFigureRef(state, reader, taxonDescription, isDescription, lastDescriptionElement, next);
			} else if (isStartingElement(next, REFERENCES)) {
				// TODO details/microcitation ??

				List<Reference<?>> refs = handleReferences(state, reader, next);
				if (!refs.isEmpty()) {
					// TODO
					Reference<?> descriptionRef = state.getConfig().getSourceReference();
					TaxonDescription description = getTaxonDescription(taxon, descriptionRef, false, true);
					TextData featurePlaceholder = getFeaturePlaceholder(state, description, feature, true);
					for (Reference<?> citation : refs) {
						featurePlaceholder.addSource(null, null, citation, null);
					}
				} else {
					String message = "No reference found in references";
					fireWarningEvent(message, next, 6);
				}
			} else if (isStartingElement(next, NUM)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, NUM)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<Feature> has no closing tag");
	}

	/**
	 * @param state
	 * @param reader
	 * @param taxonDescription
	 * @param isDescription
	 * @param lastDescriptionElement
	 * @param next
	 * @return
	 * @throws XMLStreamException
	 */
	private DescriptionElementBase makeFeatureFigureRef(MarkupImportState state, XMLEventReader reader,TaxonDescription taxonDescription, 
					boolean isDescription, DescriptionElementBase lastDescriptionElement, XMLEvent next)throws XMLStreamException {
		FigureDataHolder figureHolder = handleFigureRef(state, reader, next);
		Feature figureFeature = getFeature(state,MarkupTransformer.uuidFigures, "Figures", "Figures", "Fig.",null);
		if (isDescription) {
			TextData figureHolderTextData = null;
			// if (figureHolderTextData == null){
			figureHolderTextData = TextData.NewInstance(figureFeature);
			if (StringUtils.isNotBlank(figureHolder.num)) {
				String annotationText = "<num>" + figureHolder.num.trim()
						+ "</num>";
				Annotation annotation = Annotation.NewInstance(annotationText,
						AnnotationType.TECHNICAL(), Language.DEFAULT());
				figureHolderTextData.addAnnotation(annotation);
			}
			if (StringUtils.isNotBlank(figureHolder.figurePart)) {
				String annotationText = "<figurePart>"+ figureHolder.figurePart.trim() + "</figurePart>";
				Annotation annotation = Annotation.NewInstance(annotationText,AnnotationType.EDITORIAL(), Language.DEFAULT());
				figureHolderTextData.addAnnotation(annotation);
			}
			// if (StringUtils.isNotBlank(figureText)){
			// figureHolderTextData.putText(Language.DEFAULT(), figureText);
			// }
			taxonDescription.addElement(figureHolderTextData);
			// }
			registerFigureDemand(state, figureHolderTextData, figureHolder.ref);
		} else {
			if (lastDescriptionElement == null) {
				String message = "No description element created yet that can be referred by figure. Create new TextData instead";
				fireWarningEvent(message, next, 4);
				lastDescriptionElement = TextData.NewInstance(figureFeature);
				taxonDescription.addElement(lastDescriptionElement);
			}
			registerFigureDemand(state, lastDescriptionElement,
					figureHolder.ref);
		}
		return lastDescriptionElement;
	}

	/**
	 * @param state
	 * @param reader
	 * @param feature
	 * @param taxonDescription
	 * @param lastDescriptionElement
	 * @param distributionList 
	 * @param next
	 * @return
	 * @throws XMLStreamException
	 */
	private DescriptionElementBase makeFeatureString(MarkupImportState state,XMLEventReader reader, Feature feature, 
				TaxonDescription taxonDescription, DescriptionElementBase lastDescriptionElement, XMLEvent next) throws XMLStreamException {
		Map<String, String> subheadingMap = handleString(state, reader, next, feature);
		for (String subheading : subheadingMap.keySet()) {
			Feature subheadingFeature = feature;
			if (StringUtils.isNotBlank(subheading) && subheadingMap.size() > 1) {
				subheadingFeature = makeFeature(subheading, state, next);
			}
			TextData textData = TextData.NewInstance(subheadingFeature);
			textData.putText(Language.DEFAULT(), subheadingMap.get(subheading));
			taxonDescription.addElement(textData);
			// TODO how to handle figures when these data are split in
			// subheadings
			lastDescriptionElement = textData;
		}
		return lastDescriptionElement;
	}

	/**
	 * @param state
	 * @param reader
	 * @param feature
	 * @param taxon
	 * @param next
	 * @throws XMLStreamException
	 */
	private void makeFeatureWriter(MarkupImportState state,XMLEventReader reader, Feature feature, Taxon taxon, XMLEvent next) throws XMLStreamException {
		WriterDataHolder writer = handleWriter(state, reader, next);
		if (isNotBlank(writer.writer)) {
			// TODO
			Reference<?> ref = state.getConfig().getSourceReference();
			TaxonDescription description = getTaxonDescription(taxon, ref,
					false, true);
			TextData featurePlaceholder = getFeaturePlaceholder(state,
					description, feature, true);
			featurePlaceholder.addAnnotation(writer.annotation);
			registerFootnotes(state, featurePlaceholder, writer.footnotes);
		} else {
			String message = "Writer element is empty";
			fireWarningEvent(message, next, 4);
		}
	}

	/**
	 * @param state
	 * @param reader
	 * @param classValue
	 * @param feature
	 * @param next
	 * @throws XMLStreamException
	 */
	private void makeFeatureHeading(MarkupImportState state, XMLEventReader reader, String classValue, Feature feature, XMLEvent next) throws XMLStreamException {
		String heading = handleHeading(state, reader, next);
		if (StringUtils.isNotBlank(heading)) {
			if (!heading.equalsIgnoreCase(classValue)) {
				try {
					if (!feature.equals(state.getTransformer().getFeatureByKey(
							heading))) {
						UUID headerFeatureUuid = state.getTransformer()
								.getFeatureUuid(heading);
						if (!feature.getUuid().equals(headerFeatureUuid)) {
							String message = "Feature heading '%s' differs from feature class '%s' and can not be transformed to feature";
							message = String.format(message, heading,
									classValue);
							fireWarningEvent(message, next, 1);
						}
					}
				} catch (UndefinedTransformerMethodException e) {
					throw new RuntimeException(e);
				}
			} else {
				// do nothing
			}
		}
	}

	private List<Reference<?>> handleReferences(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		// attributes
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String bibliography = getAndRemoveAttributeValue(attributes,
				BIBLIOGRAPHY);
		String serialsAbbreviations = getAndRemoveAttributeValue(attributes,
				SERIALS_ABBREVIATIONS);
		if (isNotBlank(bibliography) || isNotBlank(serialsAbbreviations)) {
			String message = "Attributes not yet implemented for <references>";
			fireWarningEvent(message, parentEvent, 4);
		}

		List<Reference<?>> result = new ArrayList<Reference<?>>();

		// elements
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()) {
				if (isMyEndingElement(next, parentEvent)) {
					return result;
				} else {
					if (isEndingElement(next, HEADING)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, WRITER)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, FOOTNOTE)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, STRING)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else if (isEndingElement(next, REF_NUM)) {
						// NOT YET IMPLEMENTED
						popUnimplemented(next.asEndElement());
					} else {
						handleUnexpectedEndElement(next.asEndElement());
					}
				}
			} else if (next.isStartElement()) {
				if (isStartingElement(next, HEADING)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, SUB_HEADING)) {
					String subheading = getCData(state, reader, next).trim();
					String excludePattern = "(i?)(References?|Literature):?";
					if (!subheading.matches(excludePattern)) {
						fireNotYetImplementedElement(next.getLocation(), next.asStartElement().getName(), 0);
					}
				} else if (isStartingElement(next, WRITER)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, FOOTNOTE)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, STRING)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, REF_NUM)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, REFERENCE)) {
					Reference<?> ref = handleReference(state, reader, next);
					result.add(ref);
				} else {
					handleUnexpectedStartElement(next);
				}
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<References> has no closing tag");
	}

	private void handleHabitat(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		checkNoAttributes(parentEvent);
		Taxon taxon = state.getCurrentTaxon();
		// TODO which ref to take?
		Reference<?> ref = state.getConfig().getSourceReference();

		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				TaxonDescription description = getTaxonDescription(taxon, ref,
						false, true);
				UUID uuidExtractedHabitat = MarkupTransformer.uuidExtractedHabitat;
				Feature feature = getFeature(
						state,
						uuidExtractedHabitat,
						"Extracted Habitat",
						"An structured habitat that was extracted from a habitat text",
						"extr. habit.", null);
				TextData habitat = TextData.NewInstance(feature);
				habitat.putText(Language.DEFAULT(), text);
				description.addElement(habitat);

				return;
			} else if (next.isStartElement()) {
				if (isStartingElement(next, ALTITUDE)) {
					text = text.trim() + getTaggedCData(state, reader, next);
				} else if (isStartingElement(next, LIFE_CYCLE_PERIODS)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<Habitat> has no closing tag");
	}

	private String getTaggedCData(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		checkNoAttributes(parentEvent);

		String text = getXmlTag(parentEvent);
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				text += getXmlTag(next);
				return text;
			} else if (next.isStartElement()) {
				text += getTaggedCData(state, reader, next);
			} else if (next.isEndElement()) {
				text += getTaggedCData(state, reader, next);
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedEndElement(next.asEndElement());
			}
		}
		throw new IllegalStateException("Some tag has no closing tag");
	}

	private String handleDistributionLocality(MarkupImportState state,XMLEventReader reader, XMLEvent parentEvent)throws XMLStreamException {
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String classValue = getAndRemoveRequiredAttributeValue(parentEvent, attributes, CLASS);
		String statusValue =getAndRemoveAttributeValue(attributes, STATUS);
		String frequencyValue =getAndRemoveAttributeValue(attributes, FREQUENCY);
		

		Taxon taxon = state.getCurrentTaxon();
		// TODO which ref to take?
		Reference<?> ref = state.getConfig().getSourceReference();

		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (StringUtils.isNotBlank(text)) {
					String label = CdmUtils.removeTrailingDot(normalize(text));
					TaxonDescription description = getTaxonDescription(taxon, ref, false, true);
					NamedAreaLevel level = makeNamedAreaLevel(state,classValue, next);
					
					//status
					PresenceAbsenceTermBase<?> status = null;
					if (isNotBlank(statusValue)){
						try {
							status = state.getTransformer().getPresenceTermByKey(statusValue);
							if (status == null){
								//TODO
								String message = "The status '%s' could not be transformed to an CDM status";
								fireWarningEvent(message, next, 4);
							}
						} catch (UndefinedTransformerMethodException e) {
							throw new RuntimeException(e);
						}
					}else{
						status = PresenceTerm.PRESENT();
					}
					//frequency
					if (isNotBlank(frequencyValue)){
						String message = "The frequency attribute is currently not yet available in CDM";
						fireWarningEvent(message, parentEvent, 6);
					}
					
					NamedArea higherArea = null;
					List<NamedArea> areas = new ArrayList<NamedArea>(); 
					
					String patSingleArea = "([^,\\(]{3,})";
					String patSeparator = "(,|\\sand\\s)";
					String hierarchiePattern = String.format("%s\\((%s(%s%s)*)\\)",patSingleArea, patSingleArea, patSeparator, patSingleArea);
					Pattern patHierarchie = Pattern.compile(hierarchiePattern, Pattern.CASE_INSENSITIVE);
					Matcher matcher = patHierarchie.matcher(label); 
					if (matcher.matches()){
						String higherAreaStr = matcher.group(1).trim();
						higherArea =  makeArea(state, higherAreaStr, level);
						String[] innerAreas = matcher.group(2).split(patSeparator);
						for (String innerArea : innerAreas){
							if (isNotBlank(innerArea)){
								NamedArea singleArea = makeArea(state, innerArea.trim(), level);
								areas.add(singleArea);
								NamedArea partOf = singleArea.getPartOf();
//								if (partOf == null){
//									singleArea.setPartOf(higherArea);
//								}
							}
						}
					}else{
						NamedArea singleArea = makeArea(state, label, level);
						areas.add(singleArea);
					}
					
					for (NamedArea area : areas){
						//create distribution
						Distribution distribution = Distribution.NewInstance(area,status);
						description.addElement(distribution);
					}
				} else {
					String message = "Empty distribution locality";
					fireWarningEvent(message, next, 4);
				}
				return text;
			} else if (isStartingElement(next, COORDINATES)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, COORDINATES)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedEndElement(next.asEndElement());
			}
		}
		throw new IllegalStateException("<DistributionLocality> has no closing tag");
	}

	/**
	 * @param state
	 * @param areaName
	 * @param level
	 * @return 
	 */
	private NamedArea makeArea(MarkupImportState state, String areaName, NamedAreaLevel level) {
		
		
		//TODO FM vocabulary
		TermVocabulary<NamedArea> voc = null; 
		NamedAreaType areaType = null;
		
		NamedArea area = null;
		try {
			area = state.getTransformer().getNamedAreaByKey(areaName);
		} catch (UndefinedTransformerMethodException e) {
			throw new RuntimeException(e);
		}
		if (area == null){
			boolean isNewInState = false;
			UUID uuid = state.getAreaUuid(areaName);
			if (uuid == null){
				isNewInState = true;
				
				
				try {
					uuid = state.getTransformer().getNamedAreaUuid(areaName);
				} catch (UndefinedTransformerMethodException e) {
					throw new RuntimeException(e);
				}
			}	
			TermMatchMode matchMode = TermMatchMode.UUID_LABEL;
			area = getNamedArea(state, uuid, areaName, areaName, areaName, areaType, level, voc, matchMode);
			if (isNewInState){
				state.putAreaUuid(areaName, area.getUuid());
				
				//TODO just for testing -> make generic and move to better place
				String geoServiceLayer="vmap0_as_bnd_political_boundary_a";
				String layerFieldName ="nam";
				
				if ("Bangka".equals(areaName)){
					String areaValue = "PULAU BANGKA#SUMATERA SELATAN";
					GeoServiceArea geoServiceArea = new GeoServiceArea();
					geoServiceArea.add(geoServiceLayer, layerFieldName, areaValue);
					this.editGeoService.setMapping(area, geoServiceArea);
//					save(area, state);
				}
				if ("Luzon".equals(areaName)){
					GeoServiceArea geoServiceArea = new GeoServiceArea();
					
					List<String> list = Arrays.asList("HERMANA MAYOR ISLAND#CENTRAL LUZON",
							"HERMANA MENOR ISLAND#CENTRAL LUZON",
							"CENTRAL LUZON");
					for (String areaValue : list){
						geoServiceArea.add(geoServiceLayer, layerFieldName, areaValue);
					}
					
					this.editGeoService.setMapping(area, geoServiceArea);
//					save(area, state);
				}
				if ("Mindanao".equals(areaName)){
					GeoServiceArea geoServiceArea = new GeoServiceArea();
					
					List<String> list = Arrays.asList("NORTHERN MINDANAO",
							"SOUTHERN MINDANAO",
							"WESTERN MINDANAO");
					//TODO to be continued
					for (String areaValue : list){
						geoServiceArea.add(geoServiceLayer, layerFieldName, areaValue);
					}
					
					this.editGeoService.setMapping(area, geoServiceArea);
//					save(area, state);
				}
				if ("Palawan".equals(areaName)){
					GeoServiceArea geoServiceArea = new GeoServiceArea();
					
					List<String> list = Arrays.asList("PALAWAN#SOUTHERN TAGALOG");
					for (String areaValue : list){
						geoServiceArea.add(geoServiceLayer, layerFieldName, areaValue);
					}
					
					this.editGeoService.setMapping(area, geoServiceArea);
//					save(area, state);
				}
				

			}
		}
		return area;
	}
	

	/**
	 * @param state
	 * @param levelString
	 * @param next
	 * @return
	 */
	private NamedAreaLevel makeNamedAreaLevel(MarkupImportState state,
			String levelString, XMLEvent next) {
		NamedAreaLevel level;
		try {
			level = state.getTransformer().getNamedAreaLevelByKey(levelString);
			if (level == null) {
				UUID levelUuid = state.getTransformer().getNamedAreaLevelUuid(levelString);
				if (levelUuid == null) {
					String message = "Unknown distribution locality class (named area level): %s. Create new level instead.";
					message = String.format(message, levelString);
					fireWarningEvent(message, next, 6);
				}
				level = getNamedAreaLevel(state, levelUuid, levelString,
						levelString, levelString, null);
			}
		} catch (UndefinedTransformerMethodException e) {
			throw new RuntimeException(e);
		}
		return level;
	}

	private String handleHeading(MarkupImportState state,XMLEventReader reader, XMLEvent parentEvent)throws XMLStreamException {
		checkNoAttributes(parentEvent);

		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				return text;
			} else if (next.isStartElement()) {
				if (isStartingElement(next, FOOTNOTE)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedEndElement(next.asEndElement());
			}
		}
		throw new IllegalStateException("<String> has no closing tag");

	}

	/**
	 * Handle string
	 * @param state
	 * @param reader
	 * @param parentEvent
	 * @param feature only needed for distributionLocalities
	 * @return
	 * @throws XMLStreamException
	 */
	private Map<String, String> handleString(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, Feature feature)throws XMLStreamException {
		// attributes
		String classValue = getClassOnlyAttribute(parentEvent, false);
		if (StringUtils.isNotBlank(classValue)) {
			String message = "class attribute for <string> not yet implemented";
			fireWarningEvent(message, parentEvent, 2);
		}

		// subheadings
		Map<String, String> subHeadingMap = new HashMap<String, String>();
		String currentSubheading = null;

		boolean isTextMode = true;
		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				putCurrentSubheading(subHeadingMap, currentSubheading, text);
				return subHeadingMap;
			} else if (isStartingElement(next, BR)) {
				text += "<br/>";
				isTextMode = false;
			} else if (isEndingElement(next, BR)) {
				isTextMode = true;
			} else if (isHtml(next)) {
				text += getXmlTag(next);
			} else if (isStartingElement(next, SUB_HEADING)) {
				text = putCurrentSubheading(subHeadingMap,currentSubheading, text);
				// TODO footnotes
				currentSubheading = getCData(state, reader, next).trim();
			} else if (isStartingElement(next, DISTRIBUTION_LOCALITY)) {
				if (feature != null && !feature.equals(Feature.DISTRIBUTION())) {
					String message = "Distribution locality only allowed for feature of type 'distribution'";
					fireWarningEvent(message, next, 4);
				}
				text += handleDistributionLocality(state, reader, next);
			} else if (next.isCharacters()) {
				if (!isTextMode) {
					String message = "String is not in text mode";
					fireWarningEvent(message, next, 6);
				} else {
					text += next.asCharacters().getData();
				}
			} else if (isStartingElement(next, HEADING)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, HEADING)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, QUOTE)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, QUOTE)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, DEDICATION)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, DEDICATION)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, TAXONTYPE)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, TAXONTYPE)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, FULL_NAME)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, FULL_NAME)) {
				//TODO
				popUnimplemented(next.asEndElement());
			}else if (isStartingElement(next, REFERENCES)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, REFERENCES)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, GATHERING)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, GATHERING)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, ANNOTATION)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, ANNOTATION)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, HABITAT)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, HABITAT)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, FIGURE_REF)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, FIGURE_REF)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, FIGURE)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, FIGURE)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, FOOTNOTE_REF)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, FOOTNOTE_REF)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, FOOTNOTE)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, FOOTNOTE)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, WRITER)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, WRITER)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (isStartingElement(next, DATES)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, DATES)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<String> has no closing tag");
	}

	/**
	 * @param subHeadingMap
	 * @param currentSubheading
	 * @param text
	 * @return
	 */
	private String putCurrentSubheading(Map<String, String> subHeadingMap, String currentSubheading, String text) {
		if (StringUtils.isNotBlank(text)) {
			text = removeStartingMinus(text);
			subHeadingMap.put(currentSubheading, text.trim());
		}
		return "";
	}

	private String removeStartingMinus(String string) {
		string = replaceStart(string, "-");
		string = replaceStart(string, "\u002d");
		string = replaceStart(string, "\u2013");
		string = replaceStart(string, "\u2014");
		string = replaceStart(string, "--");
		return string;
	}
	
	/**
	 * @param value
	 * @param replacementString
	 */
	private String replaceStart(String value, String replacementString) {
		if (value.startsWith(replacementString) ){
			value = value.substring(replacementString.length()).trim();
		}
		while (value.startsWith("-") || value.startsWith("\u2014") ){
			value = value.substring("-".length()).trim();
		}
		return value;
	}
	
	private String getXmlTag(XMLEvent event) {
		String result;
		if (event.isStartElement()) {
			result = "<" + event.asStartElement().getName().getLocalPart()
					+ ">";
		} else if (event.isEndElement()) {
			result = "</" + event.asEndElement().getName().getLocalPart() + ">";
		} else {
			String message = "Only start or end elements are allowed as Html tags";
			throw new IllegalStateException(message);
		}
		return result;
	}

	protected static final List<String> htmlList = Arrays.asList("sub", "sup",
			"ol", "ul", "li", "i", "b", "table", "br");

	private boolean isHtml(XMLEvent event) {
		if (event.isStartElement()) {
			String tag = event.asStartElement().getName().getLocalPart();
			return htmlList.contains(tag);
		} else if (event.isEndElement()) {
			String tag = event.asEndElement().getName().getLocalPart();
			return htmlList.contains(tag);
		} else {
			return false;
		}

	}

	private TextData handleChar(MarkupImportState state, XMLEventReader reader,
			XMLEvent parentEvent) throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);
		Feature feature = makeFeature(classValue, state, parentEvent);

		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				TextData textData = TextData.NewInstance(feature);
				textData.putText(Language.DEFAULT(), text);
				return textData;
			} else if (isStartingElement(next, FIGURE_REF)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isEndingElement(next, FIGURE_REF)) {
				//TODO
				popUnimplemented(next.asEndElement());
			} else if (next.isStartElement()) {
				if (isStartingElement(next, ANNOTATION)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, ITALICS)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, BOLD)) {
					handleNotYetImplementedElement(next);
				} else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
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
			if (feature != null) {
				return feature;
			}
			uuid = state.getTransformer().getFeatureUuid(classValue);
			if (uuid == null) {
				// TODO
				String message = "Uuid is not defined for '%s'";
				message = String.format(message, classValue);
				fireWarningEvent(message, parentEvent, 8);
			}
			String featureText = StringUtils.capitalize(classValue);

			// TODO eFlora vocabulary
			TermVocabulary<Feature> voc = null;
			feature = getFeature(state, uuid, featureText, featureText, classValue, voc);
			if (feature == null) {
				throw new NullPointerException(classValue + " not recognized as a feature");
			}
			return feature;
		} catch (Exception e) {
			String message = "Could not create feature for %s: %s";
			message = String.format(message, classValue, e.getMessage());
			fireWarningEvent(message, parentEvent, 4);
			return Feature.UNKNOWN();
		}
	}

	/**
	 * This comes from the old version, needs to be checked on need
	 * 
	 * @param state
	 */
	private void doAllTheOldOtherStuff(MarkupImportState state) {
		state.putTree(null, null);
		if (unmatchedLeads == null) {
			unmatchedLeads = UnmatchedLeads.NewInstance();
		}
		state.setUnmatchedLeads(unmatchedLeads);

		// TransactionStatus tx = startTransaction();
		unmatchedLeads.saveToSession(getPolytomousKeyNodeService());

		// TODO generally do not store the reference object in the config
		Reference sourceReference = state.getConfig().getSourceReference();
		getReferenceService().saveOrUpdate(sourceReference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common
	 * .IImportConfigurator)
	 */
	protected boolean isIgnore(MarkupImportState state) {
		return !state.getConfig().isDoTaxa();
	}

}
