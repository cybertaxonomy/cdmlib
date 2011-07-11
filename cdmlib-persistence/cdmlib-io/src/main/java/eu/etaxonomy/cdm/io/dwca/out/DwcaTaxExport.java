/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.out;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
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
public class DwcaTaxExport extends DwcaExportBase {
	private static final Logger logger = Logger.getLogger(DwcaTaxExport.class);

	private static final String ROW_TYPE = "http://rs.tdwg.org/dwc/terms/Taxon";
	private static final String fileName = "coreTax.txt";
	
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
		TransactionStatus txStatus = startTransaction(true);
		
		DwcaMetaDataRecord metaRecord = new DwcaMetaDataRecord(true, fileName, ROW_TYPE);
		state.addMetaRecord(metaRecord);
		
		PrintWriter writer = null;
		try {
			
			writer = createPrintWriter(fileName, state);

			List<TaxonNode> allNodes =  getAllNodes(null);
			int i = 0;
			for (TaxonNode node : allNodes){
				i++;
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				DwcaTaxRecord record = new DwcaTaxRecord(metaRecord, config);
				
				NonViralName<?> name = CdmBase.deproxy(taxon.getName(), NonViralName.class);
				Taxon parent = node.getParent() == null ? null : node.getParent().getTaxon();
				TaxonNameBase<?, ?> basionym = name.getBasionym();
				Classification classification = node.getClassification();
				if (! this.recordExists(taxon)){
					handleTaxonBase(record, taxon, name, taxon, parent, basionym, classification, null, false, false, config);
					record.write(writer);
					this.addExistingRecord(taxon);
				}
				
				node.getClassification().getName();
				//synonyms
				handleSynonyms(taxon, writer, classification, metaRecord, config);
				
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
		return true;
		
	}

	private void handleSynonyms(Taxon taxon, PrintWriter writer, Classification classification, DwcaMetaDataRecord metaRecord, DwcaTaxExportConfigurator config) {
		Set<SynonymRelationship> synRels = taxon.getSynonymRelations();
		for (SynonymRelationship synRel :synRels ){
			DwcaTaxRecord record = new DwcaTaxRecord(metaRecord, config);
			Synonym synonym = synRel.getSynonym();
			SynonymRelationshipType type = synRel.getType();
			boolean isProParte = synRel.isProParte();
			boolean isPartial = synRel.isPartial();
			if (type == null){ // should not happen
				type = SynonymRelationshipType.SYNONYM_OF();
			}
			NonViralName<?> name = CdmBase.deproxy(synonym.getName(), NonViralName.class);
			//????
			Taxon parent = null;
			TaxonNameBase<?, ?> basionym = name.getBasionym();
			
			if (! this.recordExists(synonym)){
				handleTaxonBase(record, synonym, name, taxon, parent, basionym, classification, type, isProParte, isPartial, config);
				record.write(writer);
				this.addExistingRecord(synonym);
			}
			
		}
		
	}
	

	private void handleMisapplication(Taxon taxon, PrintWriter writer, Classification classification, DwcaMetaDataRecord metaRecord, DwcaTaxExportConfigurator config) {
		Set<Taxon> misappliedNames = taxon.getMisappliedNames();
		for (Taxon misappliedName : misappliedNames ){
			DwcaTaxRecord record = new DwcaTaxRecord(metaRecord, config);
			TaxonRelationshipType relType = TaxonRelationshipType.MISAPPLIED_NAME_FOR();
			NonViralName<?> name = CdmBase.deproxy(misappliedName.getName(), NonViralName.class);
			//????
			Taxon parent = null;
			TaxonNameBase<?, ?> basionym = name.getBasionym();
			
			if (! this.recordExists(misappliedName)){
				handleTaxonBase(record, misappliedName, name, taxon, parent, basionym, classification, relType, false, false, config);
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
	private void handleTaxonBase(DwcaTaxRecord record, TaxonBase<?> taxonBase, NonViralName<?> name, 
			Taxon acceptedTaxon, Taxon parent, TaxonNameBase<?, ?> basionym, Classification classification, 
			RelationshipTermBase<?> relType, boolean isProParte, boolean isPartial, DwcaTaxExportConfigurator config) {
		record.setId(taxonBase.getId());
		record.setUuid(taxonBase.getUuid());
		
		//maybe wrong as according to the DwC-A documentation only resolvable ids are allowed, this differs from DwC documentation
		record.setScientificNameId(name);
		record.setScientificName(name.getTitleCache());
		
		record.setAcceptedNameUsageId(acceptedTaxon.getUuid());
		record.setAcceptedNameUsage(acceptedTaxon.getName() == null? acceptedTaxon.getTitleCache() : acceptedTaxon.getName().getTitleCache());
		
		//parentNameUsage
		if (parent != null){
			record.setParentNameUsageId(parent.getUuid());
			record.setParentNameUsage(parent.getTitleCache());
		}
		
		//originalNameUsage
		// ??? - is not a name usage (concept)
		if (basionym != null){
			record.setOriginalNameUsageId(basionym.getUuid());
			record.setOriginalNameUsage(basionym.getTitleCache());
		}
		
		//nameAccordingTo
		Reference<?> sec = taxonBase.getSec();
		if (sec == null){
			String message = "There is a taxon without sec " + taxonBase.getTitleCache() + "( " + taxonBase.getId() + ")";
			logger.warn(message);
		}else{
			record.setNameAccordingToId(taxonBase.getSec().getUuid());
			record.setNameAccordingTo(taxonBase.getSec().getTitleCache());
		}
		
		//namePublishedIn
		// ??? is not a nameUsage (concept)
		if (name.getNomenclaturalReference() != null){
			record.setNamePublishedInId(name.getNomenclaturalReference().getUuid());
			record.setNamePublishedIn(name.getNomenclaturalReference() == null ? null : name.getNomenclaturalReference().getTitleCache());
		}
			
		// what is the exact difference to id and acceptedNameUsageId
		record.setTaxonConceptId(taxonBase.getUuid());
		
		//Classification
		if (config.isWithHigherClassification()){
			//FIXME all classification and rank specific fields are meant to represent the classification
			//currently the information is only compiled for the exact same range but it should be compiled
			//for all ranks above the rank of this taxon
			//TODO we do not support this yet
			record.setHigherClassification(null);
			//... higher ranks
			handleUninomialOrGenus(record, name);
			if (name.getRank() != null &&  name.getRank().equals(Rank.SUBGENUS())){
				record.setSubgenus(name.getNameCache());	
			}
			//record.setSubgenus(name.getInfraGenericEpithet());
		}
		if (name.getRank() != null &&  (name.getRank().isSupraGeneric() || name.getRank().isGenus())){
			record.setUninomial(name.getGenusOrUninomial());
		}else{
			record.setGenusPart(name.getGenusOrUninomial());
		}
		record.setInfraGenericEpithet(name.getInfraGenericEpithet());
		
		record.setSpecificEpithet(name.getSpecificEpithet());
		record.setInfraspecificEpithet(name.getInfraSpecificEpithet());
		
		record.setTaxonRank(name.getRank());
		if (name.getRank() != null){
			record.setVerbatimTaxonRank(name.getRank().getAbbreviation());
		}else{
			String message = "No rank available for " + name.getTitleCache() + "(" + name.getId() + ")";
			logger.warn(message);
		}
		
		record.setScientificNameAuthorship(name.getAuthorshipCache());
		
		// ??? - use for TextData common names?
		record.setVernacularName(null);
		
		record.setNomenclaturalCode(name.getNomenclaturalCode());
		// ??? TODO Misapplied Names, inferred synonyms
		handleTaxonomicStatus(record, name, relType, isProParte, isPartial);
		handleNomStatus(record, taxonBase, name);
		
		// TODO we need to differentiate technical
		String taxonRemarks = "";
		for (Annotation annotation : taxonBase.getAnnotations()){
			if (AnnotationType.EDITORIAL().equals(annotation.getAnnotationType())){
				taxonRemarks += CdmUtils.Nz(annotation.getText());
			}
		}
		for (Annotation annotation : name.getAnnotations()){
			if (AnnotationType.EDITORIAL().equals(annotation.getAnnotationType())){
				taxonRemarks += CdmUtils.Nz(annotation.getText());
			}
		}
		if (StringUtils.isNotBlank(taxonRemarks)){
			record.setTaxonRemarks(taxonRemarks);
		}
		
		// TODO which date is needed here (taxon, name, sec, ... ?)
		record.setModified(taxonBase.getUpdated());
		
		// ???
		record.setLanguage(null);
		
		record.setRights(taxonBase.getRights());
		
		//TODO
		record.setRightsHolder(null);
		record.setAccessRights(null);
		
		//TODO currently only via default value
		record.setBibliographicCitation(null);
		record.setInformationWithheld(null);
		
		record.setDatasetId(classification);
		record.setDatasetName(classification.getTitleCache());
		
		//TODO
		record.setSource(null);
		
		return;
	}

	/**
	 * @param record
	 * @param name
	 * @param type
	 * @param isPartial 
	 * @param isProParte 
	 */
	private void handleTaxonomicStatus(DwcaTaxRecord record,
			NonViralName<?> name, RelationshipTermBase<?> type, boolean isProParte, boolean isPartial) {
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
	private void handleNomStatus(DwcaTaxRecord record, TaxonBase<?> taxon,
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
