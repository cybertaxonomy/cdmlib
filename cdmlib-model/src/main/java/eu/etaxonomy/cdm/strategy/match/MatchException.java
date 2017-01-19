/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.match;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @created 03.08.2009
 * @version 1.0
 */
public class MatchException extends Exception {
	private static final long serialVersionUID = -5128952015522078808L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MatchException.class);

	/**
	 * 
	 */
	public MatchException() {
	}

	/**
	 * @param message
	 */
	public MatchException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public MatchException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MatchException(String message, Throwable cause) {
		super(message, cause);
	}
}
