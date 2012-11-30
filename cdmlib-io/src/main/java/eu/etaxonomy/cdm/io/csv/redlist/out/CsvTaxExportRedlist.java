/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.csv.redlist.out;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;


/**
 * @author a.oppermann
 * @created 18.10.2012
 */
@Component
public class CsvTaxExportRedlist extends CsvExportBaseRedlist {
	private static final Logger logger = Logger.getLogger(CsvTaxExportRedlist.class);

	private static final String ROW_TYPE = "http://rs.tdwg.org/dwc/terms/Taxon";
	private static final String fileName = "RedlistCoreTax.csv";
	private List<Feature> features;

	public CsvTaxExportRedlist() {
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
	protected void doInvoke(CsvTaxExportStateRedlist state){
		CsvTaxExportConfiguratorRedlist config = state.getConfig();
		TransactionStatus txStatus = startTransaction(true);
		features = config.getFeatures();
		CsvMetaDataRecordRedlist metaRecord = new CsvMetaDataRecordRedlist(true, fileName, ROW_TYPE);
		state.addMetaRecord(metaRecord);
		
		Set<UUID> classificationUuidSet = config.getClassificationUuids();
		List<Classification> classificationList = getClassificationService().find(classificationUuidSet);
		Set<Classification> classificationSet = new HashSet<Classification>();
		classificationSet.addAll(classificationList);
		
		PrintWriter writer = null;
		ByteArrayOutputStream byteArrayOutputStream;
		try {
			byteArrayOutputStream = config.getByteArrayOutputStream();
			writer = new PrintWriter(byteArrayOutputStream); 

			List<TaxonNode> allNodes =  getAllNodes(classificationSet);
			for (TaxonNode node : allNodes){
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				CsvTaxRecordRedlist record = new CsvTaxRecordRedlist(metaRecord, config);
				NonViralName<?> name = CdmBase.deproxy(taxon.getName(), NonViralName.class);
				Classification classification = node.getClassification();
				config.setClassificationTitleCache(classification.getTitleCache());
				if (! this.recordExists(taxon)){
						handleTaxonBase(record, taxon, name, taxon, classification, null, false, false, config);
						record.write(writer);
						this.addExistingRecord(taxon);
					}
				//misapplied names
				handleMisapplication(taxon, writer, classification, metaRecord, config);
				writer.flush();
			}

		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		finally{
			writer.close();
			this.clearExistingRecordIds();
		}
		commitTransaction(txStatus);
		return;
		
	}

	private void handleMisapplication(Taxon taxon, PrintWriter writer, Classification classification, CsvMetaDataRecordRedlist metaRecord, CsvTaxExportConfiguratorRedlist config) {
		Set<Taxon> misappliedNames = taxon.getMisappliedNames();
		for (Taxon misappliedName : misappliedNames ){
			CsvTaxRecordRedlist record = new CsvTaxRecordRedlist(metaRecord, config);
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
	private void handleTaxonBase(CsvTaxRecordRedlist record,TaxonBase<?> taxonBase,
			NonViralName<?> name, Taxon acceptedTaxon, Classification classification, 
			RelationshipTermBase<?> relType, boolean isProParte, boolean isPartial, 
			CsvTaxExportConfiguratorRedlist config) {
		
		record.setHeadLinePrinted(config.isHasHeaderLines());
		if(features != null)record.setPrintFeatures(features);
		config.setHasHeaderLines(false);

		record.setDatasetName(classification.getTitleCache());
		record.setScientificName(name.getTitleCache());
		record.setScientificNameId(name.getUuid().toString());
		handleTaxonomicStatus(record, name, relType, isProParte, isPartial);
		//synonyms
		handleSynonyms(record,(Taxon) taxonBase);
		//distribution
		handleDiscriptionData(record, (Taxon) taxonBase);
		if(features!= null) {
			
			List<List<String>> featureCells = new ArrayList<List<String>>(features.size());
			for(int i = 0; i < features.size(); i++) {
				featureCells.add(new ArrayList<String>());
			}
			handleRelatedRedlistStatus(record, (Taxon)taxonBase, false, featureCells);
			handleRelatedRedlistStatus(record, (Taxon)taxonBase, true, featureCells);

		}
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
			CsvTaxRecordRedlist record,
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

	private void handleSynonyms(CsvTaxRecordRedlist record, Taxon taxon) {
		
		Set<SynonymRelationship> synRels = taxon.getSynonymRelations();
		ArrayList<String> synonyms = new ArrayList<String>(); 
		for (SynonymRelationship synRel :synRels ){
			Synonym synonym = synRel.getSynonym();
			SynonymRelationshipType type = synRel.getType();
			if (type == null){ // should not happen
				type = SynonymRelationshipType.SYNONYM_OF();
			}
			NonViralName<?> name = CdmBase.deproxy(synonym.getName(), NonViralName.class);
			synonyms.add(name.getTitleCache());
		}
		record.setSynonyms(synonyms);
	}

	private void handleDiscriptionData(CsvTaxRecordRedlist record, Taxon taxon) {
		
		Set<TaxonDescription> descriptions = taxon.getDescriptions();
		ArrayList<String> distributions = new ArrayList<String>();
		for (TaxonDescription description : descriptions){
			for (DescriptionElementBase el : description.getElements()){
				if (el.isInstanceOf(Distribution.class) ){
						Distribution distribution = CdmBase.deproxy(el, Distribution.class);
						NamedArea area = distribution.getArea();
						distributions.add(area.getTitleCache());
				}

			}
		}
		record.setCountryCode(distributions);
	}
	
	private void handleRedlistStatus(CsvTaxRecordRedlist record, Taxon taxon, List<List<String>> featureCells){
		Set<TaxonDescription> descriptions = taxon.getDescriptions();

		for (TaxonDescription description : descriptions){
			for (DescriptionElementBase el : description.getElements()){
				if(el.isInstanceOf(CategoricalData.class)){
					CategoricalData categoricalData = CdmBase.deproxy(el, CategoricalData.class);
					for(State state:categoricalData.getStatesOnly()){
						Feature stateFeature = categoricalData.getFeature();
						// find matching feature and put data into according cell
						for(int i = 0; i < features.size(); i++) {
							if(features.get(i).equals(stateFeature)){
								List<String> cell = featureCells.get(i);
								cell.add(state.toString());
							}
						}
					}
				}
			}
		}
		record.setFeatures(featureCells);
	}

	private void handleRelatedRedlistStatus(CsvTaxRecordRedlist record, Taxon taxon, boolean relationFrom, List<List<String>> featureCells) {

		if (relationFrom)handleRedlistStatus(record, taxon, featureCells);
		
		
		Set<TaxonRelationship> taxRels;
		if(relationFrom){
			taxRels = taxon.getRelationsFromThisTaxon();
		}else{
			taxRels = taxon.getRelationsToThisTaxon();
		}
		for (TaxonRelationship taxRel:taxRels){
			if(taxRel.getType().equals(TaxonRelationshipType.CONGRUENT_TO())){
				Taxon relatedTaxon;
				if(relationFrom){
					relatedTaxon = taxRel.getToTaxon();
				}else{
					relatedTaxon = taxRel.getFromTaxon();
				}
				handleRedlistStatus(record, relatedTaxon, featureCells);
			}
		}
	}

	@Override
	protected boolean doCheck(CsvTaxExportStateRedlist state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}

	@Override
	protected boolean isIgnore(CsvTaxExportStateRedlist state) {
		return ! state.getConfig().isDoTaxa();
	}
	
}
