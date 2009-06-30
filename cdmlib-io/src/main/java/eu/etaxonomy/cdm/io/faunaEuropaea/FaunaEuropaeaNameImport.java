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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.CdmOneToManyMapper;
import eu.etaxonomy.cdm.io.berlinModel.CdmStringMapper;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.common.CdmAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.tcsxml.in.TcsXmlImportState;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.PublicationBase;
import eu.etaxonomy.cdm.model.reference.Publisher;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
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
	private int limitRetrieve = 10000; // TODO: Make configurable
	/* Interval for progress info message when retrieving taxa */
	private int modCount = 10000;
	/* Highest taxon index in the FauEu database */
//	private int highestTaxonIndex = 0;
	/* Number of times method buildParentName() has been called for one taxon */
	private int callCount = 0;
//	private Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = new HashMap();
	

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
	protected boolean doInvokeAlternate(FaunaEuropaeaImportState state) {				
		
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


	/* 
	 * Import with complete taxon store
	 */
	protected boolean doInvoke(FaunaEuropaeaImportState state) {				
		
		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = state.getFauEuTaxonMap();
		int highestTaxonIndex = state.getHighestTaxonIndex();
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making taxa..."); }
		
		TransactionStatus txStatus = startTransaction();
		
		success = retrieveTaxa2TaxonStore(state, fauEuTaxonMap, Q_NO_RESTRICTION);
		success = processTaxaSecondPass(state, fauEuTaxonMap);
		success = saveTaxa(stores, highestTaxonIndex, limit);
		
		commitTransaction(txStatus);
		
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

	
	/** Retrieve taxa from FauEu DB, build TaxonStore and FauEuTaxonMap */
	private boolean retrieveTaxa2TaxonStore(FaunaEuropaeaImportState state,
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
				
				ReferenceBase<?> sourceReference = fauEuConfig.getSourceReference();
				ReferenceBase<?> auctReference = fauEuConfig.getAuctReference();

				ZoologicalName zooName = ZoologicalName.NewInstance(rank);
                // set local name cache
				zooName.setNameCache(localName);
				
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
									TeamOrPersonBase<?> author = authorStore.get(autId);
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
	
	/*
	 * Processes taxa from complete taxon store
	 */
	private boolean processTaxaSecondPass(FaunaEuropaeaImportState state, 
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {

		if(logger.isInfoEnabled()) { logger.info("Processing taxa second pass..."); }

		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
//		MapWrapper<TaxonNameBase<?,?>> taxonNamesStore = (MapWrapper<TaxonNameBase<?,?>>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		ReferenceBase<?> sourceRef = fauEuConfig.getSourceReference();

		boolean success = true;

		//for (int id : fauEuTaxonMap.keySet())
		for (int id : taxonStore.keySet())
		{
			TaxonBase<?> taxonBase = taxonStore.get(id);
//			if (taxonBase == null) {
//				continue;
//			}
			if (logger.isDebugEnabled()) { logger.debug("Taxon # " + id); }
			
			TaxonNameBase<?,?> taxonName = taxonBase.getName();
			
			FaunaEuropaeaTaxon fauEuTaxon = fauEuTaxonMap.get(id);
			int originalGenusId = fauEuTaxon.getOriginalGenusId();
			if (originalGenusId != 0) {
				FaunaEuropaeaTaxon fauEuOriginalGenusTaxon = fauEuTaxonMap.get(originalGenusId);
				fauEuTaxon.setOriginalGenusName(fauEuOriginalGenusTaxon.getOriginalGenusName());
				if (logger.isDebugEnabled()) { 
					logger.debug("Original genus name: " + fauEuOriginalGenusTaxon.getOriginalGenusName()); 
				}
			}
			
			String nameString = calculateTaxonName(fauEuTaxon, taxonBase, taxonName, taxonStore, fauEuTaxonMap);
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
	
	
	/* Build basionym name cache */
	private String buildBasionymName(FaunaEuropaeaTaxon fauEuTaxon,
			TaxonBase<?> taxonBase, TaxonNameBase<?,?>taxonName, MapWrapper<TaxonBase> taxonStore,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {
		
		if (fauEuTaxon.getRankId() != R_SPECIES || fauEuTaxon.getRankId() != R_SUBSPECIES) {
			logger.error("Cannot build basionym (" + fauEuTaxon.getRankId() + ")");
			return null;
		}
		String localString = "";
		ZoologicalName zooName = (ZoologicalName)taxonName;

		if (zooName != null) {
			localString = zooName.getNameCache();
		}
		StringBuilder basionymNameCacheBuilder = new StringBuilder();
		
		if (fauEuTaxon.getRankId() == R_SUBSPECIES) {
			
		}

			basionymNameCacheBuilder.append(" ");
		
		return basionymNameCacheBuilder.toString();
	}
	
	
	/* Build parent's taxon name for taxa directly imported from FauEu DB */
	private String buildBaName(int maxCalls, String concatString, String originalGenus,
			FaunaEuropaeaTaxon fauEuTaxon, Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap, 
			MapWrapper<TaxonBase> taxonStore) {

		callCount++;
		if (logger.isDebugEnabled()) { 
			logger.debug("Call counter: " + callCount);
		}

		/* Concatenated name string of parent */
		String parentConcatString = concatString;

		StringBuilder parentConcatStringBuilder = new StringBuilder();
		/* Local name of parent */
		String parentString = null;

		int parentId = fauEuTaxon.getParentId();
		int rankId = fauEuTaxon.getRankId();
		int originalGenusId = fauEuTaxon.getOriginalGenusId();
		FaunaEuropaeaTaxon parent = fauEuTaxonMap.get(parentId);

		if (parent == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Parent of taxon (" + fauEuTaxon.getId() + ") is null");
			}
			return parentConcatString;
		}
		if (logger.isDebugEnabled()) { 
			logger.debug("Concat string: " + concatString); 
		}

		if ((rankId == R_SPECIES || rankId == R_SUBSPECIES)) {
			
			originalGenus = fauEuTaxon.getOriginalGenusName();
			UUID parentUuid = parent.getUuid();
			
			if (parentUuid != null) { 
					
				parentString = parent.getLocalName();
				
				if (logger.isDebugEnabled()) { 
					logger.debug("Parent string: " + parentString); 
				}
				if (!fauEuTaxon.isValid()) {

					parentString = "";
					if (logger.isDebugEnabled()) { 
						logger.debug("Emptied synonym's parent string"); 
					}
				}
				
				if (parent.getRankId() == R_SUBGENUS) {
					parentConcatStringBuilder.append("(");
				}
				
				if (parent.getRankId() == R_GENUS && !originalGenus.equals("")) {
					parentString = originalGenus;
				}
				
				parentConcatStringBuilder.append(parentString);

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

		if (callCount <= maxCalls) {
			if ((rankId == R_SPECIES || rankId == R_SUBSPECIES) || (rankId == R_SUBGENUS && callCount > 1)) { 
				parentConcatString = 
					buildBaName(maxCalls, parentConcatString, originalGenus, parent, fauEuTaxonMap, taxonStore);
			}
		}

		return parentConcatString;
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

	
	private boolean basionymNames(FaunaEuropaeaTaxon fauEuTaxon,
			TaxonBase<?> taxonBase, TaxonNameBase<?,?> taxonName,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap, FaunaEuropaeaImportState state) {
		
		int parentId = fauEuTaxon.getParentId();
		int taxonId = fauEuTaxon.getId();
		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		ReferenceBase<?> sourceRef = state.getConfig().getSourceReference();
		TaxonBase<?> parentTaxonBase = taxonStore.get(parentId);
		Taxon parentTaxon = parentTaxonBase.deproxy(parentTaxonBase, Taxon.class);
//		FaunaEuropaeaTaxon parent = fauEuTaxonMap.get(parentId);
		boolean success = true;
		
		if (!fauEuTaxon.isValid()) { // FauEu Synonym

			
		} else if (fauEuTaxon.isValid()) { // FauEu Taxon
			Taxon taxon = taxonBase.deproxy(taxonBase, Taxon.class);

//			if (fauEuTaxon.isParenthesis() && (fauEuTaxon.getOriginalGenusId() != 0)
//			&& (fauEuTaxon.getParentId() != fauEuTaxon.getOriginalGenusId())) {
//	}
//			} catch (Exception e) {
//				logger.error(taxonId);
//			}
			

		}
		
		return success;
	}

	
	/* Build parent's taxon name for taxa directly imported from FauEu DB */
	private String buildParentName(int maxCalls, String concatString, 
			FaunaEuropaeaTaxon fauEuTaxon, Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap,
			MapWrapper<TaxonBase> taxonStore) {

		callCount++;
		if (logger.isDebugEnabled()) { 
			logger.debug("Call counter: " + callCount);
		}

		/* Concatenated name string of parent */
		String parentConcatString = concatString;

		StringBuilder parentConcatStringBuilder = new StringBuilder();
		/* Local name of parent */
		String parentString = null;

		int parentId = fauEuTaxon.getParentId();
		int rankId = fauEuTaxon.getRankId();
		FaunaEuropaeaTaxon parent = fauEuTaxonMap.get(parentId);

		if (parent == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Parent of taxon (" + fauEuTaxon.getId() + ") is null");
			}
			return parentConcatString;
		}
		if (logger.isDebugEnabled()) { 
			logger.debug("Concat string: " + concatString); 
		}

		if ((rankId == R_SPECIES || rankId == R_SUBSPECIES) || (rankId == R_SUBGENUS && callCount > 1)) { 
			UUID parentUuid = parent.getUuid();
			if (parentUuid != null) { 
					
				parentString = parent.getLocalName();
				
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
				
				if (parent.getRankId() == R_SUBGENUS) {
					parentConcatStringBuilder.append("(");
				}
				
//				if (parent.getOriginalGenusId() != 0) {
//					originalGenusId = parent.getOriginalGenusId();
//				}
				
				parentConcatStringBuilder.append(parentString);

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

		if (callCount <= maxCalls) {
			if ((rankId == R_SPECIES || rankId == R_SUBSPECIES) || (rankId == R_SUBGENUS && callCount > 1)) { 
				parentConcatString = buildParentName(maxCalls, parentConcatString, parent, fauEuTaxonMap, taxonStore);
			}
		}

		return parentConcatString;
	}
		
	
		
	/* Build taxon's concatenated name string for taxa directly imported from FauEU DB */
	private String calculateTaxonName(FaunaEuropaeaTaxon fauEuTaxon,
			TaxonBase<?> taxonBase, TaxonNameBase<?,?>taxonName, MapWrapper<TaxonBase> taxonStore,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {

		/* Local name string */
		String localString = "";
		/* Concatenated parent's name string */
		String parentString = "";
		
//		TaxonNameBase<?,?> taxonName = taxonBase.getName();
		ZoologicalName zooName = (ZoologicalName)taxonName;

		if (zooName != null) {
			localString = zooName.getNameCache();
		}

		int rank = fauEuTaxon.getRankId();
		
		if(logger.isDebugEnabled()) { 
			logger.debug("Local taxon name (rank = " + rank + "): " + localString); 
		}
		
		callCount = 0;
		int maxCalls = maxCallsPerRank(rank);
		if (logger.isDebugEnabled()) { 
			logger.debug("Max calls: " + maxCalls);
		}
		
		taxonBase = taxonBase.deproxy(taxonBase, TaxonBase.class);
		
		// for the homotypic synonyms / basionyms build the name with the original genus
		if (taxonBase.isInstanceOf(Synonym.class)) {
			
			Synonym synonym = (Synonym)taxonBase;
			Set<SynonymRelationship> relships = synonym.getSynonymRelations();
			if (relships != null && relships.size() > 0) {
				SynonymRelationship relship = (SynonymRelationship)relships.toArray()[0];
				if (relship.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())) {
					parentString = buildBaName(maxCalls, localString, "", fauEuTaxon, fauEuTaxonMap, taxonStore);
				}
			}
		}
			
		if (parentString.equals("")) {
			parentString = buildParentName(maxCalls, localString, fauEuTaxon, fauEuTaxonMap, taxonStore);
		}
		
		parentString = (String) CdmUtils.removeDuplicateWhitespace(parentString.trim());
		return parentString;
	}
	
	
	/* Sets taxon name caches */
	private boolean setTaxonName(String concatString, FaunaEuropaeaTaxon fauEuTaxon,
			TaxonBase<?> taxonBase, FaunaEuropaeaImportConfigurator fauEuConfig) {

		boolean success = true;
		
		TaxonNameBase<?,?> taxonName = taxonBase.getName();
		ZoologicalName zooName = (ZoologicalName)taxonName;

		zooName.setNameCache(concatString);
		String titleCache = buildNameTitleCache(concatString, fauEuTaxon);
		zooName.setTitleCache(titleCache);
		//titleCache = buildNameFullTitleCache(concatString, fauEuConfig);
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
	
	
	/*
	 * Retrieves taxa in chunks from FauEu DB
	 * Creates taxon relationships
	 * Saves taxa in chunks
	 */
	private boolean processTaxaFromDatabase(FaunaEuropaeaImportState state,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {

		if(logger.isInfoEnabled()) { logger.info("Processing taxa second pass..."); }

		MapWrapper<TaxonBase> taxonBaseMap = new MapWrapper<TaxonBase>(null);

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

		// process taxa in chunks of <=limit

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
			// empty parent taxon store
//			Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
//			MapWrapper<TaxonBase> parentTaxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
//			parentTaxonStore.makeEmpty();
		}
		return success;
	}


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

	
	/*
	 * Processes taxa from complete taxon store
	 * Builds and sets complete names
	 * Saves taxa in chunks
	 */
	private boolean processTaxa(FaunaEuropaeaImportState state) {

		if(logger.isInfoEnabled()) { logger.info("Processing taxa first pass..."); }

		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = state.getFauEuTaxonMap();
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		ReferenceBase<?> sourceRef = fauEuConfig.getSourceReference();

		int n = 0;
		int highestTaxonIndex = state.getHighestTaxonIndex();
		int nbrOfTaxa = highestTaxonIndex;
//		int nbrOfTaxa = taxonStore.size();
		boolean success = true;

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

		// process taxa in chunks of <=limit

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

			Collection<TaxonBase> taxonBases = taxonStore.objects(start, limit);

			for (TaxonBase taxonBase : taxonBases) {

				TaxonNameBase<?,?> taxonName = taxonBase.getName();

				FaunaEuropaeaTaxon fauEuTaxon = findFauEuTaxonByOriginalSourceId(taxonBase, fauEuTaxonMap);

				if (logger.isDebugEnabled()) { 
					logger.debug("Taxon # " + fauEuTaxon.getId()); 
				}
				String nameString = calculateTaxonName(fauEuTaxon, taxonBase, taxonName, taxonStore, fauEuTaxonMap);
				setTaxonName(nameString, fauEuTaxon, taxonBase, fauEuConfig);

			}

			getTaxonService().saveTaxonAll(taxonBases);
			taxonStore.removeObjects(start, limit);
			taxonBases = null;
		}

		return success;
	}

	
}
