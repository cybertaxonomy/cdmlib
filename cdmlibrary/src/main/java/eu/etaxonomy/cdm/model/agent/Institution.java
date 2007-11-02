/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;

/**
 * http://rs.tdwg.org/ontology/voc/Institution.rdf
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:23
 */
public class Institution extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(Institution.class);

	//Acronym, code or initialism by which the insitution is generally known
	@Description("Acronym, code or initialism by which the insitution is generally known")
	private String code;
	@Description("")
	private String name;
	private ArrayList types;
	private Institution isPartOf;
	private Contact contact;

	public Contact getContact(){
		return contact;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setContact(Contact newVal){
		contact = newVal;
	}

	public ArrayList getTypes(){
		return types;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTypes(ArrayList newVal){
		types = newVal;
	}

	public Institution getIsPartOf(){
		return isPartOf;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setIsPartOf(Institution newVal){
		isPartOf = newVal;
	}

	public String getCode(){
		return code;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCode(String newVal){
		code = newVal;
	}

	public String getName(){
		return name;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setName(String newVal){
		name = newVal;
	}

}