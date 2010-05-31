// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.out;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.pesi.out.PesiTaxonExport.Data;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 12.02.2010
 *
 */
public class PesiExportState extends DbExportStateBase<PesiExportConfigurator>{
	private static final Logger logger = Logger.getLogger(PesiExportState.class);
	private static List<Integer> processedTaxonNameList = new ArrayList<Integer>();
	private static List<Integer> processedSourceList = new ArrayList<Integer>();
	private List<Integer> treeIndexList = new ArrayList<Integer>();
	private static final String state_db_table = "tmp_state";
	private static final String processed_taxonname_db_table = "tmp_processed_taxonname";
	private static final String processed_source_db_table = "tmp_processed_source";
	private static final String processed_treeindex_and_kingdomfk_db_table = "tmp_processed_treeindex_kingdomfk";
	private Connection connection;
	private ResultSet resultSet = null;
	private PreparedStatement Select_KingdomId_TreeIndex_NomenclaturalCode_Stmt;
	private PreparedStatement Insert_KingdomId_TreeIndex_NomenclaturalCode_Stmt;
	
	/**
	 * @param config
	 */
	public PesiExportState(PesiExportConfigurator config) {
		super(config);
		
		try {
			// Retrieve treeIndex, kingdomId and nomenclaturalCode from database table
			String sql = "SELECT kingdom_id, tree_index, nomenclatural_code FROM " + processed_treeindex_and_kingdomfk_db_table + " WHERE tree_index like ?";
			Select_KingdomId_TreeIndex_NomenclaturalCode_Stmt = connection.prepareStatement(sql);
			
			// Add TreeIndex, KingdomFk and nomenclaturalCode to database table
			sql = "INSERT INTO " + processed_treeindex_and_kingdomfk_db_table + " VALUES (?, ?, ?)";
			Insert_KingdomId_TreeIndex_NomenclaturalCode_Stmt = connection.prepareStatement(sql);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		createStateTables();
	}
	
	/**
	 * Create the database table that hosts state information.
	 */
	public boolean createStateTables() {
		connection = getConfig().getDestination().getConnection();
		
		String query;
        Statement stmt;
        
        if (deleteStateTables()) {
/*	        try {
	                query="CREATE TABLE " + state_db_table + " (cdm_id varchar(100), pesi_id int)";
	                stmt = connection.createStatement();
	                stmt.executeUpdate(query);
	                stmt.close();
	        } catch (Exception e) {
	        	logger.error("Couldn't create database table for state information.");
	            e.printStackTrace();
	            throw new RuntimeException("Couldn't create database table for state information.");
	        }
	
	        try {
	                query="CREATE TABLE " + processed_taxonname_db_table + " (cdm_id varchar(100))";
	                stmt = connection.createStatement();
	                stmt.executeUpdate(query);
	                stmt.close();
	        } catch (Exception e) {
	        	logger.error("Couldn't create database table for processed taxonnames.");
	            e.printStackTrace();
	            throw new RuntimeException("Couldn't create database table for processed taxonnames.");
	        }*/
	
	        try {
	                query="CREATE TABLE " + processed_source_db_table + " (cdm_id int)";
	                stmt = connection.createStatement();
	                stmt.executeUpdate(query);
	                stmt.close();
	        } catch (Exception e) {
	        	logger.error("Couldn't create database table for processed sources.");
	            e.printStackTrace();
	            throw new RuntimeException("Couldn't create database table for processed sources.");
	        }

	        try {
                query="CREATE TABLE " + processed_treeindex_and_kingdomfk_db_table + " (kingdom_id int, tree_index varchar(200), nomenclatural_code varchar(100))";
                stmt = connection.createStatement();
                stmt.executeUpdate(query);
                stmt.close();
	        } catch (Exception e) {
	        	logger.error("Couldn't create database table for processed treeindex and kingdomfk.");
	            e.printStackTrace();
	            throw new RuntimeException("Couldn't create database table for processed treeindex and kingdomfk.");
	        }
        }

        return true;
	}

	/**
	 * Deletes existing state tables.
	 */
	public boolean deleteStateTables() {
		boolean result = true;
		
		String query;
        Statement stmt;
        
/*        try {
                query="DROP TABLE " + state_db_table;
                stmt = connection.createStatement();
                stmt.executeUpdate(query);
                stmt.close();
        } catch (Exception e) {
        	logger.error("Couldn't drop database table for state information.");
//            result = false;
        }

        try {
                query="DROP TABLE " + processed_taxonname_db_table;
                stmt = connection.createStatement();
                stmt.executeUpdate(query);
                stmt.close();
        } catch (Exception e) {
        	logger.error("Couldn't drop database table for processed taxonnames.");
//            result = false;
        }*/

        try {
                query="DROP TABLE " + processed_source_db_table;
                stmt = connection.createStatement();
                stmt.executeUpdate(query);
                stmt.close();
        } catch (Exception e) {
        	logger.error("Couldn't drop database table for processed sources.");
//            result = false;
        }

        try {
            query="DROP TABLE " + processed_treeindex_and_kingdomfk_db_table;
            stmt = connection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
    } catch (Exception e) {
    	logger.error("Couldn't drop database table for treeindex and kingdomfk.");
//        result = false;
    }

        return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.DbExportStateBase#putDbId(eu.etaxonomy.cdm.model.common.CdmBase, int)
	 */
//	@Override
//	public void putDbId(CdmBase cdmBase, int dbId) {
//		String sql;
//		Source destination =  getConfig().getDestination();
//
//		// Add TaxonName to table of processed TaxonNames
//		sql = "INSERT INTO " + state_db_table + " VALUES ('" + cdmBase.getUuid() + "', " + dbId + ")";
//		destination.setQuery(sql);
//		destination.update(sql);
//	}

	
//	@Override
//	public Integer getDbId(CdmBase cdmBase) {
//		if (cdmBase != null) {
//			IdType type = getConfig().getIdType();
//			if (type == IdType.CDM_ID) {
//				return cdmBase.getId();
//			} else {
//				return dbIdMap.get(cdmBase.getUuid());
//			}
//		} else {
//			logger.warn("CdmBase was (null). No entries in dbIdMap available");
//			return null;
//		}
//	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.DbExportStateBase#getDbId(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
//	@Override
//	public Integer getDbId(CdmBase cdmBase) {
//		String sql;
//		Source destination =  getConfig().getDestination();
//
//		// Get datawarehouse database identifier from table for state information
//		sql = "SELECT pesi_id FROM " + state_db_table + " WHERE cdm_id = '" + cdmBase.getUuid() + "'";
//		destination.setQuery(sql);
//		ResultSet resultSet = destination.getResultSet(sql);
//		int pesiDbKey = 0;
//		try {
//			while (resultSet.next()) {
//				pesiDbKey = resultSet.getInt("pesi_id");
//			}
//		} catch (SQLException e) {
//			logger.error("Couldn't determine number of matching TaxonNames.");
//			throw new RuntimeException("Couldn't determine number of matching TaxonNames.");
//		}
////		if (pesiDbKey == 0) {
////			logger.warn("A datawarehouse database identifier could not be determined for this cdmBase entity: " + cdmBase.getUuid());
////		}
//		return pesiDbKey;
//	}
	
	/**
	 * Removes a {@link CdmBase CdmBase} entry from this state's {@link java.util.Map Map}.
	 * @param cdmBase The {@link CdmBase CdmBase} to be deleted.
	 * @return Whether deletion was successful or not.
	 */
//	public boolean removeDbId(CdmBase cdmBase) {
//		if (cdmBase != null) {
//			IdType type = getConfig().getIdType();
//			if (type != IdType.CDM_ID) {
//				dbIdMap.remove(cdmBase.getUuid());
//				return true;
//			} else {
//				return false;
//			}
//		} else {
//			logger.warn("CdmBase was (null). No entries in dbIdMap available");
//			return false;
//		}
//	}

	/**
	 * Returns whether the given object was processed before or not.
	 * @param
	 * @return
	 */
	public boolean alreadyProcessedSource(Integer sourceId) {
		if (processedSourceList.contains(sourceId)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Adds given Source to the list of processed Sources.
	 */
	public boolean addToProcessedSources(Integer sourceId) {
		if (! processedSourceList.contains(sourceId)) {
			processedSourceList.add(sourceId);
		}
		
		return true;
	}

	/**
	 * Returns whether the given object was processed before or not.
	 * @param
	 * @return
	 */
//	public boolean alreadyProcessedTaxonName(Integer taxonNameId) {
//		if (processedTaxonNameList.contains(taxonNameId)) {
//			return true;
//		} else {
//			return false;
//		}
//	}

	/**
	 * Adds given TaxonName to the list of processed TaxonNames.
	 */
//	public boolean addToProcessedTaxonNames(Integer taxonNameId) {
//		processedTaxonNameList.add(taxonNameId);
//		
//		return true;
//	}

	/**
	 * Determines ParentTaxonFk, TreeIndex and KingdomFk from database.
	 * @param taxonId
	 * @return ParentTaxonFk, TreeIndex and KingdomFk
	 */
	public boolean getParentTaxonFkAndTreeIndexAndKingdomFk(String taxonId, Data newData) {
		boolean set = false;
		try {
			Select_KingdomId_TreeIndex_NomenclaturalCode_Stmt.setString(1, "%#" + taxonId + "#%");
			resultSet  = Select_KingdomId_TreeIndex_NomenclaturalCode_Stmt.executeQuery();
		} catch (SQLException e) {
			logger.error("Could not retrieve kingdomId, TreeIndex and nomenclaturalCode from database: " + e.getMessage());
		}
		if (resultSet != null) {
			try {
				while (resultSet.next()) {
					// Only one row is of interest
					newData.setKingdomId(resultSet.getInt(1));
					newData.setTreeIndex(resultSet.getString(2));
					newData.setNomenclaturalCode(resultSet.getString(3));

					set = true;
					break;
				}
			} catch (SQLException e) {
				logger.error("Couldn't match: " + e.getMessage());
			}
		}

		if (set && newData.getTreeIndex() != null) {
			// Determine treeIndex for the given TaxonId
			String treeIndexWithoutTaxonId = newData.getTreeIndex().substring(0, newData.getTreeIndex().indexOf(taxonId));
			newData.setTreeIndex(treeIndexWithoutTaxonId + taxonId + "#");

			// Determine parentTaxonId for given TaxonId
			StringTokenizer tokenizer = new StringTokenizer(treeIndexWithoutTaxonId, "#");
			String parentTaxonIdString = null;
			while (tokenizer.hasMoreTokens()) {
				parentTaxonIdString = (String) tokenizer.nextElement();
			}

			try {
				newData.setParentTaxonId(Integer.parseInt(parentTaxonIdString));
			} catch (NumberFormatException e) {
				logger.warn("String " + parentTaxonIdString + " could not be parsed to int for taxonId " + taxonId);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Stores KingdomFk, TreeIndex and NomenclaturalCode in database.
	 * @param kingdomFk
	 * @param treeIndex
	 * @return
	 */
	public boolean addToAlreadyProcessedTreeIndexAndKingdomFk(Data newData) {
		try {
			Insert_KingdomId_TreeIndex_NomenclaturalCode_Stmt.setInt(1, newData.getKingdomId());
			Insert_KingdomId_TreeIndex_NomenclaturalCode_Stmt.setString(2, newData.getTreeIndex());
			Insert_KingdomId_TreeIndex_NomenclaturalCode_Stmt.setString(3, newData.getNomenclaturalCode());
			Insert_KingdomId_TreeIndex_NomenclaturalCode_Stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Could not store kingdomId, TreeIndex and nomenclaturalCode in database: " + e.getMessage());
		}

		return true;
	}

	/**
	 * Clears the list of processed TaxonNames.
	 */
	public void clearAlreadyProcessedTaxonNames() {
		processedTaxonNameList.clear();
	}

	/**
	 * Clears the list of already processed Sources.
	 */
	public void clearAlreadyProcessedSources() {
		processedSourceList.clear();
	}

	/**
	 * Clears the list of taxonNameId's processed in TreeIndex.
	 */
	public void clearIncludedInTreeIndex() {
		treeIndexList.clear();
	}

	/**
	 * Adds a taxonNameId to the list of taxonNameId's included in any known treeIndex.
	 */
	public void addToTreeIndex(Integer taxonNameId) {
		if (! treeIndexList.contains(taxonNameId)) {
			treeIndexList.add(taxonNameId);
		}
	}
	
	/**
	 * Checks whether a taxonNameId was added to any known treeIndex.
	 */
	public boolean isIncludedInTreeIndex(Integer taxonNameId) {
		if (treeIndexList.contains(taxonNameId)) {
			return true;
		} else {
			return false;
		}
	}
}
