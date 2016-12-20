/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.v33_34.UsernameConstraintUpdater;

/**
 * Adds an Index on a string column.
 *
 * @see {@link UsernameConstraintUpdater}
 * @author a.mueller
 * @date 16.09.2010
 *
 */
public class IndexAdder extends SchemaUpdaterStepBase<IndexAdder> implements ISchemaUpdaterStep {
	private static final Logger logger = Logger.getLogger(IndexAdder.class);

	private String tableName;

	private String columnName;

	private Integer length;

	public static final IndexAdder NewInstance(String stepName, String tableName, String columnName, Integer length){
		return new IndexAdder(stepName, tableName, columnName, length);
	}


	protected IndexAdder(String stepName, String tableName, String columnName, Integer length) {
		super(stepName);
		this.tableName = tableName;
		this.columnName = columnName;
		this.length = length == null ? 255 :length;
	}


	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		//remove 2-fold constraint
		boolean result= true;
//		result &= removeExistingConstraint(datasource, caseType);
		result &= createColumnConstraint(datasource, caseType);
		return result ? 1 : null;
	}

	private boolean createColumnConstraint(ICdmDataSource datasource, CaseType caseType) {
		try {
		    String constraintName = StringUtils.uncapitalize(tableName) + columnName + "Index";
			String updateQuery = getCreateQuery(datasource, caseType, tableName, constraintName, columnName);
			datasource.executeUpdate(updateQuery);
			return true;
		} catch (Exception e) {
			logger.warn("Unique index for username could not be created");
			return false;
		}
	}

	private String getCreateQuery(ICdmDataSource datasource, CaseType caseType, String tableName, String constraintName, String columnName) {
			DatabaseTypeEnum type = datasource.getDatabaseType();
//			String indexName = "_UniqueKey";
			String updateQuery;
			if (type.equals(DatabaseTypeEnum.MySQL)){
				//Maybe MySQL also works with the below syntax. Did not check yet.
				updateQuery = "ALTER TABLE @@"+ tableName + "@@ ADD INDEX " + constraintName + " ("+columnName+"("+length+"));";
			}else if (type.equals(DatabaseTypeEnum.H2) || type.equals(DatabaseTypeEnum.PostgreSQL) || type.equals(DatabaseTypeEnum.SqlServer2005)){
				updateQuery = "CREATE INDEX " + constraintName + " ON "+tableName+"(" + columnName + ")";
			}else{
				throw new IllegalArgumentException("Datasource type not supported yet: " + type.getName());
			}
//			updateQuery = updateQuery.replace("@indexName", indexName);
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
