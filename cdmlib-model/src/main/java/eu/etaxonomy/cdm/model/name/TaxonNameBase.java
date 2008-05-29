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
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.IParsable;
import eu.etaxonomy.cdm.model.common.IRelated;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import eu.etaxonomy.cdm.model.common.RelationshipBase;


import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.collection.PersistentSet;

import eu.etaxonomy.cdm.strategy.cache.INameCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.INonViralNameCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.NameCacheStrategyBase;


import java.lang.reflect.Method;
import java.util.*;

import javax.persistence.*;

/**
 * The upmost (abstract) class for scientific taxon names regardless of any
 * particular nomenclatural code. The scientific name including author strings and
 * maybe year can be stored as a string in the inherited {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.
 * The scientific name string without author strings and year can be stored in the {@link #getNameCache() nameCache} attribute.
 * The scientific taxon name does not depend on the use made of it
 * in a publication or a treatment ({@link taxon.TaxonBase taxon concept respectively potential taxon})
 * as an {@link taxon.Taxon "accepted" respectively "correct" (taxon) name}
 * or as a {@link taxon.Synonym synonym}.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:57
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class TaxonNameBase<T extends TaxonNameBase, S extends INameCacheStrategy> extends IdentifiableEntity<TaxonNameBase> implements IReferencedEntity, IParsable, IRelated {
	static Logger logger = Logger.getLogger(TaxonNameBase.class);
	//Non-atomised addition to a name not ruled by a nomenclatural code
	private String appendedPhrase;
	private String nomenclaturalMicroReference;
	private boolean hasProblem = false;
	protected Set<NameTypeDesignation> nameTypeDesignations  = new HashSet<NameTypeDesignation>();
	private HomotypicalGroup homotypicalGroup = new HomotypicalGroup();
	private Set<NameRelationship> relationsFromThisName = new HashSet<NameRelationship>();
	private Set<NameRelationship> relationsToThisName = new HashSet<NameRelationship>();
	private Set<NomenclaturalStatus> status = new HashSet<NomenclaturalStatus>();
	private Set<TaxonBase> taxonBases = new HashSet<TaxonBase>();
	private Rank rank;
	private INomenclaturalReference nomenclaturalReference;

	protected boolean protectedNameCache;
	
	static Method methodTaxonBaseSetName;
	
// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty taxon name instance.
	 * 
	 * @see #TaxonNameBase(Rank)
	 * @see #TaxonNameBase(HomotypicalGroup)
	 * @see #TaxonNameBase(Rank, HomotypicalGroup)
	 */
	public TaxonNameBase() {
		this(null, null);
	}
	/** 
	 * Class constructor: creates a new taxon name instance
	 * only containing its {@link common.Rank rank}.
	 * 
	 * @param  rank  the rank to be assigned to this taxon name
	 * @see    #TaxonNameBase()
	 * @see    #TaxonNameBase(HomotypicalGroup)
	 * @see    #TaxonNameBase(Rank, HomotypicalGroup)
	 */
	public TaxonNameBase(Rank rank) {
		this(rank, null);
	}
	/** 
	 * Class constructor: creates a new taxon name instance
	 * only containing its {@link common.HomotypicalGroup homotypical group}.
	 * The new taxon name instance will be also added to the set of taxon names
	 * belonging to this homotypical group. If the homotypical group 
	 * does not exist a new instance will be created for it.
	 * 
	 * @param  homotypicalGroup  the homotypical group to which this taxon name belongs
	 * @see    #TaxonNameBase()
	 * @see    #TaxonNameBase(Rank)
	 * @see    #TaxonNameBase(Rank, HomotypicalGroup)
	 */
	public TaxonNameBase(HomotypicalGroup homotypicalGroup) {
		this(null, homotypicalGroup);
	}
	/** 
	 * Class constructor: creates a new instance of a taxon name
	 * only containing its {@link common.Rank rank} and
	 * its {@link common.HomotypicalGroup homotypical group}.
	 * 
	 * @param  rank  the rank to be assigned to this taxon name
	 * @param  homotypicalGroup  the homotypical group to which this taxon name belongs
	 * @see    #TaxonNameBase()
	 * @see    #TaxonNameBase(Rank)
	 * @see    #TaxonNameBase(HomotypicalGroup)
	 */
	public TaxonNameBase(Rank rank, HomotypicalGroup homotypicalGroup) {
		super();
		this.setRank(rank);
		if (homotypicalGroup == null){
			homotypicalGroup = new HomotypicalGroup();
		}
		homotypicalGroup.addTypifiedName(this);
	}
	
