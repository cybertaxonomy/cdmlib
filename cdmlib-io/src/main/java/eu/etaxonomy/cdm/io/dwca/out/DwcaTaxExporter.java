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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.sdd.out.SDDDataSet;
import eu.etaxonomy.cdm.io.sdd.out.SDDDocumentBuilder;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author h.fradin (from a.babadshanjan)
 * @created 10.12.2008
 * @versoin 1.0
 */
@Component("sddCdmExporter")
public class DwcaTaxExporter extends CdmExportBase<DwcaTaxExportConfigurator, DwcaTaxExportState> implements ICdmExport<DwcaTaxExportConfigurator, DwcaTaxExportState> {
// public class JaxbExport extends CdmIoBase implements ICdmIoExport {
// TODO: public class JaxbExport extends CdmIoBase implements ICdmIO {

	private static final Logger logger = Logger.getLogger(DwcaTaxExporter.class);
	private SDDDocumentBuilder sddDocumentBuilder = null;

	private String ioName = null;

	
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
//		protected boolean doInvoke(IExportConfigurator config,
//		Map<String, MapWrapper<? extends CdmBase>> stores) {
	
		DwcaTaxExportConfigurator sddExpConfig = state.getConfig();
		
		String dbname = sddExpConfig.getSource() != null ? sddExpConfig.getSource().getName() : "unknown";
    	String fileName = sddExpConfig.getDestinationNameString();
		logger.info("Serializing DB " + dbname + " to file " + fileName);
		logger.debug("DbSchemaValidation = " + sddExpConfig.getDbSchemaValidation());

		TransactionStatus txStatus = startTransaction(true);
		SDDDataSet dataSet = new SDDDataSet();
		List<Taxon> taxa = null;
		List<DefinedTermBase> terms = null;

		// get data from DB

		try {
			logger.info("Retrieving data from DB");

			retrieveData(sddExpConfig, dataSet);

		} catch (Exception e) {
			logger.error("Error retrieving data");
			e.printStackTrace();
		}

		logger.info("All data retrieved");

		try {
			sddDocumentBuilder = new SDDDocumentBuilder();
			File f = new File(fileName);
			// File f = new File(fileName);
			FileOutputStream fos = new FileOutputStream(f);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF8"), true);
			sddDocumentBuilder.marshal(dataSet, f);

			// TODO: Split into one file per data set member to see whether performance improves?

			logger.info("XML file written");
			logger.info("Filename is: " + fileName);

		} catch (Exception e) {
			logger.error("Marshalling error");
			e.printStackTrace();
		} 
		commitTransaction(txStatus);
		
		return true;

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
