// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.markup.MarkupImportConfigurator;
import eu.etaxonomy.cdm.io.markup.MarkupImportState;

/**
 * Base class for XML imports
 * @author a.mueller
 * @date 28.06.2011
 *
 */
public abstract class XmlImportBase<CONFIG extends XmlImportConfiguratorBase<STATE>, STATE extends XmlImportState<CONFIG, ?>> extends CdmImportBase<CONFIG, STATE> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(XmlImportBase.class);
	
	

	protected void fireSchemaConflictEventExpectedStartTag(String elName, XMLEvent next) {
		String type = "ElementStart";
		fireSchemaConflictEvent(type, elName, next);
	}
	

	/**
	 * @param r
	 * @return
	 * @throws XMLStreamException
	 */
	protected boolean validateStartOfDocument(XMLEventReader reader) throws XMLStreamException {
		XMLEvent next = reader.nextEvent();
		if (next.isStartDocument()){
			return true;
		}else {
			fireWarningEvent("Missing start of document", next.getLocation().toString(), 16);
			return false;
		}
	}

	protected InputStream getInputStream(CONFIG config) {
			try {
				URI uri = config.getSource();
				File file = new File(uri);
				InputStream is = new FileInputStream(file);
				return is;
			}catch (Exception e) {
				String message = "Problem reading source file %s. Import can not be executed. Reason: %s.";
				message = String.format(message, config.getSource(), e.getMessage());
				fireWarningEvent(message, "Read file", 16);
				return null;
			}
	}


	/**
	 * @param elName
	 * @param next
	 * @param message
	 * @param type
	 */
	private void fireSchemaConflictEvent(String expectedType, String expectedName, XMLEvent next) {
		String message = "Schema conflict: expected %s '%s' but was %s ";
		message = String.format(message, expectedType, expectedName, next.toString());
		fireWarningEvent(message, next.getLocation().toString(), 16);
	}
	

	/**
	 * Returns the StAX-Reader (XMLEventReader) for the source.
	 * @param state
	 * @return
	 * @throws FactoryConfigurationError
	 * @throws XMLStreamException
	 */
	protected XMLEventReader getStaxReader(STATE state)	throws FactoryConfigurationError, XMLStreamException {
		String fileName = state.getConfig().getSource().toString();
		InputStream is = getInputStream(state.getConfig());
		XMLInputFactory staxFactory = XMLInputFactory.newInstance();
		XMLEventReader reader = staxFactory.createXMLEventReader(fileName, is);
		return reader;
	}

}
