/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.molecular;


import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * @author a.mueller
 * @created 04-Mar-2013 13:06:32
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Locus", propOrder = {
    "name",
    "description"
})
@XmlRootElement(name = "Locus")
@Entity
@Audited
public class Locus extends VersionableEntity {
	private static final long serialVersionUID = 3907156009866200988L;
	private static final Logger logger = Logger.getLogger(Locus.class);
	
	@XmlElement(name = "Name")
	private String name;
	
	@XmlElement(name = "Description")
	private String description;
	
//*********************** FACTORY ****************************************************/	

	public static Locus NewInstance(String name, String description){
		Locus result = new Locus();
		result.setName(name);
		result.setDescription(description);
		return result;
	}
	
//*********************** CONSTRUCTOR ****************************************************/

	//Locus currently does not allow private constructor (javassist can't lazy load 
	// and create  class).
	//Still need to find out why.
	protected Locus() {

	}

//*********************** GETTER / SETTER ****************************************************/
	
	
	
	public String getName(){
		logger.debug("getName");
		return this.name;
	}

	/**
	 * 
	 * @param name    name
	 */
	public void setName(String name){
		this.name = name;
	}

	public String getDescription(){
		return this.description;
	}

	/**
	 * 
	 * @param description    description
	 */
	public void setDescription(String description){
		this.description = description;
	}

}