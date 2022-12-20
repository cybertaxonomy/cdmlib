/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v30_40;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.database.update.ClassBaseTypeUpdater;
import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.database.update.TableDropper;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author a.mueller
 * @since Mar 01, 2015
 */
public class SchemaUpdater_341_35 extends SchemaUpdaterBase {

	private static final String startSchemaVersion = "3.4.1.0.201411210000";
	private static final String endSchemaVersion = "3.5.0.0.201531030000";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_341_35 NewInstance() {
		return new SchemaUpdater_341_35();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_341_35() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;
		String query;
		String columnNames[];
		String referencedTables[];
		String columnTypes[];

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();


		//IntextReference
		//#4706
		stepName = "Add IntextReference table";
		tableName = "IntextReference";
		columnNames = new String[]{"startpos","endpos","agent_id","annotation_id",
				"languagestring_id","media_id","occurrence_id","reference_id","taxon_id","taxonname_id"};
		referencedTables = new String[]{null, null, "AgentBase","Annotation","LanguageString","Media",
				"SpecimenOrObservationBase","Reference","TaxonBase","TaxonNameBase"};
		columnTypes = new String[]{"int","int","int","int","int","int","int","int","int","int"};
		TableCreator.NewVersionableInstance(stepList, stepName, tableName, columnNames,
				columnTypes, referencedTables, INCLUDE_AUDIT);

		//Drop EntityValidationResult and EntityConstraintViolation
		//#4709
		stepName = "Drop EntityConstraintViolation table";
		tableName = "EntityConstraintViolation";
		TableDropper.NewInstance(stepList, stepName, tableName, !INCLUDE_AUDIT);

		stepName = "Drop EntityValidationResult table";
		tableName = "EntityValidationResult";
		TableDropper.NewInstance(stepList, stepName, tableName, !INCLUDE_AUDIT);

        //... and create new entity validation and
        stepName = "Create EntityValidation table";
        tableName = "EntityValidation";
        columnNames = new String[]{"updated","crudeventtype","userfriendlydescription","userfriendlytypename",
                "validatedentityclass","validatedentityid","validatedentityuuid", "validationcount", "status"};
        columnTypes = new String[]{"datetime","string_255","string_255","string_255","string_255","int","string_36","int","string_20"};
        referencedTables = new String[]{null,null,null,null,null,null,null,null,null};
        TableCreator.NewNonVersionableInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables);

        //... constraint violation
		stepName = "Create EntityConstraintViolation table";
        tableName = "EntityConstraintViolation";
        columnNames = new String[]{"invalidvalue","message","propertypath","severity","userfriendlyfieldname",
        		"validationgroup","validator","entityvalidation_id"};
        columnTypes = new String[]{"string_255","string_255","string_255","string_255","string_255","string_255","string_255","int"};
        referencedTables = new String[]{null,null,null,null,null,null,null,"EntityValidationResult"};
        TableCreator.NewNonVersionableInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables);

        //Delete orphaned taxon nodes #2341
        stepName = "Delete orhphaned taxon nodes";
        String sql = "DELETE FROM @@TaxonNode@@ WHERE classification_id IS NULL";
        tableName = "TaxonNode";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        //identifier versionable -> annotatable
        stepName = "Upgrade identifier from versionable to annotatable";
        tableName = "Identifier";
        ClassBaseTypeUpdater.NewVersionableToAnnotatableInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

        //agent - collector title  #4311
        stepName = "Add collector title for TeamOrPersonBase";
        tableName = "AgentBase";
        newColumnName = "collectorTitle";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        //agent - collector title  #4311
        stepName = "Add protectedCollectorTitleCache to Team";
        tableName = "AgentBase";
        newColumnName = "protectedCollectorTitleCache";
        ColumnAdder.NewBooleanInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, false);


		//update DerivationEvent.taxonname_id  #4578, #3448, #4203, #4518
		stepName = "Update taxon name in derivation event";
		query = "UPDATE DeterminationEvent " +
				" SET taxonname_id = (SELECT name_id FROM TaxonBase tb WHERE tb.id = taxon_id) " +
				" WHERE taxon_id IS NOT NULL ";
		tableName = "DeterminationEvent";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, "");


        //#4110 update idInVocabulary for some new databases
        updateAreas(stepList);

		return stepList;

	}

	//#4110 update idInVocabulary for some new databases
    private void updateAreas(List<ISchemaUpdaterStep> stepList) {
		String stepName;
		String uuid;
		String tableName = "DefinedTermBase";

		//ANSI - SQL
		String queryVocUuid = " UPDATE @@DefinedTermBase@@ "
				+ " SET idInVocabulary = " +
					" (SELECT abbreviatedlabel "
					+ " FROM @@DefinedTermBase_Representation@@ MN "
					+ " INNER JOIN @@Representation@@ r ON r.id = MN.representations_id "
					+ " WHERE MN.DefinedTermBase_id = @@DefinedTermBase@@.id) "
				+ " WHERE idInVocabulary IS NULL AND EXISTS (SELECT * FROM @@TermVocabulary@@ voc WHERE voc.id = @@DefinedTermBase@@.vocabulary_id " +
						" AND voc.uuid = '%s') ";

	    // Country => all
		stepName = "Update idInVocabulary for Countries if necessary";
		uuid = Country.uuidCountryVocabulary.toString();
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid))
				.setDefaultAuditing(tableName);

		// TdwgAreas => all
		stepName = "Update idInVocabulary for TDWG areas if necessary";
		uuid = NamedArea.uuidTdwgAreaVocabulary.toString();
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid))
				.setDefaultAuditing(tableName);

		// Waterbody => all
		stepName = "Update idInVocabulary for Waterbody if necessary";
		uuid = NamedArea.uuidWaterbodyVocabulary.toString();
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName,
				String.format(queryVocUuid, uuid))
				.setDefaultAuditing(tableName);

		// Continent => None has an id
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_34_341.NewInstance();
	}
}