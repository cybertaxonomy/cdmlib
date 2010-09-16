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
public class ColumnAdder extends SchemaUpdaterStepBase implements ISchemaUpdaterStep {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ColumnAdder.class);
	
	private String tableName;
	private String newColumnName;
	private String columnType;
	private boolean includeAudTable;
	
	public static final ColumnAdder NewIntegerInstance(String stepName, String tableName, String newColumnName, boolean includeAudTable){
		return new ColumnAdder(stepName, tableName, newColumnName, "int", includeAudTable);
	}
	
	protected ColumnAdder(String stepName, String tableName, String newColumnName, String columnType, boolean includeAudTable) {
		super(stepName);
		this.tableName = tableName;
		this.newColumnName = newColumnName;
		this.columnType = columnType;
		this.includeAudTable = includeAudTable;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase#invoke(eu.etaxonomy.cdm.database.ICdmDataSource, eu.etaxonomy.cdm.common.IProgressMonitor)
	 */
	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		boolean result = true;
		result &= addColumn(tableName, newColumnName, columnType, datasource, monitor);
		if (includeAudTable){
			String aud = "_AUD";
			result &= addColumn(tableName + aud, newColumnName, columnType, datasource, monitor);
		}
		return (result == true )? 0 : null;
	}

	private boolean addColumn(String tableName, String newColumnName, String columnType, ICdmDataSource datasource, IProgressMonitor monitor) {
		DatabaseTypeEnum type = datasource.getDatabaseType();
		String updateQuery;
		if (type.equals(DatabaseTypeEnum.SqlServer2005)){
			//MySQL allows both syntaxes
			updateQuery = "ALTER TABLE @tableName ADD @columnName @columnType";
		}else if (type.equals(DatabaseTypeEnum.H2) || type.equals(DatabaseTypeEnum.PostgreSQL) || type.equals(DatabaseTypeEnum.MySQL)){
			updateQuery = "ALTER TABLE @tableName ADD COLUMN @columnName @columnType";
		}else{
			updateQuery = null;
			monitor.warning("Update step '" + this.getStepName() + "' is not supported by " + type.getName());
			return false;
		}
		updateQuery = updateQuery.replace("@tableName", tableName);
		updateQuery = updateQuery.replace("@columnName", newColumnName);
		updateQuery = updateQuery.replace("@columnType", columnType);
		datasource.executeUpdate(updateQuery);
		return true;
	}

}
