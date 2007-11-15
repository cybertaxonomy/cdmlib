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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
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
	private Set<HybridRelationship> parentRelationships;
	private Set<HybridRelationship> childRelationships;

	public BotanicalName(Rank rank) {
		super(rank);
	}

	

	@OneToMany
	public Set<HybridRelationship> getParentRelationships() {
		return parentRelationships;
	}
	protected void setParentRelationships(Set<HybridRelationship> parentRelationships) {
		this.parentRelationships = parentRelationships;
	}
	public void addParentRelationships(HybridRelationship parentRelationship) {
		this.parentRelationships.add(parentRelationship);
	}
	public void removeParentRelationships(HybridRelationship parentRelationship) {
		this.parentRelationships.remove(parentRelationship);
	}



	@OneToMany
	public Set<HybridRelationship> getChildRelationships() {
		return childRelationships;
	}
	protected void setChildRelationships(Set<HybridRelationship> childRelationships) {
		this.childRelationships = childRelationships;
	}
	public void addChildRelationship(HybridRelationship childRelationship) {
		this.childRelationships.add(childRelationship);
	}
	public void removeChildRelationship(HybridRelationship childRelationship) {
		this.childRelationships.remove(childRelationship);
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