/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.jaxb;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import org.apache.log4j.Logger;

public class WarningTolerantValidationEventHandler extends
		DefaultValidationEventHandler {
	private static final Logger logger = Logger.getLogger(WarningTolerantValidationEventHandler.class);
	
	public boolean handleEvent(ValidationEvent validationEvent) {
		// ignore warnings
		if (validationEvent.getSeverity() != ValidationEvent.WARNING) {
		  ValidationEventLocator validationEventLocator = validationEvent.getLocator();
		 // logger.warn("Line:Col[" + validationEventLocator.getLineNumber() + ":" + validationEventLocator.getColumnNumber() +"]:" + validationEvent.getMessage());
		
		  //  validationEvent.getLinkedException().printStackTrace();
		
		  //  TODO: check this
		  return true;
		} else {
		   ValidationEventLocator validationEventLocator = validationEvent.getLocator();
		  
   		   logger.warn("Line:Col[" + validationEventLocator.getLineNumber() + ":" + validationEventLocator.getColumnNumber() +"]:" + validationEvent.getMessage()+ " : "+validationEvent.getLinkedException().getStackTrace());
   		   
   		   return false;
		}
	}
}
