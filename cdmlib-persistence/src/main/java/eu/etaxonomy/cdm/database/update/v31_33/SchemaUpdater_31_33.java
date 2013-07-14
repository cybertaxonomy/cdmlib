// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.update.v31_33;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableDroper;
import eu.etaxonomy.cdm.database.update.v30_31.SchemaUpdater_30_301;


/**
 * NOT YET USED
 * @author a.mueller
 * @created Oct 11, 2011
 */
public class SchemaUpdater_31_33 extends SchemaUpdaterBase {


	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_31_33.class);
	private static final String startSchemaVersion = "3.0.1.0.201104190000";
	private static final String endSchemaVersion = "3.3.0.0.201306010000";
	
// ********************** FACTORY METHOD *******************************************
	
	public static SchemaUpdater_31_33 NewInstance(){
		return new SchemaUpdater_31_33();
	}
	
	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_31_33() {
		super(startSchemaVersion, endSchemaVersion);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterBase#getUpdaterList()
	 */
	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {
		
		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();
		
		//TODO Does it throw exception if table does not exist?
		//Was in Schemaupdater_301_31 which was never used and later deleted (r18331).
		//drop TypeDesignationBase_TaxonNameBase   //from schemaUpdater 301_31
		String stepName = "Drop duplicate TypeDesignation-TaxonName table";
		String tableName = "TypeDesignationBase_TaxonNameBase";
		ISchemaUpdaterStep step = TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//create original source type column
		stepName = "Create original source type column";
		tableName = "OriginalSourceBase";
		String columnName = "type";
		//TODO NOT NULL unclear
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT, true, null);
		stepList.add(step);
		
		//TODO ?? update original source type
		updateOriginalSourceType(stepList);
		
		//create and update elevenation max, remove error column
		updateElevationMax(stepList);
		
		//create taxon node tree index
		stepName = "Create taxon node tree index";
		tableName = "TaxonNode";
		columnName = "treeIndex";
		//TODO NOT NULL unclear
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, 255, INCLUDE_AUDIT);
		stepList.add(step);
		
		//TODO update tree index
		
		//create original source type column
		stepName = "Create taxon node sort index column";
		tableName = "TaxonNode";
		columnName = "sortIndex";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT, false, null);
		stepList.add(step);
		
		//TODO implement sorted behaviour in model first !!
		//TODO update sortindex (similar updater exists already for FeatureNode#sortIndex in schema update 25_30 
		
		//create feature node tree index
		stepName = "Create feature node tree index";
		tableName = "FeatureNode";
		columnName = "treeIndex";
		//TODO NOT NULL unclear
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, 255, INCLUDE_AUDIT);
		stepList.add(step);
				
		//TODO update tree index for feature node
		
		//update introduced: adventitious (casual) label
		//#3540
		stepName = "Update introduced: adventitious (casual) label";
		String query = " UPDATE representation r " + 
				" SET r.abbreviatedlabel = 'ia' " +
				" WHERE r.abbreviatedlabel = 'id' AND r.label = 'introduced: adventitious (casual)' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		//TODO update idInVocabulary for DefinedTerms
		
		//TODO update termType for DefinedTerms and TermVocabulary, no type must be null
		
		//TODO update DTYPE for modifiers (Stage, Scope, Sex, DeterminationModifier, Modifier -> DefinedTerm)
		
		
		//TODO update rankClass (#3521)
		
		//TODO change column type for DistanceToWaterSurface und DistanceToGround
		
		//TODO add column for DistanceToWaterSurfaceMax/Text und DistanceToGroundMax/Text
		
		//TODO update datatype of sequence.sequence => CLOB (keeping data not necessary #3325)
		//NOTE: column has been changed: #3360

		//TODO update datatype->CLOB for URIs. (DefinedTerms, TermVocabulary, Reference
		//Rights, MediaRepresentationPart ) #3345
				
		//update Sicilia -> Sicily
		//#3540
		stepName = "Update Sicilia -> Sicily";
		query = " UPDATE representation r " + 
				" SET r.label = 'Sicily', r.text = 'Sicily' " +
				" WHERE (r.abbreviatedlabel = 'SIC-SI'  OR r.abbreviatedlabel = 'SIC')  AND r.label = 'Sicilia' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		//remove homotypical group form type designation base
		stepName = "Remove column homotypical group in type designation base";
		tableName = "TypeDesignationBase";
		String oldColumnName = "homotypicalgroup_id";
		step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//add publish flag to taxon
		stepName = "Add publish flag column to taxon base";
		tableName = "TaxonBase";
		columnName = "publish";
		step = ColumnAdder.NewBooleanInstance(stepName, tableName, columnName, INCLUDE_AUDIT, true);
		stepList.add(step);
		
		//add publish flag to specimen
		stepName = "Add publish flag column to SpecimenOrObservationBase";
		tableName = "SpecimenOrObservationBase";
		columnName = "publish";
		step = ColumnAdder.NewBooleanInstance(stepName, tableName, columnName, INCLUDE_AUDIT, true);
		stepList.add(step);
		
		//TODO add columns abbrevTitle, abbrevTitleCache and protectedAbbrevTitleCache to Reference
		
		
		//add start number to PolytomousKey
		stepName = "Add start number column to PolytomousKey";
		tableName = "PolytomousKey";
		columnName = "startNumber";
		Integer defaultValue = 1;
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT,  defaultValue, true); 
		stepList.add(step);
		
		//TODO add specimenOrObservation basis of record to SpecimenOrObservationBase
		
		//TODO update specimenOrObservationBase DTYPE with DefinedTerm where necessary


		//TODO update DTYPE FieldObservation -> FieldUnit #3351
		stepName = "Update FieldObservation -> FieldUnit";
		query = " UPDATE SpecimenOrObservationBase sob " + 
				" SET sob.DTYPE = 'FieldUnit' " +
				" WHERE sob.DTYPE = 'FieldObservation' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		//add kindOfUnit to SpecimenOrObservationBase
		stepName = "Add kindOfUnit column to SpecimenOrObservationBase";
		tableName = "SpecimenOrObservationBase";
		columnName = "kindOfUnit";
		String relatedTable = "DefinedTermBase";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT,  true, relatedTable); 
		stepList.add(step);

		//TODO remove citation_id and citationmicroreference columns from Media table #2541
		//first check if columns are always empty
		
		//TODO update length of all title caches and full title cache in names
		//https://dev.e-taxonomy.eu/trac/ticket/1592
		
		//TODO rename FK column states_id -> stateData_id in DescriptionElementBase_StateData(+AUD)  #2923
		
		//TODO add sortIndex column to TaxonNode and fill with values (compare with FeatureNode filling, however, this
