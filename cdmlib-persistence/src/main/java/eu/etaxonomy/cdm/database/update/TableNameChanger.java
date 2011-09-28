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
public class TableNameChanger extends SchemaUpdaterStepBase<TableNameChanger> implements ISchemaUpdaterStep {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TableNameChanger.class);
	
	private String oldName;
	private String newName;
	private boolean includeAudTable;
	
	public static final TableNameChanger NewInstance(String stepName, String oldName, String newName, boolean includeAudTable){
		return new TableNameChanger(stepName, oldName, newName, includeAudTable);
	}
	
	protected TableNameChanger(String stepName, String oldName, String newName, boolean includeAudTable) {
		super(stepName);
		this.oldName = oldName;
		this.newName = newName;
		this.includeAudTable = includeAudTable;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase#invoke(eu.etaxonomy.cdm.database.ICdmDataSource, eu.etaxonomy.cdm.common.IProgressMonitor)
	 */
	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		boolean result = true;
		result &= renameTable(oldName, newName, datasource, monitor);
		if (includeAudTable){
			String aud = "_AUD";
			result &= renameTable(oldName + aud, newName + aud, datasource, monitor);
		}
		return (result == true )? 0 : null;
	}

	private boolean renameTable(String oldName, String newName, ICdmDataSource datasource, IProgressMonitor monitor) {
		DatabaseTypeEnum type = datasource.getDatabaseType();
		String updateQuery;
		if (type.equals(DatabaseTypeEnum.MySQL)){
			//MySQL allows both syntaxes
			updateQuery = "RENAME TABLE @oldName TO @newName";
		}else if (type.equals(DatabaseTypeEnum.H2) || type.equals(DatabaseTypeEnum.PostgreSQL) || type.equals(DatabaseTypeEnum.MySQL)){
			updateQuery = "ALTER TABLE @oldName RENAME TO @newName";
		}else if (type.equals(DatabaseTypeEnum.SqlServer2005)){
			updateQuery = "EXEC sp_rename '@oldName', '@newName'";
		}else{
			updateQuery = null;
			monitor.warning("Update step '" + this.getStepName() + "' is not supported by " + type.getName());
			return false;
		}
		updateQuery = updateQuery.replace("@oldName", oldName);
		updateQuery = updateQuery.replace("@newName", newName);
		try {
			datasource.executeQuery(updateQuery);
		} catch (SQLException e) {
			monitor.warning("Could not perform rename table operation", e);
		}
		return true;
	}

}
