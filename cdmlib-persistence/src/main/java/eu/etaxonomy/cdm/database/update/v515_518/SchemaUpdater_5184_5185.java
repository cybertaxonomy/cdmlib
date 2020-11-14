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
import eu.etaxonomy.cdm.database.update.SingleTermRemover;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 03.11.2020
 */
public class SchemaUpdater_5184_5185 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5184_5185.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_18_04;
	//FIXME
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_18_04;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5184_5185 NewInstance() {
		return new SchemaUpdater_5184_5185();
	}

	protected SchemaUpdater_5184_5185() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //#6591
        OriginalSpellingMover.NewInstance(stepList);

        String stepName = "remove original spelling name relationship type";
        String uuidTerm = "264d2be4-e378-4168-9760-a9512ffbddc4";
        String checkUsedQueries = "SELECT count(*) FROM @@NameRelationship@@ nr "
                + " INNER JOIN @@DefinedTermBase@@ nrType ON nrType.id = nr.type_id "
                + " WHERE nrType.uuid = '264d2be4-e378-4168-9760-a9512ffbddc4'";
        SingleTermRemover.NewInstance(stepList, stepName, uuidTerm, checkUsedQueries, -99);

        return stepList;
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5183_5184.NewInstance();
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}
}