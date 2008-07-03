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
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * The taxon name class for plants and fungi.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:15
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BotanicalName", propOrder = {
    "isHybridFormula",
    "isMonomHybrid",
    "isBinomHybrid",
    "isTrinomHybrid",
    "isAnamorphic",
    "hybridRelationships"
})
@XmlRootElement(name = "BotanicalName")
@Entity
public class BotanicalName extends NonViralName {
	
	private static final Logger logger = Logger.getLogger(BotanicalName.class);
	
	//if set: this name is a hybrid formula (a hybrid that does not have an own name) and no other hybrid flags may be set. A
	//hybrid name  may not have either an authorteam nor other name components.
    @XmlElement(name ="IsHybridFormula")
	private boolean isHybridFormula = false;
	
    @XmlElement(name ="IsMonomHybrid")
	private boolean isMonomHybrid = false;
	
    @XmlElement(name ="IsBinomHybrid")
	private boolean isBinomHybrid = false;
	
    @XmlElement(name ="IsTrinomHybrid")
	private boolean isTrinomHybrid = false;
	
	//Only for fungi: to indicate that the type of the name is asexual or not
    @XmlElement(name ="IsAnamorphic")
	private boolean isAnamorphic;
	
    @XmlElementWrapper(name = "HybridRelationships")
    @XmlElement(name = "HybridRelationship")
	private Set<HybridRelationship> hybridRelationships = new HashSet();

	static private INonViralNameParser nameParser = new NonViralNameParserImpl();
	
