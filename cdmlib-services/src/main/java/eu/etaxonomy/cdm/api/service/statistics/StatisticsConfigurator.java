/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * This class configures the statistical request to 
 * a CDM Store. It defines which statistical values should
 * be computed and returned.
 * 
 * on a CDM Store
 * @author a.mueller , s.buers
 * @date 21.09.2012
 *
 */

public class StatisticsConfigurator {
	

	//*********************** MEMBERS ********************************

	// the filter list represents the path to 
	private List<IdentifiableEntity> filter = new ArrayList<IdentifiableEntity>();

	private List<StatisticsTypeEnum> type = new ArrayList<StatisticsTypeEnum>();
	
	
	
	// *********************** CONSTRUCTORS ***************************
	
	
	public StatisticsConfigurator(){}
	
	public StatisticsConfigurator(List<IdentifiableEntity> filter, List<StatisticsTypeEnum> types){
		this.filter=filter;
		this.type=types;
	}
	
	
	// *************************** METHODS ******************************/
	
	public List<StatisticsTypeEnum> getType() {
		return Collections.unmodifiableList(type);
	}
	
	public void addType(StatisticsTypeEnum type){
		this.type.add(type);
	}
	
	public void setType(List<StatisticsTypeEnum> newType) {
		this.type=newType;	
	}

	//------------------------------------------------
	
	public List<IdentifiableEntity> getFilter() {
		return filter;
	}
	
	public void addFilter(IdentifiableEntity filterItem) {
		this.filter.add(filterItem);
	}



	
	
	
	
}
