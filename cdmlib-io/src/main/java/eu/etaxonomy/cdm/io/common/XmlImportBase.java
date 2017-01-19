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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import eu.etaxonomy.cdm.io.common.events.IIoEvent;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;

/**
 * Base class for XML imports
 * @author a.mueller
 * @date 28.06.2011
 *
 */
public abstract class XmlImportBase<CONFIG extends XmlImportConfiguratorBase<STATE>, STATE extends XmlImportState<CONFIG, ?>> extends CdmImportBase<CONFIG, STATE> implements IIoObserver {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(XmlImportBase.class);
	
	

	protected void fireSchemaConflictEventExpectedStartTag(String elName, XMLEventReader reader) throws XMLStreamException {
		String type = "ElementStart";
		XMLEvent next = reader.nextEvent();
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
	

	/**
	 * TODO namespace
	 * @param elName
	 * @param reader
	 * @return
	 * @throws XMLStreamException
	 */
	protected boolean isStartingElement(XMLEventReader reader, String elName) throws XMLStreamException {
		XMLEvent next;
		try {
			next = reader.peek();
		} catch (NoSuchElementException e) {
			return false;
		}
		return isStartingElement(next, elName);
	}

	protected boolean isStartingElement(XMLEvent event, String elName) throws XMLStreamException {
		boolean result = false;
		boolean isStart = event.isStartElement();
		if (isStart){
			QName name = event.asStartElement().getName();
			boolean equals = name.getLocalPart().equals(elName);
			result = equals;
		}
		return result;
	}


	protected boolean isEndingElement(XMLEventReader reader, String elName) throws XMLStreamException {
		XMLEvent next;
		try {
			next = reader.peek();
		} catch (NoSuchElementException e) {
			return false;
		}
		return isEndingElement(next, elName);
	}
	
	protected boolean isEndingElement(XMLEvent event, String elName) throws XMLStreamException {
		boolean result = false;
		boolean isEnd = event.isEndElement();
		if (isEnd){
			QName name = event.asEndElement().getName();
			result = name.getLocalPart().equals(elName);
		}
		return result;
	}


	/**
	 * Returns an input stream for the given source.
	 * @param config
	 * @return
	 */
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
		String eventString;
		if (next.isStartElement()){
			eventString = next.asStartElement().getName().getLocalPart();
		}else if(next.isEndElement()){
			eventString = next.asEndElement().getName().getLocalPart();
		}else{
			eventString = next.toString();
		}
		message = String.format(message, expectedType, expectedName, eventString);
		String location = "l." + next.getLocation().getLineNumber() + "/c." + next.getLocation().getColumnNumber();
		fireWarningEvent(message, location, 16);
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
		InputStream is = null;
		is = getInputStream(state.getConfig());  //throws exception and looks like it is not needed.
		XMLInputFactory staxFactory = XMLInputFactory.newInstance();
		XMLEventReader reader = staxFactory.createXMLEventReader(fileName, is);
		return reader;
	}
	

	/**
	 * Parses the source file with the given handler
	 * @param is
	 * @param handler
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	protected void parseSAX(STATE state, ImportHandlerBase handler)
			throws ParserConfigurationException, SAXException, IOException {
		handler.addObserver(this);
		InputStream is = getInputStream(state.getConfig());
	    SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxFactory.newSAXParser();
		saxParser.parse(is, handler);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.events.IIoObserver#handleEvent(eu.etaxonomy.cdm.io.common.events.IIoEvent)
	 */
	@Override
	public void handleEvent(IIoEvent event) {
		fire(event);
	}

}
