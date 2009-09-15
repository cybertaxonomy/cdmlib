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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.profiler.ProfilerController;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;



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
	/* Highest taxon index in the FauEu database */
	private int highestTaxonIndex = 0;
	/* Number of times method buildParentName() has been called for one taxon */
	private int callCount = 0;
	//private Map<Integer, FaunaEuropaeaTaxon> fauEuTaxonMap = new HashMap();



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
		return ! (state.getConfig().isDoTaxonomicallyIncluded() || 
		state.getConfig().isDoMisappliedNames() || state.getConfig().isDoHeterotypicSynonyms());
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
	
	protected boolean doInvoke(FaunaEuropaeaImportState state) {				
		
		boolean success = true;

		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
//		taxonStore.makeEmpty();
		taxonStore = null;
		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
		authorStore.makeEmpty();

		if(logger.isInfoEnabled()) { logger.info("Start making taxonomically included relationships..."); }

		//ReferenceBase<?> sourceRef = state.getConfig().getSourceReference();
		TransactionStatus txStatus = startTransaction();
		
		TaxonBase taxon = getTaxonService().getTaxonByUuid(UUID.fromString("ac7b30dc-6207-4c71-9752-ee0fb838a271"));
		ReferenceBase<?> sourceRef = taxon.getSec();
		TaxonomicTree tree = getTaxonomicTreeFor(state, sourceRef);

		commitTransaction(txStatus);
		
		ProfilerController.memorySnapshot();
		
		if (state.getConfig().isDoTaxonomicallyIncluded()) {
			success = processParentsChildren(state);
		}
		ProfilerController.memorySnapshot();
		if (state.getConfig().isDoMisappliedNames()) {
			success = processMisappliedNames(state);
		}
		ProfilerController.memorySnapshot();
//		if (state.getConfig().isDoHeterotypicSynonyms()) {
//			success = processHeterotypicSynonyms(state);
//		}
		
		ProfilerController.memorySnapshot();

		logger.info("End making taxa...");

		return success;
	}

	/** Retrieve child-parent uuid map from CDM DB */
	private boolean processParentsChildren(FaunaEuropaeaImportState state) {

		int limit = state.getConfig().getLimitSave();

		TransactionStatus txStatus = null;

		Map<UUID, UUID> childParentMap = null;
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		Source source = fauEuConfig.getSource();
		int i = 0;
		boolean success = true;

		String selectCount = 
			" SELECT count(*) ";

		String selectColumns = " SELECT dbo.Taxon.UUID AS ChildUuid, Parent.UUID AS ParentUuid ";
		
		String fromClause = " FROM dbo.Taxon INNER JOIN dbo.Taxon AS Parent " +
		" ON dbo.Taxon.TAX_TAX_IDPARENT = Parent.TAX_ID " +
		" WHERE (dbo.Taxon.TAX_VALID <> 0) AND (dbo.Taxon.TAX_AUT_ID <> " + A_AUCT + " OR dbo.Taxon.TAX_AUT_ID IS NULL )";
		
		String orderClause = " ORDER BY dbo.Taxon.TAX_RNK_ID ASC";

		String countQuery = 
			selectCount + fromClause;

		String selectQuery = 
			selectColumns + fromClause + orderClause;
			
		try {

//			String strQuery = 
//				" SELECT dbo.Taxon.UUID AS ChildUuid, Parent.UUID AS ParentUuid " +
//				" FROM dbo.Taxon INNER JOIN dbo.Taxon AS Parent " +
//				" ON dbo.Taxon.TAX_TAX_IDPARENT = Parent.TAX_ID " +
//				" WHERE (dbo.Taxon.TAX_VALID <> 0) AND (dbo.Taxon.TAX_AUT_ID <> " + A_AUCT + " OR dbo.Taxon.TAX_AUT_ID IS NULL )" +
//				" ORDER BY dbo.Taxon.TAX_RNK_ID ASC";

			ResultSet rs = source.getResultSet(countQuery);
			rs.next();
			int count = rs.getInt(1);
			
			rs = source.getResultSet(selectQuery);

	        if (logger.isInfoEnabled()) {
				logger.info("Number of rows: " + count);
				logger.info("Count Query: " + countQuery);
				logger.info("Select Query: " + selectQuery);
			}

	        while (rs.next()) {
				
				if ((i++ % limit) == 0) {
					
					txStatus = startTransaction();
					childParentMap = new HashMap<UUID, UUID>(limit);
					
					if(logger.isInfoEnabled()) {
						logger.info("Taxonomically included retrieved: " + (i-1)); 
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
				if (((i % limit) == 0 && i != 1 ) || i == count) { 

					success = createParentChildRelationships(state, childParentMap);

					childParentMap = null;
					commitTransaction(txStatus);

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

	
	/** Retrieve misapplied name / accepted taxon uuid map from CDM DB */
	private boolean processMisappliedNames(FaunaEuropaeaImportState state) {

		int limit = state.getConfig().getLimitSave();

		TransactionStatus txStatus = null;

		Map<UUID, UUID> childParentMap = null;
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		Source source = fauEuConfig.getSource();
		int i = 0;
		boolean success = true;

		String selectCount = 
			" SELECT count(*) ";

		String selectColumns = " SELECT Taxon.UUID AS MisappliedUuid, Parent.UUID AS AcceptedUuid ";
		
		String fromClause = " FROM Taxon INNER JOIN Taxon AS Parent " +
		" ON Taxon.TAX_TAX_IDPARENT = Parent.TAX_ID " +
		" WHERE (Taxon.TAX_VALID = 0) AND (Taxon.TAX_AUT_ID = " + A_AUCT + ")";
		
		String orderClause = " ORDER BY dbo.Taxon.TAX_RNK_ID ASC ";

		String countQuery = 
			selectCount + fromClause;

		String selectQuery = 
			selectColumns + fromClause + orderClause;
			
		try {

			ResultSet rs = source.getResultSet(countQuery);
			rs.next();
			int count = rs.getInt(1);
			
			rs = source.getResultSet(selectQuery);

	        if (logger.isInfoEnabled()) {
				logger.info("Number of rows: " + count);
				logger.info("Count Query: " + countQuery);
				logger.info("Select Query: " + selectQuery);
			}

			while (rs.next()) {
				
				if ((i++ % limit) == 0) {
					
					txStatus = startTransaction();
					childParentMap = new HashMap<UUID, UUID>(limit);
					
					if(logger.isInfoEnabled()) {
						logger.info("Misapplied names retrieved: " + (i-1) ); 
					}
				}

				String childUuidStr = rs.getString("MisappliedUuid");
				String parentUuidStr = rs.getString("AcceptedUuid");
				UUID childUuid = UUID.fromString(childUuidStr);
				UUID parentUuid = UUID.fromString(parentUuidStr);
				
				if (!childParentMap.containsKey(childUuid)) {

						childParentMap.put(childUuid, parentUuid);

				} else {
					if(logger.isDebugEnabled()) {
						logger.debug("Duplicated child UUID (" + childUuid + ")");
					}
				}

				if (((i % limit) == 0 && i != 1 ) || i == count) { 

					success = createMisappliedNameRelationships(state, childParentMap);

					childParentMap = null;
					commitTransaction(txStatus);

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

	
	/* Creates parent-child relationships.
	 * Parent-child pairs are retrieved in blocks via findByUUID(Set<UUID>) from CDM DB. 
	 */
	private boolean createParentChildRelationships(FaunaEuropaeaImportState state, Map<UUID, UUID> childParentMap) {

		TaxonBase taxon = getTaxonService().getTaxonByUuid(UUID.fromString("ac7b30dc-6207-4c71-9752-ee0fb838a271"));
		ReferenceBase<?> sourceRef = taxon.getSec();
		boolean success = true;
		
			TaxonomicTree tree = getTaxonomicTreeFor(state, sourceRef);
			
			Set<TaxonBase> childSet = new HashSet<TaxonBase>(limit);
			
			Set<UUID> childKeysSet = childParentMap.keySet();
			Set<UUID> parentValuesSet = new HashSet<UUID>(childParentMap.values());
			
			if (logger.isInfoEnabled()) {
				logger.info("Start reading children and parents");
			}
			List<TaxonBase> children = getTaxonService().findByUuid(childKeysSet);
			List<TaxonBase> parents = getTaxonService().findByUuid(parentValuesSet);
			Map<UUID, TaxonBase> parentsMap = new HashMap<UUID, TaxonBase>(parents.size());
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
			UUID childUuid = null;

			for (TaxonBase child : children) {

				try {
					Taxon childTaxon = child.deproxy(child, Taxon.class);
					childUuid = childTaxon.getUuid();
					mappedParentUuid = childParentMap.get(childUuid);
					TaxonBase parent = null;
					
					TaxonBase potentialParent = parentsMap.get(mappedParentUuid);
//					for (TaxonBase potentialParent : parents ) {
//						parentUuid = potentialParent.getUuid();
//						if(parentUuid.equals(mappedParentUuid)) {
							parent = potentialParent;
							if (logger.isDebugEnabled()) {
								logger.debug("Parent (" + mappedParentUuid + ") found for child (" + childUuid + ")");
							}
//							break;
//						}
//					}
					
					Taxon parentTaxon = parent.deproxy(parent, Taxon.class);
					
					if (childTaxon != null && parentTaxon != null) {
						
						tree.addParentChild(parentTaxon, childTaxon, sourceRef, null);
						
						if (logger.isDebugEnabled()) {
							logger.debug("Parent-child (" + mappedParentUuid + "-" + childUuid + 
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
							logger.debug("Parent(" + mappedParentUuid + ") or child (" + childUuid + " is null");
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
						mappedParentUuid + "-" + childUuid + ")", e);
				}

			}
			if (logger.isInfoEnabled()) {
				logger.info("Start saving childSet");
			}
			getTaxonService().saveTaxonAll(childSet);
			if (logger.isInfoEnabled()) {
				logger.info("End saving childSet");
			}

			parentValuesSet = null;
			childSet = null;
			children = null;
			parents = null;
			tree = null;
		
		return success;
	}

	/* Creates parent-child relationships.
	 * Parent-child pairs are retrieved in blocks via findByUUID(Set<UUID>) from CDM DB. 
	 */
	private boolean createMisappliedNameRelationships(FaunaEuropaeaImportState state, Map<UUID, UUID> fromToMap) {

		TaxonBase taxon = getTaxonService().getTaxonByUuid(UUID.fromString("ac7b30dc-6207-4c71-9752-ee0fb838a271"));
		ReferenceBase<?> sourceRef = taxon.getSec();
		boolean success = true;
		
			TaxonomicTree tree = getTaxonomicTreeFor(state, sourceRef);
			
			Set<TaxonBase> misappliedNameSet = new HashSet<TaxonBase>(limit);
			
			Set<UUID> misappliedNamesSet = fromToMap.keySet();
			Set<UUID> acceptedTaxaSet = new HashSet<UUID>(fromToMap.values());
			
			if (logger.isInfoEnabled()) {
				logger.info("Start reading misapplied names and accepted taxa");
			}
			List<TaxonBase> misappliedNames = getTaxonService().findByUuid(misappliedNamesSet);
			List<TaxonBase> acceptedTaxa = getTaxonService().findByUuid(acceptedTaxaSet);
			Map<UUID, TaxonBase> acceptedTaxaMap = new HashMap<UUID, TaxonBase>(acceptedTaxa.size());
			for (TaxonBase taxonBase : acceptedTaxa){
				acceptedTaxaMap.put(taxonBase.getUuid(), taxonBase);
			}
			
			
			if (logger.isInfoEnabled()) {
				logger.info("End reading misapplied names and accepted taxa");
			}
			
			
			if (logger.isTraceEnabled()) {
				for (UUID uuid : misappliedNamesSet) {
					logger.trace("misapplied name uuid query: " + uuid);
				}
			}
			if (logger.isTraceEnabled()) {
				for (UUID uuid : acceptedTaxaSet) {
					logger.trace("accepted taxon uuid query: " + uuid);
				}
			}
			if (logger.isTraceEnabled()) {
				for (TaxonBase tb : misappliedNames) {
					logger.trace("misapplied name uuid result: " + tb.getUuid());
				}
			}
			if (logger.isTraceEnabled()) {
				for (TaxonBase tb : acceptedTaxa) {
					logger.trace("accepted taxon uuid result: " + tb.getUuid());
				}
			}

			UUID mappedAcceptedTaxonUuid = null;
			UUID misappliedNameUuid = null;
			Taxon misappliedNameTaxon = null;
			TaxonBase acceptedTaxonBase = null;
			Taxon acceptedTaxon = null;

			for (TaxonBase misappliedName : misappliedNames) {

				try {
					misappliedNameTaxon = misappliedName.deproxy(misappliedName, Taxon.class);
					misappliedNameUuid = misappliedNameTaxon.getUuid();
					mappedAcceptedTaxonUuid = fromToMap.get(misappliedNameUuid);
					acceptedTaxonBase = null;
					
					acceptedTaxonBase = acceptedTaxaMap.get(mappedAcceptedTaxonUuid);
							if (logger.isDebugEnabled()) {
								logger.debug("Parent (" + mappedAcceptedTaxonUuid + ") found for child (" + misappliedNameUuid + ")");
							}
					
							acceptedTaxon = acceptedTaxonBase.deproxy(acceptedTaxonBase, Taxon.class);
					
					if (misappliedNameTaxon != null && acceptedTaxon != null) {
						
						acceptedTaxon.addMisappliedName(misappliedNameTaxon, sourceRef, null);
					
						if (logger.isDebugEnabled()) {
							logger.debug("Accepted taxon / misapplied name (" + mappedAcceptedTaxonUuid + "-" + misappliedNameUuid + 
							") relationship created");
						}
						if (!misappliedNameSet.contains(misappliedNameTaxon)) {
							
							misappliedNameSet.add(misappliedNameTaxon);
							
							if (logger.isTraceEnabled()) {
								logger.trace("Misapplied name taxon (" + misappliedNameUuid + ") added to Set");
							}
							
						} else {
							if (logger.isDebugEnabled()) {
								logger.debug("Duplicated misapplied name taxon (" + misappliedNameUuid + ")");
							}
						}
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("Accepted taxon (" + mappedAcceptedTaxonUuid + ") or misapplied name (" + misappliedNameUuid + " is null");
						}
					}
					
					if (misappliedNameTaxon != null && !misappliedNameSet.contains(misappliedNameTaxon)) {
						misappliedNameSet.add(misappliedNameTaxon);
						if (logger.isTraceEnabled()) {
							logger.trace("Misapplied name taxon (" + misappliedNameUuid + ") added to Set");
						}
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("Duplicated misapplied name taxon (" + misappliedNameUuid + ")");
						}
					}
					
				} catch (Exception e) {
					logger.error("Error creating misapplied name relationship accepted taxon-misapplied name (" + 
						mappedAcceptedTaxonUuid + "-" + misappliedNameUuid + ")", e);
				}

			}
			if (logger.isInfoEnabled()) {
				logger.info("Start saving misappliedNameSet");
			}
			getTaxonService().saveTaxonAll(misappliedNameSet);
			if (logger.isInfoEnabled()) {
				logger.info("End saving misappliedNameSet");
			}

			acceptedTaxaSet = null;
			misappliedNameSet = null;
			misappliedNames = null;
			acceptedTaxa = null;
			tree = null;
		
		return success;
	}
}
