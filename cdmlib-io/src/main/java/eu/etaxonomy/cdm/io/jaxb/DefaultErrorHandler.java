/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.jaxb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DefaultErrorHandler implements ErrorHandler {

    private static Logger logger = LogManager.getLogger();

	@Override
    public void error(SAXParseException exception) throws SAXException {
		logger.error(exception);
	}

	@Override
    public void fatalError(SAXParseException exception) throws SAXException {
		logger.error(exception);
	}

	@Override
    public void warning(SAXParseException exception) throws SAXException {
		logger.warn(exception);
	}
}
