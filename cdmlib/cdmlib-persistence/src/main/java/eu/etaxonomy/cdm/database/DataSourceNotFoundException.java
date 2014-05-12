/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 *
 */
public class DataSourceNotFoundException extends Exception {
	private static final long serialVersionUID = 5269129655671736295L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DataSourceNotFoundException.class);
	
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
