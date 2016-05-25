/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.markup;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.GeneralParser;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @created 30.05.2012
 *
 */
public class MarkupModsImport extends MarkupImportBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MarkupModsImport.class);

	protected static final String MODS_TITLEINFO = "titleInfo";
	protected static final String MODS_ABSTRACT = "abstract";
	protected static final String MODS_TITLE = "title";
	protected static final String MODS_SUBTITLE = "subTitle";
	protected static final String MODS_PARTNUMBER = "partNumber";
	protected static final String MODS_PARTNAME = "partName";
	protected static final String MODS_NAME = "name";
	protected static final String MODS_ORIGININFO = "originInfo";
	protected static final String MODS_IDENTIFIER = "identifier";
	protected static final String MODS_DESCRIPTION = "description";
	protected static final String MODS_NAME_PART = "namePart";
	protected static final String MODS_AFFILIATION = "affiliation";
	protected static final String MODS_PUBLISHER ="publisher";
	protected static final String MODS_DATE_ISSUED ="dateIssued";
	protected static final String MODS_PLACE ="place";
	protected static final String MODS_EDITION ="edition";


	public MarkupModsImport(MarkupDocumentImport docImport) {
		super(docImport);
	}

	public void handleMods(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent)
			throws XMLStreamException {
		checkNoAttributes(parentEvent);

		Reference modsRef = ReferenceFactory.newGeneric();
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			if (isMyEndingElement(next, parentEvent)) {
				//set the source reference
				state.getConfig().setSourceReference(modsRef);
				return;
			}else if (isStartingElement(next, MODS_TITLEINFO)) {
				handleTitleInfo(state, reader, next, modsRef);
			}else if (isStartingElement(next, MODS_ABSTRACT)) {
				String abstractStr = getCData(state, reader, next, true).trim();
				if (abstractStr.startsWith("ABSTRACT")){
					abstractStr = abstractStr.replaceFirst("ABSTRACT", "").trim();
				}
				modsRef.setReferenceAbstract(abstractStr);
			} else if (isStartingElement(next, MODS_IDENTIFIER)) {
				handleIdentifier(state, reader, next, modsRef);
			} else if (isStartingElement(next, MODS_NAME)) {
				handleName(state, reader, next, modsRef);
			} else if (isStartingElement(next, MODS_ORIGININFO)) {
				handleOriginInfo(state, reader, next, modsRef);
			} else {
				handleUnexpectedElement(next);
			}
		}
		return;
	}

	private void handleOriginInfo(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, Reference modsRef) throws XMLStreamException {
		checkNoAttributes(parentEvent);
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);

			if (isMyEndingElement(next, parentEvent)) {
				return;
			}else if (isStartingElement(next, MODS_PUBLISHER)) {
				String publisher = this.getCData(state, reader, next);
				if (modsRef.getPublisher() != null){
					fireWarningEvent("Multiple publisher infos given. Concat by ;", next, 2);
				}
				modsRef.setPublisher(CdmUtils.concat(";", modsRef.getPublisher(), publisher));
			}else if (isStartingElement(next, MODS_DATE_ISSUED)) {
				String dateIssued = this.getCData(state, reader, next);
				if (modsRef.getDatePublished() != null && ! modsRef.getDatePublished().isEmpty()){
					fireWarningEvent("Multiple publish date infos given. I overwrite older information. Please check manually ;", next, 4);
				}
				TimePeriod timePeriod = TimePeriodParser.parseString(dateIssued);
				modsRef.setDatePublished(timePeriod);
			}else if (isStartingElement(next, MODS_PLACE)) {
				String place = this.getCData(state, reader, next);
				if (modsRef.getPlacePublished() != null){
					fireWarningEvent("Multiple place published infos given. Concat by ;", next, 2);
				}
				modsRef.setPlacePublished(CdmUtils.concat(";", modsRef.getPlacePublished(), place));
			}else if (isStartingElement(next, MODS_EDITION)) {
				String edition = this.getCData(state, reader, next);
				if (modsRef.getEdition() != null){
					fireWarningEvent("Multiple edition infos given. Concat by ;", next, 2);
				}
				modsRef.setEdition(CdmUtils.concat(";", modsRef.getEdition(), edition));
			} else {
				handleUnexpectedElement(next);
			}
		}
		return;
	}

	private void handleIdentifier(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, Reference modsRef) throws XMLStreamException {
		checkNoAttributes(parentEvent);


		String identifier = getCData(state, reader, parentEvent, true).trim();

		if (GeneralParser.isIsbn(identifier)){
			modsRef.setIsbn(identifier);
		}else{
			String message = "Identifier pattern not recognized: %s";
			fireWarningEvent(String.format(message, identifier), parentEvent, 4);
		}

		return;
	}

	/**
	 * Reads all titleInfo information.
	 * ! Preliminary implementation !
	 */
	private void handleTitleInfo(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, Reference modsRef)
			throws XMLStreamException {
		checkNoAttributes(parentEvent);

		String title = null;
		String subTitle = null;
		String partNumber = null;
		String partName = null;

		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);

			if (isMyEndingElement(next, parentEvent)) {
				String all = CdmUtils.concat(" - ", title, subTitle);
				//TODO according to http://library.princeton.edu/departments/tsd/metadoc/mods/titleinfo.html
				//partNumber and partName can be repeated and the order should be kept
				String part = CdmUtils.concat(" ", partNumber, partName);
				all = CdmUtils.concat(", ", all, part);
				modsRef.setTitle(all);
				return;
			}else if (isStartingElement(next, MODS_TITLE)) {
				title = this.getCData(state, reader, next);
			}else if (isStartingElement(next, MODS_SUBTITLE)) {
				subTitle = this.getCData(state, reader, next);
			}else if (isStartingElement(next, MODS_PARTNAME)) {
				partName = this.getCData(state, reader, next);
			}else if (isStartingElement(next, MODS_PARTNUMBER)) {
				partNumber = this.getCData(state, reader, next);
			} else {
				handleUnexpectedElement(next);
			}
		}
		return;

	}

	/**
	 * Reads all titleInfo information.
	 * ! Preliminary implementation !
	 */
	private void handleName(MarkupImportState state, XMLEventReader reader, XMLEvent parent, Reference modsRef)
			throws XMLStreamException {
		String type = getOnlyAttribute(parent, "type", true);

		String description = null;
		String namePart = null;
		String partNumber = null;
		String affiliation = null;

		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);

			if (isMyEndingElement(next, parent)) {
				if (! type.equals("personal")){
					fireUnexpectedAttributeValue(parent, "type", type);  //currently we handle only "personal"
				}else{
					Person person = Person.NewInstance();
					TeamOrPersonBase<?> author = modsRef.getAuthorship();
					if (author == null){
						modsRef.setAuthorship(person);
					}else if (author.isInstanceOf(Person.class)){
						Team team = Team.NewInstance();
						team.addTeamMember(person);
						modsRef.setAuthorship(team);
					}else {
						CdmBase.deproxy(author, Team.class).addTeamMember(person);
					}
					if (isNotBlank(namePart)){
						person.setTitleCache(namePart, true);
					}
					if (isNotBlank(description)){
						fireWarningEvent("Mods:description needs to be handled manually",this.makeLocationStr(parent.getLocation()), 1);
					}
					if (isNotBlank(affiliation)){
						Institution institution = Institution.NewInstance();
						institution.setTitleCache(affiliation, true);
						person.addInstitutionalMembership(institution, null, null, null);
					}

				}

				return;
			}else if (isStartingElement(next, MODS_DESCRIPTION)) {
				description = this.getCData(state, reader, next);
			}else if (isStartingElement(next, MODS_NAME_PART)) {
				namePart = this.getCData(state, reader, next);
			}else if (isStartingElement(next, MODS_AFFILIATION)) {
				affiliation = this.getCData(state, reader, next);
			} else {
				handleUnexpectedElement(next);
			}
		}
		return;

	}
}
