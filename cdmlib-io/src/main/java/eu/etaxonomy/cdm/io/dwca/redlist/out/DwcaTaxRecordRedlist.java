// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.dwca.redlist.out;

import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author a.oppermann
 * @date 18.10.2012
 *
 */
public class DwcaTaxRecordRedlist extends DwcaRecordBaseRedlist{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaTaxRecordRedlist.class);

	private String scientificName;
	private String taxonomicStatus;
	private DwcaId datasetId;
	private String datasetName;
	private ArrayList<String> synonyms;
	private String threadStatus;
	private ArrayList<String> countryCodes;
	private ArrayList<String> rlStatus96;
	private ArrayList<String> rlStatus13;
	private ArrayList<String> headlines;
	private boolean isHeadLinePrinted;
	private boolean printRl96 = false;
	private boolean printRl13 = false;


	public DwcaTaxRecordRedlist(DwcaMetaDataRecordRedlist metaDataRecord, DwcaTaxExportConfiguratorRedlist config){
		super(metaDataRecord, config);
		//scientificNameId = new DwcaId(config);
		datasetId = new DwcaId(config);
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
			printHeadlines(writer, setHeadlines(), TermUri.DWC_DATASET_NAME);
			isHeadLinePrinted=false;
		}

		print(datasetName, writer, IS_FIRST, TermUri.DWC_DATASET_NAME);
		print(scientificName, writer, IS_NOT_FIRST, TermUri.DWC_SCIENTIFIC_NAME);
		print(taxonomicStatus, writer, IS_NOT_FIRST, TermUri.DWC_TAXONOMIC_STATUS);
		prettyPrintRedlist(synonyms, TermUri.DWC_SCIENTIFIC_NAME, writer);
		prettyPrintRedlist(countryCodes, TermUri.DWC_COUNTRY_CODE, writer);
		if(printRl96)prettyPrintRedlist(rlStatus96, TermUri.DWC_LIFESTAGE, writer);
		if(printRl13)prettyPrintRedlist(rlStatus13, TermUri.DWC_LIFESTAGE, writer);
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

	public void setRlStatus96(ArrayList<String> rlStatus96) {
		this.rlStatus96 = rlStatus96;
		printRl96 = true;
	}

	public void setRlStatus13(ArrayList<String> rlStatus13) {
		this.rlStatus13 = rlStatus13;
		printRl13 = true;
	}
	//FIXME: hard coded header lines
	private ArrayList<String> setHeadlines(){
		headlines = new ArrayList<String>(); 
		Collections.addAll(headlines, "Classification","Taxon","Taxon Status","Synonym","Distribution");
		if(printRl96)headlines.add("Rote Liste Satus 1996");
		if(printRl13)headlines.add("Rote Liste Satus 2013");
		
		return headlines;
	}

	public boolean isHeadLinePrinted() {
		return isHeadLinePrinted;
	}

	public void setHeadLinePrinted(boolean isHeadLinePrinted) {
		this.isHeadLinePrinted = isHeadLinePrinted;
	}

	public void setPrintRl96(boolean printRl96) {
		this.printRl96 = printRl96;
	}

	public void setPrintRl13(boolean printRl13) {
		this.printRl13 = printRl13;
	}

}
