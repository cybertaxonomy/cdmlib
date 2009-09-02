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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.IService;
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
import eu.etaxonomy.cdm.io.profiler.ProfilerController;
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

import com.yourkit.api.Controller;



/**
 * @author a.babadshanjan
 * @created 12.05.2009
 * @version 1.0
 */
@Component
public class FaunaEuropaeaRelTaxonIncludeImport extends FaunaEuropaeaImportBase  {
	
	public static final String OS_NAMESPACE_TAXON = "Taxon";
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaRelTaxonIncludeImport.class);

	/* Max number of taxa to retrieve (for test purposes) */
	private int maxTaxa = 0;
	/* Max number of taxa to be saved in CDM DB with one service call */
	private int limit = 5000; // TODO: Make configurable
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
	protected boolean doInvokeAlter(FaunaEuropaeaImportState state) {				
		
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making taxa..."); }
		
//		TransactionStatus txStatus = startTransaction();
		
		success = retrieveTaxa(state, fauEuTaxonMap, Q_NO_RESTRICTION);
		success = processTaxaFromDatabase(state, fauEuTaxonMap);
		
//		commitTransaction(txStatus);
		
		logger.info("End making taxa...");
		return success;
	}

	
	protected boolean doInvoke(FaunaEuropaeaImportState state) {				
		
		boolean success = true;
		

			ProfilerController.memorySnapshot();
		
			Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
			MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
			taxonStore.makeEmpty();
			MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
			authorStore.makeEmpty();
			
			if(logger.isInfoEnabled()) { logger.info("Start making taxonomically included relationships..."); }
			
	//		TransactionStatus txStatus = startTransaction();
	
			success = retrieveChildParentUuidMap(state);
			ProfilerController.memorySnapshot();
			success = createRelationships(state);
			
	//		commitTransaction(txStatus);
	
			logger.info("End making taxa...");
			ProfilerController.memorySnapshot();

		return success;
	}

	/** Retrieve child-parent uuid map from CDM DB */
	private boolean retrieveChildParentUuidMap(FaunaEuropaeaImportState state) {

		Map<UUID, UUID> childParentMap = state.getChildParentMap();
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		Source source = fauEuConfig.getSource();
		int i = 0;
		boolean success = true;

		try {

			String strQuery = 
				" SELECT dbo.Taxon.UUID AS ChildUuid, Parent.UUID AS ParentUuid " +
				" FROM dbo.Taxon INNER JOIN dbo.Taxon AS Parent " +
				" ON dbo.Taxon.TAX_TAX_IDPARENT = Parent.TAX_ID " +
				" WHERE (dbo.Taxon.TAX_VALID <> 0) AND (dbo.Taxon.TAX_AUT_ID <> " + A_AUCT + ")";

			if (logger.isInfoEnabled()) {
				logger.info("Query: " + strQuery);
			}

			ResultSet rs = source.getResultSet(strQuery);
			
			while (rs.next()) {
				
				if ((i++ % modCount) == 0 && i != 1 ) { 
					if(logger.isInfoEnabled()) {
						logger.info("Parent-child mappings retrieved: " + (i-1)); 
					}
				}

				String childUuidStr = rs.getString("ChildUuid");
				String parentUuidStr = rs.getString("ParentUuid");
				UUID childUuid = UUID.fromString(childUuidStr);
				UUID parentUuid = UUID.fromString(parentUuidStr);
				
				if (!childParentMap.containsKey(childUuid)) {

						childParentMap.put(childUuid, parentUuid);

				} else {
					if(logger.isDebugEnabled()) {
						logger.debug("Duplicated child UUID (" + childUuid + ")");
					}
				}
			}

		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			success = false;
		}
		return success;		
	}

	
	/** Retrieve taxa from FauEu DB and build FauEuTaxonMap only */
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

	
	public Map<UUID, UUID> partMap(int border, Map<UUID, UUID> map) {

		if (logger.isInfoEnabled()) {
			logger.info("Map size: " + map.size());
		}
		Set<Map.Entry<UUID, UUID>> entries = map.entrySet();
		Iterator<Map.Entry<UUID, UUID>> entryIter = entries.iterator();
		Map<UUID, UUID> partMap = new HashMap<UUID, UUID>();

		for (int i = 0; i < border; i++) {
			//while (entryIter.hasNext()) {

			Map.Entry<UUID, UUID> mapEntry = (Map.Entry<UUID, UUID>)entryIter.next();
			partMap.put(mapEntry.getKey(), mapEntry.getValue());
			entryIter.remove();
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Map size: " + map.size());
		}
		return partMap;
	}		

