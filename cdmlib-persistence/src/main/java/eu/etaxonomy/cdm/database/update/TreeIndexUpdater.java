// $Id$
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
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @date 09.08.2013
 *
 */
public class TreeIndexUpdater extends AuditedSchemaUpdaterStepBase<TreeIndexUpdater> implements ISchemaUpdaterStep {
	private static final Logger logger = Logger.getLogger(TreeIndexUpdater.class);
	
	private String indexColumnName = "treeIndex";
	private String treeIdColumnName;
	private String parentIdColumnName = "parent_id";
	
	public static final TreeIndexUpdater NewInstance(String stepName, String tableName, String treeIdColumnName, boolean includeAudTable){
		return new TreeIndexUpdater(stepName, tableName, treeIdColumnName, null, includeAudTable);
	}

	public static final TreeIndexUpdater NewInstance(String stepName, String tableName, String treeIdColumnName, String indexColumnName, boolean includeAudTable){
		return new TreeIndexUpdater(stepName, tableName, treeIdColumnName, indexColumnName, includeAudTable);
	}
	
	
	protected TreeIndexUpdater(String stepName, String tableName, String treeIdColumnName, String indexColumnName, boolean includeAudTable) {
		super(stepName);
		this.tableName = tableName;
		this.treeIdColumnName = treeIdColumnName;
		this.indexColumnName = indexColumnName == null ? this.indexColumnName : indexColumnName;
		this.includeAudTable = includeAudTable;
	}

	@Override
	protected boolean invokeOnTable(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) {
		try{
			boolean result = true;			
			
	//		String charType = "CHAR";  //TODO may depend on database type
			
			//clean up  //this should not happen with correct "delete" implementation
			String sql = String.format(" DELETE FROM %s WHERE %s IS NULL ", tableName, treeIdColumnName);
			datasource.executeUpdate(sql);
			
			sql = String.format(" UPDATE %s SET %s = NULL", tableName, indexColumnName);
			datasource.executeUpdate(sql);
			
			//start
			sql = String.format(" UPDATE %s tn " +
					" SET tn.%s = CONCAT('#c', tn.%s, '#') " +
					" WHERE tn.%s IS NULL AND tn.%s IS NOT NULL ", 
					tableName, indexColumnName, treeIdColumnName, parentIdColumnName, treeIdColumnName);
			datasource.executeUpdate(sql);
			
			//width search index creation
			String sqlCount = String.format(" SELECT count(*) as n " +
					" FROM %s child INNER JOIN %s parent ON child.%s = parent.id " +
					" WHERE parent.%s IS NOT NULL AND child.%s IS NULL ",
					tableName, tableName, parentIdColumnName, indexColumnName, indexColumnName);
			
			Long n;
			do {
			
				sql = String.format(" UPDATE %s child INNER JOIN %s parent ON child.%s = parent.id " +
						" SET child.%s = CONCAT( parent.%s, child.id, '#') " +
						" WHERE parent.%s IS NOT NULL AND child.%s IS NULL ", 
							tableName, tableName, parentIdColumnName, indexColumnName, indexColumnName,
							indexColumnName, indexColumnName);
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
			}
			
			return result;
		}catch(Exception e){
			monitor.warning(e.getMessage(), e);
			logger.error(e.getMessage());
			return false;
		}
	}


}
