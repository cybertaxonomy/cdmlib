/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.A_AUCT;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.P_PARENTHESIS;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.Q_NO_RESTRICTION;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.R_GENUS;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.R_SUBGENUS;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.R_SPECIES;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.R_SUBSPECIES;
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

import eu.etaxonomy.cdm.common.CdmUtils;
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

	/* Max number of taxa to retrieve (for test purposes) */
	private int maxTaxa = 0;
	/* Max number of taxa to be saved with one service call */
	private int limit = 10000; // TODO: Make configurable
	/* Interval for progress info message when retrieving taxa */
	private int modCount = 10000;
	/* Highest taxon index in the FauEu database */
	private int highestTaxonIndex = 0;
	/* Number of times method buildParentName() has been called for one taxon */
	private int callCount = 0;
	private Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = new HashMap();
	

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
//		Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = new HashMap();
		FaunaEuropaeaImportConfigurator fauEuConfig = (FaunaEuropaeaImportConfigurator)config;
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making taxa..."); }
		
		success = retrieveTaxa(fauEuConfig, stores, fauEuTaxonMap, Q_NO_RESTRICTION);
		success = processTaxaSecondPass(fauEuConfig, stores, fauEuTaxonMap);
		success = saveTaxa(stores);
		
		logger.info("End making taxa...");
		return success;
	}


	private boolean retrieveTaxa(FaunaEuropaeaImportConfigurator fauEuConfig, 
			Map<String, MapWrapper<? extends CdmBase>> stores,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap, int valid) {

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
			
            String top = "";
			if (maxTaxa > 0) {
				top = "TOP " + maxTaxa;
			}
			
			String validClause = "";
			if (valid == T_STATUS_ACCEPTED || valid == T_STATUS_NOT_ACCEPTED) {
				validClause = " AND " + " TAX_VALID = " + valid;
			}
			
			strQuery = 
				" SELECT " + top + " Taxon.*, rank.*, author.* " + 
				" FROM dbo.Taxon " +
				" LEFT OUTER JOIN dbo.author ON dbo.Taxon.TAX_AUT_ID = dbo.author.aut_id " +
				" LEFT OUTER JOIN dbo.rank ON dbo.Taxon.TAX_RNK_ID = dbo.rank.rnk_id " +
				" WHERE (1=1)" +
				validClause;

			if (logger.isDebugEnabled()) {
				logger.debug("Query: " + strQuery);
			}
			rs = source.getResultSet(strQuery);
			
			while (rs.next()) {

				if ((i++ % modCount) == 0 && i != 1 ) { 
					if(logger.isInfoEnabled()) {
						logger.info("Taxa retrieved: " + (i-1)); 
					}
				}

				int taxonId = rs.getInt("TAX_ID");
				String localName = rs.getString("TAX_NAME");
				int rankId = rs.getInt("TAX_RNK_ID");
				int parentId = rs.getInt("TAX_TAX_IDPARENT");
				int familyId = rs.getInt("TAX_TAX_IDFAMILY");
				int genusId = rs.getInt("TAX_TAX_IDGENUS");
				int autId = rs.getInt("TAX_AUT_ID");
				int status = rs.getInt("TAX_VALID");
				int year = rs.getInt("TAX_YEAR");
				int parenthesis = rs.getInt("TAX_PARENTHESIS");
				String autName = rs.getString("aut_name");
				Rank rank = null;
				UUID taxonBaseUuid = UUID.randomUUID();

				try {
					rank = FaunaEuropaeaTransformer.rankId2Rank(rs, false);
				} catch (UnknownCdmTypeException e) {
					logger.warn("Taxon (" + taxonId + ") has unknown rank (" + rankId + ") and could not be saved.");
					continue;
				} catch (NullPointerException e) {
					logger.warn("Taxon (" + taxonId + ") has rank null and could not be saved.");
					continue;
				}
				
				ReferenceBase<?> reference = null;

				ZoologicalName zooName = ZoologicalName.NewInstance(rank);
				String nameTitleCache = localName;
				
                // set local name cache
				zooName.setNameCache(localName);
				
				TaxonBase<?> taxonBase;
				FaunaEuropaeaTaxon fauEuTaxon = new FaunaEuropaeaTaxon();

				Synonym synonym;
				Taxon taxon;
				try {
					if ((status == T_STATUS_ACCEPTED) || (autId == A_AUCT)) {
						taxon = Taxon.NewInstance(zooName, reference);
						taxonBase = taxon;
					} else if ((status == T_STATUS_NOT_ACCEPTED) && (autId != A_AUCT)) {
						synonym = Synonym.NewInstance(zooName, reference);
						taxonBase = synonym;
					} else {
						logger.warn("Unknown taxon status " + status + ". Taxon (" + taxonId + ") ignored.");
						continue;
					}

					taxonBase.setUuid(taxonBaseUuid);
					
					fauEuTaxon.setUuid(taxonBaseUuid);
					fauEuTaxon.setLocalName(localName);
					fauEuTaxon.setParentId(parentId);
					fauEuTaxon.setId(taxonId);
					fauEuTaxon.setRankId(rankId);
					fauEuTaxon.setYear(year);
					fauEuTaxon.setAuthor(autName);
					if (parenthesis == P_PARENTHESIS) {
						fauEuTaxon.setParenthesis(true);
					} else {
						fauEuTaxon.setParenthesis(false);
					}
					if (status == T_STATUS_ACCEPTED) {
						fauEuTaxon.setValid(true);
					} else {
						fauEuTaxon.setValid(false);
					}

					ImportHelper.setOriginalSource(taxonBase, fauEuConfig.getSourceReference(), taxonId, namespace);
					
					if (!taxonStore.containsId(taxonId)) {
						if (taxonBase == null) {
							logger.warn("Taxon base is null");
						}
						taxonStore.put(taxonId, taxonBase);
						fauEuTaxonMap.put(taxonId, fauEuTaxon);
//						if (logger.isDebugEnabled()) { 
//							logger.debug("Stored taxon base (" + taxonId + ") " + localName); 
//						}
					} else {
						logger.warn("Not imported taxon base with duplicated TAX_ID (" + taxonId + 
								") " + localName);
					}
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

		if(logger.isInfoEnabled()) { logger.info("Processing taxa second pass..."); }

		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);

		boolean success = true;

		for (int id : taxonStore.keySet())
		{
			TaxonBase<?> taxonBase = taxonStore.get(id);
			FaunaEuropaeaTaxon fauEuTaxon = fauEuTaxonMap.get(id);
			
			if (logger.isDebugEnabled()) { logger.debug("Taxon # " + id); }
			String nameString = calculateTaxonName(fauEuTaxon, taxonBase, taxonStore, fauEuTaxonMap);
//			String nameString = calculateTaxonName(fauEuTaxon, taxonBase, taxonStore);
			setTaxonName(nameString, fauEuTaxon, taxonBase, fauEuConfig);
			
		}
		return success;	
	}

	
	/* Remove last part of name */
	private String removeEpithet(String nameString) {
		
		String subString = nameString;
		int index = nameString.lastIndexOf(" ");
		if (index > 0) {
			subString = nameString.substring(0, index);
		}
		return subString;
	}
	
	
	/* Build name title cache */
	private String buildNameTitleCache(String nameString, FaunaEuropaeaTaxon fauEuTaxon) {
		
		StringBuilder titleCacheStringBuilder = new StringBuilder(nameString);
		int year = fauEuTaxon.getYear();
		if (year != 0) { // TODO: What do do with authors like xp, xf, etc?
			titleCacheStringBuilder.append(" ");
			if (fauEuTaxon.isParenthesis() == true) {
				titleCacheStringBuilder.append("(");
			}
			titleCacheStringBuilder.append(fauEuTaxon.getAuthor());
			titleCacheStringBuilder.append(" ");
			titleCacheStringBuilder.append(year);
			if (fauEuTaxon.isParenthesis() == true) {
				titleCacheStringBuilder.append(")");
			}
		}
		return titleCacheStringBuilder.toString();
	}


	/* For a given rank, returns max number of recurse calls of buildParentName() */
	public static int maxCallsPerRank(int rankId) {

		int result;
		switch(rankId) {
		case R_GENUS: 
			result = 1;
			break;
		case R_SUBGENUS: 
			result = 2;
			break;
		case R_SPECIES: 
			result = 3;
			break;
		case R_SUBSPECIES: 
			result = 4;
			break;
		default: 
			result = 1;
		}
		return result;
	}
			
			
	/* Build parent's taxon name */
	private String buildParentName(int originalTaxonRank, String concatString, FaunaEuropaeaTaxon fauEuTaxon,
			MapWrapper<TaxonBase> taxonStore) {

		callCount++;

		/* Concatenated name string of parent */
//		String parentConcatString = "";
		String parentConcatString = concatString;

		StringBuilder parentConcatStringBuilder = new StringBuilder();
		/* Local name of parent */
		String parentString = null;

		int parentId = fauEuTaxon.getParentId();
		int rankId = fauEuTaxon.getRankId();
		FaunaEuropaeaTaxon parent = fauEuTaxonMap.get(parentId);
//		TaxonBase<?> parentTaxonBase = null;
//		TaxonNameBase<?,?> parentName = null;

		if (parent == null) {
			logger.warn("Parent of taxon (" + fauEuTaxon.getId() + ") is null");
			return parentConcatString;
		}
		if (logger.isDebugEnabled()) { 
			logger.debug("Concat string: " + concatString); 
		}

		if ((rankId == R_SPECIES || rankId == R_SUBSPECIES) || (rankId == R_SUBGENUS && callCount > 1)) { 
			UUID parentUuid = parent.getUuid();
			if (parentUuid != null) { 
//				parentTaxonBase = taxonStore.get(parentId);
//				if (parentTaxonBase != null) {
//				parentName = parentTaxonBase.getName();
//				if(parentName != null) {
//				parentString = ((ZoologicalName)parentName).getNameCache();


				parentString = parent.getLocalName();

//				if (fauEuTaxon.isValid()) {
//				parentString = parent.getLocalName();
//				} else {
//				parentString = fauEuTaxon.get
//				}

				// parentString is either already complete or just local, depending on whether the
				// parent taxon has already processed
				if (logger.isDebugEnabled()) { 
					logger.debug("Parent string: " + parentString); 
				}
				if (!fauEuTaxon.isValid()) {
//					parentString = exchangeEpithet(parentString);
					parentString = "";
					if (logger.isDebugEnabled()) { 
						logger.debug("Emptied synonym's parent string"); 
					}
				}
//				} else {
//				logger.warn("Parent taxon name of taxon (uuid= " + parentUuid.toString() + "), id = " +
//				parent.getId() + ") is null");
//				}
//				} else {
//				logger.warn("Parent taxon (uuid= " + parentUuid.toString() + "), id = " +
//				parent.getId() + ") is null");
//				} 
//				if (parent.isNameComplete()) {
//				// make sure to stop parent name building
//				callCount = 1000;
//				}
//				if (parent.getRankId() == R_SUBGENUS && !parent.isNameComplete()) {
				if (parent.getRankId() == R_SUBGENUS) {
					parentConcatStringBuilder.append("(");
				}
				parentConcatStringBuilder.append(parentString);
//				if (parent.getRankId() == R_SUBGENUS && !parent.isNameComplete()) {
				if (parent.getRankId() == R_SUBGENUS) {
					parentConcatStringBuilder.append(")");
				}
				parentConcatStringBuilder.append(" ");
				parentConcatStringBuilder.append(concatString);
				parentConcatString = parentConcatStringBuilder.toString();

				if (logger.isDebugEnabled()) { 
					logger.debug("Concatenated name: " + parentConcatString); 
				}
			} else {
				logger.warn("Parent uuid of " + parentId + " is null");
			}

		} else { // Higher ranks
			if (logger.isDebugEnabled()) { 
				logger.debug("Name complete for taxon rank (" + rankId + ")");
			}
			return parentConcatString;
		}
		int maxCalls = maxCallsPerRank(originalTaxonRank);
		if (logger.isDebugEnabled()) { 
			logger.debug("Call counter: " + callCount);
			logger.debug("Max calls: " + maxCalls);
		}
		if (callCount <= maxCalls) {
			if ((rankId == R_SPECIES || rankId == R_SUBSPECIES) || (rankId == R_SUBGENUS && callCount > 1)) { 
				parentConcatString = buildParentName(originalTaxonRank, parentConcatString, parent, taxonStore);
			}
		}

		return parentConcatString;
	}
		
	
	/* Build taxon's concatenated name string */
	private String calculateTaxonName(FaunaEuropaeaTaxon fauEuTaxon,
			TaxonBase<?> taxonBase, MapWrapper<TaxonBase> taxonStore,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {

		/* Local name string */
		String localString = "";
		/* Concatenated parent's name string */
		String parentString = "";
		
		TaxonNameBase<?,?> taxonName = taxonBase.getName();
		ZoologicalName zooName = (ZoologicalName)taxonName;

		if (zooName != null) {
			localString = zooName.getNameCache();
		}

		int rank = fauEuTaxon.getRankId();
		
		if(logger.isDebugEnabled()) { 
			logger.debug("Local taxon name (rank = " + rank + "): " + localString); 
		}
		
		callCount = 0;
		parentString = buildParentName(rank, localString, fauEuTaxon, taxonStore);
		parentString = (String) CdmUtils.removeDuplicateWhitespace(parentString.trim());
		return parentString;
	}

	
	/* Build taxon name */
	private boolean setTaxonName(String concatString, FaunaEuropaeaTaxon fauEuTaxon,
			TaxonBase<?> taxonBase, FaunaEuropaeaImportConfigurator fauEuConfig) {

		boolean success = true;
		
		TaxonNameBase<?,?> taxonName = taxonBase.getName();
		ZoologicalName zooName = (ZoologicalName)taxonName;

		zooName.setNameCache(concatString);
		String titleCache = buildNameTitleCache(concatString, fauEuTaxon);
		zooName.setTitleCache(titleCache);
		zooName.setFullTitleCache(titleCache); // TODO: Add reference, NC status
		
		ImportHelper.setOriginalSource(taxonName, fauEuConfig.getSourceReference(), 
				fauEuTaxon.getId(), "TaxonName");

		// Set the complete scientific name in FaunaEuropaeaTaxon,
		// including parent(s) parts.
//		fauEuTaxon.setScientificName(concatString);
		fauEuTaxon.setNameComplete(true);

		if (logger.isDebugEnabled()) { 
			logger.debug("Name stored: " + concatString); 
		}
		return success;
	}
	
	
	private boolean saveTaxa(Map<String, MapWrapper<? extends CdmBase>> stores) {

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

		// save taxa in chunks of <=limit
		
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
				if(logger.isInfoEnabled()) { logger.info("n = " + n + " limit = " + limit); }
			}

			Collection<TaxonBase> taxonMapPart = taxonStore.objects(start, limit);
			getTaxonService().saveTaxonAll(taxonMapPart);
			taxonMapPart = null;
			taxonStore.removeObjects(start, limit);
		}
		
		return success;
	}
}
