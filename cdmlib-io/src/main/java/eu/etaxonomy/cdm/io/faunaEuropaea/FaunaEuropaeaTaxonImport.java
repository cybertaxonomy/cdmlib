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
	private int maxTaxa = 5000;
	/* Max number of taxa to be saved with one service call */
	private int limit = 10000; // TODO: Make configurable
	/* Interval for progress info message when retrieving taxa */
	private int modCount = 10000;
	/* The highest taxon index in the FauEu database */
	private int highestTaxonIndex = 0;
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
//				" SELECT Taxon.*, rank.* " + 
//				" FROM dbo.Taxon INNER JOIN dbo.rank ON dbo.Taxon.TAX_RNK_ID = dbo.rank.rnk_id " +
//				" WHERE (1=1)";

            String top = "";
			if (maxTaxa > 0) {
				top = "TOP " + maxTaxa;
			}
			
			strQuery = 
				" SELECT " + top + " Taxon.*, rank.*, author.* " + 
				" FROM dbo.Taxon LEFT OUTER JOIN dbo.rank ON dbo.Taxon.TAX_RNK_ID = dbo.rank.rnk_id " +
				" LEFT OUTER JOIN dbo.author ON dbo.Taxon.TAX_AUT_ID = dbo.author.aut_id " +
				" WHERE (1=1)";

			if (logger.isInfoEnabled()) {
				logger.info("Query: " + strQuery);
			}
			rs = source.getResultSet(strQuery);
			

//			int i = 0;
			while (rs.next()) {

				if ((i++ % modCount) == 0 && i != 1 ) { 
					if(logger.isInfoEnabled()) {
						logger.info("Taxa retrieved: " + (i-1)); 
					}
				}

				int taxonId = rs.getInt("TAX_ID");
				String taxonName = rs.getString("TAX_NAME");
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
//					success = false; 
					continue;
				} catch (NullPointerException e) {
					logger.warn("Taxon (" + taxonId + ") has rank null and could not be saved.");
//					success = false;
					continue;
				}
				
				ReferenceBase<?> reference = null;

				ZoologicalName zooName = ZoologicalName.NewInstance(rank);
				String nameTitleCache = taxonName;
				
                // set local name cache
				
				zooName.setNameCache(taxonName);
				
