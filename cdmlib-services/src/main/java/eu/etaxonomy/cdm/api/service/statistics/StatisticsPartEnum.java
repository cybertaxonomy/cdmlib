/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.statistics;

/**
 * @author a.mueller
 * @since 21.09.2012
 *
 */
public enum StatisticsPartEnum {
	
	ALL ("All", "All data in the datastore"),
	CLASSIFICATION("Classification", "Data per classification"),
	TAXON_NODE("TaxonNode","Data of tree from this taxon node")
	;
	
	
	
	private String label;
	private String description;
	
	private StatisticsPartEnum(String label, String description){
		this.label = label;
		this.description = description;
	}

	public String getLabel(){
		return label;
	}
	
	public String getDesription(){
		return description;
	}

	
//	public static StatisticsPartEnum getDefault(){
//		return ALL;
//	}
//	
}
