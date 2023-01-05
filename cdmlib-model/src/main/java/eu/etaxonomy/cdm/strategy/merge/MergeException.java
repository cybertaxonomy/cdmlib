/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.merge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author a.mueller
 * @since 03.08.2009
 */
public class MergeException extends Exception {

	private static final long serialVersionUID = 4817603805690549936L;
	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	public MergeException() {
	}

	public MergeException(String message) {
		super(message);
	}

	public MergeException(Throwable cause) {
		super(cause);
	}

	public MergeException(String message, Throwable cause) {
		super(message, cause);
	}
}