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

import java.util.Collections;
import java.util.List;

/**
 * This class configures the statistical request to 
 * a CDM Store. It defines which statistical values should
 * be computed and returned.
 * 
 * on a CDM Store
 * @author a.mueller
 * @date 21.09.2012
 *
 */
public class StatisticsConfigurator {
	
	public static StatisticsConfigurator NewDefaultAllConfigurator(){
		StatisticsConfigurator result = new StatisticsConfigurator();
		result.addPart(StatisticsPartEnum.ALL);
		return result;
	}
	
	private List<StatisticsPartEnum> partList;

	
// *************************** METHODS ******************************/
	
	public void addPart(StatisticsPartEnum part) {
		this.partList.add(part);
	}
	
	public void addPart(int index, StatisticsPartEnum part) {
		this.partList.add(index, part);
	}
	
	public List<StatisticsPartEnum> getPartList() {
		return Collections.unmodifiableList(partList);
	}
	
	
	
	
}
