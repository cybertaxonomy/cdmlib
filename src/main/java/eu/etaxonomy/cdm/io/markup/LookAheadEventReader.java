// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.markup;

import javax.mail.MethodNotSupportedException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.XmlImportState;

/**
 * @author a.mueller
 * @date 28.06.2011
 *
 */
public class LookAheadEventReader implements XMLEventReader {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(LookAheadEventReader.class);

	XMLEventReader reader;
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Object next() {
		throw new RuntimeException("Iterator methods are not supported by this EventReader");
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new RuntimeException("Iterator methods are not supported by this EventReader");
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLEventReader#nextEvent()
	 */
	@Override
	public XMLEvent nextEvent() throws XMLStreamException {
		return reader.nextEvent();
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLEventReader#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return reader.hasNext();
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLEventReader#peek()
	 */
	@Override
	public XMLEvent peek() throws XMLStreamException {
		return reader.peek();
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLEventReader#getElementText()
	 */
	@Override
	public String getElementText() throws XMLStreamException {
		return reader.getElementText();
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLEventReader#nextTag()
	 */
	@Override
	public XMLEvent nextTag() throws XMLStreamException {
		return reader.nextTag();
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLEventReader#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return reader.getProperty(name);
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLEventReader#close()
	 */
	@Override
	public void close() throws XMLStreamException {
		reader.close();
	}

}
