/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v33_34;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.database.update.TableDroper;
import eu.etaxonomy.cdm.database.update.v34_35.SchemaUpdater_341_35;

/**
 * @author a.mueller
 * @since Jan 14, 2014
 */
public class SchemaUpdater_34_341 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_34_341.class);
	private static final String endSchemaVersion = "3.4.1.0.201411210000";
	private static final String startSchemaVersion = "3.4.0.0.201407010000";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_34_341 NewInstance() {
		return new SchemaUpdater_34_341();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_34_341() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
//		String columnName;
		String newColumnName;
		String oldColumnName;
		String query;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//DnaMarker in Primer
		//TODO H2 / PostGreSQL / SQL Server
		stepName = "Add foreign key for Primer.dnaMarker";
		tableName = "Primer";
		newColumnName = "dnaMarker_id";
		boolean notNull = false;
		String referencedTable = "DefinedTermBase";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);

		//Institution for DerivationEvent
		stepName = "Add foreign key for DerivationEvent.institution";
		tableName = "DerivationEvent";
		newColumnName = "institution_id";
		referencedTable = "AgentBase";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);

		//Institution for Amplication
		stepName = "Add foreign key for Amplification.institution";
		tableName = "Amplification";
		newColumnName = "institution_id";
		referencedTable = "AgentBase";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);

		//TaxonName for DeterminationEvent
		stepName = "Add foreign key for DeterminationEvent.taxonName";
		tableName = "DeterminationEvent";
		newColumnName = "taxonname_id";
		referencedTable = "TaxonNameBase";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);

		//TaxonName for DeterminationEvent #3448, #4203, #4518
		stepName = "Add foreign key for DeterminationEvent.taxonName";
		tableName = "DnaQuality";
		newColumnName = "typedPurificationMethod_id";
		referencedTable = "MaterialOrMethodEvent";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);

		//update DerivationEvent.taxonname_id #3448, #4203, #4518
		stepName = "Update taxon name in derivation event";
		query = "UPDATE DeterminationEvent " +
				" SET taxonname_id = (SELECT name_id FROM TaxonBase tb WHERE tb.id = taxon_id) " +
				" WHERE taxon_id IS NOT NULL ";
		tableName = "DeterminationEvent";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, "", -99);

		mergePresenceAbsenceVocs(stepList);


		//SingleReadAlignment #4529
		stepName = "Remove Sequence_SingleRead";  //we assume that this field is not yet used
		tableName = "Sequence_SingleRead";
		TableDroper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT, true);

 		//Add SingleReadAlignment #4529
 		stepName = "Add SingleReadAlignment";
 		tableName = "SingleReadAlignment";
 		String[] columnNames = new String[]{"shifts","editedsequence","reversecomplement",
 				"consensusalignment_id","singleread_id"};
 		String[] columnTypes = new String[]{"clob","clob","bit","int","int"};
 		String[] referencedTables = new String[]{null, null,null,"Sequence","SingleRead"};
 		TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes,
 				referencedTables, INCLUDE_AUDIT, true);

 		//Add labelCache to amplification #4542
 		stepName = "Add column 'labelCache'";
		tableName = "Amplification";
		newColumnName = "labelcache";
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);


 		//SPLIT Amplification and Amplification result

 		// Amplification #4541
		stepName = "Create table 'AmplificationResult'";
		tableName = "AmplificationResult";
		TableCreator.NewAnnotatableInstance(stepList, stepName, tableName,
				new String[] { "successful", "successText", "dnaSample_id", "amplification_id",
						"cloning_id", "gelPhoto_id",
						}, // colNames
				new String[] {"bit", "string_255", "int", "int", "int", "int"}, // columnTypes
				new String[] { null, null, "SpecimenOrObservationBase", "Amplification", "MaterialOrMethodEvent",
						"DefinedTermBase", "Media" }, // referencedTables
				INCLUDE_AUDIT);