//		had a bad performance
		
		//specimen descriptions #3571
		//TODO add column DescriptionBase.Specimen_ID  #3571
		stepName = "Add specimen_id column to DescriptionBase";
		tableName = "SpecimenOrObservationBase";
		columnName = "specimen_id";
		boolean notNull = false;
		String referencedTable = "SpecimenOrObservationBase";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT, notNull, referencedTable);
		stepList.add(step);
		
		//TODO update DescriptionBase.Specimen_ID data  #3571

		//TODO remove tables DescriptionBase_SpecimenOrObservationBase(_AUD)  #3571
		stepName = "Remove table DescriptionBase_SpecimenOrObservationBase";
		tableName = "DescriptionBase_SpecimenOrObservationBase";
		TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//TODO create table CdmPreferences  #3555
		
		//TODO fill CdmPreferences with default values
		
		//update RightsTerm to RightsType #1306
		stepName = "Update RightsTerm -> RightsType";
		String updateSql = "UPDATE DefinedTermBase SET DTYPE = 'RightsType'  WHERE DTYPE = 'RightsTerm'";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, updateSql);
		stepList.add(step);
		
		//remove table Sequence_GenBankAccession #3552
		stepName = "Remove table Sequence_GenBankAccession";
		tableName = "Sequence_GenBankAccession";
		TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//remove table GenBankAccession #3552
		stepName = "Remove table GenBankAccession";
		tableName = "GenBankAccession";
		TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//TODO (read first #3360) add columns GeneticAccessionNumber(String) to Sequence 

		//TODO update molecular data #3360
		
		
		//add MediaSpecimen column #3614
		stepName = "Add mediaSpecimen column to SpecimenOrObservationBase";
		tableName = "SpecimenOrObservationBase";
		columnName = "mediaSpecimen_id";
		notNull = false;
		referencedTable = "Media";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT, notNull, referencedTable);
		stepList.add(step);
		
		//remove DescriptionBase_Feature  #2202
		stepName = "Remove table DescriptionBase_Feature";
		tableName = "DescriptionBase_Feature";
		TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//add timeperiod to columns to description element base #3312
		addTimeperiodToDescriptionElement(stepList);


		//TODO add Marker vocabulary and terms #3591 => TermUpdater
		
		//remove SpecimenOrObservationBase_Media #3597
		  //TODO check if SpecimenOrObservationBase_Media has data => move to first position, don't run update if data exists
		if (true){
			throw new RuntimeException("Required check for SpecimenOrObservationBase_Media");
		}
		
		  //SpecimenOrObservationBase_Media #3597
		stepName = "Remove table SpecimenOrObservationBase_Media";
		tableName = "SpecimenOrObservationBase_Media";
		TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
		stepList.add(step);
		
		
		
		
		return stepList;
	}

	/**
	 * @param stepList
	 * @return
	 */
	private void addTimeperiodToDescriptionElement(
			List<ISchemaUpdaterStep> stepList) {
		String stepName;
		String tableName;
		ISchemaUpdaterStep step;
		String columnName;
		//start  #3312
		stepName = "Create time period start column in description element base";
		tableName = "DescriptionElementBase";
		columnName = "timeperiod_start";
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, INCLUDE_AUDIT);
		stepList.add(step);

		//end #3312
		stepName = "Create time period end column in description element base";
		tableName = "DescriptionElementBase";
		columnName = "timeperiod_end";
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, INCLUDE_AUDIT);
		stepList.add(step);

		//freetext #3312
		stepName = "Create time period freetext column in description element base";
		tableName = "DescriptionElementBase";
		columnName = "timeperiod_freetext";
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		return;
	}

	private void updateElevationMax(List<ISchemaUpdaterStep> stepList) {
		//create column
		String stepName = "Create absoluteElevationMax column";
		String tableName = "GatheringEvent";
		String columnName = "absoluteElevationMax";
		ISchemaUpdaterStep step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT, false, null);
		stepList.add(step);
		
		
		//update max
		stepName = "Update gathering elevation max";
		//all audits to unknown type
		String query = " UPDATE GatheringEvent ge " + 
				" SET ge.absoluteElevationMax = ge.elevation + ge.elevationErrorRadius,  " +
				"     ge.absoluteElevation =  ge.elevationErrorRadius - ge.elevationErrorRadius" +
				" WHERE ge.elevationErrorRadius is not null ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		//TODO same for AUD
		
		//remove error column
		stepName = "Remove elevationErrorRadius column";
		tableName = "GatheringEvent";
		columnName = "elevationErrorRadius";
		step = ColumnRemover.NewInstance(stepName, tableName, columnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		
	}

	/**
	 * @param stepList
	 */
	private void updateOriginalSourceType(List<ISchemaUpdaterStep> stepList) {
		String stepName;
		ISchemaUpdaterStep step;
		stepName = "Create original source type column";
		//all audits to unknown type
		String query = "UPDATE OriginalSourceBase_AUD SET type = 0 ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		 //all data to unknown
		query = "UPDATE OriginalSourceBase SET type = 0 ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		 //all imports recognized by idInSOurce and by missing nameInSource
		query = "UPDATE OriginalSourceBase SET type = 3 WHERE " +
				"((idInSource IS NOT NULL) OR (idNamespace IS NOT NULL))  AND " +
				"( nameUsedInSource IS NULL AND originalNameString IS NULL ) ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		 //all imports recognized by idInSOurce and by missing nameInSource
		query = "UPDATE OriginalSourceBase SET type = 1 WHERE " +
				"(idInSource IS NULL AND idNamespace IS NULL) AND " +
				"( citation IS NOT NULL ) ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterBase#getNextUpdater()
	 */
	@Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterBase#getPreviousUpdater()
	 */
	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_30_301.NewInstance();
	}

}
