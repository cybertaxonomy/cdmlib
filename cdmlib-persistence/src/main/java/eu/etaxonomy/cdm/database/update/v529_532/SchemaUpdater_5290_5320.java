/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v529_532;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.database.update.v527_529.SchemaUpdater_5271_5290;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 08.07.2022
 */
public class SchemaUpdater_5290_5320 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(SchemaUpdater_5290_5320.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_29_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_32_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5290_5320 NewInstance() {
		return new SchemaUpdater_5290_5320();
	}

	protected SchemaUpdater_5290_5320() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5271_5290.NewInstance();
    }

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#10083 Update inverse representation of 'is blocking name for'
		stepName = "Update inverse representation of 'is blocking name for'";
		UUID uuidTerm = UUID.fromString("1dab357f-2e12-4511-97a4-e5153589e6a6");
		String description = "has blocking name";
		String label = "has blocking name";
		String abbrev = null;
		UUID uuidEnglish = Language.uuidEnglish;
		TermRepresentationUpdater.NewInverseInstance(stepList, stepName, uuidTerm, description, label, abbrev, uuidEnglish);

	    //#10057 add accessed columns
        stepName = "Add accessed_start";
        tableName = "OriginalSourceBase";
        String newColumnName = "accessed_start";
        int size = 50;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, size, INCLUDE_AUDIT);

        stepName = "Add accessed_end";
        newColumnName = "accessed_end";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, size, INCLUDE_AUDIT);

        stepName = "Add accessed_freetext";
        newColumnName = "accessed_freetext";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);


		return stepList;
    }
}