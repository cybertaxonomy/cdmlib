/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAgent;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.BotanicNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.TaxonNameParserBotanicalNameImpl;

import java.util.*;

import javax.naming.NameParser;
import javax.persistence.*;

/**
 * Taxon name class for plants
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:15
 */
@Entity
public class BotanicalName extends NonViralName {
	static Logger logger = Logger.getLogger(BotanicalName.class);
	//if set: this name is a hybrid formula (a hybrid that does not have an own name) and no other hybrid flags may be set. A
	//hybrid name  may not have either an authorteam nor other name components.
	private boolean isHybridFormula = false;
	private boolean isMonomHybrid = false;
	private boolean isBinomHybrid = false;
	private boolean isTrinomHybrid = false;
	//Only for fungi: to indicate that the type of the name is asexual or not
	private boolean isAnamorphic;
	private Set<HybridRelationship> hybridRelationships = new HashSet();

	/**
	 * @param rank
	 * @return
	 */
	public static BotanicalName NewInstance(Rank rank){
		return new BotanicalName(rank);
	}
	
	/**
	 * Returns a parsed Name
	 * @param fullName
	 * @return
	 */
	public static BotanicalName PARSED_NAME(String fullName){
		return PARSED_NAME(fullName, Rank.GENUS());
	}
	
	/**
	 * Returns a parsed Name
	 * @param fullName
	 * @return
	 */
	public static BotanicalName PARSED_NAME(String fullName, Rank rank){
		if (nameParser == null){
			nameParser = new TaxonNameParserBotanicalNameImpl();
		}
		return (BotanicalName)nameParser.parseFullName(fullName, rank);
	}
	
	//needed by hibernate
	protected BotanicalName(){
		super();
		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	}
	public BotanicalName(Rank rank) {
		super(rank);
		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	}
	public BotanicalName(Rank rank, String genusOrUninomial, String specificEpithet, String infraSpecificEpithet, INomenclaturalAgent combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef) {
		super(rank, genusOrUninomial, specificEpithet, infraSpecificEpithet, combinationAuthorTeam, nomenclaturalReference, nomenclMicroRef);
		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	}

	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	public Set<HybridRelationship> getHybridRelationships() {
		return hybridRelationships;
	}
	protected void setHybridRelationships(Set<HybridRelationship> relationships) {
		this.hybridRelationships = relationships;
	}
	public void addHybridRelationship(HybridRelationship relationship) {
		this.hybridRelationships.add(relationship);
	}
	public void removeHybridRelationship(HybridRelationship relationship) {
		this.hybridRelationships.remove(relationship);
	}

	@Transient
	public Set<HybridRelationship> getParentRelationships() {
		// FIXME: filter relations
		return hybridRelationships;
	}
	@Transient
	public Set<HybridRelationship> getChildRelationships() {
		// FIXME: filter relations
		return hybridRelationships;
	}



	public boolean isHybridFormula(){
		return this.isHybridFormula;
	}

	/**
	 * 
	 * @param isHybridFormula    isHybridFormula
	 */
	public void setHybridFormula(boolean isHybridFormula){
		this.isHybridFormula = isHybridFormula;
	}

	public boolean isMonomHybrid(){
		return this.isMonomHybrid;
	}

	/**
	 * 
	 * @param isMonomHybrid    isMonomHybrid
	 */
	public void setMonomHybrid(boolean isMonomHybrid){
		this.isMonomHybrid = isMonomHybrid;
	}

	public boolean isBinomHybrid(){
		return this.isBinomHybrid;
	}

	/**
	 * 
	 * @param isBinomHybrid    isBinomHybrid
	 */
	public void setBinomHybrid(boolean isBinomHybrid){
		this.isBinomHybrid = isBinomHybrid;
	}

	public boolean isTrinomHybrid(){
		return this.isTrinomHybrid;
	}

	/**
	 * 
	 * @param isTrinomHybrid    isTrinomHybrid
	 */
	public void setTrinomHybrid(boolean isTrinomHybrid){
		this.isTrinomHybrid = isTrinomHybrid;
	}

	public boolean isAnamorphic(){
		return this.isAnamorphic;
	}

	/**
	 * 
	 * @param isAnamorphic    isAnamorphic
	 */
	public void setAnamorphic(boolean isAnamorphic){
		this.isAnamorphic = isAnamorphic;
	}

}