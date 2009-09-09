/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.jaxb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DefaultErrorHandler implements ErrorHandler {

    private static Log log = LogFactory.getLog(DefaultErrorHandler.class);
	
	public void error(SAXParseException exception) throws SAXException {
		log.error(exception);
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		log.error(exception);
	}

	public void warning(SAXParseException exception) throws SAXException {
		log.warn(exception);
	}
}
