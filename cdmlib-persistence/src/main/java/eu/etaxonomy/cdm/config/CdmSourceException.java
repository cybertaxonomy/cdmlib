/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.config;

/**
 * Exception class which wraps around exceptions related to 
 * Cdm Source functionality 
 * 
 * @author cmathew
 *
 */
public class CdmSourceException extends Exception {
	
	/**
	 * Constructor which simply uses the error message
	 * 
	 * @param message , representing the error
	 */
	public CdmSourceException(String message) {
		super(message);
	}

}
