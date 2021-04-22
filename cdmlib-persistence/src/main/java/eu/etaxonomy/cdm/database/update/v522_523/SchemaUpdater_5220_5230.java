/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v522_523;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TableDropper;
import eu.etaxonomy.cdm.database.update.v518_522.SchemaUpdater_5186_5220;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 22.04.2021
 */
public class SchemaUpdater_5220_5230 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5220_5230.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_22_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_23_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5220_5230 NewInstance() {
		return new SchemaUpdater_5220_5230();
	}

	protected SchemaUpdater_5220_5230() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;
		String referencedTable;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //#9536
        //TaxonBase.secMicroReference
        stepName = "Remove TaxonBase.secMicroReference";
        tableName = "TaxonBase";
        String oldColumnName = "secMicroReference";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //TaxonBase.sec_id
        stepName = "Remove TaxonBase.sec_id";
        tableName = "TaxonBase";
        oldColumnName = "sec_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //Classification.microreference
        stepName = "Remove Classification.microreference";
        tableName = "Classification";
        oldColumnName = "microreference";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //Classification.reference_id
        stepName = "Remove Classification.reference_id";
        tableName = "Classification";
        oldColumnName = "reference_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //DescriptionElementBase_OriginalSourceBase
        stepName = "Remove table DescriptionElementBase_OriginalSourceBase ";
        tableName = "DescriptionElementBase_OriginalSourceBase";
        TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

        //TaxonName_NomenclaturalStatus
        stepName = "Remove table TaxonName_NomenclaturalStatus ";
        tableName = "TaxonName_NomenclaturalStatus";
        TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

        return stepList;
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5186_5220.NewInstance();
    }
}