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

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:15
 */
@Entity
public class CategoricalData extends FeatureBase {
	static Logger logger = Logger.getLogger(CategoricalData.class);
	//whether the sequence of ordered states is important
	private boolean orderRelevant;
	private List<State> states = new ArrayList();

	public CategoricalData() {
		super();
	}
	
	@ManyToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public List<State> getStates(){
		return this.states;
	}
	private void setStates(List<State> states){
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
	public void setOrderRelevant(boolean orderRelevant){
		this.orderRelevant = orderRelevant;
	}

}