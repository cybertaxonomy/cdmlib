/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pesi.faunaEuropaea;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.Source;
//import eu.etaxonomy.cdm.io.profiler.ProfilerController;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
//import com.yourkit.api.Controller;


/**
 * @author a.babadshanjan
 * @created 12.05.2009
 * @version 1.0
 */
@Component
public class FaunaEuropaeaDistributionImport extends FaunaEuropaeaImportBase {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaDistributionImport.class);

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(FaunaEuropaeaImportState state) {
		boolean result = true;
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		logger.warn("Checking for Distributions not yet fully implemented");
		result &= checkReferenceStatus(fauEuConfig);
		
		return result;
	}
	
	private boolean checkReferenceStatus(FaunaEuropaeaImportConfigurator fauEuConfig) {
		boolean result = true;
//		try {
//			Source source = fauEuConfig.getSource();
//			String sqlStr = "";
	//		ResultSet rs = source.getResultSet(sqlStr);
			return result;
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return false;
//		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected void doInvoke(FaunaEuropaeaImportState state) {	
		/*
		logger.warn("Start distribution doInvoke");
		ProfilerController.memorySnapshot();
		*/
		int limit = state.getConfig().getLimitSave();
		UUID noDataUuid;
		/* Taxon store for retrieving taxa from and saving taxa to CDM */
		List<TaxonBase> taxonList = null;
		/* UUID store as input for retrieving taxa from CDM */
		Set<UUID> taxonUuids = null;
		/* Store to hold helper objects */
		Map<UUID, FaunaEuropaeaDistributionTaxon> fauEuTaxonMap = null;
		
		
		TransactionStatus txStatus = null;
		
		//txStatus = startTransaction();
		noDataUuid = getTermService().save(PresenceTerm.NewInstance("no data", "no data", "nod"));
		//commitTransaction(txStatus);	
		
		FaunaEuropaeaTransformer.setUUIDs(noDataUuid);
		
		txStatus = null;
		
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		Source source = fauEuConfig.getSource();
		
        int i = 0;
		
		String selectCount = 
			" SELECT count(*) ";

		String selectColumns = 
			" SELECT distribution.*, Area.*, Taxon.UUID ";
			
		String fromClause = 
			" FROM distribution INNER JOIN " +
            " Area ON distribution.dis_ara_id = Area.ara_id INNER JOIN " +
            " Taxon ON distribution.dis_tax_id = Taxon.TAX_ID ";
		String orderBy = " ORDER BY distribution.dis_tax_id";
		
		String countQuery = 
			selectCount + fromClause;

		String selectQuery = 
			selectColumns + fromClause + orderBy;
		


		if(logger.isInfoEnabled()) { logger.info("Start making distributions..."); }
		
		try {
			ResultSet rs = source.getResultSet(countQuery);
			rs.next();
			int count = rs.getInt(1);

			rs = source.getResultSet(selectQuery);

			if (logger.isInfoEnabled()) {
				logger.info("Number of rows: " + count);
				logger.info("Count Query: " + countQuery);
				logger.info("Select Query: " + selectQuery);
			}
			
			//int taxonId;
			
			while (rs.next()) {
				if ((i++ % limit) == 0) {

					txStatus = startTransaction();
					taxonUuids = new HashSet<UUID>(limit);
					fauEuTaxonMap = new HashMap<UUID, FaunaEuropaeaDistributionTaxon>(limit);

					if(logger.isInfoEnabled()) {
						logger.info("i = " + i + " - Distribution import transaction started"); 
					}
				}

				//taxonId = rs.getInt("dis_tax_id");
				int disId = rs.getInt("dis_id");
				int occStatusId = rs.getInt("dis_present");
				int areaId = rs.getInt("ara_id");
				String areaName = rs.getString("ara_name");
				String areaCode = rs.getString("ara_code");
				int extraLimital = rs.getInt("ara_extralimital");
				UUID currentTaxonUuid = null;
				if (resultSetHasColumn(rs,"UUID")){
					currentTaxonUuid = UUID.fromString(rs.getString("UUID"));
				} else {
					currentTaxonUuid = UUID.randomUUID();
				}

				FaunaEuropaeaDistribution fauEuDistribution = new FaunaEuropaeaDistribution();
				fauEuDistribution.setDistributionId(disId);
				fauEuDistribution.setOccurrenceStatusId(occStatusId);
				fauEuDistribution.setAreaId(areaId);
				fauEuDistribution.setAreaName(areaName);
				fauEuDistribution.setAreaCode(areaCode);
				fauEuDistribution.setExtraLimital(extraLimital);

				if (!taxonUuids.contains(currentTaxonUuid)) {
					taxonUuids.add(currentTaxonUuid);
					FaunaEuropaeaDistributionTaxon fauEuDistributionTaxon = 
						new FaunaEuropaeaDistributionTaxon(currentTaxonUuid);
					fauEuTaxonMap.put(currentTaxonUuid, fauEuDistributionTaxon);
					fauEuDistributionTaxon = null;
				} else {
					if (logger.isTraceEnabled()) { 
						logger.trace("Taxon (" + currentTaxonUuid + ") already stored.");
						continue;
					}
				}

				fauEuTaxonMap.get(currentTaxonUuid).addDistribution(fauEuDistribution);

				if (((i % limit) == 0 && i != 1 ) || i == count ) { 

					try {
						commitTaxaAndDistribution(state, noDataUuid, taxonUuids, fauEuTaxonMap, txStatus);
						taxonUuids = null;
						taxonList = null;
						fauEuTaxonMap = null;
						
					} catch (Exception e) {
						logger.error("Commit of taxa and distributions failed" + e.getMessage());
						e.printStackTrace();
					}
					
					if(logger.isInfoEnabled()) { logger.info("i = " + i + " - Transaction committed");}
				}

					
			}	
			if (taxonUuids != null){
				try {
					commitTaxaAndDistribution(state, noDataUuid, taxonUuids, fauEuTaxonMap, txStatus);
					taxonUuids = null;
					taxonList = null;
					fauEuTaxonMap = null;
				} catch (Exception e) {
					logger.error("Commit of taxa and distributions failed");
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			state.setUnsuccessfull();
		}
		if(logger.isInfoEnabled()) { logger.info("End making distributions..."); }
		
		return;
	}

	private void commitTaxaAndDistribution(
			FaunaEuropaeaImportState state, UUID noDataUuid,
			Set<UUID> taxonUuids,
			Map<UUID, FaunaEuropaeaDistributionTaxon> fauEuTaxonMap,
			TransactionStatus txStatus) throws Exception {
		 List<TaxonBase> taxonList = prepareTaxaAndDistribution(getTaxonService().find(taxonUuids), fauEuTaxonMap, noDataUuid, state);

		getTaxonService().save(taxonList);
		commitTransaction(txStatus);
		
	}
	
	private List<TaxonBase> prepareTaxaAndDistribution(List<TaxonBase> taxonList, Map<UUID, FaunaEuropaeaDistributionTaxon> fauEuTaxonMap, UUID noData, FaunaEuropaeaImportState state) throws Exception{
	
		Distribution newDistribution = null;
		NamedArea namedArea;
		PresenceAbsenceTermBase<?> presenceAbsenceStatus;
		FaunaEuropaeaDistributionTaxon fauEuHelperTaxon;
		UUID taxonUuid;
		TaxonDescription taxonDescription;
		Taxon taxon;
		for (TaxonBase<?> taxonBase : taxonList) {

			if (taxonBase != null) {
				
				if (taxonBase instanceof Taxon) {
					taxon = CdmBase.deproxy(taxonBase, Taxon.class);
				} else {
					logger.warn("TaxonBase (" + taxonBase.getId() + " is not of type Taxon but: " 
							+ taxonBase.getClass().getSimpleName());
					continue;
				}
	
				
				Set<TaxonDescription> descriptionSet = taxon.getDescriptions();
				if (descriptionSet.size() > 0) {
					taxonDescription = descriptionSet.iterator().next(); 
				} else {
					taxonDescription = TaxonDescription.NewInstance();
					taxon.addDescription(taxonDescription);
				}
	
				taxonUuid = taxonBase.getUuid();
				fauEuHelperTaxon= fauEuTaxonMap.get(taxonUuid);
	
				for (FaunaEuropaeaDistribution fauEuHelperDistribution : fauEuHelperTaxon.getDistributions()) {
					namedArea = null;
					newDistribution = null;
					presenceAbsenceStatus = null;
					
					if (fauEuHelperDistribution.getOccurrenceStatusId() != 0 && fauEuHelperDistribution.getOccurrenceStatusId() != 2 && fauEuHelperDistribution.getOccurrenceStatusId() != 1){
						presenceAbsenceStatus = (PresenceAbsenceTermBase<?>)getTermService().find(noData);
					}else{
						presenceAbsenceStatus = FaunaEuropaeaTransformer.occStatus2PresenceAbsence(fauEuHelperDistribution.getOccurrenceStatusId());
					}

					namedArea = FaunaEuropaeaTransformer.areaId2TdwgArea(fauEuHelperDistribution);
					
					if (namedArea == null){
						UUID areaUuid= FaunaEuropaeaTransformer.getUUIDByAreaAbbr(fauEuHelperDistribution.getAreaCode());
						if (areaUuid == null){
							logger.warn("Area " + fauEuHelperDistribution.getAreaCode() + "not found in FE transformer");
						}
						namedArea = getNamedArea(state, areaUuid, fauEuHelperDistribution.getAreaName(), fauEuHelperDistribution.getAreaName(), fauEuHelperDistribution.getAreaCode(), null, null);
						
					}
					
					newDistribution = Distribution.NewInstance(namedArea, presenceAbsenceStatus);
					newDistribution.setCreated(null);
					
					taxonDescription.addElement(newDistribution);
				}
			}
		}
		return taxonList;
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(FaunaEuropaeaImportState state){
		return !state.getConfig().isDoOccurrence();
	}
}
