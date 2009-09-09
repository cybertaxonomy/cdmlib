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
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.T_STATUS_ACCEPTED;
import static eu.etaxonomy.cdm.io.faunaEuropaea.FaunaEuropaeaTransformer.T_STATUS_NOT_ACCEPTED;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
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
	
	protected boolean doInvoke(FaunaEuropaeaImportState state) {				
		
		boolean success = true;

		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		taxonStore.makeEmpty();
		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
		authorStore.makeEmpty();

		if(logger.isInfoEnabled()) { logger.info("Start making taxonomically included relationships..."); }

		//ReferenceBase<?> sourceRef = state.getConfig().getSourceReference();
		TransactionStatus txStatus = startTransaction();
		
		TaxonBase taxon = getTaxonService().getTaxonByUuid(UUID.fromString("ac7b30dc-6207-4c71-9752-ee0fb838a271"));
		ReferenceBase<?> sourceRef = taxon.getSec();
		TaxonomicTree tree= getTaxonomicTreeFor(state, sourceRef);

		commitTransaction(txStatus);
		
		ProfilerController.memorySnapshot();		
		success = processParentsChildren(state);
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

		try {

			String strQuery = 
				" SELECT dbo.Taxon.UUID AS ChildUuid, Parent.UUID AS ParentUuid " +
				" FROM dbo.Taxon INNER JOIN dbo.Taxon AS Parent " +
				" ON dbo.Taxon.TAX_TAX_IDPARENT = Parent.TAX_ID " +
				" WHERE (dbo.Taxon.TAX_VALID <> 0) AND (dbo.Taxon.TAX_AUT_ID <> " + A_AUCT + " OR dbo.Taxon.TAX_AUT_ID IS NULL )" +
				" ORDER BY dbo.Taxon.TAX_RNK_ID ASC";

			if (logger.isInfoEnabled()) {
				logger.info("Query: " + strQuery);
			}

			ResultSet rs = source.getResultSet(strQuery);
			
			while (rs.next()) {
				
				if ((i++ % limit) == 0) {
					
					txStatus = startTransaction();
					childParentMap = new HashMap<UUID, UUID>(limit);
					
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
				if (((i % limit) == 0 && i != 1 )) { 

					success = createRelationships(state, childParentMap);

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


	/* Creates parent-child relationships.
	 * Parent-child pairs are retrieved in blocks via findByUUID(Set<UUID>) from CDM DB. 
	 * It takes about 5min to save a block of 5000 taxa.*/
	private boolean createRelationships(FaunaEuropaeaImportState state, Map<UUID, UUID> childParentMap) {

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
						
//						makeTaxonomicallyIncluded(state, parentTaxon, childTaxon, sourceRef, null, tree);
//						makeTaxonomicallyIncluded(state, parentTaxon, childTaxon, sourceRef, null);
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

	/**
	 * @param state
	 * @param sourceRef
	 */
	private TaxonomicTree getTaxonomicTreeFor(FaunaEuropaeaImportState state, ReferenceBase<?> sourceRef) {
		
		TaxonomicTree tree;
		UUID treeUuid = state.getTreeUuid(sourceRef);
		if (treeUuid == null){
			if(logger.isInfoEnabled()) { logger.info(".. creating new taxonomic tree"); }
			
			TransactionStatus txStatus = startTransaction();
			tree = makeTreeMemSave(state, sourceRef);
			commitTransaction(txStatus);
			
		} else {
			tree = getTaxonService().getTaxonomicTreeByUuid(treeUuid);
		}
		return tree;
	}
	
}
