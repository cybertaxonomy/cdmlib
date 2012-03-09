// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.IoStateBase;
import eu.etaxonomy.cdm.io.common.events.IIoEvent;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.common.events.IoProblemEvent;
import eu.etaxonomy.cdm.io.dwca.TermUri;

/**
 * @author a.mueller
 * @date 23.11.2011
 *
 */
public class ConverterBase<STATE extends IoStateBase> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ConverterBase.class);

	protected STATE state;
	
	
	protected void fireWarningEvent(String message, CsvStreamItem item, Integer severity) {
		fireWarningEvent(message, getDataLocation(item), severity, 1);
	}
	
	private String getDataLocation(CsvStreamItem item) {
		String location = item.getLocation();
		return location;
	}
	
	protected void fireWarningEvent(String message, String dataLocation, Integer severity) {
		fireWarningEvent(message, dataLocation, severity, 1);
	}
	
	protected void fireWarningEvent(String message, String dataLocation, Integer severity, int stackDepth) {
		stackDepth++;
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		int lineNumber = stackTrace[stackDepth].getLineNumber();
		String methodName = stackTrace[stackDepth].getMethodName();

		IoProblemEvent event = IoProblemEvent.NewInstance(this.getClass(), message, dataLocation, 
				lineNumber, severity, methodName);
		
		//for performance improvement one may read:
		//http://stackoverflow.com/questions/421280/in-java-how-do-i-find-the-caller-of-a-method-using-stacktrace-or-reflection
//		Object o = new SecurityManager().getSecurityContext();
		
		logger.warn(message + "; " + dataLocation);  //TDOO preliminary until event system works on Converters
		
		fire(event);
	}
	
	protected void fire(IIoEvent event){
		Set<IIoObserver> observers = state.getConfig().getObservers();
		for (IIoObserver observer: observers){
			observer.handleEvent(event);
		}
	}
	


	/**
	 * Returns the value for the given term in the item.
	 */
	protected String getValue(CsvStreamItem item, TermUri term) {
		return item.get(term.getUriString());
	}
	
	/**
	 * Checks if the given term has a value in item that is not blank (null, empty or whitespace only). 
	 * @param term
	 * @param item
	 * @return true if value is not blank
	 */
	protected boolean exists(TermUri term, CsvStreamItem item) {
		return ! StringUtils.isBlank(getValue(item, term));
	}

}
