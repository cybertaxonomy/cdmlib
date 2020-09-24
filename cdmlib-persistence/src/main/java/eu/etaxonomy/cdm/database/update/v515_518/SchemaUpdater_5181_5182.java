/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v515_518;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 02.09.2020
 */
public class SchemaUpdater_5181_5182 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5181_5182.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_18_01;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_18_02;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5181_5182 NewInstance() {
		return new SchemaUpdater_5181_5182();
	}

	protected SchemaUpdater_5181_5182() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

//        //6581
		stepName = "Add sourcedName column to NomenclaturalSource";
		tableName = "OriginalSourceBase";
		newColumnName = "sourcedName_id";
		String referencedTable = "TaxonName";
		//TODO handle NotNull
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

		String sql = "UPDATE @@OriginalSourceBase@@ "
		       + " SET sourcedName_id = (SELECT tn.id "
               + "         FROM @@TaxonName@@ tn WHERE tn.nomenclaturalSource_id = @@OriginalSourceBase@@.id) "
               + " WHERE EXISTS ( "
               + "       SELECT * "
               + "       FROM @@TaxonName@@ tn "
               + "       WHERE tn.nomenclaturalSource_id = @@OriginalSourceBase@@.id)";
		String sql_aud = "UPDATE @@OriginalSourceBase_AUD@@ "
	               + " SET sourcedName_id = (SELECT tn.id "
	               + "         FROM @@TaxonName_AUD@@ tn WHERE tn.nomenclaturalSource_id = @@OriginalSourceBase_AUD@@.id) "
	               + " WHERE EXISTS ( "
	               + "       SELECT * "
	               + "       FROM @@TaxonName_AUD@@ tn "
	               + "       WHERE tn.nomenclaturalSource_id = @@OriginalSourceBase_AUD@@.id)";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, sql_aud, -99);

        return stepList;
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5180_5181.NewInstance();
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return SchemaUpdater_5182_5183.NewInstance();
	}
}
