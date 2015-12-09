/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.strategy.cache.agent.InstitutionDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

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
	"isPartOf"
})
@XmlRootElement(name = "Institution")
@Entity
// @Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.agent.AgentBase")
@Audited
@Configurable
public class Institution extends AgentBase<IIdentifiableEntityCacheStrategy<Institution>> {
	private static final long serialVersionUID = -951321271656955808L;
	public static final Logger logger = Logger.getLogger(Institution.class);

    @XmlElement(name = "Code")
    @Field
    //TODO Val #3379
//    @NullOrNotEmpty
    @Size(max = 255)
	private String code;

    @XmlElement(name = "Name")
    @Field
//TODO Val #3379
//    @NullOrNotEmpty
    @Size(max = 255)
	private String name;

    @XmlElementWrapper(name = "Types", nillable = true)
    @XmlElement(name = "Type")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    //preliminary  #5369
    @JoinTable(joinColumns = @JoinColumn( name="AgentBase_id"))
	private Set<DefinedTerm> types;  //InstitutionTypes

    @XmlElement(name = "IsPartOf")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Institution isPartOf;

	/**
	 * Creates a new empty institution instance.
	 */
	public static Institution NewInstance(){
		return new Institution();
	}


	/**
	 * Class constructor.
	 */
	protected Institution() {
		super();
		this.cacheStrategy = new InstitutionDefaultCacheStrategy();
	}

	/**
	 * Returns the set of institution types (categories)
	 * used to describe or circumscribe <i>this</i> institution's activities.
	 * Institution types are items of a controlled {@link eu.etaxonomy.cdm.model.common.TermVocabulary vocabulary}.
	 *
	 * @return	the set of institution types
	 */
	public Set<DefinedTerm> getTypes(){
		if(types == null) {
			this.types = new HashSet<DefinedTerm>();
		}
		return this.types;
	}

	/**
	 * Adds a new institutional type (from the corresponding {@link eu.etaxonomy.cdm.model.common.TermVocabulary vocabulary})
	 * to the set of institution types of <i>this</i> institution.
	 *
	 * @param  t  any type of institution
	 * @see 	  #getTypes()
	 */
	public void addType(DefinedTerm type){
		getTypes().add(type);
	}

	/**
	 * Removes one element from the set of institution types for <i>this</i> institution.
	 *
	 * @param  t  the institution type which should be deleted
	 * @see       #getTypes()
	 */
	public void removeType(DefinedTerm type){
		getTypes().remove(type);
	}

	/**
	 * Returns the parent institution of this institution.
	 * This is for instance the case when this institution is a herbarium
	 * belonging to a parent institution such as a museum.
	 */
	public Institution getIsPartOf(){
		return this.isPartOf;
	}

	/**
	 * Assigns a parent institution to which this institution belongs.
	 *
	 * @param  isPartOf  the parent institution
	 * @see	   #getIsPartOf()
	 */
	public void setIsPartOf(Institution parentInstitution){
		this.isPartOf = parentInstitution;
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
		this.code = StringUtils.isBlank(code) ? null : code;
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
		this.name = StringUtils.isBlank(name) ? null: name;
	}

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> Institution. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> Institution.
	 *
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		try{
			Institution result = (Institution) super.clone();
			//no changes to code, isPartOf, name, types
			return result;
		}catch (CloneNotSupportedException e){
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}