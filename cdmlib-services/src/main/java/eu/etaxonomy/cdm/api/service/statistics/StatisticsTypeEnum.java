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
 * @author a.mueller, (s.buers)
 \* @since 21.09.2012
 *
 */
public enum StatisticsTypeEnum {
	
	//TODO keep only one of the parameters label or description
//	ALL ("All", "All data in the datastore");
	CLASSIFICATION("Classifications", "All classifications"),
	ALL_TAXA("All_taxa", "Accepted taxa and synonyms (concepts)"),
	ACCEPTED_TAXA("Accepted_taxa", "Accepted taxa"),
	SYNONYMS("Synonyms", "All synonyms"),
	TAXON_NAMES("Taxon_names", "All taxon names (not"),
	ALL_REFERENCES("References", "References"),
	NOMENCLATURAL_REFERENCES("Nomenclatural_references", "Nomenclatural references"),
	DESCRIPTIONS("Descriptions", "Descriptions"),
	DESCRIPTIVE_SOURCE_REFERENCES("Descriptive_source_references", "Descriptive source references")
	;
	
	
	
	private String label;
	
	private String description;
	
	private StatisticsTypeEnum(String label, String description){
		this.label = label;
		this.description = description;
	}

	public String getLabel(){
		return label;
	}
	
	public String getDesription(){
		return description;
	}
	
}
