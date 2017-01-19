/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.update.v25_30;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * Creates the table needed by the {@link org.hibernate.id.enhanced.TableGenerator}
 * We expect the generator to be configured with <code>prefer_entity_table_as_segment_value</code> 
 * set to <code>true</code> (the generator does not make lots of sense without this option)
 * 
 * We also create sequences for all tables that are not empty. Otherwise we would run into 
 * id conflicts, because the generator expects the database to be empty and creates sequences,
 * if they do not exist, as needed.
 * 
 * @author n.hoffmann
 * @created Oct 27, 2010
 */
public class SequenceTableCreator extends SchemaUpdaterStepBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SequenceTableCreator.class);
	
	// TODO These values are configurable in the enhanced.TableGenerator
	// can we retrieve these values from the identity generator directly?
	private static final String TABLE_NAME = "hibernate_sequences";
	private static final String SEGMENT_COLUMN_NAME = "sequence_name";
	private static final String VALUE_COLUMN_NAME = "next_val";
	private static final int INCREMENT_SIZE = 10;
		
	/**
	 * @param stepName
	 */
	protected SequenceTableCreator(String stepName) {
		super(stepName);
	}

	public static SequenceTableCreator NewInstance(String stepName){
		return new SequenceTableCreator(stepName);
	}

	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType){
		boolean result = true;
		try {
			result &= createSequenceTable(datasource, monitor, caseType);
			result &= makeEntriesForEntityTables(datasource, monitor, caseType);
		} catch (Exception e) {
			monitor.warning(e.getMessage(), e);
			result = false;
		}
		
		return (result == true ) ? 0 : null;
	}

	/**
	 * @param monitor 
	 * @param datasource 
	 * @param caseType 
	 * @return
	 * @throws SQLException 
	 */
	private boolean createSequenceTable(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		boolean result = true;
		String createTableQuery = null;
		// TODO add create table statements for other supported databases
		if(datasource.getDatabaseType().equals(DatabaseTypeEnum.MySQL)){
			createTableQuery = "CREATE TABLE `" + TABLE_NAME + "` (" +
				"  `" + SEGMENT_COLUMN_NAME + "` varchar(255) NOT NULL," +
				"  `" + VALUE_COLUMN_NAME + "` bigint(20) default NULL," +
				"  PRIMARY KEY  (`" + SEGMENT_COLUMN_NAME + "`)" +
				");";
		}else{
			throw new RuntimeException("Database type " + datasource.getDatabaseType() + " is currently not supported by the updater");
		}
		
		datasource.executeUpdate(createTableQuery);
		
		return result;
	}
	
	/**
	 * @param monitor 
	 * @param datasource 
	 * @param caseType 
	 * @return
	 * @throws SQLException 
	 */
	private boolean makeEntriesForEntityTables(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		
		DatabaseMetaData metaData = datasource.getMetaData();
		ResultSet resultSet = metaData.getTables(datasource.getDatabase(), null, null, null);
				
		String maxIdQuery = "SELECT MAX(ID) FROM @tableName";
		String insertQuery = "INSERT INTO " + TABLE_NAME + " ( " + SEGMENT_COLUMN_NAME + ", " + VALUE_COLUMN_NAME + ")" +
				" VALUES ('@tableName', (@maxId + " + INCREMENT_SIZE + "))";
		
		Object maxId = null;
		
		while(resultSet.next()){
			// through debugging we found out that the table name is in column 3. 
			// TODO improve this if you know that this will not always be the case for all database types 
			// and/or if you know of a good way to do it in more generic way
			String tableName = resultSet.getString(3);
			// this way we simply filter out all relation tables, could have done this with a tableNamePattern passed to getTables(...)
			// but this was faster. 
			if(tableName.contains("_")){
				continue;
			}
			
			try{
				String query = maxIdQuery.replace("@tableName", tableName);
				maxId = datasource.getSingleValue(query);
			}catch(MySQLSyntaxErrorException e){
				// table does not have a column id, so it is not an entity table
				maxId = null;
			}
			
			// empty tables will not set the maxId. 
			// Empty tables will also not need to be updated, because the identity generator creates sequences for tables it did not have before
			if(maxId != null){
				monitor.subTask("Inserting sequence for table: " + tableName + " with maxId: " + maxId);
				
				String query = insertQuery.replace("@tableName", tableName);
				query = query.replace("@maxId", maxId.toString());
				
				datasource.executeUpdate(query);
			}
			
		}
		return true;
	}

}
