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
		  logger.warn("Line:Col[" + validationEventLocator.getLineNumber() + ":" + validationEventLocator.getColumnNumber() +"]:" + validationEvent.getMessage());
		  return true;
		} else {
		   ValidationEventLocator validationEventLocator = validationEvent.getLocator();
   		   logger.error("Line:Col[" + validationEventLocator.getLineNumber() + ":" + validationEventLocator.getColumnNumber() +"]:" + validationEvent.getMessage());
   		   
   		   return false;
		}
	}
}
