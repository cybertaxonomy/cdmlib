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

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * The (abstract) class representing a typification of an {@link HomotypicalGroup homotypical group}.
 * This allows to define a type designation only once for the homotypical group
 * as a whole instead of defining a type designation for each one of the taxon
 * names subsumed under one homotypical group.<BR>
 * All {@link TaxonNameBase taxon names} which have a {@link Rank rank} "species aggregate" or lower
 * can only be typified by specimens (a {@link SpecimenTypeDesignation specimen type designation}), but taxon
 * names with a higher rank might be typified by an other taxon name with
 * "species" rank (a {@link NameTypeDesignation name type designation}).
 * 
 * @author a.mueller
 * @created 07.08.2008
 * @version 1.0
 */
@XmlRootElement(name = "TypeDesignationBase")
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class TypeDesignationBase extends ReferencedEntityBase implements ITypeDesignation {
	private static final Logger logger = Logger.getLogger(TypeDesignationBase.class);


	@XmlElementWrapper(name = "TypifiedNames")
	@XmlElement(name = "TypifiedName")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Set<TaxonNameBase> typifiedNames = new HashSet<TaxonNameBase>();
	
	@XmlElement(name = "HomotypicalGroup")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private HomotypicalGroup homotypicalGroup;

// **************** CONSTRUCTOR *************************************/
	
	protected TypeDesignationBase(){
		super();
	}
	
	protected TypeDesignationBase(ReferenceBase citation, String citationMicroReference, String originalNameString){
		super(citation, citationMicroReference, originalNameString);
	}
	
	
// **************** METHODS *************************************/


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.name.ITypeDesignation#getHomotypicalGroup()
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
	@ManyToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TaxonNameBase> getTypifiedNames() {
		return typifiedNames;
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