//********* METHODS **************************************/

	
	/**
	 * Returns the boolean value "true" if the components of this taxon name
	 * follow the rules of the corresponding {@link NomenclaturalCode nomenclatural code},
	 * "false" otherwise. The nomenclatural code depends on
	 * the concrete name subclass ({@link BacterialName BacterialName},
	 * {@link BotanicalName BotanicalName}, {@link CultivarPlantName CultivarPlantName},
	 * {@link ZoologicalName ZoologicalName} or {@link ViralName ViralName}) 
	 * to which this taxon name belongs.
	 *  
	 * @return  the boolean value expressing the compliance of this taxon name to the nomenclatural code
	 */
	@Transient
	public abstract boolean isCodeCompliant();
	

	/** 
	 * Returns the set of all {@link NameRelationship name relationships}
	 * in which this taxon name is involved. A taxon name can be both source
	 * in some name relationships or target in some others.
	 *  
	 * @see    #getRelationsToThisName()
	 * @see    #getRelationsFromThisName()
	 * @see    #addNameRelationship(NameRelationship)
	 * @see    #addRelationshipToName(TaxonNameBase, NameRelationshipType, String)
	 * @see    #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
	 */
	@Transient
	public Set<NameRelationship> getNameRelations() {
		Set<NameRelationship> rels = new HashSet<NameRelationship>();
		rels.addAll(getRelationsFromThisName());
		rels.addAll(getRelationsToThisName());
		return rels;
	}
	/**
	 * Creates a new {@link NameRelationship#NameRelationship(TaxonNameBase, TaxonNameBase, NameRelationshipType, String) name relationship} from this taxon name to another taxon name
	 * and adds it both to the set of {@link #getRelationsFromThisName() relations from this taxon name} and
	 * to the set of {@link #getRelationsToThisName() relations to the other taxon name}.
	 * 
	 * @param toName		  the taxon name of the target for this new name relationship
	 * @param type			  the type of this new name relationship
	 * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
	 * @see    				  #getRelationsToThisName()
	 * @see    				  #getNameRelations()
	 * @see    				  #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
	 * @see    				  #addNameRelationship(NameRelationship)
	 */
	public void addRelationshipToName(TaxonNameBase toName, NameRelationshipType type, String ruleConsidered){
		NameRelationship rel = new NameRelationship(toName, this, type, ruleConsidered);
	}
	/**
	 * Creates a new {@link NameRelationship#NameRelationship(TaxonNameBase, TaxonNameBase, NameRelationshipType, String) name relationship} from another taxon name to this taxon name
	 * and adds it both to the set of {@link #getRelationsToThisName() relations to this taxon name} and
	 * to the set of {@link #getRelationsFromThisName() relations from the other taxon name}.
	 * 
	 * @param fromName		  the taxon name of the source for this new name relationship
	 * @param type			  the type of this new name relationship
	 * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
	 * @see    				  #getRelationsFromThisName()
	 * @see    				  #getNameRelations()
	 * @see    				  #addRelationshipToName(TaxonNameBase, NameRelationshipType, String)
	 * @see    				  #addNameRelationship(NameRelationship)
	 */
	public void addRelationshipFromName(TaxonNameBase fromName, NameRelationshipType type, String ruleConsidered){
		NameRelationship rel = new NameRelationship(this, fromName, type, ruleConsidered);
	}
	/**
	 * Adds an existing {@link NameRelationship name relationship} either to the set of
	 * {@link #getRelationsToThisName() relations to this taxon name} or to the set of
	 * {@link #getRelationsFromThisName() relations from this taxon name}. If neither the
	 * source nor the target of the name relationship match with this taxon
	 * no addition will be carried out.
	 * 
	 * @param rel  the name relationship to be added to one of this taxon name's name relationships sets
	 * @see    	   #getNameRelations()
	 * @see    	   #addRelationshipToName(TaxonNameBase, NameRelationshipType, String)
	 * @see    	   #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
	 */
	protected void addNameRelationship(NameRelationship rel) {
		if (rel!=null && rel.getToName().equals(this)){
			this.relationsToThisName.add(rel);
		}else if(rel!=null && rel.getFromName().equals(this)){
			this.relationsFromThisName.add(rel);			
		}else{
			//TODO: raise error???
		}
	}
	/** 
	 * Removes one {@link NameRelationship name relationship} from one of both sets of
	 * {@link #getNameRelations() name relationships} in which this taxon name is involved.
	 * The name relationship will also be removed from one of both sets belonging
	 * to the second taxon name involved. Furthermore the fromName and toName
	 * attributes of the name relationship object will be nullified. 
	 *
	 * @param  nameRelation  the name relationship which should be deleted from one of both sets
	 * @see    #getNameRelations()
	 */
	public void removeNameRelationship(NameRelationship nameRelation) {
		//TODO to be implemented?
		logger.warn("not yet fully implemented?");
		this.relationsToThisName.remove(nameRelation);
		this.relationsFromThisName.remove(nameRelation);
	}
	
	
	public void addRelationship(RelationshipBase relation) {
		if (relation instanceof NameRelationship){
			addNameRelationship((NameRelationship)relation);
		}else{
			//TODO exception handling
		}
	}

	
	/** 
	 * Returns the set of all {@link NameRelationship name relationships}
	 * in which this taxon name is involved as a source.
	 *  
	 * @see    #getNameRelations()
	 * @see    #getRelationsToThisName()
	 * @see    #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
	 */
	@OneToMany(mappedBy="relatedFrom", fetch= FetchType.EAGER)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<NameRelationship> getRelationsFromThisName() {
		return relationsFromThisName;
	}
	private void setRelationsFromThisName(Set<NameRelationship> relationsFromThisName) {
		this.relationsFromThisName = relationsFromThisName;
	}
	
	/** 
	 * Returns the set of all {@link NameRelationship name relationships}
	 * in which this taxon name is involved as a target.
	 *  
	 * @see    #getNameRelations()
	 * @see    #getRelationsFromThisName()
	 * @see    #addRelationshipToName(TaxonNameBase, NameRelationshipType, String)
	 */
	@OneToMany(mappedBy="relatedTo", fetch= FetchType.EAGER)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<NameRelationship> getRelationsToThisName() {
		return relationsToThisName;
	}
	private void setRelationsToThisName(Set<NameRelationship> relationsToThisName) {
		this.relationsToThisName = relationsToThisName;
	}

	
	/** 
	 * Returns the set of {@link NomenclaturalStatus nomenclatural status} assigned
	 * to this taxon name according to its corresponding nomenclature code.
	 * This includes the {@link NomenclaturalStatusType type} of the nomenclatural status
	 * and the nomenclatural code rule considered.
	 *
	 * @see     NomenclaturalStatus
	 * @see     NomenclaturalStatusType
	 */
	@OneToMany(fetch= FetchType.EAGER)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<NomenclaturalStatus> getStatus() {
		return status;
	}
	/** 
	 * @see     #getStatus()
	 */
	protected void setStatus(Set<NomenclaturalStatus> nomStatus) {
		this.status = nomStatus;
	}
	/** 
	 * Adds a new {@link NomenclaturalStatus nomenclatural status}
	 * to this taxon name's set of nomenclatural status.
	 *
	 * @param  nomStatus  the nomenclatural status to be added
	 * @see 			  #getStatus()
	 */
	public void addStatus(NomenclaturalStatus nomStatus) {
		this.status.add(nomStatus);
	}
	/** 
	 * Removes one element from the set of nomenclatural status of this taxon name.
	 * Type and ruleConsidered attributes of the nomenclatural status object
	 * will be nullified.
	 *
	 * @param  nomStatus  the nomenclatural status of this taxon name which should be deleted
	 * @see     		  #getStatus()
	 */
	public void removeStatus(NomenclaturalStatus nomStatus) {
		//TODO to be implemented?
		logger.warn("not yet fully implemented?");
		this.status.remove(nomStatus);
	}

	
	/**
	 * Indicates if this taxon name is a {@link NameRelationshipType.BASIONYM() basionym}
	 * or a {@link NameRelationshipType.REPLACED_SYNONYM() replaced synonym}
	 * of any other taxon name. Returns true, if a basionym or a replaced synonym 
	 * relationship from this taxon name to another taxon name exists,
	 * false otherwise (also in case this taxon name is the only one in the
	 * homotypical group).
	 */
	@Transient
	public boolean isOriginalCombination(){
		Set<NameRelationship> relationsFromThisName = this.getRelationsFromThisName();
		for (NameRelationship relation : relationsFromThisName) {
			if (relation.getType().equals(NameRelationshipType.BASIONYM()) ||
					relation.getType().equals(NameRelationshipType.REPLACED_SYNONYM())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the taxon name which is the {@link NameRelationshipType.BASIONYM() basionym} of this taxon name.
	 * The basionym of a taxon name is its epithet-bringing synonym.
	 * For instance Pinus abies L. was published by Linnaeus and the botanist
	 * Karsten transferred later this taxon to the genus Picea. Therefore,
	 * Pinus abies L. is the basionym of the new combination Picea abies (L.) H. Karst.
	 */
	@Transient
	public T getBasionym(){
		//TODO: pick the right name relationships...
		return null;
	}
	/**
	 * Assigns another taxon name as {@link NameRelationshipType.BASIONYM() basionym} of this taxon name.
	 * The basionym relationship will be added to this taxon name
	 * and to the basionym. The basionym cannot have itself a basionym.
	 * 
	 * @see  #getBasionym()
	 * @see  #setBasionym(TaxonNameBase, String)
	 */
	public void setBasionym(T basionym){
		setBasionym(basionym, null);
	}
	/**
	 * Assigns another taxon name as {@link NameRelationshipType.BASIONYM() basionym} of this taxon name
	 * and keeps the nomenclatural rule considered for it. The basionym
	 * relationship will be added to this taxon name and to the basionym.
	 * The basionym cannot have itself a basionym.
	 * 
	 * @see  #getBasionym()
	 * @see  #setBasionym(TaxonNameBase)
	 */
	public void setBasionym(T basionym, String ruleConsidered){
		basionym.addRelationshipToName(this, NameRelationshipType.BASIONYM(), ruleConsidered);
	}



	@Transient
	public abstract S getCacheStrategy();
	public abstract void setCacheStrategy(S cacheStrategy);
	
	/** 
	 * Returns the taxonomic {@link Rank rank} of this taxon name.
	 *
	 * @see 	Rank
	 */
	@ManyToOne
	//@Cascade({CascadeType.SAVE_UPDATE})
	public Rank getRank(){
		return this.rank;
	}
	/**
	 * @see  #getRank()
	 */
	public void setRank(Rank rank){
		this.rank = rank;
	}

	/** 
	 * Returns the {@link reference.INomenclaturalReference nomenclatural reference} of this taxon name.
	 * The nomenclatural reference is here meant to be the one publication
	 * this taxon name was originally published in while fulfilling the formal
	 * requirements as specified by the corresponding nomenclatural code.
	 *
	 * @see 	reference.INomenclaturalReference
	 * @see 	reference.ReferenceBase
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public INomenclaturalReference getNomenclaturalReference(){
		return (INomenclaturalReference) this.nomenclaturalReference;
	}
	/**
	 * Assigns a nomenclatural {@link reference.INomenclaturalReference nomenclatural reference} to this taxon name.
	 * The corresponding {@link reference.ReferenceBase.isNomenclaturallyRelevant nomenclaturally relevant flag} will be set to true
	 * as it is obviously used for nomenclatural purposes.
	 *
	 * @see  #getNomenclaturalReference()
	 */
	public void setNomenclaturalReference(INomenclaturalReference nomenclaturalReference){
		this.nomenclaturalReference = nomenclaturalReference;
	}

	/** 
	 * Returns the appended phrase string assigned to this taxon name.
	 * The appended phrase is a non-atomised addition to a name. It is
	 * not ruled by a nomenclatural code.
	 */
	public String getAppendedPhrase(){
		return this.appendedPhrase;
	}
	/**
	 * @see  #getAppendedPhrase()
	 */
	public void setAppendedPhrase(String appendedPhrase){
		this.appendedPhrase = appendedPhrase;
	}

	/** 
	 * Returns the details string of the nomenclatural reference assigned
	 * to this taxon name. The details describe the exact localisation within
	 * the publication used as nomenclature reference. These are mostly
	 * (implicitly) pages but can also be figures or tables or any other
	 * element of a publication. A nomenclatural micro reference (details)
	 * requires the existence of a nomenclatural reference.
	 */
	//Details of the nomenclatural reference (protologue). 
	public String getNomenclaturalMicroReference(){
		return this.nomenclaturalMicroReference;
	}
	/**
	 * @see  #getNomenclaturalMicroReference()
	 */
	public void setNomenclaturalMicroReference(String nomenclaturalMicroReference){
		this.nomenclaturalMicroReference = nomenclaturalMicroReference;
	}

	/**
	 * Returns the boolean value of the flag indicating whether the 
	 * or not (false) the {@link #getNameCache() nameCache} (scientific name without author strings and year)
	 * string of this taxon name. This flag shows whether the getNameCache
	 * method should return a generated value (false) or the present name cache
	 * string (true).  
	 *  
	 * @return  the boolean value of the protectedNameCache flag
	 * @see     #getNameCache()
	 */
	//this flag will be set to true if the parseName method was unable to successfully parse the name
	public boolean getHasProblem(){
		return this.hasProblem;
	}
	/**
	 * @see  #getHasProblem()
	 */
	public void setHasProblem(boolean hasProblem){
		this.hasProblem = hasProblem;
	}
	/**
	 * @see  #getHasProblem()
	 */
	public boolean hasProblem(){
		return getHasProblem();
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
	@Deprecated //only for bidirectional and persistence use
	protected void setHomotypicalGroup(HomotypicalGroup newHomotypicalGroup) {
		this.homotypicalGroup = newHomotypicalGroup;		
	}

	@Transient
	public StrictReferenceBase getCitation(){
		logger.warn("getCitation not yet implemented");
		return null;
	}

	@Transient
	public String getCitationString(){
		logger.warn("getCitationString not yet implemented");
		return null;
	}

	@Transient
	public String[] getProblems(){
		logger.warn("getProblems not yet implemented");
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

	@OneToMany(mappedBy="name", fetch= FetchType.EAGER)
	public Set<TaxonBase> getTaxonBases() {
		return this.taxonBases;
	}
	protected void setTaxonBases(Set<TaxonBase> taxonBases) {
		if (taxonBases == null){
			taxonBases = new HashSet<TaxonBase>();
		}else{
			this.taxonBases = taxonBases;
		}
	}
	//TODO protected
	public void addTaxonBase(TaxonBase taxonBase){
		taxonBases.add(taxonBase);
		initMethods();
		invokeSetMethod(methodTaxonBaseSetName, taxonBase);
	}

	private void initMethods(){
		if (methodTaxonBaseSetName == null){
			try {
				methodTaxonBaseSetName = TaxonBase.class.getDeclaredMethod("setName", TaxonNameBase.class);
				methodTaxonBaseSetName.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
				//TODO handle exception
			}
		}
	}
	
	
	/**
	 * Return a set of taxa that use this name.
	 * @return Set<Taxon> The set of taxa this TaxonName belongs to
	 */
	@Transient
	public Set<Taxon> getTaxa(){
		Set<Taxon> result = new HashSet<Taxon>();
		for (TaxonBase taxonBase : this.taxonBases){
			if (taxonBase instanceof Taxon){
				result.add((Taxon)taxonBase);
			}
		}
		return result;
	}
	
	/**
	 * Return a set of synonyms that use this name
	 * @return The set of synonyms this TaxonName belongs to
	 */
	@Transient
	public Set<Synonym> getSynonyms() {
		Set<Synonym> result = new HashSet<Synonym>();
		for (TaxonBase taxonBase : this.taxonBases){
			if (taxonBase instanceof Synonym){
				result.add((Synonym)taxonBase);
			}
		}
		return result;
	}
	
	@Transient
	public Set<SpecimenTypeDesignation> getSpecimenTypeDesignations() {
		return this.getHomotypicalGroup().getTypeDesignations();
	}
	
// ***********
	public boolean isHomotypic(TaxonNameBase homoTypicName) {
		if (homoTypicName == null) {
			return false;
		}
		HomotypicalGroup homotypicGroup = homoTypicName.getHomotypicalGroup();
		if (homotypicGroup == null || this.getHomotypicalGroup() == null) {
			return false;
		}
		if (homotypicGroup.equals(this.getHomotypicalGroup())) {
			return true;
		}
		return false;
	}
	
	
	
//*********  Rank comparison shortcuts   ********************//
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
	abstract public NomenclaturalCode getNomeclaturalCode();

	@Override
	public String generateTitle() {
		// TODO Auto-generated method stub
		return null;
	}
	

}