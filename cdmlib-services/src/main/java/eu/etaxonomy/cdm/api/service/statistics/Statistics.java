// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * This class is meant to hold the result of a statistical request
 * to a CDM Store.
 * It e.g. holds the information how many classifications, taxa,
 * names, synonyms, references, etc. are stored in the store.
 * This information may also be hold for certain parts of a store. E.g.
 * may store the statistical information for each classification.
 * This partitioninc is recursive so a partition is again represented
 * by a {@link Statistics}.
 * 
 * @author a.mueller
 * @date 21.09.2012
 */
public class Statistics {
	
//	private StatisticsConfigurator request;
	
	private Map<StatisticsTypeEnum, Number> countMap;
	
	private Map<StatisticsPartEnum, Map<IdentifiableEntity, Statistics>> partList;

	
//	public Statistics(StatisticsConfigurator configurator){
//		this.request=configurator;
////		this.countMap= new HashMap<StatisticsTypeEnum, Number>();
//		
//	}
//	
//	public void setRequest(StatisticsConfigurator request) {
//		this.request = request;
//	}
//
//	public StatisticsConfigurator getRequest() {
//		return request;
//	}

	public Map<StatisticsTypeEnum, Number> getCountMap() {
		return countMap;
	}

	public Map<StatisticsPartEnum, Map<IdentifiableEntity, Statistics>> getPartList() {
		return partList;
	}
	
}
