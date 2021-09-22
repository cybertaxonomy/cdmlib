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
public class SchemaUpdater_5270_5271 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5270_5271.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_27_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_27_01;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5270_5271 NewInstance() {
		return new SchemaUpdater_5270_5271();
	}

	protected SchemaUpdater_5270_5271() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5250_5251.NewInstance();
    }

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

	    //#9755 Adapt grex representation
        stepName = "Adapt representation for grex (infraspec.)";
        UUID uuidTerm = UUID.fromString("08dcb4ff-ac58-48a3-93af-efb3d836ac84");
        String description = "Grex as used e.g. for Hieracium by German school (Zahn et al.)";
        String label = "Grex (infraspec.)";
        String abbrev = "gx";
        UUID uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
        TermRepresentationUpdater.NewInstance(stepList, stepName, uuidTerm, description, label, abbrev, uuidLanguage, true);

        //#9755 Adapt grex representation
        stepName = "Adapt proles abbreviation";
        uuidTerm = UUID.fromString("8810d1ba-6a34-4ae3-a355-919ccd1cd1a5");
        abbrev = "proles";
        uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
        TermRepresentationUpdater.NewInstance(stepList, stepName, uuidTerm, null, null, abbrev, uuidLanguage, true);


        return stepList;
    }
}