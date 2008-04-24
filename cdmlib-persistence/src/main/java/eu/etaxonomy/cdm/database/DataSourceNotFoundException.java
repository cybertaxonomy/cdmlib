/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

/**
 * @author a.mueller
 *
 */
public class DataSourceNotFoundException extends Exception {


	/**
	 * @param message
	 */
	public DataSourceNotFoundException() {
		super();
	}
	
	/**
	 * @param message
	 */
	public DataSourceNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DataSourceNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DataSourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
