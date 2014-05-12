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
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NameTypeParser;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @created 30.05.2012
 * 
 */
public class MarkupNomenclatureImport extends MarkupImportBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MarkupNomenclatureImport.class);


//	private NonViralNameParserImpl parser = new NonViralNameParserImpl();

	private MarkupSpecimenImport specimenImport;

	public MarkupNomenclatureImport(MarkupDocumentImport docImport, MarkupSpecimenImport specimenImport) {
		super(docImport);
		this.specimenImport = specimenImport;
	}

	public void handleNomenclature(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent)
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

	private void handleHomotypes(MarkupImportState state,
			XMLEventReader reader, StartElement parentEvent)
			throws XMLStreamException {
		checkNoAttributes(parentEvent);

		HomotypicalGroup homotypicalGroup = null;

		boolean hasNom = false;
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				checkMandatoryElement(hasNom, parentEvent, NOM);
				state.setLatestAuthorInHomotype(null);
				return;
			} else if (isEndingElement(next, NAME_TYPE)) {
				state.setNameType(false);
			} else if (isStartingElement(next, NOM)) {
				NonViralName<?> name = handleNom(state, reader, next, homotypicalGroup);
				homotypicalGroup = name.getHomotypicalGroup();
				hasNom = true;
			} else if (isStartingElement(next, NAME_TYPE)) {
				state.setNameType(true);
				handleNameType(state, reader, next, homotypicalGroup);
			} else if (isStartingElement(next, SPECIMEN_TYPE)) {
				specimenImport.handleSpecimenType(state, reader, next, homotypicalGroup);
			} else if (isStartingElement(next, NOTES)) {
				handleNotYetImplementedElement(next);
			} else {
				handleUnexpectedElement(next);
			}
		}
		state.setLatestAuthorInHomotype(null);
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
	private NonViralName<?> handleNom(MarkupImportState state, XMLEventReader reader, 
			XMLEvent parentEvent, HomotypicalGroup homotypicalGroup) throws XMLStreamException {
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
		String text = "";
		
		boolean nameFilled = false;
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				// fill the name with all data gathered, if not yet done before
				if (nameFilled == false){
					fillName(state, nameMap, name, next);
				}
				handleNomText(state, parentEvent, text, isNameType);
				return name;
			} else if (isEndingElement(next, ANNOTATION)) {
				// NOT YET IMPLEMENTED //TODO test
				// handleSimpleAnnotation
				popUnimplemented(next.asEndElement());
			}else if (isStartingElement(next, FULL_NAME)) {
				handleFullName(state, reader, name, next);
			} else if (isStartingElement(next, NUM)) {
				handleNomNum(state, reader, next);
			} else if (isStartingElement(next, NAME)) {
				handleName(state, reader, next, nameMap);
			} else if (isStartingElement(next, CITATION)) {
				//we need to fill the name here to have nomenclatural author available for the following citations
				fillName(state, nameMap, name, next);
				nameFilled = true;
				handleCitation(state, reader, next, name, nameMap);
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else if (isStartingElement(next, HOMONYM)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, NOTES)) {
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, ANNOTATION)) {
				handleNotYetImplementedElement(next);
			} else {
				handleUnexpectedElement(next);
			}
		}
		// TODO handle missing end element
		throw new IllegalStateException("Nom has no closing tag");
	}

	/**
	 * Handles appearance of text within <nom> tags.
	 * Usually this is not expected except for some information that is already handled
	 * elsewhere, e.g. the string Nametype is holding information that is available already
	 * via the surrounding nametype tag. Therefore this information can be neglected.
	 * This method is open for upcoming cases which need to be handled. 
	 * @param state
	 * @param event
	 * @param text
	 * @param isNameType
	 */
	private void handleNomText(MarkupImportState state, XMLEvent event, String text, boolean isNameType) {
		if (isBlank(text)){
			return;
		}
		text = text.trim();
		//neglect known redundant strings
		if (isNameType && text.matches("(?i)^Esp[\u00E8\u00C8]ce[·\\-\\s]type\\:$")){
			return;
		}//neglect meaningless punctuation
		else if (isPunctuation(text)){
			return;	
		}else{
			String message = "Unhandled text in <nom> tag: \"%s\"";
			fireWarningEvent(String.format(message, text), event, 4);
		}
	}
	
	/**
	 * @param state
	 * @param reader
	 * @param next
	 * @throws XMLStreamException
	 */
	private void handleNomNum(MarkupImportState state, XMLEventReader reader,
			XMLEvent next) throws XMLStreamException {
		String num = getCData(state, reader, next);
		num = num.replace(".", "");
		num = num.replace(")", "");
		if (StringUtils.isNotBlank(num)) {
			if (state.getCurrentTaxonNum() != null
					&& !state.getCurrentTaxonNum().equals(num)) {
				String message = "Taxontitle num and homotypes/nom/num differ ( %s <-> %s ). I use the later one.";
				message = String.format(message,
						state.getCurrentTaxonNum(), num);
				fireWarningEvent(message, next, 4);
			}
			state.setCurrentTaxonNum(num);
		}
	}

	/**
	 * @param state
	 * @param reader
	 * @param name
	 * @param next
	 * @throws XMLStreamException
	 */
	private void handleFullName(MarkupImportState state, XMLEventReader reader,
			NonViralName<?> name, XMLEvent next) throws XMLStreamException {
		String fullNameStr;
		Map<String, Attribute> attrs = getAttributes(next);
		String rankStr = getAndRemoveRequiredAttributeValue(next,
				attrs, "rank");
		Rank rank = makeRank(state, rankStr, false);
		name.setRank(rank);
		if (rank == null) {
			String message = "Rank was computed as null. This must not be.";
			fireWarningEvent(message, next, 6);
			name.setRank(Rank.UNKNOWN_RANK());
		}
		if (!attrs.isEmpty()) {
			handleUnexpectedAttributes(next.getLocation(), attrs);
		}
		fullNameStr = getCData(state, reader, next);
		name.setTitleCache(fullNameStr, true);
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
			} else if (isStartingElement(next, ANNOTATION)) {
				handleNotYetImplementedElement(next); // TODO test handleSimpleAnnotation
			} else if (next.isCharacters()) {
				text += next.asCharacters().getData();
			} else {
				handleUnexpectedElement(next);
			}
		}
		throw new IllegalStateException("name has no closing tag");
	}

	private void fillName(MarkupImportState state, Map<String, String> nameMap,
			NonViralName<?> name, XMLEvent event) {

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

		if (!name.isProtectedTitleCache()) { // otherwise fullName

			makeRankDecision(state, nameMap, name, event, infrank);

			// test consistency of rank and authors
			testRankAuthorConsistency(name, event, authorStr, paraut,infrParAut, infrAut);

			// authors
			makeNomenclaturalAuthors(state, event, name, authorStr, paraut, infrParAut, infrAut);
		}

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
				NomenclaturalStatusType nomStatusType = NomenclaturalStatusType.getNomenclaturalStatusTypeByAbbreviation(statusStr, name);
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
					if ((key.equalsIgnoreCase(GENUS_ABBREVIATION)
							&& isNotBlank(state.getLatestGenusEpithet()) || isGenusAbbrev(
								value, state.getLatestGenusEpithet()))) {
						value = state.getLatestGenusEpithet();
					}
					name.setGenusOrUninomial(toFirstCapital(value));
				} else if (rank.isInfraGeneric()) {
					name.setInfraGenericEpithet(toFirstCapital(value));
				} else if (rank.isSpecies()) {
					if (state.getConfig().isAllowCapitalSpeciesEpithet()
							&& isFirstCapitalWord(value)) { // capital letters
															// are allowed for
															// species epithet
															// in case of person
															// names (e.g.
															// Manilkara
															// Welwitschii Engl.
						name.setSpecificEpithet(value);
					} else {
						name.setSpecificEpithet(value.toLowerCase());
					}
				} else if (rank.isInfraSpecific()) {
					name.setInfraSpecificEpithet(value.toLowerCase());
				} else {
					String message = "Invalid rank '%s'. Can't decide which epithet to fill with '%s'";
					message = String.format(message, rank.getTitleCache(),
							value);
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
	 * @param state 
	 * @param name
	 * @param event
	 * @param authorStr
	 * @param paraut
	 * @param infrParAut
	 * @param infrAut
	 */
	private void makeNomenclaturalAuthors(MarkupImportState state, XMLEvent event, NonViralName<?> name, 
			String authorStr, String paraut, String infrParAut, String infrAut) {
		if (name.getRank() != null && name.getRank().isInfraSpecific()) {
			if (StringUtils.isNotBlank(infrAut)) {
				INomenclaturalAuthor[] authorAndEx = authorAndEx(infrAut, event);
				name.setCombinationAuthorTeam(authorAndEx[0]);
				name.setExCombinationAuthorTeam(authorAndEx[1]);
			}
			if (StringUtils.isNotBlank(infrParAut)) {
				INomenclaturalAuthor[] authorAndEx = authorAndEx(infrParAut,event);
				name.setBasionymAuthorTeam(authorAndEx[0]);
				name.setExBasionymAuthorTeam(authorAndEx[1]);
			}
		} else {
			if (name.getRank() == null) {
				String message = "No rank defined. Check correct usage of authors!";
				fireWarningEvent(message, event, 4);
				if (isNotBlank(infrParAut) || isNotBlank(infrAut)) {
					authorStr = infrAut;
					paraut = infrParAut;
				}
			}
			if (StringUtils.isNotBlank(authorStr)) {
				INomenclaturalAuthor[] authorAndEx = authorAndEx(authorStr,	event);
				name.setCombinationAuthorTeam(authorAndEx[0]);
				name.setExCombinationAuthorTeam(authorAndEx[1]);
			}
			if (StringUtils.isNotBlank(paraut)) {
				INomenclaturalAuthor[] authorAndEx = authorAndEx(paraut, event);
				name.setBasionymAuthorTeam(authorAndEx[0]);
				name.setExBasionymAuthorTeam(authorAndEx[1]);
			}
		}
		
		//remember author for following citations
		state.setLatestAuthorInHomotype((TeamOrPersonBase<?>)name.getCombinationAuthorTeam());
	}

	private TeamOrPersonBase<?>[] authorAndEx(String authorAndEx, XMLEvent xmlEvent) {
		authorAndEx = authorAndEx.trim();
		TeamOrPersonBase<?>[] result = new TeamOrPersonBase[2];

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
			SynonymRelationshipType synonymType = SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
			if (taxon.getHomotypicGroup().equals(homotypicalGroup)) {
				synonymType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
			}
			taxon.addSynonymName(name, synonymType);
		} else {
			name = CdmBase.deproxy(taxon.getName(), NonViralName.class);
		}
		return name;
	}

	private void handleCitation(MarkupImportState state, XMLEventReader reader,
			XMLEvent parentEvent, NonViralName<?> name, Map<String, String> nameMap) throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);

		state.setCitation(true);
		boolean hasRefPart = false;
		Map<String, String> refMap = new HashMap<String, String>();
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				checkMandatoryElement(hasRefPart, parentEvent.asStartElement(), REF_PART);
				Reference<?> reference = createReference(state, refMap, next);
				String microReference = refMap.get(DETAILS);
				doCitation(state, name, classValue, reference, microReference, parentEvent);
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

	private void handleRefPart(MarkupImportState state, XMLEventReader reader,
			XMLEvent parentEvent, Map<String, String> refMap)
			throws XMLStreamException {
		String classValue = getClassOnlyAttribute(parentEvent);

		String text = "";
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				refMap.put(classValue, text);
				return;
			} else if (next.isStartElement()) {
				if (isStartingElement(next, ANNOTATION)) {
					handleNotYetImplementedElement(next); // TODO test
															// handleSimpleAnnotation
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

	private void doCitation(MarkupImportState state, NonViralName<?> name,
			String classValue, Reference<?> reference, String microCitation,
			XMLEvent parentEvent) {
		if (PUBLICATION.equalsIgnoreCase(classValue)) {
			name.setNomenclaturalReference(reference);
			name.setNomenclaturalMicroReference(microCitation);
		} else if (USAGE.equalsIgnoreCase(classValue)) {
			Taxon taxon = state.getCurrentTaxon();
			TaxonDescription td = getTaxonDescription(taxon, state.getConfig().getSourceReference(), false, true);
			TextData citation = TextData.NewInstance(Feature.CITATION());
			// TODO name used in source
			citation.addSource(OriginalSourceType.PrimaryTaxonomicSource, null, null, reference, microCitation);
			td.addElement(citation);
		} else if (TYPE.equalsIgnoreCase(classValue)) {
			handleNotYetImplementedAttributeValue(parentEvent, CLASS, classValue);
		} else {
			// TODO Not yet implemented
			handleNotYetImplementedAttributeValue(parentEvent, CLASS, classValue);
		}
	}

	/**
	 * Tests if the names rank is consistent with the given author strings.
	 * NOTE: Tags for authors are differ depending on the rank.
	 * 
	 * @param name
	 * @param event
	 * @param authorStr
	 * @param paraut
	 * @param infrParAut
	 * @param infrAut
	 */
	private void testRankAuthorConsistency(NonViralName<?> name, XMLEvent event,
			String authorStr, String paraut, String infrParAut, String infrAut) {
		if (name.getRank() == null) {
			return;
		}
		if (name.getRank().isInfraSpecific()) {
			if (StringUtils.isBlank(infrParAut)
					&& StringUtils.isBlank(infrAut) // was isNotBlank before
													// 29.5.2012
					&& (StringUtils.isNotBlank(paraut) || StringUtils
							.isNotBlank(authorStr)) && !name.isAutonym()) {
				String message = "Rank is infraspecicific but has only specific or higher author(s)";
				fireWarningEvent(message, event, 4);
			}
		} else {
			// is not infraspecific
			if (StringUtils.isNotBlank(infrParAut)
					|| StringUtils.isNotBlank(infrAut)) {
				String message = "Rank is not infraspecicific but name has infra author(s)";
				fireWarningEvent(message, event, 4);
			}
		}
	}

	private Reference<?> createReference(MarkupImportState state,
			Map<String, String> refMap, XMLEvent parentEvent) {
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
			reference = handleCitationSpecific(state, type, authorStr,
					titleStr, titleCache, volume, edition, editors, pubName, pages, refMap, parentEvent);

		} else { // no citation
			reference = handleNonCitationSpecific(type, authorStr, titleStr,
					titleCache, volume, edition, editors, pubName);
		}

		//year
		TimePeriod timeperiod = TimePeriodParser.parseString(year);
		if (reference.getType().equals(ReferenceType.BookSection)){
			reference.getInBook().setDatePublished(timeperiod);
		}
		reference.setDatePublished(timeperiod);
		
		// TODO
		String[] unhandledList = new String[] { ALTERNATEPUBTITLE, ISSUE, NOTES, STATUS };
		for (String unhandled : unhandledList) {
			String value = getAndRemoveMapKey(refMap, unhandled);
			if (isNotBlank(value)) {
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

	
	/**
	 * Handles references used in the citation tag
	 * @see #handleNonCitationSpecific(String, String, String, String, String, String, String, String)
	 */
	private Reference<?> handleCitationSpecific(MarkupImportState state,
			String type, String authorStr, String titleStr, String titleCache,
			String volume, String edition, String editors, String pubName, String pages, Map<String, String> refMap, XMLEvent parentEvent) {
		
		if (titleStr != null){
			String message = "Currently it is not expected that a titleStr exists in a citation";
			fireWarningEvent(message, parentEvent, 4);
		}

		RefType refType = defineRefTypeForCitation(type, volume, editors, authorStr, pubName, parentEvent);
		Reference<?> reference;
		if (refType == RefType.Article) {
			IArticle article = ReferenceFactory.newArticle();
			if (pubName != null) {
				IJournal journal = ReferenceFactory.newJournal();
				journal.setTitle(pubName);
				article.setInJournal(journal);
				article.setVolume(volume);
				if (isNotBlank(edition)){
					String message = "Article must not have an edition.";
					fireWarningEvent(message, parentEvent, 4);
				}
			}
			reference = (Reference<?>) article;
		} else if (refType == RefType.BookSection) {
			//Book Section
			reference = ReferenceFactory.newBookSection();
			IBook  book = ReferenceFactory.newBook();
			reference.setInBook(book);
			book.setTitle(pubName);
			book.setVolume(volume);
			book.setEdition(edition);
			
			if (state.getConfig().isUseEditorAsInAuthorWhereNeeded()){
				TeamOrPersonBase<?> inAuthor = createAuthor(editors);
				book.setAuthorTeam(inAuthor);
				editors = null;
			}
		} else if (refType == RefType.Book){
			//Book
			reference = ReferenceFactory.newBook();
			reference.setTitle(pubName);
			reference.setVolume(volume);
			reference.setEdition(edition);
		}else if (refType == RefType.Generic){
			//Generic - undefinable
//			String message = "Can't define the type of the reference. Use generic instead";
//			fireWarningEvent(message, parentEvent, 4);
			reference = ReferenceFactory.newGeneric();
			reference.setTitle(pubName);
			reference.setEdition(edition);
			
			//volume indicates an in-reference
			if (isNotBlank(volume)){
				Reference<?> partOf = ReferenceFactory.newGeneric();
				partOf.setVolume(volume);
				partOf.setInReference(reference);
				reference = partOf;
			}
		}else if (refType == RefType.LatestUsed){
			Reference<?> latestReference = state.getLatestReferenceInHomotype();
			if (latestReference == null){
				String message = "No former reference available for incomplete citation";
				fireWarningEvent(message, parentEvent, 6);
				reference = ReferenceFactory.newGeneric();
			}else{
				if (latestReference.getInReference() != null){
					reference = (Reference<?>)latestReference.clone();
				}else{
					String message = "Latest reference is not an in-reference. This is not yet handled.";
					fireWarningEvent(message, parentEvent, 6);
					reference = ReferenceFactory.newGeneric();
				}
			}
			reference.setVolume(volume);
			if (isNotBlank(edition)){
				String message = "Edition not yet handled for incomplete citations";
				fireWarningEvent(message, parentEvent, 4);
			}
			
		}else{
			String message = "Unhandled reference type: %s" ;
			fireWarningEvent(String.format(message, refType.toString()), parentEvent, 8);
			reference = ReferenceFactory.newGeneric();
		}
		
		//author
		TeamOrPersonBase<?> author;
		if (isBlank(authorStr)){
			if (refType != RefType.LatestUsed){
				author = state.getLatestAuthorInHomotype();
				reference.setAuthorTeam(author);
			}
		}else{
			author = createAuthor(authorStr);
			state.setLatestAuthorInHomotype(author);
			reference.setAuthorTeam(author);
		}
		

		//title, titleCache
		handleTitlesInCitation(titleStr, titleCache, parentEvent, reference);

		//editors
		handleEditorsInCitation(edition, editors, reference, parentEvent);
		
		//pages
		handlePages(state, refMap, parentEvent, reference, pages);
		
		//remember reference for following citation
		state.setLatestReferenceInHomotype(reference);
		
		return reference;
	}

	private void handleEditorsInCitation(String edition, String editors, Reference<?> reference, XMLEvent parentEvent) {
		//editor
		reference.setEditor(editors);
		if ( editors != null){
			String message = "Citation reference has an editor. This is unusual for a citation reference (appears regularly in <reference> references";
			fireWarningEvent(message, parentEvent, 4);
		}
	}

	private void handleTitlesInCitation(String titleStr, String titleCache,
			XMLEvent parentEvent, Reference<?> reference) {
		if (isNotBlank(titleStr)){
			reference.setTitle(titleStr);
		}
		//titleCache
		if (StringUtils.isNotBlank(titleCache)) {
			reference.setTitleCache(titleCache, true);
		}
		if (titleStr != null || titleCache != null){
			String message = "Citation reference has a title or a full title. Both is unusual for a citation reference (appears regularly in <reference> references";
			fireWarningEvent(message, parentEvent, 4);
		}
	}

	private enum RefType{
		Article,
		BookSection,
		Book,
		Generic,
		LatestUsed
	}
	
	private RefType defineRefTypeForCitation(String type, String volume, String editors, 
			String authorStr, String pubName, XMLEvent parentEvent) {
		if ("journal".equalsIgnoreCase(type)){
			return RefType.Article;
		}else {
			if (editors == null){
				//no editors
				if (pubName == null){
					//looks like we need to use reference info from former citations here
					return RefType.LatestUsed;
				}else if (volume == null){
					return RefType.Book;  //Book must not have in-authors
				}else{
					return RefType.Generic;
				}

			}else{
				//editors
				if (pubName != null){
					return RefType.BookSection;
				}else{
					String message = "Unexpected state: Citation has editors but no pubName";
					fireWarningEvent(message, parentEvent, 4);
					return RefType.Generic;
				}
			}
		}
	}


	private boolean isArticle(String type, String volume, String editors) {
		if ("journal".equalsIgnoreCase(type)){
			return true;
		}else if (volume != null && editors == null){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * in work
	 * @return
	 */
	private Reference<?> handleNonCitationSpecific(String type, String authorStr,
			String titleStr, String titleCache, String volume, String edition,
			String editors, String pubName) {
		Reference<?> reference;
		if (isArticle(type, volume, editors)) {
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

		//title
		reference.setTitle(titleStr);
		if (StringUtils.isNotBlank(titleCache)) {
			reference.setTitleCache(titleCache, true);
		}
		
		//edition
		reference.setEdition(edition);
		reference.setEditor(editors);

		//pubName
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
		
		//volume
		reference.setVolume(volume);
		return reference;
	}

	private void handlePages(MarkupImportState state,
			Map<String, String> refMap, XMLEvent parentEvent,
			Reference<?> reference, String pages) {
		// TODO check if this is handled correctly in FM markup
		boolean switchPages = state.getConfig().isHandlePagesAsDetailWhereNeeded();
		if (switchPages){
			if (pages != null ){
				String detail = refMap.get(DETAILS);
				if (isBlank(detail)){
					if (pages.contains("-")){
						String message = "There is a pages tag with '-'. Unclear if this really means pages";
						fireWarningEvent(message, parentEvent, 8);
						reference.setPages(pages);
					}else{
						//handle pages as detail, this is at least true for Flora Malesiana
						refMap.put(DETAILS, pages); 
					}
				}else{
					if (! pages.contains("-")){
						String message = "There are pages and detail available where pages may also hold details information.";
						fireWarningEvent(message, parentEvent, 8);
					}
					reference.setPages(pages);
				}
			}
		}
	}

	public Reference<?> handleReference(MarkupImportState state,
			XMLEventReader reader, XMLEvent parentEvent)
			throws XMLStreamException {
		checkNoAttributes(parentEvent);

		boolean hasRefPart = false;
		Map<String, String> refMap = new HashMap<String, String>();
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				checkMandatoryElement(hasRefPart, parentEvent.asStartElement(), REF_PART);
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

}
