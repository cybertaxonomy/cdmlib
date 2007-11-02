/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.name;


import org.apache.log4j.Logger;

/**
 * Taxon name class for plants
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:39
 */
public class BotanicalName extends NonViralName {
	static Logger logger = Logger.getLogger(BotanicalName.class);

	//if set: this name is a hybrid formula (a hybrid that does not have an own name) and no other hybrid flags may be set. A
	//hybrid name  may not have either an authorteam nor other name components. 
	@Description("if set: this name is a hybrid formula (a hybrid that does not have an own name) and no other hybrid flags may be set. A hybrid name  may not have either an authorteam nor other name components. ")
	private boolean isHybridFormula = False;
	@Description("")
	private boolean isMonomHybrid = False;
	@Description("")
	private boolean isBinomHybrid = False;
	@Description("")
	private boolean isTrinomHybrid = False;
	//Only for fungi: to indicate that the type of the name is asexual or not
	@Description("Only for fungi: to indicate that the type of the name is asexual or not")
	private boolean isAnamorphic;
	private ArrayList parentRelationships;
	private ArrayList childRelationships;

	public ArrayList getParentRelationships(){
		return parentRelationships;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setParentRelationships(ArrayList newVal){
		parentRelationships = newVal;
	}

	public ArrayList getChildRelationships(){
		return childRelationships;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setChildRelationships(ArrayList newVal){
		childRelationships = newVal;
	}

	public boolean isHybridFormula(){
		return isHybridFormula;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setHybridFormula(boolean newVal){
		isHybridFormula = newVal;
	}

	public boolean isMonomHybrid(){
		return isMonomHybrid;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setMonomHybrid(boolean newVal){
		isMonomHybrid = newVal;
	}

	public boolean isBinomHybrid(){
		return isBinomHybrid;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setBinomHybrid(boolean newVal){
		isBinomHybrid = newVal;
	}

	public boolean isTrinomHybrid(){
		return isTrinomHybrid;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTrinomHybrid(boolean newVal){
		isTrinomHybrid = newVal;
	}

	public boolean isAnamorphic(){
		return isAnamorphic;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAnamorphic(boolean newVal){
		isAnamorphic = newVal;
	}

}