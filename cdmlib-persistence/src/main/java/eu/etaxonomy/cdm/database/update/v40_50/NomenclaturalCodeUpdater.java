/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v40_50;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * Updates the CdmPreference NomenclaturalCode  #3658
 *
 * @author a.mueller
 * @since 13.10.2016
 */
public class NomenclaturalCodeUpdater extends SchemaUpdaterStepBase {

    private static final Logger logger = LogManager.getLogger();

	private static final String stepName = "Update nomenclatural code";

// **************************** STATIC METHODS ********************************/

	public static final NomenclaturalCodeUpdater NewInstance(List<ISchemaUpdaterStep> stepList){
		NomenclaturalCodeUpdater result = new NomenclaturalCodeUpdater(stepList);
		return result;
	}

	private NomenclaturalCodeUpdater(List<ISchemaUpdaterStep> stepList) {
		super(stepList, stepName);
	}

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {

	    String existsSql = " SELECT count(*) FROM @@CdmPreference@@ "
	            + " WHERE key_predicate = 'model.name.NC' AND key_subject = '/'";
	    existsSql = caseType.replaceTableNames(existsSql);
	    if (((Number)datasource.getSingleValue(existsSql)).intValue() > 0 ){
	        return;
	    }

	    String sql = "SELECT count(DTYPE) as n, DTYPE FROM @@TaxonNameBase@@ "
	            + " GROUP BY DTYPE ORDER BY count(DTYPE) DESC ";
	    sql = caseType.replaceTableNames(sql);
	    ResultSet rs = datasource.executeQuery(sql);
	    if (rs.next()){
	        String dtype = rs.getString(2);
	        NomenclaturalCode code = NomenclaturalCode.fromDtype(dtype);

	        if (code != null){
	            String insertSql = " INSERT INTO @@CdmPreference@@ (key_predicate, key_subject, value)"
	                    + " VALUES ('model.name.NC','/','eu.etaxonomy.cdm.model.name.NomenclaturalCode.%s')";

	            insertSql = String.format(insertSql, code.getUuid());
	            insertSql = caseType.replaceTableNames(insertSql);
	            datasource.executeUpdate(insertSql);
	            logger.warn("Nomenclatural code updated");
	        }
	    }
	    return;
	}

}