//	public Map<UUID, UUID> childParentMap partMap(int start, int limit, Map<UUID, UUID> childParentMap) {
//		
//		int index = 0;
//		
//		for (int i = 0; i < limit; i++) {
//			
//			int j = start + i;
//			
//			Object object = childParentMap.get(j);
//			if(object != null) {
//				childParentMap.put(index, childParentMap.get(j));
//				index++;
//			} else {
//				if (logger.isDebugEnabled()) { logger.debug("Object (" + j + ") is null"); }
//			}
//		}
//		return (Map<UUID, UUID> childParentMap)internalPartMap.values();
//	}

	
	/** Creates parent-child relationships.
	 * Single Parent-child pairs are retrieved via findByUUID(UUID) from CDM DB 
	 * This takes inacceptable long time. */
	private boolean createRelationships_(FaunaEuropaeaImportState state) {

		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		taxonStore.makeEmpty();
		Map<UUID, UUID> childParentMap = state.getChildParentMap();
		ReferenceBase<?> sourceRef = state.getConfig().getSourceReference();

		int upperBorder = childParentMap.size();
		int nbrOfBlocks = 0;

		boolean success = true;

		if (upperBorder < limit) {             // TODO: test with critical values
			limit = upperBorder;
		} else {
			nbrOfBlocks = upperBorder / limit;
		}

		if(logger.isInfoEnabled()) { 
			logger.info("number of child-parent pairs = " + upperBorder 
					+ ", limit = " + limit
					+ ", number of blocks = " + nbrOfBlocks); 
		}

		for (int j = 1; j <= nbrOfBlocks + 1; j++) {
			int offset = j - 1;
			int start = offset * limit;

			if(logger.isInfoEnabled()) { logger.info("Processing child-parent pairs: " + start + " - " + (start + limit - 1)); }

			if(logger.isInfoEnabled()) { 
				logger.info("index = " + j 
						+ ", offset = " + offset
						+ ", start = " + start); 
			}

			if (j == nbrOfBlocks + 1) {
				limit = upperBorder - nbrOfBlocks * limit;
				if(logger.isInfoEnabled()) { logger.info("number of blocks = " + nbrOfBlocks + " limit = " + limit); }
			}

			TransactionStatus txStatus = startTransaction();
			
			
//			for (int k = 1; k <= start + offset; k++) {       // TODO: test borders
//			int k = 0;

			Map<UUID, UUID> childParentPartMap = partMap(limit, childParentMap);
			Set<TaxonBase> childSet = new HashSet<TaxonBase>(limit);
			
			if (logger.isInfoEnabled()) {
				logger.info("Partmap size: " + childParentPartMap.size());
			}

			for (UUID childUuid : childParentPartMap.keySet()) {
//			for (UUID childUuid : childParentMap.keySet()) {

				UUID parentUuid = childParentPartMap.get(childUuid);

				try {
					TaxonBase<?> parent = getTaxonService().findByUuid(parentUuid);
					if (logger.isTraceEnabled()) {
						logger.trace("Parent find called (" + parentUuid + ")");
					}
					TaxonBase<?> child = getTaxonService().findByUuid(childUuid);
					if (logger.isTraceEnabled()) {
						logger.trace("Child find called (" + childUuid + ")");
					}
					Taxon parentTaxon = parent.deproxy(parent, Taxon.class);
					Taxon childTaxon = child.deproxy(child, Taxon.class);

					if (childTaxon != null && parentTaxon != null) {
						
//						makeTaxonomicallyIncluded(state, parentTaxon, childTaxon, sourceRef, null);
						
						if (logger.isDebugEnabled()) {
							logger.debug("Parent-child (" + parentUuid + "-" + childUuid + 
							") relationship created");
						}
						if (!childSet.contains(childTaxon)) {
							
							childSet.add(childTaxon);
							
							if (logger.isTraceEnabled()) {
								logger.trace("Child taxon (" + childUuid + ") added to Set");
							}
							
						} else {
							if (logger.isDebugEnabled()) {
								logger.debug("Duplicated child taxon (" + childUuid + ")");
							}
						}
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("Parent(" + parentUuid + ") or child (" + childUuid + " is null");
						}
					}
					
//					if (childTaxon != null && !childSet.contains(childTaxon)) {
//						childSet.add(childTaxon);
//						if (logger.isDebugEnabled()) {
//							logger.debug("Child taxon (" + childUuid + ") added to Set");
//						}
//					} else {
//						if (logger.isDebugEnabled()) {
//							logger.debug("Duplicated child taxon (" + childUuid + ")");
//						}
//					}
					
				} catch (Exception e) {
					logger.error("Error creating taxonomically included relationship parent-child (" + 
							parentUuid + "-" + childUuid + ")");
				}

			}
			getTaxonService().saveTaxonAll(childSet);
			commitTransaction(txStatus);
		}
		return success;
	}
			
			
	/* Creates parent-child relationships.
	 * Parent-child pairs are retrieved in blocks via findByUUID(Set<UUID>) from CDM DB. 
	 * It takes about 5min to save a block of 5000 taxa.*/
	private boolean createRelationships(FaunaEuropaeaImportState state) {

		Map<UUID, UUID> childParentUuidMap = state.getChildParentMap();
		ReferenceBase<?> sourceRef = state.getConfig().getSourceReference();
//		UUID treeUuid = state.getTree(sourceRef).getUuid();
//		TaxonomicTree tree = getTaxonService().getTaxonomicTreeByUuid(treeUuid);
//		TaxonomicTree tree = state.getTree(sourceRef);

		int upperBorder = childParentUuidMap.size();
		int nbrOfBlocks = 0;

		boolean success = true;

		if (upperBorder < limit) {             // TODO: test with critical values
			limit = upperBorder;
		} else {
			nbrOfBlocks = upperBorder / limit;
		}

		if(logger.isInfoEnabled()) { 
			logger.info("number of child-parent pairs = " + upperBorder 
					+ ", limit = " + limit
					+ ", number of blocks = " + nbrOfBlocks); 
		}

		for (int j = 1; j <= nbrOfBlocks + 1; j++) {
			int offset = j - 1;
			int start = offset * limit;

			if(logger.isInfoEnabled()) { logger.info("Processing child-parent pairs: " + start + " - " + (start + limit - 1)); }

			if(logger.isInfoEnabled()) { 
				logger.info("index = " + j 
						+ ", offset = " + offset
						+ ", start = " + start); 
			}

			if (j == nbrOfBlocks + 1) {
				limit = upperBorder - nbrOfBlocks * limit;
				if(logger.isInfoEnabled()) { logger.info("number of blocks = " + nbrOfBlocks + " limit = " + limit); }
			}

			TransactionStatus txStatus = startTransaction();
			//add tree to new session
			TaxonomicTree tree = state.getTree(sourceRef);
			if (tree == null){
				tree = makeTree(state, sourceRef);
			}
			getTaxonService().saveTaxonomicTree(tree);
			
			Map<UUID, UUID> childParentPartUuidMap = partMap(limit, childParentUuidMap);
			Set<TaxonBase> childSet = new HashSet<TaxonBase>(limit);
			
			Set<UUID> childKeysSet = childParentPartUuidMap.keySet();
			Set<UUID> parentValuesSet = new HashSet<UUID>(childParentPartUuidMap.values());
			
			if (logger.isInfoEnabled()) {
				logger.info("Start reading children and parents");
			}
			List<TaxonBase> children = getTaxonService().findByUuid(childKeysSet);
			List<TaxonBase> parents = getTaxonService().findByUuid(parentValuesSet);
			Map<UUID, TaxonBase> parentsMap = new HashMap<UUID, TaxonBase>();
			for (TaxonBase taxonBase : parents){
				parentsMap.put(taxonBase.getUuid(), taxonBase);
			}
			
			
			if (logger.isInfoEnabled()) {
				logger.info("End reading children and parents");
			}
			
			
			if (logger.isTraceEnabled()) {
				for (UUID uuid : childKeysSet) {
					logger.trace("child uuid query: " + uuid);
				}
			}
			if (logger.isTraceEnabled()) {
				for (UUID uuid : parentValuesSet) {
					logger.trace("parent uuid query: " + uuid);
				}
			}
			if (logger.isTraceEnabled()) {
				for (TaxonBase tb : children) {
					logger.trace("child uuid result: " + tb.getUuid());
				}
			}
			if (logger.isTraceEnabled()) {
				for (TaxonBase tb : parents) {
					logger.trace("parent uuid result: " + tb.getUuid());
				}
			}

			UUID mappedParentUuid = null;
			UUID parentUuid = null;
			UUID childUuid = null;

			for (TaxonBase child : children) {

				try {
					Taxon childTaxon = child.deproxy(child, Taxon.class);
					childUuid = childTaxon.getUuid();
					mappedParentUuid = childParentPartUuidMap.get(childUuid);
					TaxonBase parent = null;
					
					TaxonBase potentialParent = parentsMap.get(mappedParentUuid);
//					for (TaxonBase potentialParent : parents ) {
//						parentUuid = potentialParent.getUuid();
//						if(parentUuid.equals(mappedParentUuid)) {
							parent = potentialParent;
							if (logger.isDebugEnabled()) {
								logger.debug("Parent (" + parentUuid + ") found for child (" + childUuid + ")");
							}
//							break;
//						}
//					}
					
					Taxon parentTaxon = parent.deproxy(parent, Taxon.class);
					
					if (childTaxon != null && parentTaxon != null) {
						
//						makeTaxonomicallyIncluded(state, parentTaxon, childTaxon, sourceRef, null, tree);
						makeTaxonomicallyIncluded(state, parentTaxon, childTaxon, sourceRef, null);
						
						if (logger.isDebugEnabled()) {
							logger.debug("Parent-child (" + parentUuid + "-" + childUuid + 
							") relationship created");
						}
						if (!childSet.contains(childTaxon)) {
							
							childSet.add(childTaxon);
							
							if (logger.isTraceEnabled()) {
								logger.trace("Child taxon (" + childUuid + ") added to Set");
							}
							
						} else {
							if (logger.isDebugEnabled()) {
								logger.debug("Duplicated child taxon (" + childUuid + ")");
							}
						}
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("Parent(" + parentUuid + ") or child (" + childUuid + " is null");
						}
					}
					
					if (childTaxon != null && !childSet.contains(childTaxon)) {
						childSet.add(childTaxon);
						if (logger.isDebugEnabled()) {
							logger.debug("Child taxon (" + childUuid + ") added to Set");
						}
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("Duplicated child taxon (" + childUuid + ")");
						}
					}
					
				} catch (Exception e) {
					logger.error("Error creating taxonomically included relationship parent-child (" + 
							parentUuid + "-" + childUuid + ")");
				}

			}
			if (logger.isInfoEnabled()) {
				logger.info("Start saving childSet");
			}
			getTaxonService().saveTaxonAll(childSet);
			if (logger.isInfoEnabled()) {
				logger.info("End saving childSet");
			}
