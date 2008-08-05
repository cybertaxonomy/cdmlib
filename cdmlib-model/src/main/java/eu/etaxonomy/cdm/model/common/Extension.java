/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

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
public class Extension extends VersionableEntity implements Cloneable {
	static Logger logger = Logger.getLogger(Extension.class);
	private String value;
	private ExtensionType type;
	private IdentifiableEntity extendedObj;
	
	@Transient
	public IdentifiableEntity getExtendedObj() {
		return extendedObj;
	}
	public void setExtendedObj(IdentifiableEntity extendedObj) {
		this.extendedObj = extendedObj;
	}

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
	
	//****************** CLONE ************************************************/
	 
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		Extension result = (Extension)super.clone();	
		//no changes to: type, value
		return result;
	}
	
	/**
	 * Clones this extension and sets the clone's extended object to 'extendedObject'
	 * @see java.lang.Object#clone()
	 */
	public Extension clone(IdentifiableEntity extendedObject) throws CloneNotSupportedException{
		Extension result = (Extension)clone();
		result.setExtendedObj(extendedObject);
		return result;
	}

}