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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

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
	private PreparedStatement select_KingdomId_TreeIndex_NomenclaturalCode_Stmt;
	private PreparedStatement insert_KingdomId_TreeIndex_NomenclaturalCode_Stmt;
	private PreparedStatement insert_state_Stmt;
	private PreparedStatement select_state_Stmt;
	
	/**
	 * @param config
	 */
	public PesiExportState(PesiExportConfigurator config) {
		super(config);
		
		try {
			connection = getConfig().getDestination().getConnection();

			// Retrieve treeIndex, kingdomId and nomenclaturalCode from database
//			String sql = "SELECT kingdom_id, tree_index, nomenclatural_code FROM " + processed_treeindex_and_kingdomfk_db_table + " WHERE tree_index like ?";
//			select_KingdomId_TreeIndex_NomenclaturalCode_Stmt = connection.prepareStatement(sql);
			
			// Add TreeIndex, KingdomFk and nomenclaturalCode to database
//			sql = "INSERT INTO " + processed_treeindex_and_kingdomfk_db_table + " VALUES (?, ?, ?)";
//			insert_KingdomId_TreeIndex_NomenclaturalCode_Stmt = connection.prepareStatement(sql);
			
			// Add cdmId/pesiId pair to database 
			String sql = "INSERT INTO " + state_db_table + " VALUES ( ?, ?)";
			insert_state_Stmt = connection.prepareStatement(sql);
			
			sql = "SELECT pesi_id FROM " + state_db_table + " WHERE cdm_id = ?";
			select_state_Stmt = connection.prepareStatement(sql);

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
		
		String query;
        Statement stmt;
        
        if (deleteStateTables()) {
/*	        try {
	                query="CREATE TABLE " + state_db_table + " (cdm_id varchar(100), pesi_id int)";
	                stmt = connection.createStatement();
	                stmt.executeUpdate(query);
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
	        }
	
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
	        }*/
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
        }

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
    }*/

        return result;
	}
	
	/**
	 * Put datawarehouse database identifier into table for state information.
	 * @param cdmBase
	 * @param dbId
	 */
	@Override
	public void putDbId(CdmBase cdmBase, int dbId) {
//		try {
//			insert_state_Stmt.setInt(1, cdmBase.getId());
//			insert_state_Stmt.setInt(2, dbId);
//			insert_state_Stmt.executeUpdate();
//		} catch (SQLException e) {
//			logger.error("Could not store state information into database: " + e.getMessage());
//			e.printStackTrace();
//		}
	}

	/**
	 * Get datawarehouse database identifier from table for state information.
	 * @param cdmBase
	 * @return
	 */
	@Override
	public Integer getDbId(CdmBase cdmBase) {
		return cdmBase.getId();
		
//		ResultSet resultSet = null;
//		try {
//			select_state_Stmt.setInt(1, cdmBase.getId());
//			resultSet = select_state_Stmt.executeQuery();
//		} catch (SQLException e) {
//			logger.error("Could not determine datawarehouse database identifier: " + e.getMessage());
//			e.printStackTrace();
//		}
//
//		Integer pesiDbKey = 0;
//		try {
//			if (resultSet != null) {
//				while (resultSet.next()) {
//					pesiDbKey = resultSet.getInt("pesi_id");
//				}
//			}
//		} catch (SQLException e) {
//			logger.error("Could not determine pesiId for cdmId.");
//			throw new RuntimeException("Could not determine pesiId for cdmId.");
//		}
//		if (pesiDbKey == 0) {
//			logger.warn("A datawarehouse database identifier could not be determined for this cdmBase entity: " + cdmBase.getId());
//			pesiDbKey = null;
//		}
//		return pesiDbKey;
	}
	

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
	 * Clears the list of already processed Sources.
	 */
	public void clearAlreadyProcessedSources() {
		processedSourceList.clear();
	}

}
