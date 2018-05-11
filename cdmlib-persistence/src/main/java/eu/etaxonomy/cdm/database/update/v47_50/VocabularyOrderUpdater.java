/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v47_50;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;


/**
 * Updates the CdmPreference NomenclaturalCode  #3658
 *
 * @author a.mueller
 * @since 13.10.2016
 */
public class VocabularyOrderUpdater extends SchemaUpdaterStepBase {
    private static final Logger logger = Logger.getLogger(VocabularyOrderUpdater.class);

	private static final String stepName = "Update vocabulary order";

	private List<String[]> data = new ArrayList<>();

// **************************** STATIC METHODS ********************************/

	public static final VocabularyOrderUpdater NewInstance(List<ISchemaUpdaterStep> stepList){
		VocabularyOrderUpdater result = new VocabularyOrderUpdater();
		stepList.add(result);
		return result;
	}

	private VocabularyOrderUpdater() {
		super(stepName);
	}

	public void add(String uuid, Integer id){
	    data.add(new String[]{uuid, String.valueOf(id)});
	}

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {

        try{
            for (String[] dat : data){
                invokeSingle(datasource, monitor, caseType, result, dat);
            }

        } catch (Exception e) {
            String message = e.getMessage();
            monitor.warning(message, e);
            result.addException(e, message, this, "invoke");
        }

	    return;
	}

    /**
     * @param datasource
     * @param monitor
     * @param caseType
     * @param result
     * @param dat
     * @throws SQLException
     */
    private void invokeSingle(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result, String[] dat) throws SQLException {
        String query = "UPDATE DefinedTermBase "
                + " SET orderIndex = " + dat[1] + " "
                + " WHERE uuid = '" + dat[0] + "'";
        datasource.executeUpdate(query);

    }

}
