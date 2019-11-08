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

/**
 * @author a.mueller
 * @date 01.11.2019
 */
public class SchemaUpdater_511_5111 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_511_5111.class);

	private static final String startSchemaVersion = "5.11.0.0.20191104";
	private static final String endSchemaVersion = "5.11.1.0.20191108";

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_511_5111 NewInstance() {
		return new SchemaUpdater_511_5111();
	}

	protected SchemaUpdater_511_5111() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //#8628 Add ORCID to person class
        stepName = "Add ORCID to person class";
        tableName = "AgentBase";
        newColumnName ="orcid";
        int length = 16;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

        return stepList;
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_582_511.NewInstance();
	}
}