//				StringBuilder nameTitleCacheBuilder = new StringBuilder(taxonName);
//				if (year != 0) { // TODO: What do do with authors like xp, xf, etc?
//					nameTitleCacheBuilder.append(" ");
//					if (parenthesis == P_PARENTHESIS) {
//						nameTitleCacheBuilder.append("(");
//					}
//					nameTitleCacheBuilder.append(autName);
//					nameTitleCacheBuilder.append(" ");
//					nameTitleCacheBuilder.append(year);
//					if (parenthesis == P_PARENTHESIS) {
//						nameTitleCacheBuilder.append(")");
//					}
//				}
//				nameTitleCache = nameTitleCacheBuilder.toString();
//				zooName.setTitleCache(nameTitleCache);
//				zooName.setFullTitleCache(nameTitleCache); // FIXME: reference, NC status

				TaxonBase<?> taxonBase;
				FaunaEuropaeaTaxon fauEuTaxon = new FaunaEuropaeaTaxon();

				Synonym synonym;
				Taxon taxon;
				try {
//					logger.debug(status);
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
					
	                // set local title cache
					
//					taxonBase.setTitleCache(nameTitleCache);

					fauEuTaxon.setUuid(taxonBaseUuid);
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

		if(logger.isInfoEnabled()) { logger.info("Processing taxa second pass..."); }

		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);

		boolean success = true;

		for (int id : taxonStore.keySet())
		{
			TaxonBase<?> taxonBase = taxonStore.get(id);
			FaunaEuropaeaTaxon fauEuTaxon = fauEuTaxonMap.get(id);
			
//			success = updateProperties(fauEuConfig, taxonBase, taxonStore, fauEuTaxon, fauEuTaxonMap);
			String nameString = buildTaxonName(fauEuTaxon, taxonBase, taxonStore);
			
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
		
//		String[] tokens = nameString.split("[\\s]+");
//		StringBuilder stringBuilder = 
//		int len = tokens.length - 1;
//		for (int i = 0; i < len - 1; i++) {
//		 String lastToken = tokens[len];
//		}
//		return lastToken;
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

	
	/* Build taxon name */
	private String buildTaxonName(FaunaEuropaeaTaxon fauEuTaxon,
			TaxonBase<?> taxonBase, MapWrapper<TaxonBase> taxonStore) {

		String localString = "";
		String parentString = "";
		String parentNameCache = null;
		
		FaunaEuropaeaTaxon parent = null;
		
		TaxonNameBase<?,?> taxonName = taxonBase.getName();
		TaxonBase<?> parentTaxonBase = null;

		ZoologicalName zooName = (ZoologicalName)taxonName;
		TaxonNameBase<?,?> parentName = null;
		
		boolean parentComplete = false;

		if (zooName != null) {
			localString = zooName.getNameCache();
		}

		int rank = fauEuTaxon.getRankId();
		if (rank == R_SPECIES || rank == R_SUBSPECIES) { // For Species, Subspecies
		//if (rank > R_GENUS) {
			StringBuilder parentStringBuilder = new StringBuilder();
			if(logger.isInfoEnabled()) { 
				logger.info("Local taxon name (rank = " + rank + "): " + localString); 
			}

			// The scientific name in FaunaEuropaeaTaxon is set only once it has been built completely,
			// including parent(s) parts.

			int parentId = fauEuTaxon.getParentId();
			parent = fauEuTaxonMap.get(parentId);
			if (parent != null) {
				UUID parentUuid = parent.getUuid();
				if (parentUuid != null) { 
					parentTaxonBase = taxonStore.get(parentId);
					parentNameCache = parent.getScientificName();
					if (parentNameCache == null) { // parent name has not been built yet
						if (parentTaxonBase != null) {
							parentName = parentTaxonBase.getName();
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
						if (parent.getRankId() == R_SUBGENUS) {
							parentStringBuilder.append("(");
						}
						parentStringBuilder.append(parentNameCache);
						if (parent.getRankId() == R_SUBGENUS) {
							parentStringBuilder.append(")");
						}
//						parentStringBuilder.append(" ");
//						parentStringBuilder.append(((ZoologicalName)taxonName).getNameCache());
						parentString = parentStringBuilder.toString();
						if (logger.isInfoEnabled()) { logger.info("Parent name part built: " + parentString); }
					} else { // parent name is already complete
						parentComplete = true;
						parentString = parent.getScientificName();
						if (logger.isInfoEnabled()) { logger.info("Parent name is complete: " + parentString); }
					}
				} else {
					logger.warn("Parent uuid of " + localString + " is null");
				}
			} else {
				logger.warn("Parent of " + localString + " is null");
			}
			if (parent != null && parent.getRankId() >= R_GENUS  && parentComplete == false) { 
				parentString = buildTaxonName(parent, parentTaxonBase, taxonStore);
			}
		}
		
		StringBuilder concatStringBuilder = new StringBuilder(parentString);
		if (!parentString.equals("")) {
			
			if (!fauEuTaxon.isValid()) { // for synonyms remove the local name of the parent (accepted taxon)
				parentString = removeEpithet(parentString);
			}
			concatStringBuilder.append(" ");
		}
		
		concatStringBuilder.append(localString);
		String concatString = concatStringBuilder.toString();
		concatString = (String) CdmUtils.removeDuplicateWhitespace(concatString.trim());
		
		zooName.setNameCache(concatString);
		String titleCache = buildNameTitleCache(concatString, fauEuTaxon);
		zooName.setTitleCache(titleCache);
		zooName.setFullTitleCache(titleCache); // TODO: Add reference, NC status
		
		fauEuTaxon.setScientificName(concatString);
		return concatString;
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
