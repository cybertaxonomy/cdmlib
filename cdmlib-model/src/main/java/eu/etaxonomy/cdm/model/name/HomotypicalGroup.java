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


/**
 * The homotypical group class represents a set of {@link TaxonName taxon names} associated
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
	private static final long serialVersionUID = -2308347613205551766L;

	private static final Logger logger = Logger.getLogger(HomotypicalGroup.class);

	@XmlElementWrapper(name = "TypifiedNames")
	@XmlElement(name = "TypifiedName")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@OneToMany(mappedBy="homotypicalGroup", fetch=FetchType.LAZY)
	protected Set<TaxonName> typifiedNames = new HashSet<>();

// ******************** static methods **************************************/
	/**
	 * Creates a new homotypical group instance with an empty set of typified
	 * {@link TaxonName taxon names}.
	 *
	 * @see #HomotypicalGroup()
	 */
	public static HomotypicalGroup NewInstance(){
		return new HomotypicalGroup();
	}


//********************** CONSTRUCTOR ********************************************/

	/**
	 * Class constructor: creates a new homotypical group instance with an
	 * empty set of typified {@link TaxonName taxon names}.
	 */
	protected HomotypicalGroup() {
		super();
	}

// ********************** GETTER/SETTER/ADDER/REMOVER ********************************/

	/**
	 * Returns the set of {@link TaxonName taxon names} that belong to <i>this</i> homotypical group.
	 *
	 * @see	#getSpecimenTypeDesignations()
	 */
	public Set<TaxonName> getTypifiedNames() {
		return typifiedNames;
	}

	/**
	 * Adds a new {@link TaxonName taxon name} to the set of taxon names that belong
	 * to <i>this</i> homotypical group.
	 *
	 * @param  typifiedName  the taxon name to be added to <i>this</i> group
	 * @see 			  	 #getTypifiedNames()
	 * @see 			  	 #removeTypifiedName(TaxonName)
	 */
	public void addTypifiedName(TaxonName typifiedName) {
		if (typifiedName != null){
			typifiedNames.add(typifiedName);
			//if (typifiedName.getHomotypicalGroup() != null && !typifiedName.getHomotypicalGroup().equals(this))
			typifiedName.setHomotypicalGroup(this);
		}
	}

	/**
	 * @see #removeTypifiedName(TaxonName, boolean)
	 * @param typifiedName
	 */
	public void removeTypifiedName(TaxonName typifiedName) {
		removeTypifiedName(typifiedName, false);
	}


	/**
	 * Removes one element from the set of {@link TaxonName taxon names}
	 * that belong to <i>this</i> homotypical group.
	 *
	 * @param  typifiedName	the taxon name which should be removed from the corresponding set
	 * @param  removeGroup  if <code>true</code> the typified name is given a new
	 * 						homotypical group
	 * @see    #addTypifiedName(TaxonName)
	 */
	public void removeTypifiedName(TaxonName typifiedName, boolean removeGroup) {
		if (removeGroup){
			HomotypicalGroup newHomotypicalGroup = HomotypicalGroup.NewInstance();
			typifiedName.setHomotypicalGroup(newHomotypicalGroup);
		}

		typifiedNames.remove(typifiedName);
	}

	/**
	 * Merges the typified {@link TaxonName taxon names} from one homotypical group into
	 * the set of typified taxon names of <i>this</i> homotypical group.
	 *
	 * @param	homotypicalGroupToMerge the homotypical group the typified names of which
	 * 									are to be transferred to <i>this</i> homotypical group
	 */
	public void merge(HomotypicalGroup homotypicalGroupToMerge){
		if (homotypicalGroupToMerge != null){
			Set<TaxonName> typifiedNames = new HashSet<>();
			typifiedNames.addAll(homotypicalGroupToMerge.getTypifiedNames());
			for (TaxonName typifiedName: typifiedNames){
				this.addTypifiedName(typifiedName);
			}
		}
	}


	/**
	 * Returns the set of {@link SpecimenTypeDesignation specimen type designations} that
	 * typify the {@link TaxonName taxon names} belonging to <i>this</i> homotypical group
	 * including the status of these designations.
	 *
	 * @see	#getTypifiedNames()
	 * @see	#getNameTypeDesignations()
	 * @see	#getTypeDesignations()
	 * @see	TaxonName#getSpecimenTypeDesignations()
	 */
	@Transient
	public Set<SpecimenTypeDesignation> getSpecimenTypeDesignations(){
		Set<SpecimenTypeDesignation> result = new HashSet<SpecimenTypeDesignation>();
		for (TaxonName taxonName : typifiedNames){
			result.addAll(taxonName.getSpecimenTypeDesignations());
		}
		return result;
	}

	/**
	 * Returns the set of {@link NameTypeDesignation name type designations} that
	 * typify the {@link TaxonName taxon names} belonging to <i>this</i> homotypical group
	 * including the status of these designations.
	 *
	 * @see	#getTypifiedNames()
	 * @see	#getSpecimenTypeDesignations()
	 * @see	#getTypeDesignations()
	 * @see	TaxonName#getNameTypeDesignations()
	 */
	@Transient
	public Set<NameTypeDesignation> getNameTypeDesignations(){
		Set<NameTypeDesignation> result = new HashSet<NameTypeDesignation>();
		for (TaxonName taxonName : typifiedNames){
			result.addAll(taxonName.getNameTypeDesignations());
		}
		return result;
	}


	/**
	 * Returns the set of all {@link TypeDesignationBase type designations} that
	 * typify the {@link TaxonName taxon names} belonging to <i>this</i> homotypical group
	 * (this includes either {@link NameTypeDesignation name type designations} or
	 * {@link SpecimenTypeDesignation specimen type designations}).
	 *
	 * @see	#getTypifiedNames()
	 * @see	#getNameTypeDesignations()
	 * @see	#getSpecimenTypeDesignations()
	 * @see	TaxonName#getTypeDesignations()
	 */
	@Transient
	public Set<TypeDesignationBase> getTypeDesignations(){
		Set<TypeDesignationBase> result = new HashSet<TypeDesignationBase>();
		for (TaxonName taxonName : typifiedNames){
			result.addAll(taxonName.getTypeDesignations());
		}
		return result;
	}


    /**
     * Creates a basionym relationship to all other names in this names homotypical
     * group.
     *
     * @see HomotypicalGroup.setGroupBasionym(TaxonName basionymName)
     *
     * @param basionymName
     * @throws IllegalArgumentException if basionymName is not member in this homotypical group
     */
	@Transient
	public void setGroupBasionym(TaxonName basionymName) throws IllegalArgumentException{
    	setGroupBasionym(basionymName, null, null, null);
    }

	public void setGroupBasionym(TaxonName basionymName, Reference citation, String microCitation, String ruleConsidered)
    			throws IllegalArgumentException {
    	if (! typifiedNames.contains(basionymName)){
        	throw new IllegalArgumentException("Name to be set as basionym/original combination must be part of the homotypical group but is not");
        }
        if (typifiedNames.size() < 2){return;}
//
    	//Add new relations
        Set<TaxonName> typified = new HashSet<>();
        for (TaxonName name : typifiedNames){
        	typified.add(name);
        }
        for (TaxonName name : typified) {
    		if (!name.equals(basionymName)) {
		    	name.addRelationshipFromName(basionymName, NameRelationshipType.BASIONYM(), citation, microCitation, ruleConsidered);
			}
    	}
        typifiedNames= typified;
    }



    /**
     * Removes all basionym relationships between basionymName and any other name
     * in it's homotypic group
     *
     * @param basionymName
     */
     public static void removeGroupBasionym(TaxonName basionymName) {
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

//     /**
//     * TODO
//     * This method tries to set basionym(s) for a homotypical group
//     * Rule: Basionym must not have basionym author and not protectedAuthorshipCache
//     *       Others must have basionymAuthor
//     *       BasionymAuhtors must have equal NomenclaturalTitleCache to basionym
//       *     last epithet must fit (be equal except for ending), otherwise we expect it to be replaced synonym
//       *     those names fitting to a basionym relationship must remove all other basionym (and replaced synonym?) relationships
//       * Open: All types of relationships to replaced synonyms
//     */
//    public void guessAndSetBasionym(){
//         Map<String, INonViralName> candidates = new HashMap<>();
//         for (TaxonName typifiedName : this.typifiedNames){
//             if (! typifiedName.protectedAuthorshipCache && nvn.getBasionymAuthorship() == null){
//                 candidates.add(typifiedName);
//             }
//         }
//         if (candidates.size() == 1){
//             for (TaxonName typifiedName : this.typifiedNames){
//                 removeGroupBasionym(typifiedName);
//             }
//             this.setGroupBasionym(candidates.iterator().next());
//         }
//
//     }


	/**
	 * Returns all taxon names in the homotypic group that do not have an
	 * 'is_basionym_for' (zool.: 'is_original_combination_for')
	 * or a replaced synonym relationship.
	 * @return
	 */
	@Transient
	public Set<TaxonName> getUnrelatedNames(){
		Set<NameRelationship> set = getBasionymOrReplacedSynonymRelations(true, true);
		Set<TaxonName> result = new HashSet<>();
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
	public Set<TaxonName> getNewCombinations(){
		Set<NameRelationship> set = getBasionymOrReplacedSynonymRelations(true, true);
		Set<TaxonName> result = new HashSet<>();
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
	public Set<TaxonName> getBasionymsOrReplacedSynonyms(){
		Set<NameRelationship> set = getBasionymOrReplacedSynonymRelations(true, true);
		Set<TaxonName> result = new HashSet<>();
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
	public Set<TaxonName> getBasionyms(){
		Set<NameRelationship> set = getBasionymOrReplacedSynonymRelations(true, false);
		Set<TaxonName> result = new HashSet<>();
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
	public Set<TaxonName> getReplacedSynonym(){
		Set<NameRelationship> set = getBasionymOrReplacedSynonymRelations(false, true);
		Set<TaxonName> result = new HashSet<>();
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
		Set<TaxonName> names = this.getTypifiedNames();
		if (names.size() > 1){
			for (TaxonName name : names){
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
		TaxonName toName = nameRel.getToName();
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
