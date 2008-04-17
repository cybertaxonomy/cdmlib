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
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.strategy.INameCacheStrategy;
import eu.etaxonomy.cdm.strategy.ITaxonNameParser;

import java.util.*;

import javax.persistence.*;

/**
 * The upmost (abstract) class for scientific taxon names regardless of any
 * particular nomenclatural code. The scientific name including author strings and
 * maybe year is stored in IdentifiableEntity.titleCache
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:57
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class TaxonNameBase<T extends TaxonNameBase> extends IdentifiableEntity<TaxonNameBase> implements IReferencedEntity {
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
	private Set<NameRelationship> relationsFromThisName = new HashSet();
	private Set<NameRelationship> relationsToThisName = new HashSet();
	private Set<NomenclaturalStatus> status = new HashSet();
	private Rank rank;
	//if set, the Reference.isNomenclaturallyRelevant flag should be set to true!
	private INomenclaturalReference nomenclaturalReference;

	
	//TODO 
	protected boolean protectedNameCache;

	protected INameCacheStrategy cacheStrategy;

	static protected ITaxonNameParser nameParser;
	
//	/**
//	 * Returns a TaxonNameBase instance 
//	 * @param fullName
//	 */
//	abstract public static TaxonNameBase PARSED_NAME(String fullName);
	
// ************* CONSTRUCTORS *************/	
	public TaxonNameBase() {
		super();
	}
	public TaxonNameBase(Rank rank) {
		super();
		this.setRank(rank);
	}
	
//********* METHODS **************************************/
	
	protected String generateNameCache(){
		if (cacheStrategy == null){
			logger.warn("No CacheStrategy defined for taxonName: " + this.toString());
			return null;
		}else{
			return cacheStrategy.getNameCache(this);
		}
	}
	
	public String getNameCache() {
		if (protectedNameCache){
			return this.nameCache;			
		}
		// is title dirty, i.e. equal NULL?
		if (nameCache == null){
			this.nameCache = generateNameCache();
		}
		return nameCache;
	}

	public void setNameCache(String nameCache){
		this.nameCache = nameCache;
		// TODO this.setProtectedNameCache(true);
	}
//TODO 
//	public void setTitleCache(String titleCache, boolean protectCache){
//		this.titleCache = titleCache;
//		this.setProtectedTitleCache(protectCache);
//	}
	

	@Transient
	public abstract boolean isCodeCompliant();
	

	@Transient
	public Set<NameRelationship> getNameRelations() {
		Set<NameRelationship> rels = new HashSet<NameRelationship>();
		rels.addAll(getRelationsFromThisName());
		rels.addAll(getRelationsToThisName());
		return rels;
	}
	/**
	 * Add a name relationship to both names involved
	 * @param rel
	 */
	public void addRelationshipToName(TaxonNameBase toName, NameRelationshipType type, String ruleConsidered){
		NameRelationship rel = new NameRelationship(toName, this, type, ruleConsidered);
	}
	public void addRelationshipFromName(TaxonNameBase fromName, NameRelationshipType type, String ruleConsidered){
		NameRelationship rel = new NameRelationship(this, fromName, type, ruleConsidered);
	}
	protected void addNameRelationship(NameRelationship rel) {
		if (rel!=null && rel.getToName().equals(this)){
			this.relationsToThisName.add(rel);
		}else if(rel!=null && rel.getFromName().equals(this)){
			this.relationsFromThisName.add(rel);			
		}else{
			//TODO: raise error???
		}
	}
	public void removeNameRelationship(NameRelationship nameRelation) {
		this.relationsToThisName.remove(nameRelation);
		this.relationsFromThisName.remove(nameRelation);
	}
	
	
	@OneToMany(mappedBy="fromName")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<NameRelationship> getRelationsFromThisName() {
		return relationsFromThisName;
	}
	private void setRelationsFromThisName(Set<NameRelationship> relationsFromThisName) {
		this.relationsFromThisName = relationsFromThisName;
	}
	
	@OneToMany(mappedBy="toName")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<NameRelationship> getRelationsToThisName() {
		return relationsToThisName;
	}
	private void setRelationsToThisName(Set<NameRelationship> relationsToThisName) {
		this.relationsToThisName = relationsToThisName;
	}

	

	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<NomenclaturalStatus> getStatus() {
		return status;
	}
	protected void setStatus(Set<NomenclaturalStatus> nomStatus) {
		this.status = nomStatus;
	}
	public void addStatus(NomenclaturalStatus nomStatus) {
		this.status.add(nomStatus);
	}
	public void removeStatus(NomenclaturalStatus nomStatus) {
		this.status.remove(nomStatus);
	}


	@Transient
	public T getBasionym(){
		//TODO: pick the right name relationships...
		return null;
	}
	public void setBasionym(T basionym){
		setBasionym(basionym, null);
	}
	public void setBasionym(T basionym, String ruleConsidered){
		basionym.addRelationshipToName(this, NameRelationshipType.BASIONYM(), null);
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
	//@Cascade({CascadeType.SAVE_UPDATE})
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
	//TODO @Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	@Cascade(CascadeType.SAVE_UPDATE)
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
	public String getReferenceYear(){
		if (this.getNomenclaturalReference() != null ){
			return this.getNomenclaturalReference().getYear();
		}else{
			return null;
		}
	}

	
	/**
	 * Return a set of taxa that use this name
	 * @return
	 */
	@Transient
	public Set<Taxon> getTaxa(){
		// TODO: implement this method via bidirectional TaxonBase-NameBase relation or use a DAO instead
		return null;
	}
	/**
	 * Return a set of synonyms that use this name
	 * @return
	 */
	@Transient
	public Set<Synonym> getSynonyms(){
		
		// TODO: implement this method via bidirectional TaxonBase-NameBase relation or use a DAO instead
		return null;
	}
	
	@Transient
	public Set<SpecimenTypeDesignation> getSpecimenTypeDesignations() {
		return this.getHomotypicalGroup().getTypeDesignations();
	}
	
	// Rank comparison shortcuts
	@Transient
	public boolean isSupraGeneric() {
		return getRank().isSupraGeneric();
	}
	@Transient
	public boolean isGenus() {
		return getRank().isGenus();
	}
	@Transient
	public boolean isInfraGeneric() {
		return getRank().isInfraGeneric();
	}
	@Transient
	public boolean isSpecies() {
		return getRank().isSpecies();
	}
	@Transient
	public boolean isInfraSpecific() {
		return getRank().isInfraSpecific();
	}
	@Transient
	public boolean isSupraGeneric(Rank rank) {
		return getRank().isHigher(rank);
	}
}