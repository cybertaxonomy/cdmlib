/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v50_55;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;

/**
/**
 * @author a.mueller
 * @date 09.06.2017
 *
 */
public class SchemaUpdater_55_551 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_55_551.class);
	private static final String startSchemaVersion = "5.5.0.0.20190221";
	private static final String endSchemaVersion = "5.5.1.0.20190301";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_55_551 NewInstance() {
		return new SchemaUpdater_55_551();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_55_551() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		ISchemaUpdaterStep step;
		String newColumnName;
		String query;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();


//		//#6699 delete term version
//		//just in case not fixed before yet
//		stepName = "Delete term version";
//		query = "DELETE FROM @@CdmMetaData@@ WHERE propertyName = 'TERM_VERSION'";
//		step = SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepName, query, -99);
//        stepList.add(step);


        //TODO remove proparte and partial columns

		updateConceptRelationshipSymbolsAgain(stepList);

        return stepList;

	}

    //7514  the update in 50_55 was not yet correct
    private void updateConceptRelationshipSymbolsAgain(List<ISchemaUpdaterStep> stepList) {

        //Update misapplied name symbols
        String stepName = "Update misapplied name symbols again";
        String query = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol='"+UTF8.EM_DASH_DOUBLE+"' , inverseSymbol = '"+UTF8.EN_DASH+"' "
                + " WHERE uuid = '1ed87175-59dd-437e-959e-0d71583d8417' ";
        String tableName = "DefinedTermBase";
        ISchemaUpdaterStep step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, -99);
        stepList.add(step);

        //Update pro parte misapplied name symbols
        stepName = "Update pro parte misapplied name symbols again";
        query = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol='"+UTF8.EM_DASH_DOUBLE+"(p.p.)' , inverseSymbol = '"+UTF8.EN_DASH+"(p.p.)' "
                + " WHERE uuid = 'b59b4bd2-11ff-45d1-bae2-146efdeee206' ";
        tableName = "DefinedTermBase";
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, -99);
        stepList.add(step);

        //Update partial misapplied name symbols
        stepName = "Update partial misapplied name symbols again";
        query = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol='"+UTF8.EM_DASH_DOUBLE+"(part.)' , inverseSymbol = '"+UTF8.EN_DASH+"(part.)' "
                + " WHERE uuid = '859fb615-b0e8-440b-866e-8a19f493cd36' ";
        tableName = "DefinedTermBase";
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, -99);
        stepList.add(step);

    }


    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_50_55.NewInstance();
	}

}
