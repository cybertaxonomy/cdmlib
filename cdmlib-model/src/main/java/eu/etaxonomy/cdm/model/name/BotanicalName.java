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

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.cache.BotanicNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

import java.util.*;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Taxon name class for plants
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:15
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
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
	
	
	/**
	 * @param rank
	 * @return
	 */
	public static BotanicalName NewInstance(Rank rank){
		return new BotanicalName(rank, null);
	}


	/**
	 * @param rank
	 * @param homotypicalGroup
	 * @return
	 */
	public static BotanicalName NewInstance(Rank rank, HomotypicalGroup homotypicalGroup){
		return new BotanicalName(rank, homotypicalGroup);
	}
	
	public static  BotanicalName NewInstance(Rank rank, String genusOrUninomial, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		return new BotanicalName(rank, genusOrUninomial, specificEpithet, infraSpecificEpithet, combinationAuthorTeam, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
	}
	
	/**
	 * Returns a parsed Name
	 * @param fullName
	 * @return
	 */
	public static BotanicalName PARSED_NAME(String fullNameString){
		return PARSED_NAME(fullNameString, Rank.GENUS());
	}
	
	/**
	 * Returns a parsed Name
	 * @param fullName
	 * @return
	 */
	public static BotanicalName PARSED_NAME(String fullNameString, Rank rank){
		if (nameParser == null){
			nameParser = new NonViralNameParserImpl();
		}
		return (BotanicalName)nameParser.parseFullName(fullNameString, NomenclaturalCode.ICBN(),  rank);
	}
	
	/**
	 * Returns a parsed Name
	 * @param fullName
	 * @return
	 */
	public static BotanicalName PARSED_REFERENCE(String fullNameAndReferenceString){
		return PARSED_REFERENCE(fullNameAndReferenceString, Rank.GENUS());
	}
	
	/**
	 * Returns a parsed Name
	 * @param fullName
	 * @return
	 */
	public static BotanicalName PARSED_REFERENCE(String fullNameAndReferenceString, Rank rank){
		if (nameParser == null){
			nameParser = new NonViralNameParserImpl();
		}
		return (BotanicalName)nameParser.parseFullReference(fullNameAndReferenceString, NomenclaturalCode.ICBN(), rank);
	}
	
	//needed by hibernate
	protected BotanicalName(){
		super();
		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	}
	protected BotanicalName(Rank rank, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
		this.cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	}
	protected BotanicalName(Rank rank, String genusOrUninomial, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		super(rank, genusOrUninomial, specificEpithet, infraSpecificEpithet, combinationAuthorTeam, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
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

	public void addRelationship(RelationshipBase relation) {
		if (relation instanceof HybridRelationship){
			addHybridRelationship((HybridRelationship)relation);
		}else {
			super.addRelationship(relation);
		}
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
	
	
	@Transient
	@Override
	public NomenclaturalCode getNomeclaturalCode(){
		return NomenclaturalCode.ICBN();

	}

}