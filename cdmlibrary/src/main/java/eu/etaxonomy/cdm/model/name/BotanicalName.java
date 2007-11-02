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
 * @created 02-Nov-2007 19:35:59
 */
@Entity
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
	 * @param parentRelationships
	 */
	public void setParentRelationships(ArrayList parentRelationships){
		;
	}

	public ArrayList getChildRelationships(){
		return childRelationships;
	}

	/**
	 * 
	 * @param childRelationships
	 */
	public void setChildRelationships(ArrayList childRelationships){
		;
	}

	public boolean isHybridFormula(){
		return isHybridFormula;
	}

	/**
	 * 
	 * @param isHybridFormula
	 */
	public void setHybridFormula(boolean isHybridFormula){
		;
	}

	public boolean isMonomHybrid(){
		return isMonomHybrid;
	}

	/**
	 * 
	 * @param isMonomHybrid
	 */
	public void setMonomHybrid(boolean isMonomHybrid){
		;
	}

	public boolean isBinomHybrid(){
		return isBinomHybrid;
	}

	/**
	 * 
	 * @param isBinomHybrid
	 */
	public void setBinomHybrid(boolean isBinomHybrid){
		;
	}

	public boolean isTrinomHybrid(){
		return isTrinomHybrid;
	}

	/**
	 * 
	 * @param isTrinomHybrid
	 */
	public void setTrinomHybrid(boolean isTrinomHybrid){
		;
	}

	public boolean isAnamorphic(){
		return isAnamorphic;
	}

	/**
	 * 
	 * @param isAnamorphic
	 */
	public void setAnamorphic(boolean isAnamorphic){
		;
	}

}