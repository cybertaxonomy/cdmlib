// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v33_34;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * @author a.mueller
 * @date 16.09.2010
 *
 */
public class UsernameConstraintUpdater extends SchemaUpdaterStepBase<UsernameConstraintUpdater> implements ISchemaUpdaterStep {
	private static final Logger logger = Logger.getLogger(UsernameConstraintUpdater.class);
	
	public static final UsernameConstraintUpdater NewInstance(String stepName){
		return new UsernameConstraintUpdater(stepName);
	}

	
	protected UsernameConstraintUpdater(String stepName) {
		super(stepName);
	}
	

	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		//remove 2-fold constraint
		removeExistingConstraint(datasource, caseType);
		createUsernameConstraint(datasource, caseType);
		createUuidConstraint(datasource, caseType);
		return null;
	}
	
	private void createUuidConstraint(ICdmDataSource datasource,
			CaseType caseType) {
		try {
			String updateQuery = getCreateQuery(datasource, caseType, "@@UserAccount@@", "username_", "username");
			datasource.executeUpdate(updateQuery);
		} catch (SQLException e) {
			logger.warn("Unique index for UserAccount.uuid could not be created");
		}
	}
	
	private void createUsernameConstraint(ICdmDataSource datasource,
			CaseType caseType) {
		try {
			String updateQuery = getCreateQuery(datasource, caseType, "@@UserAccount@@", "username_", "username");
			datasource.executeUpdate(updateQuery);
		} catch (SQLException e) {
			logger.warn("Unique index for username could not be created");
		}
	}
	
	private String getCreateQuery(ICdmDataSource datasource, CaseType caseType, String tableName, String constraintName, String columnName) {
			DatabaseTypeEnum type = datasource.getDatabaseType();
			String indexName = "_UniqueKey";
			String updateQuery;
			if (type.equals(DatabaseTypeEnum.MySQL)){
				updateQuery = "ALTER TABLE @@"+ tableName + "@@ ADD UNIQUE INDEX " + constraintName + " ("+columnName+");";
			}else if (type.equals(DatabaseTypeEnum.H2) || type.equals(DatabaseTypeEnum.PostgreSQL) || type.equals(DatabaseTypeEnum.SqlServer2005)){
				updateQuery = "CREATE UNIQUE INDEX " + constraintName + " ON "+tableName+"(" + columnName + ")";
			}else{
				throw new IllegalArgumentException("Datasource type not supported: " + type.getName());
			}
			updateQuery = updateQuery.replace("@indexName", indexName);
			caseType.replaceTableNames("@@UserAccount@@");
			return updateQuery;
	}


	private void removeExistingConstraint(ICdmDataSource datasource, CaseType caseType) {
		try {
			DatabaseTypeEnum type = datasource.getDatabaseType();
			String indexName = "_UniqueKey";
			String updateQuery;
			if (type.equals(DatabaseTypeEnum.MySQL)){
				updateQuery = "ALTER TABLE @@UserAccount@@ DROP INDEX @indexName";
			}else if (type.equals(DatabaseTypeEnum.H2)){
				updateQuery = "ALTER TABLE @@UserAccount@@ DROP CONSTRAINT IF EXISTS @indexName";
			}else if (type.equals(DatabaseTypeEnum.PostgreSQL)){
				updateQuery = "ALTER TABLE @@UserAccount@@ DROP CONSTRAINT @indexName";
			}else if (type.equals(DatabaseTypeEnum.SqlServer2005)){
				//TODO
				throw new RuntimeException("Remove index not yet supported for SQLServer");
			}else{
				throw new IllegalArgumentException("Datasource type not supported: " + type.getName());
			}
			updateQuery = updateQuery.replace("@indexName", indexName);
			updateQuery = caseType.replaceTableNames("@@UserAccount@@");
			datasource.executeUpdate(updateQuery);
		} catch (SQLException e) {
			logger.warn("Old index could not be removed");
		}
	}

