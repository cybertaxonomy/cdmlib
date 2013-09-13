/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Any;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

/**
 * This class aims to make available more "attributes" for identifiable entities
 * in a flexible way. Application developers (and even users) can define their own
 * "attributes" as an ExtensionType and add data to Identifiable instances via
 * Extension instances.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Extension", propOrder = {
    "value",
    "type",
    "extendedObj"
})
@Entity
@Audited
public class Extension extends VersionableEntity implements Cloneable {
	private static final long serialVersionUID = -857207737641432202L;
	@SuppressWarnings("unused")
	private static final  Logger logger = Logger.getLogger(Extension.class);
	
    @XmlElement(name = "Value")
	@Lob
    private String value;
	
    @XmlElement(name = "ExtensionType")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private ExtensionType type;
	
    @XmlElement(name = "ExtendedObject")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @Any(metaDef = "CdmBase",
	    	 metaColumn=@Column(name = "extendedObj_type"),
	    	 fetch = FetchType.LAZY,
	    	 optional = false)
	@JoinColumn(name = "extendedObj_id")
	@NotAudited
	private IdentifiableEntity extendedObj;
	
	public static Extension NewInstance(){
		return new Extension();
	}
	
	/**
	 * Creates a new extension and adds it to the extended object.
	 * @param extendedObject
	 * @param value
	 * @param extensionType
	 * @return
	 */
	public static Extension NewInstance(IdentifiableEntity<?> extendedObject, String value, ExtensionType extensionType){
		Extension extension = new Extension();
		extension.setValue(value);
		extension.setType(extensionType);
		extendedObject.addExtension(extension);
		return extension;
	}
	
	/**
	 * TODO should not be private but throws error in persistence/io test
	 * Constructor
	 */
	protected Extension(){
	}
	
	public IdentifiableEntity getExtendedObj() {
		return extendedObj;
	}
	//TODO make not public, but TaxonTaoHibernateImpl.delete has to be changed then
	public void setExtendedObj(IdentifiableEntity extendedObj) {
		this.extendedObj = extendedObj;
	}

	
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

//***************************** TO STRING ***********************************
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.CdmBase#toString()
	 */
	@Override
	public String toString() {
		if (StringUtils.isNotBlank(this.value)){
			return "Ext.: " + this.value;
		}else{
			return super.toString();
		}
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