//			getTaxonService().clear();
//			if (logger.isInfoEnabled()) {
//				logger.info("End clearing session");
//			}
			commitTransaction(txStatus);
			if (logger.isInfoEnabled()) {
				logger.info("End commit transaction");
			}
			parentValuesSet = null;
			childSet = null;
			childParentPartUuidMap = null;
			children = null;
			parents = null;
		}
		return success;
	}

	
	/* Creates parent-child relationships.
	 * Taxon bases are retrieved in blocks from CDM DB.
	 * Parent is retrieved from CDM DB via original source id if not found in current block.
	 * In case of blocksize = 20.000 this takes ca. 1-2 hours per block.
	 *  */
	private boolean createRelationships_old(FaunaEuropaeaTaxon fauEuTaxon,
			TaxonBase<?> taxonBase, TaxonNameBase<?,?> taxonName, List<Taxon> taxa,
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
			if (logger.isDebugEnabled()) {
				logger.debug("Parent (" + parentId + ") found in parent taxon store");
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
			if (logger.isDebugEnabled()) {
				logger.debug("Parent (" + parentId + ") retrieved from DB via original source id");
			}
		}
		
		if (!parentTaxonStore.containsId(parentId)) {
			parentTaxonStore.put(parentId, parentTaxonBase);
		}


		
		Taxon parentTaxon = parentTaxonBase.deproxy(parentTaxonBase, Taxon.class);

		boolean success = true;
		
//		if (!fauEuTaxon.isValid()) { // FauEu Synonym

//		} else if (fauEuTaxon.isValid()) { // FauEu Taxon
		
			Taxon taxon = taxonBase.deproxy(taxonBase, Taxon.class);

			try {
				// add this taxon as child to parent
				if (parentTaxon != null) {
//					makeTaxonomicallyIncluded(state, parentTaxon, taxon, sourceRef, null);
					if (logger.isDebugEnabled()) {
						logger.debug("Parent-child (" + parentId + "-" + taxonId + 
						") relationship created");
					}
				}

			} catch (Exception e) {
				logger.error("Error creating taxonomically included relationship Parent-child (" + 
						parentId + "-" + taxonId + ")");
			}
			
			
//		}
		
		return success;
	}
	

	private boolean makeTaxonomicallyIncluded(FaunaEuropaeaImportState state, Taxon toTaxon, Taxon fromTaxon, 
			ReferenceBase citation, String microCitation){
		boolean success = true;
		ReferenceBase sec = toTaxon.getSec();
		sec = CdmBase.deproxy(sec, ReferenceBase.class);
		sec = citation;
		TaxonomicTree tree = state.getTree(sec);
		
		
		
//		Session session = getTaxonService().getSession();
		
//		if (session.contains(sec)) {
//			logger.debug("Sec contained in session. Id = " + sec.getId());
//		} else {
//			logger.info("Sec not contained in session. Id = " + sec.getId());
//			getReferenceService().merge(sec);
//		}
		
		if (tree == null){
			tree = makeTree(state, sec);
		}

//		if (session.contains(tree)) {
//			logger.debug("Taxonomic tree contained in session. Id = " + tree.getId());
//		} else {
//			logger.info("Taxonomic tree not contained in session. Id = " + tree.getId());
//			UUID treeUuid = state.getTree(sec).getUuid();
//			tree = getTaxonService().getTaxonomicTreeByUuid(treeUuid);
//			logger.info("Tree retrieved");
//		}
		
		success = tree.addParentChild(toTaxon, fromTaxon, citation, microCitation);
		return success;
	}

	
