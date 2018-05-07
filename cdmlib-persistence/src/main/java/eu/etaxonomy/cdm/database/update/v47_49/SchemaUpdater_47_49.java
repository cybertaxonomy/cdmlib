/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v47_49;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.database.update.v41_47.SchemaUpdater_41_47;

/**
/**
 * @author a.mueller
 * @date 09.06.2017
 *
 */
public class SchemaUpdater_47_49 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_47_49.class);
	private static final String endSchemaVersion = "4.9.0.0.20170710";
	private static final String startSchemaVersion = "4.7.0.0.201710040000";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_47_49 NewInstance() {
		return new SchemaUpdater_47_49();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_47_49() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		ISchemaUpdaterStep step;
		String newColumnName;
		String query;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#7109 nom. valid => nom. val.
		stepName = "nom valid => nom. val. (abbrevLabel)";
		UUID uuidTerm = UUID.fromString("bd036217-5499-4ccd-8f4c-72e06158db93");
		UUID uuidLanguage = UUID.fromString("160a5b6c-87f5-4422-9bda-78cd404c179e");
		step = TermRepresentationUpdater.NewInstance(stepName, uuidTerm,
		        null, null, "nom. val.", uuidLanguage);
		stepList.add(step);

		//... idInVoc
		stepName = "nom valid => nom. val. (idInVocabulary)";
        query = "UPDATE @@DefinedTermBase@@ "
                + " SET idInVocabulary = 'nom. val.' "
                + " WHERE uuid = '" + uuidTerm + "'";
		tableName = "DefinedTermBase";
		step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, -99);
		stepList.add(step);

		//#7096 Add second symbol attribute to DefinedTermBase
		stepName = "Add second symbol to DefinedTermBase";
		tableName = "DefinedTermBase";
		newColumnName = "symbol2";
		int length = 30;
		step = ColumnAdder.NewStringInstance(stepName, tableName, newColumnName, length, INCLUDE_AUDIT);
		stepList.add(step);

		//#6879 Update uuid and name for admin user group
	    stepName = "nom valid => nom. val. (idInVocabulary)";
        query = "UPDATE @@PermissionGroup@@ "
                + " SET uuid='1739df71-bf73-4dc6-8320-aaaf72cb555f', name='Admin' "
                + " WHERE  name='admin' or name='Admin'";
        tableName = "PermissionGroup";
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, -99);
        stepList.add(step);






