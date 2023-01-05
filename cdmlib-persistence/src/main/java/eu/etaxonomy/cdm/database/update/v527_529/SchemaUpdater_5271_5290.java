/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v527_529;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.database.update.v525_527.SchemaUpdater_5270_5271;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 27.10.2021
 */
public class SchemaUpdater_5271_5290 extends SchemaUpdaterBase {

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_27_01;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_29_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5271_5290 NewInstance() {
		return new SchemaUpdater_5271_5290();
	}

	protected SchemaUpdater_5271_5290() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5270_5271.NewInstance();
    }

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#2506 status for specimen
		stepName = "Add status for specimen";
		tableName = "OccurrenceStatus";
		String[] columnNames = new String[]{"unit_id","type_id"};
        String[] columnTypes = new String[]{"int","int"};
        String[] referencedTables = new String[]{"SpecimenOrObservationBase","DefinedTermBase"};
		TableCreator.NewSingleSourcedInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT);

        return stepList;
    }
}