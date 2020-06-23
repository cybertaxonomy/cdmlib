/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v512_515;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 10.06.2020
 */
public class SchemaUpdater_5150_5151 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5150_5151.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_15_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_15_01;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5150_5151 NewInstance() {
		return new SchemaUpdater_5150_5151();
	}

	protected SchemaUpdater_5150_5151() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String columnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

	    //#9037 Remove column supportsXXX
        stepName = "Remove column supportscategoricaldata";
        tableName = "DefinedTermBase";
        columnName = "supportscategoricaldata";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        stepName = "Remove column supportscommontaxonname";
        columnName = "supportscommontaxonname";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        stepName = "Remove column supportsdistribution";
        columnName = "supportsdistribution";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        stepName = "Remove column supportsindividualassociation";
        columnName = "supportsindividualassociation";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        stepName = "Remove column supportsquantitativedata";
        columnName = "supportsquantitativedata";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        stepName = "Remove column supportstaxoninteraction";
        columnName = "supportstaxoninteraction";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        stepName = "Remove column supportstextdata";
        columnName = "supportstextdata";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        //#9036 Remove column unplaced, excluded, doubtful
        stepName = "Remove column excluded";
        tableName = "TaxonNode";
        columnName = "excluded";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        stepName = "Remove column unplaced";
        columnName = "unplaced";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        stepName = "Remove column doubtful";
        columnName = "doubtful";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        //#9009
        stepName = "Create MediaMetaData table";
        tableName = "MediaMetaData";
        String[] columnNames = new String[]{"pairkey","pairvalue","mediarepresentation_id"};
        String[] columnTypes = new String[]{"string_255","string_255","int"};
        String[] referencedTables = new String[]{null,null,"MediaRepresentationPart"};
        TableCreator.NewAuditedCdmBaseInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables);

        return stepList;
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5120_5150.NewInstance();
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return SchemaUpdater_5151_5152.NewInstance();
	}
}
