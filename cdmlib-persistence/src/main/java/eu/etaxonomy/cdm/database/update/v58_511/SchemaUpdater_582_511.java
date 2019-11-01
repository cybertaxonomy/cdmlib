/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v58_511;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TableDroper;
import eu.etaxonomy.cdm.database.update.v55_58.SchemaUpdater_581_582;

/**
 * @author a.mueller
 * @date 01.11.2019
 */
public class SchemaUpdater_582_511 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_582_511.class);

	private static final String endSchemaVersion = "5.11.0.0.20191104";
	private static final String startSchemaVersion = "5.8.2.0.20190815";

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_582_511 NewInstance() {
		return new SchemaUpdater_582_511();
	}

	protected SchemaUpdater_582_511() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //#8625 Add count column to StateData
        stepName = "Add count column to StateData";
        tableName = "StateData";
        newColumnName ="number";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, null, !NOT_NULL);

        //#8466
        deleteFeatureTreeTables(stepList);

        return stepList;
    }

    private void deleteFeatureTreeTables(List<ISchemaUpdaterStep> stepList) {
        //#8466
        String tableName = "FeatureTree_Annotation";
        String stepName = "Drop table " + tableName;
        TableDroper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT, true);

        tableName = "FeatureTree_Credit";
        stepName = "Drop table " + tableName;
        TableDroper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT, true);

        tableName = "FeatureTree_Extension";
        stepName = "Drop table " + tableName;
        TableDroper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT, true);

        tableName = "FeatureTree_Identifier";
        stepName = "Drop table " + tableName;
        TableDroper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT, true);

        tableName = "FeatureTree_Marker";
        stepName = "Drop table " + tableName;
        TableDroper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT, true);

        tableName = "FeatureTree_OriginalSourceBase";
        stepName = "Drop table " + tableName;
        TableDroper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT, true);

        tableName = "FeatureTree_Representation";
        stepName = "Drop table " + tableName;
        TableDroper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT, true);

        tableName = "FeatureTree_RightsInfo";
        stepName = "Drop table " + tableName;
        TableDroper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT, true);

        tableName = "FeatureTree";
        stepName = "Drop table " + tableName;
        TableDroper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT, true);

    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_581_582.NewInstance();
	}
}
