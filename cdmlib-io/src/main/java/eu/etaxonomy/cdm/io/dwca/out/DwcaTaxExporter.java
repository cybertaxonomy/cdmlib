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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.sdd.out.SDDDataSet;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 18.04.2011
 */
@Component("dwcaTaxExporter")
public class DwcaTaxExporter extends CdmExportBase<DwcaTaxExportConfigurator, DwcaTaxExportState> implements ICdmExport<DwcaTaxExportConfigurator, DwcaTaxExportState> {
	private static final Logger logger = Logger.getLogger(DwcaTaxExporter.class);

	/**
	 * 
	 */
	public DwcaTaxExporter() {
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
			
			File f = new File(fileName);
			if (!f.exists()){
				f.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(f);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF8"), true);

			
			
			DwcaTaxRecord record = new DwcaTaxRecord();
			List<TaxonNode> allNodes =  getClassificationService().getAllNodes();
			for (TaxonNode node : allNodes){
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				NonViralName<?> name = CdmBase.deproxy(taxon.getName(), NonViralName.class);
				Taxon parent = node.getParent() == null ? null : node.getParent().getTaxon();
				TaxonNameBase<?, ?> basionym = name.getBasionym();
				
				//ids als UUIDs?
				record.setScientificNameId(name.getId());
				record.setAcceptedNameUsageId(taxon.getId());
				record.setParentNameUsageId(parent == null ? null : parent.getId());
				// ??? - is not a name usage (concept)
//			record.setOriginalNameUsageId(basionym.getId());
				record.setNameAccordingToId(taxon.getSec().getId());
				record.setNamePublishedInId(name.getNomenclaturalReference() == null ? null : name.getNomenclaturalReference().getId());
				record.setTaxonConceptId(taxon.getId());
				
				record.setScientificName(name.getTitleCache());
				// ???
				record.setAcceptedNameUsage(taxon.getTitleCache());
				record.setParentNameUsage(parent == null ? null : parent.getTitleCache());
				// ??? is not a nameUsage (concept)
				record.setOriginalNameUsage(basionym == null ? null : basionym.getTitleCache());
				record.setNameAccordingTo(taxon.getSec().getTitleCache());
				record.setNamePublishedIn(name.getNomenclaturalReference() == null ? null : name.getNomenclaturalReference().getTitleCache());
				
				//???
				record.setHigherClassification(null);
				//... higher ranks
				
				//epethita
				Rank rank = name.getRank();
				String firstEpi = name.getGenusOrUninomial();
				if (!StringUtils.isBlank(firstEpi)){
					if (rank != null){
						if (rank.equals(Rank.GENUS())){
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
							record.setGenus(firstEpi);	
						} 
						
					}
				}
				//TODO other subgneric ranks ??
				record.setSubgenus(name.getInfraGenericEpithet());
				record.setSpecificEpithet(name.getSpecificEpithet());
				record.setInfraspecificEpithet(name.getInfraSpecificEpithet());
				
				record.setTaxonRank(name.getRank());
				record.setVerbatimTaxonRank(name.getRank().getTitleCache());
				record.setScientificNameAuthorship(name.getAuthorshipCache());
				
				// ???
				record.setVernacularName(null);
				
				record.setNomenclaturalCode(name.getNomenclaturalCode());
				// ???
				record.setTaxonomicStatus("Synonym");
				handleNomStatus(record, taxon, name);
				// ???
				record.setTaxonRemarks(null);
				// ??? which date is needed here (taxon, name, sec, ... ?)
				record.setModified(taxon.getUpdated());
				// ???
				record.setLanguage(null);
				
				//....
				record.write(writer);
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

	/**
	 * @param record
	 * @param taxon
	 * @param name
	 */
	private void handleNomStatus(DwcaTaxRecord record, Taxon taxon,
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
		logger.warn("No check implemented for Jaxb export");
		return result;
	}


	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return false;
	}
	
}
