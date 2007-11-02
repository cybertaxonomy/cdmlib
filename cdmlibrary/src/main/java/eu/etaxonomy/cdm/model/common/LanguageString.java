/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:25
 */
public class LanguageString {
	static Logger logger = Logger.getLogger(LanguageString.class);

	@Description("")
	private char text;
	private Language language;

	public Language getLanguage(){
		return language;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLanguage(Language newVal){
		language = newVal;
	}

	public char getText(){
		return text;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setText(char newVal){
		text = newVal;
	}

}