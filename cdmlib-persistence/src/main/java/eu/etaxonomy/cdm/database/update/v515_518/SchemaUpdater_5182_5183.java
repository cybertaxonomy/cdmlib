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

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 02.09.2020
 */
public class SchemaUpdater_5182_5183 extends SchemaUpdaterBase {

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_18_02;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_18_03;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5182_5183 NewInstance() {
		return new SchemaUpdater_5182_5183();
	}

	protected SchemaUpdater_5182_5183() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#9124
		AltitudeMeterCorrector.NewInstance(stepList);

        return stepList;
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5181_5182.NewInstance();
    }
}