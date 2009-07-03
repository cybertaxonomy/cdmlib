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
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.R_SPECIES;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.R_SUBGENUS;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.R_SUBSPECIES;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.T_STATUS_ACCEPTED;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.T_STATUS_NOT_ACCEPTED;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.babadshanjan
 * @created 12.05.2009
 * @version 1.0
 */
@Component
public class FaunaEuropaeaNameImport extends FaunaEuropaeaImportBase  {
	
	public static final String OS_NAMESPACE_TAXON = "Taxon";
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaNameImport.class);

	/* Max number of taxa to retrieve (for test purposes) */
	private int maxTaxa = 5000;
	/* Max number of taxa to be saved in CDM DB with one service call */
	private int limit = 1000; // TODO: Make configurable
	/* Max number of taxa to be retrieved from CDM DB with one service call */
	//private int limitRetrieve = 10000;
	/* Interval for progress info message when retrieving taxa */
	private int modCount = 10000;
	

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
	
	/* 
	 * Import with part taxon store
	 */
	protected boolean doInvokeAlter(FaunaEuropaeaImportState state) {				
		
		Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = state.getFauEuTaxonMap();
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making taxa..."); }
		
		TransactionStatus txStatus = startTransaction();
		
		success = retrieveTaxa2FauEuMap(state, fauEuTaxonMap, Q_NO_RESTRICTION);
//		success = processTaxa(state);
		success = processTaxaFromDatabase(state, fauEuTaxonMap);
		
		commitTransaction(txStatus);
		
		logger.info("End making taxa...");
		return success;
	}


	/** 
	 * Import taxa from FauEU DB
	 */
	protected boolean doInvoke(FaunaEuropaeaImportState state) {				
		
		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = state.getFauEuTaxonMap();
//		int highestTaxonIndex = state.getHighestTaxonIndex();
//		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making taxa..."); }
		
//		TransactionStatus txStatus = startTransaction();
		
		success = retrieveTaxa2TaxonStore(state, fauEuTaxonMap, Q_NO_RESTRICTION);
		success = processTaxaSecondPass(state, fauEuTaxonMap);
		success = saveTaxa(stores, state.getHighestTaxonIndex(), limit);
		
//		commitTransaction(txStatus);
		
		logger.info("End making taxa...");
		return success;
	}

	
	/** Retrieve taxa from FauEu DB and build FauEuTaxonMap only */
	private boolean retrieveTaxa2FauEuMap(FaunaEuropaeaImportState state,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap, int valid) {

		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		ReferenceBase<?> sourceRef = fauEuConfig.getSourceReference();
		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);

		Source source = fauEuConfig.getSource();
