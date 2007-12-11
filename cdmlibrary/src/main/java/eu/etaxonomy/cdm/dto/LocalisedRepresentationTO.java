/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.dto;

/**
 * 
 * 
 * @author a.kohlbecker
 * @author  m.doering
 * @version 1.0
 * @created 11.12.2007 12:10:45
 *
 */
public class LocalisedRepresentationTO {

	private String description;
	private String language;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
}
