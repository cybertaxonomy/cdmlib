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
 * "attributes" as an ExtensionType and add data to Identifiable instances via
 * Extension instances.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:23
 */
@Entity
public class Extension extends VersionableEntity {
	static Logger logger = Logger.getLogger(Extension.class);
	private String value;
	private ExtensionType type;
	@ManyToOne
	public ExtensionType getType(){
		return this.type;
	}

	/**
	 * 
	 * @param type    type
	 */
	public void setType(ExtensionType type){
		this.type = type;
	}

	public String getValue(){
		return this.value;
	}

	/**
	 * 
	 * @param value    value
	 */
	public void setValue(String value){
		this.value = value;
	}

}