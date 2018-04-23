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
import java.util.Map;

/**
 * This class is meant to hold the result of a statistical request to a CDM
 * Store. It e.g. holds the information how many classifications, taxa, names,
 * synonyms, references, etc. are stored in the store. This information may also
 * be hold for certain parts of a store. E.g. may store the statistical
 * information for each classification. This partitioninc is recursive so a
 * partition is again represented by a {@link Statistics}.
 * 
 * @author a.mueller, a.kohlbecker, s.buers
 \* @since 21.09.2012
 */
public class Statistics {

	private StatisticsConfigurator request;

	// it's a pitty, but for JSON Map keys must be Strings
	// see also: JSONObject _fromMap( Map map, JsonConfig jsonConfig )
	// --> TODO: modify MapJSONValueProcessor.processArrayValue(Object value,
	// JsonConfig jsonConfig)???

	private Map<String, Number> countMap;

	public Statistics(StatisticsConfigurator configurator) {
		this.request = configurator;
		this.countMap = new HashMap<String, Number>();
	}

	public void setRequest(StatisticsConfigurator request) {
		this.request = request;
	}

	public StatisticsConfigurator getRequest() {
		return request;
	}

	public Map<String, Number> getCountMap() {
		return countMap;
	}

	public void addCount(StatisticsTypeEnum type, Long number) {
		this.countMap.put(type.getLabel(), number);
	}
	
}
