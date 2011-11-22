/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
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
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.constraints.NotEmpty;

import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.validation.Level2;

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
 * Whenever more than one state data belongs to a categorical data they should be
 * interpreted as being related by the inclusive disjunction "or".  
 * <P>
 * This class corresponds partially to CodedDescriptionType according to
 * the SDD schema.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:15
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CategoricalData", propOrder = {
    "orderRelevant",
    "states"
})
@XmlRootElement(name = "CategoricalData")
@Entity
@Audited
@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionElementBase")
public class CategoricalData extends DescriptionElementBase implements Cloneable{
	private static final long serialVersionUID = -6298361966947668998L;
	private static final Logger logger = Logger.getLogger(CategoricalData.class);

	//whether the sequence of ordered states is important
	@XmlElement(name = "OrderRelevant")
	private boolean orderRelevant;
	
	@XmlElementWrapper(name = "States")
	@XmlElement(name = "State")
	@ManyToMany(fetch = FetchType.LAZY)
	@Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE,CascadeType.DELETE_ORPHAN })
	@IndexedEmbedded(depth = 2)
	@NotEmpty(groups = Level2.class)
	private List<StateData> states = new ArrayList<StateData>();

	
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
		logger.debug("NewInstance");
		return new CategoricalData();
	}
	
	/** 
	 * Returns the (ordered) list of {@link State states} describing the {@link Feature feature}
	 * corresponding to <i>this</i> categorical data.
	 */
	
	public List<StateData> getStates(){
		return this.states;
	}
	
	protected void setStates(List<StateData> states){
		this.states = states;
	}
	
	/**
	 * Convenience method which returns only the list of states. Leaving out modifiers and modifying text.
	 * @return
	 */
	@Transient
	public List<State> getStatesOnly(){
		List<State> result = new ArrayList<State>();
		for (StateData stateData : getStates()){
			State state = stateData.getState();
			result.add(state);
		}
		return result;
	}

	/**
	 * Adds a {@link State state} to the list of {@link #getStates() states}
	 * describing the {@link Feature feature} corresponding to <i>this</i> categorical data.
	 * 
	 * @param state	the state to be added to <i>this</i> categorical data
	 * @see    	   	#getStates()
	 */
	public void addState(StateData state){
		this.states.add(state);
	}
	
	/**
	 * Convenience method which creates a state data from a given state with no modifiers
	 * and adds it to the list of state data
	 * @see #addState(StateData)
	 * @param state
	 */
	public void addState(State state){
		StateData stateData = StateData.NewInstance(state);
		this.states.add(stateData);
	}
	
	
	/** 
	 * Removes one element from the set of {@link #getStates() states}
	 * describing the {@link Feature feature} corresponding to <i>this</i> categorical data.
	 *
	 * @param  state	the state which should be removed
	 * @see     		#getStates()
	 * @see     		#addState(State)
	 */
	public void removeState(StateData state){
		this.states.remove(state);
	}

	//rename to isStateSequenceIntentional ??
	/**
	 * Returns the boolean value of the flag indicating whether the sequence of
	 * {@link StateData state data} belonging to <i>this</i> categorical data is intentional
	 * (true) and therefore relevant for interpretation or analysis or not (false).
	 * The use of this flag depends mostly on the {@link Feature feature} of <i>this</i> categorical data.
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
	

//*********************************** CLONE *****************************************/

	/** 
	 * Clones <i>this</i> categorical data. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> categorical data by
	 * modifying only some of the attributes.
	 * 
	 * @see eu.etaxonomy.cdm.model.description.DescriptionElementBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {

		try {
			CategoricalData result = (CategoricalData)super.clone();
			
			//states
			result.states = new ArrayList<StateData>();
			for (StateData stateData : getStates()){
				//TODO do we need to clone here? 
				//StateData newState = (StateData)stateData.clone();
				result.states.add(stateData);
			}
			
			return result;
			//no changes to: orderRelevant
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}	

}