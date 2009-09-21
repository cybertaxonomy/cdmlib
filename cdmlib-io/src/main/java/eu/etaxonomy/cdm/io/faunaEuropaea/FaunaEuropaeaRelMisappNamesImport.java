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
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.babadshanjan
 * @created 12.05.2009
 * @version 1.0
 */
@Component
public class FaunaEuropaeaRelMisappNamesImport extends FaunaEuropaeaImportBase  {
	
	public static final String OS_NAMESPACE_TAXON = "Taxon";
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaRelMisappNamesImport.class);

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
	
	protected boolean doInvoke(FaunaEuropaeaImportState state) {				
		
		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		taxonStore.makeEmpty();
		MapWrapper<TeamOrPersonBase> authorStore = (MapWrapper<TeamOrPersonBase>)stores.get(ICdmIO.TEAM_STORE);
		authorStore.makeEmpty();
		
		boolean success = true;
		
		if(logger.isInfoEnabled()) { logger.info("Start making taxa..."); }
		
		TransactionStatus txStatus = startTransaction();

		success = retrieveChildParentUuidMap(state);
		success = createRelationships(state);
		
		commitTransaction(txStatus);

		logger.info("End making taxa...");
		return success;
	}

	
	/** Retrieve child-parent uuid map from CDM DB */
	private boolean retrieveChildParentUuidMap(FaunaEuropaeaImportState state) {

		Map<UUID, UUID> childParentMap = state.getChildParentMap();
		Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
		MapWrapper<TaxonBase> taxonStore = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		FaunaEuropaeaImportConfigurator fauEuConfig = state.getConfig();
		ReferenceBase<?> sourceRef = fauEuConfig.getSourceReference();
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


	/** Creates parent-child relationships.
	 * Parent-child pairs are retrieved in blocks via findByUUID(Set<UUID>) from CDM DB 
	 * This takes about 2min for a block of 5000.*/
	private boolean createRelationships(FaunaEuropaeaImportState state) {

		Map<UUID, UUID> childParentUuidMap = state.getChildParentMap();
		ReferenceBase<?> sourceRef = state.getConfig().getSourceReference();

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

			Map<UUID, UUID> childParentPartUuidMap = partMap(limit, childParentUuidMap);
			Set<TaxonBase> childSet = new HashSet<TaxonBase>(limit);
			
			Set<UUID> childKeysSet = childParentPartUuidMap.keySet();
			Set<UUID> parentValuesSet = new HashSet<UUID>(childParentPartUuidMap.values());
			
			List<TaxonBase> children = getTaxonService().findByUuid(childKeysSet);
			List<TaxonBase> parents = getTaxonService().findByUuid(parentValuesSet);
			
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
					
					for (TaxonBase potentialParent : parents ) {
						parentUuid = potentialParent.getUuid();
						if(parentUuid.equals(mappedParentUuid)) {
							parent = potentialParent;
							if (logger.isDebugEnabled()) {
								logger.debug("Parent (" + parentUuid + ") found for child (" + childUuid + ")");
							}
							break;
						}
					}
					
					Taxon parentTaxon = parent.deproxy(parent, Taxon.class);
					
					if (childTaxon != null && parentTaxon != null) {
						
						makeMisappliedName(parentTaxon, childTaxon, sourceRef, null);
						
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
			getTaxonService().saveTaxonAll(childSet);
			commitTransaction(txStatus);
			parentValuesSet = null;
			childSet = null;
			childParentPartUuidMap = null;
			children = null;
			parents = null;
		}
		return success;
	}

	
	private void makeMisappliedName(Taxon toTaxon, Taxon fromTaxon, 
			ReferenceBase citation, String microCitation) {
		try {
			fromTaxon.addMisappliedName(toTaxon, citation, microCitation);
			if (logger.isInfoEnabled()) {
				logger.info("Misapplied name created from taxon " + fromTaxon.getUuid() 
						+ " to " + toTaxon.getUuid());
			}
		} catch (Exception e) {
			logger.error("Error creating misapplied name relationship from taxon " 
					+ fromTaxon.getUuid() + " to " + toTaxon.getUuid());
		}
	}
	
}
