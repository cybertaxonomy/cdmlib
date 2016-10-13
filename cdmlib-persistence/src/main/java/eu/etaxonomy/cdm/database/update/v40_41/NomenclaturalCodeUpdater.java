// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v40_41;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.ITermUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;


/**
 * Updates the CdmPreference NomenclaturalCode  #3658
 *
 * @author a.mueller
 * @date 13.10.2016
 */
public class NomenclaturalCodeUpdater extends SchemaUpdaterStepBase<NomenclaturalCodeUpdater> implements ITermUpdaterStep{
    private static final Logger logger = Logger.getLogger(NomenclaturalCodeUpdater.class);

	private static final String stepName = "Update nomenclatural code";

// **************************** STATIC METHODS ********************************/

	public static final NomenclaturalCodeUpdater NewInstance(List<ISchemaUpdaterStep> stepList){
		NomenclaturalCodeUpdater result = new NomenclaturalCodeUpdater();
		stepList.add(result);
		return result;
	}

	private NomenclaturalCodeUpdater() {
		super(stepName);
	}

	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {

	    String sql = "SELECT count(DTYPE) as n, DTYPE FROM TaxonNameBase GROUP BY DTYPE";
	    ResultSet rs = datasource.executeQuery(sql);
	    if (rs.next()){
//	        int n = rs.getInt(1);
	        String dtype = rs.getString(2);
	        NomenclaturalCode code = NomenclaturalCode.fromString(dtype);

	        if (code != null){
//	            String nomCodeKey = PreferencePredicate.NomenclaturalCode.getKey();
	            String insertSql = " INSERT INTO @@CdmPreference@@ (key_predicate, key_subject, value)"
	                    + " VALUES ('model.name.NC','/','eu.etaxonomy.cdm.model.name.NomenclaturalCode.%s')";

	            insertSql = String.format(insertSql, code.getUuid());
	            datasource.executeUpdate(insertSql);
	            logger.warn("Nomenclatural code updated");
	        }
	    }

	    return 0;
	}



}
