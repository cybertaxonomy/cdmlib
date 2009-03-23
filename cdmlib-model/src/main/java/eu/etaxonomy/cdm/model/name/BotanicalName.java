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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * The taxon name class for plants and fungi.
 * <P>
 * This class corresponds to: NameBotanical according to the ABCD schema.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:15
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BotanicalName", propOrder = {
    "hybridFormula",
    "monomHybrid",
    "binomHybrid",
    "trinomHybrid",
    "anamorphic",
    "hybridRelationships"
})
@XmlRootElement(name = "BotanicalName")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.name.TaxonNameBase")
@Audited
public class BotanicalName extends NonViralName<BotanicalName> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6818651572463497727L;

	private static final Logger logger = Logger.getLogger(BotanicalName.class);
	
	//if set: this name is a hybrid formula (a hybrid that does not have an own name) and no other hybrid flags may be set. A
	//hybrid name  may not have either an authorteam nor other name components.
    @XmlElement(name ="IsHybridFormula")
	private boolean hybridFormula = false;
	
    @XmlElement(name ="IsMonomHybrid")
	private boolean monomHybrid = false;
	
    @XmlElement(name ="IsBinomHybrid")
	private boolean binomHybrid = false;
	
    @XmlElement(name ="IsTrinomHybrid")
	private boolean trinomHybrid = false;
	
	//Only for fungi: to indicate that the type of the name is asexual or not
    @XmlElement(name ="IsAnamorphic")
	private boolean anamorphic;
	
    @XmlElementWrapper(name = "HybridRelationships")
    @XmlElement(name = "HybridRelationship")
    @OneToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	private Set<HybridRelationship> hybridRelationships = new HashSet();

	static private INonViralNameParser nameParser = new NonViralNameParserImpl();
	
	// ************* CONSTRUCTORS *************/	
	//needed by hibernate
	/** 
	 * Class constructor: creates a new botanical taxon name instance
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see #BotanicalName(Rank, HomotypicalGroup)
	 * @see #BotanicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 */
	protected BotanicalName(){
		super();
		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	}
	/** 
	 * Class constructor: creates a new botanical taxon name instance
	 * only containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * The new botanical taxon name instance will be also added to the set of
	 * botanical taxon names belonging to this homotypical group.
	 * 
	 * @param	rank  the rank to be assigned to <i>this</i> botanical taxon name
	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> botanical taxon name belongs
	 * @see 	#BotanicalName()
	 * @see 	#BotanicalName(Rank, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 */
	protected BotanicalName(Rank rank, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	}
	/** 
	 * Class constructor: creates a new botanical taxon name instance
	 * containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group},
	 * its scientific name components, its {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase author(team)},
	 * its {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference nomenclatural reference} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * The new botanical taxon name instance will be also added to the set of
	 * botanical taxon names belonging to this homotypical group.
	 * 
	 * @param	rank  the rank to be assigned to <i>this</i> botanical taxon name
	 * @param	genusOrUninomial the string for <i>this</i> botanical taxon name
	 * 			if its rank is genus or higher or for the genus part
	 * 			if its rank is lower than genus
	 * @param	infraGenericEpithet  the string for the first epithet of
	 * 			<i>this</i> botanical taxon name if its rank is lower than genus
	 * 			and higher than species aggregate
	 * @param	specificEpithet  the string for the first epithet of
	 * 			<i>this</i> botanical taxon name if its rank is species aggregate or lower
	 * @param	infraSpecificEpithet  the string for the second epithet of
	 * 			<i>this</i> botanical taxon name if its rank is lower than species
	 * @param	combinationAuthorTeam  the author or the team who published <i>this</i> botanical taxon name
	 * @param	nomenclaturalReference  the nomenclatural reference where <i>this</i> botanical taxon name was published
	 * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> botanical taxon name belongs
	 * @see 	#BotanicalName()
	 * @see 	#BotanicalName(Rank, HomotypicalGroup)
	 * @see		#NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	protected BotanicalName(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		super(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorTeam, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	}

	
	//********* METHODS **************************************/
	
	/** 
	 * Creates a new botanical taxon name instance
	 * only containing its {@link Rank rank} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param	rank	the rank to be assigned to <i>this</i> botanical taxon name
	 * @see 			#BotanicalName(Rank, HomotypicalGroup)
	 * @see 			#NewInstance(Rank, HomotypicalGroup)
	 * @see 			#NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 			eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 */
	public static BotanicalName NewInstance(Rank rank){
		return new BotanicalName(rank, null);
	}
	/** 
	 * Creates a new botanical taxon name instance
	 * only containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group} and 
 	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * The new botanical taxon name instance will be also added to the set of
	 * botanical taxon names belonging to this homotypical group.
	 * 
	 * @param  rank  the rank to be assigned to <i>this</i> botanical taxon name
	 * @param  homotypicalGroup  the homotypical group to which <i>this</i> botanical taxon name belongs
	 * @see    #NewInstance(Rank)
	 * @see    #NewInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see    #BotanicalName(Rank, HomotypicalGroup)
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 */
	public static BotanicalName NewInstance(Rank rank, HomotypicalGroup homotypicalGroup){
		return new BotanicalName(rank, homotypicalGroup);
	}
	/** 
	 * Creates a new botanical taxon name instance
	 * containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group},
	 * its scientific name components, its {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase author(team)},
	 * its {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference nomenclatural reference} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
	 * The new botanical taxon name instance will be also added to the set of
	 * botanical taxon names belonging to this homotypical group.
	 * 
	 * @param	rank  the rank to be assigned to <i>this</i> botanical taxon name
	 * @param	genusOrUninomial the string for <i>this</i> botanical taxon name
	 * 			if its rank is genus or higher or for the genus part
	 * 			if its rank is lower than genus
	 * @param	infraGenericEpithet  the string for the first epithet of
	 * 			<i>this</i> botanical taxon name if its rank is lower than genus
	 * 			and higher than species aggregate
	 * @param	specificEpithet  the string for the first epithet of
	 * 			<i>this</i> botanical taxon name if its rank is species aggregate or lower
	 * @param	infraSpecificEpithet  the string for the second epithet of
	 * 			<i>this</i> botanical taxon name if its rank is lower than species
	 * @param	combinationAuthorTeam  the author or the team who published <i>this</i> botanical taxon name
	 * @param	nomenclaturalReference  the nomenclatural reference where <i>this</i> botanical taxon name was published
	 * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> botanical taxon name belongs
	 * @see 	#NewInstance(Rank)
	 * @see 	#NewInstance(Rank, HomotypicalGroup)
	 * @see		ZoologicalName#ZoologicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
	 */
	public static  BotanicalName NewInstance(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		return new BotanicalName(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorTeam, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
	}
	
	/**
	 * Returns a botanical taxon name based on parsing a string representing
	 * all elements (according to the ICBN) of a botanical taxon name (where
	 * the scientific name is an uninomial) including authorship but without
	 * nomenclatural reference. If the {@link Rank rank} is not "Genus" it should be
	 * set afterwards with the {@link TaxonNameBase#setRank(Rank) setRank} methode.
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
		return (BotanicalName)nameParser.parseFullName(fullNameString, NomenclaturalCode.ICBN,  rank);
	}
	
	/**
	 * Returns a botanical taxon name based on parsing a string representing
	 * all elements (according to the ICBN) of a botanical taxon name (where
	 * the scientific name is an uninomial) including authorship and
	 * nomenclatural reference. Eventually a new {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference nomenclatural reference}
	 * instance will also be created. If the {@link Rank rank} is not "Genus" it should be
	 * set afterwards with the {@link TaxonNameBase#setRank(Rank) setRank} methode.
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
	 * Eventually a new {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference nomenclatural reference}
	 * instance will also be created.
	 * 
	 * @param	fullNameAndReferenceString  the string to be parsed 
	 * @param   rank						the rank of the taxon name
	 * @return								the new botanical taxon name
	 */
	public static BotanicalName PARSED_REFERENCE(String fullNameAndReferenceString, Rank rank){
		if (nameParser == null){
			nameParser = new NonViralNameParserImpl();
		}
		return (BotanicalName)nameParser.parseReferencedName(fullNameAndReferenceString, NomenclaturalCode.ICBN, rank);
	}
	
	
	/** 
	 * Returns the set of all {@link HybridRelationship hybrid relationships}
	 * in which <i>this</i> botanical taxon name is involved. Any botanical taxon name
	 * (even itself a hybrid taxon name) can be a parent of another hybrid
	 * taxon name.
	 *  
	 * @see    #getParentRelationships()
	 * @see    #getChildRelationships()
	 * @see    #addHybridRelationship(HybridRelationship)
	 * @see    #addRelationship(RelationshipBase)
	 */
	public Set<HybridRelationship> getHybridRelationships() {
		return hybridRelationships;
	}

	/**
	 * Adds the given {@link HybridRelationship hybrid relationship} to the set
	 * of {@link #getHybridRelationships() hybrid relationships} of both botanical taxon names
	 * involved in this hybrid relationship. One of both botanical taxon names
	 * must be <i>this</i> botanical taxon name otherwise no addition will be carried
	 * out. The {@link eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedTo() child botanical taxon name}
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
	protected void addHybridRelationship(HybridRelationship relationship) {
		this.hybridRelationships.add(relationship);
	}
	
	/**
	 * Creates a new {@link HybridRelationship#HybridRelationship(BotanicalName, BotanicalName, HybridRelationshipType, String) hybrid relationship} 
	 * to <i>this</i> botanical name. A HybridRelationship may be of type
	 * "is first/second parent" or "is male/female parent". By invoking this
	 * method <i>this</i> botanical name becomes a hybrid child of the parent
	 * botanical name.
	 * 
	 * @param parentName	  the botanical name of the parent for this new hybrid name relationship
	 * @param type			  the type of this new name relationship
	 * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
	 * @see    				  #addHybridChild(BotanicalName, HybridRelationshipType,String )
	 * @see    				  #getRelationsToThisName()
	 * @see    				  #getNameRelations()
	 * @see    				  #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
	 * @see    				  #addNameRelationship(NameRelationship)
	 */
	public void addHybridParent(BotanicalName parentName, HybridRelationshipType type, String ruleConsidered){
		HybridRelationship rel = new HybridRelationship(this, parentName, type, ruleConsidered);
	}
	
	/**
	 * Creates a new {@link HybridRelationship#HybridRelationship(BotanicalName, BotanicalName, HybridRelationshipType, String) hybrid relationship} 
	 * to <i>this</i> botanical name. A HybridRelationship may be of type
	 * "is first/second parent" or "is male/female parent". By invoking this
	 * method <i>this</i> botanical name becomes a parent of the hybrid child
	 * botanical name.
	 * 
	 * @param childName		  the botanical name of the child for this new hybrid name relationship
	 * @param type			  the type of this new name relationship
	 * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
	 * @see    				  #addHybridParent(BotanicalName, HybridRelationshipType,String )
	 * @see    				  #getRelationsToThisName()
	 * @see    				  #getNameRelations()
	 * @see    				  #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
	 * @see    				  #addNameRelationship(NameRelationship)
	 */
	public void addHybridChild(BotanicalName childName, HybridRelationshipType type, String ruleConsidered){
		HybridRelationship rel = new HybridRelationship(childName, this, type, ruleConsidered);
	}
	
	
	/** 
	 * Removes one {@link HybridRelationship hybrid relationship} from the set of
	 * {@link #getHybridRelationships() hybrid relationships} in which <i>this</i> botanical taxon name
	 * is involved. The hybrid relationship will also be removed from the set
	 * belonging to the second botanical taxon name involved. 
	 *
	 * @param  relationship  the hybrid relationship which should be deleted from the corresponding sets
	 * @see    				 #getHybridRelationships()
	 */
	public void removeHybridRelationship(HybridRelationship relationship) {
		//TODO
		logger.warn("Birelationship not yet implemented");
		this.hybridRelationships.remove(relationship);
	}
	
	/** 
	 * Returns the set of all {@link HybridRelationship hybrid relationships}
	 * in which <i>this</i> botanical taxon name is involved as a {@link common.RelationshipBase#getRelatedFrom() parent}.
	 *  
	 * @see    #getHybridRelationships()
	 * @see    #getChildRelationships()
	 * @see    HybridRelationshipType
	 */
	public Set<HybridRelationship> getParentRelationships() {
		// FIXME: filter relations
		return hybridRelationships;
	}
	/** 
	 * Returns the set of all {@link HybridRelationship hybrid relationships}
	 * in which <i>this</i> botanical taxon name is involved as a {@link common.RelationshipBase#getRelatedTo() child}.
	 *  
	 * @see    #getHybridRelationships()
	 * @see    #getParentRelationships()
	 * @see    HybridRelationshipType
	 */
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
	 * @param relation  the relationship to be added to some of <i>this</i> taxon name's relationships sets
	 * @see    	   		#addHybridRelationship(HybridRelationship)
	 * @see    	   		TaxonNameBase#addRelationship(RelationshipBase)
	 * @see    	   		TaxonNameBase#addNameRelationship(NameRelationship)
	 */
	@Override
	@Deprecated  //To be used by RelationshipBase only
	public void addRelationship(RelationshipBase relation) {
		if (relation instanceof HybridRelationship){
			addHybridRelationship((HybridRelationship)relation);
		}else {
			super.addRelationship(relation);
		}
	}

	/**
	 * Returns the boolean value of the flag indicating whether the name of <i>this</i>
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
		return this.hybridFormula;
	}

	/**
	 * @see  #isHybridFormula()
	 */
	public void setHybridFormula(boolean hybridFormula){
		this.hybridFormula = hybridFormula;
	}

	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i> botanical
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
		return this.monomHybrid;
	}

	/**
	 * @see  #isMonomHybrid()
	 * @see	 #isBinomHybrid()
	 * @see	 #isTrinomHybrid()
	 */
	public void setMonomHybrid(boolean monomHybrid){
		this.monomHybrid = monomHybrid;
	}

	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i> botanical
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
		return this.binomHybrid;
	}

	/**
	 * @see	 #isBinomHybrid()
	 * @see  #isMonomHybrid()
	 * @see	 #isTrinomHybrid()
	 */
	public void setBinomHybrid(boolean binomHybrid){
		this.binomHybrid = binomHybrid;
	}

	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i> botanical
	 * taxon name is the name of an infraspecific hybrid (true) or not (false).
	 * In this case the term "notho-" (optionally abbreviated "n-") is used as
	 * a prefix to the term denoting the infraspecific rank of <i>this</i> botanical
	 * taxon name. If this flag is set no other hybrid flags may be set.
	 *  
	 * @return  the boolean value of the isTrinomHybrid flag
	 * @see		#isHybridFormula()
	 * @see		#isMonomHybrid()
	 * @see		#isBinomHybrid()
	 */
	public boolean isTrinomHybrid(){
		return this.trinomHybrid;
	}

	/**
	 * @see	 #isTrinomHybrid()
	 * @see	 #isBinomHybrid()
	 * @see  #isMonomHybrid()
	 */
	public void setTrinomHybrid(boolean trinomHybrid){
		this.trinomHybrid = trinomHybrid;
	}

	/**
	 * Returns the boolean value of the flag indicating whether the specimen
	 * type of <i>this</i> botanical taxon name for a fungus is asexual (true) or not
	 * (false). This applies only in case of fungi. The Article 59 of the ICBN
	 * permits mycologists to give asexually reproducing fungi (anamorphs)
	 * separate names from their sexual states (teleomorphs).
	 *  
	 * @return  the boolean value of the isAnamorphic flag
	 */
	public boolean isAnamorphic(){
		return this.anamorphic;
	}

	/**
	 * @see  #isAnamorphic()
	 */
	public void setAnamorphic(boolean anamorphic){
		this.anamorphic = anamorphic;
	}
	
	
	/**
	 * Returns the {@link NomenclaturalCode nomenclatural code} that governs
	 * the construction of <i>this</i> botanical taxon name, that is the
	 * International Code of Botanical Nomenclature. This method overrides
	 * the getNomeclaturalCode method from {@link NonViralName NonViralName}.
	 *
	 * @return  the nomenclatural code for plants
	 * @see  	NonViralName#isCodeCompliant()
	 * @see  	TaxonNameBase#getHasProblem()
	 */
	@Override
	public NomenclaturalCode getNomenclaturalCode(){
		return NomenclaturalCode.ICBN;
	}

}