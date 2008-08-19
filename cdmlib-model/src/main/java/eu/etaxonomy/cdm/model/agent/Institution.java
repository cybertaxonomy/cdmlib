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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents public or private institutions.
 * It includes name, contact details and institution type.
 * <P>
 * This class corresponds to: <ul>
 * <li> Institution according to the TDWG ontology
 * <li> Institution according to the TCS
 * <li> Organisation (Institution) according to the ABCD schema
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:29
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Institution", propOrder = {
	"code",
	"name",
	"types",
	"isPartOf",
	"contact"
})
@XmlRootElement(name = "Institution")
@Entity
public class Institution extends Agent {
	
	static Logger logger = Logger.getLogger(Institution.class);
	
    @XmlElement(name = "Code")
	private String code;
	
    @XmlElement(name = "Name")
	private String name;
	
    @XmlElementWrapper(name = "Types")
    @XmlElement(name = "Type")
    //@XmlIDREF
    //@XmlSchemaType(name = "IDREF")
	private Set<InstitutionType> types = new HashSet<InstitutionType>();
	
    @XmlElement(name = "IsPartOf")
    //@XmlIDREF
    //@XmlSchemaType(name = "IDREF")
	private Institution isPartOf;
	
    @XmlElement(name = "Contact")
	private Contact contact;

	/**
	 * Creates a new empty institution instance.
	 */
	public static Institution NewInstance(){
		return new Institution();
	}
	
	
	/** 
	 * Class constructor.
	 */
	public Institution() {
		super();
	}

	/** 
	 * Returns the {@link Contact contact} corresponding to <i>this</i> institution.
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
	 * used to describe or circumscribe <i>this</i> institution's activities.
	 * Institution types are items of a controlled {@link eu.etaxonomy.cdm.model.common.TermVocabulary vocabulary}.
	 *
	 * @return	the set of institution types
	 * @see     InstitutionType
	 */
	@ManyToMany
	public Set<InstitutionType> getTypes(){
		return this.types;
	}
	
	/** 
	 * Adds a new institutional type (from the corresponding {@link eu.etaxonomy.cdm.model.common.TermVocabulary vocabulary})
	 * to the set of institution types of <i>this</i> institution.
	 *
	 * @param  t  any type of institution
	 * @see 	  #getTypes()
	 * @see 	  InstitutionType
	 */
	public void addType(InstitutionType t){
		this.types.add(t);
	}
	
	/** 
	 * Removes one element from the set of institution types for <i>this</i> institution.
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

	/**
	 * Generates the identification string for this institution.
	 * The string is based on its name and code as well as on the name and code of
	 * its parent institution, if existing.
	 * This method overrides {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle() generateTitle}.
	 * The result might be kept as {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#setTitleCache(String) titleCache} if the
	 * flag {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#protectedTitleCache protectedTitleCache} is not set.
	 * 
	 * @return  the identification string
	 */
	@Override
	public String generateTitle(){
		return "";
	}

}