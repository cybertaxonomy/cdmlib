/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.TaxonComparator;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;


/**
 * The homotypical group class represents a set of {@link TaxonNameBase taxon names} associated
 * on the base of their typifications. Since it can be asserted that two taxon
 * names are typified by the same type without mentioning the type itself, even
 * taxon names without explicit {@link TypeDesignationBase type designation} can belong
 * to an homotypical group.<BR>
 * Taxon names belonging to an homotypical group and the taxon names or
 * {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase specimens} used as types for their
 * {@link TypeDesignationBase type designations} have the following properties: <ul>
 * <li>	A taxon name belongs exactly to one homotypical group
 * <li>	A type specimen or a type name can be used as a type only for taxon
 * 		names belonging to the same homotypical group<BR>
 * 		- therefore an homotypical group circumscribes a set of types<BR>
 * 		- each taxon name shares a subset of these types<BR>
 * 		- each type is used by a subset of these taxon names
 * 			within the homotypical group
 * <li>	Names that share at least one common type must belong to the same
 * 		homotypical group
 * <li>	Names that share the same basionym or replaced synonym must belong to
 * 		the same homotypical group
 * </ul>
 * 
 * @see		TypeDesignationBase
 * @see		NameTypeDesignation
 * @see		SpecimenTypeDesignation
 * @author  m.doering
 * @version 1.0
 * @created 08-Nov-2007
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HomotypicalGroup", propOrder = {
    "typifiedNames"
})
@Entity
//@Audited
public class HomotypicalGroup extends AnnotatableEntity {
	static Logger logger = Logger.getLogger(HomotypicalGroup.class);

	@XmlElementWrapper(name = "TypifiedNames")
	@XmlElement(name = "TypifiedName")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	protected Set<TaxonNameBase> typifiedNames = new HashSet<TaxonNameBase>();
	    
	/** 
	 * Class constructor: creates a new homotypical group instance with an
	 * empty set of typified {@link TaxonNameBase taxon names}.
	 */
	public HomotypicalGroup() {
		super();
	}
	
	/** 
	 * Creates a new homotypical group instance with an empty set of typified
	 * {@link TaxonNameBase taxon names}.
	 * 
	 * @see #HomotypicalGroup()
	 */
	public static HomotypicalGroup NewInstance(){
		return new HomotypicalGroup();
	}
		
	/** 
	 * Returns the set of {@link TaxonNameBase taxon names} that belong to <i>this</i> homotypical group.
	 *
	 * @see	#getSpecimenTypeDesignations()
	 */
	@OneToMany(mappedBy="homotypicalGroup", fetch=FetchType.LAZY)
	public Set<TaxonNameBase> getTypifiedNames() {
		return typifiedNames;
	}
	/** 
	 * @see #getTypifiedNames()
	 */
	protected void setTypifiedNames(Set<TaxonNameBase> typifiedNames) {
		this.typifiedNames = typifiedNames;
	}
	
	/** 
	 * Adds a new {@link TaxonNameBase taxon name} to the set of taxon names that belong
	 * to <i>this</i> homotypical group.
	 *
	 * @param  typifiedName  the taxon name to be added to <i>this</i> group
	 * @see 			  	 #getTypifiedNames()
	 * @see 			  	 #removeTypifiedName(TaxonNameBase)
	 */
	public void addTypifiedName(TaxonNameBase typifiedName) {
		if (typifiedName != null){
			typifiedName.setHomotypicalGroup(this);
			typifiedNames.add(typifiedName);
		}
	}
	/** 
	 * Removes one element from the set of {@link TaxonNameBase taxon names}
	 * that belong to <i>this</i> homotypical group.
	 *
	 * @param  taxonBase	the taxon name which should be removed from the corresponding set
	 * @see    				#addTypifiedName(TaxonNameBase)
	 */
	public void removeTypifiedName(TaxonNameBase typifiedName) {
		typifiedName.setHomotypicalGroup(null);
		typifiedNames.remove(typifiedName);	
	}

	/**
	 * Merges the typified {@link TaxonNameBase taxon names} from one homotypical group into
	 * the set of typified taxon names of <i>this</i> homotypical group.
	 *  
	 * @param	homotypicalGroupToMerge the homotypical group the typified names of which
	 * 									are to be transferred to <i>this</i> homotypical group
	 */
	public void merge(HomotypicalGroup homotypicalGroupToMerge){
		if (homotypicalGroupToMerge != null){
			Set<TaxonNameBase> typifiedNames = new HashSet<TaxonNameBase>();
			typifiedNames.addAll(homotypicalGroupToMerge.getTypifiedNames());
			for (TaxonNameBase typifiedName: typifiedNames){
				this.addTypifiedName(typifiedName);
			}
		}
	}
	
	
	/** 
	 * Returns the set of {@link SpecimenTypeDesignation specimen type designations} that
	 * typify the {@link TaxonNameBase taxon names} belonging to <i>this</i> homotypical group
	 * including the status of these designations.
	 *
	 * @see	#getTypifiedNames()
	 * @see	#getNameTypeDesignations()
	 * @see	#getTypeDesignations()
	 * @see	TaxonNameBase#getSpecimenTypeDesignations()
	 */
	@Transient
	public Set<SpecimenTypeDesignation> getSpecimenTypeDesignations(){
		Set<SpecimenTypeDesignation> result = new HashSet<SpecimenTypeDesignation>();
		for (TaxonNameBase taxonName : typifiedNames){
			result.addAll(taxonName.getSpecimenTypeDesignations());
		}
		return result;
	}
	
	/** 
	 * Returns the set of {@link NameTypeDesignation name type designations} that
	 * typify the {@link TaxonNameBase taxon names} belonging to <i>this</i> homotypical group
	 * including the status of these designations.
	 *
	 * @see	#getTypifiedNames()
	 * @see	#getSpecimenTypeDesignations()
	 * @see	#getTypeDesignations()
	 * @see	TaxonNameBase#getNameTypeDesignations()
	 */
	@Transient
	public Set<NameTypeDesignation> getNameTypeDesignations(){
		Set<NameTypeDesignation> result = new HashSet<NameTypeDesignation>();
		for (TaxonNameBase taxonName : typifiedNames){
			result.addAll(taxonName.getNameTypeDesignations());
		}
		return result;
	}
	
	
	/** 
	 * Returns the set of all {@link TypeDesignationBase type designations} that
	 * typify the {@link TaxonNameBase taxon names} belonging to <i>this</i> homotypical group
	 * (this includes either {@link NameTypeDesignation name type designations} or
	 * {@link SpecimenTypeDesignation specimen type designations}).
	 *
	 * @see	#getTypifiedNames()
	 * @see	#getNameTypeDesignations()
	 * @see	#getSpecimenTypeDesignations()
	 * @see	TaxonNameBase#getTypeDesignations()
	 */
	@Transient
	public Set<TypeDesignationBase> getTypeDesignations(){
		Set<TypeDesignationBase> result = new HashSet<TypeDesignationBase>();
		for (TaxonNameBase taxonName : typifiedNames){
			result.addAll(taxonName.getTypeDesignations());
		}
		return result;
	}
	
