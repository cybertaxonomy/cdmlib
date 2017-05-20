/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v41_47;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.MnTableCreator;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.database.update.UniqueIndexDropper;
import eu.etaxonomy.cdm.database.update.v40_41.SchemaUpdater_40_41;

/**
 * @author a.mueller
 * @created 16.04.2016
 */
/**
 * @author a.mueller
 * @date 09.05.2017
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
		ISchemaUpdaterStep step;
		String newColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

	    //#6600 remove Unique indexes from Rights MN tables
        removeUniqueIndexForRights(stepList);


		//#6529
		//Extend WorkingSet to allow a more fine grained definiton of taxon set
		//min rank
        stepName = "Add minRank column";
        tableName = "WorkingSet";
        newColumnName = "minRank_id";
        String referencedTable = "DefinedTermBase";
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
        stepList.add(step);

        //max rank
        stepName = "Add maxRank column";
        tableName = "WorkingSet";
        newColumnName = "maxRank_id";
        referencedTable = "DefinedTermBase";
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
        stepList.add(step);

        //subtree filter
        stepName= "Add geo filter MN table to WorkingSet";
        String firstTableName = "WorkingSet";
        String secondTableAlias = "NamedArea";
        String secondTableName = "DefinedTermBase";
        String attributeName = "geoFilter";
        boolean isList = ! IS_LIST;
        step = MnTableCreator.NewMnInstance(stepName, firstTableName, null, secondTableName, secondTableAlias, attributeName, INCLUDE_AUDIT, isList, IS_M_TO_M);
        stepList.add(step);

        //subtree filter
        stepName= "Add subtree filter MN table to WorkingSet";
        firstTableName = "WorkingSet";
        secondTableName = "TaxonNode";
        secondTableAlias = null;
        attributeName = "taxonSubtreeFilter";
        isList = ! IS_LIST;
        step = MnTableCreator.NewMnInstance(stepName, firstTableName, null, secondTableName, secondTableAlias, attributeName, INCLUDE_AUDIT, isList, IS_M_TO_M);
        stepList.add(step);

        //#6258
        stepName = "Add Registration table";
        tableName = "Registration";
        String[] columnNames = new String[]{"identifier","specificIdentifier","registrationDate","status",
                "institution_id","name_id","submitter_id"};
        String[] referencedTables = new String[]{null, null, null, null,
                "AgentBase","TaxonNameBase","User"};
        String[] columnTypes = new String[]{"string_255","string_255","datetime","string_10","int","int","int"};
        step = TableCreator.NewAnnotatableInstance(stepName, tableName,
                columnNames, columnTypes, referencedTables, INCLUDE_AUDIT);
        stepList.add(step);

        //add blockedBy_id
        stepName= "Add blockedBy_id to Registration";
        firstTableName = "Registration";
        secondTableName = "Registration";
        attributeName = "blockedBy";
        isList = ! IS_LIST;
        step = MnTableCreator.NewMnInstance(stepName, firstTableName, null, secondTableName, null, attributeName, INCLUDE_AUDIT, isList, IS_M_TO_M);
        stepList.add(step);

        //add type designations
        stepName= "Add type designations to Registration";
        firstTableName = "Registration";
        String firstColumnName = "registrations";
        secondTableName = "TypeDesignationBase";
        attributeName = "typeDesignations";
        isList = false;
        step = MnTableCreator.NewMnInstance(stepName, firstTableName, null, firstColumnName, secondTableName, null, attributeName, INCLUDE_AUDIT, isList, IS_M_TO_M);
        stepList.add(step);

        //#5258
        //Add "accessed" to Reference
        stepName = "Add 'accessed' to Reference";
        tableName = "Reference";
        newColumnName = "accessed";
        step = ColumnAdder.NewDateTimeInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL);
        stepList.add(step);

        //#6618 Add structure column to DefinedTermBase (Character)
        stepName = "Add structure column to DefinedTermBase (Character)";
        tableName = "DefinedTermBase";
        newColumnName = "structure_id";
        referencedTable = "FeatureNode";
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
        stepList.add(step);

        //#6618 Add property column to DefinedTermBase (Character)
        stepName = "Add property column to DefinedTermBase (Character)";
        tableName = "DefinedTermBase";
        newColumnName = "property_id";
        referencedTable = "FeatureNode";
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
        stepList.add(step);

        //#6361 and children
        mergeTaxonName(stepList);

        //#6535 update termtype for CdmMetaData (int => string)
        updateTermTypeForCdmMetaDataPropertyName(stepList);


        //ModelUpdateResult

        //Remove termupdater and allow update only from version 4.0



        return stepList;
    }

    /**
     * @param stepList
     */
	//#6600 remove Unique indexes from Rights MN tables
    private void removeUniqueIndexForRights(List<ISchemaUpdaterStep> stepList) {
        String tableName;
        ISchemaUpdaterStep step;

        tableName = "AgentBase_RightsInfo";
        String columnName = "rights_id";
        step = UniqueIndexDropper.NewInstance(tableName, columnName, !INCLUDE_AUDIT);
        stepList.add(step);

        tableName = "Classification_RightsInfo";
        columnName = "rights_id";
        step = UniqueIndexDropper.NewInstance(tableName, columnName, !INCLUDE_AUDIT);
        stepList.add(step);

        tableName = "Collection_RightsInfo";
        columnName = "rights_id";
        step = UniqueIndexDropper.NewInstance(tableName, columnName, !INCLUDE_AUDIT);
        stepList.add(step);

        tableName = "DefinedTermBase_RightsInfo";
        columnName = "rights_id";
        step = UniqueIndexDropper.NewInstance(tableName, columnName, !INCLUDE_AUDIT);
        stepList.add(step);

        tableName = "DescriptionBase_RightsInfo";
        columnName = "rights_id";
        step = UniqueIndexDropper.NewInstance(tableName, columnName, !INCLUDE_AUDIT);
        stepList.add(step);

        tableName = "FeatureTree_RightsInfo";
        columnName = "rights_id";
        step = UniqueIndexDropper.NewInstance(tableName, columnName, !INCLUDE_AUDIT);
        stepList.add(step);

        tableName = "Media_RightsInfo";
        columnName = "rights_id";
        step = UniqueIndexDropper.NewInstance(tableName, columnName, !INCLUDE_AUDIT);
        stepList.add(step);

        tableName = "PolytomousKey_RightsInfo";
        columnName = "rights_id";
        step = UniqueIndexDropper.NewInstance(tableName, columnName, !INCLUDE_AUDIT);
        stepList.add(step);

        tableName = "Reference_RightsInfo";
        columnName = "rights_id";
        step = UniqueIndexDropper.NewInstance(tableName, columnName, !INCLUDE_AUDIT);
        stepList.add(step);

        tableName = "SpecimenOrObservationBase_RightsInfo";
        columnName = "rights_id";
        step = UniqueIndexDropper.NewInstance(tableName, columnName, !INCLUDE_AUDIT);
        stepList.add(step);

        tableName = "TaxonBase_RightsInfo";
        columnName = "rights_id";
        step = UniqueIndexDropper.NewInstance(tableName, columnName, !INCLUDE_AUDIT);
        stepList.add(step);

        tableName = "TaxonNameBase_RightsInfo";
        columnName = "rights_id";
        step = UniqueIndexDropper.NewInstance(tableName, columnName, !INCLUDE_AUDIT);
        stepList.add(step);

        tableName = "TermVocabulary_RightsInfo";
        columnName = "rights_id";
        step = UniqueIndexDropper.NewInstance(tableName, columnName, !INCLUDE_AUDIT);
        stepList.add(step);
    }


    /**
     * #6535 update termtype for CdmMetaData (int => string)
     */
    private void updateTermTypeForCdmMetaDataPropertyName(List<ISchemaUpdaterStep> stepList) {
        String stepName = "Rename CdmMetaData.propertyName column";
        String tableName = "CdmMetaData";
        String oldColumnName = "propertyName";
        String newColumnName = "propertyNameOld";
        ISchemaUpdaterStep step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, ! INCLUDE_AUDIT);
        stepList.add(step);

        //... create new column
        stepName = "Create new CdmMetaData.propertyName column";
        tableName = "CdmMetaData";
        newColumnName = "propertyName";
        step = ColumnAdder.NewStringInstance(stepName, tableName, newColumnName, 20, ! INCLUDE_AUDIT);
        stepList.add(step);

        updateSingleTermTypeForCdmMetaDataPropertyName(stepList, "SCHEMA_VERSION", 1 );
        updateSingleTermTypeForCdmMetaDataPropertyName(stepList, "TERM_VERSION", 2 );
        updateSingleTermTypeForCdmMetaDataPropertyName(stepList, "CREATED", 3 );
        updateSingleTermTypeForCdmMetaDataPropertyName(stepList, "CREATE_NOTE", 4 );
        updateSingleTermTypeForCdmMetaDataPropertyName(stepList, "INST_NAME", 5 );
        updateSingleTermTypeForCdmMetaDataPropertyName(stepList, "INST_ID", 6 );

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
        ISchemaUpdaterStep step = SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepName, query, -99);
        stepList.add(step);

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
        ISchemaUpdaterStep step = ColumnAdder.NewStringInstance(stepName, tableName, newColumnName, length, INCLUDE_AUDIT);
        stepList.add(step);

        //#6367 #6368
        updateNameTypes(stepList);


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
        SimpleSchemaUpdaterStep simpleStep = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, "TaxonNameBase", -99);
        stepList.add(simpleStep);
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_40_41.NewInstance();
	}

}
