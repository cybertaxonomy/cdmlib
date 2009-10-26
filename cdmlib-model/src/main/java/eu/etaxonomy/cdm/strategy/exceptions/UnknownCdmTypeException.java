/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.strategy.exceptions;

/**
 * @author a.mueller
 *
 */
public class UnknownCdmTypeException extends Exception {

	/**
	 * 
	 */
	public UnknownCdmTypeException() {
		super();
	}

	/**
	 * @param message
	 */
	public UnknownCdmTypeException(String message) {
		super(message);
	}
	
	/**
	 * @param cause
	 */
	public UnknownCdmTypeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnknownCdmTypeException(String message, Throwable cause) {
		super(message, cause);
	}

}
