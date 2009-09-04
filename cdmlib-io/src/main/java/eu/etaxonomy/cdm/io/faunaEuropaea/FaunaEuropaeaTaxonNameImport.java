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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.profiler.ProfilerController;
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
public class FaunaEuropaeaTaxonNameImport extends FaunaEuropaeaImportBase  {
	
	public static final String OS_NAMESPACE_TAXON = "Taxon";
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaTaxonNameImport.class);

	/* Max number of taxa to retrieve (for test purposes) */
	private int maxTaxa = 0;
	/* Interval for progress info message when retrieving taxa */
//	private int modCount = 10000;
	

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
		
		ProfilerController.memorySnapshot();

//		TransactionStatus txStatus = null;
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making taxa..."); }

//		if (state.getConfig().isUseTransactions()) {
//			txStatus = startTransaction();
//		}
		
		success = retrieveTaxa(state);
//		success = processTaxaSecondPass(state);
//		success = saveTaxa(state, state.getHighestTaxonIndex(), state.getConfig().getLimitSave());
		
//		if (state.getConfig().isUseTransactions()) {
//			commitTransaction(txStatus);
//		}
		
		logger.info("End making taxa...");
		ProfilerController.memorySnapshot();
		return success;
	}

	
	/** Retrieve taxa from FauEu DB, fill TaxonStore */
	private boolean retrieveTaxa(FaunaEuropaeaImportState state) {

		int limit = state.getConfig().getLimitSave();

		TransactionStatus txStatus = null;

		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
		
//		MapWrapper<TaxonBase> nameStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXONNAME_STORE);
//		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
//		Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = state.getFauEuTaxonMap();
		
		Map<Integer, TaxonBase<?>> taxonMap = null;
		Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = null;

//		Map<Integer, TaxonBase<?>> taxonMap = new HashMap<Integer, TaxonBase<?>>(limit);
//		Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = new HashMap<Integer, FaunaEuropaeaTaxon>(limit);
		
//		state.setFauEuTaxonMap(fauEuTaxonMap);
//		state.setFauEuTaxonMap(fauEuTaxonMap);

		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		ReferenceBase<?> sourceRef = fauEuConfig.getSourceReference();

		Source source = fauEuConfig.getSource();
		int i = 0;
		boolean success = true;

		try {

			String strQuery = 
				" SELECT Taxon.*, rank.*, author.* " + 
				" FROM dbo.Taxon " +
				" LEFT OUTER JOIN dbo.author ON dbo.Taxon.TAX_AUT_ID = dbo.author.aut_id " +
				" LEFT OUTER JOIN dbo.rank ON dbo.Taxon.TAX_RNK_ID = dbo.rank.rnk_id " +
				" WHERE (1=1)";

			if (logger.isDebugEnabled()) {
				logger.debug("Query: " + strQuery);
			}
			ResultSet rs = source.getResultSet(strQuery);

			while (rs.next()) {

				if ((i++ % limit) == 0) { 
					if (state.getConfig().isUseTransactions()) {
						txStatus = startTransaction();
					}
					taxonMap = new HashMap<Integer, TaxonBase<?>>(limit);
					fauEuTaxonMap = new HashMap<Integer, FaunaEuropaeaTaxon>(limit);
					if(logger.isInfoEnabled()) {
						logger.info("i = " + i + " - Transaction started"); 
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
				String autName = rs.getString("aut_name");

				Rank rank = null;
				int parenthesis = rs.getInt("TAX_PARENTHESIS");
				UUID taxonBaseUuid = null;
				if (resultSetHasColumn(rs,"UUID")){
					taxonBaseUuid = UUID.fromString(rs.getString("UUID"));
				} else {
					taxonBaseUuid = UUID.randomUUID();
				}

//				String originalGenusName = "";
//				String genusName = "";
//				String specificEpithet = "";


				FaunaEuropaeaTaxon fauEuTaxon = new FaunaEuropaeaTaxon();
				fauEuTaxon.setUuid(taxonBaseUuid);
				fauEuTaxon.setLocalName(rs.getString("TAX_NAME"));
				fauEuTaxon.setParentId(rs.getInt("TAX_TAX_IDPARENT"));
//				fauEuTaxon.setOriginalGenusId(rs.getInt("TAX_TAX_IDGENUS"));
				fauEuTaxon.setId(rs.getInt("TAX_ID"));
				fauEuTaxon.setRankId(rs.getInt("TAX_RNK_ID"));
				fauEuTaxon.setYear(rs.getInt("TAX_YEAR"));
//				fauEuTaxon.setAuthorId(autId);
				fauEuTaxon.setAuthorName(rs.getString("aut_name"));
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
					ImportHelper.setOriginalSource(zooName, fauEuConfig.getSourceReference(), taxonId, "TaxonName");


					if (!taxonMap.containsKey(taxonId)) {
						if (taxonBase == null) {
							if (logger.isDebugEnabled()) { 
								logger.debug("Taxon base is null. Taxon (" + taxonId + ") ignored.");
							}
							continue;
						}
						taxonMap.put(taxonId, taxonBase);
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
					e.printStackTrace();
				}

				if ((i % limit) == 0 && i != 1 ) { 

					success = processTaxaSecondPass(state, taxonMap, fauEuTaxonMap);
					success = saveTaxa(state,  taxonMap);

//					taxonStore.makeNewMap((IService)getTaxonService());
					
					if (logger.isDebugEnabled()) {
						logger.debug("Final size of taxon store is: " + taxonMap.size());
					}
					taxonMap = null;
					fauEuTaxonMap = null;
					
					if (state.getConfig().isUseTransactions()) {
						commitTransaction(txStatus);
					}
					if(logger.isInfoEnabled()) {
						logger.info("i = " + i + " - Transaction committed"); 
					}
				}

			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			success = false;
		}

		return success;
	}
	
	
	/**
	 * Processes taxa from complete taxon store
	 */
	private boolean processTaxaSecondPass(FaunaEuropaeaImportState state, Map<Integer, TaxonBase<?>> taxonMap,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {

		if(logger.isDebugEnabled()) { logger.debug("Processing taxa second pass..."); }

//		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
//		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
//		Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = state.getFauEuTaxonMap();

		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		ReferenceBase<?> sourceRef = fauEuConfig.getSourceReference();
		
		boolean success = true;

		for (int id : taxonMap.keySet())
		{
			TaxonBase<?> taxonBase = taxonMap.get(id);
			if (logger.isDebugEnabled()) { logger.debug("Taxon # " + id); }
			
			TaxonNameBase<?,?> taxonName = taxonBase.getName();
			
			FaunaEuropaeaTaxon fauEuTaxon = fauEuTaxonMap.get(id);
			
			String nameString = 
				buildTaxonName(fauEuTaxon, taxonBase, taxonName, false, fauEuTaxonMap, fauEuConfig);
			
			if (fauEuConfig.isDoBasionyms() && (fauEuTaxon.isValid()) &&
					(fauEuTaxon.getOriginalGenusId() != 0) &&
					(fauEuTaxon.getParentId() != fauEuTaxon.getOriginalGenusId())) {
				success = createBasionym(fauEuTaxon, taxonBase, taxonName, fauEuTaxonMap, fauEuConfig);
			}
		}
		return success;	
	}

	
	private boolean createBasionym(FaunaEuropaeaTaxon fauEuTaxon, TaxonBase<?> taxonBase, TaxonNameBase<?,?>taxonName, 
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap, FaunaEuropaeaImportConfigurator fauEuConfig) {

//		if (fauEuTaxon.isParenthesis() && (fauEuTaxon.getOriginalGenusId() != 0)
//		&& (fauEuTaxon.getParentId() != fauEuTaxon.getOriginalGenusId())) {

		boolean success = true;

		try {
			ZoologicalName zooName = taxonName.deproxy(taxonName, ZoologicalName.class);
			Taxon taxon = taxonBase.deproxy(taxonBase, Taxon.class);
			
			// create basionym
			ZoologicalName basionym = ZoologicalName.NewInstance(taxonName.getRank());
			basionym.setCombinationAuthorTeam(zooName.getCombinationAuthorTeam());
			basionym.setPublicationYear(zooName.getPublicationYear());
			zooName.addBasionym(basionym, fauEuConfig.getSourceReference(), null, null);
			zooName.setBasionymAuthorTeam(zooName.getCombinationAuthorTeam());
			if (logger.isDebugEnabled()) {
				logger.debug("Basionym created (" + fauEuTaxon.getId() + ")");
			}

			// create homotypic synonym
			Synonym homotypicSynonym = Synonym.NewInstance(basionym, fauEuConfig.getSourceReference());
//			SynonymRelationship synRel = 
//			taxon.addSynonym(homotypicSynonym, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF(), 
//			sourceReference, null);
//			homotypicSynonym.addRelationship(synRel);
			taxon.addHomotypicSynonym(homotypicSynonym, fauEuConfig.getSourceReference(), null);
			if (logger.isDebugEnabled()) {
				logger.debug("Homotypic synonym created (" + fauEuTaxon.getId() + ")");
			}
			
			buildTaxonName(fauEuTaxon, homotypicSynonym, basionym, true, fauEuTaxonMap, fauEuConfig);
			
		} catch (Exception e) {
			logger.warn("Exception occurred when creating basionym for " + fauEuTaxon.getId());
			e.printStackTrace();
		}
		
		
		return success;
	}
	
	
	/* Build name title cache */
	private String buildNameTitleCache(String nameString, boolean useOriginalGenus, FaunaEuropaeaTaxon fauEuTaxon) {
		
		StringBuilder titleCacheStringBuilder = new StringBuilder(nameString);
		int year = fauEuTaxon.getYear();
		if (year != 0) { // TODO: Ignore authors like xp, xf, etc?
			titleCacheStringBuilder.append(" ");
			if ((fauEuTaxon.isParenthesis() == true) && !useOriginalGenus) {
				titleCacheStringBuilder.append("(");
			}
			titleCacheStringBuilder.append(fauEuTaxon.getAuthor());
			titleCacheStringBuilder.append(" ");
			titleCacheStringBuilder.append(year);
			if ((fauEuTaxon.isParenthesis() == true) && !useOriginalGenus) {
				titleCacheStringBuilder.append(")");
			}
		}
		return titleCacheStringBuilder.toString();
	}


	/* Build taxon title cache */
	private String buildTaxonTitleCache(String nameCache, ReferenceBase<?> reference) {
		
		StringBuilder titleCacheStringBuilder = new StringBuilder(nameCache);
		titleCacheStringBuilder.append(" sec. ");
		titleCacheStringBuilder.append(reference.getTitleCache());
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
			genusOrUninomial.delete(0, genusOrUninomial.length());
			genusOrUninomial.append(originalGenusName);
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

		if (useOriginalGenus == true) {
			infraGenericEpithet.delete(0, infraGenericEpithet.length());
			stringBuilder.append(" ");
			return stringBuilder.toString();
		}

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
		if (parent == null) {
			nameCacheStringBuilder.append(fauEuTaxon.getLocalName());
			if (logger.isInfoEnabled()) {
				logger.info("Parent of (" + fauEuTaxon.getId() + ") is null");
			}
			return nameCacheStringBuilder.toString();
		}
		int grandParentId = parent.getParentId();
		FaunaEuropaeaTaxon grandParent = fauEuTaxonMap.get(grandParentId);
		int greatGrandParentId = grandParent.getParentId();
		FaunaEuropaeaTaxon greatGrandParent = fauEuTaxonMap.get(greatGrandParentId);

		
		if (fauEuTaxon.isValid()) { // Taxon
			
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
		} else { // Synonym
			
			if (rank == R_SPECIES) {

				if(grandParent.getRankId() == R_SUBGENUS) {
					
					String genusSubGenusPart = genusSubGenusPart(originalGenus, useOriginalGenus, 
							genusOrUninomial.append(greatGrandParent.getLocalName()), 
							infraGenericEpithet.append(grandParent.getLocalName()));
					nameCacheStringBuilder.append(genusSubGenusPart);

				} else if (grandParent.getRankId() == R_GENUS) {
					
					String genusPart = genusPart(originalGenus, useOriginalGenus, 
							genusOrUninomial.append(grandParent.getLocalName()));
					nameCacheStringBuilder.append(genusPart);

				}

			} else if (rank == R_SUBSPECIES) {
				
				int greatGreatGrandParentId = grandParent.getParentId();
				FaunaEuropaeaTaxon greatGreatGrandParent = fauEuTaxonMap.get(greatGreatGrandParentId);
				
				if(greatGrandParent.getRankId() == R_SUBGENUS) {
					
					String genusSubGenusPart = genusSubGenusPart(originalGenus, useOriginalGenus, 
							genusOrUninomial.append(greatGreatGrandParent.getLocalName()), 
							infraGenericEpithet.append(greatGrandParent.getLocalName()));
					nameCacheStringBuilder.append(genusSubGenusPart);
					
				} else if (greatGrandParent.getRankId() == R_GENUS) {
					
					String genusPart = genusPart(originalGenus, useOriginalGenus, 
							genusOrUninomial.append(greatGreatGrandParent.getLocalName()));
					nameCacheStringBuilder.append(genusPart);
				}
				
				nameCacheStringBuilder.append(grandParent.getLocalName());
				nameCacheStringBuilder.append(" ");
				specificEpithet.append(grandParent.getLocalName());
			}
			
			nameCacheStringBuilder.append(fauEuTaxon.getLocalName());
			infraSpecificEpithet.append(fauEuTaxon.getLocalName());
			
		}
		
		return nameCacheStringBuilder.toString();
	}
	
	
	/** Build taxon's name parts and caches */
	private String buildTaxonName(FaunaEuropaeaTaxon fauEuTaxon, TaxonBase<?> taxonBase, TaxonNameBase<?,?>taxonName,
			boolean useOriginalGenus, Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap, FaunaEuropaeaImportConfigurator fauEuConfig) {

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
			if (rank == R_SUBGENUS) {
				infraGenericEpithet.append(localString);
			} else {
				genusOrUninomial.append(localString);
			}
			
		} else {

			taxonBase = taxonBase.deproxy(taxonBase, TaxonBase.class);

//			completeString = 
//				buildLowerTaxonName(originalGenus, useOriginalGenus,
//						genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet,
//						fauEuTaxon, fauEuTaxonMap);
//			
//			completeString = (String) CdmUtils.removeDuplicateWhitespace(completeString.trim());

		}
		setCompleteTaxonName(completeString, useOriginalGenus,
				genusOrUninomial.toString(), infraGenericEpithet.toString(), 
				specificEpithet.toString(), infraSpecificEpithet.toString(),
				fauEuTaxon, taxonBase, fauEuConfig);
		return completeString;
	}
	
	
	/** Sets name parts and caches */
	private boolean setCompleteTaxonName(String concatString, boolean useOriginalGenus,
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
		String titleCache = buildNameTitleCache(concatString, useOriginalGenus, fauEuTaxon);
		zooName.setTitleCache(titleCache);
		//titleCache = buildNameFullTitleCache(concatString, fauEuConfig);
		zooName.setFullTitleCache(titleCache); // TODO: Add reference, NC status
		
//		ImportHelper.setOriginalSource(taxonName, fauEuConfig.getSourceReference(), 
//				fauEuTaxon.getId(), "TaxonName");

		titleCache = buildTaxonTitleCache(concatString, fauEuConfig.getSourceReference());
		taxonBase.setTitleCache(titleCache);
			
		if (logger.isDebugEnabled()) { 
			logger.debug("Name stored: " + concatString); 
		}
		return success;
	}

	
	protected boolean saveTaxa(FaunaEuropaeaImportState state, Map<Integer, TaxonBase<?>> taxonMap) {

		boolean success = true;

//		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
//		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		
		if(logger.isDebugEnabled()) { logger.debug("Saving taxa ..."); }


		getTaxonService().saveTaxonAll(taxonMap.values());


		return success;
	}
	

}
