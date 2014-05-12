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

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 *
 * @param <T>
 */
public abstract class AuditedSchemaUpdaterStepBase<T extends AuditedSchemaUpdaterStepBase<T>> extends SchemaUpdaterStepBase<T> implements ISchemaUpdaterStep {

	protected String tableName;
	protected boolean includeAudTable;
	protected boolean isAuditing;

	
	/**
	 * Constructor
	 * @param stepName
	 */
	protected AuditedSchemaUpdaterStepBase(String stepName) {
		super(stepName);
	}

	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		boolean result = true;
		isAuditing = false;
		result &= invokeOnTable(caseType.transformTo(tableName), datasource, monitor, caseType);
		if (includeAudTable){
			String aud = "_AUD";
			isAuditing = true;
			result &= invokeOnTable(caseType.transformTo(tableName + aud), datasource, monitor, caseType);
		}
		return (result == true )? 0 : null;
	}
	
	/**
	 * Invoke the update on the given table of name tableName.
	 * @param tableName the tableName, already in the correct case
	 * @param datasource the data source
	 * @param monitor the monitor
	 * @param caseType the caseType (in case other tables are also affected
	 * @return
	 */
	protected abstract boolean invokeOnTable(String tableName, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType);

}