//	public int calculateBlockSize(int limit, int upperBorder) {
//
//		int blockSize = 0;
//		
//		if (upperBorder < limit) {
//			limit = upperBorder;
//		} else {
//			blockSize = upperBorder / limit;
//		}
//	}
	
	
	private boolean processTaxaFromDatabase(FaunaEuropaeaImportState state,
			Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap) {

		if(logger.isInfoEnabled()) { logger.info("Processing taxa second pass..."); }

		MapWrapper<TaxonBase> taxonBaseMap = new MapWrapper<TaxonBase>(null);

		int nbrOfTaxa = getTaxonService().count(Taxon.class);
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
    		
			List<Taxon> taxa = getTaxonService().getAllTaxa(limit, start);
			if(logger.isInfoEnabled()) { 
				logger.info(taxa.size() +  " taxa retrieved from CDM DB"); 
			}

			for (TaxonBase taxonBase : taxa) {

				TaxonNameBase<?,?> taxonName = taxonBase.getName();

				FaunaEuropaeaTaxon fauEuTaxon = findFauEuTaxonByOriginalSourceId(taxonBase, fauEuTaxonMap);
				

				if (logger.isDebugEnabled()) { 
					logger.debug("Taxon # " + fauEuTaxon.getId()); 
				}
				//createRelationships(fauEuTaxon, taxonBase, taxonName, taxa, fauEuTaxonMap, state);
			}

			getTaxonService().saveTaxonAll(taxa);
			taxa = null;
			
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

	
}
