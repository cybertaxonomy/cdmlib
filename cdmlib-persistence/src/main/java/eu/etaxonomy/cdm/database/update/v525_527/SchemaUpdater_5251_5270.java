/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v525_527;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.database.update.v523_525.SchemaUpdater_5250_5251;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 22.04.2021
 */
public class SchemaUpdater_5251_5270 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5251_5270.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_25_01;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_27_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5251_5270 NewInstance() {
		return new SchemaUpdater_5251_5270();
	}

	protected SchemaUpdater_5251_5270() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5250_5251.NewInstance();
    }

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//add cultivarGroup
		//#9761
		stepName = "Add cultivarGroup";
		tableName = "TaxonName";
		newColumnName = "cultivarGroup";
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

		//TODO update where rank = CultivarGroup

		//#9755 Add Gp abbreviation to cultivar group rank
		stepName = "Add abbrev to cultivar group rank";
		UUID uuidTerm = UUID.fromString("d763e7d3-e7de-4bb1-9d75-225ca6948659");
		UUID uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		TermRepresentationUpdater.NewInstance(stepList, stepName, uuidTerm, null, null, "Gp", uuidLanguage, true);

        return stepList;
    }

}