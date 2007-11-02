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

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:04
 */
public class CategoricalData extends FeatureBase {
	static Logger logger = Logger.getLogger(CategoricalData.class);

	//whether the sequence of ordered states is important
	@Description("whether the sequence of ordered states is important")
	private boolean orderRelevant;
	private java.util.ArrayList states;

	public java.util.ArrayList getStates(){
		return states;
	}

	/**
	 * 
	 * @param states
	 */
	public void setStates(java.util.ArrayList states){
		;
	}

	public boolean getOrderRelevant(){
		return orderRelevant;
	}

	/**
	 * 
	 * @param orderRelevant
	 */
	public void setOrderRelevant(boolean orderRelevant){
		;
	}

}