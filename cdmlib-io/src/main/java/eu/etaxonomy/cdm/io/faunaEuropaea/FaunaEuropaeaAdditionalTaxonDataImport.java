/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.babadshanjan
 * @created 21.08.2010
 * @version 1.0
 */
@Component
public class FaunaEuropaeaAdditionalTaxonDataImport extends FaunaEuropaeaImportBase  {
	
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaAdditionalTaxonDataImport.class);
	private static final String parentPluralString = "Synonyms";
	private static final String pluralString = "InfraGenericEpithets";
	private static final String acceptedTaxonUUID = "A9C24E42-69F5-4681-9399-041E652CF338"; // any accepted taxon uuid, taken from original fauna europaea database

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(FaunaEuropaeaImportState state) {
		boolean result = true;
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		logger.warn("Checking for Taxa not yet fully implemented");
		result &= checkTaxonStatus(fauEuConfig);
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(FaunaEuropaeaImportState state) {
		return ! state.getConfig().isDoTaxa();
	}

	private boolean checkTaxonStatus(FaunaEuropaeaImportConfigurator fauEuConfig) {
		boolean result = true;
//		try {
			Source source = fauEuConfig.getSource();
			String sqlStr = "";
			ResultSet rs = source.getResultSet(sqlStr);
			return result;
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return false;
//		}
	}
	
	/** 
	 * Import taxa from FauEU DB
	 */
	protected boolean doInvoke(FaunaEuropaeaImportState state) {				
		
		boolean success = true;
		if(logger.isInfoEnabled()) {
			logger.info("Started creating " + pluralString + "...");
		}
		
		success = processAdditionalInfraGenericEpithets(state);
		
		logger.info("The End is Nigh... " + pluralString + "...");
		return success;
	}

	/**
	 * 
	 * @param state
	 * @return
	 */
	private boolean processAdditionalInfraGenericEpithets(FaunaEuropaeaImportState state) {
		boolean success = true;
		int count = 0;
		int pageSize = 1000;
		Set<UUID> uuidSet = new HashSet<UUID>();
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		ICdmDataSource destination = fauEuConfig.getDestination();

		String selectQuery = "SELECT t.uuid from TaxonNameBase t INNER JOIN " +
				"TaxonNameBase t2 ON t.GenusOrUninomial = t2.GenusOrUninomial AND t.SpecificEpithet = t2.SpecificEpithet " +
				"WHERE t.InfraGenericEpithet IS NULL AND t.rank_id = 764 AND t2.rank_id = 766 AND t2.InfraGenericEpithet IS NOT NULL";
		
		logger.error("Retrieving TaxonNames...");
		ResultSet resultSet = destination.executeQuery(selectQuery);

		TransactionStatus txStatus = null;
		List<TaxonNameBase> taxonNames = null;
		txStatus = startTransaction(false);
		
		// Collect UUIDs
		try {
			while (resultSet.next()) {
				uuidSet.add(UUID.fromString(resultSet.getString("UUID")));
			}
		} catch (SQLException e) {
			logger.error("An error occured: " + e.getMessage());
			e.printStackTrace();
		}

		// Fetch TaxonName objects for UUIDs
		taxonNames = getNameService().find(uuidSet);
		
		for (TaxonNameBase taxonName : taxonNames) {
			
			// Check whether its taxonName has an infraGenericEpithet
			if (taxonName != null && (taxonName.isInstanceOf(NonViralName.class))) {
				NonViralName targetNonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
				String infraGenericEpithet = targetNonViralName.getInfraGenericEpithet();
				if (infraGenericEpithet == null) {
					String genusOrUninomial = targetNonViralName.getGenusOrUninomial();
					String specificEpithet = targetNonViralName.getSpecificEpithet();
					List<TaxonBase> foundTaxa = getTaxonService().listTaxaByName(Taxon.class, genusOrUninomial, "*", specificEpithet, 
							"*", null, pageSize, 1);
					if (foundTaxa.size() == 1) {
						// one matching Taxon found
						TaxonBase taxon = foundTaxa.iterator().next();
						if (taxon != null) {
							TaxonNameBase name = taxon.getName();
							if (name != null && name.isInstanceOf(NonViralName.class)) {
								NonViralName nonViralName = CdmBase.deproxy(name, NonViralName.class);
								infraGenericEpithet = nonViralName.getInfraGenericEpithet();
								
								// set infraGenericEpithet
//									targetNonViralName.setInfraGenericEpithet(infraGenericEpithet);
								logger.error("Added an InfraGenericEpithet to this TaxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
								count++;
							}
						}
					} else if (foundTaxa.size() > 1) {
						logger.error("Multiple taxa match search criteria: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
						for (TaxonBase foundTaxon : foundTaxa) {
							logger.error(foundTaxon.getUuid() + ", " + foundTaxon.getTitleCache());
						}
					} else if (foundTaxa.size() == 0) {
//							logger.error("No matches for search criteria: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
					}
				}
				
			}
		}

		// Commit transaction
		commitTransaction(txStatus);
		logger.error("Committed transaction.");
		
		return success;
	}

}
