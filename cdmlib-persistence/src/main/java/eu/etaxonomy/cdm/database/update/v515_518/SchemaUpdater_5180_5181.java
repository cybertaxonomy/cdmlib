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

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 02.09.2020
 */
public class SchemaUpdater_5180_5181 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5180_5181.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_18_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_18_01;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5180_5181 NewInstance() {
		return new SchemaUpdater_5180_5181();
	}

	protected SchemaUpdater_5180_5181() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //6581
        //move nomenclatural status reference to source
        stepName = "rename NomenclaturalSource";
        String sql = "UPDATE @@OriginalSourceBase@@ "
                + " SET DTYPE = 'NomenclaturalSource', sourceType='NOR' "
                + " WHERE id IN (SELECT nomenclaturalSource_id FROM @@TaxonName@@ WHERE nomenclaturalSource_id IS NOT NULL) ";
        String sql_aud = "UPDATE @@OriginalSourceBase_AUD@@  SET DTYPE = 'NomenclaturalSource', sourceType='NOR'  WHERE id IN (SELECT nomenclaturalSource_id FROM @@TaxonName_AUD@@ WHERE nomenclaturalSource_id IS NOT NULL)";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, sql_aud, -99);

        return stepList;
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5152_5180.NewInstance();
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return SchemaUpdater_5181_5182.NewInstance();
	}
}
