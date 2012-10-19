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
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author a.oppermann
 * @date 18.10.2012
 *
 */
public class DwcaTaxRecordRedlist extends DwcaRecordBaseRedlist{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaTaxRecordRedlist.class);

	private DwcaId scientificNameId;
	private String scientificName;
	private String taxonomicStatus;
	private DwcaId datasetId;
	private String datasetName;
	private ArrayList<String> synonyms;

	
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
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void write(PrintWriter writer) {
		print(datasetName, writer, IS_FIRST, TermUri.DWC_DATASET_NAME);
		print(scientificName, writer, IS_NOT_FIRST, TermUri.DWC_SCIENTIFIC_NAME);
		print(taxonomicStatus, writer, IS_NOT_FIRST, TermUri.DWC_TAXONOMIC_STATUS);
		for (String synonym:synonyms){
			print(synonym, writer, IS_NOT_FIRST, TermUri.DWC_SCIENTIFIC_NAME);	
		}
		
		
//		if (config.isWithHigherClassification()){
//			print(higherClassification, writer, IS_NOT_FIRST, TermUri.DWC_HIGHER_CLASSIFICATION);
//			print(kingdom, writer, IS_NOT_FIRST, TermUri.DWC_KINGDOM);
//			print(phylum, writer, IS_NOT_FIRST, TermUri.DWC_PHYLUM);
//			print(clazz, writer, IS_NOT_FIRST, TermUri.DWC_CLASS);
//			print(order, writer, IS_NOT_FIRST, TermUri.DWC_ORDER);
//			print(family, writer, IS_NOT_FIRST, TermUri.DWC_FAMILY);
//			print(genus, writer, IS_NOT_FIRST, TermUri.DWC_GENUS);
//			print(subgenus, writer, IS_NOT_FIRST, TermUri.DWC_SUBGENUS);
//		}
		writer.println();
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
//	public String getSynonym() {
//		return synonym;
//	}
//	public void setSynonym(String synonym) {
//		this.synonym = synonym;
//	}
	public void setSynonyms(ArrayList<String> synonyms){
		this.synonyms = synonyms;
	}

	
}
