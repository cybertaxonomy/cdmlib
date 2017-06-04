/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v33_34;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

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
public class UsernameConstraintUpdater extends SchemaUpdaterStepBase implements ISchemaUpdaterStep {
	private static final Logger logger = Logger.getLogger(UsernameConstraintUpdater.class);

	private String tableName;

	private String columnName;

	public static final UsernameConstraintUpdater NewInstance(String stepName, String tableName, String columnName){
		return new UsernameConstraintUpdater(stepName, tableName, columnName);
	}


	protected UsernameConstraintUpdater(String stepName, String tableName, String columnName) {
		super(stepName);
		this.tableName = tableName;
		this.columnName = columnName;
	}


	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		//remove 2-fold constraint
		boolean result = removeDuplicates(datasource, caseType);
		result &= removeExistingConstraint(datasource, caseType);
		result &= createColumnConstraint(datasource, caseType);
		result &= createUuidConstraint(datasource, caseType);
		return result ? 1 : null;
	}

	private boolean removeDuplicates(ICdmDataSource datasource, CaseType caseType) {
		try {
			Set<String> existing = new HashSet<String>();
			String sql = " SELECT id, columnName as uniquecol FROM tableName ";
			sql = sql.replace("columnName", columnName).replace("tableName", caseType.transformTo(tableName));
			ResultSet rs = datasource.executeQuery(sql);
			while (rs.next()){
				int id = rs.getInt("id");
				String key = rs.getString("uniquecol");
				while (key == null || existing.contains(key.toLowerCase())){
					key = key == null ? "_" : key + "_";
					String sqlUpdate = "UPDATE tableName SET columnName = '" + key + "' WHERE id = " + id;
					sqlUpdate = sqlUpdate.replace("columnName", columnName).replace("tableName", caseType.transformTo(tableName));
					datasource.executeUpdate(sqlUpdate);
				}
				existing.add(key.toLowerCase());
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


	private boolean createUuidConstraint(ICdmDataSource datasource, CaseType caseType) {
		try {
			String updateQuery = getCreateQuery(datasource, caseType, tableName, tableName + "_UniqueKey", "uuid");
			datasource.executeUpdate(updateQuery);
			return true;
		} catch (Exception e) {
			logger.warn("Unique index for " + tableName + ".uuid could not be created");
			return false;
		}
	}

	private boolean createColumnConstraint(ICdmDataSource datasource, CaseType caseType) {
		try {
			String updateQuery = getCreateQuery(datasource, caseType, tableName, columnName + "_", columnName);
			datasource.executeUpdate(updateQuery);
			return true;
		} catch (Exception e) {
			logger.warn("Unique index for username could not be created");
			return false;
		}
	}

	private String getCreateQuery(ICdmDataSource datasource, CaseType caseType, String tableName, String constraintName, String columnName) {
			DatabaseTypeEnum type = datasource.getDatabaseType();
			String indexName = "_UniqueKey";
			String updateQuery;
			if (type.equals(DatabaseTypeEnum.MySQL)){
				//Maybe MySQL also works with the below syntax. Did not check yet.
				updateQuery = "ALTER TABLE @@"+ tableName + "@@ ADD UNIQUE INDEX " + constraintName + " ("+columnName+");";
			}else if (type.equals(DatabaseTypeEnum.H2) || type.equals(DatabaseTypeEnum.PostgreSQL) || type.equals(DatabaseTypeEnum.SqlServer2005)){
				updateQuery = "CREATE UNIQUE INDEX " + constraintName + " ON "+tableName+"(" + columnName + ")";
			}else{
				throw new IllegalArgumentException("Datasource type not supported: " + type.getName());
			}
			updateQuery = updateQuery.replace("@indexName", indexName);
			updateQuery = caseType.replaceTableNames(updateQuery);
			return updateQuery;
	}

	private boolean removeExistingConstraint(ICdmDataSource datasource, CaseType caseType) {
		try {
			DatabaseTypeEnum type = datasource.getDatabaseType();
			String indexName = "_UniqueKey";
			String updateQuery = makeRemoveConstraintUpdateQuery(caseType, type, indexName);
			try {
				datasource.executeUpdate(updateQuery);
			} catch (Exception e) {
				indexName = "uuid";
				updateQuery = makeRemoveConstraintUpdateQuery(caseType, type, indexName);
				datasource.executeUpdate(updateQuery);
			}
			return true;
		} catch (Exception e) {
			logger.warn("Old index could not be removed");
			return false;
		}
	}


	/**
	 * @param caseType
	 * @param type
	 * @param indexName
	 * @param updateQuery
	 * @return
	 */
	private String makeRemoveConstraintUpdateQuery(CaseType caseType,
			DatabaseTypeEnum type, String indexName) {
		String updateQuery;
		if (type.equals(DatabaseTypeEnum.MySQL)){
			updateQuery = "ALTER TABLE @@" + tableName + "@@ DROP INDEX @indexName";
		}else if (type.equals(DatabaseTypeEnum.H2)){
			updateQuery = "ALTER TABLE @@" + tableName + "@@ DROP CONSTRAINT IF EXISTS @indexName";
		}else if (type.equals(DatabaseTypeEnum.PostgreSQL)){
			updateQuery = "ALTER TABLE @@" + tableName + "@@ DROP CONSTRAINT @indexName";
		}else if (type.equals(DatabaseTypeEnum.SqlServer2005)){
			//TODO
			throw new RuntimeException("Remove index not yet supported for SQLServer");
		}else{
			throw new IllegalArgumentException("Datasource type not supported: " + type.getName());
		}
		updateQuery = updateQuery.replace("@indexName", indexName);
		updateQuery = caseType.replaceTableNames(updateQuery);
		return updateQuery;
	}
}
