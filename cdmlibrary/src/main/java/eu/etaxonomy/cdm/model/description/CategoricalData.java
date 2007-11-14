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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:15
 */
@Entity
public class CategoricalData extends FeatureBase {
	public CategoricalData() {
		super();
	}

	static Logger logger = Logger.getLogger(CategoricalData.class);
	//whether the sequence of ordered states is important
	private boolean orderRelevant;
	private ArrayList<State> states;

	public ArrayList<State> getStates(){
		return this.states;
	}
	private void setStates(ArrayList<State> states){
		this.states = states;
	}
	public void addState(State state){
		this.states.add(state);
	}
	public void removeState(State state){
		this.states.remove(state);
	}

	public boolean getOrderRelevant(){
		return this.orderRelevant;
	}

	/**
	 * 
	 * @param orderRelevant    orderRelevant
	 */
	public void setOrderRelevant(boolean orderRelevant){
		this.orderRelevant = orderRelevant;
	}

}