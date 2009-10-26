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
public class StringNotParsableException extends Exception {

	/**
	 * 
	 */
	public StringNotParsableException() {
		super();
	}

	/**
	 * @param message
	 */
	public StringNotParsableException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public StringNotParsableException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public StringNotParsableException(String message, Throwable cause) {
		super(message, cause);
	}

}
