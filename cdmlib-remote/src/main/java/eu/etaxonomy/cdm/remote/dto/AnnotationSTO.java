/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import org.apache.log4j.Logger;

/**
 * @author nho
 * @created 24.09.2008
 * @version 1.0
 */
public class AnnotationSTO extends BaseTO {
	private static Logger logger = Logger.getLogger(AnnotationSTO.class);
	
	
	private String text;

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
}
