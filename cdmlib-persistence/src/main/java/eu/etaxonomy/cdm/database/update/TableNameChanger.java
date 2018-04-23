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

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 \* @since 16.09.2010
 *
 */
public class TableNameChanger
            extends SchemaUpdaterStepBase{

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

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {
		invokeOnTable(caseType.transformTo(oldName), caseType.transformTo(newName),
		        datasource, monitor, result);
		updateHibernateSequence(datasource, monitor, newName, oldName); //no result&= as hibernateSequence problems may not lead to a complete fail
		if (includeAudTable){
			String aud = "_AUD";
			invokeOnTable(caseType.transformTo(oldName + aud), caseType.transformTo(newName + aud),
			        datasource, monitor, result);
		}
		return;
	}

	//does not support AuditedSchemaUpdaterStepBase signature
	private void invokeOnTable(String oldName, String newName, ICdmDataSource datasource,
	        IProgressMonitor monitor, SchemaUpdateResult result) {
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
			String message ="Update step '" + this.getStepName() + "' is not supported by " + type.getName();
			monitor.warning(message);
			result.addError(message, getStepName() + ", TableNameChanger.invokeOnTable");
            return;
		}
		updateQuery = updateQuery.replace("@oldName", oldName);
		updateQuery = updateQuery.replace("@newName", newName);
		try {
			datasource.executeUpdate(updateQuery);
		} catch (SQLException e) {
			String message = "Could not perform rename table operation";
			monitor.warning("Could not perform rename table operation", e);
			logger.warn(message+ ": "  + e.getMessage());
			result.addException(e, message, getStepName() + ", TableNameChanger.invokeOnTable");
			return;
		}
		return;
	}

	/**
	 *
	 * @param datasource
	 * @param monitor
	 * @param table
	 * @param oldVal
	 * @return
	 */
	private boolean updateHibernateSequence(ICdmDataSource datasource, IProgressMonitor monitor, String newName, String oldName){
		try{
			//TODO do we need to "case" this table name?
			String sql = " UPDATE hibernate_sequences SET sequence_name = '%s' WHERE sequence_name = '%s'";
			datasource.executeUpdate(String.format(sql, newName ,oldName));
			return true;
		} catch (Exception e) {
			String message = "Exception occurred when trying to read or update hibernate_sequences table for value " + this.newName + ": " + e.getMessage();
			monitor.warning(message, e);
			logger.error(message);
			return false;
		}

	}

}
