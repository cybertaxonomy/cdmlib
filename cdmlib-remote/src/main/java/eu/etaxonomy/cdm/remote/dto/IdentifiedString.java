/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

/**
 * A String globally uniquely identified by an uuid.
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 11.12.2007 14:13:29
 *
 */
public class IdentifiedString  {
	
	private String uuid;
	private String value;
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getText() {
		return value;
	}
	public void setText(String text) {
		this.value = text;
	}
	

}
