/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.match;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author a.mueller
 * @since 03.08.2009
 */
public class MatchException extends Exception {

	private static final long serialVersionUID = -5128952015522078808L;
	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	public MatchException() {
	}

	public MatchException(String message) {
		super(message);
	}

	public MatchException(Throwable cause) {
		super(cause);
	}

	public MatchException(String message, Throwable cause) {
		super(message, cause);
	}
}