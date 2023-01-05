/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @since 16.09.2010
 */
public class ColumnRemover
        extends AuditedSchemaUpdaterStepBase{

    private static final Logger logger = LogManager.getLogger();

	private final String oldColumnName;

	public static final ColumnRemover NewInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String oldColumnName, boolean includeAudTable){
		return new ColumnRemover(stepList, stepName, tableName, oldColumnName, includeAudTable);
	}


	protected ColumnRemover(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String oldColumnName, boolean includeAudTable) {
		super(stepList, stepName, tableName, includeAudTable);
		this.oldColumnName = oldColumnName;
	}

    @Override
    protected void invokeOnTable(String tableName, ICdmDataSource datasource,
            IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) {
        try {
			String updateQuery = getUpdateQueryString(tableName, datasource, monitor);
			datasource.executeUpdate(updateQuery);
			return;
		} catch ( Exception e) {
		    String message = e.getMessage();
			monitor.warning(message);
			logger.warn(e);
            result.addWarning(message, this, "invokeOnTable");
            return;
		}
	}

	public String getUpdateQueryString(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) throws DatabaseTypeNotSupportedException {
		String updateQuery;
		DatabaseTypeEnum type = datasource.getDatabaseType();

		updateQuery = "ALTER TABLE @tableName DROP COLUMN @columnName";
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
		updateQuery = updateQuery.replace("@columnName", oldColumnName);

		return updateQuery;
	}

//	public static String getDropColumnSeperator(ICdmDataSource datasource) throws DatabaseTypeNotSupportedException {
//		DatabaseTypeEnum type = datasource.getDatabaseType();
//		if (type.equals(DatabaseTypeEnum.SqlServer2005)){
//			return "DROP ";
//		}else if (type.equals(DatabaseTypeEnum.H2) || type.equals(DatabaseTypeEnum.PostgreSQL) || type.equals(DatabaseTypeEnum.MySQL)){
//			return "DROP COLUMN ";
//		}else{
//			throw new DatabaseTypeNotSupportedException(datasource.getName());
//		}
//	}

}
