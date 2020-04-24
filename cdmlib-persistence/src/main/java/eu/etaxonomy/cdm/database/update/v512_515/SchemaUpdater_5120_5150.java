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
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.database.update.v511_512.SchemaUpdater_5112_5120;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 02.12.2019
 */
public class SchemaUpdater_5120_5150 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5120_5150.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_12_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_15_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5120_5150 NewInstance() {
		return new SchemaUpdater_5120_5150();
	}

	protected SchemaUpdater_5120_5150() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;
		String columnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        return stepList;
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5112_5120.NewInstance();
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

}
