/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database;

import org.apache.log4j.Logger;

/**
 * @author e.-m.lee
 * @since 12.01.2010
 *
 */
public class DatabaseSchemaMismatchException extends Exception {
	private static final Logger logger = Logger.getLogger(DatabaseSchemaMismatchException.class);
	
	/**
	 * @param message
	 */
	public DatabaseSchemaMismatchException() {
		super();
	}
	
	/**
	 * @param message
	 */
	public DatabaseSchemaMismatchException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DatabaseSchemaMismatchException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DatabaseSchemaMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

}