	// ************* CONSTRUCTORS *************/	
	//needed by hibernate
	/** 
	 * Class constructor: creates a new botanical taxon name instance
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see #BotanicalName(Rank, HomotypicalGroup)
	 * @see #BotanicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see eu.etaxonomy.cdm.strategy.cache.BotanicNameDefaultCacheStrategy
	 */
	protected BotanicalName(){
		super();
		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	}
	/** 
	 * Class constructor: creates a new botanical taxon name instance
	 * only containing its {@link common.Rank rank},
	 * its {@link HomotypicalGroup homotypical group} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * The new botanical taxon name instance will be also added to the set of
	 * botanical taxon names belonging to this homotypical group.
	 * 
	 * @param	rank  the rank to be assigned to this botanical taxon name
	 * @param	homotypicalGroup  the homotypical group to which this botanical taxon name belongs
	 * @see 	#BotanicalName()
	 * @see 	#BotanicalName(Rank, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.BotanicNameDefaultCacheStrategy
	 */
	protected BotanicalName(Rank rank, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	}
	/** 
	 * Class constructor: creates a new botanical taxon name instance
	 * containing its {@link common.Rank rank},
	 * its {@link HomotypicalGroup homotypical group},
	 * its scientific name components, its {@link agent.TeamOrPersonBase author(team)},
	 * its {@link reference.INomenclaturalReference nomenclatural reference} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * The new botanical taxon name instance will be also added to the set of
	 * botanical taxon names belonging to this homotypical group.
	 * 
	 * @param	rank  the rank to be assigned to this botanical taxon name
	 * @param	genusOrUninomial the string for this botanical taxon name
	 * 			if its rank is genus or higher or for the genus part
	 * 			if its rank is lower than genus
	 * @param	infraGenericEpithet  the string for the first epithet of
	 * 			this botanical taxon name if its rank is lower than genus
	 * 			and higher than species aggregate
	 * @param	specificEpithet  the string for the first epithet of
	 * 			this botanical taxon name if its rank is species aggregate or lower
	 * @param	infraSpecificEpithet  the string for the second epithet of
	 * 			this botanical taxon name if its rank is lower than species
	 * @param	combinationAuthorTeam  the author or the team who published this botanical taxon name
	 * @param	nomenclaturalReference  the nomenclatural reference where this botanical taxon name was published
	 * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
	 * @param	homotypicalGroup  the homotypical group to which this botanical taxon name belongs
	 * @see 	#BotanicalName()
	 * @see 	#BotanicalName(Rank, HomotypicalGroup)
	 * @see		#NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.BotanicNameDefaultCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.INonViralNameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.IIdentifiableEntityCacheStrategy
	 */
	protected BotanicalName(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		super(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorTeam, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	}

	
	//********* METHODS **************************************/
	
	/** 
	 * Creates a new botanical taxon name instance
	 * only containing its {@link common.Rank rank} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param	rank	the rank to be assigned to this botanical taxon name
	 * @see 			#BotanicalName(Rank, HomotypicalGroup)
	 * @see 			#NewInstance(Rank, HomotypicalGroup)
	 * @see 			#NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 			eu.etaxonomy.cdm.strategy.cache.BotanicNameDefaultCacheStrategy
	 */
	public static BotanicalName NewInstance(Rank rank){
		return new BotanicalName(rank, null);
	}
	/** 
	 * Creates a new botanical taxon name instance
	 * only containing its {@link common.Rank rank},
	 * its {@link HomotypicalGroup homotypical group} and 
 	 * the {@link eu.etaxonomy.cdm.strategy.cache.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * The new botanical taxon name instance will be also added to the set of
	 * botanical taxon names belonging to this homotypical group.
	 * 
	 * @param  rank  the rank to be assigned to this botanical taxon name
	 * @param  homotypicalGroup  the homotypical group to which this botanical taxon name belongs
	 * @see    #NewInstance(Rank)
	 * @see    #NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see    #BotanicalName(Rank, HomotypicalGroup)
	 * @see    eu.etaxonomy.cdm.strategy.cache.BotanicNameDefaultCacheStrategy
	 */
	public static BotanicalName NewInstance(Rank rank, HomotypicalGroup homotypicalGroup){
		return new BotanicalName(rank, homotypicalGroup);
	}
	/** 
	 * Creates a new botanical taxon name instance
	 * containing its {@link common.Rank rank},
	 * its {@link HomotypicalGroup homotypical group},
	 * its scientific name components, its {@link agent.TeamOrPersonBase author(team)},
	 * its {@link reference.INomenclaturalReference nomenclatural reference} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * The new botanical taxon name instance will be also added to the set of
	 * botanical taxon names belonging to this homotypical group.
	 * 
	 * @param	rank  the rank to be assigned to this botanical taxon name
	 * @param	genusOrUninomial the string for this botanical taxon name
	 * 			if its rank is genus or higher or for the genus part
	 * 			if its rank is lower than genus
	 * @param	infraGenericEpithet  the string for the first epithet of
	 * 			this botanical taxon name if its rank is lower than genus
	 * 			and higher than species aggregate
	 * @param	specificEpithet  the string for the first epithet of
	 * 			this botanical taxon name if its rank is species aggregate or lower
	 * @param	infraSpecificEpithet  the string for the second epithet of
	 * 			this botanical taxon name if its rank is lower than species
	 * @param	combinationAuthorTeam  the author or the team who published this botanical taxon name
	 * @param	nomenclaturalReference  the nomenclatural reference where this botanical taxon name was published
	 * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
	 * @param	homotypicalGroup  the homotypical group to which this botanical taxon name belongs
	 * @see 	#NewInstance(Rank)
	 * @see 	#NewInstance(Rank, HomotypicalGroup)
	 * @see		#ZoologicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.BotanicNameDefaultCacheStrategy
	 */
	public static  BotanicalName NewInstance(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		return new BotanicalName(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorTeam, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
	}
	
	/**
	 * Returns a botanical taxon name based on parsing a string representing
	 * all elements (according to the ICBN) of a botanical taxon name (where
	 * the scientific name is an uninomial) including authorship but without
	 * nomenclatural reference.
	 * 
	 * @param	fullNameString  the string to be parsed 
	 * @return					the new botanical taxon name
	 */
	public static BotanicalName PARSED_NAME(String fullNameString){
		return PARSED_NAME(fullNameString, Rank.GENUS());
	}
	
	/**
	 * Returns a botanical taxon name based on parsing a string representing
	 * all elements (according to the ICBN) of a botanical taxon name including
	 * authorship but without nomenclatural reference. The parsing result
	 * depends on the given rank of the botanical taxon name to be created.
	 * 
	 * @param 	fullNameString  the string to be parsed 
	 * @param   rank			the rank of the taxon name
	 * @return					the new botanical taxon name
	 */
	public static BotanicalName PARSED_NAME(String fullNameString, Rank rank){
		if (nameParser == null){
			nameParser = new NonViralNameParserImpl();
		}
		return (BotanicalName)nameParser.parseFullName(fullNameString, NomenclaturalCode.ICBN(),  rank);
	}
	
	/**
	 * Returns a botanical taxon name based on parsing a string representing
	 * all elements (according to the ICBN) of a botanical taxon name (where
	 * the scientific name is an uninomial) including authorship and
	 * nomenclatural reference. Eventually a new {@link reference.INomenclaturalReference nomenclatural reference}
	 * instance will also be created.
	 * 
	 * @param	fullNameAndReferenceString  the string to be parsed 
	 * @return								the new botanical taxon name
	 */
	public static BotanicalName PARSED_REFERENCE(String fullNameAndReferenceString){
		return PARSED_REFERENCE(fullNameAndReferenceString, Rank.GENUS());
	}
	
	/**
	 * Returns a botanical taxon name based on parsing a string representing
	 * all elements (according to the ICBN) of a botanical taxon name including
	 * authorship and nomenclatural reference. The parsing result depends on
	 * the given rank of the botanical taxon name to be created.
	 * Eventually a new {@link reference.INomenclaturalReference nomenclatural reference}
	 * instance will also be created.
	 * 
	 * @param	fullNameAndReferenceString  the string to be parsed 
	 * @return								the new botanical taxon name
	 */
	public static BotanicalName PARSED_REFERENCE(String fullNameAndReferenceString, Rank rank){
		if (nameParser == null){
			nameParser = new NonViralNameParserImpl();
		}
		return (BotanicalName)nameParser.parseFullReference(fullNameAndReferenceString, NomenclaturalCode.ICBN(), rank);
	}
	
	
	/** 
	 * Returns the set of all {@link HybridRelationship hybrid relationships}
	 * in which this botanical taxon name is involved. Any botanical taxon name
	 * (even itself a hybrid taxon name) can be a parent of another hybrid
	 * taxon name.
	 *  
	 * @see    #getParentRelationships()
	 * @see    #getChildRelationships()
	 * @see    #addHybridRelationship(HybridRelationship)
	 * @see    #addRelationship(RelationshipBase)
	 */
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	public Set<HybridRelationship> getHybridRelationships() {
		return hybridRelationships;
	}
	/**
	 * @see  #getHybridRelationships()
	 */
	protected void setHybridRelationships(Set<HybridRelationship> relationships) {
		this.hybridRelationships = relationships;
	}
	/**
	 * Adds the given {@link HybridRelationship hybrid relationship} to the set
	 * of {@link #getHybridRelationships() hybrid relationships} of both botanical taxon names
	 * involved in this hybrid relationship. One of both botanical taxon names
	 * must be this botanical taxon name else no addition will be carried out.
	 * The {@link common.RelationshipBase#getRelatedTo() child botanical taxon name}
	 * must be a hybrid, which means that one of its four hybrid flags must be set.
	 * 
	 * @param relationship  the hybrid relationship to be added
	 * @see    				#isHybridFormula()
	 * @see    				#isMonomHybrid()
	 * @see    				#isBinomHybrid()
	 * @see    				#isTrinomHybrid()
	 * @see    				#getHybridRelationships()
	 * @see    				#getParentRelationships()
	 * @see    				#getChildRelationships()
	 * @see    				#addRelationship(RelationshipBase)
	 */
	public void addHybridRelationship(HybridRelationship relationship) {
		this.hybridRelationships.add(relationship);
	}
	/** 
	 * Removes one {@link HybridRelationship hybrid relationship} from the set of
	 * {@link #getHybridRelationships() hybrid relationships} in which this botanical taxon name
	 * is involved. The hybrid relationship will also be removed from the set
	 * belonging to the second botanical taxon name involved. 
	 *
	 * @param  relationship  the hybrid relationship which should be deleted from the corresponding sets
	 * @see    				 #getHybridRelationships()
	 */
	public void removeHybridRelationship(HybridRelationship relationship) {
		this.hybridRelationships.remove(relationship);
	}

	/** 
	 * Returns the set of all {@link HybridRelationship hybrid relationships}
	 * in which this botanical taxon name is involved as a {@link common.RelationshipBase#getRelatedFrom() parent}.
	 *  
	 * @see    #getHybridRelationships()
	 * @see    #getChildRelationships()
	 * @see    HybridRelationshipType
	 */
	@Transient
	public Set<HybridRelationship> getParentRelationships() {
		// FIXME: filter relations
		return hybridRelationships;
	}
	/** 
	 * Returns the set of all {@link HybridRelationship hybrid relationships}
	 * in which this botanical taxon name is involved as a {@link common.RelationshipBase#getRelatedTo() child}.
	 *  
	 * @see    #getHybridRelationships()
	 * @see    #getParentRelationships()
	 * @see    HybridRelationshipType
	 */
	@Transient
	public Set<HybridRelationship> getChildRelationships() {
		// FIXME: filter relations
		return hybridRelationships;
	}

	/**
	 * Does the same as the addHybridRelationship method if the given
	 * {@link common.RelationshipBase relation} is also a {@link HybridRelationship hybrid relationship}.
	 * Otherwise this method does the same as the overwritten {@link TaxonNameBase#addRelationship(RelationshipBase) addRelationship}
	 * method from TaxonNameBase.
	 * 
	 * @param relation  the relationship to be added to some of this taxon name's relationships sets
	 * @see    	   		#addHybridRelationship(HybridRelationship)
	 * @see    	   		TaxonNameBase#addRelationship(RelationshipBase)
	 * @see    	   		TaxonNameBase#addNameRelationship(NameRelationship)
	 */
	public void addRelationship(RelationshipBase relation) {
		if (relation instanceof HybridRelationship){
			addHybridRelationship((HybridRelationship)relation);
		}else {
			super.addRelationship(relation);
		}
	}

	/**
	 * Returns the boolean value of the flag indicating whether the name of this
	 * botanical taxon name is a hybrid formula (true) or not (false). A hybrid
	 * named by a hybrid formula (composed with its parent names by placing the
	 * multiplication sign between them) does not have an own published name
	 * and therefore has neither an {@link NonViralName#getAuthorshipCache() autorship}
	 * nor other name components. If this flag is set no other hybrid flags may
	 * be set.
	 *  
	 * @return  the boolean value of the isHybridFormula flag
	 * @see		#isMonomHybrid()
	 * @see		#isBinomHybrid()
	 * @see		#isTrinomHybrid()
	 */
	public boolean isHybridFormula(){
		return this.isHybridFormula;
	}

	/**
	 * @see  #isHybridFormula()
	 */
	public void setHybridFormula(boolean isHybridFormula){
		this.isHybridFormula = isHybridFormula;
	}

	/**
	 * Returns the boolean value of the flag indicating whether this botanical
	 * taxon name is the name of an intergeneric hybrid (true) or not (false).
	 * In this case the multiplication sign is placed before the scientific
	 * name. If this flag is set no other hybrid flags may be set.
	 *  
	 * @return  the boolean value of the isMonomHybrid flag
	 * @see		#isHybridFormula()
	 * @see		#isBinomHybrid()
	 * @see		#isTrinomHybrid()
	 */
	public boolean isMonomHybrid(){
		return this.isMonomHybrid;
	}

	/**
	 * @see  #isMonomHybrid()
	 * @see	 #isBinomHybrid()
	 * @see	 #isTrinomHybrid()
	 */
	public void setMonomHybrid(boolean isMonomHybrid){
		this.isMonomHybrid = isMonomHybrid;
	}

	/**
	 * Returns the boolean value of the flag indicating whether this botanical
	 * taxon name is the name of an interspecific hybrid (true) or not (false).
	 * In this case the multiplication sign is placed before the species
	 * epithet. If this flag is set no other hybrid flags may be set.
	 *  
	 * @return  the boolean value of the isBinomHybrid flag
	 * @see		#isHybridFormula()
	 * @see		#isMonomHybrid()
	 * @see		#isTrinomHybrid()
	 */
	public boolean isBinomHybrid(){
		return this.isBinomHybrid;
	}

	/**
	 * @see	 #isBinomHybrid()
	 * @see  #isMonomHybrid()
	 * @see	 #isTrinomHybrid()
	 */
	public void setBinomHybrid(boolean isBinomHybrid){
		this.isBinomHybrid = isBinomHybrid;
	}

	/**
	 * Returns the boolean value of the flag indicating whether this botanical
	 * taxon name is the name of an infraspecific hybrid (true) or not (false).
	 * In this case the term "notho-" (optionally abbreviated "n-") is used as
	 * a prefix to the term denoting the infraspecific rank of this botanical
	 * taxon name. If this flag is set no other hybrid flags may be set.
	 *  
	 * @return  the boolean value of the isTrinomHybrid flag
	 * @see		#isHybridFormula()
	 * @see		#isMonomHybrid()
	 * @see		#isBinomHybrid()
	 */
	public boolean isTrinomHybrid(){
		return this.isTrinomHybrid;
	}

	/**
	 * @see	 #isTrinomHybrid()
	 * @see	 #isBinomHybrid()
	 * @see  #isMonomHybrid()
	 */
	public void setTrinomHybrid(boolean isTrinomHybrid){
		this.isTrinomHybrid = isTrinomHybrid;
	}

	/**
	 * Returns the boolean value of the flag indicating whether the specimen
	 * type of this botanical taxon name for a fungus is asexual (true) or not
	 * (false). This applies only in case of fungi. The Article 59 of the ICBN
	 * permits mycologists to give asexually reproducing fungi (anamorphs)
	 * separate names from their sexual states (teleomorphs).
	 *  
	 * @return  the boolean value of the isAnamorphic flag
	 */
	public boolean isAnamorphic(){
		return this.isAnamorphic;
	}

	/**
	 * @see  #isAnamorphic()
	 */
	public void setAnamorphic(boolean isAnamorphic){
		this.isAnamorphic = isAnamorphic;
	}
	
	
	/**
	 * Returns the {@link NomenclaturalCode nomenclatural code} that governs
	 * the construction of this botanical taxon name, that is the
	 * International Code of Botanical Nomenclature. This method overrides
	 * the getNomeclaturalCode method from {@link NonViralName#getNomeclaturalCode() NonViralName}.
	 *
	 * @return  the nomenclatural code for plants
	 * @see  	NonViralName#isCodeCompliant()
	 * @see  	TaxonNameBase#getHasProblem()
	 */
	@Transient
	@Override
	public NomenclaturalCode getNomenclaturalCode(){
		return NomenclaturalCode.ICBN();

	}

}