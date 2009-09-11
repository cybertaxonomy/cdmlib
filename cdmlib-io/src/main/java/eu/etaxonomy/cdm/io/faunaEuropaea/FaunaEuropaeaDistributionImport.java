/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


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
			Source source = fauEuConfig.getSource();
			String sqlStr = "";
	//		ResultSet rs = source.getResultSet(sqlStr);
			return result;
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return false;
//		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(FaunaEuropaeaImportState state) {	
		
		int limit = state.getConfig().getLimitSave();
		
		/* Taxon store for retrieving taxa from and saving taxa to CDM */
		List<TaxonBase> taxonList = null;
		/* UUID store as input for retrieving taxa from CDM */
		Set<UUID> taxonUuids = null;
		/* Store to hold helper objects */
		Map<UUID, FaunaEuropaeaDistributionTaxon> fauEuTaxonMap = null;
		
//		Map<UUID, TaxonBase<?>> taxonMap = null;
//		Set<TaxonBase<?>> taxonSet = null;
		
		TransactionStatus txStatus = null;
		
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		Source source = fauEuConfig.getSource();
		
		String namespace = "Distribution";
		boolean success = true;
		
		
		//make not needed maps empty
//		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
//		MapWrapper<TeamOrPersonBase<?>> authorStore = (MapWrapper<TeamOrPersonBase<?>>)stores.get(ICdmIO.TEAM_STORE);
//		authorStore.makeEmpty();

		if(logger.isInfoEnabled()) { logger.info("Start making distributions..."); }
		
		try {
			String strQuery = 
				" SELECT distribution.*, Area.*, Taxon.UUID " + 
				" FROM distribution INNER JOIN " +
                " Area ON distribution.dis_ara_id = Area.ara_id INNER JOIN " +
                " Taxon ON distribution.dis_tax_id = Taxon.TAX_ID ";
            								
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			while (rs.next()) {
				
				if ((i++ % limit) == 0) {
					
					txStatus = startTransaction();
					taxonUuids = new HashSet<UUID>(limit);
					fauEuTaxonMap = new HashMap<UUID, FaunaEuropaeaDistributionTaxon>(limit);
//					taxonSet = new HashSet<TaxonBase<?>>(limit);
					
					if(logger.isInfoEnabled()) {
						logger.info("i = " + i + " - Distribution import transaction started"); 
					}
				}
				
				int taxonId = rs.getInt("dis_tax_id");
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
				fauEuDistribution.setTaxonId(taxonId);
				fauEuDistribution.setDistributionId(disId);
				fauEuDistribution.setOccurrenceStatusId(occStatusId);
				fauEuDistribution.setAreaId(areaId);
				fauEuDistribution.setAreaName(areaName);
				fauEuDistribution.setAreaCode(areaCode);
				fauEuDistribution.setExtraLimital(extraLimital);
				fauEuDistribution.setTaxonUuid(currentTaxonUuid);
				
				if (!taxonUuids.contains(currentTaxonUuid)) {
					taxonUuids.add(currentTaxonUuid);
					FaunaEuropaeaDistributionTaxon fauEuDistributionTaxon = 
						new FaunaEuropaeaDistributionTaxon(currentTaxonUuid);
					fauEuTaxonMap.put(currentTaxonUuid, fauEuDistributionTaxon);
				} else {
					if (logger.isTraceEnabled()) { 
						logger.trace("Taxon (" + currentTaxonUuid + ") already stored.");
						continue;
					}
				}
				
				fauEuTaxonMap.get(currentTaxonUuid).addDistribution(fauEuDistribution);
				
				if ((i % limit) == 0 && i != 1 ) {
					
					try {

						taxonList = getTaxonService().findByUuid(taxonUuids);

						for (TaxonBase taxonBase : taxonList) {

							if (taxonBase != null) {
								Taxon taxon;
								if (taxonBase instanceof Taxon) {
									taxon = taxonBase.deproxy(taxonBase, Taxon.class);
								} else {
									logger.warn("TaxonBase (" + taxonId + " is not of type Taxon but: " 
											+ taxonBase.getClass().getSimpleName());
									continue;
								}

								TaxonDescription taxonDescription;
								Set<TaxonDescription> descriptionSet = taxon.getDescriptions();
								if (descriptionSet.size() > 0) {
									taxonDescription = descriptionSet.iterator().next(); 
								} else {
									taxonDescription = TaxonDescription.NewInstance();
									taxon.addDescription(taxonDescription);
								}

								UUID taxonUuid = taxonBase.getUuid();
								FaunaEuropaeaDistributionTaxon fauEuHelperTaxon = fauEuTaxonMap.get(taxonUuid);
								
								for (FaunaEuropaeaDistribution fauEuHelperDistribution : fauEuHelperTaxon.getDistributions()) {

									PresenceAbsenceTermBase<?> presenceAbsenceStatus 
									= FaunaEuropaeaTransformer.occStatus2PresenceAbsence(fauEuHelperDistribution.getOccurrenceStatusId());
									NamedArea namedArea = 
										FaunaEuropaeaTransformer.areaId2TdwgArea(fauEuHelperDistribution);

									Distribution newDistribution = Distribution.NewInstance(namedArea, presenceAbsenceStatus);
									taxonDescription.addElement(newDistribution);
								}
							}
						}
						if(logger.isInfoEnabled()) { logger.info("Saving taxa..."); }

						getTaxonService().saveTaxonAll(taxonList);
						
						taxonUuids = null;
						taxonList = null;
						fauEuTaxonMap = null;
						commitTransaction(txStatus);
						
					} catch (Exception e) {
						logger.warn("An exception occurred when creating distribution with id " + disId);
						e.printStackTrace();
					}
				}
			}		
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
		if(logger.isInfoEnabled()) { logger.info("End making distributions..."); }
		
		return true;
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(FaunaEuropaeaImportState state){
		return !state.getConfig().isDoOccurrence();
	}
}
