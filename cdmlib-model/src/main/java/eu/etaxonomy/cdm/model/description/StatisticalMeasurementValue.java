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

import java.util.*;

import javax.persistence.*;

/**
 * This class represents the assignment of numerical values to {@link Feature features}
 * corresponding to {@link QuantitativeData quantitative data}. A statistical measurement
 * value instance constitutes an atomized part of an information piece
 * (quantitative data) so that several statistical measurement value instances
 * may belong to one quantitative data instance.
 * <P>
 * This class corresponds to CharacterMeasureDataType according
 * to the SDD schema.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:54
 */
@Entity
public class StatisticalMeasurementValue extends VersionableEntity {
	static Logger logger = Logger.getLogger(StatisticalMeasurementValue.class);
	private float value;
	private Set<Modifier> modifiers = new HashSet();
	private StatisticalMeasure type;


	/** 
	 * Class constructor: creates a new empty statistical measurement value
	 * instance.
	 */
	protected StatisticalMeasurementValue(){
		super();
	}
	
	/** 
	 * Creates a new empty statistical measurement value instance.
	 */
	public static StatisticalMeasurementValue NewInstance(){
		return new StatisticalMeasurementValue();
	}
	
	/** 
	 * Returns the type of {@link StatisticalMeasure statistical measure} used in
	 * <i>this</i> statistical measurement value.
	 */
	@ManyToOne
	public StatisticalMeasure getType(){
		return this.type;
	}
	/** 
	 * @see	#getType()
	 */
	public void setType(StatisticalMeasure type){
		this.type = type;
	}


	/** 
	 * Returns the numerical value used to describe the {@link Feature feature}
	 * corresponding to the {@link QuantitativeData quantitative data} <i>this</i>
	 * statistical measurement value belongs to.
	 */
	public float getValue(){
		return this.value;
	}
	/** 
	 * @see	#getValue()
	 */
	public void setValue(float value){
		this.value = value;
	}
	
	
	/** 
	 * Returns the set of {@link Modifier modifiers} used to qualify the validity
	 * or probability of <i>this</i> statistical measurement value.
	 * This is only metainformation.
	 */
	@OneToMany
	public Set<Modifier> getModifiers() {
		return modifiers;
	}
	/**
	 * @see	#getModifiers() 
	 */
	protected void setModifiers(Set<Modifier> modifiers) {
		this.modifiers = modifiers;
	}
	/**
	 * Adds a {@link Modifier modifier} to the set of {@link #getModifiers() modifiers}
	 * used to qualify the validity of <i>this</i> statistical measurement value.
	 * 
	 * @param modifier	the modifier to be added to <i>this</i> statistical measurement value
	 * @see    	   		#getModifiers()
	 */
	public void addModifier(Modifier modifier) {
		this.modifiers.add(modifier);
	}
	/** 
	 * Removes one element from the set of {@link #getModifiers() modifiers}
	 * used to qualify the validity of <i>this</i> statistical measurement value.
	 *
	 * @param  modifier	the modifier which should be removed
	 * @see     		#getModifiers()
	 * @see     		#addModifier(Modifier)
	 */
	public void removeModifier(Modifier modifier) {
		this.modifiers.remove(modifier);
	}

}