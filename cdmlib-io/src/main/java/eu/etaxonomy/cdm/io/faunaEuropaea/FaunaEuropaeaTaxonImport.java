/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.T_STATUS_ACCEPTED;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.T_STATUS_NOT_ACCEPTED;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.CdmOneToManyMapper;
import eu.etaxonomy.cdm.io.berlinModel.CdmStringMapper;
import eu.etaxonomy.cdm.io.common.CdmAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.PublicationBase;
import eu.etaxonomy.cdm.model.reference.Publisher;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.babadshanjan
 * @created 12.05.2009
 * @version 1.0
 */
@Component
public class FaunaEuropaeaTaxonImport extends FaunaEuropaeaImportBase  {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaTaxonImport.class);

	/* Interval for progress info message when retrieving taxa */
	private int modCount = 10000;
	/* Max number of taxa to be saved with one service call */
	private int limit = 10000; // TODO: Make configurable
	/* The highest taxon index in the FauEu database */
	private int highestTaxonIndex = 0;
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config) {
		boolean result = true;
		FaunaEuropaeaImportConfigurator fauEuConfig = (FaunaEuropaeaImportConfigurator)config;
		logger.warn("Checking for Taxa not yet fully implemented");
		result &= checkTaxonStatus(fauEuConfig);
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config) {
		return !config.isDoTaxa();
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
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(IImportConfigurator config, 
			Map<String, MapWrapper<? extends CdmBase>> stores) {				
		
		MapWrapper<TaxonNameBase<?,?>> taxonNamesStore = (MapWrapper<TaxonNameBase<?,?>>)stores.get(ICdmIO.TAXONNAME_STORE);
		Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = new HashMap();
		FaunaEuropaeaImportConfigurator fauEuConfig = (FaunaEuropaeaImportConfigurator)config;
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making taxa..."); }
		
		success = retrieveTaxa(fauEuConfig, stores, fauEuTaxonMap);
		success = processTaxaSecondPass(fauEuConfig, stores, fauEuTaxonMap);
		success = saveTaxa(stores);
		
		logger.info("End making taxa...");
		return success;
	}


	private boolean retrieveTaxa(FaunaEuropaeaImportConfigurator fauEuConfig, 
			Map<String, MapWrapper<? extends CdmBase>> stores,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {

//		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
//		MapWrapper<ReferenceBase> refStore = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.NOMREF_STORE);
//		MapWrapper<TaxonNameBase<?,?>> taxonNamesStore = (MapWrapper<TaxonNameBase<?,?>>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);

		Source source = fauEuConfig.getSource();
		String namespace = "Taxon";
		int i = 0;
		boolean success = true;

		try {
			
			String strQuery = 
				" SELECT MAX(TAX_ID) AS TAX_ID FROM dbo.Taxon ";
			
			ResultSet rs = source.getResultSet(strQuery);
			while (rs.next()) {
				int maxTaxonId = rs.getInt("TAX_ID");
				highestTaxonIndex = maxTaxonId;
			}
			
//			strQuery = 
//				" SELECT Taxon.*, rank.*, TaxRefs.* " + 
//				" FROM dbo.Taxon " +
//				" INNER JOIN dbo.rank ON dbo.Taxon.TAX_RNK_ID = dbo.rank.rnk_id " +
//				" INNER JOIN dbo.TaxRefs ON dbo.Taxon.TAX_ID = dbo.TaxRefs.trf_tax_id " +
//				" WHERE (1=1)";

			strQuery = 
				" SELECT Taxon.*, rank.* " + 
				" FROM dbo.Taxon INNER JOIN dbo.rank ON dbo.Taxon.TAX_RNK_ID = dbo.rank.rnk_id " +
				" WHERE (1=1)";

			rs = source.getResultSet(strQuery);
			

//			int i = 0;
			while (rs.next()) {

				if ((i++ % modCount) == 0 && i != 1 ) { 
					if(logger.isInfoEnabled()) {
						logger.info("Taxa retrieved: " + (i-1)); 
					}
				}

				int taxonId = rs.getInt("TAX_ID");
				int parentId = rs.getInt("TAX_TAX_IDPARENT");
				String taxonName = rs.getString("TAX_NAME");
				int statusFk = rs.getInt("TAX_VALID");
				int rankId = rs.getInt("rnk_id");
				Rank rank = null;
				UUID taxonBaseUuid = UUID.randomUUID();

				try {
					rank = FaunaEuropaeaTransformer.rankId2Rank(rs, false);
				} catch (UnknownCdmTypeException e) {
					logger.warn("Taxon (" + taxonId + ") has unknown rank (" + rankId + ") and could not be saved.");
					success = false; 
				}

				ReferenceBase<?> reference = null;

				ZoologicalName zooName = ZoologicalName.NewInstance(rank);
//				zooName.setId(taxonId);
				zooName.setNameCache(taxonName);
//				zooName.setTitleCache(taxonName); // FIXME: Add the author
//				zooName.setFullTitleCache(taxonName); // FIXME: Add author, reference, NC status

				TaxonBase<?> taxonBase;
				FaunaEuropaeaTaxon fauEuTaxon = new FaunaEuropaeaTaxon();

				Synonym synonym;
				Taxon taxon;
				try {
//					logger.debug(statusFk);
					if (statusFk == T_STATUS_ACCEPTED) {
						taxon = Taxon.NewInstance(zooName, reference);
						taxonBase = taxon;
					} else if (statusFk == T_STATUS_NOT_ACCEPTED) {
						synonym = Synonym.NewInstance(zooName, reference);
						taxonBase = synonym;
					} else {
						logger.warn("Taxon status " + statusFk + " not yet implemented. Taxon (" + taxonId + ") ignored.");
						continue;
					}

					taxonBase.setUuid(taxonBaseUuid);
//					taxonBase.setId(taxonId);
					taxonBase.setTitleCache(taxonName);

					fauEuTaxon.setUuid(taxonBaseUuid);
					fauEuTaxon.setParentId(parentId);
					fauEuTaxon.setId(taxonId);

					ImportHelper.setOriginalSource(taxonBase, fauEuConfig.getSourceReference(), taxonId, namespace);
					
					if (!taxonStore.containsId(taxonId)) {
						if (taxonBase == null) {
							logger.warn("Taxon base is null");
						}
						taxonStore.put(taxonId, taxonBase);
						fauEuTaxonMap.put(taxonId, fauEuTaxon);
						if (logger.isDebugEnabled()) { 
							logger.debug("Stored taxon base (" + taxonId + ") " + taxonName); 
						}
					} else {
						logger.warn("Not imported taxon base with duplicated TAX_ID (" + taxonId + 
								") " + taxonName);
					}

//					if(!taxonNamesStore.containsId(taxonId) && !taxonStore.containsId(taxonId) && !taxonStore.containsId(taxonId)) {
//						taxonNamesStore.put(taxonId, zooName);
//						taxonStore.put(taxonId, taxonBase);
//						fauEuTaxonMap.put(taxonId, fauEuTaxon);
//					} else {
//						logger.warn("Ignoring taxon with duplicated id " + taxonId);
//					}

				} catch (Exception e) {
					logger.warn("An exception occurred when creating taxon base with id " + taxonId + 
					". Taxon base could not be saved.");
				}
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			success = false;
		}

		return success;
	}
	
	
	private boolean processTaxaSecondPass(FaunaEuropaeaImportConfigurator fauEuConfig,
			Map<String, MapWrapper<? extends CdmBase>> stores, 
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {

		if(logger.isInfoEnabled()) { logger.info("Processing taxa second pass ..."); }

		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);

		boolean success = true;

		for (int id : taxonStore.keySet())
		{
			TaxonBase<?> taxonBase = taxonStore.get(id);
			FaunaEuropaeaTaxon fauEuTaxon = fauEuTaxonMap.get(id);
			
			success = updateProperties(fauEuConfig, taxonBase, taxonStore, fauEuTaxon, fauEuTaxonMap);
			
		}
		return success;	
	}

	
	private boolean updateProperties(FaunaEuropaeaImportConfigurator fauEuConfig,
			TaxonBase<?> taxonBase, MapWrapper<TaxonBase> taxonStore,
			FaunaEuropaeaTaxon fauEuTaxon, Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {
		
		boolean success = true;
		
		TaxonNameBase<?,?> taxonName = taxonBase.getName();
		ZoologicalName zooName = (ZoologicalName)taxonName;
		String zooNameCache = null;
		if (zooName != null) {
			zooNameCache = zooName.getNameCache();
		}
		
		if (taxonName.getRank() == Rank.SPECIES()) {
			if(logger.isDebugEnabled()) { logger.debug("Species " + zooNameCache); }

			// Set taxonomic parent
			// Concat taxon name

			int parentId = fauEuTaxon.getParentId();
			FaunaEuropaeaTaxon parent = fauEuTaxonMap.get(parentId);
			if (parent != null) {
				UUID parentUuid = parent.getUuid();
				if (parentUuid != null) { 
					TaxonBase<?> parentTaxonBase = taxonStore.get(parentId);
					String parentNameCache = null;
					if (parentTaxonBase != null) {
						TaxonNameBase<?,?> parentName = parentTaxonBase.getName();
						if(parentName != null) {
							parentNameCache = ((ZoologicalName)parentName).getNameCache();
						} else {
							logger.warn("Parent taxon name of taxon (uuid= " + parentUuid.toString() + "), id = " +
									parent.getId() + ") is null");
						}
					} else {
						logger.warn("Parent taxon (uuid= " + parentUuid.toString() + "), id = " +
								parent.getId() + ") is null");
					} 
					StringBuilder name = new StringBuilder(parentNameCache);
					name.append(" ");
					name.append(((ZoologicalName)taxonName).getNameCache());
					String concatenatedName = name.toString();
					zooName.setNameCache(concatenatedName);
					zooName.setTitleCache(concatenatedName); // FIXME: Add author
					zooName.setFullTitleCache(concatenatedName); // FIXME: Add author, reference, NC status
					name.append(" sec. ");
					name.append(fauEuConfig.getSourceReference().getTitleCache());
					taxonBase.setTitleCache(name.toString()); // FIXME: Add author
					taxonBase.setSec(fauEuConfig.getSourceReference());
					
					if (taxonBase instanceof Taxon && parentTaxonBase instanceof Taxon) {
						((Taxon)parentTaxonBase).addTaxonomicChild((Taxon)taxonBase, null, null);
						// TODO: citation, microcitation
					}
				} else {
					logger.warn("Parent uuid of " + zooNameCache + " is null");
				}
			} else {
				logger.warn("Parent of " + zooNameCache + " is null");
			}
		}
		return success;
	}
	
	private boolean saveTaxa(Map<String, MapWrapper<? extends CdmBase>> stores) {

//		MapWrapper<TaxonNameBase<?,?>> taxonNameStore = (MapWrapper<TaxonNameBase<?,?>>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);

		int n = 0;
		int nbrOfTaxa = highestTaxonIndex;
//		int nbrOfTaxa = taxonStore.size();
		boolean success = true;

		if(logger.isInfoEnabled()) { logger.info("Saving taxa ..."); }

		if (nbrOfTaxa < limit) {             // TODO: test with critical values
			limit = nbrOfTaxa;
		} else {
			n = nbrOfTaxa / limit;
		}

		if(logger.isInfoEnabled()) { 
			logger.info("number of taxa = " + taxonStore.size() 
					+ ", highest taxon index = " + highestTaxonIndex 
					+ ", limit = " + limit
					+ ", n = " + n); 
		}

		// save taxa in chunks
		
		for (int j = 1; j <= n + 1; j++)
		{
			int offset = j - 1;
			int start = offset * limit;

			if(logger.isInfoEnabled()) { logger.info("Saving taxa: " + start + " - " + (start + limit - 1)); }

			if(logger.isInfoEnabled()) { 
				logger.info("index = " + j 
						+ ", offset = " + offset
						+ ", start = " + start); 
			}
			
			if (j == n + 1) {
				limit = nbrOfTaxa - n * limit;
				if(logger.isInfoEnabled()) { logger.info(", n = " + n + " limit = " + limit); }
			}

			Collection<TaxonBase> taxonMapPart = taxonStore.objects(start, limit);
			getTaxonService().saveTaxonAll(taxonMapPart);
			taxonMapPart = null;
			taxonStore.removeObjects(start, limit);
		}
		
		return success;
	}
}
