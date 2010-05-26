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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.pesi.out.PesiTaxonExport.Data;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 12.02.2010
 *
 */
public class PesiExportState extends DbExportStateBase<PesiExportConfigurator>{
	private static final Logger logger = Logger.getLogger(PesiExportState.class);
	private static final String state_db_table = "tmp_state";
	private static final String processed_taxonname_db_table = "tmp_processed_taxonname";
	private static final String processed_source_db_table = "tmp_processed_source";
	private static final String processed_treeindex_and_kingdomfk_db_table = "tmp_processed_treeindex_kingdomfk";
	
	/**
	 * @param config
	 */
	public PesiExportState(PesiExportConfigurator config) {
		super(config);
		createStateTables();
	}
	
	/**
	 * Create the database table that hosts state information.
	 */
	public boolean createStateTables() {
		Connection connection = getConfig().getDestination().getConnection();
		
		String query;
        Statement stmt;
        
        if (deleteStateTables()) {
	        try {
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
                query="CREATE TABLE " + processed_treeindex_and_kingdomfk_db_table + " (kingdom_id int, tree_index varchar(100))";
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
		Connection connection = getConfig().getDestination().getConnection();
		
		String query;
        Statement stmt;
        
        try {
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
    }

        return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.DbExportStateBase#putDbId(eu.etaxonomy.cdm.model.common.CdmBase, int)
	 */
	@Override
	public void putDbId(CdmBase cdmBase, int dbId) {
		String sql;
		Source destination =  getConfig().getDestination();

		// Add TaxonName to table of processed TaxonNames
		sql = "INSERT INTO " + state_db_table + " VALUES ('" + cdmBase.getUuid() + "', " + dbId + ")";
		destination.setQuery(sql);
		destination.update(sql);
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

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.DbExportStateBase#getDbId(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	public Integer getDbId(CdmBase cdmBase) {
		String sql;
		Source destination =  getConfig().getDestination();

		// Get datawarehouse database identifier from table for state information
		sql = "SELECT pesi_id FROM " + state_db_table + " WHERE cdm_id = '" + cdmBase.getUuid() + "'";
		destination.setQuery(sql);
		ResultSet resultSet = destination.getResultSet(sql);
		int pesiDbKey = 0;
		try {
			while (resultSet.next()) {
				pesiDbKey = resultSet.getInt("pesi_id");
			}
		} catch (SQLException e) {
			logger.error("Couldn't determine number of matching TaxonNames.");
			throw new RuntimeException("Couldn't determine number of matching TaxonNames.");
		}
//		if (pesiDbKey == 0) {
//			logger.warn("A datawarehouse database identifier could not be determined for this cdmBase entity: " + cdmBase.getUuid());
//		}
		return pesiDbKey;
	}
	
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
	public boolean alreadyProcessed(Object object) {
//		if (processedList.contains(taxonName)) {
//			return true;
//		} else {
//			return false;
//		}
		
		boolean result = false;
		String sql = null;
		Source destination =  getConfig().getDestination();

		if (object instanceof TaxonNameBase) {
			// Count occurrence of taxonName in table of processed TaxonNames
			TaxonNameBase taxonName = CdmBase.deproxy(object, TaxonNameBase.class);
			sql = "SELECT count(*) FROM " + processed_taxonname_db_table + " WHERE cdm_id = '" + taxonName.getUuid() + "'";
		} else if (object instanceof Integer) {
			// Count occurrence of source in table of processed Sources
			sql = "SELECT count(*) FROM " + processed_source_db_table + " WHERE cdm_id = " + (Integer)object;
		}
		if (sql != null) {
			destination.setQuery(sql);
			ResultSet resultSet = destination.getResultSet(sql);
			int count = 0;
			if (resultSet != null) {
				try {
					resultSet.next();
					count = resultSet.getInt(1);
				} catch (SQLException e) {
					logger.error("Couldn't match.");
				}
				
				if (count == 1) {
					result = true;
				} else if (count == 0) {
					result = false;
				} else if (count > 1) {
					logger.error("This object exists more than once in database table: " + object);
				}
			}
		}
		return result;
	}

	/**
	 * Add given TaxonName to the list of processed TaxonName's.
	 * @param sourceFk
	 */
	public boolean addToProcessed(Object object) {
//		processedList.add(taxonName);
		
		String sql = null;
		Source destination =  getConfig().getDestination();

		if (object instanceof TaxonNameBase) {
			// Add TaxonName to table of processed TaxonNames
			TaxonNameBase taxonName = CdmBase.deproxy(object, TaxonNameBase.class);
			sql = "INSERT INTO " + processed_taxonname_db_table + " VALUES ('" + taxonName.getUuid() + "')";
		} else if (object instanceof Integer) {
			// Add SourceFk to table of processed Sources
			sql = "INSERT INTO " + processed_source_db_table + " VALUES (" + (Integer)object + ")";
		}
		
		if (sql != null) {
			destination.setQuery(sql);
			destination.update(sql);
		}
		return true;
	}

	/**
	 * 
	 * @param taxonId
	 * @return
	 */
	public boolean alreadyProcessedTreeIndexAndKingdomFk(String taxonId, Data newData) {
		String sql = null;
		Source destination =  getConfig().getDestination();

		// Retrieve treeIndex and kingdomId from table of processed TreeIndex and KingdomFk
		sql = "SELECT kingdom_id, tree_index FROM " + processed_treeindex_and_kingdomfk_db_table + " WHERE tree_index like '%#" + taxonId + "#%'";

		int count = 0;
		if (sql != null) {
			destination.setQuery(sql);
			ResultSet resultSet = destination.getResultSet(sql);
			if (resultSet != null) {
				try {
					while (resultSet.next()) {
						newData.setKingdomId(resultSet.getInt(1));
						newData.setTreeIndex(resultSet.getString(2));
						count++;
					}
				} catch (SQLException e) {
					logger.error("Couldn't match: " + e.getMessage());
				}
				
				if (count > 1) {
					logger.warn("Select retrieved more than one column for taxonId " + taxonId);
				}
			}
		}

		if (count == 1 && newData.getTreeIndex() != null) {
			// Determine parentTaxonId for given TaxonId
			StringTokenizer tokenizer = new StringTokenizer(newData.getTreeIndex(), "#");
			String parentTaxonIdString = null;
			while (tokenizer.hasMoreTokens()) {
				parentTaxonIdString = (String) tokenizer.nextElement();
			}
//			String parentTaxonIdString = treeIndex.substring(treeIndex.lastIndexOf("#", treeIndex.length()-1)+1, treeIndex.length()-1);

			// Determine treeIndex for the given TaxonId
			newData.setTreeIndex(newData.getTreeIndex().substring(0, newData.getTreeIndex().indexOf(taxonId)) + taxonId + "#");

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
	 * 
	 * @param kingdomFk
	 * @param treeIndex
	 * @return
	 */
	public boolean addToAlreadyProcessedTreeIndexAndKingdomFk(Integer kingdomFk, String treeIndex) {
		String sql = null;
		Source destination =  getConfig().getDestination();

		// Add TreeIndex and KingdomFk to table of processed TreeIndex and KingdomFk
		sql = "INSERT INTO " + processed_treeindex_and_kingdomfk_db_table + " VALUES (" + kingdomFk + ", '" + treeIndex + "')";

		if (sql != null) {
			destination.setQuery(sql);
			destination.update(sql);
		}
		return true;
	}

	/**
	 * Clears the database table containing already processed TaxonNames.
	 */
	public void clearAlreadyProcessedTaxonNames() {
		String sql;
		Source destination =  getConfig().getDestination();

		sql = "DELETE FROM " + processed_taxonname_db_table;
		destination.setQuery(sql);
		destination.update(sql);
	}

}
