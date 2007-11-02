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
 * workaround for enumerations
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:36
 */
public class Representation extends VersionableEntity {
	static Logger logger = Logger.getLogger(Representation.class);

	@Description("")
	private String label;
	@Description("")
	private String abbreviatedLabel;
	@Description("")
	private String description;
	private Language language;

	public Language getLanguage(){
		return language;
	}

	/**
	 * 
	 * @param language
	 */
	public void setLanguage(Language language){
		;
	}

	public String getLabel(){
		return label;
	}

	/**
	 * 
	 * @param label
	 */
	public void setLabel(String label){
		;
	}

	public String getAbbreviatedLabel(){
		return abbreviatedLabel;
	}

	/**
	 * 
	 * @param abbreviatedLabel
	 */
	public void setAbbreviatedLabel(String abbreviatedLabel){
		;
	}

	public String getDescription(){
		return description;
	}

	/**
	 * 
	 * @param description
	 */
	public void setDescription(String description){
		;
	}

}