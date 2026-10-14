/**
 * Copyright (C) 2024 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v601_60x;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TableDropper;
import eu.etaxonomy.cdm.database.update.v540_558.SchemaUpdater_5540_5580;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 2026-10-08
 */
public class SchemaUpdater_6010_60x0 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_06_01_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_06_01_00;

// ********************** FACTORY METHOD *************************************

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5540_5580.NewInstance();
    }

	public static SchemaUpdater_6010_60x0 NewInstance() {
		return new SchemaUpdater_6010_60x0();
	}

	SchemaUpdater_6010_60x0() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

	    //#3556
		//drop MediaRepresentation_MediaRepresentationPart_AUD
        stepName = "Drop MediaRepresentation_MediaRepresentationPart_AUD";
        String tableName = "MediaRepresentation_MediaRepresentationPart_AUD";
        TableDropper.NewInstance(stepList, stepName, tableName, !INCLUDE_AUDIT);

        //MediaRepresentationPart
        stepName = "Drop MediaRepresentationPart";
        tableName = "MediaRepresentationPart";
        TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

        return stepList;
    }
}