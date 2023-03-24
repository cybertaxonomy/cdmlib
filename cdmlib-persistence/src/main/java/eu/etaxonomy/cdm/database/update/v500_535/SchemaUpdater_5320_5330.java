/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v500_535;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 30.09.2022
 */
public class SchemaUpdater_5320_5330 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_32_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_33_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5320_5330 NewInstance() {
		return new SchemaUpdater_5320_5330();
	}

	protected SchemaUpdater_5320_5330() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5290_5320.NewInstance();
    }

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#10163
        stepName = "Change label + desc. 'Conserved Desig' => 'Conservation Designated'";
        String uuidTerm = "4e9c9702-a74d-4033-9d47-792ad123712c";
        String description = "Conservation Designated";
        String label = "Conservation designated";
        String abbrev = null;
        UUID uuidLatin = Language.uuidLatin;  //not really true but all nom. status currently use Latin representations
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, UUID.fromString(uuidTerm), description, label, abbrev, uuidLatin);

        stepName = "Revert label + desc. for orth. cons. from incorrect 'Conservation Designated' => 'Orthography Conserved'";
        uuidTerm = "34a7d383-988b-4117-b8c0-52b947f8c711";
        description = "Orthography Conserved";
        label = "Orthography Conserved";
//        abbrev = null;
        uuidLatin = Language.uuidLatin;  //not really true but all nom. status currently use Latin representations
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, UUID.fromString(uuidTerm), description, label, abbrev, uuidLatin);

		return stepList;
    }
}