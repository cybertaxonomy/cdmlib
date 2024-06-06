/**
 * Copyright (C) 2023 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v54x_54x;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 2024-06-04
 */
public class SchemaUpdater_5430_5431 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_43_00;
	//TODO
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_43_01;

// ********************** FACTORY METHOD *************************************

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5401_5430.NewInstance();
    }

	public static SchemaUpdater_5430_5431 NewInstance() {
		return new SchemaUpdater_5430_5431();
	}

	SchemaUpdater_5430_5431() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
//		String columnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#10313 Update default colors
		stepName = "Update default color for 'present'";
		String sql = "UPDATE DefinedTermBase "
		          + " SET defaultColor = '4daf4a' "
		          + " WHERE uuid = 'cef81d25-501c-48d8-bbea-542ec50de2c2' AND defaultColor = '8dd3c7'";
		tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        stepName = "Update default color for 'native'";
        sql = "UPDATE DefinedTermBase "
                + " SET defaultColor = '4daf4a' "
                + " WHERE uuid = 'ddeac4f2-d8fa-43b8-ad7e-ca13abdd32c7' AND defaultColor = 'ffffb3'";
        tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        stepName = "Update default color for 'native: doubtfully native'";
        sql = "UPDATE DefinedTermBase "
                + " SET defaultColor = '377eb8' "
                + " WHERE uuid = '310373bf-7df4-4d02-8cb3-bcc7448805fc' AND defaultColor = '80b1d3'";
        tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        stepName = "Update default color for 'cultivated'";
        sql = "UPDATE DefinedTermBase "
                + " SET defaultColor = '984ea3' "
                + " WHERE uuid = '9eb99fe6-59e2-4445-8e6a-478365bd0fa9' AND defaultColor = 'b3de69'";
        tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        stepName = "Update default color for 'introduced'";
        sql = "UPDATE DefinedTermBase "
                + " SET defaultColor = 'ff7f00' "
                + " WHERE uuid = '643cf9d1-a5f1-4622-9837-82ef961e880b' AND defaultColor = 'd9d9d9'";
        tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        stepName = "Update default color for 'introduced adventious'";
        sql = "UPDATE DefinedTermBase "
                + " SET defaultColor = 'ffff33' "
                + " WHERE uuid = '42946bd6-9c22-45ad-a910-7427e8f60bfd' AND defaultColor = 'ffed61'";
        tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        stepName = "Update default color for 'introduced cultivated'";
        sql = "UPDATE DefinedTermBase "
                + " SET defaultColor = 'a65628' "
                + " WHERE uuid = 'fac8c347-8262-44a1-b0a4-db4de451c021' AND defaultColor = '555555'";
        tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        stepName = "Update default color for 'naturalised'";
        sql = "UPDATE DefinedTermBase "
                + " SET defaultColor = 'f781bf' "
                + " WHERE uuid = 'e191e89a-a751-4b0c-b883-7f1de70915c9' AND defaultColor = 'ff0d6f'";
        tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        return stepList;
    }
}