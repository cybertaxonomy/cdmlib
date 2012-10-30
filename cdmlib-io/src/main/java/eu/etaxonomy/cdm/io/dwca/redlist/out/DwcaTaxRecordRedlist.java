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
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author a.oppermann
 * @date 18.10.2012
 *
 */
public class DwcaTaxRecordRedlist extends DwcaRecordBaseRedlist implements IDwcaAreaRecord{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaTaxRecordRedlist.class);

	private DwcaId scientificNameId;
	private String scientificName;
	private String taxonomicStatus;
	private DwcaId datasetId;
	private String datasetName;
	private ArrayList<String> synonyms;
	private DwcaId locationId;
	private String locationIdString;
	private String locality;
	private String threadStatus;
	private ArrayList<String> countryCodes;
	private String rlStatus;
	private ArrayList<String> headlines;
	private boolean isHeadLinePrinted;
	


	public DwcaTaxRecordRedlist(DwcaMetaDataRecordRedlist metaDataRecord, DwcaTaxExportConfiguratorRedlist config){
		super(metaDataRecord, config);
		scientificNameId = new DwcaId(config);
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
//			addKnownField("threatStatus", "http://iucn.org/terms/threatStatus");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void write(PrintWriter writer) {
		if(isHeadLinePrinted()){
			printHeadlines(writer);
		}

		print(datasetName, writer, IS_FIRST, TermUri.DWC_DATASET_NAME);
		print(scientificName, writer, IS_NOT_FIRST, TermUri.DWC_SCIENTIFIC_NAME);
		print(taxonomicStatus, writer, IS_NOT_FIRST, TermUri.DWC_TAXONOMIC_STATUS);
		prettyPrintRedlist(synonyms, TermUri.DWC_SCIENTIFIC_NAME, writer);
		prettyPrintRedlist(countryCodes, TermUri.DWC_COUNTRY_CODE, writer);
		print(rlStatus, writer, IS_NOT_FIRST, TermUri.DWC_LIFESTAGE);

		writer.println();
	}
	

	private void printHeadlines(PrintWriter writer){
		setHeadlines();
		for(String headline:headlines){
			if(headlines.get(0).equals(headline)){
				print(headline, writer, IS_FIRST, TermUri.DWC_DATASET_NAME);
			}
			else{
				print(headline, writer, IS_NOT_FIRST, TermUri.DWC_DATASET_NAME);
			}
		}
		writer.println();
		isHeadLinePrinted=false;
	}
	
	private void prettyPrintRedlist(ArrayList<String> list, TermUri termUri, PrintWriter writer){
		if(list.isEmpty()){
			print("", writer, IS_NOT_FIRST, termUri);
		}else{
			for (String element:list){
				if(list.get(0).equals(element)){
					writer.write("\t");
					print(element, writer, IS_FIRST, termUri);
					if(list.size()>1)
						writer.write(",");
				}else if(list.get(list.size()-1).equals(element)){
					print(element, writer, IS_FIRST, termUri);
				}else{
					print(element, writer, IS_FIRST, termUri);
					writer.write(",");
				}
			}
		}
	}
	

	public String getScientificNameId() {
		return scientificNameId.getId();
	}
	public void setScientificNameId(TaxonNameBase<?, ?> scientificNameId) {
		this.scientificNameId.setId(scientificNameId);
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

	@Override
	public void setLocationId(NamedArea area) {
		if (area.isInstanceOf(TdwgArea.class)){
			String locationId = "TDWG:" + area.getRepresentation(Language.ENGLISH()).getAbbreviatedLabel();
			this.locationIdString = locationId;
		}else if (area.isInstanceOf(WaterbodyOrCountry.class)){
			WaterbodyOrCountry country = CdmBase.deproxy(area, WaterbodyOrCountry.class);
			String locationId = "ISO3166:" + country.getIso3166_A2();
			this.locationIdString = locationId;
		}else{
			this.locationId.setId(area);
		}
		
	}
	public String getLocationId() {
		return locationId.getId();
	}

	@Override
	public void setLocality(String locality) {
		this.locality = locality;
		
	}

	@Override
	public void setCountryCode(String iso3166_A2) {
		// TODO Auto-generated method stub
		
	}
	
	public void setRlStatus(String rlStatus) {
		this.rlStatus = rlStatus;
	}
	
	//FIXME: hardcoded headerlines
	private void setHeadlines(){
		headlines = new ArrayList<String>(); 
		Collections.addAll(headlines, "Classification","Taxon","Taxon Status","Synonym","Distribution", "Redlist Status");
	}
	
	public boolean isHeadLinePrinted() {
		return isHeadLinePrinted;
	}

	public void setHeadLinePrinted(boolean isHeadLinePrinted) {
		this.isHeadLinePrinted = isHeadLinePrinted;
	}
}
