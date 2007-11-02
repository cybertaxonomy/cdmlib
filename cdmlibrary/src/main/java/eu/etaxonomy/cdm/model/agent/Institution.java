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
 * @created 02-Nov-2007 19:18:18
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
	 * @param contact
	 */
	public void setContact(Contact contact){
		;
	}

	public ArrayList getTypes(){
		return types;
	}

	/**
	 * 
	 * @param types
	 */
	public void setTypes(ArrayList types){
		;
	}

	public Institution getIsPartOf(){
		return isPartOf;
	}

	/**
	 * 
	 * @param isPartOf
	 */
	public void setIsPartOf(Institution isPartOf){
		;
	}

	public String getCode(){
		return code;
	}

	/**
	 * 
	 * @param code
	 */
	public void setCode(String code){
		;
	}

	public String getName(){
		return name;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name){
		;
	}

}