//	/** 
//	 * Returns the set of {@link SpecimenTypeDesignation specimen type designations} that
//	 * typify <i>this</i> homotypical group including the status of these designations.
//	 *
//	 * @see	#getTypifiedNames()
//	 */
//	@OneToMany
//	@Cascade({CascadeType.SAVE_UPDATE})
//	public Set<SpecimenTypeDesignation> getSpecimenTypeDesignations() {
//		return specimenTypeDesignations;
//	}
//	/** 
//	 * @see #getSpecimenTypeDesignations()
//	 */
//	protected void setSpecimenTypeDesignations(Set<SpecimenTypeDesignation> specimenTypeDesignations) {
//		this.specimenTypeDesignations = specimenTypeDesignations;
//	}	
//	/** 
//	 * Adds a new {@link SpecimenTypeDesignation specimen type designation} to the set
//	 * of specimen type designations assigned to <i>this</i> homotypical group and eventually
//	 * (with a boolean parameter) also to the corresponding set of each of the
//	 * {@link TaxonNameBase taxon names} belonging to <i>this</i> homotypical group.
//	 *
//	 * @param  specimenTypeDesignation	the specimen type designation to be added
//	 * @param  addToAllNames	the boolean flag indicating whether the addition will also
//	 * 							carried out for each taxon name
//	 * 
//	 * @see 			  		TaxonNameBase#getSpecimenTypeDesignations()
//	 * @see 			  		SpecimenTypeDesignation
//	 */
//	public void addSpecimenTypeDesignation(SpecimenTypeDesignation specimenTypeDesignation, boolean addToAllNames) {
//		if (specimenTypeDesignation != null){
//			specimenTypeDesignation.setHomotypicalGroup(this);
//			specimenTypeDesignations.add(specimenTypeDesignation);
//		}
//		if (addToAllNames){
//			for (TaxonNameBase taxonNameBase : this.typifiedNames){
//				taxonNameBase.addSpecimenTypeDesignation(specimenTypeDesignation);
//			}
//		}
//	}	
//	/** 
//	 * Removes one element from the set of {@link SpecimenTypeDesignation specimen type designations} assigned to the
//	 * {@link HomotypicalGroup homotypical group} to which this {@link TaxonNameBase taxon name} belongs.
//	 * The same element will be removed from the corresponding set of each of
//	 * the taxon names belonging to <i>this</i> homotypical group. Furthermore the
//	 * homotypical group attribute of the specimen type designation will be
//	 * nullified.
//	 *
//	 * @param  specimenTypeDesignation  the specimen type designation which should be deleted
//	 * @see     		  		#getSpecimenTypeDesignations()
//	 * @see    					#addSpecimenTypeDesignation(SpecimenTypeDesignation, boolean)
//	 * @see     		  		TaxonNameBase#removeSpecimenTypeDesignation(SpecimenTypeDesignation)
//	 * @see     		  		SpecimenTypeDesignation#getHomotypicalGroup()
//	 */
//	public void removeSpecimenTypeDesignation(SpecimenTypeDesignation specimenTypeDesignation) {
//		if (specimenTypeDesignation != null){
//			specimenTypeDesignation.setHomotypicalGroup(null);
//			specimenTypeDesignations.remove(specimenTypeDesignation);
//		}
//		for (TaxonNameBase taxonNameBase : this.typifiedNames){
//			taxonNameBase.removeSpecimenTypeDesignation(specimenTypeDesignation);
//		}
//	}	

	
//	/** 
//	 * Returns the set of {@link NameTypeDesignation name type designations} that
//	 * typify <i>this</i> homotypical group including the status of these designations.
//	 *
//	 * @see	#getTypifiedNames()
//	 */
//	@OneToMany
//	@Cascade({CascadeType.SAVE_UPDATE})
//	public Set<NameTypeDesignation> getNameTypeDesignations() {
//		return nameTypeDesignations;
//	}
//	/** 
//	 * @see #getNameTypeDesignations()
//	 */
//	protected void setNameTypeDesignations(Set<NameTypeDesignation> nameTypeDesignations) {
//		this.nameTypeDesignations = nameTypeDesignations;
//	}	
//	/** 
//	 * Adds a new {@link NameTypeDesignation name type designation} to the set
//	 * of name type designations assigned to <i>this</i> homotypical group and eventually
//	 * (with a boolean parameter) also to the corresponding set of each of the
//	 * {@link TaxonNameBase taxon names} belonging to <i>this</i> homotypical group.
//	 *
//	 * @param  nameTypeDesignation	the name type designation to be added
//	 * @param  addToAllNames	the boolean flag indicating whether the addition will also
//	 * 							carried out for each taxon name
//	 * 
//	 * @see 			  		TaxonNameBase#getNameTypeDesignations()
//	 * @see 			  		NameTypeDesignation
//	 */
//	public void addNameTypeDesignation(NameTypeDesignation nameTypeDesignation, boolean addToAllNames) {
//		if (nameTypeDesignation != null){
//			nameTypeDesignation.setHomotypicalGroup(this);
//			nameTypeDesignations.add(nameTypeDesignation);
//		}
//		if (addToAllNames){
//			for (TaxonNameBase taxonNameBase : this.typifiedNames){
//				taxonNameBase.addNameTypeDesignation(nameTypeDesignation);
//			}
//		}
//	}	
//	/** 
//	 * Removes one element from the set of {@link NameTypeDesignation name type designations} assigned to the
//	 * {@link HomotypicalGroup homotypical group} to which this {@link TaxonNameBase taxon name} belongs.
//	 * The same element will be removed from the corresponding set of each of
//	 * the taxon names belonging to <i>this</i> homotypical group. Furthermore the
//	 * homotypical group attribute of the name type designation will be
//	 * nullified.
//	 *
//	 * @param  nameTypeDesignation  the name type designation which should be deleted
//	 * @see     		  		#getNameTypeDesignations()
//	 * @see    					#addNameTypeDesignation(NameTypeDesignation, boolean)
//	 * @see     		  		TaxonNameBase#removeNameTypeDesignation(NameTypeDesignation)
//	 * @see     		  		NameTypeDesignation#getHomotypicalGroup()
//	 */
//	public void removeNameTypeDesignation(NameTypeDesignation nameTypeDesignation) {
//		if (nameTypeDesignation != null){
//			nameTypeDesignation.setHomotypicalGroup(null);
//			nameTypeDesignations.remove(nameTypeDesignation);
//		}
//		for (TaxonNameBase taxonNameBase : this.typifiedNames){
//			taxonNameBase.removeNameTypeDesignation(nameTypeDesignation);
//		}
//	}	
	
	
	/**
	 * Retrieves the ordered list (depending on the date of publication) of
	 * {@link taxon.Synonym synonyms} (according to a given reference)
	 * the {@link TaxonNameBase taxon names} of which belong to <i>this</i> homotypical group.
	 * If other names are part of <i>this</i> group that are not considered synonyms
	 * according to the respective reference, then they will not be included in
	 * the result set.
	 * 
	 * @param  sec	the reference whose treatment is to be considered
	 * @return		the ordered list of synonyms
	 * @see			TaxonNameBase#getSynonyms()
	 * @see			TaxonNameBase#getTaxa()
	 * @see			taxon.Synonym
	 */
	@Transient
	public List<Synonym> getSynonymsInGroup(ReferenceBase sec){
		List<Synonym> result = new ArrayList();
		for (TaxonNameBase<?, ?>n : this.getTypifiedNames()){
			for (Synonym s:n.getSynonyms()){
				if ( (s.getSec() == null && sec == null) ||
						s.getSec().equals(sec)){
					result.add(s);
				}
			}
		}
		Collections.sort(result, new TaxonComparator());
		return result;
	}
}
