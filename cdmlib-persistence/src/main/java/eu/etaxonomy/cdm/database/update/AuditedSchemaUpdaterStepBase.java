package eu.etaxonomy.cdm.database.update;

import java.sql.SQLException;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

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
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		boolean result = true;
		isAuditing = false;
		result &= invokeOnTable(tableName, datasource, monitor);
		if (includeAudTable){
			String aud = "_AUD";
			isAuditing = true;
			result &= invokeOnTable(tableName + aud, datasource, monitor);
		}
		return (result == true )? 0 : null;
	}
	
	protected abstract boolean invokeOnTable(String tableName, ICdmDataSource datasource, IProgressMonitor monitor);

}
