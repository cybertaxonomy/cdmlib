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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.v525_527.SchemaUpdater_5270_5271;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 27.10.2021
 */
public class SchemaUpdater_5271_5290 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5271_5290.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_27_01;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_27_01;

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

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        return stepList;
    }
}