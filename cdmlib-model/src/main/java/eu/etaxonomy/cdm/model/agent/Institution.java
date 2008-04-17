/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;

/**
 * A public or private institution.
 * It includes name, contact details and institution type.
 * <p>
 * See also the <a href="http://rs.tdwg.org/ontology/voc/Institution.rdf">TDWG Ontology</a>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:29
 */
@Entity
public class Institution extends Agent {
	static Logger logger = Logger.getLogger(Institution.class);
	private String code;
	private String name;
	private Set<InstitutionType> types = new HashSet();
	private Institution isPartOf;
	private Contact contact;

	/** 
	 * Class constructor
	 */
	public Institution() {
		super();
		// TODO Auto-generated constructor stub
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Contact getContact(){
		return this.contact;
	}
	/** 
	 * Assigns a {@link Contact contact} to this institution.
	 *
	 * @param  contact  the contact which should be assigned to this institution
	 */
	public void setContact(Contact contact){
		this.contact = contact;
	}

	@ManyToMany
	public Set<InstitutionType> getTypes(){
		return this.types;
	}
	
	/** 
	 * Adds a new institutional type from the corresponding vocabulary
	 * to describe better this institution or circumscribe its activities.
	 *
	 * @param  t  any type of institution relevant for describing this institution
	 * @see 	  InstitutionType
	 */
	public void addType(InstitutionType t){
		this.types.add(t);
	}
	
	/** 
	 * Removes one element from the set of institution types for this institution.
	 *
	 * @param  t  the institution type describing this institution or its activities
	 * 			  which should be deleted
	 * @see       #addType(InstitutionType)
	 */
	public void removeType(InstitutionType t){
		this.types.remove(t);
	}
	protected void setTypes(Set<InstitutionType> types){
		this.types = types;
	}


	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Institution getIsPartOf(){
		return this.isPartOf;
	}
	/** 
	 * Assigns a parent institution to this institution.
	 * This is for instance the case when a herbarium
	 * belongs to a museum (parent institution).
	 *
	 * @param  isPartOf  the institution to which this institution belongs
	 */
	public void setIsPartOf(Institution isPartOf){
		this.isPartOf = isPartOf;
	}

	public String getCode(){
		return this.code;
	}
	/** 
	 * Assigns a code (can also be an acronym or initials)
	 * by which this institution is known among experts.
	 *
	 * @param  code  the string which should be assigned as an identification code
	 * 				 to this institution
	 */
	public void setCode(String code){
		this.code = code;
	}

	
	public String getName(){
		return this.name;
	}
	/** 
	 * Assigns a full name, as distinct from a code, an acronym or initials,
	 * by which this institution is generally known.
	 *
	 * @param  name  the string which should be assigned as a full name
	 * 				 to this institution
	 */
	public void setName(String name){
		this.name = name;
	}

	@Override
	/**
	 * Generates the complete identification string of this institution
	 * on the basis of all its attributes.
	 * This method overrides {@link common.IdentifiableEntity#generateTitle() generateTitle}.
	 * The result might be kept as {@link common.IdentifiableEntity#setTitleCache(String) titleCache} if the
	 * flag {@link common.IdentifiableEntity#protectedTitleCache protectedTitleCache} is not set.
	 * 
	 * @return  the string which contains the complete identification of this institution
	 */
	public String generateTitle(){
		return "";
	}

}