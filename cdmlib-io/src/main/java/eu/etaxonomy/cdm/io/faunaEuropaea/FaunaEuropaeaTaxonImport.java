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
public class FaunaEuropaeaTaxonImport extends FaunaEuropaeaImportBase  {
	
	public static final String OS_NAMESPACE_TAXON = "Taxon";
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaTaxonImport.class);

	/* Max number of taxa to retrieve (for test purposes) */
	private int maxTaxa = 0;
	/* Max number of taxa to be saved in CDM DB with one service call */
	private int limit = 1000; // TODO: Make configurable
	/* Max number of taxa to be retrieved from CDM DB with one service call */
	private int limitRetrieve = 10000; // TODO: Make configurable
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
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	protected boolean doInvoke(FaunaEuropaeaImportState state) {				
		
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making taxa..."); }
		
		TransactionStatus txStatus = startTransaction();
		
		success = retrieveTaxa(state, fauEuTaxonMap, Q_NO_RESTRICTION);
//		success = processTaxa(state);
		success = processTaxaFromDatabase(state, fauEuTaxonMap);
		
		commitTransaction(txStatus);
		
		logger.info("End making taxa...");
		return success;
	}


	protected boolean doInvokeAlter(FaunaEuropaeaImportState state) {				
		
		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase<?>> taxonStore = (MapWrapper<TaxonBase<?>>)stores.get(ICdmIO.TAXON_STORE);
//		MapWrapper<TaxonNameBase<?,?>> taxonNamesStore = (MapWrapper<TaxonNameBase<?,?>>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
//		authorStore = null;
//		Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = new HashMap();
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making taxa..."); }
		
		success = retrieveTaxa(state, fauEuTaxonMap, Q_NO_RESTRICTION);
		success = processTaxaSecondPass(state, fauEuTaxonMap);
		success = saveTaxa(state, highestTaxonIndex, state.getConfig().getLimitSave());
//		success = saveTaxa(stores);
		
		logger.info("End making taxa...");
		return success;
	}

	
	/** Retrieve tax from FauEu DB and build FauEuTaxonMap only */
	private boolean retrieveTaxa(FaunaEuropaeaImportState state,
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
				int originalGenusId = rs.getInt("TAX_TAX_IDGENUS");
				int autId = rs.getInt("TAX_AUT_ID");
				int status = rs.getInt("TAX_VALID");
				int year = rs.getInt("TAX_YEAR");
				int parenthesis = rs.getInt("TAX_PARENTHESIS");
				String autName = rs.getString("aut_name");
				Rank rank = null;
//				UUID taxonBaseUuid = UUID.randomUUID();

				FaunaEuropaeaTaxon fauEuTaxon = new FaunaEuropaeaTaxon();
//				fauEuTaxon.setUuid(taxonBaseUuid);
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
				
//				ReferenceBase<?> sourceReference = fauEuConfig.getSourceReference();
//				ReferenceBase<?> auctReference = fauEuConfig.getAuctReference();
//
//				ZoologicalName zooName = ZoologicalName.NewInstance(rank);
//                // set local name cache
//				zooName.setNameCache(localName);
//				
//				TaxonBase<?> taxonBase;
//
//				Synonym synonym;
//				Taxon taxon;
//				try {
//					if ((status == T_STATUS_ACCEPTED) || (autId == A_AUCT)) { // taxon
//						if (autId == A_AUCT) { // misapplied name
//							taxon = Taxon.NewInstance(zooName, auctReference);
//							if (logger.isDebugEnabled()) {
//								logger.debug("Misapplied name created (" + taxonId + ")");
//							}
//						} else { // regular taxon
//							taxon = Taxon.NewInstance(zooName, sourceReference);
//							if (logger.isDebugEnabled()) {
//								logger.debug("Taxon created (" + taxonId + ")");
//							}
//							
//							if (fauEuTaxon.isParenthesis() && (fauEuTaxon.getOriginalGenusId() != 0)
//									&& (fauEuTaxon.getParentId() != fauEuTaxon.getOriginalGenusId())) {
//
//								// create basionym
//								TeamOrPersonBase<?> author = authorStore.get(autId);
//								ZoologicalName basionym = ZoologicalName.NewInstance(rank);
//								basionym.setNameCache(localName);
//								basionym.setCombinationAuthorTeam(author);
//								basionym.setPublicationYear(year);
//								zooName.addBasionym(basionym, sourceReference, null, null);
//								zooName.setBasionymAuthorTeam(author);
//								if (logger.isDebugEnabled()) {
//									logger.debug("Basionym created (" + taxonId + ")");
//								}
//
//								// create homotypic synonym
//								Synonym homotypicSynonym = Synonym.NewInstance(basionym, sourceReference);
//								taxon.addSynonym(homotypicSynonym, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF(), 
//										sourceReference, null);
//								if (logger.isDebugEnabled()) {
//									logger.debug("Homotypic synonym created (" + taxonId + ")");
//								}
//								
//							}
//							
//						}
//						taxonBase = taxon;
//					} else if ((status == T_STATUS_NOT_ACCEPTED) && (autId != A_AUCT)) { // synonym
//						synonym = Synonym.NewInstance(zooName, sourceReference);
//						if (logger.isDebugEnabled()) {
//							logger.debug("Synonym created (" + taxonId + ")");
//						}
//						taxonBase = synonym;
//					} else {
//						logger.warn("Unknown taxon status " + status + ". Taxon (" + taxonId + ") ignored.");
//						continue;
//					}
//
//					taxonBase.setUuid(taxonBaseUuid);
//					
//					ImportHelper.setOriginalSource(taxonBase, fauEuConfig.getSourceReference(), taxonId, namespace);
//					
					
//					if (!taxonStore.containsId(taxonId)) {
//						if (taxonBase == null) {
//							if (logger.isDebugEnabled()) { 
//								logger.debug("Taxon base is null. Taxon (" + taxonId + ") ignored.");
//							}
//							continue;
//						}
						
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

	
	/** Retrieve tax from FauEu DB, build TaxonStore andFauEuTaxonMap */
	private boolean retrieveTaxa_(FaunaEuropaeaImportState state,
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
				int originalGenusId = rs.getInt("TAX_TAX_IDGENUS");
				int autId = rs.getInt("TAX_AUT_ID");
				int status = rs.getInt("TAX_VALID");
				int year = rs.getInt("TAX_YEAR");
				int parenthesis = rs.getInt("TAX_PARENTHESIS");
				String autName = rs.getString("aut_name");
				Rank rank = null;
				UUID taxonBaseUuid = UUID.randomUUID();

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
								taxon.addSynonym(homotypicSynonym, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF(), 
										sourceReference, null);
								if (logger.isDebugEnabled()) {
									logger.debug("Homotypic synonym created (" + taxonId + ")");
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
	
	
	private boolean processTaxaSecondPass_(FaunaEuropaeaImportState state, 
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {

		if(logger.isInfoEnabled()) { logger.info("Processing taxa second pass..."); }

		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
//		MapWrapper<TaxonNameBase<?,?>> taxonNamesStore = (MapWrapper<TaxonNameBase<?,?>>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		ReferenceBase<?> sourceRef = fauEuConfig.getSourceReference();

		boolean success = true;

		for (int id : taxonStore.keySet())
		{
			TaxonBase<?> taxonBase = taxonStore.get(id);
			TaxonNameBase<?,?> taxonName = taxonBase.getName();
			FaunaEuropaeaTaxon fauEuTaxon = fauEuTaxonMap.get(id);
			
			if (logger.isDebugEnabled()) { logger.debug("Taxon # " + id); }
			String nameString = calculateTaxonName_(fauEuTaxon, taxonBase, taxonName, taxonStore, fauEuTaxonMap);
			createRelationships__(fauEuTaxon, taxonBase, taxonName, fauEuTaxonMap, state);
			setTaxonName(nameString, fauEuTaxon, taxonBase, fauEuConfig);
		}
		return success;	
	}

	
	private boolean processTaxaSecondPass(FaunaEuropaeaImportState state, 
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {

		if(logger.isInfoEnabled()) { logger.info("Processing taxa second pass..."); }

		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
//		MapWrapper<TaxonNameBase<?,?>> taxonNamesStore = (MapWrapper<TaxonNameBase<?,?>>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		ReferenceBase<?> sourceRef = fauEuConfig.getSourceReference();

		boolean success = true;

		for (int id : fauEuTaxonMap.keySet())
		//for (int id : taxonStore.keySet())
		{
			TaxonBase<?> taxonBase = taxonStore.get(id);
			if (taxonBase == null) {
				continue;
			}
			TaxonNameBase<?,?> taxonName = taxonBase.getName();
			FaunaEuropaeaTaxon fauEuTaxon = fauEuTaxonMap.get(id);
			
			if (logger.isDebugEnabled()) { logger.debug("Taxon # " + id); }
			String nameString = calculateTaxonName_(fauEuTaxon, taxonBase, taxonName, taxonStore, fauEuTaxonMap);
			createRelationships__(fauEuTaxon, taxonBase, taxonName, fauEuTaxonMap, state);
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

	
	/** Creates relationships if taxon bases are retrieved in chunks from CDM DB */
	private boolean createRelationships(FaunaEuropaeaTaxon fauEuTaxon,
			TaxonBase<?> taxonBase, TaxonNameBase<?,?> taxonName, List<TaxonBase> taxonBases,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap, FaunaEuropaeaImportState state) {
		
		int parentId = fauEuTaxon.getParentId();
		int taxonId = fauEuTaxon.getId();
		FaunaEuropaeaTaxon parentFauEuTaxon = fauEuTaxonMap.get(parentId);
		if (parentFauEuTaxon == null) {
			if (logger.isInfoEnabled()) {
				logger.info("Parent taxon is null (" + parentId + ")");
			}
			return false;
		}
//		UUID parentUuid = parentFauEuTaxon.getUuid();
		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> parentTaxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		ReferenceBase<?> sourceRef = state.getConfig().getSourceReference();

		TaxonBase<?> parentTaxonBase = null;
		
//		for (TaxonBase<?> potentialParentTaxon : taxonBases) {
//			if(potentialParentTaxon.getUuid().equals(parentUuid)) {
//				parentTaxonBase = potentialParentTaxon;
//				break;
//			}
//		}
//		if (parentTaxonBase == null) { 
//			parentTaxonBase = getTaxonService().getTaxonByUuid(parentUuid); 
//		}
		
		// TODO: Copy parents from taxonBases to parentTaxonStore
		
		if (parentTaxonStore.containsId(parentId)) {
			parentTaxonBase = parentTaxonStore.get(parentId);
			if (logger.isInfoEnabled()) {
				logger.info("Parent (" + parentId + ") found in parent taxon store");
			}
//		} else {
//			for (TaxonBase<?> potentialParentTaxon : taxonBases) {
//				if(potentialParentTaxon.getId() == parentId) {
//					parentTaxonBase = potentialParentTaxon;
//					if (logger.isInfoEnabled()) {
//						logger.info("Parent (" + parentId + ") found in taxon base list");
//					}
//					break;
//				}
//			}
		}
		if (parentTaxonBase == null) {
			ISourceable sourceable = 
				getCommonService().getSourcedObjectByIdInSource(TaxonBase.class, Integer.toString(parentId), OS_NAMESPACE_TAXON);
			parentTaxonBase = ((IdentifiableEntity)sourceable).deproxy(sourceable, TaxonBase.class);
			if (logger.isInfoEnabled()) {
				logger.info("Parent (" + parentId + ") retrieved from DB via original source id");
			}
		}
		
		if (!parentTaxonStore.containsId(parentId)) {
			parentTaxonStore.put(parentId, parentTaxonBase);
		}


		
		Taxon parentTaxon = parentTaxonBase.deproxy(parentTaxonBase, Taxon.class);

		boolean success = true;
		
		if (!fauEuTaxon.isValid()) { // FauEu Synonym

//			if (fauEuTaxon.getAuthor() != null && fauEuTaxon.getAuthor().equals("A_AUCT_NAME")) {
//				try {
//					// add misapplied name relationship from this taxon to parent
//					Taxon taxon = taxonBase.deproxy(taxonBase, Taxon.class);
//					taxon.addMisappliedName(parentTaxon, sourceRef, null);
//					if (logger.isDebugEnabled()) {
//						logger.debug("Misapplied name created " + taxon.getUuid());
//					}
//
//				} catch (Exception e) {
//					logger.error("Error creating misapplied name relationship for taxon (" + 
//							parentId + ")");
//				}
//			}
//			
//			else if((fauEuTaxon.getAuthor() == null) 
//					|| (fauEuTaxon.getAuthor() != null && !fauEuTaxon.getAuthor().equals("A_AUCT_NAME"))) {
//				try {
//					// add this synonym as heterotypic synonym to parent
//					Synonym synonym = taxonBase.deproxy(taxonBase, Synonym.class);
//					parentTaxon.addSynonym(synonym, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
//					if (logger.isDebugEnabled()) {
//						logger.debug("Heterotypic synonym created " + synonym.getUuid());
//					}
//
//				} catch (Exception e) {
//					logger.error("Error creating heterotypic synonym for taxon (" + parentId + ")");
////					e.printStackTrace();
//				}
//			}
			
		} else if (fauEuTaxon.isValid()) { // FauEu Taxon
			Taxon taxon = taxonBase.deproxy(taxonBase, Taxon.class);

			try {
				// add this taxon as child to parent
				if (parentTaxon != null) {
					makeTaxonomicallyIncluded(state, parentTaxon, taxon, sourceRef, null);
					if (logger.isDebugEnabled()) {
						logger.debug("Parent-child (" + parentId + "-" + taxonId + 
						") relationship created");
					}
				}

			} catch (Exception e) {
				logger.error("Error creating taxonomically included relationship Parent-child (" + 
						parentId + "-" + taxonId + ")");
			}
			
			// TODO: build name and title caches of additional created objects
			// (basionyms, homotypic synonyms)
			
//			if (fauEuTaxon.isParenthesis() && (fauEuTaxon.getOriginalGenusId() != 0)
//					&& (fauEuTaxon.getParentId() != fauEuTaxon.getOriginalGenusId())) {
//			calculateTaxonName(fauEuTaxon, true, taxonBase, taxonName, taxonStore, fauEuTaxonMap);
//			}
			
		}
		
		return success;
	}

	/** Creates relationships for existing taxon store in memory */
	private boolean createRelationships_(FaunaEuropaeaTaxon fauEuTaxon,
			TaxonBase<?> taxonBase, TaxonNameBase<?,?> taxonName, List<TaxonBase> taxonBases,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap, FaunaEuropaeaImportState state) {
		
		int parentId = fauEuTaxon.getParentId();
		int taxonId = fauEuTaxon.getId();
		FaunaEuropaeaTaxon parentFauEuTaxon = fauEuTaxonMap.get(parentId);
		if (parentFauEuTaxon == null) {
			if (logger.isInfoEnabled()) {
				logger.info("Parent taxon is null (" + parentId + ")");
			}
			return false;
		}
		UUID parentUuid = parentFauEuTaxon.getUuid();
		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		ReferenceBase<?> sourceRef = state.getConfig().getSourceReference();

		TaxonBase<?> parentTaxonBase = null;
		
		for (TaxonBase<?> potentialParentTaxon : taxonBases) {
			if(potentialParentTaxon.getUuid().equals(parentUuid)) {
				parentTaxonBase = potentialParentTaxon;
				break;
			}
		}
		if (parentTaxonBase == null) { 
			parentTaxonBase = getTaxonService().getTaxonByUuid(parentUuid); 
		}
		
//		TaxonBase<?> parentTaxonBase = taxonStore.get(parentId);
		Taxon parentTaxon = parentTaxonBase.deproxy(parentTaxonBase, Taxon.class);
//		FaunaEuropaeaTaxon parent = fauEuTaxonMap.get(parentId);
		boolean success = true;
		
		if (!fauEuTaxon.isValid()) { // FauEu Synonym

			if (fauEuTaxon.getAuthor() != null && fauEuTaxon.getAuthor().equals("A_AUCT_NAME")) {
				try {
					// add misapplied name relationship from this taxon to parent
					Taxon taxon = taxonBase.deproxy(taxonBase, Taxon.class);
					taxon.addMisappliedName(parentTaxon, sourceRef, null);
					if (logger.isDebugEnabled()) {
						logger.debug("Misapplied name created " + taxon.getUuid());
					}

				} catch (Exception e) {
					logger.error("Error creating misapplied name relationship for taxon (" + 
							parentId + ")");
				}
			}
			
			else if((fauEuTaxon.getAuthor() == null) 
					|| (fauEuTaxon.getAuthor() != null && !fauEuTaxon.getAuthor().equals("A_AUCT_NAME"))) {
				try {
					// add this synonym as heterotypic synonym to parent
					Synonym synonym = taxonBase.deproxy(taxonBase, Synonym.class);
					parentTaxon.addSynonym(synonym, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
					if (logger.isDebugEnabled()) {
						logger.debug("Heterotypic synonym created " + synonym.getUuid());
					}

				} catch (Exception e) {
					logger.error("Error creating heterotypic synonym for taxon (" + parentId + ")");
//					e.printStackTrace();
				}
			}
			
		} else if (fauEuTaxon.isValid()) { // FauEu Taxon
			Taxon taxon = taxonBase.deproxy(taxonBase, Taxon.class);

			try {
				// add this taxon as child to parent
				if (parentTaxon != null) {
					makeTaxonomicallyIncluded(state, parentTaxon, taxon, sourceRef, null);
					if (logger.isDebugEnabled()) {
						logger.debug("Parent-child (" + parentId + "-" + taxonId + 
						") relationship created");
					}
				}

			} catch (Exception e) {
				logger.error("Error creating taxonomically included relationship Parent-child (" + 
						parentId + "-" + taxonId + ")");
			}
			
			// TODO: build name and title caches of additional created objects
			// (basionyms, homotypic synonyms)
			
//			if (fauEuTaxon.isParenthesis() && (fauEuTaxon.getOriginalGenusId() != 0)
//					&& (fauEuTaxon.getParentId() != fauEuTaxon.getOriginalGenusId())) {
//			}

		}
		
		return success;
	}

	
	private boolean createRelationships__(FaunaEuropaeaTaxon fauEuTaxon,
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

			if (fauEuTaxon.getAuthor() != null && fauEuTaxon.getAuthor().equals("A_AUCT_NAME")) {
				try {
					// add misapplied name relationship from this taxon to parent
					Taxon taxon = taxonBase.deproxy(taxonBase, Taxon.class);
					taxon.addMisappliedName(parentTaxon, sourceRef, null);
					if (logger.isInfoEnabled()) {
						logger.info("Misapplied name created " + taxon.getUuid());
					}

				} catch (Exception e) {
					logger.error("Error creating misapplied name relationship for taxon (" + 
							parentId + ")");
				}
			}
			
			else if((fauEuTaxon.getAuthor() == null) 
					|| (fauEuTaxon.getAuthor() != null && !fauEuTaxon.getAuthor().equals("A_AUCT_NAME"))) {
				try {
					// add this synonym as heterotypic synonym to parent
					Synonym synonym = taxonBase.deproxy(taxonBase, Synonym.class);
					parentTaxon.addSynonym(synonym, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
					if (logger.isDebugEnabled()) {
						logger.debug("Heterotypic synonym created " + synonym.getUuid());
					}

				} catch (Exception e) {
					logger.error("Error creating heterotypic synonym for taxon (" + parentId + ")");
//					e.printStackTrace();
				}
			}
			
		} else if (fauEuTaxon.isValid()) { // FauEu Taxon
			Taxon taxon = taxonBase.deproxy(taxonBase, Taxon.class);

			try {
				// add this taxon as child to parent
				if (parentTaxon != null) {
					makeTaxonomicallyIncluded(state, parentTaxon, taxon, sourceRef, null);
					if (logger.isDebugEnabled()) {
						logger.debug("Parent-child (" + parentId + "-" + taxonId + 
						") relationship created");
					}
				}

			} catch (Exception e) {
				logger.error("Error creating taxonomically included relationship Parent-child (" + 
						parentId + "-" + taxonId + ")");
			}
			
			// TODO: build name and title caches of additional created objects
			// (basionyms, homotypic synonyms)
			
//			if (fauEuTaxon.isParenthesis() && (fauEuTaxon.getOriginalGenusId() != 0)
//					&& (fauEuTaxon.getParentId() != fauEuTaxon.getOriginalGenusId())) {
//			}

		}
		
		return success;
	}

	
	private boolean makeTaxonomicallyIncluded(FaunaEuropaeaImportState state, Taxon toTaxon, Taxon fromTaxon, ReferenceBase citation, String microCitation){
		boolean success = true;
		ReferenceBase sec = toTaxon.getSec();
		TaxonomicTree tree = state.getTree(sec);
		if (tree == null){
			tree = makeTree(state, sec);
		}
		success = tree.addParentChild(toTaxon, fromTaxon, citation, microCitation);
		return success;
	}

	
	/* Build parent's taxon name for taxa directly imported from FauEu DB 
	 * and additionally created taxa and names */
	private String buildParentName(int maxCalls, String concatString, boolean useOriginalGenus, 
			FaunaEuropaeaTaxon fauEuTaxon, MapWrapper<TaxonBase> taxonStore) {

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
				parentConcatString = buildParentName(maxCalls, parentConcatString, true, parent, taxonStore);
			}
		}

		return parentConcatString;
	}
		
	
	/* Build parent's taxon name for taxa directly imported from FauEu DB*/
	private String buildParentName_(int maxCalls, String concatString, FaunaEuropaeaTaxon fauEuTaxon,
//	private String buildParentName(int originalTaxonRank, String concatString, FaunaEuropaeaTaxon fauEuTaxon,
			MapWrapper<TaxonBase> taxonStore) {

		callCount++;
		if (logger.isDebugEnabled()) { 
			logger.debug("Call counter: " + callCount);
		}

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
//		int maxCalls = maxCallsPerRank(originalTaxonRank);
//		if (logger.isDebugEnabled()) { 
//			logger.debug("Call counter: " + callCount);
//			logger.debug("Max calls: " + maxCalls);
//		}
		if (callCount <= maxCalls) {
			if ((rankId == R_SPECIES || rankId == R_SUBSPECIES) || (rankId == R_SUBGENUS && callCount > 1)) { 
				parentConcatString = buildParentName_(maxCalls, parentConcatString, parent, taxonStore);
//				parentConcatString = buildParentName(originalTaxonRank, parentConcatString, parent, taxonStore);
			}
		}

		return parentConcatString;
	}
	
		
	/* Build taxon's concatenated name string for taxa directly imported from FauEU DB 
	 * and additionally created taxa */
	private String calculateTaxonName(FaunaEuropaeaTaxon fauEuTaxon, boolean useOriginalGenus,
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
		parentString = buildParentName(maxCalls, localString, useOriginalGenus, fauEuTaxon, taxonStore);
//		parentString = buildParentName(rank, localString, fauEuTaxon, taxonStore);
		parentString = (String) CdmUtils.removeDuplicateWhitespace(parentString.trim());
		return parentString;
	}
	
	
	/* Build taxon's concatenated name string for taxa directly imported from FauEU DB */
	private String calculateTaxonName_(FaunaEuropaeaTaxon fauEuTaxon,
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
		parentString = buildParentName(maxCalls, localString, false, fauEuTaxon, taxonStore);
//		parentString = buildParentName(rank, localString, fauEuTaxon, taxonStore);
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
		//titleCache = buildNameFullTitleCache(concatString, fauEuConfig);
		zooName.setFullTitleCache(titleCache); // TODO: Add reference, NC status
		
		ImportHelper.setOriginalSource(taxonName, fauEuConfig.getSourceReference(), 
				fauEuTaxon.getId(), "TaxonName");

		// Set the complete scientific name in FaunaEuropaeaTaxon,
		// including parent(s) parts.
//		fauEuTaxon.setScientificName(concatString);
//		fauEuTaxon.setNameComplete(true);

		if (logger.isDebugEnabled()) { 
			logger.debug("Name stored: " + concatString); 
		}
		return success;
	}
	
	
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

    		TransactionStatus txStatus = startTransaction();
    		
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
				createRelationships(fauEuTaxon, taxonBase, taxonName, taxonBases, fauEuTaxonMap, state);
			}

			getTaxonService().saveTaxonAll(taxonBases);
			taxonBases = null;
			
			commitTransaction(txStatus);
			
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

	
	private boolean processTaxa(FaunaEuropaeaImportState state) {

		if(logger.isInfoEnabled()) { logger.info("Processing taxa first pass..."); }

		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		ReferenceBase<?> sourceRef = fauEuConfig.getSourceReference();

		int n = 0;
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

//				OriginalSource os = (OriginalSource) taxonBase.getSources().toArray()[0];
//				String taxonBaseIdStr = os.getIdInSource();
//				int taxonBaseId = Integer.parseInt(taxonBaseIdStr);
//				FaunaEuropaeaTaxon fauEuTaxon = fauEuTaxonMap.get(taxonBaseId); 
				
				FaunaEuropaeaTaxon fauEuTaxon = findFauEuTaxonByOriginalSourceId(taxonBase, fauEuTaxonMap);

				if (logger.isDebugEnabled()) { 
					logger.debug("Taxon # " + fauEuTaxon.getId()); 
				}
				String nameString = calculateTaxonName(fauEuTaxon, false, taxonBase, taxonName, taxonStore, fauEuTaxonMap);
				setTaxonName(nameString, fauEuTaxon, taxonBase, fauEuConfig);

			}

			getTaxonService().saveTaxonAll(taxonBases);
			taxonStore.removeObjects(start, limit);
			taxonBases = null;
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
			//taxonStore.removeObjects(start, limit);
		}
		
		return success;
	}
}
