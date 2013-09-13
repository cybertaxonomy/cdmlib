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
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.TaxonComparator;


/**
 * The homotypical group class represents a set of {@link TaxonNameBase taxon names} associated
 * on the base of their typifications. Since it can be asserted that two taxon
 * names are typified by the same type without mentioning the type itself, even
 * taxon names without explicit {@link TypeDesignationBase type designation} can belong
 * to an homotypical group.<BR>
 * Taxon names belonging to an homotypical group and the taxon names or
 * {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnit specimens} used as types for their
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
@Audited
public class HomotypicalGroup extends AnnotatableEntity {
	private static final Logger logger = Logger.getLogger(HomotypicalGroup.class);

	@XmlElementWrapper(name = "TypifiedNames")
	@XmlElement(name = "TypifiedName")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@OneToMany(mappedBy="homotypicalGroup", fetch=FetchType.LAZY)
	protected Set<TaxonNameBase> typifiedNames = new HashSet<TaxonNameBase>();

// ******************** static methods **************************************/
	/** 
	 * Creates a new homotypical group instance with an empty set of typified
	 * {@link TaxonNameBase taxon names}.
	 * 
	 * @see #HomotypicalGroup()
	 */
	public static HomotypicalGroup NewInstance(){
		return new HomotypicalGroup();
	}
	
	
//********************** CONSTRUCTOR ********************************************/
	
	/** 
	 * Class constructor: creates a new homotypical group instance with an
	 * empty set of typified {@link TaxonNameBase taxon names}.
	 */
	public HomotypicalGroup() {
		super();
	}

