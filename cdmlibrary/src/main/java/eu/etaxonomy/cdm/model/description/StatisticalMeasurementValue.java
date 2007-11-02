/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.description;


import etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:19
 */
public class StatisticalMeasurementValue extends VersionableEntity {
	static Logger logger = Logger.getLogger(StatisticalMeasurementValue.class);

	@Description("")
	private float value;
	private StatisticalMeasure type;
	private ArrayList modifiers;

	public StatisticalMeasure getType(){
		return type;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setType(StatisticalMeasure newVal){
		type = newVal;
	}

	public ArrayList getModifiers(){
		return modifiers;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setModifiers(ArrayList newVal){
		modifiers = newVal;
	}

	public float getValue(){
		return value;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setValue(float newVal){
		value = newVal;
	}

}