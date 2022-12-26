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
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.update.ClassBaseTypeUpdater;
import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ColumnTypeChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.MnTableCreator;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.database.update.TableNameChanger;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;

/**
 * @author a.mueller
 * @date 09.06.2017
 */
public class SchemaUpdater_47_50 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final String endSchemaVersion = "5.0.0.0.20180514";
	private static final String startSchemaVersion = "4.7.0.0.201710040000";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_47_50 NewInstance() {
		return new SchemaUpdater_47_50();
	}

	protected SchemaUpdater_47_50() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;
		String query;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//
		stepName = "Update taxonName LSID authority namespaces";
		query = "UPDATE @@LSIDAuthority_namespaces@@ "
		        + " SET namespaces_element = 'eu.etaxonomy.cdm.model.name.TaxonName' "
		        + " WHERE namespaces_element = 'eu.etaxonomy.cdm.model.name.TaxonNameBase'";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query);

		//#6699 delete term version
		stepName = "Delete term version";
		query = "DELETE FROM @@CdmMetaData@@ WHERE propertyName = 'TERM_VERSION'";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query);

        //#6581 make nomenclatural reference and OriginalSource
        stepName = "Make nomenclatural reference and OriginalSource";
        tableName = "TaxonName";
        newColumnName = "nomenclaturalSource_id";
        String referencedTable = "OriginalSourceBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

		//#7109 nom. valid => nom. val.
		stepName = "nom valid => nom. val. (abbrevLabel)";
		UUID uuidTerm = UUID.fromString("bd036217-5499-4ccd-8f4c-72e06158db93");
		UUID uuidLanguage = UUID.fromString("160a5b6c-87f5-4422-9bda-78cd404c179e");
		TermRepresentationUpdater.NewInstance(stepList, stepName, uuidTerm,
		        null, null, "nom. val.", uuidLanguage);

		//... idInVoc
		stepName = "nom valid => nom. val. (idInVocabulary)";
        query = "UPDATE @@DefinedTermBase@@ "
                + " SET idInVocabulary = 'nom. val.' "
                + " WHERE uuid = '" + uuidTerm + "'";
		tableName = "DefinedTermBase";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName);

		//#7074 change type for Media.mediaCreated
		changeTypeMediaCreated(stepList);

		//#6752 add Reference.datePublished_verbatimDate
	    stepName = "Add Reference.datePublished_verbatimDate";
	    tableName = "Reference";
	    newColumnName = "datePublished_verbatimDate";
	    ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

		//#7096 Add second symbol attribute to DefinedTermBase
		stepName = "Add second symbol to DefinedTermBase";
		tableName = "DefinedTermBase";
		newColumnName = "symbol2";
		int length = 30;
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

		//#6879 Update uuid and name for admin user group
	    stepName = "Update uuid and name for admin user group";
        query = "UPDATE @@PermissionGroup@@ "
                + " SET uuid='1739df71-bf73-4dc6-8320-aaaf72cb555f', name='Admin' "
                + " WHERE  name='admin' or name='Admin'";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query);

        //#7405 Rename WorkingSet to DescriptiveDataSet
        String oldTableName = "WorkingSet";
        changeSingleWorkingSetTableName(stepList, oldTableName);

        oldTableName = "WorkingSet_Annotation";
        changeSingleWorkingSetTableName(stepList, oldTableName);

        oldTableName = "WorkingSet_DescriptionBase";
        changeSingleWorkingSetTableName(stepList, oldTableName);

        oldTableName = "WorkingSet_Marker";
        changeSingleWorkingSetTableName(stepList, oldTableName);

        oldTableName = "WorkingSet_NamedArea";
        changeSingleWorkingSetTableName(stepList, oldTableName);

        oldTableName = "WorkingSet_Representation";
        changeSingleWorkingSetTableName(stepList, oldTableName);

        oldTableName = "WorkingSet_TaxonNode";
        changeSingleWorkingSetTableName(stepList, oldTableName);



        //#2335 Make DescriptiveDataSet IdentifiableEntity
        stepName = "Make DescriptiveDataSet IdentifiableEntity";
        tableName = "DescriptiveDataSet";
        ClassBaseTypeUpdater.NewAnnotatableToIdentifiableInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

        //TODO add titleCache updater, but maybe not necessary as real data does not really exist
        //except for Campanula test data, and it is easy to update via TaxEditor cache updater


        //#7374 Set titleCache of feature trees to protected
        stepName = "Set titleCache of feature trees to protected";
        query = "UPDATE @@FeatureTree@@ "
                + " SET protectedTitleCache = @TRUE@ ";
        tableName = "FeatureTree";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName);

        //#7238 rename lastName and firstName
        stepName = "rename lastName";
        tableName = "AgentBase";
        String oldColumnName = "lastname";
        newColumnName = "familyName";
        int size = 255;
        ColumnNameChanger.NewVarCharInstance(stepList, stepName, tableName, oldColumnName, newColumnName, size, INCLUDE_AUDIT);

        //... firstName
        stepName = "rename firstName";
        tableName = "AgentBase";
        oldColumnName = "firstname";
        newColumnName = "givenName";
        size = 255;
        ColumnNameChanger.NewVarCharInstance(stepList, stepName, tableName, oldColumnName, newColumnName, size, INCLUDE_AUDIT);

        //#7210 Add salt field for User
        stepName = "Add salt field for User";
        tableName = "UserAccount";
        newColumnName = "salt";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

        //#6943 Add combination 'in'-author
        stepName = "Add combination 'in'-author";
        tableName = "TaxonName";
        newColumnName = "inCombinationAuthorship_id";
        referencedTable = "AgentBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //#6943 Add basionym 'in'-author
        stepName = "Add basionym 'in'-author";
        tableName = "TaxonName";
        newColumnName = "inBasionymAuthorship_id";
        referencedTable = "AgentBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //#6916 Link IntextReference to OriginalSource
        stepName = "Link IntextReference to OriginalSource";
        tableName = "IntextReference";
        newColumnName = "source_id";
        referencedTable = "OriginalSourceBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //#6720 Make individual count a string
        stepName = "Make individual count a string";
        tableName = "SpecimenOrObservationBase";
        String columnName = "individualCount";
        size = 255;
        ColumnTypeChanger.NewInt2StringInstance(stepList, stepName, tableName, columnName, size, INCLUDE_AUDIT, null, !NOT_NULL);

        updateSpecimenTypeDesignationStatusOrder(stepList);

        //#7144 Set Country area level
        stepName = "Set Country area level";
        query = " UPDATE @@DefinedTermBase@@ " +
                " SET level_id = ( SELECT id FROM (SELECT id FROM DefinedTermBase WHERE uuid = '79db63a4-1563-461e-8e41-48f5722feca4') as drv) " +
                " WHERE DTYPE = 'Country' ";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query);

        //#6588
        stepName = "Add ExternalLink table";
        tableName = "ExternalLink";
        String[] columnNames = new String[]{"linkType","uri","size"};
        String[] referencedTables = new String[]{null, null, null};
        String[] columnTypes = new String[]{"string_10","clob","int"};
        TableCreator.NewVersionableInstance(stepList, stepName, tableName,
                columnNames, columnTypes, referencedTables, INCLUDE_AUDIT);

        //add i18n description
        stepName= "Add i18n description to ExternalLink";
        String firstTableName = "ExternalLink";
        String attributeName = "description";
        MnTableCreator.NewDescriptionInstance(stepList, stepName, firstTableName, null, attributeName, INCLUDE_AUDIT);

        //#6588 add link to sources
        stepName= "Add external link to sources";
        firstTableName = "OriginalSourceBase";
        String secondTableName = "ExternalLink";
        attributeName = "links";
        boolean isList = false;
        MnTableCreator.NewMnInstance(stepList, stepName, firstTableName, null, secondTableName, null, attributeName, INCLUDE_AUDIT, isList, IS_1_TO_M);

        //#7334 Make pro parte/partial concept relationships
        ProParteSynonymUpdater.NewInstance(stepList);

        return stepList;
    }

    /**
     * @param stepList
     */
    private void changeTypeMediaCreated(List<ISchemaUpdaterStep> stepList) {
        //rename old column
        String stepName = "Rename Media.mediaCreated";
        String tableName = "Media";
        String oldColumnName = "mediaCreated";
        String newColumnName = "mediaCreatedOld";
        ColumnNameChanger.NewDateTimeInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //add timeperiod columns
        stepName = "Add mediaCreated_start";
        tableName = "Media";
        newColumnName = "mediaCreated_start";
        int size = 50;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, size, INCLUDE_AUDIT);

        stepName = "Add mediaCreated_end";
        newColumnName = "mediaCreated_end";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, size, INCLUDE_AUDIT);

        stepName = "Add mediaCreated_freetext";
        newColumnName = "mediaCreated_freetext";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        //move data
        stepName = "Copy mediaCreated to new columns";
        String queryTemplate = "UPDATE @@Media@@ "
                + " SET mediaCreated_start = %s "
                + " WHERE mediaCreatedOld IS NOT NULL ";
        String queryDefault = String.format(queryTemplate, "Left(Replace(Replace(Replace(mediaCreatedOld, '-', ''), ':', ''), ' ', '_'), 13)");
        String queryPostgres = String.format(queryTemplate, "to_char(mediaCreatedOld,'YYYYMMDD HH24MI')");
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, queryDefault, tableName)
                  .put(DatabaseTypeEnum.PostgreSQL, queryPostgres)
                  .putAudited(DatabaseTypeEnum.PostgreSQL, queryPostgres);

        //delete old column
        stepName = "Remove old mediaCreated";
        String columnName = "mediaCreatedOld";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);
    }

    /**
     * @param stepList
     */
    private void updateSpecimenTypeDesignationStatusOrder(List<ISchemaUpdaterStep> stepList) {
        VocabularyOrderUpdater updater = VocabularyOrderUpdater.NewInstance(stepList);
        updater.add("a407dbc7-e60c-46ff-be11-eddf4c5a970d", 1);
        updater.add("05002d46-083e-4b27-8731-2e7c28a8825c", 2);
        updater.add("93ef8257-0a08-47bb-9b36-542417ae7560", 3);
        updater.add("7a1a8a53-78f4-4fc0-89f7-782e94992d08", 4);
        updater.add("f3b60bdb-4638-4ca9-a0c7-36e77d8459bb", 5);
        updater.add("052a5ff0-8e9a-4355-b24f-5e4bb6071f44", 6);
        updater.add("26e13359-8f77-4e40-a85a-56c01782fce0", 7);
        updater.add("7afc2f4f-f70a-4aa5-80a5-87764f746bde", 8);
        updater.add("989a2715-71d5-4fbe-aa9a-db9168353744", 9);
        updater.add("95b90696-e103-4bc0-b60b-c594983fb566", 10);
        updater.add("eb7df2e5-d9a7-479d-970c-c6f2b0a761d7", 11);
        updater.add("497137f3-b614-4183-8a22-97fcd6e2bdd8", 12);
        updater.add("7244bc51-14d8-41a6-9524-7dc5303bba29", 13);
        updater.add("0c39e2a5-2fe0-4d4f-819a-f609b5340339", 14);
        updater.add("01d91053-7004-4984-aa0d-9f4de59d6205", 15);
        updater.add("8d2fed1f-242e-4bcf-bbd7-e85133e479dc", 16);
        updater.add("49c96cae-6be6-401e-9b36-1bc12d9dc8f9", 17);
        updater.add("643513d0-32f5-46ba-840b-d9b9caf8160f", 18);
        updater.add("b7807acc-f559-474e-ad4a-e7a41e085e34", 19);
        updater.add("230fd762-b143-49de-ac2e-744bcc48a63b", 20);
        updater.add("7194020b-a326-4b47-9bfe-9f31a30aba7f", 21);
    }

    /**
     * @param #6368
     */
    private void changeSingleWorkingSetTableName(List<ISchemaUpdaterStep> stepList, String oldTableName) {
        String stepName = "Rename " +  oldTableName;
        String newTableName = oldTableName.replace("WorkingSet", "DescriptiveDataSet");
        boolean includeDtype = !oldTableName.contains("_");

        TableNameChanger.NewInstance(stepList, stepName, oldTableName,
                newTableName, INCLUDE_AUDIT, includeDtype);

        if (oldTableName.contains("_")){
            stepName = "Rename " +  oldTableName + ".workingSet_id";
            String oldColumnName = "WorkingSet_id";
            String newColumnName = "DescriptiveDataSet_id";
            if ("WorkingSet_DescriptionBase".equals(oldTableName)){
                oldColumnName = "WorkingSets_id";
                newColumnName = "DescriptiveDataSets_id";
            }
            ColumnNameChanger.NewIntegerInstance(stepList, stepName, newTableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        }
    }

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_41_47.NewInstance();
	}
}