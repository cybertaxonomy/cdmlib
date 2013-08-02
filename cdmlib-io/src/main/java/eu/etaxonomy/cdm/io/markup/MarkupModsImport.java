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
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

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
	protected static final String MODS_NAME = "name";
	protected static final String MODS_ORIGININFO = "originInfo";
	protected static final String MODS_IDENTIFIER = "identifier";

	
	public MarkupModsImport(MarkupDocumentImport docImport) {
		super(docImport);
	}

	public void handleMods(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent)
			throws XMLStreamException {
		checkNoAttributes(parentEvent);
		
		Reference<?> modsRef = ReferenceFactory.newGeneric();
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
				handleNotYetImplementedElement(next);
			} else if (isStartingElement(next, MODS_ORIGININFO)) {
				handleNotYetImplementedElement(next);
			} else {
				handleUnexpectedElement(next);
			}
		}
		return;
	}

	private void handleIdentifier(MarkupImportState state, XMLEventReader reader, 
			XMLEvent parentEvent, Reference<?> modsRef) throws XMLStreamException {
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
	private void handleTitleInfo(MarkupImportState state, XMLEventReader reader, XMLEvent parentEvent, Reference<?> modsRef) 
			throws XMLStreamException {
		checkNoAttributes(parentEvent);

		
		String title = null;
		String subTitle = null;
		String partNumber = null;
		
		while (reader.hasNext()) {
			XMLEvent next = readNoWhitespace(reader);
			
			if (isMyEndingElement(next, parentEvent)) {
				String all = CdmUtils.concat(" - ", title, subTitle);
				all = CdmUtils.concat(", ", all, partNumber);
				modsRef.setTitle(all);
				return;
			}else if (isStartingElement(next, MODS_TITLE)) {
				title = this.getCData(state, reader, next);
			}else if (isStartingElement(next, MODS_SUBTITLE)) {
				subTitle = this.getCData(state, reader, next);
			}else if (isStartingElement(next, MODS_PARTNUMBER)) {
				partNumber = this.getCData(state, reader, next);
			} else {
				handleUnexpectedElement(next);
			}
		}
		return;
		
	}
}
