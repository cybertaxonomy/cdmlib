/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v522_523;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableDropper;
import eu.etaxonomy.cdm.database.update.v518_522.SchemaUpdater_5186_5220;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 22.04.2021
 */
public class SchemaUpdater_5220_5230 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5220_5230.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_22_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_23_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5220_5230 NewInstance() {
		return new SchemaUpdater_5220_5230();
	}

	protected SchemaUpdater_5220_5230() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //#9536
        //TaxonBase.secMicroReference
        stepName = "Remove TaxonBase.secMicroReference";
        tableName = "TaxonBase";
        String oldColumnName = "secMicroReference";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //TaxonBase.sec_id
        stepName = "Remove TaxonBase.sec_id";
        tableName = "TaxonBase";
        oldColumnName = "sec_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //Classification.microreference
        stepName = "Remove Classification.microreference";
        tableName = "Classification";
        oldColumnName = "microreference";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //Classification.reference_id
        stepName = "Remove Classification.reference_id";
        tableName = "Classification";
        oldColumnName = "reference_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //DescriptionElementBase_OriginalSourceBase
        stepName = "Remove table DescriptionElementBase_OriginalSourceBase ";
        tableName = "DescriptionElementBase_OriginalSourceBase";
        TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

        //TaxonName_NomenclaturalStatus
        stepName = "Remove table TaxonName_NomenclaturalStatus ";
        tableName = "TaxonName_NomenclaturalStatus";
        TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

        //#9613 fix uuids for roles
        stepName = "Set uuid for role project manager";
        String query = "UPDATE @@GrantedAuthorityImpl@@ "
                + " SET uuid = '9eabd2c6-0590-4a1e-95f5-99cc58b63aa7' "
                + " WHERE authority = 'ROLE_PROJECT_MANAGER'";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, -99);

        stepName = "Set uuid for role user manager";
        query = "UPDATE @@GrantedAuthorityImpl@@ "
                + " SET uuid = '74d340a9-b472-4b97-b52a-c140e27a5c76' "
                + " WHERE authority = 'ROLE_USER_MANAGER'";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, -99);

        stepName = "Set uuid for role publish";
        query = "UPDATE @@GrantedAuthorityImpl@@ "
                + " SET uuid = '9ffa7879-cc67-4592-a14a-b251cccde1a7' "
                + " WHERE authority = 'ROLE_PUBLISH'";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, -99);

        //probably fixed already, but just in case
        stepName = "Set uuid for role project manager";
        query = "UPDATE @@GrantedAuthorityImpl@@ "
                + " SET uuid = 'be004bf6-0498-48e3-9f06-ff93fc9cdc9a' "
                + " WHERE authority = 'ROLE_REMOTING'";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, -99);

        //#9614 use underscore for role based group names
        stepName = "Use underscore for group Editor_Extended_Create";
        query = "UPDATE @@PermissionGroup@@ "
                + " SET name = 'Editor_Extended_Create' "
                + " WHERE name = 'EditorExtendedCreate'";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, -99);

        stepName = "Use underscore for group Project_Manager";
        query = "UPDATE @@PermissionGroup@@ "
                + " SET name = 'Project_Manager' "
                + " WHERE name = 'ProjectManager'";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, -99);

        stepName = "Use underscore for group Editor_Reference";
        query = "UPDATE @@PermissionGroup@@ "
                + " SET name = 'Editor_Reference' "
                + " WHERE name = 'Editor-Reference'";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, -99);

        //#9619
        stepName = "Add Taxon.conceptId";
        tableName = "TaxonBase";
        newColumnName = "conceptId";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);


        //#9619
        stepName = "Add Taxon.conceptDefinitions";
        tableName = "TaxonBase";
        newColumnName = "conceptDefinitions";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        //#9619
        stepName = "Add Taxon.conceptStatus";
        tableName = "TaxonBase";
        newColumnName = "conceptStatus";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        //#9619
        stepName = "Add Taxon.taxonTypes";
        tableName = "TaxonBase";
        newColumnName = "taxonTypes";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        //#9619
        stepName = "Add Taxon.currentConceptPeriod_start";
        tableName = "TaxonBase";
        newColumnName = "currentConceptPeriod_start";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        stepName = "Add Taxon.currentConceptPeriod_end";
        tableName = "TaxonBase";
        newColumnName = "currentConceptPeriod_end";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        stepName = "Add Taxon.currentConceptPeriod_freetext";
        tableName = "TaxonBase";
        newColumnName = "currentConceptPeriod_freetext";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        return stepList;
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5186_5220.NewInstance();
    }
}