//	private boolean checkExists(ICdmDataSource datasource) throws SQLException, DatabaseTypeNotSupportedException {
//		DatabaseTypeEnum type = datasource.getDatabaseType();
//		if (type.equals(DatabaseTypeEnum.MySQL)){
//			String sql = "SELECT count(*)	FROM information_schema.TABLE_CONSTRAINTS " + 
//					" WHERE table_name ='@tableName' AND CONSTRAINT_SCHEMA = '@dbName' AND CONSTRAINT_TYPE = 'UNIQUE' ";
//			sql = sql.replace("@tableName", tableName);
//			sql = sql.replace("@columnName", indexColumn);
//			sql = sql.replace("@dbName", datasource.getDatabase());
//			long count = (Long)datasource.getSingleValue(sql);
//			return count > 0;
//		}else if (type.equals(DatabaseTypeEnum.PostgreSQL)){
//			logger.warn("checkExists not yet implemented for PostGreSQL" );
//			return true;
//		}else if (type.equals(DatabaseTypeEnum.H2)){
//			String indexName = getIndexName(datasource);
//			return indexName != null;
//		}else{
//			// not needed
//			return true;
//		}
//	}


//	public String getUpdateQueryString(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) throws DatabaseTypeNotSupportedException, SQLException {
//		//NOTE: no caseType required here
//		String updateQuery;
//		DatabaseTypeEnum type = datasource.getDatabaseType();
//		String indexName = getIndexName(datasource);
//		
////		if (type.equals(DatabaseTypeEnum.SqlServer2005)){
//			//MySQL allows both syntaxes
////			updateQuery = "ALTER TABLE @tableName ADD @columnName @columnType";
////		}else
//			if (type.equals(DatabaseTypeEnum.H2)){
//			updateQuery = "ALTER TABLE @tableName DROP CONSTRAINT IF EXISTS @indexName";
//		}else if (type.equals(DatabaseTypeEnum.PostgreSQL)){
////			updateQuery = "DROP INDEX IF EXISTS @indexName";  // does not work because index is used in the constraint
////			updateQuery = "ALTER TABLE @tableName DROP CONSTRAINT IF EXISTS @indexName"; //"if exists" does not work (version 8.4) 
//			updateQuery = "ALTER TABLE @tableName DROP CONSTRAINT @indexName";
//		}else if (type.equals(DatabaseTypeEnum.MySQL)){
//			updateQuery = "ALTER TABLE @tableName DROP INDEX @indexName";
//		}else{
//			updateQuery = null;
//			String warning = "Update step '" + this.getStepName() + "' is not supported by " + type.getName();
//			monitor.warning(warning);
//			throw new DatabaseTypeNotSupportedException(warning);
//		}
//		updateQuery = updateQuery.replace("@tableName", tableName);
//		updateQuery = updateQuery.replace("@indexName", indexName);
//		
//		return updateQuery;
//	}
//
//
//	private String getIndexName(ICdmDataSource datasource) throws DatabaseTypeNotSupportedException, SQLException {
//		String result = this.indexColumn;
//		DatabaseTypeEnum type = datasource.getDatabaseType();
//		if (type.equals(DatabaseTypeEnum.SqlServer2005)){
//			throw new DatabaseTypeNotSupportedException(type.toString());
//		}else if (type.equals(DatabaseTypeEnum.MySQL)){
//			result = this.indexColumn;
//		}else if (type.equals(DatabaseTypeEnum.H2) ){
////			String sql = "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME = @tableName AND INDEX_TYPE_NAME = 'UNIQUE INDEX'"; 
//			String sql = "SELECT CONSTRAINT_NAME " + 
//					" FROM INFORMATION_SCHEMA.CONSTRAINTS "+
//					" WHERE CONSTRAINT_CATALOG = '@dbName' AND "+
//					" TABLE_NAME = '@tableName' AND CONSTRAINT_TYPE = 'UNIQUE' AND "+ 
//					" COLUMN_LIST = '@columnName'"; 
//			sql = sql.replace("@tableName", tableName.toUpperCase());
//			sql = sql.replace("@columnName", indexColumn.toUpperCase());
//			sql = sql.replace("@dbName", datasource.getDatabase().toUpperCase());
//			String constraintName = (String)datasource.getSingleValue(sql);
//			result = constraintName;
//		}else if (type.equals(DatabaseTypeEnum.PostgreSQL)){
//			//TODO do we need this cased?
//			result = this.tableName + "_" + this.indexColumn + "_key";
//		}else{
//			throw new DatabaseTypeNotSupportedException(type.toString());
//		}
//		return result;
//		
//	}




}