//		// amplification result - single reads #4541
//		stepName = "Add single reads to amplification result";
//		String firstTable = "AmplificationResult";
//		String secondTable = "SingleRead";
//		MnTableCreator
//				.NewMnInstance(stepList, stepName, firstTable, null, secondTable, null,
//						SchemaUpdaterBase.INCLUDE_AUDIT, false, true);

		//Institution for Amplication
		stepName = "Add foreign key for SingleRead.amplificationresult";
		tableName = "SingleRead";
		newColumnName = "amplificationresult_id";
		referencedTable = "AmplificationResult";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);

		//drop Amplification_SingleRead #4541
		stepName = "Drop Amplification_SingleRead";
		tableName = "Amplification_SingleRead";
		TableDroper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT, true);

		//remove successful, successText, ...
		stepName = "Remove successful ... from Amplification";
 		tableName = "Amplification";
 		oldColumnName = "successful";
 		ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);
 		oldColumnName = "successText";
 		ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);
 		oldColumnName = "dnaSample_id";
 		ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);
 		oldColumnName = "cloning_id";
 		ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);
 		oldColumnName = "gelPhoto_id";
 		ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

 		stepName = "Remove amplification_id from SingleRead";
 		tableName = "SingleRead";
		oldColumnName = "amplification_id";
 		ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);


		//SpecimenOrObservationBase_Sequence (was incorrect mapping before)
		stepName = "Remove SpecimenOrObservationBase_Sequence";
		tableName = "SpecimenOrObservationBase_Sequence";
		TableDroper.NewInstance(stepList, stepName, tableName, true, true);

		return stepList;

	}

	private void mergePresenceAbsenceVocs(List<ISchemaUpdaterStep> stepList) {
		String stepName;
		String tableName;
		String newColumnName;
		String query;
		//PAT
		//ad absence term
		stepName = "Create absenceterm column";
		tableName = "DefinedTermBase";
		newColumnName = "absenceterm";
		ColumnAdder.NewBooleanInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, null);

 		//set default value
		stepName ="Update AbsenceTerm vocabulary";
		tableName = "DefinedTermBase";
		query = " UPDATE @@DefinedTermBase@@ " +
                " SET absenceterm = @@FALSE@@ " +
                " WHERE termType = 'PAT' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName, 99);

 		//vocabulary for absence terms
		stepName ="Update AbsenceTerm vocabulary";
		tableName = "DefinedTermBase";
		query = " UPDATE @@DefinedTermBase@@ " +
                " SET absenceterm = @@TRUE@@, "
                	+ " vocabulary_id = "
                			+ "(SELECT id FROM @@TermVocabulary@@ "
                			+ " WHERE uuid = 'adbbbe15-c4d3-47b7-80a8-c7d104e53a05'),"
                	  + " orderindex = orderindex + "
                	  		+ " (SELECT max(orderindex) FROM "
                	  		+ " (SELECT * FROM DefinedTermBase dtb2 "
                	  		+ " WHERE dtb2.termtype = 'PAT' AND dtb2.absenceterm = 0 "
                	  		+ ") as tmp )" +
                " WHERE DTYPE = 'AbsenceTerm' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName, 99);

 		//PAT  - DTYPE
 		stepName ="Update PresenceAbsenceTerms DTYPE";
		tableName = "DefinedTermBase";
		query = " UPDATE @@DefinedTermBase@@ " +
                " SET DTYPE = 'PresenceAbsenceTerm' " +
                " WHERE termType = 'PAT' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName, 99);

 		//PAT  - remove absence vocabulary
 		stepName ="Remove Absence Vocabulary I";
		tableName = "TermVocabulary_Representation";
 		query = " DELETE FROM TermVocabulary_Representation " +
				" WHERE TermVocabulary_id in (SELECT id FROM TermVocabulary WHERE uuid = '5cd438c8-a8a1-4958-842e-169e83e2ceee') ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName, 99);

 		//PAT  - remove absence vocabulary
 		stepName ="Remove Absence Vocabulary II";
		tableName = "TermVocabulary_Representation";
 		query = " DELETE FROM TermVocabulary"
 				+ " WHERE uuid = '5cd438c8-a8a1-4958-842e-169e83e2ceee' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName, 99);

 		//PAT  - remove absence vocabulary
 		stepName ="Remove Absence Vocabulary III";
		tableName = "Representation";
 		query = " DELETE FROM Representation "
 				+ " WHERE text = 'AbsenceTerm'  AND label = 'AbsenceTerm' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName, 99);

 		//PAT  - update representation
 		stepName ="Update Presence Absence vocabulary representation";
		tableName = "Representation";
 		query = " UPDATE Representation "
 				+ " SET text = 'Presence Absence Term', label = 'Presence Absence Term' "
 				+ " WHERE text = 'Presence Term'  AND label = 'Presence Term' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName, 99);

 		//PAT  - update titlecache
 		stepName ="Update Presence Absence vocabulary titlecache";
		tableName = "TermVocabulary";
 		query = " UPDATE TermVocabulary "
 				+ " SET titleCache = 'Presence Absence Term' "
 				+ " WHERE uuid = 'adbbbe15-c4d3-47b7-80a8-c7d104e53a05' ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName, 99);
	}



	@Override
	public ISchemaUpdater getNextUpdater() {
		return SchemaUpdater_341_35.NewInstance();
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_331_34.NewInstance();
	}

}
