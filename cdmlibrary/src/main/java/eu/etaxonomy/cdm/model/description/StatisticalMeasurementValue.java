/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:34
 */
@Entity
public class StatisticalMeasurementValue extends VersionableEntity {
	static Logger logger = Logger.getLogger(StatisticalMeasurementValue.class);

	@Description("")
	private float value;
	private ArrayList modifiers;
	private StatisticalMeasure type;

	public StatisticalMeasure getType(){
		return type;
	}

	/**
	 * 
	 * @param type
	 */
	public void setType(StatisticalMeasure type){
		;
	}

	public ArrayList getModifiers(){
		return modifiers;
	}

	/**
	 * 
	 * @param modifiers
	 */
	public void setModifiers(ArrayList modifiers){
		;
	}

	public float getValue(){
		return value;
	}

	/**
	 * 
	 * @param value
	 */
	public void setValue(float value){
		;
	}

}