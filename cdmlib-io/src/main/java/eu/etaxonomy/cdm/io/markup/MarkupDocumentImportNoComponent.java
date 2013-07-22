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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;


/**
 * @author a.mueller
 * 
 */
public class MarkupDocumentImportNoComponent extends MarkupImportBase {
	private static final Logger logger = Logger.getLogger(MarkupDocumentImportNoComponent.class);
	
	private MarkupKeyImport keyImport;
	private MarkupSpecimenImport specimenImport;

	private MarkupNomenclatureImport nomenclatureImport;
	
	public MarkupDocumentImportNoComponent(MarkupDocumentImport docImport) {
		super(docImport);
		this.keyImport = new MarkupKeyImport(docImport);
		this.specimenImport = new MarkupSpecimenImport(docImport);
		nomenclatureImport = new MarkupNomenclatureImport(docImport, keyImport, specimenImport);
	}

	public void doInvoke(MarkupImportState state) throws XMLStreamException { 
		XMLEventReader reader = state.getReader();
		
		// publication (= root element)
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

		
		return;

	}

	private void handlePublication(MarkupImportState state, XMLEventReader reader, XMLEvent currentEvent, String elName) throws XMLStreamException {

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
			} else if (isStartingElement(next, MODS)){
				handleNotYetImplementedElement(next);
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
			if (isMyEndingElement(next, parentEvent)) {
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
			} else if (isStartingElement(next, TAXON)) {
				Taxon thisTaxon = handleTaxon(state, reader, next.asStartElement());
				doTaxonRelation(state, thisTaxon, lastTaxon, parentEvent.getLocation());
				if (state.isTaxonInClassification() == true){
					lastTaxon = thisTaxon;
					// TODO for imports spanning multiple documents ?? Still needed?
					state.getConfig().setLastTaxonUuid(lastTaxon.getUuid());
				}
			} else if (isStartingElement(next, ADDENDA)) {
				handleNotYetImplementedElement(next);
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
	private void doTaxonRelation(MarkupImportState state, Taxon taxon, Taxon lastTaxon, Location dataLocation) {

		if (state.isTaxonInClassification() == false){
			return;
		}
		
		Classification tree = makeTree(state, dataLocation);
		if (lastTaxon == null) {
			tree.addChildTaxon(taxon, null, null);
			return;
		}
		Rank thisRank = taxon.getName().getRank();
		Rank lastRank = lastTaxon.getName().getRank();
		if (lastRank == null){
			String message = "Last rank was null. Can't create tree correctly";
			fireWarningEvent(message, makeLocationStr(dataLocation), 12);
		}
		if (lastTaxon.getTaxonNodes().size() > 0) {
			TaxonNode lastNode = lastTaxon.getTaxonNodes().iterator().next();
			if (thisRank == null){
				String message = "Rank is undefined for taxon '%s'. Can't create classification without rank.";
				message = String.format(message, taxon.getName().getTitleCache());
				fireWarningEvent(message, makeLocationStr(dataLocation), 6);
			}else if (thisRank.isLower(lastRank)) {
				lastNode.addChildTaxon(taxon, null, null);
				fillMissingEpithetsForTaxa(lastTaxon, taxon);
			} else if (thisRank.equals(lastRank)) {
				TaxonNode parent = lastNode.getParent();
				if (parent != null) {
					parent.addChildTaxon(taxon, null, null);
					fillMissingEpithetsForTaxa(parent.getTaxon(), taxon);
				} else {
					tree.addChildTaxon(taxon, null, null);
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
		Classification result = Classification.NewInstance(state.getConfig().getClassificationName(), getDefaultLanguage(state));
		state.putTree(null, result);
		return result;
	}

	private Taxon handleTaxon(MarkupImportState state, XMLEventReader reader, StartElement parentEvent) throws XMLStreamException {
		// TODO progress monitoring
		Map<String, Attribute> attributes = getAttributes(parentEvent);
		Taxon taxon = createTaxonAndName(state, attributes);
		state.setCurrentTaxon(taxon);
		state.addNewFeatureSorterLists(taxon.getUuid().toString());
		
		boolean hasTitle = false;
		boolean hasNomenclature = false;
		String taxonTitle = null;

		Reference<?> descriptionReference = state.getConfig().getSourceReference();
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (next.isEndElement()) {
				if (isMyEndingElement(next, parentEvent)) {
//					checkMandatoryElement(hasTitle, parentEvent, TAXONTITLE);
					checkMandatoryElement(hasNomenclature, parentEvent,	NOMENCLATURE);
					boolean inClassification = getAndRemoveBooleanAttributeValue(next, attributes, "inClassification", true);
					state.setTaxonInClassification(inClassification);
					handleUnexpectedAttributes(parentEvent.getLocation(),attributes);
					if (taxon.getName().getRank() == null){
						String warning = "No rank exists for taxon " + taxon.getTitleCache();
						fireWarningEvent(warning, next, 12);
						taxon.getName().setRank(Rank.UNKNOWN_RANK());
					}
					
					keyImport.makeKeyNodes(state, parentEvent, taxonTitle);
					state.setCurrentTaxon(null);
					state.setCurrentTaxonNum(null);
					if (taxon.getName().getRank().isHigher(Rank.GENUS())){
						state.setLatestGenusEpithet(null);
					}else{
						state.setLatestGenusEpithet(((NonViralName<?>)taxon.getName()).getGenusOrUninomial());
					}
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
					keyImport.handleKey(state, reader, next);
				} else if (isStartingElement(next, NOMENCLATURE)) {
					nomenclatureImport.handleNomenclature(state, reader, next);
					hasNomenclature = true;
				} else if (isStartingElement(next, FEATURE)) {
					handleFeature(state, reader, next);
				} else if (isStartingElement(next, NOTES)) {
					// TODO is this the correct way to handle notes?
					String note = handleNotes(state, reader, next);

					UUID notesUuid;
					try {
						notesUuid = state.getTransformer().getFeatureUuid("notes");
						Feature feature = getFeature(state, notesUuid, "Notes",	"Notes", "note", null);
						TextData textData = TextData.NewInstance(feature);
						textData.putText(getDefaultLanguage(state), note);
						TaxonDescription description = getTaxonDescription(taxon, descriptionReference, false, true);
						description.addElement(textData);
					} catch (UndefinedTransformerMethodException e) {
						String message = "getFeatureUuid method not yet implemented";
						fireWarningEvent(message, next, 8);
					}
				} else if (isStartingElement(next, REFERENCES)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, FIGURE_REF)) {
					TaxonDescription desc = getTaxonDescription(taxon, state.getConfig().getSourceReference(), IMAGE_GALLERY, CREATE_NEW);
					TextData textData;
					if (desc.getElements().isEmpty()){
						textData = TextData.NewInstance(Feature.IMAGE());
						desc.addElement(textData);
					}
					textData = (TextData)desc.getElements().iterator().next();
					makeFeatureFigureRef(state, reader, desc, false, textData, next);
				} else if (isStartingElement(next, FIGURE)) {
					handleFigure(state, reader, next);
				} else if (isStartingElement(next, FOOTNOTE)) {
					FootnoteDataHolder footnote = handleFootnote(state, reader,	next);
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
		Rank rank = null;  //Rank.SPECIES(); // default
		boolean isCultivar = checkAndRemoveAttributeValue(attributes, CLASS, "cultivated");
		if (isCultivar) {
			name = CultivarPlantName.NewInstance(rank);
		} else {
			name = createNameByCode(state, rank);
		}
		Taxon taxon = Taxon.NewInstance(name, state.getConfig().getSourceReference());
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
				}else if (isStartingElement(next, FOOTNOTE_REF)) {
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
					Annotation annotation = Annotation.NewInstance(text, writerAnnotationType, getDefaultLanguage(state));
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

	private void registerGivenFigure(MarkupImportState state, XMLEvent next, String id, Media figure) {
		state.registerFigure(id, figure);
		Set<AnnotatableEntity> demands = state.getFigureDemands(id);
		if (demands != null) {
			for (AnnotatableEntity entity : demands) {
				attachFigure(state, next, entity, figure);
			}
		}
		save(figure, state);
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

	private void registerFigureDemand(MarkupImportState state, XMLEvent next, AnnotatableEntity entity, String figureRef) {
		Media existingFigure = state.getFigure(figureRef);
		if (existingFigure != null) {
			attachFigure(state, next, entity, existingFigure);
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
		Annotation annotation = Annotation.NewInstance(footnote.string, annotationType, getDefaultLanguage(state));
		// TODO transient objects
		entity.addAnnotation(annotation);
		save(entity, state);
	}

	private void attachFigure(MarkupImportState state, XMLEvent next, AnnotatableEntity entity, Media figure) {
		// IdentifiableEntity<?> toSave;
		if (entity.isInstanceOf(TextData.class)) {
			TextData deb = CdmBase.deproxy(entity, TextData.class);
			deb.addMedia(figure);
			// toSave = ((TaxonDescription)deb.getInDescription()).getTaxon();
		} else if (entity.isInstanceOf(SpecimenOrObservationBase.class)) {
			String message = "figures for specimen should be handled as Textdata";
			fireWarningEvent(message, next, 4);
			// toSave = ime;
		} else if (entity.isInstanceOf(IdentifiableMediaEntity.class)) {
			IdentifiableMediaEntity<?> ime = CdmBase.deproxy(entity, IdentifiableMediaEntity.class);
			ime.addMedia(figure);
			// toSave = ime;
		} else {
			String message = "Unsupported entity to attach media: %s";
			message = String.format(message, entity.getClass().getName());
			// toSave = null;
		}
		save(entity, state);
	}

	private Media handleFigure(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		// FigureDataHolder result = new FigureDataHolder();

		Map<String, Attribute> attributes = getAttributes(parentEvent);
		String id = getAndRemoveAttributeValue(attributes, ID);
		String type = getAndRemoveAttributeValue(attributes, TYPE);
		String urlAttr = getAndRemoveAttributeValue(attributes, URL);
		checkNoAttributes(attributes, parentEvent);

		String urlString = null;
		String legendString = null;
		String titleString = null;
		String numString = null;
		String text = null;
		if (isNotBlank(urlAttr)){
			urlString = CdmUtils.Nz(state.getBaseMediaUrl()) + urlAttr;
		}
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				if (isNotBlank(text)){
					fireWarningEvent("Text not yet handled for figures: " + text, next, 4);
				}
				Media media = makeFigure(state, id, type, urlString, legendString, titleString, numString, next);
				return media;
			} else if (isStartingElement(next, FIGURE_LEGEND)) {
				// TODO same as figure string ?
				legendString = handleFootnoteString(state, reader, next);
			} else if (isStartingElement(next, FIGURE_TITLE)) {
				titleString = getCData(state, reader, next);
			} else if (isStartingElement(next, URL)) {
				String localUrl = getCData(state, reader, next);
				String url = CdmUtils.Nz(state.getBaseMediaUrl()) + localUrl;
				if (isBlank(urlString)){
					urlString = url;
				}
				if (! url.equals(urlString)){
					String message = "URL attribute and URL element differ. Attribute: %s, Element: %s";
					fireWarningEvent(String.format(message, urlString, url), next, 2);
				}
			} else if (isStartingElement(next, NUM)) {
				numString = getCData(state, reader, next);
			} else if (next.isCharacters()) {
				text += CdmUtils.concat("", text, next.asCharacters().getData());
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
	private Media makeFigure(MarkupImportState state, String id, String type, String urlString, 
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
				//TODO
			} else {
				String message = "Unknown figure type '%s'";
				message = String.format(message, type);
				fireWarningEvent(message, next, 2);
			}
			media = docImport.getImageMedia(urlString, docImport.getReadMediaData(), isFigure);
			
			if (media != null){
				// title
				if (StringUtils.isNotBlank(titleString)) {
					media.putTitle(getDefaultLanguage(state), titleString);
				}
				// legend
				if (StringUtils.isNotBlank(legendString)) {
					media.putDescription(getDefaultLanguage(state), legendString);
				}
				if (StringUtils.isNotBlank(numString)) {
					// TODO use concrete source (e.g. DAPHNIPHYLLACEAE in FM
					// vol.13)
					Reference<?> citation = state.getConfig().getSourceReference();
					media.addImportSource(  numString, "num", citation, null);
					// TODO name used in source if available
				}
				// TODO which citation
				if (StringUtils.isNotBlank(id)) {
					media.addImportSource(id, null, state.getConfig().getSourceReference(), null);
				} else {
					String message = "Figure id should never be empty or null";
					fireWarningEvent(message, next, 6);
				}

				// text
				// do nothing
				registerGivenFigure(state, next, id, media);
				
			}else{
				String message = "No media found: ";
				fireWarningEvent(message, next, 4);
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

		return media;
	}

	private FigureDataHolder handleFigureRef(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent)
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
					text += specimenImport.handleInLineGathering(state, reader, next);
				} else if (isStartingElement(next, REFERENCES)) {
					text += " " + handleInLineReferences(state, reader, next)+ " ";
				} else if (isStartingElement(next, BR)) {
					text += "<br/>";
					isTextMode = false;
				} else if (isStartingElement(next, NOMENCLATURE)) {
					handleNotYetImplementedElement(next);
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
		Reference<?> reference = nomenclatureImport.handleReference(state, reader, parentEvent);
		String result = "<cdm:ref uuid='%s'>%s</ref>";
		result = String.format(result, reference.getUuid(), reference.getTitleCache());
		save(reference, state);
		return result;
	}

	private void handleFeature(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent) throws XMLStreamException {
		Map<String, Attribute> attrs = getAttributes(parentEvent);
		Boolean isFreetext = getAndRemoveBooleanAttributeValue(parentEvent, attrs, IS_FREETEXT, false);
		String classValue =getAndRemoveRequiredAttributeValue(parentEvent, attrs, CLASS);
		checkNoAttributes(attrs, parentEvent);
		
		
		Feature feature = makeFeature(classValue, state, parentEvent, null);
		Taxon taxon = state.getCurrentTaxon();
		TaxonDescription taxonDescription = getTaxonDescription(taxon, state.getConfig().getSourceReference(), NO_IMAGE_GALLERY, CREATE_NEW);
		// TextData figureHolderTextData = null; //for use with one TextData for
		// all figure only

		boolean isDescription = feature.equals(Feature.DESCRIPTION());
		DescriptionElementBase lastDescriptionElement = null;
		
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				state.putFeatureToGeneralSorterList(feature);
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
				List<TextData> textDataList = handleChar(state, reader, next, null);
				for (TextData textData : textDataList){
					taxonDescription.addElement(textData);
				}
			} else if (isStartingElement(next, STRING)) {
				lastDescriptionElement = makeFeatureString(state, reader,feature, taxonDescription, lastDescriptionElement,next, isFreetext);
			} else if (isStartingElement(next, FIGURE_REF)) {
				lastDescriptionElement = makeFeatureFigureRef(state, reader, taxonDescription, isDescription, lastDescriptionElement, next);
			} else if (isStartingElement(next, REFERENCES)) {
				// TODO details/microcitation ??

				List<Reference<?>> refs = handleReferences(state, reader, next);
				if (!refs.isEmpty()) {
					// TODO
					Reference<?> descriptionRef = state.getConfig().getSourceReference();
					TaxonDescription description = getTaxonDescription(taxon, descriptionRef, false, true);
					TextData featurePlaceholder = docImport.getFeaturePlaceholder(state, description, feature, true);
					for (Reference<?> citation : refs) {
						featurePlaceholder.addSource(OriginalSourceType.PrimaryTaxonomicSource,
								null, null, citation, null);
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
					boolean isDescription, DescriptionElementBase lastDescriptionElement, XMLEvent next) throws XMLStreamException {
		FigureDataHolder figureHolder = handleFigureRef(state, reader, next);
		Feature figureFeature = getFeature(state, MarkupTransformer.uuidFigures, "Figures", "Figures", "Fig.",null);
		if (isDescription) {
			TextData figureHolderTextData = null;
			// if (figureHolderTextData == null){
			figureHolderTextData = TextData.NewInstance(figureFeature);
			if (StringUtils.isNotBlank(figureHolder.num)) {
				String annotationText = "<num>" + figureHolder.num.trim() + "</num>";
				Annotation annotation = Annotation.NewInstance(annotationText, AnnotationType.TECHNICAL(), getDefaultLanguage(state));
				figureHolderTextData.addAnnotation(annotation);
			}
			if (StringUtils.isNotBlank(figureHolder.figurePart)) {
				String annotationText = "<figurePart>"+ figureHolder.figurePart.trim() + "</figurePart>";
				Annotation annotation = Annotation.NewInstance(annotationText,AnnotationType.EDITORIAL(), getDefaultLanguage(state));
				figureHolderTextData.addAnnotation(annotation);
			}
			// if (StringUtils.isNotBlank(figureText)){
			// figureHolderTextData.putText(language, figureText);
			// }
			taxonDescription.addElement(figureHolderTextData);
			// }
			registerFigureDemand(state, next, figureHolderTextData, figureHolder.ref);
		} else {
			if (lastDescriptionElement == null) {
				String message = "No description element created yet that can be referred by figure. Create new TextData instead";
				fireWarningEvent(message, next, 4);
				lastDescriptionElement = TextData.NewInstance(figureFeature);
				taxonDescription.addElement(lastDescriptionElement);
			}
			registerFigureDemand(state, next, lastDescriptionElement,	figureHolder.ref);
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
				TaxonDescription taxonDescription, DescriptionElementBase lastDescriptionElement, XMLEvent next, Boolean isFreetext) throws XMLStreamException {
		
		//for specimen only
		if (feature.equals(Feature.SPECIMEN()) || feature.equals(Feature.MATERIALS_EXAMINED())){
			
			List<DescriptionElementBase> specimens = specimenImport.handleMaterialsExamined(state, reader, next, feature);
			for (DescriptionElementBase specimen : specimens){
				taxonDescription.addElement(specimen);
				lastDescriptionElement = specimen;
			}
			state.setCurrentCollector(null);
			
			return lastDescriptionElement;
		}else{
		
			//others
			Map<String, String> subheadingMap = handleString(state, reader, next, feature);
			for (String subheading : subheadingMap.keySet()) {
				Feature subheadingFeature = feature;
				if (StringUtils.isNotBlank(subheading) && subheadingMap.size() > 1) {
					subheadingFeature = makeFeature(subheading, state, next, null);
				}
				if (feature.equals(Feature.COMMON_NAME()) && (isFreetext == null || !isFreetext)){
					List<DescriptionElementBase> commonNames = makeVernacular(state, subheading, subheadingMap.get(subheading));
					for (DescriptionElementBase commonName : commonNames){
						taxonDescription.addElement(commonName);
						lastDescriptionElement = commonName;
					}
				}else {
					TextData textData = TextData.NewInstance(subheadingFeature);
					textData.putText(getDefaultLanguage(state), subheadingMap.get(subheading));
					taxonDescription.addElement(textData);
					lastDescriptionElement = textData;
					// TODO how to handle figures when these data are split in
					// subheadings
				}
			}
			return lastDescriptionElement;
		}
	}

	private List<DescriptionElementBase> makeVernacular(MarkupImportState state, String subheading, String commonNameString) throws XMLStreamException {
		List<DescriptionElementBase> result = new ArrayList<DescriptionElementBase>();
		String[] splits = commonNameString.split(",");
		for (String split : splits){
			split = split.trim();
			if (! split.matches(".*\\(.*\\)\\.?")){
				fireWarningEvent("Common name string '"+split+"' does not match given pattern", state.getReader().peek(), 4);
			}
			
			String name = split.replaceAll("\\(.*\\)", "").replace(".", "").trim();
			String languageStr = split.replaceFirst(".*\\(", "").replaceAll("\\)\\.?", "").trim();
			
			Language language = null;
			if (StringUtils.isNotBlank(languageStr)){
				try {
					UUID langUuid = state.getTransformer().getLanguageUuid(languageStr);
					TermVocabulary<?> voc = null;
					language = getLanguage(state, langUuid, languageStr, languageStr, null, voc);
					if (language == null){
						logger.warn("Language " + languageStr + " not recognized by transformer");
					}
				} catch (UndefinedTransformerMethodException e) {
					throw new RuntimeException(e);
				}
			}
			NamedArea area = null;
			CommonTaxonName commonTaxonName = CommonTaxonName.NewInstance(name, language, area);
			result.add(commonTaxonName);
		}
		
		return result;
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
			TextData featurePlaceholder = docImport.getFeaturePlaceholder(state,
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
					Reference<?> ref = nomenclatureImport.handleReference(state, reader, next);
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
				habitat.putText(getDefaultLanguage(state), text);
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
								String message = "The presence/absence status '%s' could not be transformed to an CDM status";								
								fireWarningEvent(String.format(message, statusValue), next, 4);
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
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("<DistributionLocality> has no closing tag");
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
				if (! isTextMode) {
					String message = "String is not in text mode";
					fireWarningEvent(message, next, 6);
				} else {
					text += next.asCharacters().getData();
				}
			} else if (isStartingElement(next, HEADING)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, VERNACULAR_NAMES)) {
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
				//TODO  //TODO test handleSimpleAnnotation
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
			"ol", "ul", "li", "i", "b", "table", "br","tr","td");

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

	/**
	 * Handle the char or subchar element. As 
	 * @param state the import state
	 * @param reader 
	 * @param parentEvent
	 * @param parentFeature in case of subchars we need to attache the newly created feature to a parent feature, should be <code>null</code>
	 * for top level chars.  
	 * @return List of TextData. Not a single one as the recursive TextData will also be returned
	 * @throws XMLStreamException
	 */
	private List<TextData> handleChar(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, Feature parentFeature) throws XMLStreamException {
		List<TextData> result = new ArrayList<TextData>();
		String classValue = getClassOnlyAttribute(parentEvent);
		Feature feature = makeFeature(classValue, state, parentEvent, parentFeature);

		boolean isTextMode = true;
		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				state.putFeatureToCharSorterList(feature);
				TextData textData = TextData.NewInstance(feature);
				textData.putText(getDefaultLanguage(state), text);
				result.add(textData);
				return result;
			} else if (isStartingElement(next, FIGURE_REF)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, FOOTNOTE_REF)) {
				//TODO
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, BR)) {
				text += "<br/>";
				isTextMode = false;
			} else if (isEndingElement(next, BR)) {
				isTextMode = true;
			} else if (isHtml(next)) {
				text += getXmlTag(next);
			} else if (next.isStartElement()) {
				if (isStartingElement(next, ANNOTATION)) {
					handleNotYetImplementedElement(next); //TODO test handleSimpleAnnotation
				} else if (isStartingElement(next, ITALICS)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, BOLD)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, FIGURE)) {
					handleFigure(state, reader, next);
				} else if (isStartingElement(next, SUB_CHAR)) {
					List<TextData> textData = handleChar(state, reader, next, feature);
					result.addAll(textData);
				} else if (isStartingElement(next, FOOTNOTE)) {
					FootnoteDataHolder footnote = handleFootnote(state, reader,	next);
					if (footnote.isRef()) {
						String message = "Ref footnote not implemented here";
						fireWarningEvent(message, next, 4);
					} else {
						registerGivenFootnote(state, footnote);
					}
				} else {
					handleUnexpectedStartElement(next.asStartElement());
				}
			} else if (next.isCharacters()) {
				if (!isTextMode) {
					String message = "String is not in text mode";
					fireWarningEvent(message, next, 6);
				} else {
					text += next.asCharacters().getData();
				}
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
	 * @param parentFeature 
	 * @return
	 * @throws UndefinedTransformerMethodException
	 */
	private Feature makeFeature(String classValue, MarkupImportState state, XMLEvent parentEvent, Feature parentFeature) {
		UUID uuid;
		try {
			String featureText = StringUtils.capitalize(classValue);
			if (parentFeature != null){
				featureText = "<%s>" + featureText;
				featureText = String.format(featureText, parentFeature.getTitleCache());
				classValue = "<%s>" + classValue;
				classValue = String.format(classValue, parentFeature.getTitleCache());
			}

			
			Feature feature = state.getTransformer().getFeatureByKey(classValue);
			if (feature != null) {
				return feature;
			}
			uuid = state.getTransformer().getFeatureUuid(classValue);
			
			if (uuid == null){
				uuid = state.getUnknownFeatureUuid(classValue);
			}
			
			if (uuid == null) {
				// TODO
				String message = "Uuid is not defined for '%s'";
				message = String.format(message, classValue);
				fireWarningEvent(message, parentEvent, 8);
				uuid = UUID.randomUUID();
				state.putUnknownFeatureUuid(classValue, uuid);
			}

			// TODO eFlora vocabulary
			TermVocabulary<Feature> voc = null;
			feature = getFeature(state, uuid, featureText, featureText, classValue, voc);
			if (parentFeature != null){
				parentFeature.addIncludes(feature);
				save(parentFeature, state);
			}
			save(feature, state);
					
			if (feature == null) {
				throw new NullPointerException(classValue + " not recognized as a feature");
			}
//			state.putFeatureToCurrentList(feature);
			return feature;
		} catch (Exception e) {
			String message = "Could not create feature for %s: %s";
			message = String.format(message, classValue, e.getMessage());
			fireWarningEvent(message, parentEvent, 4);
			state.putUnknownFeatureUuid(classValue, null);
//			e.printStackTrace();
			return Feature.UNKNOWN();
		}
	}

}
