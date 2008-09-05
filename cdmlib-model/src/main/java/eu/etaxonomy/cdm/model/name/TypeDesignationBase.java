/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * The (abstract) class representing a typification of one or several {@link TaxonNameBase taxon names}.<BR>
 * All taxon names which have a {@link Rank rank} "species aggregate" or lower
 * can only be typified by specimens (a {@link SpecimenTypeDesignation specimen type designation}), but taxon
 * names with a higher rank might be typified by an other taxon name with
 * rank "species" or "genus" (a {@link NameTypeDesignation name type designation}).
 * 
 * @see		TaxonNameBase
 * @see		NameTypeDesignation
 * @see		SpecimenTypeDesignation
 * @author  a.mueller
 * @created 07.08.2008
 * @version 1.0
 */
@XmlRootElement(name = "TypeDesignationBase")
@XmlType(name = "TypeDesignationBase", propOrder = {
    "typifiedNames",
    "homotypicalGroup",
    "isNotDesignated"
})
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class TypeDesignationBase extends ReferencedEntityBase implements ITypeDesignation {
	private static final Logger logger = Logger.getLogger(TypeDesignationBase.class);


	@XmlElement(name = "IsNotDesignated")
	private boolean isNotDesignated;
	
	@XmlElementWrapper(name = "TypifiedNames")
	@XmlElement(name = "TypifiedName")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
    // Need these references (bidirectional) to fill table TypeDesignationBase_TaxonNameBase
	private Set<TaxonNameBase> typifiedNames = new HashSet<TaxonNameBase>();
	
	@XmlElement(name = "HomotypicalGroup")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private HomotypicalGroup homotypicalGroup;

// **************** CONSTRUCTOR *************************************/

	/** 
	 * Class constructor: creates a new empty type designation.
	 * 
	 * @see	#TypeDesignationBase(ReferenceBase, String, String, Boolean)
	 */
	protected TypeDesignationBase(){
		super();
	}
	
	/**
	 * Class constructor: creates a new type designation
	 * (including its {@link ReferenceBase reference source} and eventually
	 * the taxon name string originally used by this reference when establishing
	 * the former designation).
	 * 
	 * @param citation				the reference source for the new designation
	 * @param citationMicroReference	the string with the details describing the exact localisation within the reference
	 * @param originalNameString	the taxon name string used originally in the reference source for the new designation
	 * @param isNotDesignated		the boolean flag indicating whether there is no type at all for 
	 * 								<i>this</i> type designation
	 * @see							#TypeDesignationBase()
	 * @see							#isNotDesignated()
	 * @see							TaxonNameBase#getTypeDesignations()
	 */
	protected TypeDesignationBase(ReferenceBase citation, String citationMicroReference, String originalNameString, boolean isNotDesignated){
		super(citation, citationMicroReference, originalNameString);
		this.isNotDesignated = isNotDesignated;
	}
	
	
// **************** METHODS *************************************/


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.name.ITypeDesignation#getHomotypicalGroup()
	 */
	/** 
	 * Returns the {@link HomotypicalGroup homotypical group} to which all (in <i>this</i>
	 * type designation) typified {@link TaxonNameBase taxon names} belong.
	 *  
	 * @see   #getTypifiedNames()
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public HomotypicalGroup getHomotypicalGroup() {
		return homotypicalGroup;
	}

	@Deprecated //for hibernate use only
	private void setHomotypicalGroup(HomotypicalGroup homotypicalGroup) {
		this.homotypicalGroup = homotypicalGroup;		
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.name.ITypeDesignation#getTypifiedNames()
	 */
	/** 
	 * Returns the set of {@link TaxonNameBase taxon names} typified in <i>this</i>
	 * type designation. This is a subset of the taxon names belonging to the
	 * corresponding {@link #getHomotypicalGroup() homotypical group}.
	 */
	@ManyToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TaxonNameBase> getTypifiedNames() {
		return typifiedNames;
	}

	/**
	 * Returns the boolean value "true" if it is known that a type does not
	 * exist and therefore the {@link TaxonNameBase taxon name} to which <i>this</i>
	 * type designation is assigned must still be typified. Two
	 * cases must be differentiated: <BR><ul> 
	 * <li> a) it is unknown whether a type exists and 
	 * <li> b) it is known that no type exists
	 *  </ul>
	 * If a) is true there should be no TypeDesignation instance at all
	 * assigned to the "typified" taxon name.<BR>
	 * If b) is true there should be a TypeDesignation instance with the
	 * flag isNotDesignated set. The typeName attribute, in case of a
	 * {@link NameTypeDesignation name type designation}, or the typeSpecimen attribute,
	 * in case of a {@link SpecimenTypeDesignation specimen type designation}, should then be "null".
	 */
	public boolean isNotDesignated() {
		return isNotDesignated;
	}

	/**
	 * @see   #isNotDesignated()
	 */
	public void setNotDesignated(boolean isNotDesignated) {
		this.isNotDesignated = isNotDesignated;
	}


	@Deprecated //for hibernate use only
	private void setTypifiedNames(Set<TaxonNameBase> typifiedNames) {
		this.typifiedNames = typifiedNames;
	}
	
	@Deprecated //for bidirectional use only
	protected void addTypifiedName(TaxonNameBase taxonName){
		this.typifiedNames.add(taxonName);
	}
	

}