//        //#5149 remove unique index on Sequence_Reference.citations_id
//        tableName = "Sequence_Reference";
//        String columnName = "citations_id";
//        step = UniqueIndexDropper.NewInstance(tableName, columnName, !INCLUDE_AUDIT);
//        stepList.add(step);
//
//
//        //#6340 nom status invalid updater
//        step = NomStatusInvalidUpdater.NewInstance();
//        stepList.add(step);
//
//		//#6529
//		//Extend WorkingSet to allow a more fine grained definiton of taxon set
//		//min rank
//        stepName = "Add minRank column";
//        tableName = "WorkingSet";
//        newColumnName = "minRank_id";
//        String referencedTable = "DefinedTermBase";
//        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
//        stepList.add(step);
//
//        //max rank
//        stepName = "Add maxRank column";
//        tableName = "WorkingSet";
//        newColumnName = "maxRank_id";
//        referencedTable = "DefinedTermBase";
//        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
//        stepList.add(step);
//
//        //subtree filter
//        stepName= "Add geo filter MN table to WorkingSet";
//        String firstTableName = "WorkingSet";
//        String secondTableAlias = "NamedArea";
//        String secondTableName = "DefinedTermBase";
//        String attributeName = "geoFilter";
//        boolean isList = ! IS_LIST;
//        step = MnTableCreator.NewMnInstance(stepName, firstTableName, null, secondTableName, secondTableAlias, attributeName, INCLUDE_AUDIT, isList, IS_M_TO_M);
//        stepList.add(step);
//
//        //subtree filter
//        stepName= "Add subtree filter MN table to WorkingSet";
//        firstTableName = "WorkingSet";
//        secondTableName = "TaxonNode";
//        secondTableAlias = null;
//        attributeName = "taxonSubtreeFilter";
//        isList = ! IS_LIST;
//        step = MnTableCreator.NewMnInstance(stepName, firstTableName, null, secondTableName, secondTableAlias, attributeName, INCLUDE_AUDIT, isList, IS_M_TO_M);
//        stepList.add(step);
//
//        //#6258
//        stepName = "Add Registration table";
//        tableName = "Registration";
//        String[] columnNames = new String[]{"identifier","specificIdentifier","registrationDate","status",
//                "institution_id","name_id","submitter_id"};
//        String[] referencedTables = new String[]{null, null, null, null,
//                "AgentBase","TaxonNameBase","UserAccount"};
//        String[] columnTypes = new String[]{"string_255","string_255","datetime","string_10","int","int","int"};
//        step = TableCreator.NewAnnotatableInstance(stepName, tableName,
//                columnNames, columnTypes, referencedTables, INCLUDE_AUDIT);
//        stepList.add(step);
//
//        //add blockedBy_id
//        stepName= "Add blockedBy_id to Registration";
//        firstTableName = "Registration";
//        secondTableName = "Registration";
//        attributeName = "blockedBy";
//        isList = ! IS_LIST;
//        step = MnTableCreator.NewMnInstance(stepName, firstTableName, null, secondTableName, null, attributeName, INCLUDE_AUDIT, isList, IS_M_TO_M);
//        stepList.add(step);
//
//        //add type designations
//        stepName= "Add type designations to Registration";
//        firstTableName = "Registration";
//        String firstColumnName = "registrations";
//        secondTableName = "TypeDesignationBase";
//        attributeName = "typeDesignations";
//        isList = false;
//        step = MnTableCreator.NewMnInstance(stepName, firstTableName, null, firstColumnName, secondTableName, null, attributeName, INCLUDE_AUDIT, isList, IS_M_TO_M);
//        stepList.add(step);
//
//        //#5258
//        //Add "accessed" to Reference
//        stepName = "Add 'accessed' to Reference";
//        tableName = "Reference";
//        newColumnName = "accessed";
//        step = ColumnAdder.NewDateTimeInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL);
//        stepList.add(step);
//
//        //#6618 Add structure column to DefinedTermBase (Character)
//        stepName = "Add structure column to DefinedTermBase (Character)";
//        tableName = "DefinedTermBase";
//        newColumnName = "structure_id";
//        referencedTable = "FeatureNode";
//        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
//        stepList.add(step);
//
//        //#6618 Add property column to DefinedTermBase (Character)
//        stepName = "Add property column to DefinedTermBase (Character)";
//        tableName = "DefinedTermBase";
//        newColumnName = "property_id";
//        referencedTable = "FeatureNode";
//        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
//        stepList.add(step);
//
//        //##6661 Add initials to agent base
//        stepName = "Add initials to AgentBase";
//        tableName = "AgentBase";
//        newColumnName = "initials";
//        int length = 80;
//        step = ColumnAdder.NewStringInstance(stepName, tableName, newColumnName, length, INCLUDE_AUDIT);
//        stepList.add(step);
//
//        stepName = "Update initials and firstname";
//        step = InitialsUpdater.NewInstance();
//        stepList.add(step);
//
//        //#6663
//        //Add "lastRetrieved" to Reference
//        stepName = "Add 'lastRetrieved' to Reference";
//        tableName = "Reference";
//        newColumnName = "lastRetrieved";
//        step = ColumnAdder.NewDateTimeInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL);
//        stepList.add(step);
//
//        stepName = "Add externalId to Reference";
//        tableName = "Reference";
//        newColumnName = "externalId";
//        length = 255;
//        step = ColumnAdder.NewStringInstance(stepName, tableName, newColumnName, length, INCLUDE_AUDIT);
//        stepList.add(step);
//
//        stepName = "Add externalLink to Reference";
//        tableName = "Reference";
//        newColumnName = "externalLink";
//        step = ColumnAdder.NewClobInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT);
//        stepList.add(step);
//
//        stepName = "Add authorityType to Reference";
//        tableName = "Reference";
//        newColumnName = "authorityType";
//        length = 10;
//        step = ColumnAdder.NewStringInstance(stepName, tableName, newColumnName, length, INCLUDE_AUDIT);
//        stepList.add(step);
//
//        //#6472 add key to IntextReference
//        stepName = "Add key to IntextReference";
//        tableName = "IntextReference";
//        newColumnName = "key_id";
//        referencedTable = "PolytomousKey";
//        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
//        stepList.add(step);
//
//        //#5817 rename relationshipTermBase_inverseRepresentation
//        stepName = "Rename relationshipTermBase_inverseRepresentation";
//        String oldName = "RelationshipTermBase_inverseRepresentation";
//        String newName = "TermBase_inverseRepresentation";
//        step = TableNameChanger.NewInstance(stepName, oldName, newName, INCLUDE_AUDIT);
//        stepList.add(step);
//
//        //#5817 rename TermBase_inverseRepresentation.relationshipTermBase_id
//        stepName = "Rename relationshipTermBase_inverseRepresentation.relationshipTermBase_id";
//        tableName = newName;
//        String oldColumnName = "relationshipTermBase_id";
//        newColumnName = "term_id";
//        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
//        stepList.add(step);
//
//        //#6226 remove orphaned PolytomousKeyNodes
//        stepName = "remove orphaned PolytomousKeyNodes";
//        String query = " DELETE FROM @@PolytomousKeyNode@@ WHERE key_id NOT IN (SELECT id FROM @@PolytomousKey@@)";
//        String aud_query = " DELETE FROM @@PolytomousKeyNode_AUD@@ WHERE key_id NOT IN (SELECT id FROM @@PolytomousKey_AUD@@)";
//        step = SimpleSchemaUpdaterStep.NewExplicitAuditedInstance(stepName, query, aud_query, -99);
//        stepList.add(step);

        return stepList;
    }


    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_41_47.NewInstance();
	}

}
