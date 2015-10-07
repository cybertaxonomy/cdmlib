// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.facade;

import org.apache.log4j.Logger;

/**
 * This exception should be thrown if a derived unit facade does not support a certain
 * method because its derived unit type (specimenOrObservation type) does not support
 * the functionality.
 * 
 * @see DerivedUnitFacadeNotSupportedException
 * 
 * @author a.mueller
 * @date 17.05.2010
 *
 */
public class MethodNotSupportedByDerivedUnitTypeException extends Exception {
	private static final long serialVersionUID = -1135345372784107810L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MethodNotSupportedByDerivedUnitTypeException.class);


	/**
	 * @param arg0
	 */
	public MethodNotSupportedByDerivedUnitTypeException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public MethodNotSupportedByDerivedUnitTypeException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public MethodNotSupportedByDerivedUnitTypeException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}


}
