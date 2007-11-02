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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * This class aims to make available more "attributes" for identifiable entities
 * in a flexible way. Application developers (and even users) can define their own
 * "attrributes" as an ExtensionType and add data to Identifiable instances via
 * Extension instances.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:06
 */
@Entity
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
	 * @param type
	 */
	public void setType(ExtensionType type){
		;
	}

	public String getValue(){
		return value;
	}

	/**
	 * 
	 * @param value
	 */
	public void setValue(String value){
		;
	}

}