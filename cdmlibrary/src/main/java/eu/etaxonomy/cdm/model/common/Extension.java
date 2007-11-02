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
 * This class aims to make available more "attributes" for identifiable entities
 * in a flexible way. Application developers (and even users) can define their own
 * "attrributes" as an ExtensionType and add data to Identifiable instances via
 * Extension instances.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:47
 */
public class Extension extends VersionableEntity {
	static Logger logger = Logger.getLogger(Extension.class);

	@Description("")
	private String value;
	private ExtensionType type;

	public ExtensionType getType(){
		return type;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setType(ExtensionType newVal){
		type = newVal;
	}

	public String getValue(){
		return value;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setValue(String newVal){
		value = newVal;
	}

}