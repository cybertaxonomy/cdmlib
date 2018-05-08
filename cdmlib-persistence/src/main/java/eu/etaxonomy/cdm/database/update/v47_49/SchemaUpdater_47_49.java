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
        tableName = "PermissionGroup";
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, -99);
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
                + " WHERE sequence_name = WorkingSet";
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


        //7276  Make User.emailAddress a unique field
        //TODO H2 / PostGreSQL / SQL Server
        //User.email unique
        stepName = "Update User.emailAdress unique index";
        tableName = "UserAccount";
        String columnName = "emailAddress";
        step = UsernameConstraintUpdater.NewInstance(stepName, tableName, columnName);
        stepList.add(step);

        return stepList;
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
