/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import eu.etaxonomy.cdm.model.common.Taxon;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/Institution.rdf
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:29
 */
@Entity
public class Institution extends Agent {
	public Institution() {
		super();
		// TODO Auto-generated constructor stub
	}

	static Logger logger = Logger.getLogger(Institution.class);
	//Acronym, code or initialism by which the insitution is generally known
	private String code;
	private String name;
	private Set<InstitutionType> types;
	private Institution isPartOf;
	private Contact contact;

	public Contact getContact(){
		return this.contact;
	}

	/**
	 * 
	 * @param contact    contact
	 */
	public void setContact(Contact contact){
		this.contact = contact;
	}

	@OneToMany
	public Set<InstitutionType> getTypes(){
		return this.types;
	}
	public void addTypes(InstitutionType t){
		this.types.add(t);
	}
	public void removeTypes(InstitutionType t){
		this.types.remove(t);
	}

	/**
	 * 
	 * @param types    types
	 */
	public void setTypes(Set<InstitutionType> types){
		this.types = types;
	}

	public Institution getIsPartOf(){
		return this.isPartOf;
	}

	/**
	 * 
	 * @param isPartOf    isPartOf
	 */
	public void setIsPartOf(Institution isPartOf){
		this.isPartOf = isPartOf;
	}

	public String getCode(){
		return this.code;
	}

	/**
	 * 
	 * @param code    code
	 */
	public void setCode(String code){
		this.code = code;
	}

	public String getName(){
		return this.name;
	}

	/**
	 * 
	 * @param name    name
	 */
	public void setName(String name){
		this.name = name;
	}

	@Override
	public String generateTitle(){
		return "";
	}

}