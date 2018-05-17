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
 * @since 2016-10-18
 *
 */
public class IndexAdder extends SchemaUpdaterStepBase {
	private static final Logger logger = Logger.getLogger(IndexAdder.class);

	private String tableName;

	private String columnName;

	private Integer length;

// ********************** FACTORY ****************************************/

	public static final IndexAdder NewStringInstance(String stepName, String tableName, String columnName, Integer length){
		return new IndexAdder(stepName, tableName, columnName, length == null ? 255 : length);
	}

    public static final IndexAdder NewIntegerInstance(String stepName, String tableName, String columnName){
        return new IndexAdder(stepName, tableName, columnName, null);
    }

// **************************** CONSTRUCTOR *********************************/

	protected IndexAdder(String stepName, String tableName, String columnName, Integer length) {
		super(stepName);
		this.tableName = tableName;
		this.columnName = columnName;
		this.length = length;
	}

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {
        //remove 2-fold constraint
//		result &= removeExistingConstraint(datasource, caseType);
		createColumnConstraint(datasource, caseType, result);
		return;
	}

	private void createColumnConstraint(ICdmDataSource datasource,
	        CaseType caseType, SchemaUpdateResult result) {
		try {
		    String constraintName = StringUtils.uncapitalize(tableName) + columnName + "Index";
			if(constraintName.length()>64){
			    //MySQL has problems with index names > 64,  https://stackoverflow.com/questions/28615903/error-1059-identifier-name-too-long-on-foreign-key-constraints-from-existing-ta
			    constraintName = constraintName.replace("Base", "");
			}
		    String updateQuery = getCreateQuery(datasource, caseType, tableName, constraintName, columnName);
			datasource.executeUpdate(updateQuery);
			return;
		} catch (Exception e) {
		    String message = "Unique index for " + columnName + " could not be created";
			logger.warn(message);
			result.addException(e, message, "IndexAdder.createColumnConstraint");
			return;
		}
	}

	private String getCreateQuery(ICdmDataSource datasource, CaseType caseType, String tableName, String constraintName, String columnName) {
			DatabaseTypeEnum type = datasource.getDatabaseType();
//			String indexName = "_UniqueKey";
			String updateQuery;
			if (type.equals(DatabaseTypeEnum.MySQL)){
				//Maybe MySQL also works with the below syntax. Did not check yet.
				updateQuery = "ALTER TABLE @@"+ tableName + "@@ ADD INDEX " + constraintName + " ("+columnName+ makeLength()+");";
			}else if (type.equals(DatabaseTypeEnum.H2) || type.equals(DatabaseTypeEnum.PostgreSQL) || type.equals(DatabaseTypeEnum.SqlServer2005)){
				updateQuery = "CREATE INDEX " + constraintName + " ON "+tableName+"(" + columnName + ")";
			}else{
				throw new IllegalArgumentException("Datasource type not supported yet: " + type.getName());
			}
//			updateQuery = updateQuery.replace("@indexName", indexName);
			updateQuery = caseType.replaceTableNames(updateQuery);
			return updateQuery;
	}

	/**
     * @param length2
     * @return
     */
    private String makeLength() {
        if (length != null){
            return "(" + length + ")";
        }else{
            return "";
        }
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
