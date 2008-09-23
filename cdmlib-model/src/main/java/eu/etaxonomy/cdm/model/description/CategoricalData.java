/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;

import java.util.*;
import javax.persistence.*;

/**
 * This class represents information pieces expressed in categorical type of
 * data (in opposition to {@link QuantitativeData quantitative data} on one side and to literal data on
 * the other side). Only {@link TaxonDescription taxon descriptions} and
 * {@link SpecimenDescription specimen descriptions} may contain categorical data.<BR>
 * The "color of petals" {@link Feature feature} for instance can be described with
 * {@link State state terms} such as "blue" or "white". If the color of petals of a
 * particular tree is described as "mostly blue" and "exceptionally white" two
 * {@link StateData state data} instances must be assigned to an instance of the
 * present class: the first one with the state "blue" and the {@link Modifier modifier}
 * "mostly" and the second one with the state "white" and the modifier "exceptionally".  
 * <P>
 * This class corresponds partially to CodedDescriptionType according to
 * the SDD schema.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:15
 */
@Entity
public class CategoricalData extends DescriptionElementBase {
	static Logger logger = Logger.getLogger(CategoricalData.class);

	
	//whether the sequence of ordered states is important
	private boolean orderRelevant;
	private List<State> states = new ArrayList();

	
	/** 
	 * Class constructor: creates a new empty categorical data instance.
	 */
	protected CategoricalData() {
		super(null);
	}
	
	/** 
	 * Creates a new empty categorical data instance.
	 */
	public static CategoricalData NewInstance(){
		return new CategoricalData();
	}
	
	/** 
	 * Returns the (ordered) list of {@link State states} describing the {@link Feature feature}
	 * corresponding to <i>this</i> categorical data.
	 */
	@ManyToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public List<State> getStates(){
		return this.states;
	}
	/**
	 * @see	#getStates() 
	 */
	private void setStates(List<State> states){
		this.states = states;
	}
	/**
	 * Adds a {@link State state} to the list of {@link #getStates() states}
	 * describing the {@link Feature feature} corresponding to <i>this</i> categorical data.
	 * 
	 * @param state	the state to be added to <i>this</i> categorical data
	 * @see    	   	#getStates()
	 */
	public void addState(State state){
		this.states.add(state);
	}
	/** 
	 * Removes one element from the set of {@link #getStates() states}
	 * describing the {@link Feature feature} corresponding to <i>this</i> categorical data.
	 *
	 * @param  state	the state which should be removed
	 * @see     		#getStates()
	 * @see     		#addState(State)
	 */
	public void removeState(State state){
		this.states.remove(state);
	}

	/**
	 * Returns the boolean value of the flag indicating whether the {@link StateData state data}
	 * belonging to <i>this</i> categorical data should be treated as an
	 * {@link List "ordered" list} (true) according to the {@link State states} or as an
	 * {@link Set "unordered" set} (false). The use of this flag depends mostly
	 * on the {@link Feature feature} of <i>this</i> categorical data.
	 *  
	 * @return  the boolean value of the orderRelevant flag
	 */
	public boolean getOrderRelevant(){
		return this.orderRelevant;
	}
	/**
	 * @see	#getOrderRelevant() 
	 */
	public void setOrderRelevant(boolean orderRelevant){
		this.orderRelevant = orderRelevant;
	}

}