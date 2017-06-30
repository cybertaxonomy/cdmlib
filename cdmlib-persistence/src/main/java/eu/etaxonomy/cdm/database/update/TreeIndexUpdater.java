/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.ITreeNode;

/**
 * @author a.mueller
 * @date 09.08.2013
 *
 */
public class TreeIndexUpdater
        extends AuditedSchemaUpdaterStepBase
        implements ISchemaUpdaterStep {

    private static final Logger logger = Logger.getLogger(TreeIndexUpdater.class);

	private String indexColumnName = "treeIndex";
	private final String treeIdColumnName;
	private final String parentIdColumnName = "parent_id";

	public static final TreeIndexUpdater NewInstance(String stepName, String tableName, String treeIdColumnName, boolean includeAudTable){
		return new TreeIndexUpdater(stepName, tableName, treeIdColumnName, null, includeAudTable);
	}

	public static final TreeIndexUpdater NewInstance(String stepName, String tableName, String treeIdColumnName, String indexColumnName, boolean includeAudTable){
		return new TreeIndexUpdater(stepName, tableName, treeIdColumnName, indexColumnName, includeAudTable);
	}


	protected TreeIndexUpdater(String stepName, String tableName, String treeIdColumnName, String indexColumnName, boolean includeAudTable) {
		super(stepName, tableName, includeAudTable);
		this.treeIdColumnName = treeIdColumnName;
		this.indexColumnName = indexColumnName == null ? this.indexColumnName : indexColumnName;
	}

    @Override
    protected void invokeOnTable(String tableName, ICdmDataSource datasource,
            IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) {
        try{

	//		String charType = "CHAR";  //TODO may depend on database type

			//clean up nodes without classification  //this should not happen with correct "delete" implementation
			String sql = String.format(" DELETE FROM %s WHERE %s IS NULL ", tableName, treeIdColumnName);
			datasource.executeUpdate(sql);

			//... set all index entries to NULL
			sql = String.format(" UPDATE %s SET %s = NULL", tableName, indexColumnName);
			datasource.executeUpdate(sql);

			//start
			String separator = ITreeNode.separator;
			String treePrefix = ITreeNode.treePrefix;
			sql = String.format(" UPDATE %s " +
					" SET %s = CONCAT('%s%s', %s, '%s', id, '%s') " +
					" WHERE %s IS NULL AND %s IS NOT NULL ",
						tableName,
						indexColumnName, separator, treePrefix, treeIdColumnName, separator, separator,
						parentIdColumnName, treeIdColumnName);
			datasource.executeUpdate(sql);

			//width search index creation
			String sqlCount = String.format(" SELECT count(*) as n " +
					" FROM %s child INNER JOIN %s parent ON child.%s = parent.id " +
					" WHERE parent.%s IS NOT NULL AND child.%s IS NULL ",
					tableName, tableName, parentIdColumnName, indexColumnName, indexColumnName);

			Long n;
			do {

				//MySQL
				if (datasource.getDatabaseType().equals(DatabaseTypeEnum.MySQL)){
					sql = String.format(" UPDATE %s child " +
							" INNER JOIN %s parent ON child.%s = parent.id " +
							" SET child.%s = CONCAT( parent.%s, child.id, '%s') " +
							" WHERE parent.%s IS NOT NULL AND child.%s IS NULL ",
								tableName,
								tableName, parentIdColumnName,
								indexColumnName, indexColumnName, separator,
								indexColumnName, indexColumnName);
				}else{
					//ANSI
					//http://stackoverflow.com/questions/1293330/how-can-i-do-an-update-statement-with-join-in-sql
					//does not work with MySQL as MySQL does not allow to use the same table in Subselect and Update (error 1093: http://dev.mysql.com/doc/refman/5.1/de/subquery-errors.html)
					sql = String.format(" UPDATE %s " +
							" SET %s = ( " +
								" ( SELECT CONCAT ( parent.%s, %s.id, '%s') " +
									" FROM %s parent " +
									" WHERE parent.id = %s.%s ) " +
								" ) " +
							" WHERE EXISTS ( " +
									" SELECT * " +
									" FROM %s parent " +
									" WHERE parent.id = %s.%s AND parent.%s IS NOT NULL AND %s.%s IS NULL " +
								") " +
								" ",
							tableName,
							indexColumnName,
								indexColumnName, tableName, separator,
								tableName,
								tableName, parentIdColumnName,

								tableName,
								tableName, parentIdColumnName, indexColumnName, tableName, indexColumnName
							);
				}

				datasource.executeUpdate(sql);

				n = (Long)datasource.getSingleValue(sqlCount);
			}	while (n > 0) ;

			sqlCount = String.format(" SELECT count(*) as n " +
					" FROM %s " +
					" WHERE %s IS NULL ", tableName, indexColumnName);
			n = (Long)datasource.getSingleValue(sqlCount);
			if (n > 0){
				String message = "There are tree nodes with no tree index in %s. This indicates that there is a problem in the tree structure of 1 or more classifications.";
				logger.error(String.format(message, tableName));
				result.addWarning(message, (String)null, getStepName());
			}

			return;
		}catch(Exception e){
		    String message = e.getMessage();
			monitor.warning(message, e);
			logger.error(message);
			result.addException(e, message, getStepName() + ", TreeIndexUpdater.invokeOnTable");
			return;
		}
	}

}
