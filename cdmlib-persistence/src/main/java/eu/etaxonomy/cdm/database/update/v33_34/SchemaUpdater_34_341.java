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
		ISchemaUpdaterStep step;
//		String columnName;
		String newColumnName;
		String oldColumnName;
		String query;

		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();
		
		//DnaMarker in Primer
		//TODO H2 / PostGreSQL / SQL Server
		stepName = "Add foreign key for Primer.dnaMarker";
		tableName = "Primer";
		newColumnName = "dnaMarker_id";
		boolean notNull = false;
		String referencedTable = "DefinedTermBase";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);
		stepList.add(step);
		
		//Institution for DerivationEvent
		stepName = "Add foreign key for DerivationEvent.institution";
		tableName = "DerivationEvent";
		newColumnName = "institution_id";
		referencedTable = "AgentBase";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);
		stepList.add(step);

		//Institution for Amplication
		stepName = "Add foreign key for Amplification.institution";
		tableName = "Amplification";
		newColumnName = "institution_id";
		referencedTable = "AgentBase";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);
		stepList.add(step);
		
		//TaxonName for DeterminationEvent
		stepName = "Add foreign key for DeterminationEvent.taxonName";
		tableName = "DeterminationEvent";
		newColumnName = "taxonname_id";
		referencedTable = "TaxonNameBase";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);
		stepList.add(step);
		
		//TaxonName for DeterminationEvent #3448, #4203, #4518
		stepName = "Add foreign key for DeterminationEvent.taxonName";
		tableName = "DnaQuality";
		newColumnName = "typedPurificationMethod_id";
		referencedTable = "MaterialOrMethodEvent";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);
		stepList.add(step);
		
		
		//update DerivationEvent.taxonname_id #3448, #4203, #4518
		stepName = "Update taxon name in derivation event";
		query = "UPDATE DeterminationEvent " +
				" SET taxonname_id = (SELECT name_id FROM TaxonBase tb WHERE tb.id = taxon_id) " + 
				" WHERE taxon_id IS NOT NULL ";
		tableName = "DeterminationEvent";
		step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, "", -99);
		stepList.add(step);
 		
		mergePresenceAbsenceVocs(stepList);
	
		
		//SingleReadAlignment #4529
		stepName = "Remove Sequence_SingleRead";  //we assume that this field is not yet used
		tableName = "Sequence_SingleRead";
		step = TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT, true);
 		stepList.add(step);
 		
 		//Add SingleReadAlignment #4529
 		stepName = "Add SingleReadAlignment";
 		tableName = "SingleReadAlignment";
 		String[] columnNames = new String[]{"shifts","editedsequence","reversecomplement",
 				"consensusalignment_id","singleread_id"};
 		String[] columnTypes = new String[]{"clob","clob","bit","int","int"};
 		String[] referencedTables = new String[]{null, null,null,"Sequence","SingleRead"};
 		step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, 
 				referencedTables, INCLUDE_AUDIT, true);
 		stepList.add(step);
 		
 		//Add labelCache to amplification #4542
 		stepName = "Add column 'labelCache'";
		tableName = "Amplification";
		newColumnName = "labelcache";
		step = ColumnAdder.NewStringInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT);
		stepList.add(step);
		
 		
 		//SPLIT Amplification and Amplification result
 		
 		// Amplification #4541
		stepName = "Create table 'AmplificationResult'";
		tableName = "AmplificationResult";
		step = TableCreator.NewAnnotatableInstance(stepName, tableName,
				new String[] { "successful", "successText", "dnaSample_id", "amplification_id",
						"cloning_id", "gelPhoto_id",
						}, // colNames
				new String[] {"bit", "string_255", "int", "int", "int", "int"}, // columnTypes
				new String[] { null, null, "SpecimenOrObservationBase", "Amplification", "MaterialOrMethodEvent",
						"DefinedTermBase", "Media" }, // referencedTables
				INCLUDE_AUDIT);
		stepList.add(step);
		
