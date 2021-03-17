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
 * @date 24.11.2020
 */
public class SchemaUpdater_5185_5186 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5185_5186.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_18_05;
	//FIXME
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_18_06;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5185_5186 NewInstance() {
		return new SchemaUpdater_5185_5186();
	}

	protected SchemaUpdater_5185_5186() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        adaptNomenclaturalStanding(stepList);

		//#9322 remove Invalid Designation taxon relationship
        String stepName = "remove invalid designation taxon relationship type";
        String uuidTerm = "605b1d01-f2b1-4544-b2e0-6f08def3d6ed";
        String checkUsedQueries = "SELECT count(*) FROM @@TaxonRelationship@@ tr "
                + " INNER JOIN @@DefinedTermBase@@ trType ON trType.id = tr.type_id "
                + " WHERE trType.uuid = '605b1d01-f2b1-4544-b2e0-6f08def3d6ed'";
        SingleTermRemover.NewInstance(stepList, stepName, uuidTerm, checkUsedQueries, -99);

        stepName = "remove invalid designation taxon relationship type";
        uuidTerm = "605b1d01-f2b1-4544-b2e0-6f08def3d6ed";
        checkUsedQueries = "SELECT count(*) FROM @@TaxonRelationship_AUD@@ tr "
                + " INNER JOIN @@DefinedTermBase_AUD@@ trType ON trType.id = tr.type_id "
                + " WHERE trType.uuid = '605b1d01-f2b1-4544-b2e0-6f08def3d6ed'";
        SingleTermRemover.NewAudInstance(stepList, stepName, uuidTerm, checkUsedQueries, -99);

        return stepList;
    }

    private void adaptNomenclaturalStanding(List<ISchemaUpdaterStep> stepList) {

        //OTHER DESIGNATIONS
        String stepName = "Set nom status and name relationship types to OTHER DESIGNATIONS where appropriate";
        String nonAuditedTableName = "DefinedTermBase";
        String defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET nomenclaturalStanding = 'NO' "
                + " WHERE uuid IN ( "
                + "     '24955174-aa5c-4e71-a2fd-3efc79e885db'"   //status nom. confus.
                + "    ,'90f5012b-705b-4488-b4c6-002d2bc5198e'"   //status nom. ambig.
                + "    ) ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName, -99);
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5184_5185.NewInstance();
    }
}