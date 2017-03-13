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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;


/**
 * @author a.mueller
 *
 */
public class MarkupDocumentImportNoComponent extends MarkupImportBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MarkupDocumentImportNoComponent.class);

	private final MarkupKeyImport keyImport;

	private final MarkupModsImport modsImport;
	private final MarkupSpecimenImport specimenImport;
	private final MarkupNomenclatureImport nomenclatureImport;

	public MarkupDocumentImportNoComponent(MarkupDocumentImport docImport) {
		super(docImport);
		this.keyImport = new MarkupKeyImport(docImport);
		this.specimenImport = new MarkupSpecimenImport(docImport);
		this.nomenclatureImport = new MarkupNomenclatureImport(docImport, specimenImport);
		this.modsImport = new MarkupModsImport(docImport);
		this.featureImport = new MarkupFeatureImport(docImport, specimenImport, nomenclatureImport, keyImport);
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
			if (isEndingElement(event, elName)) {
				return;
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
				modsImport.handleMods(state, reader, next);
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
					String message = "The following %d key leads are unmatched: %s";
					message = String.format(message, unmatched.size(), state.getUnmatchedLeads().toString());
					fireWarningEvent(message, next, 6);
				}
//				save(keyNodesToSave, state);

				return;
			} else if (isStartingElement(next, TAXON)) {
				state.setCurrentTaxonExcluded(false);
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
	private TaxonNode doTaxonRelation(MarkupImportState state, Taxon taxon, Taxon lastTaxon, Location dataLocation) {

		if (state.isTaxonInClassification() == false){
			return null;
		}

		boolean excluded = state.isCurrentTaxonExcluded();
		TaxonNode node;
		Classification tree = makeTree(state, dataLocation);
		if (lastTaxon == null) {
			node = tree.addChildTaxon(taxon, null, null);
			node.setExcluded(excluded);
			return node;
		}
		Rank thisRank = taxon.getName().getRank();
		Rank lastRank = lastTaxon.getName().getRank();
		if (lastRank == null){
			String message = "Last rank was null. Can't create tree correctly";
			fireWarningEvent(message, makeLocationStr(dataLocation), 12);
		}
		if (!lastTaxon.getTaxonNodes().isEmpty()) {
			TaxonNode lastNode = lastTaxon.getTaxonNodes().iterator().next();
			if (thisRank == null){
			    String message = "Rank is undefined for taxon '%s'. Can't create classification without rank.";
				message = String.format(message, taxon.getName().getTitleCache());
				fireWarningEvent(message, makeLocationStr(dataLocation), 6);
				node = null;
			}else if (thisRank.isLower(lastRank)) {
			    node = null;
	            node = lastNode.addChildTaxon(taxon, null, null);
				fillMissingEpithetsForTaxa(lastTaxon, taxon);
			} else if (thisRank.equals(lastRank)) {
			    TaxonNode parent = lastNode.getParent();
				if (parent != null && parent.getTaxon() != null) {
					node = parent.addChildTaxon(taxon, null, null);
					fillMissingEpithetsForTaxa(parent.getTaxon(), taxon);
				} else {
					node = tree.addChildTaxon(taxon, null, null);
				}
			} else if (thisRank.isHigher(lastRank)) {
			    TaxonNode parent = lastNode.getParent();
				if (parent != null){
					node = doTaxonRelation(state, taxon, parent.getTaxon(), dataLocation);
				}else{
					String warning = "No parent available for lastNode. Classification can not be build correctly. Maybe the rank was missing for the lastNode";
					fireWarningEvent(warning, makeLocationStr(dataLocation), 16);
					//TODO what to do in this case (haven't spend time to think about yet
					node = null;
				}

				// TaxonNode parentNode = handleTaxonRelation(state, taxon,
				// lastNode.getParent().getTaxon());
				// parentNode.addChildTaxon(taxon, null, null, null);
			}else{
			    fireWarningEvent("Unhandled case", makeLocationStr(dataLocation), 8);
			    node = null;
			}
		} else {
			String message = "Last taxon has no node";
			fireWarningEvent(message, makeLocationStr(dataLocation), 6);
			node = null;
		}
		if (excluded){
		    if (node != null){
		        node.setExcluded(excluded);
		    }else{
		        fireWarningEvent("Taxon is excluded but no taxon node can be created", makeLocationStr(dataLocation), 4);
		    }
		}
		return node;
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
		Taxon taxon = createTaxonAndName(state, attributes, parentEvent);
		state.setCurrentTaxon(taxon);
		state.addNewFeatureSorterLists(taxon.getUuid().toString());

		boolean hasTitle = false;
		boolean hasNomenclature = false;
		String taxonTitle = null;

		Reference sourceReference = state.getConfig().getSourceReference();
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
//				checkMandatoryElement(hasTitle, parentEvent, TAXONTITLE);
				checkMandatoryElement(hasNomenclature, parentEvent,	NOMENCLATURE);
				boolean inClassification = getAndRemoveBooleanAttributeValue(next, attributes, "inClassification", true);
				state.setTaxonInClassification(inClassification);
				handleUnexpectedAttributes(parentEvent.getLocation(),attributes);
				if (taxon.getName().getRank() == null){
					String warning = "No rank exists for taxon " + taxon.getTitleCache();
					fireWarningEvent(warning, next, 12);
					taxon.getName().setRank(Rank.UNKNOWN_RANK());
				}
				//hybrid
				if (state.isTaxonIsHybrid() && !taxon.getName().isHybrid()){
				    fireWarningEvent("Taxon is hybrid but name is not a hybrid name", next, 4);
				}
				state.setTaxonIsHybrid(false);

				keyImport.makeKeyNodes(state, parentEvent, taxonTitle);
				state.setCurrentTaxon(null);
				state.setCurrentTaxonNum(null);
				if (taxon.getName().getRank().isHigher(Rank.GENUS())){
					state.setLatestGenusEpithet(null);
				}else{
					state.setLatestGenusEpithet(taxon.getName().getGenusOrUninomial());
				}
				save(taxon, state);
				return taxon;
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
					featureImport.handleFeature(state, reader, next);
				} else if (isStartingElement(next, NOTES)) {
					// TODO is this the correct way to handle notes?
					String note = handleNotes(state, reader, next);

					UUID notesUuid;
					try {
						notesUuid = state.getTransformer().getFeatureUuid("notes");
						Feature feature = getFeature(state, notesUuid, "Notes",	"Notes", "note", null);
						TextData textData = TextData.NewInstance(feature);
						textData.addPrimaryTaxonomicSource(sourceReference);
						textData.putText(getDefaultLanguage(state), note);
						TaxonDescription description = getDefaultTaxonDescription(taxon, false, true, sourceReference);
						description.addElement(textData);
					} catch (UndefinedTransformerMethodException e) {
						String message = "getFeatureUuid method not yet implemented";
						fireWarningEvent(message, next, 8);
					}
				} else if (isStartingElement(next, REFERENCES)) {
					handleNotYetImplementedElement(next);
				} else if (isStartingElement(next, FIGURE_REF)) {
					TaxonDescription desc = getTaxonDescription(taxon, sourceReference, IMAGE_GALLERY, CREATE_NEW);
					TextData textData;
					if (desc.getElements().isEmpty()){
						textData = TextData.NewInstance(Feature.IMAGE());
						textData.addPrimaryTaxonomicSource(sourceReference);
						desc.addElement(textData);
					}
					textData = (TextData)desc.getElements().iterator().next();
					featureImport.makeFeatureFigureRef(state, reader, desc, false, textData, sourceReference, next);
				} else if (isStartingElement(next, FIGURE)) {
					handleFigure(state, reader, next, specimenImport, nomenclatureImport);
				} else if (isStartingElement(next, FOOTNOTE)) {
					FootnoteDataHolder footnote = handleFootnote(state, reader,	next, specimenImport, nomenclatureImport);
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
		if (state.getConfig().isHandleWriterManually()){
		    fireWarningEvent("<Writer> is expected to be handled manually", next, 1);
		}else{
		    taxon.addExtension(writer.extension);
	        // TODO what if taxonTitle comes later
	        taxonTitle = taxonTitle != null ? taxonTitle : taxon.getName() == null ? null : taxon.getName().getNameCache();
	        if (writer.extension != null) {
	            if (StringUtils.isBlank(taxonTitle)){
	                fireWarningEvent("No taxon title defined for writer. Please add sec.title manually.", next, 6);
	                taxonTitle = null;
	            }
	            Reference sec = ReferenceFactory.newBookSection();
	            sec.setTitle(taxonTitle);
	            TeamOrPersonBase<?> author = createAuthor(state, writer.writer);
	            sec.setAuthorship(author);
	            sec.setInReference(state.getConfig().getSourceReference());
	            taxon.setSec(sec);
	            registerFootnotes(state, sec, writer.footnotes);
	        } else {
	            String message = "There is no writer extension defined";
	            fireWarningEvent(message, next, 6);
	        }
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
		Map<String, SubheadingResult> stringMap = handleString(state, reader,	next, null);
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
	 * @param event
	 */
	private Taxon createTaxonAndName(MarkupImportState state,
			Map<String, Attribute> attributes, StartElement event) {
		INonViralName name;
		Rank rank = null;  //Rank.SPECIES(); // default
		boolean isCultivar = checkAndRemoveAttributeValue(attributes, CLASS, "cultivated");

		if (isCultivar) {
			name = TaxonNameFactory.NewCultivarInstance(rank);
		} else {
			name = createNameByCode(state, rank);
		}
		Taxon taxon = Taxon.NewInstance(name, state.getConfig().getSourceReference());
		if (checkAndRemoveAttributeValue(attributes, CLASS, "dubious")) {
			taxon.setDoubtful(true);
		} else if (checkAndRemoveAttributeValue(attributes, CLASS, "excluded")) {
			state.setCurrentTaxonExcluded(true);
		}
        state.setTaxonIsHybrid(checkAndRemoveAttributeValue(attributes, CLASS, "hybrid"));

		// TODO insufficient, new, expected
		handleNotYetImplementedAttribute(attributes, CLASS, event);
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
					if (state.getConfig().isDoExtensionForTaxonTitle() && checkMandatoryText(text, parentEvent)) {
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


}
