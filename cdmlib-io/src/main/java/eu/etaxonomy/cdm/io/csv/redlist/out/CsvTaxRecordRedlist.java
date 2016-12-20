/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.csv.redlist.out;

import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author a.oppermann
 * @date 18.10.2012
 *
 */
public class CsvTaxRecordRedlist extends CsvRecordBaseRedlist{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CsvTaxRecordRedlist.class);

	private String scientificNameId;
	private String scientificName;
	private String taxonomicStatus;
	private CsvId datasetId;
	private String datasetName;
	private ArrayList<String> synonyms;
	private String threadStatus;
	private ArrayList<String> countryCodes;
	private ArrayList<String> headlines;
	private boolean isHeadLinePrinted;
	private List<Feature> features;
	private List<List<String>> featuresCells;

	/**
	 * 
	 * @param metaDataRecord
	 * @param config
	 */
	public CsvTaxRecordRedlist(CsvMetaDataRecordRedlist metaDataRecord, CsvTaxExportConfiguratorRedlist config){
		super(metaDataRecord, config);
		datasetId = new CsvId(config);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.dwca.out.DwcaRecordBase#registerKnownFields()
	 */
	protected void registerKnownFields(){
		try {
			addKnownField(TermUri.DWC_SCIENTIFIC_NAME);
			addKnownField(TermUri.DWC_TAXONOMIC_STATUS);
			addKnownField(TermUri.DWC_DATASET_NAME);
			addKnownField("countryCode", "http://rs.tdwg.org/dwc/terms/countryCode");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(PrintWriter writer) {
		if(isHeadLinePrinted()){
			printHeadline(writer, setHeadlines(), TermUri.DWC_DATASET_NAME);
			isHeadLinePrinted=false;
		}

		print(datasetName, writer, IS_FIRST, TermUri.DWC_DATASET_NAME);
		print(scientificName, writer, IS_NOT_FIRST, TermUri.DWC_SCIENTIFIC_NAME);
		print(scientificNameId, writer, IS_NOT_FIRST, TermUri.DWC_DATASET_ID);
		print(taxonomicStatus, writer, IS_NOT_FIRST, TermUri.DWC_TAXONOMIC_STATUS);
		print(synonyms, TermUri.DWC_SCIENTIFIC_NAME, writer);
		print(countryCodes, TermUri.DWC_COUNTRY_CODE, writer);
		if(features != null ||featuresCells != null || !featuresCells.isEmpty()){
			for(List<String> featureList : featuresCells) {
				print((ArrayList<String>)featureList, TermUri.DWC_LIFESTAGE, writer);
			}
		}
		writer.println();
	}

	public String getDatasetName() {
		return datasetName;
	}
	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}
	public String getScientificName() {
		return scientificName;
	}
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}
	public String getTaxonomicStatus() {
		return taxonomicStatus;
	}
	public void setTaxonomicStatus(String taxonomicStatus) {
		this.taxonomicStatus = taxonomicStatus;
	}
	public String getDatasetId() {
		return datasetId.getId();
	}
	public void setDatasetId(Classification classification) {
		this.datasetId.setId(classification);
	}
	public void setSynonyms(ArrayList<String> synonyms){
		this.synonyms = synonyms;
	}
	public ArrayList<String> getSynonyms(){
		return synonyms;
	}
	public String getThreadStatus() {
		return threadStatus;
	}
	public void setThreadStatus(String threadStatus) {
		this.threadStatus = threadStatus;
	}

	public void setCountryCode(ArrayList<String> countryCodes) {
		this.countryCodes = countryCodes;
	}

	//FIXME: hard coded header lines
	private ArrayList<String> setHeadlines(){
		headlines = new ArrayList<String>(); 
		Collections.addAll(headlines, "Classification","Wissenschaftlicher Name","Taxon ID","Taxon Status","Synonym","Distribution");

		if(features != null || !features.isEmpty()){
			for(Feature f : features) {
				headlines.add(f.getLabel());
			}
		}
		return headlines;
	}

	public boolean isHeadLinePrinted() {
		return isHeadLinePrinted;
	}

	public void setHeadLinePrinted(boolean isHeadLinePrinted) {
		this.isHeadLinePrinted = isHeadLinePrinted;
	}

	public void setScientificNameId(String scientificNameId) {
		this.scientificNameId = scientificNameId;
	}

	public void setPrintFeatures(List<Feature> features) {
		this.features = features;
		
	}

	public void setFeatures(List<List<String>> featureCells) {
		this.featuresCells = featureCells;
		
	}

}
