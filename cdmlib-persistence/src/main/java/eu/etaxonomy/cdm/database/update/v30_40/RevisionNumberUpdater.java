/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v30_40;

import java.sql.SQLException;
import java.util.List;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;


/**
 * Updates the xxxObj_type field in Annotations, Markers, Extensions, Identifiers.
 * Not needed anymore as long as we gave up bidirectionality #5743
 *
 * @author a.mueller
 * @since 25.04.2016
 */
public class RevisionNumberUpdater extends SchemaUpdaterStepBase{


// **************************** STATIC METHODS ********************************/

	public static final RevisionNumberUpdater NewInstance(List<ISchemaUpdaterStep> stepList, String stepName){
		RevisionNumberUpdater result = new RevisionNumberUpdater(stepList, stepName);
		return result;
	}

	private RevisionNumberUpdater(List<ISchemaUpdaterStep> stepList, String stepName) {
		super(stepList, stepName);
	}

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {

	    String sql;
	    String casedAuditTable = caseType.transformTo("AuditEvent");

	    DatabaseTypeEnum databaseType = datasource.getDatabaseType();
	    if (databaseType == DatabaseTypeEnum.MySQL){
	        sql = "ALTER TABLE "+casedAuditTable+" ALTER revisionnumber DROP DEFAULT";  //needed? There was no default before
	        datasource.executeUpdate(sql);
	        sql = "ALTER TABLE "+casedAuditTable+" CHANGE COLUMN revisionnumber revisionnumber INT(11) NOT NULL FIRST";
	        datasource.executeUpdate(sql);
	    }else if (databaseType == DatabaseTypeEnum.H2){
	        sql = "ALTER TABLE "+casedAuditTable+" ALTER COLUMN revisionnumber INT NOT NULL";
	        datasource.executeUpdate(sql);
        }else if (databaseType == DatabaseTypeEnum.PostgreSQL){
	        //NOTHING TO DO
        }else if (databaseType == DatabaseTypeEnum.SqlServer2005){
            throw new RuntimeException("SQLServer not supported by RevisionNumberUpdater");
	    }else{
	        throw new RuntimeException("Database type " + databaseType.toString() + " not supported by RevisionNumberUpdater");
	    }
	    return;
	}



}