//		// amplification result - single reads #4541
//		stepName = "Add single reads to amplification result";
//		String firstTable = "AmplificationResult";
//		String secondTable = "SingleRead";
//		step = MnTableCreator
//				.NewMnInstance(stepName, firstTable, null, secondTable, null,
//						SchemaUpdaterBase.INCLUDE_AUDIT, false, true);
//		stepList.add(step);
		
		//Institution for Amplication
		stepName = "Add foreign key for SingleRead.amplificationresult";
		tableName = "SingleRead";
		newColumnName = "amplificationresult_id";
		referencedTable = "AmplificationResult";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);
		stepList.add(step);


		//drop Amplification_SingleRead #4541
		stepName = "Drop Amplification_SingleRead";
		tableName = "Amplification_SingleRead";
		step = TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT, true);
		stepList.add(step);
		
		//remove successful, successText, ...
		stepName = "Remove successful ... from Amplification";
 		tableName = "Amplification";
 		oldColumnName = "successful";
 		step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, INCLUDE_AUDIT);
 		stepList.add(step);
 		oldColumnName = "successText";
 		step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, INCLUDE_AUDIT);
 		stepList.add(step);
 		oldColumnName = "dnaSample_id";
 		step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, INCLUDE_AUDIT);
 		stepList.add(step);
 		oldColumnName = "cloning_id";
 		step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, INCLUDE_AUDIT);
 		stepList.add(step);
 		oldColumnName = "gelPhoto_id";
 		step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, INCLUDE_AUDIT);
 		stepList.add(step);
 		
 		stepName = "Remove amplification_id from SingleRead";
 		tableName = "SingleRead";
		oldColumnName = "amplification_id";
 		step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, INCLUDE_AUDIT);
 		stepList.add(step);
 
 	
 		
		//SpecimenOrObservationBase_Sequence (was incorrect mapping before)
		stepName = "Remove SpecimenOrObservationBase_Sequence";
		tableName = "SpecimenOrObservationBase_Sequence";
		step = TableDroper.NewInstance(stepName, tableName, true, true);
		stepList.add(step);
		
		return stepList;
		
	}

	private void mergePresenceAbsenceVocs(List<ISchemaUpdaterStep> stepList) {
		String stepName;
		String tableName;
		ISchemaUpdaterStep step;
		String newColumnName;
		String query;
		//PAT
		//ad absence term
		stepName = "Create absenceterm column";
		tableName = "DefinedTermBase";
		newColumnName = "absenceterm";
		step = ColumnAdder.NewBooleanInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, null);
		stepList.add(step);

 		//set default value
		stepName ="Update AbsenceTerm vocabulary";
		tableName = "DefinedTermBase";
		query = " UPDATE @@DefinedTermBase@@ " +
                " SET absenceterm = @@FALSE@@ " +
                " WHERE termType = 'PAT' ";
		step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, 99);
 		stepList.add(step);
		
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
		step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, 99);
 		stepList.add(step);
 
 		//PAT  - DTYPE
 		stepName ="Update PresenceAbsenceTerms DTYPE";
		tableName = "DefinedTermBase";
		query = " UPDATE @@DefinedTermBase@@ " +
                " SET DTYPE = 'PresenceAbsenceTerm' " +
                " WHERE termType = 'PAT' ";
		step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, 99);
 		stepList.add(step);
 		
 		//PAT  - remove absence vocabulary
 		stepName ="Remove Absence Vocabulary I";
		tableName = "TermVocabulary_Representation";
 		query = " DELETE FROM TermVocabulary_Representation " + 
				" WHERE TermVocabulary_id in (SELECT id FROM TermVocabulary WHERE uuid = '5cd438c8-a8a1-4958-842e-169e83e2ceee') ";
		step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, 99);
 		stepList.add(step);
 		
 		//PAT  - remove absence vocabulary
 		stepName ="Remove Absence Vocabulary II";
		tableName = "TermVocabulary_Representation";
 		query = " DELETE FROM TermVocabulary"
 				+ " WHERE uuid = '5cd438c8-a8a1-4958-842e-169e83e2ceee' ";
		step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, 99);
 		stepList.add(step);

 		//PAT  - remove absence vocabulary
 		stepName ="Remove Absence Vocabulary III";
		tableName = "Representation";
 		query = " DELETE FROM Representation "
 				+ " WHERE text = 'AbsenceTerm'  AND label = 'AbsenceTerm' ";
		step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, 99);
 		stepList.add(step);
 		
 		//PAT  - update representation
 		stepName ="Update Presence Absence vocabulary representation";
		tableName = "Representation";
 		query = " UPDATE Representation "
 				+ " SET text = 'Presence Absence Term', label = 'Presence Absence Term' "
 				+ " WHERE text = 'Presence Term'  AND label = 'Presence Term' ";
		step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, 99);
 		stepList.add(step);
 		
 		//PAT  - update titlecache
 		stepName ="Update Presence Absence vocabulary titlecache";
		tableName = "TermVocabulary";
 		query = " UPDATE TermVocabulary "
 				+ " SET titleCache = 'Presence Absence Term' "
 				+ " WHERE uuid = 'adbbbe15-c4d3-47b7-80a8-c7d104e53a05' ";
		step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, 99);
 		stepList.add(step);
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
