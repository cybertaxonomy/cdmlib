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

import eu.etaxonomy.cdm.database.update.ClassBaseTypeUpdater;
import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ColumnTypeChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableNameChanger;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.database.update.v33_34.UsernameConstraintUpdater;
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

        //#6581 make nomenclatural reference and OriginalSource
        stepName = "Make nomenclatural reference and OriginalSource";
        tableName = "TaxonName";
        newColumnName = "nomenclaturalSource_id";
        String referencedTable = "OriginalSourceBase";
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
        stepList.add(step);

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
	    stepName = "Update uuid and name for admin user group";
        query = "UPDATE @@PermissionGroup@@ "
                + " SET uuid='1739df71-bf73-4dc6-8320-aaaf72cb555f', name='Admin' "
                + " WHERE  name='admin' or name='Admin'";
        step = SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepName, query, -99);
        stepList.add(step);

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

        stepName = "Update hibernate_sequences for WorkingSet renaming";
        query = " UPDATE hibernate_sequences "
                + " SET sequence_name = 'DescriptiveDataSet' "
                + " WHERE sequence_name = 'WorkingSet'";
        step = SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepName, query, -99);
        stepList.add(step);

        //#2335 Make WorkingSet IdentifiableEntity
        stepName = "Make DescriptiveDataSet IdentifiableEntity";
        tableName = "DescriptiveDataSet";
        step = ClassBaseTypeUpdater.NewAnnotatableToIdentifiableInstance(stepName, tableName, INCLUDE_AUDIT);
        stepList.add(step);

        //TODO add titleCache updater, but maybe not necessary as real data does not really exist
        //except for Campanula test data, and it is easy to update via TaxEditor cache updater


        //#7374 Set titleCache of feature trees to protected
        stepName = "Set titleCache of feature trees to protected";
        query = "UPDATE @@FeatureTree@@ "
                + " SET protectedTitleCache = @TRUE@ ";
        tableName = "FeatureTree";
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, -99);
        stepList.add(step);


        //#7238 rename lastName and firstName
        stepName = "rename lastName";
        tableName = "AgentBase";
        String oldColumnName = "lastname";
        newColumnName = "familyName";
        step = ColumnNameChanger.NewClobInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //... firstName
        stepName = "rename firstName";
        tableName = "AgentBase";
        oldColumnName = "firstname";
        newColumnName = "givenName";
        step = ColumnNameChanger.NewClobInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //#7210 Add salt field for User
        stepName = "Add salt field for User";
        tableName = "UserAccount";
        newColumnName = "salt";
        step = ColumnAdder.NewStringInstance(stepName, tableName, newColumnName, length, INCLUDE_AUDIT);
        stepList.add(step);

        //#6943 Add combination 'in'-author
        stepName = "Add combination 'in'-author";
        tableName = "TaxonName";
        newColumnName = "inCombinationAuthorship_id";
        referencedTable = "AgentBase";
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
        stepList.add(step);

        //#6943 Add basionym 'in'-author
        stepName = "Add basionym 'in'-author";
        tableName = "TaxonName";
        newColumnName = "inBasionymAuthorship_id";
        referencedTable = "AgentBase";
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
        stepList.add(step);

        //#6916 Link IntextReference to OriginalSource
        stepName = "Link IntextReference to OriginalSource";
        tableName = "IntextReference";
        newColumnName = "source_id";
        referencedTable = "OriginalSourceBase";
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
        stepList.add(step);

        //#6720 Make individual count a string
        stepName = "Make individual count a string";
        tableName = "SpecimenOrObservationBase";
        String columnName = "individualCount";
        int size = 255;
        step = ColumnTypeChanger.NewInt2StringInstance(stepName, tableName, columnName, size, INCLUDE_AUDIT, null, !NOT_NULL);
        stepList.add(step);

        updateSpecimenTypeDesignationStatusOrder(stepList);

        //#7144 Set Country area level
        stepName = "Set Country area level";
        query = " UPDATE @@DefinedTermBase@@ " +
                " SET level_id = ( SELECT id FROM (SELECT id FROM DefinedTermBase WHERE uuid = '79db63a4-1563-461e-8e41-48f5722feca4') as drv) " +
                " WHERE DTYPE = 'Country' ";
        step = SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepName, query, -99);
        stepList.add(step);



        //7276  Make User.emailAddress a unique field
        //TODO H2 / PostGreSQL / SQL Server
        //User.email unique
        stepName = "Update User.emailAdress unique index";
        tableName = "UserAccount";
        columnName = "emailAddress";
        step = UsernameConstraintUpdater.NewInstance(stepName, tableName, columnName);
        stepList.add(step);

        return stepList;
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
        ISchemaUpdaterStep step = TableNameChanger.NewInstance(stepName, oldTableName, newTableName, INCLUDE_AUDIT);
        stepList.add(step);

        if (oldTableName.contains("_")){
            stepName = "Rename " +  oldTableName + ".workingSet_id";
            String oldColumnName = "WorkingSet_id";
            String newColumnName = "DescriptiveDataSet_id";
            if ("WorkingSet_DescriptionBase".equals(oldTableName)){
                oldColumnName = "WorkingSets_id";
                newColumnName = "DescriptiveDataSets_id";
            }
            step = ColumnNameChanger.NewIntegerInstance(stepName, newTableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
            stepList.add(step);
        }
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
