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
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 20.10.2020
 */
public class SchemaUpdater_5183_5184 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5183_5184.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_18_03;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_18_04;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5183_5184 NewInstance() {
		return new SchemaUpdater_5183_5184();
	}

	protected SchemaUpdater_5183_5184() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		ProtologMover.NewInstance(stepList);

        return stepList;
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5182_5183.NewInstance();
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}
}
