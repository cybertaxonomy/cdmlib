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

	/** 
	 * Returns the {@link Contact contact} corresponding to this institution.
	 * It includes telecommunication data
	 * and electronic as well as multiple postal addresses.
 	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Contact getContact(){
		return this.contact;
	}
	/** 
	 * @see  #getContact()
	 */
	public void setContact(Contact contact){
		this.contact = contact;
	}

	/** 
	 * Returns the set of institution {@link InstitutionType types} (categories)
	 * used to describe or circumscribe this institution's activities.
	 * Institution types are items of a controlled {@link common.TermVocabulary vocabulary}.
	 *
	 * @return	the set of institution types
	 * @see     InstitutionType
	 */
	@ManyToMany
	public Set<InstitutionType> getTypes(){
		return this.types;
	}
	
	/** 
	 * Adds a new institutional type (from the corresponding {@link common.TermVocabulary vocabulary})
	 * to the set of institution types of this institution.
	 *
	 * @param  t  any type of institution
	 * @see 	  #getTypes()
	 * @see 	  InstitutionType
	 */
	public void addType(InstitutionType t){
		this.types.add(t);
	}
	
	/** 
	 * Removes one element from the set of institution types for this institution.
	 *
	 * @param  t  the institution type which should be deleted
	 * @see       #getTypes()
	 */
	public void removeType(InstitutionType t){
		this.types.remove(t);
	}
	/** 
	 * @see     #getTypes()
	 */
	protected void setTypes(Set<InstitutionType> types){
		this.types = types;
	}


	/** 
	 * Returns the parent institution of this institution.
	 * This is for instance the case when this institution is a herbarium
	 * belonging to a parent institution such as a museum.
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Institution getIsPartOf(){
		return this.isPartOf;
	}
	/** 
	 * Assigns a parent institution to which this institution belongs.
	 *
	 * @param  isPartOf  the parent institution
	 * @see	   #getIsPartOf()
	 */
	public void setIsPartOf(Institution isPartOf){
		this.isPartOf = isPartOf;
	}

	/**
	 * Returns the string representing the code (can also be an acronym or initials)
	 * by which this institution is known among experts.
	 */
	public String getCode(){
		return this.code;
	}
	/** 
	 * @see	   #getCode()
	 */
	public void setCode(String code){
		this.code = code;
	}

	
	/** 
	 * Returns the full name, as distinct from a code, an acronym or initials,
	 * by which this institution is generally known.
	 */
	public String getName(){
		return this.name;
	}
	/** 
	 * @see	   #getName()
	 */
	public void setName(String name){
		this.name = name;
	}

	@Override
	/**
	 * Generates the identification string for this institution.
	 * The string is based on its name and code as well as on the name and code of
	 * its parent institution, if existing.
	 * This method overrides {@link common.IdentifiableEntity#generateTitle() generateTitle}.
	 * The result might be kept as {@link common.IdentifiableEntity#setTitleCache(String) titleCache} if the
	 * flag {@link common.IdentifiableEntity#protectedTitleCache protectedTitleCache} is not set.
	 * 
	 * @return  the identification string
	 */
	public String generateTitle(){
		return "";
	}

}