/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

/**
 * workaround for enumerations
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:14
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
	 * @param newVal
	 */
	public void setLanguage(Language newVal){
		language = newVal;
	}

	public String getLabel(){
		return label;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLabel(String newVal){
		label = newVal;
	}

	public String getAbbreviatedLabel(){
		return abbreviatedLabel;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAbbreviatedLabel(String newVal){
		abbreviatedLabel = newVal;
	}

	public String getDescription(){
		return description;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDescription(String newVal){
		description = newVal;
	}

}