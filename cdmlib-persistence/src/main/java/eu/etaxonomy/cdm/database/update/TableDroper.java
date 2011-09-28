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

import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @date 16.09.2010
 *
 */
public class TableDroper extends SchemaUpdaterStepBase<TableDroper> implements ISchemaUpdaterStep {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TableDroper.class);
	
	private String tableName;
		private boolean includeAudTable;
	
	public static final TableDroper NewInstance(String stepName, String tableName, boolean includeAudTable){
		return new TableDroper(stepName, tableName, includeAudTable);
	}

	
	protected TableDroper(String stepName, String tableName, boolean includeAudTable) {
		super(stepName);
		this.tableName = tableName;
		this.includeAudTable = includeAudTable;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase#invoke(eu.etaxonomy.cdm.database.ICdmDataSource, eu.etaxonomy.cdm.common.IProgressMonitor)
	 */
	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		boolean result = true;
		result &= removeTable(tableName, datasource, monitor);
		if (includeAudTable){
			String aud = "_AUD";
			result &= removeTable(tableName + aud, datasource, monitor);
		}
		return (result == true )? 0 : null;
	}

	private boolean removeTable(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) {
		try {
			String updateQuery = getUpdateQueryString(tableName, datasource, monitor);
			datasource.executeUpdate(updateQuery);
			return true;
		} catch ( DatabaseTypeNotSupportedException e) {
			return false;
		}
	}

	public String getUpdateQueryString(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) throws DatabaseTypeNotSupportedException {
		String updateQuery;
		DatabaseTypeEnum type = datasource.getDatabaseType();
		
		updateQuery = "DROP TABLE @tableName";
		if (type.equals(DatabaseTypeEnum.SqlServer2005)){
			//MySQL allows both syntaxes
//			updateQuery = "ALTER TABLE @tableName ADD @columnName @columnType";
		}else if (type.equals(DatabaseTypeEnum.H2) || type.equals(DatabaseTypeEnum.PostgreSQL) || type.equals(DatabaseTypeEnum.MySQL)){
//			updateQuery = "ALTER TABLE @tableName @addSeparator @columnName @columnType";
		}else{
			updateQuery = null;
			String warning = "Update step '" + this.getStepName() + "' is not supported by " + type.getName();
			monitor.warning(warning);
			throw new DatabaseTypeNotSupportedException(warning);
		}
		updateQuery = updateQuery.replace("@tableName", tableName);
		
		return updateQuery;
	}


}
