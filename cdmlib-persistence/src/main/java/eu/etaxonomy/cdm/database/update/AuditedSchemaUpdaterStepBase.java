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
 * Base class for a {@link ISchemaUpdaterStep schema update step} which supports automated handling
 * of auditing tables.
 *
 * @see CdmUpdater
 * @see ISchemaUpdater
 *
 * @author a.mueller
 *
 * @param <T>
 */
public abstract class AuditedSchemaUpdaterStepBase extends SchemaUpdaterStepBase {

	protected String tableName;
	protected boolean includeAudTable;
	protected boolean isAuditing;

    protected AuditedSchemaUpdaterStepBase(String stepName, boolean includedAudTable) {
        super(stepName);
        this.includeAudTable = includedAudTable;
    }

    protected AuditedSchemaUpdaterStepBase(String stepName, String tableName, boolean includedAudTable) {
        super(stepName);
        this.includeAudTable = includedAudTable;
        this.tableName = tableName;
    }

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {
        isAuditing = false;
		invokeOnTable(caseType.transformTo(tableName), datasource, monitor, caseType, result);
		if (includeAudTable){
			String aud = "_AUD";
			isAuditing = true;
			invokeOnTable(caseType.transformTo(tableName + aud), datasource, monitor, caseType, result);
		}
		return;
	}

	/**
	 * Invoke the update on the given table of name tableName.
	 * @param tableName the tableName, already in the correct case
	 * @param datasource the data source
	 * @param monitor the monitor
	 * @param caseType the caseType (in case other tables are also affected
	 * @param result
	 * @return
	 */
	protected abstract void invokeOnTable(String tableName, ICdmDataSource datasource,
	        IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result);

}
