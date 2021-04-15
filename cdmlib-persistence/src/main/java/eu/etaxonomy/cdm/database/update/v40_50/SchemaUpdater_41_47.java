/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v40_50;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.IndexRenamer;
import eu.etaxonomy.cdm.database.update.MnTableCreator;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.database.update.TableNameChanger;
import eu.etaxonomy.cdm.database.update.UniqueIndexDropper;

/**
 * @author a.mueller
 * @since 09.05.2017
 *
 */
public class SchemaUpdater_41_47 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_41_47.class);
	private static final String startSchemaVersion = "4.1.0.0.201607300000";
	private static final String endSchemaVersion = "4.7.0.0.201710040000";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_41_47 NewInstance() {
		return new SchemaUpdater_41_47();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_41_47() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

	    //#6600 remove unique indexes from Rights MN tables
        removeUniqueIndexForRights(stepList);

        //#5149 remove unique index on Sequence_Reference.citations_id
        tableName = "Sequence_Reference";
        String columnName = "citations_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, columnName, !INCLUDE_AUDIT);

        //#6340 nom status invalid updater
        NomStatusInvalidUpdater.NewInstance(stepList);

		//#6529
		//Extend WorkingSet to allow a more fine grained definiton of taxon set
		//min rank
        stepName = "Add minRank column";
        tableName = "WorkingSet";
        newColumnName = "minRank_id";
        String referencedTable = "DefinedTermBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //max rank
        stepName = "Add maxRank column";
        tableName = "WorkingSet";
        newColumnName = "maxRank_id";
        referencedTable = "DefinedTermBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //geo filter
        stepName= "Add geo filter MN table to WorkingSet";
        String firstTableName = "WorkingSet";
        String secondTableName = "DefinedTermBase";
        String secondTableAlias = "NamedArea";
        String attributeName = "geoFilter";
        boolean isList = ! IS_LIST;
        MnTableCreator.NewMnInstance(stepList, stepName, firstTableName, null, secondTableName, secondTableAlias, attributeName, INCLUDE_AUDIT, isList, IS_M_TO_M);

        //subtree filter
        stepName= "Add subtree filter MN table to WorkingSet";
        firstTableName = "WorkingSet";
        secondTableName = "TaxonNode";
        secondTableAlias = null;
        attributeName = "taxonSubtreeFilter";
        isList = ! IS_LIST;
        MnTableCreator.NewMnInstance(stepList, stepName, firstTableName, null, secondTableName, secondTableAlias, attributeName, INCLUDE_AUDIT, isList, IS_M_TO_M);

        //#6258
        stepName = "Add Registration table";
        tableName = "Registration";
        String[] columnNames = new String[]{"identifier","specificIdentifier","registrationDate","status",
                "institution_id","name_id","submitter_id"};
        String[] referencedTables = new String[]{null, null, null, null,
                "AgentBase","TaxonNameBase","UserAccount"};
        String[] columnTypes = new String[]{"string_255","string_255","datetime","string_10","int","int","int"};
        TableCreator.NewAnnotatableInstance(stepList, stepName, tableName,
                columnNames, columnTypes, referencedTables, INCLUDE_AUDIT);

        //add blockedBy_id
        stepName= "Add blockedBy_id to Registration";
        firstTableName = "Registration";
        secondTableName = "Registration";
        attributeName = "blockedBy";
        isList = ! IS_LIST;
        MnTableCreator.NewMnInstance(stepList, stepName, firstTableName, null, secondTableName, null, attributeName, INCLUDE_AUDIT, isList, IS_M_TO_M);

        //add type designations
        stepName= "Add type designations to Registration";
        firstTableName = "Registration";
        String firstColumnName = "registrations";
        secondTableName = "TypeDesignationBase";
        attributeName = "typeDesignations";
        isList = false;
        MnTableCreator.NewMnInstance(stepList, stepName, firstTableName, null, firstColumnName, secondTableName, null, attributeName, INCLUDE_AUDIT, isList, IS_M_TO_M);

        //#5258
        //Add "accessed" to Reference
        stepName = "Add 'accessed' to Reference";
        tableName = "Reference";
        newColumnName = "accessed";
        ColumnAdder.NewDateTimeInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL);

        //#6618 Add structure column to DefinedTermBase (Character)
        stepName = "Add structure column to DefinedTermBase (Character)";
        tableName = "DefinedTermBase";
        newColumnName = "structure_id";
        referencedTable = "FeatureNode";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //#6618 Add property column to DefinedTermBase (Character)
        stepName = "Add property column to DefinedTermBase (Character)";
        tableName = "DefinedTermBase";
        newColumnName = "property_id";
        referencedTable = "FeatureNode";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //#6361 and children
        mergeTaxonName(stepList);

        //#6535 update termtype for CdmMetaData (int => string)
        updateTermTypeForCdmMetaDataPropertyName(stepList);

        //##6661 Add initials to agent base
        stepName = "Add initials to AgentBase";
        tableName = "AgentBase";
        newColumnName = "initials";
        int length = 80;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

        stepName = "Update initials and firstname";
        InitialsUpdater.NewInstance(stepList);

        //#6663
        //Add "lastRetrieved" to Reference
        stepName = "Add 'lastRetrieved' to Reference";
        tableName = "Reference";
        newColumnName = "lastRetrieved";
        ColumnAdder.NewDateTimeInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL);

        stepName = "Add externalId to Reference";
        tableName = "Reference";
        newColumnName = "externalId";
        length = 255;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

        stepName = "Add externalLink to Reference";
        tableName = "Reference";
        newColumnName = "externalLink";
        ColumnAdder.NewClobInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        stepName = "Add authorityType to Reference";
        tableName = "Reference";
        newColumnName = "authorityType";
        length = 10;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

        //#6472 add key to IntextReference
        stepName = "Add key to IntextReference";
        tableName = "IntextReference";
        newColumnName = "key_id";
        referencedTable = "PolytomousKey";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //#5817 rename relationshipTermBase_inverseRepresentation
        stepName = "Rename relationshipTermBase_inverseRepresentation";
        String oldName = "RelationshipTermBase_inverseRepresentation";
        String newName = "TermBase_inverseRepresentation";
        TableNameChanger.NewInstance(stepList, stepName, oldName, newName, INCLUDE_AUDIT);

        //#5817 rename TermBase_inverseRepresentation.relationshipTermBase_id
        stepName = "Rename relationshipTermBase_inverseRepresentation.relationshipTermBase_id";
        tableName = newName;
        String oldColumnName = "relationshipTermBase_id";
        newColumnName = "term_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //#6226 remove orphaned PolytomousKeyNodes
        stepName = "remove orphaned PolytomousKeyNodes";
        String query = " DELETE FROM @@PolytomousKeyNode@@ WHERE key_id NOT IN (SELECT id FROM @@PolytomousKey@@)";
        String aud_query = " DELETE FROM @@PolytomousKeyNode_AUD@@ WHERE key_id NOT IN (SELECT id FROM @@PolytomousKey_AUD@@)";
        SimpleSchemaUpdaterStep.NewExplicitAuditedInstance(stepList, stepName, query, aud_query, -99);

        //#6226 remove orphaned key statements
        OrphanedKeyStatementRemover.NewInstance(stepList);

        return stepList;
    }

    /**
     * @param stepList
     */
	//#6600 remove Unique indexes from Rights MN tables
    private void removeUniqueIndexForRights(List<ISchemaUpdaterStep> stepList) {
        String tableName;

        tableName = "AgentBase_RightsInfo";
        String columnName = "rights_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, columnName, !INCLUDE_AUDIT);

        tableName = "Classification_RightsInfo";
        columnName = "rights_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, columnName, !INCLUDE_AUDIT);

        tableName = "Collection_RightsInfo";
        columnName = "rights_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, columnName, !INCLUDE_AUDIT);

        tableName = "DefinedTermBase_RightsInfo";
        columnName = "rights_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, columnName, !INCLUDE_AUDIT);

        tableName = "DescriptionBase_RightsInfo";
        columnName = "rights_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, columnName, !INCLUDE_AUDIT);

        tableName = "FeatureTree_RightsInfo";
        columnName = "rights_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, columnName, !INCLUDE_AUDIT);

        tableName = "Media_RightsInfo";
        columnName = "rights_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, columnName, !INCLUDE_AUDIT);

        tableName = "PolytomousKey_RightsInfo";
        columnName = "rights_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, columnName, !INCLUDE_AUDIT);

        tableName = "Reference_RightsInfo";
        columnName = "rights_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, columnName, !INCLUDE_AUDIT);

        tableName = "SpecimenOrObservationBase_RightsInfo";
        columnName = "rights_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, columnName, !INCLUDE_AUDIT);

        tableName = "TaxonBase_RightsInfo";
        columnName = "rights_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, columnName, !INCLUDE_AUDIT);

        tableName = "TaxonNameBase_RightsInfo";
        columnName = "rights_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, columnName, !INCLUDE_AUDIT);

        tableName = "TermVocabulary_RightsInfo";
        columnName = "rights_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, columnName, !INCLUDE_AUDIT);
    }


    /**
     * #6535 update termtype for CdmMetaData (int => string)
     */
    private void updateTermTypeForCdmMetaDataPropertyName(List<ISchemaUpdaterStep> stepList) {
        String stepName = "Rename CdmMetaData.propertyName column";
        String tableName = "CdmMetaData";
        String oldColumnName = "propertyName";
        String newColumnName = "propertyNameOld";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, ! INCLUDE_AUDIT);

        //... create new column
        stepName = "Create new CdmMetaData.propertyName column";
        tableName = "CdmMetaData";
        newColumnName = "propertyName";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, 20, ! INCLUDE_AUDIT);

        updateSingleTermTypeForCdmMetaDataPropertyName(stepList, "SCHEMA_VERSION", 0);
        updateSingleTermTypeForCdmMetaDataPropertyName(stepList, "TERM_VERSION", 1 );
        updateSingleTermTypeForCdmMetaDataPropertyName(stepList, "CREATED", 2 );
        updateSingleTermTypeForCdmMetaDataPropertyName(stepList, "CREATE_NOTE", 3 );
        updateSingleTermTypeForCdmMetaDataPropertyName(stepList, "INST_NAME", 4 );
        updateSingleTermTypeForCdmMetaDataPropertyName(stepList, "INST_ID", 5 );

        //... create new column
        stepName = "Remove column CdmMetaData.propertyNameOld";
        tableName = "CdmMetaData";
        oldColumnName = "propertyNameOld";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);

    }


    /**
     * @param stepList
     * @param name
     * @param index
     */
    private void updateSingleTermTypeForCdmMetaDataPropertyName(List<ISchemaUpdaterStep> stepList,
            String name, int index) {
        String stepName = "Update value for " + name;
        String query = "UPDATE @@CdmMetaData@@ SET propertyName = '"+name+"' WHERE propertyNameOld = "+index;
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, -99);
    }

    /**
     * #6361 and children
     */
    private void mergeTaxonName(List<ISchemaUpdaterStep> stepList) {

        //#6367 Add nameType column to TaxonNameBase
        String stepName = "Add nameType column to TaxonNameBase";
        String tableName = "TaxonNameBase";
        String newColumnName = "nameType";
        int length = 15;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

        //#6367 #6368
        updateNameTypes(stepList);

        // update anamorphic
        stepName = "Update anamorphic ";
        String query = "UPDATE @@TaxonNameBase@@ tnb "
                + " SET anamorphic = @FALSE@ "
                + " WHERE anamorphic IS NULL " ;
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, "TaxonNameBase", -99);

        //#6368 Remove DTYPE
        stepName = "Remove DTYPE from TaxonNameBase";
        tableName = "TaxonNameBase";
        String oldColumnName = "DTYPE";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //#6368
        changeTaxonNameTableName(stepList);

        //#6717 update index names
        IndexRenamer.NewStringInstance(stepList, "TaxonName",
                "taxonNameBaseNameCacheIndex", "taxonNameNameCacheIndex", "nameCache", 255);

        IndexRenamer.NewStringInstance(stepList, "TaxonName",
                "taxonNameBaseTitleCacheIndex", "taxonNameTitleCacheIndex", "titleCache", 333);

    }

    /**
     * #6368
     */
    private void changeTaxonNameTableName(List<ISchemaUpdaterStep> stepList) {

        //Update
        String oldName = "TaxonNameBase";
        changeSingleTaxonNameTableName(stepList, oldName);

        oldName = "TaxonNameBase_Annotation";
        changeSingleTaxonNameTableName(stepList, oldName);

        oldName = "TaxonNameBase_Credit";
        changeSingleTaxonNameTableName(stepList, oldName);

        oldName = "TaxonNameBase_Extension";
        changeSingleTaxonNameTableName(stepList, oldName);

        oldName = "TaxonNameBase_Identifier";
        changeSingleTaxonNameTableName(stepList, oldName);

        oldName = "TaxonNameBase_Marker";
        changeSingleTaxonNameTableName(stepList, oldName);

        oldName = "TaxonNameBase_NomenclaturalStatus";
        changeSingleTaxonNameTableName(stepList, oldName);

        oldName = "TaxonNameBase_OriginalSourceBase";
        changeSingleTaxonNameTableName(stepList, oldName);

        oldName = "TaxonNameBase_RightsInfo";
        changeSingleTaxonNameTableName(stepList, oldName);

        oldName = "TaxonNameBase_TypeDesignationBase";
        changeSingleTaxonNameTableName(stepList, oldName);

        //hibernate sequence
        String stepName = "Update hibernate sequence entry name for TaxonNameBase";
        String query = "UPDATE hibernate_sequences SET sequence_name = 'TaxonName' WHERE sequence_name = 'TaxonNameBase'";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, -99);

        //grantedauthority for taxonnamebase
        stepName = "Update GrantedAuthorityImpl for TaxonNameBase";
        query = "UPDATE GrantedAuthorityImpl " +
                " SET authority = Replace (authority, 'TAXONNAMEBASE','TAXONNAME') " +
                " WHERE authority like '%TAXONNAMEBASE%' ";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, -99);


        //LSIDAuthority_namespaces for taxonnamebase
        stepName = "Upate LSIDAuthority_namespaces for TaxonNameBase";
        query = "UPDATE @@LSIDAuthority_namespaces@@ " +
                " SET namespaces_element = Replace (namespaces_element, 'TaxonNameBase','TaxonName') " +
                " WHERE namespaces_element like '%TaxonNameBase%' ";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, -99);
    }

    /**
     * @param #6368
     */
    private void changeSingleTaxonNameTableName(List<ISchemaUpdaterStep> stepList, String oldTableName) {
        String stepName = "Rename " +  oldTableName;
        String newTableName = oldTableName.replace("TaxonNameBase", "TaxonName");
        TableNameChanger.NewInstance(stepList, stepName, oldTableName, newTableName, INCLUDE_AUDIT);

        if (oldTableName.contains("_")){
            stepName = "Rename " +  oldTableName + ".taxonNameBase_id";
            String oldColumnName = "TaxonNameBase_id";
            String newColumnName = "TaxonName_id";
            ColumnNameChanger.NewIntegerInstance(stepList, stepName, newTableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        }
    }

    /**
     * #6367
     */
    private void updateNameTypes(List<ISchemaUpdaterStep> stepList) {
        updateSingleNameType(stepList, "ViralName", "ICVCN");
        updateSingleNameType(stepList, "NonViralName", "NonViral");
        updateSingleNameType(stepList, "BotanicalName", "ICNAFP");
        updateSingleNameType(stepList, "ZoologicalName", "ICZN");
        updateSingleNameType(stepList, "CultivarPlantName", "ICNCP");
        updateSingleNameType(stepList, "BacterialName", "ICNB");
    }

    /**
     * @param uuid the uuid
     * @param oldSymbol
     * @param newSybol
     */
    private void updateSingleNameType(List<ISchemaUpdaterStep> stepList,
            String dtype, String nameType) {

        String stepName = "Update nameType for " + dtype;
        String query = "UPDATE @@TaxonNameBase@@ tnb "
                + " SET nameType = '" + nameType + "'"
                + " WHERE dtype = '" + dtype + "'" ;
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, "TaxonNameBase", -99);
    }

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_40_41.NewInstance();
	}

}
