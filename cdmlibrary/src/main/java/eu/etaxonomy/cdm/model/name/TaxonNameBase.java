/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
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
public abstract class TaxonNameBase extends IdentifiableEntity implements IReferencedEntity {
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
	private Set<ITypeDesignation> typeDesignations;
	private Set<NameRelationship> nameRelations;
	// name relations are bidirectional! Keep track of the inverse too
	private Set<NameRelationship> inverseNameRelations;
	private Set<NomenclaturalStatus> status;
	private Rank rank;
	//if set, the Reference.isNomenclaturallyRelevant flag should be set to true!
	private INomenclaturalReference nomenclaturalReference;
	private Set<TaxonNameBase> newCombinations;
	// bidrectional with newCombinations. Keep congruent
	private TaxonNameBase basionym;

	protected INameCacheStrategy cacheStrategy;

	
	// CONSTRUCTORS
	
	public TaxonNameBase(Rank rank) {
		this.setRank(rank);
	}

	
	// properties
	public String getNameCache() {
		return nameCache;
	}
	public void setNameCache(String nameCache) {
		this.nameCache = nameCache;
	}



	@OneToMany
	public Set<ITypeDesignation> getTypeDesignations() {
		return typeDesignations;
	}
	protected void setTypeDesignations(Set<ITypeDesignation> typeDesignations) {
		this.typeDesignations = typeDesignations;
	}
	public void addTypeDesignation(ITypeDesignation typeDesignation) {
		this.typeDesignations.add(typeDesignation);
	}
	public void removeTypeDesignation(ITypeDesignation typeDesignation) {
		this.typeDesignations.remove(typeDesignation);
	}



	@OneToMany
	public Set<NameRelationship> getNameRelations() {
		return nameRelations;
	}
	protected void setNameRelations(Set<NameRelationship> nameRelations) {
		this.nameRelations = nameRelations;
	}


	@OneToMany
	public Set<NameRelationship> getInverseNameRelations() {
		return inverseNameRelations;
	}
	protected void setInverseNameRelations(Set<NameRelationship> inverseNameRelations) {
		this.inverseNameRelations = inverseNameRelations;
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

	

	@OneToMany
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
	
	public Rank getRank(){
		return this.rank;
	}

	/**
	 * 
	 * @param rank    rank
	 */
	public void setRank(Rank rank){
		this.rank = rank;
	}

	public INomenclaturalReference getNomenclaturalReference(){
		return this.nomenclaturalReference;
	}

	/**
	 * 
	 * @param nomenclaturalReference    nomenclaturalReference
	 */
	public void setNomenclaturalReference(INomenclaturalReference nomenclaturalReference){
		this.nomenclaturalReference = nomenclaturalReference;
	}


	public String getName(){
		if (nameCache == null){ 
			return cacheStrategy.getNameCache(this);
		}else{
			return nameCache;
		}
	}

	/**
	 * 
	 * @param name    name
	 */
	public void setName(String name){
		this.nameCache = name;
	}

	public String getAppendedPhrase(){
		return this.appendedPhrase;
	}

	/**
	 * 
	 * @param appendedPhrase    appendedPhrase
	 */
	public void setAppendedPhrase(String appendedPhrase){
		this.appendedPhrase = appendedPhrase;
	}

	public String getNomenclaturalMicroReference(){
		return this.nomenclaturalMicroReference;
	}

	/**
	 * 
	 * @param nomenclaturalMicroReference    nomenclaturalMicroReference
	 */
	public void setNomenclaturalMicroReference(String nomenclaturalMicroReference){
		this.nomenclaturalMicroReference = nomenclaturalMicroReference;
	}

	public boolean getHasProblem(){
		return this.hasProblem;
	}

	/**
	 * 
	 * @param hasProblem    hasProblem
	 */
	public void setHasProblem(boolean hasProblem){
		this.hasProblem = hasProblem;
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

}