//		String namespace = "Taxon";
		int i = 0;
		boolean success = true;

		try {
			
			String strQuery = 
				" SELECT MAX(TAX_ID) AS TAX_ID FROM dbo.Taxon ";
			
			ResultSet rs = source.getResultSet(strQuery);
			while (rs.next()) {
				int maxTaxonId = rs.getInt("TAX_ID");
				state.setHighestTaxonIndex(maxTaxonId);
//				highestTaxonIndex = maxTaxonId;
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
				int originalGenusId = rs.getInt("TAX_TAX_IDGENUS");
				int autId = rs.getInt("TAX_AUT_ID");
				int status = rs.getInt("TAX_VALID");
				int year = rs.getInt("TAX_YEAR");
				int parenthesis = rs.getInt("TAX_PARENTHESIS");
				String autName = rs.getString("aut_name");
				Rank rank = null;
				UUID taxonBaseUuid = null;
				if (resultSetHasColumn(rs, "UUID")){
					taxonBaseUuid = UUID.fromString(rs.getString("UUID"));
				} else {
					taxonBaseUuid = UUID.randomUUID();
				}

				FaunaEuropaeaTaxon fauEuTaxon = new FaunaEuropaeaTaxon();
				fauEuTaxon.setUuid(taxonBaseUuid);
				fauEuTaxon.setLocalName(localName);
				fauEuTaxon.setParentId(parentId);
				fauEuTaxon.setOriginalGenusId(originalGenusId);
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

				try {
					rank = FaunaEuropaeaTransformer.rankId2Rank(rs, false);
				} catch (UnknownCdmTypeException e) {
					logger.warn("Taxon (" + taxonId + ") has unknown rank (" + rankId + ") and could not be saved.");
					continue;
				} catch (NullPointerException e) {
					logger.warn("Taxon (" + taxonId + ") has rank null and can not be saved.");
					continue;
				}
				
				try {


					if (!fauEuTaxonMap.containsKey(taxonId)) {
						if (fauEuTaxon == null) {
							if (logger.isDebugEnabled()) { 
								logger.debug("Taxon base is null. Taxon (" + taxonId + ") ignored.");
							}
							continue;
						}


//						taxonStore.put(taxonId, taxonBase);

						fauEuTaxonMap.put(taxonId, fauEuTaxon);

//						if (logger.isDebugEnabled()) { 
//						logger.debug("Stored taxon base (" + taxonId + ") " + localName); 
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

	
	/** Retrieve taxa from FauEu DB, fill TaxonStore and FauEuTaxonMap */
	private boolean retrieveTaxa2TaxonStore(FaunaEuropaeaImportState state,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap, int valid) {

		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		ReferenceBase<?> sourceRef = fauEuConfig.getSourceReference();
		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);

		Source source = fauEuConfig.getSource();
		int i = 0;
		boolean success = true;

		try {
			
			String strQuery = 
				" SELECT MAX(TAX_ID) AS TAX_ID FROM dbo.Taxon ";
			
			ResultSet rs = source.getResultSet(strQuery);
			while (rs.next()) {
				int maxTaxonId = rs.getInt("TAX_ID");
				state.setHighestTaxonIndex(maxTaxonId);
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
				int originalGenusId = rs.getInt("TAX_TAX_IDGENUS");
				int autId = rs.getInt("TAX_AUT_ID");
				int status = rs.getInt("TAX_VALID");
				int year = rs.getInt("TAX_YEAR");
				int parenthesis = rs.getInt("TAX_PARENTHESIS");
				String autName = rs.getString("aut_name");
				Rank rank = null;
				UUID taxonBaseUuid = null;
				if (resultSetHasColumn(rs,"UUID")){
					taxonBaseUuid = UUID.fromString(rs.getString("UUID"));
				} else {
					taxonBaseUuid = UUID.randomUUID();
				}

				FaunaEuropaeaTaxon fauEuTaxon = new FaunaEuropaeaTaxon();
				fauEuTaxon.setUuid(taxonBaseUuid);
				fauEuTaxon.setLocalName(localName);
				fauEuTaxon.setParentId(parentId);
				fauEuTaxon.setOriginalGenusId(originalGenusId);
				fauEuTaxon.setId(taxonId);
				fauEuTaxon.setRankId(rankId);
				fauEuTaxon.setYear(year);
				fauEuTaxon.setAuthorId(autId);
				fauEuTaxon.setAuthorName(autName);
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

				try {
					rank = FaunaEuropaeaTransformer.rankId2Rank(rs, false);
				} catch (UnknownCdmTypeException e) {
					logger.warn("Taxon (" + taxonId + ") has unknown rank (" + rankId + ") and could not be saved.");
					continue;
				} catch (NullPointerException e) {
					logger.warn("Taxon (" + taxonId + ") has rank null and can not be saved.");
					continue;
				}
				
				ReferenceBase<?> sourceReference = fauEuConfig.getSourceReference();
				ReferenceBase<?> auctReference = fauEuConfig.getAuctReference();

				ZoologicalName zooName = ZoologicalName.NewInstance(rank);
				TeamOrPersonBase<?> author = authorStore.get(autId);
				zooName.setCombinationAuthorTeam(author);
				zooName.setPublicationYear(year);

				// set local name cache
//				zooName.setNameCache(localName);
				
				TaxonBase<?> taxonBase;

				Synonym synonym;
				Taxon taxon;
				try {
					if ((status == T_STATUS_ACCEPTED) || (autId == A_AUCT)) { // taxon
						if (autId == A_AUCT) { // misapplied name
							taxon = Taxon.NewInstance(zooName, auctReference);
							if (logger.isDebugEnabled()) {
								logger.debug("Misapplied name created (" + taxonId + ")");
							}
						} else { // regular taxon
							taxon = Taxon.NewInstance(zooName, sourceReference);
							if (logger.isDebugEnabled()) {
								logger.debug("Taxon created (" + taxonId + ")");
							}
							
							if (fauEuConfig.isDoBasionyms()) {
								if (fauEuTaxon.isParenthesis() && (fauEuTaxon.getOriginalGenusId() != 0)
										&& (fauEuTaxon.getParentId() != fauEuTaxon.getOriginalGenusId())) {

									// create basionym
									ZoologicalName basionym = ZoologicalName.NewInstance(rank);
									basionym.setNameCache(localName);
									basionym.setCombinationAuthorTeam(author);
									basionym.setPublicationYear(year);
									zooName.addBasionym(basionym, sourceReference, null, null);
									zooName.setBasionymAuthorTeam(author);
									if (logger.isDebugEnabled()) {
										logger.debug("Basionym created (" + taxonId + ")");
									}

									// create homotypic synonym
									Synonym homotypicSynonym = Synonym.NewInstance(basionym, sourceReference);
//									SynonymRelationship synRel = 
//										taxon.addSynonym(homotypicSynonym, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF(), 
//												sourceReference, null);
//									homotypicSynonym.addRelationship(synRel);
									taxon.addHomotypicSynonym(homotypicSynonym, sourceReference, null);
									if (logger.isDebugEnabled()) {
										logger.debug("Homotypic synonym created (" + taxonId + ")");
									}

								}
							}
							
						}
						taxonBase = taxon;
					} else if ((status == T_STATUS_NOT_ACCEPTED) && (autId != A_AUCT)) { // synonym
						synonym = Synonym.NewInstance(zooName, sourceReference);
						if (logger.isDebugEnabled()) {
							logger.debug("Synonym created (" + taxonId + ")");
						}
						taxonBase = synonym;
					} else {
						logger.warn("Unknown taxon status " + status + ". Taxon (" + taxonId + ") ignored.");
						continue;
					}

					taxonBase.setUuid(taxonBaseUuid);
					
					ImportHelper.setOriginalSource(taxonBase, fauEuConfig.getSourceReference(), taxonId, OS_NAMESPACE_TAXON);
					
					
					if (!taxonStore.containsId(taxonId)) {
						if (taxonBase == null) {
							if (logger.isDebugEnabled()) { 
								logger.debug("Taxon base is null. Taxon (" + taxonId + ") ignored.");
							}
							continue;
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
	
	
//	private boolean createAdditionalObjects(FaunaEuropaeaTaxon fauEuTaxon,
//			TaxonBase<?> taxonBase, ZoologicalName zooName,
//			FaunaEuropaeaImportState state) { 
//
//		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
//		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
//		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
//		ReferenceBase<?> sourceRef = fauEuConfig.getSourceReference();
//		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
//
//		// create basionym
//		if (fauEuTaxon.isParenthesis() && (fauEuTaxon.getOriginalGenusId() != 0)
//				&& (fauEuTaxon.getParentId() != fauEuTaxon.getOriginalGenusId())) {
//			TeamOrPersonBase<?> author = authorStore.get(fauEuTaxon.getAuthorId());
//			ZoologicalName basionym = ZoologicalName.NewInstance(fauEuTaxon.getRankId());
//			basionym.setNameCache(fauEuTaxon.getLocalName());
//			basionym.setCombinationAuthorTeam(author);
//			basionym.setPublicationYear(fauEuTaxon.getYear());
//			// TODO: add microcitation, rule considered
//			zooName.addBasionym(basionym, sourceRef, null, null);
//			zooName.setBasionymAuthorTeam(author);
//		}
//	}
	
	/**
	 * Processes taxa from complete taxon store
	 */
	private boolean processTaxaSecondPass(FaunaEuropaeaImportState state, 
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {

		if(logger.isInfoEnabled()) { logger.info("Processing taxa second pass..."); }

		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		ReferenceBase<?> sourceRef = fauEuConfig.getSourceReference();

		boolean success = true;

		//for (int id : fauEuTaxonMap.keySet())
		for (int id : taxonStore.keySet())
		{
			TaxonBase<?> taxonBase = taxonStore.get(id);
			if (logger.isDebugEnabled()) { logger.debug("Taxon # " + id); }
			
			TaxonNameBase<?,?> taxonName = taxonBase.getName();
			
			FaunaEuropaeaTaxon fauEuTaxon = fauEuTaxonMap.get(id);
			int originalGenusId = fauEuTaxon.getOriginalGenusId();
			if (originalGenusId != 0) {
				FaunaEuropaeaTaxon fauEuOriginalGenusTaxon = fauEuTaxonMap.get(originalGenusId);
				if (fauEuOriginalGenusTaxon != null) {
					fauEuTaxon.setOriginalGenusName(fauEuOriginalGenusTaxon.getLocalName());
					if (logger.isDebugEnabled()) { 
						logger.debug("Original genus name: " + fauEuOriginalGenusTaxon.getLocalName()); 
					}
				} else {
					if (logger.isDebugEnabled()) { 
						logger.debug("Original genus FauEu taxon is null"); 
					}

				}
			}
			
//			String nameString = calculateTaxonName(fauEuTaxon, taxonBase, taxonName, taxonStore, fauEuTaxonMap);
			String nameString = 
				buildTaxonName(fauEuTaxon, taxonBase, taxonName, taxonStore, fauEuTaxonMap, fauEuConfig);
//			setTaxonName(nameString, fauEuTaxon, taxonBase, fauEuConfig);
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
		if (year != 0) { // TODO: Ignore authors like xp, xf, etc?
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


	/* Build name full title cache */
	private String buildNameFullTitleCache(String titleCache, FaunaEuropaeaImportConfigurator fauEuConfig) {
		
		StringBuilder fullTitleCacheStringBuilder = new StringBuilder(titleCache);
		fullTitleCacheStringBuilder.append(" ");
		fullTitleCacheStringBuilder.append(fauEuConfig.getSourceReferenceTitle());
		return fullTitleCacheStringBuilder.toString();
	}
	
	
	private String genusPart(StringBuilder originalGenusName, boolean useOriginalGenus, 
			StringBuilder genusOrUninomial) {

		StringBuilder stringBuilder = new StringBuilder();
		
		if(useOriginalGenus == true) {
			stringBuilder.append(originalGenusName);
		} else {
			stringBuilder.append(genusOrUninomial);
		}
		stringBuilder.append(" ");

		return stringBuilder.toString();
	}

	
	private String genusSubGenusPart(StringBuilder originalGenusName, boolean useOriginalGenus,
			StringBuilder genusOrUninomial,
			StringBuilder infraGenericEpithet) {

		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append(genusPart(originalGenusName, useOriginalGenus, genusOrUninomial));

		stringBuilder.append("(");
		stringBuilder.append(infraGenericEpithet);
		stringBuilder.append(")");
		stringBuilder.append(" ");
		
		return stringBuilder.toString();
	}
	
	
	/** Build species and subspecies names */
	private String buildLowerTaxonName(StringBuilder originalGenus, boolean useOriginalGenus,
			StringBuilder genusOrUninomial, StringBuilder infraGenericEpithet, 
			StringBuilder specificEpithet, StringBuilder infraSpecificEpithet,
			FaunaEuropaeaTaxon fauEuTaxon, Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {
		
		StringBuilder nameCacheStringBuilder = new StringBuilder();

		int rank = fauEuTaxon.getRankId();
		int parentId = fauEuTaxon.getParentId();
		FaunaEuropaeaTaxon parent = fauEuTaxonMap.get(parentId);
		int grandParentId = parent.getParentId();
		FaunaEuropaeaTaxon grandParent = fauEuTaxonMap.get(grandParentId);
		
		if (rank == R_SPECIES) {

			if(parent.getRankId() == R_SUBGENUS) {

				String genusSubGenusPart = genusSubGenusPart(originalGenus, useOriginalGenus, 
						genusOrUninomial.append(grandParent.getLocalName()), 
						infraGenericEpithet.append(parent.getLocalName()));
				nameCacheStringBuilder.append(genusSubGenusPart);

			} else if(parent.getRankId() == R_GENUS) {

				String genusPart = genusPart(originalGenus, useOriginalGenus, 
						genusOrUninomial.append(parent.getLocalName()));
				nameCacheStringBuilder.append(genusPart);
			}
			nameCacheStringBuilder.append(fauEuTaxon.getLocalName());
			specificEpithet.append(fauEuTaxon.getLocalName());

		} else if (rank == R_SUBSPECIES) {

			if(grandParent.getRankId() == R_SUBGENUS) {

				int greatGrandParentId = grandParent.getParentId();
				FaunaEuropaeaTaxon greatGrandParent = fauEuTaxonMap.get(greatGrandParentId);

				String genusSubGenusPart = genusSubGenusPart(originalGenus, useOriginalGenus, 
						genusOrUninomial.append(greatGrandParent.getLocalName()), 
						infraGenericEpithet.append(grandParent.getLocalName()));
				nameCacheStringBuilder.append(genusSubGenusPart);

			} else if (grandParent.getRankId() == R_GENUS) {

				String genusPart = genusPart(originalGenus, useOriginalGenus, 
						genusOrUninomial.append(grandParent.getLocalName()));
				nameCacheStringBuilder.append(genusPart);

			}
			nameCacheStringBuilder.append(parent.getLocalName());
			nameCacheStringBuilder.append(" ");
			nameCacheStringBuilder.append(fauEuTaxon.getLocalName());
			specificEpithet.append(parent.getLocalName());
			infraSpecificEpithet.append(fauEuTaxon.getLocalName());
		}
		
		return nameCacheStringBuilder.toString();
	}
	
	
	/** Build taxon's name parts and caches */
	private String buildTaxonName(FaunaEuropaeaTaxon fauEuTaxon,
			TaxonBase<?> taxonBase, TaxonNameBase<?,?>taxonName, MapWrapper<TaxonBase> taxonStore,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap, FaunaEuropaeaImportConfigurator fauEuConfig) {

		/* Local taxon name string */
		String localString = "";
		/* Concatenated taxon name string */
		String completeString = "";

		StringBuilder originalGenus = new StringBuilder(fauEuTaxon.getOriginalGenusName()); 
		StringBuilder genusOrUninomial = new StringBuilder();
		StringBuilder infraGenericEpithet = new StringBuilder(); 
		StringBuilder specificEpithet = new StringBuilder();
		StringBuilder infraSpecificEpithet = new StringBuilder();

		localString = fauEuTaxon.getLocalName();

		int rank = fauEuTaxon.getRankId();

		if(logger.isDebugEnabled()) { 
			logger.debug("Local taxon name (rank = " + rank + "): " + localString); 
		}

		if (rank < R_SPECIES) {

			completeString = localString;

		} else {

			taxonBase = taxonBase.deproxy(taxonBase, TaxonBase.class);

			// for the homotypic synonyms / basionyms build the name with the original genus
			if (taxonBase.isInstanceOf(Synonym.class)) {

				Synonym synonym = (Synonym)taxonBase;
				Set<SynonymRelationship> relships = synonym.getSynonymRelations();
				if (relships != null && relships.size() > 0) {
					SynonymRelationship relship = (SynonymRelationship)relships.toArray()[0];
					if (relship.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())) {
						completeString = 
							buildLowerTaxonName(originalGenus, true,
									genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet,
									fauEuTaxon, fauEuTaxonMap);
					}
				}
			}

			if (completeString.equals("")) {
				completeString = 
					buildLowerTaxonName(originalGenus, false,
							genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet,
							fauEuTaxon, fauEuTaxonMap);
			}

			completeString = (String) CdmUtils.removeDuplicateWhitespace(completeString.trim());

		}
		setCompleteTaxonName(completeString, 
				genusOrUninomial.toString(), infraGenericEpithet.toString(), 
				specificEpithet.toString(), infraSpecificEpithet.toString(),
				fauEuTaxon, taxonBase, fauEuConfig);
		return completeString;
	}
	
	
	/** Sets name parts and caches */
	private boolean setCompleteTaxonName(String concatString, 
			String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, 
			FaunaEuropaeaTaxon fauEuTaxon, TaxonBase<?> taxonBase, FaunaEuropaeaImportConfigurator fauEuConfig) {

		boolean success = true;
		
		TaxonNameBase<?,?> taxonName = taxonBase.getName();
		ZoologicalName zooName = (ZoologicalName)taxonName;
		
		if (!genusOrUninomial.equals("")) {
			zooName.setGenusOrUninomial(genusOrUninomial);
			if (logger.isDebugEnabled()) { 
				logger.debug("genusOrUninomial: " + genusOrUninomial); 
			}
		}
		if (!infraGenericEpithet.equals("")) {
			zooName.setInfraGenericEpithet(infraGenericEpithet);
			if (logger.isDebugEnabled()) { 
				logger.debug("infraGenericEpithet: " + infraGenericEpithet); 
			}
		}
		if (!specificEpithet.equals("")) {
			zooName.setSpecificEpithet(specificEpithet);
			if (logger.isDebugEnabled()) { 
				logger.debug("specificEpithet: " + specificEpithet); 
			}
		}
		if (!infraSpecificEpithet.equals("")) {
			zooName.setInfraSpecificEpithet(infraSpecificEpithet);
			if (logger.isDebugEnabled()) { 
				logger.debug("infraSpecificEpithet: " + infraSpecificEpithet); 
			}
		}

		zooName.setNameCache(concatString);
		String titleCache = buildNameTitleCache(concatString, fauEuTaxon);
		zooName.setTitleCache(titleCache);
		//titleCache = buildNameFullTitleCache(concatString, fauEuConfig);
		zooName.setFullTitleCache(titleCache); // TODO: Add reference, NC status
		
		ImportHelper.setOriginalSource(taxonName, fauEuConfig.getSourceReference(), 
				fauEuTaxon.getId(), "TaxonName");

		if (logger.isDebugEnabled()) { 
			logger.debug("Name stored: " + concatString); 
		}
		return success;
	}
	
	
	/**
	 * Retrieves taxa in blocks from CDM DB
	 * Creates taxon relationships
	 * Saves taxa in blocks
	 */
	private boolean processTaxaFromDatabase(FaunaEuropaeaImportState state,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {

		if(logger.isInfoEnabled()) { logger.info("Processing taxa second pass..."); }

		int nbrOfTaxa = getTaxonService().count(TaxonBase.class);
		int n = 0;

		boolean success = true;

		if (nbrOfTaxa < limit) {             // TODO: test with critical values
			limit = nbrOfTaxa;
		} else {
			n = nbrOfTaxa / limit;
		}

		if(logger.isInfoEnabled()) { 
			logger.info("number of taxa = " + nbrOfTaxa 
					+ ", limit = " + limit
					+ ", n = " + n); 
		}

		// process taxa in blocks of <=limit

		for (int j = 1; j <= n + 1; j++)
		{
			int offset = j - 1;
			int start = offset * limit;

			if(logger.isInfoEnabled()) { logger.info("Processing taxa: " + start + " - " + (start + limit - 1)); }

			if(logger.isInfoEnabled()) { 
				logger.info("index = " + j 
						+ ", offset = " + offset
						+ ", start = " + start); 
			}

			if (j == n + 1) {
				limit = nbrOfTaxa - n * limit;
				if(logger.isInfoEnabled()) { logger.info("n = " + n + " limit = " + limit); }
			}

//    		TransactionStatus txStatus = startTransaction();
    		
			List<TaxonBase> taxonBases = getTaxonService().getAllTaxonBases(limit, start);
			if(logger.isInfoEnabled()) { 
				logger.info(taxonBases.size() +  " taxa retrieved from CDM DB"); 
			}

			for (TaxonBase taxonBase : taxonBases) {

				TaxonNameBase<?,?> taxonName = taxonBase.getName();

				FaunaEuropaeaTaxon fauEuTaxon = findFauEuTaxonByOriginalSourceId(taxonBase, fauEuTaxonMap);
				

				if (logger.isDebugEnabled()) { 
					logger.debug("Taxon # " + fauEuTaxon.getId()); 
				}
//				createRelationships(fauEuTaxon, taxonBase, taxonName, taxonBases, fauEuTaxonMap, state);
			}

			getTaxonService().saveTaxonAll(taxonBases);
			taxonBases = null;
		}
		return success;
	}


	/** Identifies taxon via original source id */
	private FaunaEuropaeaTaxon findFauEuTaxonByOriginalSourceId(TaxonBase<?> taxonBase, 
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {

		Set set = taxonBase.getSources();
		Object[] array = set.toArray();
		if (array.length == 0) { return null; }
		OriginalSource os = (OriginalSource) taxonBase.getSources().toArray()[0];
		String taxonBaseIdStr = os.getIdInSource();
		int taxonBaseId = Integer.parseInt(taxonBaseIdStr);
		FaunaEuropaeaTaxon fauEuTaxon = fauEuTaxonMap.get(taxonBaseId); 

		return fauEuTaxon;
	}

}
