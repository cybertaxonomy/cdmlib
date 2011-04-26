/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.sdd.out.SDDDataSet;
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
public class DwcaTaxExport extends DwcaExportBase {
	private static final Logger logger = Logger.getLogger(DwcaTaxExport.class);

	/**
	 * 
	 */
	public DwcaTaxExport() {
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
	protected boolean doInvoke(DwcaTaxExportState state){
		DwcaTaxExportConfigurator config = state.getConfig();
		String dbname = config.getSource() != null ? config.getSource().getName() : "unknown";
    	String fileName = config.getDestinationNameString();
		logger.info("Serializing DB " + dbname + " to file " + fileName);
		TransactionStatus txStatus = startTransaction(true);

		try {
			final String coreTaxFileName = "coreTax.txt";
			fileName = fileName + File.separatorChar + coreTaxFileName;
			File f = new File(fileName);
			if (!f.exists()){
				f.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(f);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF8"), true);

			
			
			List<TaxonNode> allNodes =  getAllNodes(null);
			for (TaxonNode node : allNodes){
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				DwcaTaxRecord record = new DwcaTaxRecord();
				
				NonViralName<?> name = CdmBase.deproxy(taxon.getName(), NonViralName.class);
				Taxon parent = node.getParent() == null ? null : node.getParent().getTaxon();
				TaxonNameBase<?, ?> basionym = name.getBasionym();
				Classification classification = node.getClassification();
				if (! this.recordExists(taxon)){
					handleTaxonBase(record, taxon, name, taxon, parent, basionym, classification, null);
					record.write(writer);
					this.addExistingRecord(taxon);
				}
				
				node.getClassification().getName();
				//synonyms
				handleSynonyms(taxon, writer, classification);
				
				//misapplied names
				handleMisapplication(taxon, writer, classification);
				
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
		commitTransaction(txStatus);
		return true;
	}
	


	private void handleSynonyms(Taxon taxon, PrintWriter writer, Classification classification) {
		//TODO avoid duplicates
		Set<SynonymRelationship> synRels = taxon.getSynonymRelations();
		for (SynonymRelationship synRel :synRels ){
			DwcaTaxRecord record = new DwcaTaxRecord();
			Synonym synonym = synRel.getSynonym();
			SynonymRelationshipType type = synRel.getType();
			if (type == null){ // should not happen
				type = SynonymRelationshipType.SYNONYM_OF();
			}
			NonViralName<?> name = CdmBase.deproxy(synonym.getName(), NonViralName.class);
			//????
			Taxon parent = null;
			TaxonNameBase<?, ?> basionym = name.getBasionym();
			
			if (! this.recordExists(synonym)){
				handleTaxonBase(record, synonym, name, taxon, parent, basionym, classification, type);
				record.write(writer);
				this.addExistingRecord(synonym);
			}
			
		}
		
	}
	

	private void handleMisapplication(Taxon taxon, PrintWriter writer, Classification classification) {
		//TODO avoid duplicates
		Set<Taxon> misappliedNames = taxon.getMisappliedNames();
		for (Taxon misappliedName : misappliedNames ){
			DwcaTaxRecord record = new DwcaTaxRecord();
			TaxonRelationshipType relType = TaxonRelationshipType.MISAPPLIED_NAME_FOR();
			NonViralName<?> name = CdmBase.deproxy(misappliedName.getName(), NonViralName.class);
			//????
			Taxon parent = null;
			TaxonNameBase<?, ?> basionym = name.getBasionym();
			
			if (! this.recordExists(misappliedName)){
				handleTaxonBase(record, misappliedName, name, taxon, parent, basionym, classification, relType);
				record.write(writer);
				this.addExistingRecord(misappliedName);
			}
		}	
	}

	/**
	 * @param record
	 * @param taxon
	 * @param name
	 * @param parent
	 * @param basionym
	 * @param type 
	 * @return
	 */
	/**
	 * @param record
	 * @param taxonBase
	 * @param name
	 * @param acceptedTaxon
	 * @param parent
	 * @param basionym
	 * @param type
	 */
	private void handleTaxonBase(DwcaTaxRecord record, TaxonBase taxonBase, NonViralName<?> name, 
			Taxon acceptedTaxon, Taxon parent, TaxonNameBase<?, ?> basionym, Classification classification, 
			RelationshipTermBase<?> relType) {
		//ids als UUIDs?
		record.setId(taxonBase.getId());
		record.setScientificNameId(name.getId());
		record.setAcceptedNameUsageId(acceptedTaxon.getId());
		record.setParentNameUsageId(parent == null ? null : parent.getId());
		// ??? - is not a name usage (concept)
//			record.setOriginalNameUsageId(basionym.getId());
		Reference sec = taxonBase.getSec();
		if (sec == null){
			String message = "There is a taxon without sec " + taxonBase.getTitleCache() + "( " + taxonBase.getId() + ")";
			logger.warn(message);
		}else{
			record.setNameAccordingToId(taxonBase.getSec().getId());
			record.setNameAccordingTo(taxonBase.getSec().getTitleCache());
		}
		record.setNamePublishedInId(name.getNomenclaturalReference() == null ? null : name.getNomenclaturalReference().getId());
		// what is the difference to id
		record.setTaxonConceptId(taxonBase.getId());
		
		record.setScientificName(name.getTitleCache());
		// ???
		record.setAcceptedNameUsage(acceptedTaxon.getTitleCache());
		record.setParentNameUsage(parent == null ? null : parent.getTitleCache());
		// ??? is not a nameUsage (concept)
		record.setOriginalNameUsage(basionym == null ? null : basionym.getTitleCache());
		record.setNamePublishedIn(name.getNomenclaturalReference() == null ? null : name.getNomenclaturalReference().getTitleCache());
		
		//???
		record.setHigherClassification(null);
		
		//... higher ranks
		handleUninomialOrGenus(record, name);
		
		//TODO other subgneric ranks ??
		record.setSubgenus(name.getInfraGenericEpithet());
		record.setSpecificEpithet(name.getSpecificEpithet());
		record.setInfraspecificEpithet(name.getInfraSpecificEpithet());
		
		record.setTaxonRank(name.getRank());
		if (name.getRank() != null){
			record.setVerbatimTaxonRank(name.getRank().getTitleCache());
		}else{
			String message = "No rank available for " + name.getTitleCache() + "(" + name.getId() + ")";
			logger.warn(message);
		}
		record.setScientificNameAuthorship(name.getAuthorshipCache());
		
		// ??? - use for TextData names?
		record.setVernacularName(null);
		
		record.setNomenclaturalCode(name.getNomenclaturalCode());
		// ??? TODO Misapplied Names, inferred synonyms
		handleTaxonomicStatus(record, name, relType);
		handleNomStatus(record, taxonBase, name);
		// ???
		record.setTaxonRemarks(null);
		// ??? which date is needed here (taxon, name, sec, ... ?)
		record.setModified(taxonBase.getUpdated());
		// ???
		record.setLanguage(null);
		
		record.setRights(taxonBase.getRights());
		
		//TODO
		record.setRightsHolder(null);
		record.setAccessRights(null);
		record.setBibliographicCitation(null);
		record.setInformationWithheld(null);
		
		record.setDatasetId(classification.getId());
		record.setDatasetName(classification.getTitleCache());
		
		record.setSource(null);
		
		return;
	}

	/**
	 * @param record
	 * @param name
	 * @param type
	 */
	private void handleTaxonomicStatus(DwcaTaxRecord record,
			NonViralName<?> name, RelationshipTermBase<?> type) {
		if (type == null){
			record.setTaxonomicStatus(name.getNomenclaturalCode().acceptedTaxonStatusLabel());
		}else{
			String status = name.getNomenclaturalCode().synonymStatusLabel();
			if (type.equals(SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF())){
				status = "heterotypic synonym";
			}else if(type.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())){
				status = "homotypic synonym";
			}else if(type.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())){
				status = "misapplied";
			}
			record.setTaxonomicStatus(status);
		}
	}

	/**
	 * @param record
	 * @param name
	 */
	private void handleUninomialOrGenus(DwcaTaxRecord record, NonViralName<?> name) {
		//epethita
		String firstEpi = name.getGenusOrUninomial();
		if (!StringUtils.isBlank(firstEpi)){
			Rank rank = name.getRank();
			if (rank != null){
				if (rank.isLower(Rank.GENUS())){
					record.setGenus(firstEpi);	
				}else if (rank.equals(Rank.GENUS())){
					record.setGenus(firstEpi);	
				}else if (rank.equals(Rank.KINGDOM())){
					record.setKingdom(firstEpi);	
				}else if (rank.equals(Rank.PHYLUM())){
					record.setPhylum(firstEpi);	
				}else if (rank.equals(Rank.CLASS())){
					record.setClazz(firstEpi);	
				}else if (rank.equals(Rank.ORDER())){
					record.setOrder(firstEpi);	
				}else if (rank.equals(Rank.FAMILY())){
					record.setFamily(firstEpi);	
				}else{
					// !!!
					String message = "Rank not covered. Set uninomial as genus instead: " + rank.getLabel();
					logger.warn(message);
//					record.setGenus(firstEpi);	
				} 
				
			}
		}
	}


	/**
	 * @param record
	 * @param taxon
	 * @param name
	 */
	private void handleNomStatus(DwcaTaxRecord record, TaxonBase taxon,
			NonViralName<?> name) {
		int nStatus = name.getStatus().size();
		if (nStatus > 0){
			if (name.getStatus().size()> 1){
				String warning = "There is more than 1 nomenclatural status ( " + name.getStatus().size()+ "): " + taxon.getTitleCache();
				logger.warn(warning);
			}
			NomenclaturalStatusType status = name.getStatus().iterator().next().getType();
			record.setNomenclaturalStatus(status);
		}else{
			record.setNomenclaturalStatus(null);
		}
	}


	private void retrieveData (IExportConfigurator config, SDDDataSet sddDataSet) {

		DwcaTaxExportConfigurator sddExpConfig = (DwcaTaxExportConfigurator)config;
		final int MAX_ROWS = 50000;

//		int agentRows = numberOfRows;
//		int definedTermBaseRows = numberOfRows;
//		int referenceBaseRows = numberOfRows;
//		int taxonNameBaseRows = numberOfRows;
//		int taxonBaseRows = numberOfRows;
//		int relationshipRows = numberOfRows;
//		int occurrencesRows = numberOfRows;
//		int mediaRows = numberOfRows;
//		int featureDataRows = numberOfRows;
//		int languageDataRows = numberOfRows;
//		int termVocabularyRows = numberOfRows;
//		int homotypicalGroupRows = numberOfRows;


	}


	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}


	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoTaxa();
	}
	
}
