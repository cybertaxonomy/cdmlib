/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.redlist.out;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.dwca.out.DwcaTaxRecord;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * @author a.mueller
 * @created 18.04.2011
 */
@Component
public class DwcaTaxExportRedlist extends DwcaExportBaseRedlist {
	private static final Logger logger = Logger.getLogger(DwcaTaxExportRedlist.class);

	private static final String ROW_TYPE = "http://rs.tdwg.org/dwc/terms/Taxon";
	private static final String fileName = "RedlistCoreTax.txt";
	
	/**
	 * 
	 */
	public DwcaTaxExportRedlist() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}

	/** Retrieves data from a CDM DB and serializes them CDM to XML.
	 * Starts with root taxa and traverses the classification to retrieve children taxa, synonyms and relationships.
	 * Taxa that are not part of the classification are not found.
	 * 
	 * @param exImpConfig
	 * @param dbname
	 * @param filename
	 */
	@Override
	protected void doInvoke(DwcaTaxExportStateRedlist state){
		DwcaTaxExportConfiguratorRedlist config = state.getConfig();
		TransactionStatus txStatus = startTransaction(true);
		
		DwcaMetaDataRecordRedlist metaRecord = new DwcaMetaDataRecordRedlist(true, fileName, ROW_TYPE);
		state.addMetaRecord(metaRecord);
		
		Set<UUID> classificationUuidSet = config.getClassificationUuids();
		List<Classification> classificationList = getClassificationService().find(classificationUuidSet);
		Set<Classification> classificationSet = new HashSet<Classification>();
		classificationSet.addAll(classificationList);
		
		
		PrintWriter writer = null;
		try {
			writer = createPrintWriter(fileName, state);

			List<TaxonNode> allNodes =  getAllNodes(classificationSet);
			for (TaxonNode node : allNodes){
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				DwcaTaxRecordRedlist record = new DwcaTaxRecordRedlist(metaRecord, config);
				
				NonViralName<?> name = CdmBase.deproxy(taxon.getName(), NonViralName.class);
				Classification classification = node.getClassification();
				if (! this.recordExists(taxon)){
						handleTaxonBase(record, taxon, name, taxon, classification, null, false, false, config);
						record.write(writer);
						this.addExistingRecord(taxon);
					}
				node.getClassification().getName();

				//misapplied names
				handleMisapplication(taxon, writer, classification, metaRecord, config);
				
				writer.flush();
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			closeWriter(writer, state);
		}
		commitTransaction(txStatus);
		return;
		
	}

	

	private void handleMisapplication(Taxon taxon, PrintWriter writer, Classification classification, DwcaMetaDataRecordRedlist metaRecord, DwcaTaxExportConfiguratorRedlist config) {
		Set<Taxon> misappliedNames = taxon.getMisappliedNames();
		for (Taxon misappliedName : misappliedNames ){
			DwcaTaxRecordRedlist record = new DwcaTaxRecordRedlist(metaRecord, config);
			TaxonRelationshipType relType = TaxonRelationshipType.MISAPPLIED_NAME_FOR();
			NonViralName<?> name = CdmBase.deproxy(misappliedName.getName(), NonViralName.class);
		
			if (! this.recordExists(misappliedName)){
				handleTaxonBase(record, misappliedName, name, taxon, classification, relType, false, false, config);
				record.write(writer);
				this.addExistingRecord(misappliedName);
			}
		}	
	}

	/**
	 * @param record
	 * @param taxonBase
	 * @param name
	 * @param acceptedTaxon
	 * @param parent
	 * @param basionym
	 * @param isPartial 
	 * @param isProParte 
	 * @param config 
	 * @param type
	 */
	private void handleTaxonBase(
			DwcaTaxRecordRedlist record,
			TaxonBase<?> taxonBase,
			NonViralName<?> name, 
			Taxon acceptedTaxon, 
			Classification classification, 
			RelationshipTermBase<?> relType, 
			boolean isProParte, 
			boolean isPartial, 
			DwcaTaxExportConfiguratorRedlist config) {
		record.setId(taxonBase.getId());
		record.setUuid(taxonBase.getUuid());
		
		//maybe wrong as according to the DwC-A documentation only resolvable ids are allowed, this differs from DwC documentation
		record.setScientificNameId(name);
		record.setScientificName(name.getTitleCache());
		
		
		//synonyms
		handleSynonyms(record,(Taxon) taxonBase);
		
		handleTaxonomicStatus(record, name, relType, isProParte, isPartial);

		
		record.setDatasetId(classification);
		record.setDatasetName(classification.getTitleCache());

		return;
	}


	/**
	 * @param record
	 * @param name
	 * @param type
	 * @param isPartial 
	 * @param isProParte 
	 */
	private void handleTaxonomicStatus(
			DwcaTaxRecordRedlist record,
			NonViralName<?> name, 
			RelationshipTermBase<?> type,
			boolean isProParte,
			boolean isPartial) {
		if (type == null){
			record.setTaxonomicStatus(name.getNomenclaturalCode().acceptedTaxonStatusLabel());
		}else{
			String status = name.getNomenclaturalCode().synonymStatusLabel();
			if (type.equals(SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF())){
				status = "heterotypicSynonym";
			}else if(type.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())){
				status = "homotypicSynonym";
			}else if(type.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())){
				status = "misapplied";
			}
			if (isProParte){
				status = "proParteSynonym";
			}else if (isPartial){
				String message = "Partial synonym is not part of the gbif toxonomic status vocabulary";
				logger.warn(message);
				status = "partialSynonym";
			}
			
			record.setTaxonomicStatus(status);
		}
	}

	private void handleSynonyms(
			DwcaTaxRecordRedlist record,
			Taxon taxon
//			Classification classification 
//			DwcaMetaDataRecordRedlist metaRecord, 
//			DwcaTaxExportConfiguratorRedlist config
			) {
		
		Set<SynonymRelationship> synRels = taxon.getSynonymRelations();
		ArrayList<String> synonyms = new ArrayList<String>(); 
		for (SynonymRelationship synRel :synRels ){
//			DwcaTaxRecordRedlist record = new DwcaTaxRecordRedlist(metaRecord, config);
			Synonym synonym = synRel.getSynonym();
			SynonymRelationshipType type = synRel.getType();
//			boolean isProParte = synRel.isProParte();
//			boolean isPartial = synRel.isPartial();
			if (type == null){ // should not happen
				type = SynonymRelationshipType.SYNONYM_OF();
			}
			NonViralName<?> name = CdmBase.deproxy(synonym.getName(), NonViralName.class);
			synonyms.add(name.getTitleCache());
		}
		record.setSynonyms(synonyms);
	}


	@Override
	protected boolean doCheck(DwcaTaxExportStateRedlist state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}


	@Override
	protected boolean isIgnore(DwcaTaxExportStateRedlist state) {
		return ! state.getConfig().isDoTaxa();
	}


	
}
