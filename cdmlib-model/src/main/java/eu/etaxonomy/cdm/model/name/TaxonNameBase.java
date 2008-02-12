/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.strategy.INameCacheStrategy;

import java.util.*;

import javax.persistence.*;

/**
 * The upmost (abstract) class for scientific taxon names regardless of the any
 * particular nomenclatural code. The scientific name including author strings and
 * maybe year is stored in IdentifiableEntity.titleCache
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:57
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class TaxonNameBase extends IdentifiableEntity<TaxonNameBase> implements IReferencedEntity {
	static Logger logger = Logger.getLogger(TaxonNameBase.class);
	//The scientific name without author strings and year
	private String nameCache;
	//Non-atomised addition to a name not ruled by a nomenclatural code
	private String appendedPhrase;
	//Details of the nomenclatural reference (protologue). These are mostly (implicitly) pages but can also be figures or
	//tables or any other element of a publication. {only if a nomenclatural reference exists}
	private String nomenclaturalMicroReference;
	//this flag will be set to true if the parseName method was unable to successfully parse the name
	private boolean hasProblem = false;
	protected Set<NameTypeDesignation> nameTypeDesignations  = new HashSet();
	private HomotypicalGroup homotypicalGroup = new HomotypicalGroup();
	private Set<NameRelationship> nameRelations = new HashSet();
	private Set<NomenclaturalStatus> status = new HashSet();
	private Rank rank;
	//if set, the Reference.isNomenclaturallyRelevant flag should be set to true!
	private INomenclaturalReference nomenclaturalReference;
	private Set<TaxonNameBase> newCombinations = new HashSet();
	// bidrectional with newCombinations. Keep congruent
	private TaxonNameBase basionym;

	protected INameCacheStrategy cacheStrategy;

	
	// CONSTRUCTORS	
	public TaxonNameBase() {
		super();
	}
	public TaxonNameBase(Rank rank) {
		super();
		this.setRank(rank);
	}

	
	public String getNameCache() {
		return nameCache;
	}
	public void setNameCache(String nameCache) {
		this.nameCache = nameCache;
	}


	@Transient
	public abstract boolean isCodeCompliant();
	

	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<NameRelationship> getNameRelations() {
		return nameRelations;
	}
	protected void setNameRelations(Set<NameRelationship> nameRelations) {
		this.nameRelations = nameRelations;
	}
	public void addNameRelation(NameRelationship nameRelation) {
		// checks whether this is a normal relation or an inverse one 
		// and adds it to the appropiate set
		//this.inverseNameRelations
		this.nameRelations.add(nameRelation);
	}
	public void removeNameRelation(NameRelationship nameRelation) {
		// this.inverseNameRelations
		this.nameRelations.remove(nameRelation);
	}
	
	
	@Transient
	public Set<NameRelationship> getIncomingNameRelations() {
		// FIXME: filter relations
		return nameRelations;
	}
	@Transient
	public Set<NameRelationship> getOutgoingNameRelations() {
		// FIXME: filter relations
		return nameRelations;
	}

	

	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<NomenclaturalStatus> getStatus() {
		return status;
	}
	protected void setStatus(Set<NomenclaturalStatus> status) {
		this.status = status;
	}
	public void addStatus(NomenclaturalStatus status) {
		this.status.add(status);
	}
	public void removeStatus(NomenclaturalStatus status) {
		this.status.remove(status);
	}



	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TaxonNameBase> getNewCombinations() {
		return newCombinations;
	}
	protected void setNewCombinations(Set<TaxonNameBase> newCombinations) {
		this.newCombinations = newCombinations;
	}
	public void addNewCombination(TaxonNameBase newCombination) {
		// TODO: add basionym relation too!
		this.newCombinations.add(newCombination);
	}
	public void removeNewCombination(TaxonNameBase newCombination) {
		this.newCombinations.remove(newCombination);
	}


	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public TaxonNameBase getBasionym(){
		return this.basionym;
	}
	public void setBasionym(TaxonNameBase basionym){
		// TODO: add newCombination relation too!
		this.basionym = basionym;
	}



	//TODO for PROTOTYPE
	@Transient
	public INameCacheStrategy getCacheStrategy() {
		return cacheStrategy;
	}
	public void setCacheStrategy(INameCacheStrategy cacheStrategy) {
		this.cacheStrategy = cacheStrategy;
	}
	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Rank getRank(){
		return this.rank;
	}
	public void setRank(Rank rank){
		this.rank = rank;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public ReferenceBase getNomenclaturalReference(){
		return (ReferenceBase) this.nomenclaturalReference;
	}
	public void setNomenclaturalReference(INomenclaturalReference nomenclaturalReference){
		this.nomenclaturalReference = nomenclaturalReference;
	}


	public String getAppendedPhrase(){
		return this.appendedPhrase;
	}
	public void setAppendedPhrase(String appendedPhrase){
		this.appendedPhrase = appendedPhrase;
	}

	public String getNomenclaturalMicroReference(){
		return this.nomenclaturalMicroReference;
	}
	public void setNomenclaturalMicroReference(String nomenclaturalMicroReference){
		this.nomenclaturalMicroReference = nomenclaturalMicroReference;
	}

	public boolean getHasProblem(){
		return this.hasProblem;
	}
	public void setHasProblem(boolean hasProblem){
		this.hasProblem = hasProblem;
	}


	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	public Set<NameTypeDesignation> getNameTypeDesignations() {
		return nameTypeDesignations;
	}
	protected void setNameTypeDesignations(Set<NameTypeDesignation> nameTypeDesignations) {
		this.nameTypeDesignations = nameTypeDesignations;
	}
	
	public void addTypeDesignation(TaxonNameBase typeSpecies, ReferenceBase citation, String citationMicroReference, String originalNameString, boolean isRejectedType, boolean isConservedType) {
		NameTypeDesignation td = new NameTypeDesignation(this, typeSpecies, citation, citationMicroReference, originalNameString, isRejectedType, isConservedType);
	}
	public void addTypeDesignation(Specimen typeSpecimen, TypeDesignationStatus status, ReferenceBase citation, String citationMicroReference, String originalNameString) {
		this.homotypicalGroup.addTypeDesignation(typeSpecimen, status,  citation, citationMicroReference, originalNameString);
	}
	public void removeTypeDesignation(NameTypeDesignation typeDesignation) {
		this.nameTypeDesignations.remove(typeDesignation);
	}
	public void removeTypeDesignation(SpecimenTypeDesignation typeDesignation) {
		this.homotypicalGroup.removeTypeDesignation(typeDesignation);
	}


	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public HomotypicalGroup getHomotypicalGroup() {
		return homotypicalGroup;
	}
	public void setHomotypicalGroup(HomotypicalGroup newHomotypicalGroup) {
		if(this.homotypicalGroup == newHomotypicalGroup) return;
		if (homotypicalGroup != null) { 
			homotypicalGroup.typifiedNames.remove(this);
		}
		if (newHomotypicalGroup!= null) { 
			newHomotypicalGroup.typifiedNames.add(this);
		}
		this.homotypicalGroup = newHomotypicalGroup;		
	}

	@Transient
	public StrictReferenceBase getCitation(){
		return null;
	}

	@Transient
	public String getCitationString(){
		return null;
	}

	@Transient
	public String[] getProblems(){
		return null;
	}

	/**
	 * returns year of according nomenclatural reference, null if nomenclatural
	 * reference does not exist
	 */
	@Transient
	public String getYear(){
		return "";
	}

	/**
	 * 
	 * @param fullname    fullname
	 */
	public boolean parseName(String fullname){
		return false;
	}	
	
	/**
	 * Return a set of taxa that use this name
	 * @return
	 */
	public Set<Taxon> getTaxa(){
		// TODO: implement this method via bidirectional TaxonBase-NameBase relation or use a DAO instead
		return null;
	}
	/**
	 * Return a set of synonyms that use this name
	 * @return
	 */
	public Set<Synonym> getSynonyms(){
		// TODO: implement this method via bidirectional TaxonBase-NameBase relation or use a DAO instead
		return null;
	}
}