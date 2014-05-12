/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.common.init;

import org.apache.log4j.Logger;

/**
 * @author AM
 *
 */
public class TermNotFoundException extends Exception {
	private static final long serialVersionUID = 4288479011948189304L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermNotFoundException.class);
	
	
	/**
	 * 
	 */
	public TermNotFoundException() {
		super();
	}

	/**
	 * @param arg0
	 */
	public TermNotFoundException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public TermNotFoundException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public TermNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
