/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.csv.redlist.out;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 20.04.2011
 */
@Component
public class CsvDistributionExportRedlist extends CsvExportBaseRedlist {
	private static final Logger logger = Logger.getLogger(CsvDistributionExportRedlist.class);

	private static final String ROW_TYPE = "http://rs.gbif.org/terms/1.0/Distribution";
	private static final String fileName = "distribution.txt";


	/**
	 * Constructor
	 */
	public CsvDistributionExportRedlist() {
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
		
		PrintWriter writer = null;
		try {
			
		
			writer = createPrintWriter(fileName, state);
			
			CsvMetaDataRecordRedlist metaRecord = new CsvMetaDataRecordRedlist(! IS_CORE, fileName, ROW_TYPE);
			state.addMetaRecord(metaRecord);

			List<TaxonNode> allNodes =  getAllNodes(null);
			for (TaxonNode node : allNodes){
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				
				Set<TaxonDescription> descriptions = taxon.getDescriptions();
				for (TaxonDescription description : descriptions){
					for (DescriptionElementBase el : description.getElements()){
						if (el.isInstanceOf(Distribution.class) ){
							if (! recordExists(el)){
								CsvDistributionRecordRedlist record = new CsvDistributionRecordRedlist(metaRecord, config);
								Distribution distribution = CdmBase.deproxy(el, Distribution.class);
								handleDistribution(record, distribution, taxon, config);
								record.write(writer);
								this.addExistingRecord(distribution);
							}
						}else if (el.getFeature().equals(Feature.DISTRIBUTION())){
							//TODO
							String message = "Distribution export for TextData not yet implemented";
							logger.warn(message);
						}
					}
				}
				
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
		}finally {
			closeWriter(writer, state);
			this.clearExistingRecordIds();
		}
		commitTransaction(txStatus);
		return;
	}
	



	private void handleDistribution(CsvDistributionRecordRedlist record, Distribution distribution, Taxon taxon, CsvTaxExportConfiguratorRedlist config) {
		record.setId(taxon.getId());
		record.setUuid(taxon.getUuid());
		handleArea(record, distribution.getArea(), taxon, true);
		//TODO missing
		record.setLifeStage(null);
		record.setOccurrenceStatus(distribution.getStatus());
		//TODO missing
		record.setThreadStatus(null);
		record.setEstablishmentMeans(distribution.getStatus());
		//TODO missing
		record.setAppendixCITES(null);
		//TODO missing
		record.setEventDate(null);
		//TODO missing
		record.setSeasonalDate(null);
		//FIXME
		record.setSource(getSources(distribution, config));
		//FIXME
		record.setOccurrenceRemarks(null);
		
	}
	
	@Override
	protected boolean doCheck(CsvTaxExportStateRedlist state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}


	@Override
	protected boolean isIgnore(CsvTaxExportStateRedlist state) {
		return ! state.getConfig().isDoDistributions();
	}
	
}
