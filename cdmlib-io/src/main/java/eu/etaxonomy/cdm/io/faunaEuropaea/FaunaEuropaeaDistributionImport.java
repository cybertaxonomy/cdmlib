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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

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
public class FaunaEuropaeaDistributionImport  extends FaunaEuropaeaImportBase {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaDistributionImport.class);

	private int modCount = 10000;
	/* Max number of references to be saved with one service call */
	private int limit = 20000; // TODO: Make configurable
	
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
			ResultSet rs = source.getResultSet(sqlStr);
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
		
		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		
		//make not needed maps empty
//		MapWrapper<TeamOrPersonBase<?>> authorStore = (MapWrapper<TeamOrPersonBase<?>>)stores.get(ICdmIO.TEAM_STORE);
//		authorMap.makeEmpty();

		Set<TaxonBase> taxonMap = new HashSet<TaxonBase>();
		
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		Source source = fauEuConfig.getSource();
		
		String namespace = "Distribution";
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making Distributions..."); }
		
		try {
			String strQuery = 
				" SELECT distribution.*, Area.* " + 
                " FROM distribution INNER JOIN Area ON distribution.dis_ara_id = Area.ara_id " +
                " WHERE (1=1)";
            								
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			while (rs.next()) {
				
				if ((i++ % modCount) == 0 && i!= 1 ) { 
					if(logger.isInfoEnabled()) {
						logger.info("Distributions handled: " + (i-1)); 
					}
				}
				
				int disId = rs.getInt("dis_id");
				int taxonId = rs.getInt("dis_tax_id");
				int occStatusId = rs.getInt("ara_id");
				
				TaxonBase<?> taxonBase = taxonStore.get(taxonId);
				
				try {
				if (taxonBase != null) {
					Taxon taxon;
					if (taxonBase instanceof Taxon) {
						taxon = (Taxon)taxonBase;
					} else {
						logger.warn("TaxonBase (" + taxonId + " is not of type Taxon but: " 
								+ taxonBase.getClass().getSimpleName());
						continue;
					}
					
					TaxonDescription taxonDescription;
					Set<TaxonDescription> descriptionSet= taxon.getDescriptions();
					if (descriptionSet.size() > 0) {
						taxonDescription = descriptionSet.iterator().next(); 
					} else {
						taxonDescription = TaxonDescription.NewInstance();
						taxon.addDescription(taxonDescription);
					}
										
    				PresenceAbsenceTermBase<?> presenceAbsenceStatus 
    				= FaunaEuropaeaTransformer.occStatus2PresenceAbsence(occStatusId);
					NamedArea namedArea = FaunaEuropaeaTransformer.areaId2TdwgArea(rs);
    				
					Distribution newDistribution = Distribution.NewInstance(namedArea, presenceAbsenceStatus);
					taxonDescription.addElement(newDistribution);
					
					//ImportHelper.setOriginalSource(reference, fauEuConfig.getSourceReference(), disId, namespace);
					
//					if (!refStore.containsId(refId)) {
//						if (reference == null) {
//							logger.warn("Reference is null");
//						}
//						refStore.put(refId, reference);
//					} else {
//						logger.warn("Reference with duplicated ref_id (" + refId + 
//								") not imported.");
//					}
					taxonMap.add(taxon);
				}
					
				} catch (Exception e) {
					logger.warn("An exception occurred when creating distribution with id " + disId + 
					". Reference could not be saved.");
					e.printStackTrace();
				}
			}
			
			if(logger.isInfoEnabled()) { logger.info("Saving distributions ..."); }
			
			success = saveTaxa(stores, state.getHighestTaxonIndex(), limit);
			// save taxa
//			getTaxonService().saveTaxonAll(taxonMap);
			
			if(logger.isInfoEnabled()) { logger.info("End making distributions..."); }
			
			return true;
			
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(FaunaEuropaeaImportState state){
		return !state.getConfig().isDoOccurrence();
	}
}