// ********************** GETTER/SETTER/ADDER/REMOVER ********************************/
		
	/** 
	 * Returns the set of {@link TaxonNameBase taxon names} that belong to <i>this</i> homotypical group.
	 *
	 * @see	#getSpecimenTypeDesignations()
	 */
	public Set<TaxonNameBase> getTypifiedNames() {
		return typifiedNames;
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
			typifiedNames.add(typifiedName);
			typifiedName.setHomotypicalGroup(this);
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
		HomotypicalGroup newHomotypicalGroup = HomotypicalGroup.NewInstance();
		typifiedName.setHomotypicalGroup(newHomotypicalGroup);
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
	 * @deprecated synonyms should not depend on the sec. Therefore this method will be removed in 
	 * version 3.1 or higher. Use {@link Taxon#getSynonymsInGroup(HomotypicalGroup)} instead. But be
	 * aware that the semantics is slightly different.
	 * @param  sec	the reference whose treatment is to be considered
	 * @return		the ordered list of synonyms
	 * @see			TaxonNameBase#getSynonyms()
	 * @see			TaxonNameBase#getTaxa()
	 * @see			taxon.Synonym
	 */
	@Deprecated
	public List<Synonym> getSynonymsInGroup(Reference sec){
		List<Synonym> result = new ArrayList<Synonym>();
		for (TaxonNameBase<?, ?>name : this.getTypifiedNames()){
			for (Synonym synonym : name.getSynonyms()){
				if ( (synonym.getSec() == null && sec == null) ||
						synonym.getSec() != null && synonym.getSec().equals(sec)){
					result.add(synonym);
				}
			}
		}
		Collections.sort(result, new TaxonComparator());
		return result;
	}
	
    /**
     * Creates a basionym relationship to all other names in this names homotypical
     * group. 
     * 
     * @see HomotypicalGroup.setGroupBasionym(TaxonNameBase basionymName)
     *
     * @param basionymName
     * @throws IllegalArgumentException if basionymName is not member in this homotypical group
     */
	public void setGroupBasionym(TaxonNameBase basionymName) throws IllegalArgumentException{
    	setGroupBasionym(basionymName, null, null, null);
    }	
    
	public void setGroupBasionym(TaxonNameBase basionymName, Reference citation, String microCitation, String ruleConsidered) 
    			throws IllegalArgumentException {
    	if (! typifiedNames.contains(basionymName)){
        	throw new IllegalArgumentException("Name to be set as basionym/original combination must be part of the homotypical group but is not");
        }
        if (typifiedNames.size() < 2){return;}
//        
    	//Add new relations
        for (TaxonNameBase name : typifiedNames) {
    		if (!name.equals(basionymName)) {
		    	name.addRelationshipFromName(basionymName, NameRelationshipType.BASIONYM(), citation, microCitation, ruleConsidered);
			}
    	}
    }
    
    /**
     * Removes all basionym relationships between basionymName and any other name 
     * in its homotypic group
     *
     * @param basionymName
     */
     public static void removeGroupBasionym(TaxonNameBase basionymName) {
    	 HomotypicalGroup homotypicalGroup = basionymName.getHomotypicalGroup();
         Set<NameRelationship> relations = basionymName.getRelationsFromThisName();
         Set<NameRelationship> removeRelations = new HashSet<NameRelationship>();
        
         for (NameRelationship relation : relations) {
                
                 // If this is a basionym relation, and toName is in the homotypical group,
                 //      remove the relationship.
                 if (relation.getType().isBasionymRelation() &&
                                 relation.getToName().getHomotypicalGroup().equals(homotypicalGroup)) {
                         removeRelations.add(relation);
                 }
         }
         
          // Removing relations from a set through which we are iterating causes a
          //      ConcurrentModificationException. Therefore, we delete the targeted
          //      relations in a second step.
          for (NameRelationship relation : removeRelations) {
                  basionymName.removeNameRelationship(relation);
          }
     }
	
	
	/**
	 * Returns all taxon names in the homotypical group that do not have an 'is_basionym_for' (zool.: 'is_original_combination_for') 
	 * or a replaced synonym relationship.
	 * @return
	 */
	@Transient
	public Set<TaxonNameBase> getUnrelatedNames(){
		Set<NameRelationship> set = getBasionymOrReplacedSynonymRelations(true, true);
		Set<TaxonNameBase> result = new HashSet<TaxonNameBase>();
		result.addAll(this.getTypifiedNames());
		for (NameRelationship nameRelationship : set){
			result.remove(nameRelationship.getFromName());
			result.remove(nameRelationship.getToName());
		}
		return result;
	}	
	
	/**
	 * Returns all taxon names in the homotypical group that are new combinations (have a basionym/original combination 
	 * or a replaced synonym).
	 * @return
	 */
	@Transient
	public Set<TaxonNameBase> getNewCombinations(){
		Set<NameRelationship> set = getBasionymOrReplacedSynonymRelations(true, true);
		Set<TaxonNameBase> result = new HashSet<TaxonNameBase>();
		for (NameRelationship nameRelationship : set){
			result.add(nameRelationship.getToName());
		}
		return result;
	}	

	
	
	/**
	 * Returns all taxon names in the homotypical group that have an 'is_basionym_for' (zool.: 'is_original_combination_for') 
	 * or a replaced synonym relationship.
	 * @return
	 */
	@Transient
	public Set<TaxonNameBase> getBasionymsOrReplacedSynonyms(){
		Set<NameRelationship> set = getBasionymOrReplacedSynonymRelations(true, true);
		Set<TaxonNameBase> result = new HashSet<TaxonNameBase>();
		for (NameRelationship nameRelationship : set){
			result.add(nameRelationship.getFromName());
		}
		return result;
	}	
	
	/**
	 * Returns all taxon names in the homotypical group that have a 'is_basionym_for' (zool.: 'is_original_combination_for') relationship.
	 * @return
	 */
	@Transient
	public Set<TaxonNameBase> getBasionyms(){
		Set<NameRelationship> set = getBasionymOrReplacedSynonymRelations(true, false);
		Set<TaxonNameBase> result = new HashSet<TaxonNameBase>();
		for (NameRelationship nameRelationship : set){
			result.add(nameRelationship.getFromName());
		}
		return result;
	}

	/**
	 * Returns all taxon names in the homotypical group that have a 'is_replaced_synonym_for' relationship.
	 * @return
	 */
	@Transient
	public Set<TaxonNameBase> getReplacedSynonym(){
		Set<NameRelationship> set = getBasionymOrReplacedSynonymRelations(false, true);
		Set<TaxonNameBase> result = new HashSet<TaxonNameBase>();
		for (NameRelationship nameRelationship : set){
			result.add(nameRelationship.getFromName());
		}
		return result;
	}
	
	/**
	 * Returns the name relationships that represent either a basionym (original combination) relationship or
	 * a replaced synonym relationship.  
	 * @return
	 */
	@Transient
	public Set<NameRelationship> getBasionymAndReplacedSynonymRelations(){
		return getBasionymOrReplacedSynonymRelations(true, true);
	}
	
	/**
	 * Computes all basionym and replaced synonym relationships between names in this group.
	 * If <code>doBasionym</code> is <code>false</code> basionym relationships are excluded.
	 * If <code>doReplacedSynonym</code> is <code>false</code> replaced synonym relationships are excluded.
	 * @param doBasionym
	 * @param doReplacedSynonym
	 * @return
	 */
	@Transient
	private Set<NameRelationship> getBasionymOrReplacedSynonymRelations(boolean doBasionym, boolean doReplacedSynonym){
		Set<NameRelationship> result = new HashSet<NameRelationship>(); 
		Set<TaxonNameBase> names = this.getTypifiedNames();
		if (names.size() > 1){
			for (TaxonNameBase name : names){
				Set nameRels = name.getNameRelations();
				//TODO make getNameRelations generic
				for (Object obj : nameRels){
					NameRelationship nameRel = (NameRelationship)obj;
					NameRelationshipType type = nameRel.getType();
					if ( type.isBasionymRelation() && doBasionym){
						if (testRelatedNameInThisGroup(nameRel)){
							result.add(nameRel);
						}else{
							logger.warn("Name has basionym relation to a name that is not in the same homotypical group");
						}
					}else if (type.isReplacedSynonymRelation() && doReplacedSynonym)  {
						if (testRelatedNameInThisGroup(nameRel)){
							result.add(nameRel);
						}else{
							logger.warn("Name has replaced synonym relation to a name that is not in the same homotypical group");
						}
					}
				}
			}
		}
		return result;
	}
	
	private boolean testRelatedNameInThisGroup(NameRelationship nameRel){
		TaxonNameBase toName = nameRel.getToName();
		return (this.getTypifiedNames().contains(toName));
	}
	
	private boolean isBasionymOrRepSynRel(NameRelationshipType relType){
		if (relType == null){
			throw new IllegalArgumentException("NameRelationshipType should never be null");
		}else if (relType.equals(NameRelationshipType.BASIONYM())) {
			return true;
		}else if (relType.equals(NameRelationshipType.REPLACED_SYNONYM())){
			return true;
		}else{
			return false;
		}
	}
	
	
}
