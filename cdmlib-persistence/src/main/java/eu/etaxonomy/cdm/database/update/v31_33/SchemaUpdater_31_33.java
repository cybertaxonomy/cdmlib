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

import com.sun.tools.xjc.reader.gbind.Sequence;

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
import eu.etaxonomy.cdm.database.update.TableDroper;
import eu.etaxonomy.cdm.database.update.v30_31.SchemaUpdater_30_301;
import eu.etaxonomy.cdm.model.agent.InstitutionType;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.MaterialAndMethod;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.NaturalLanguageTerm;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.location.Continent;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.RankClass;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;


/**
 * NOT YET USED
 * @author a.mueller
 * @created Oct 11, 2011
 */
public class SchemaUpdater_31_33 extends SchemaUpdaterBase {

	private static final Logger logger = Logger.getLogger(SchemaUpdater_31_33.class);
	private static final String startSchemaVersion = "3.0.1.0.201104190000";
	private static final String endSchemaVersion = "3.3.0.0.201308010000";
	
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
		
		//CHECKS
		
		//remove SpecimenOrObservationBase_Media #3597
		  //TODO check if SpecimenOrObservationBase_Media has data => move to first position, don't run update if data exists
		if (false){
			throw new RuntimeException("Required check for SpecimenOrObservationBase_Media");
		}else{
			logger.warn("CHECKS for inconsistent data not running !!!!");
		}
		
		
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
		String columnName = "sourceType";
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, 4, INCLUDE_AUDIT);
		((ColumnAdder)step).setNotNull(true);
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
		
		//idInVocabulary for DefinedTerms
		stepName = "Create idInVocabulary column in DefinedTermBase";
		tableName = "DefinedTermBase";
		columnName = "idInVocabulary";
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, 255, INCLUDE_AUDIT);
		stepList.add(step);
		//TODO update idInVocabulary
		
		
		//termType for DefinedTerms and TermVocabulary, no type must be null
		stepName = "Create termType column in DefinedTermBase";
		tableName = "DefinedTermBase";
		columnName = "termType";
		//TODO NOT NULL unclear
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, 255, INCLUDE_AUDIT);
		stepList.add(step);
		
		stepName = "Create termType column in TermVocabulary";
		tableName = "TermVocabulary";
		columnName = "termType";
		//TODO NOT NULL unclear
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, 4, INCLUDE_AUDIT);
		stepList.add(step);
		
		
		//update termType for DefinedTerms, no type must be null
		updateTermTypesForTerms(stepList);
		
		//update termType for TermVocabulary, no type must be null
		updateTermTypesForVocabularies(stepList);
		
		//update DTYPE of DefinedTerms
		updateDtypeOfDefinedTerms(stepList);
		
		
		//rankClass (#3521)
		stepName = "Create rankClass column in DefinedTermBase";
		tableName = "DefinedTermBase";
		columnName = "rankClass";
		//TODO NOT NULL unclear
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, 255, INCLUDE_AUDIT);
		stepList.add(step);
		
		//update rankClass (#3521)
		updateRankClass(stepList);
		
		//update datatype->CLOB for URIs. (DefinedTerms, TermVocabulary, Reference, Rights, MediaRepresentationPart ) 
		//#3345,    TODO adapt type to <65k
		//TODO sequence.sequence has been changed #3360
		changeUriType(stepList);

				
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
		stepName = "Add abbrevTitle to Reference";
		tableName = "Reference";
		columnName = "abbrevTitle";
		int length = 255;
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, length, INCLUDE_AUDIT);
		stepList.add(step);

		stepName = "Add abbrevTitleCache to Reference";
		tableName = "Reference";
		columnName = "abbrevTitleCache";
		length = 1023;
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, length, INCLUDE_AUDIT);
		stepList.add(step);
		
		stepName = "Add protectedAbbrevTitleCache to Reference";
		tableName = "Reference";
		columnName = "protectedAbbrevTitleCache";
		step = ColumnAdder.NewBooleanInstance(stepName, tableName, columnName, INCLUDE_AUDIT, false); 
		stepList.add(step);
		
		//add doi to reference
		stepName = "Add doi to Reference";
		tableName = "Reference";
		columnName = "doi";
		length = 255;
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, length, INCLUDE_AUDIT);
		stepList.add(step);
		
		
		//add start number to PolytomousKey
		stepName = "Add start number column to PolytomousKey";
		tableName = "PolytomousKey";
		columnName = "startNumber";
		Integer defaultValue = 1;
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT,  defaultValue, true); 
		stepList.add(step);
		
		//add recordBasis  to specimenOrObservationBase 
		stepName = "Add recordBasis to SpecimenOrObservationBase";
		tableName = "SpecimenOrObservationBase";
		columnName = "recordBasis";
		length = 4;
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, length, INCLUDE_AUDIT);
		stepList.add(step);
				
		
		
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
		columnName = "kindOfUnit_id";
		String relatedTable = "DefinedTermBase";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT,  true, relatedTable); 
		stepList.add(step);

		//TODO remove citation_id and citationmicroreference columns from Media table #2541
		//first check if columns are always empty
		
		//TODO update length of all title caches and full title cache in names
		//https://dev.e-taxonomy.eu/trac/ticket/1592
		
		//rename FK column states_id -> stateData_id in DescriptionElementBase_StateData(+AUD)  #2923
		stepName = "Update states_id to stateData_id in DescriptionElementBase_StateData";
		tableName = "DescriptionElementBase_StateData";
		oldColumnName = "states_id";
		String newColumnName = "stateData_id";
		step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		
		//TODO add sortIndex column to TaxonNode and fill with values (compare with FeatureNode filling, however, this
//		had a bad performance
		
		//specimen descriptions #3571
		//TODO add column DescriptionBase.Specimen_ID  #3571
		stepName = "Add specimen_id column to DescriptionBase";
		tableName = "DescriptionBase";
		columnName = "specimen_id";
		boolean notNull = false;
		String referencedTable = "SpecimenOrObservationBase";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT, notNull, referencedTable);
		stepList.add(step);
		
		//TODO update DescriptionBase.Specimen_ID data  #3571

		//TODO remove tables DescriptionBase_SpecimenOrObservationBase(_AUD)  #3571
		stepName = "Remove table DescriptionBase_SpecimenOrObservationBase";
		tableName = "DescriptionBase_SpecimenOrObservationBase";
		step = TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//create table CdmPreferences  #3555
		stepName = "Create table 'CdmPreferences'";
		tableName = "CdmPreferences";
		TableCreator stepPref = TableCreator.NewInstance(stepName, tableName, 
				new String[]{"key_subject", "key_predicate","value"},  //colNames 
				new String[]{"string_100", "string_200","string_1023",},  // columnTypes
				new String[]{null, "DefinedTermBase",null},  //referencedTables 
				! INCLUDE_AUDIT, false);
		stepPref.setPrimaryKeyParams("key_subject, key_predicate", null);
		stepList.add(stepPref);
		//TODO length of key >= 1000
		
		//TODO fill CdmPreferences with default values
		
		//update RightsTerm to RightsType #1306
		stepName = "Update RightsTerm -> RightsType";
		String updateSql = "UPDATE DefinedTermBase SET DTYPE = 'RightsType'  WHERE DTYPE = 'RightsTerm'";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, updateSql);
		stepList.add(step);
		
		//remove table Sequence_GenBankAccession #3552
		stepName = "Remove table Sequence_GenBankAccession";
		tableName = "Sequence_GenBankAccession";
		step = TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//remove table GenBankAccession #3552
		stepName = "Remove table GenBankAccession";
		tableName = "GenBankAccession";
		step = TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
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
		step = TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//add timeperiod to columns to description element base #3312
		addTimeperiodToDescriptionElement(stepList);


		//TODO add Marker vocabulary and terms #3591 => TermUpdater
		
		//SpecimenOrObservationBase_Media #3597
		stepName = "Remove table SpecimenOrObservationBase_Media";
		tableName = "SpecimenOrObservationBase_Media";
		step = TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
		stepList.add(step);
		
		
		//Amplification #3360
		stepName = "Create table 'Primer'";
		tableName = "Primer";
		step = TableCreator.NewAnnotatableInstance(stepName, tableName, 
				new String[]{"label","sequence_id","publishedIn_id"},  //colNames 
				new String[]{"string_255","int","int"},  // columnTypes
				new String[]{null,Sequence.class.getSimpleName(),Reference.class.getSimpleName()},  //referencedTables 
				INCLUDE_AUDIT);
		stepList.add(step);
		
		//MaterialAndMethod #3360
		stepName = "Create table 'MaterialAndMethod'";
		tableName = MaterialAndMethod.class.getSimpleName();
		step = TableCreator.NewAnnotatableInstance(stepName, tableName, 
				new String[]{"DTYPE", "materialMethodTerm_id","materialMethodText"},  //colNames 
				new String[]{"string_255", "int","string_1000",},  // columnTypes
				new String[]{null, "DefinedTermBase",null},  //referencedTables 
				INCLUDE_AUDIT);
		stepList.add(step);
		
		//Cloning #3360
		stepName = "Create table 'Cloning'";
		tableName = "Cloning";
		String matMetName = MaterialAndMethod.class.getSimpleName();
		step = TableCreator.NewEventInstance(stepName, tableName, 
				new String[]{"strain","method_id","forwardPrimer_id","reversePrimer_id"},  //colNames 
				new String[]{"string_255", "int","int","int"},  // columnTypes
				new String[]{null, matMetName,"Primer","Primer"},  //referencedTables 
				INCLUDE_AUDIT);
		stepList.add(step);
		
		
		//Amplification #3360
		stepName = "Create table 'Amplification'";
		tableName = "Amplification";
		step = TableCreator.NewEventInstance(stepName, tableName, 
				new String[]{"dnaSample_id","dnaMarker_id","forwardPrimer_id","reversePrimer_id","purification_id","cloning_id", "gelPhoto_id", "successful","successText","ladderUsed","electrophoresisVoltage","gelRunningTime","gelConcentration"},  //colNames 
				new String[]{"int","int","int","int","int","int","int", "bit","string_255","string_255","double","double","double"},  // columnTypes
				new String[]{"SpecimenOrObservationBase","DefinedTermBase","Primer","Primer",matMetName, matMetName, "Media", null, null, null, null, null, null},  //referencedTables 
				INCLUDE_AUDIT);
		stepList.add(step);
		
		//SingleRead #3360
		stepName = "Create table 'SingleRead'";
		tableName = "SingleRead";
		step = TableCreator.NewEventInstance(stepName, tableName, 
				new String[]{"amplification_id","materialAndMethod_id","primer_id","pherogram_id","direction","sequence_length"},  //colNames 
				new String[]{"int","int","int","int","int","int"},  // columnTypes
				new String[]{"Amplification",matMetName, "Primer","Media", null, null},  //referencedTables 
				INCLUDE_AUDIT);
		//TODO length sequence_string
		stepList.add(step);
		
		//sequence - consensussequence_string  #3360
		stepName= "Add sequence_string to single read";
		columnName = "sequence_string";
		step = ColumnAdder.NewClobInstance(stepName, tableName, columnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//amplification - single reads  #3360
		stepName= "Add single reads to amplification";
		String firstTable =  "Amplification";
		String secondTable =  "SingleRead";
		step = MnTableCreator.NewMnInstance(stepName, firstTable, null, secondTable, null, SchemaUpdaterBase.INCLUDE_AUDIT, false, true);
		stepList.add(step);
		
		//sequence - single reads  #3360
		stepName= "Add single reads to sequence";
		firstTable =  "Sequence";
		secondTable =  "SingleRead";
		step = MnTableCreator.NewMnInstance(stepName, firstTable, null, secondTable, null, SchemaUpdaterBase.INCLUDE_AUDIT, false, true);
		stepList.add(step);
		
		//sequence - barcode  #3360
		stepName= "Add barcodesequencepart_length to sequence";
		tableName = "Sequence";
		columnName = "barcodeSequencePart_length";
		defaultValue = null;
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT, defaultValue, false);
		stepList.add(step);

		//sequence - barcode  #3360
		stepName= "Add barcodesequencepart_string to sequence";
		tableName = "Sequence";
		columnName = "barcodeSequencePart_string";
		step = ColumnAdder.NewClobInstance(stepName, tableName, columnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//sequence - consensussequence_length  #3360
		stepName= "Add consensusSequence_length to sequence";
		tableName = "Sequence";
		columnName = "consensusSequence_length";
		defaultValue = null;
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT, defaultValue, false);
		stepList.add(step);

		//sequence - consensussequence_string  #3360
		stepName= "Add consensusSequence_string to sequence";
		tableName = "Sequence";
		columnName = "consensusSequence_string";
		step = ColumnAdder.NewClobInstance(stepName, tableName, columnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//sequence - contigFile  #3360
		stepName= "Add contigFile to sequence";
		tableName = "Sequence";
		columnName = "contigFile_id";
		referencedTable = "Media";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT, false, referencedTable);
		stepList.add(step);
		
		//sequence - boldprocessid  #3360
		stepName= "Add boldprocessId to sequence";
		tableName = "Sequence";
		columnName = "boldProcessId";
		length = 20;
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, length, INCLUDE_AUDIT);
		stepList.add(step);

		//sequence - boldprocessid  #3360
		stepName= "Add geneticAccessionNumber to sequence";
		tableName = "Sequence";
		columnName = "geneticAccessionNumber";
		length = 20;
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, length, INCLUDE_AUDIT);
		stepList.add(step);
		
		//sequence - haplotype  #3360
		stepName= "Add haplotype to sequence";
		tableName = "Sequence";
		columnName = "haplotype";
		length = 100;
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, length, INCLUDE_AUDIT);
		stepList.add(step);

		//sequence - isBarcode  #3360
		stepName= "Add isBarcode to sequence";
		tableName = "Sequence";
		columnName = "isBarcode";
		step = ColumnAdder.NewBooleanInstance(stepName, tableName, columnName, INCLUDE_AUDIT, false);
		stepList.add(step);
		
		//sequence - dnaMarker  #3360
		stepName= "Add dnaMarker to sequence";
		tableName = "Sequence";
		columnName = "dnaMarker_id";
		referencedTable = "DefinedTermBase";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT, false, referencedTable);
		stepList.add(step);
		
		//sequence - dnaSample  #3360
		stepName= "Add dnaSample to sequence";
		tableName = "Sequence";
		columnName = "dnaSample_id";
		referencedTable = "SpecimenOrObservationBase";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT, false, referencedTable);
		stepList.add(step);
		
		return stepList;
	}

	private void updateRankClass(List<ISchemaUpdaterStep> stepList) {
		//suprageneric
		String stepName = "Updater rankClass for suprageneric ranks";
		String query = "UPDATE DefinedTermBase dtb " + 
				" SET dtb.rankClass = '" + RankClass.Suprageneric.getKey() + "' " + 
				" WHERE dtb.DTYPE = 'Rank' AND dtb.orderindex < (SELECT orderindex FROM DefinedTermBase WHERE uuid = '" + Rank.uuidGenus + "')";
		ISchemaUpdaterStep step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		//genus
		stepName = "Updater rankClass for genus rank";
		query = "UPDATE DefinedTermBase dtb " + 
				" SET dtb.rankClass = '" + RankClass.Genus.getKey() + "' " + 
				" WHERE dtb.DTYPE = 'Rank' AND dtb.orderindex = (SELECT orderindex FROM DefinedTermBase WHERE uuid = '" + Rank.uuidGenus + "')";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//infrageneric
		stepName = "Updater rankClass for ranks below genus";
		query = "UPDATE DefinedTermBase dtb " + 
				" SET dtb.rankClass = '" + RankClass.Infrageneric.getKey() + "' " + 
				" WHERE dtb.DTYPE = 'Rank' " +
					"AND dtb.orderindex > (SELECT orderindex FROM DefinedTermBase WHERE uuid = '" + Rank.uuidGenus + "')" + 
					"AND dtb.orderindex <= (SELECT orderindex FROM DefinedTermBase WHERE uuid = '" + Rank.uuidInfragenericTaxon + "')";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//agg.
		stepName = "Updater rankClass for species group ranks";
		query = "UPDATE DefinedTermBase dtb " + 
				" SET dtb.rankClass = '" + RankClass.SpeciesGroup.getKey() + "' " + 
				" WHERE dtb.DTYPE = 'Rank' " +
					"AND dtb.orderindex > (SELECT orderindex FROM DefinedTermBase WHERE uuid = '" + Rank.uuidInfragenericTaxon + "')" + 
					"AND dtb.orderindex < (SELECT orderindex FROM DefinedTermBase WHERE uuid = '" + Rank.uuidSpecies + "')";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
//		41bcc6ac-37d3-4fd4-bb80-3cc5b04298b9
		
		//species
		stepName = "Updater rankClass for species rank";
		query = "UPDATE DefinedTermBase dtb " + 
				" SET dtb.rankClass = '" + RankClass.Species.getKey() + "' " + 
				" WHERE dtb.DTYPE = 'Rank' AND dtb.orderindex = (SELECT orderindex FROM DefinedTermBase WHERE uuid = '" + Rank.uuidSpecies + "')";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//infraspecific
		stepName = "Updater rankClass for infraspecific ranks";
		query = "UPDATE DefinedTermBase dtb " + 
				" SET dtb.rankClass = '" + RankClass.Infraspecific.getKey() + "' " + 
				" WHERE dtb.DTYPE = 'Rank' AND dtb.orderindex > (SELECT orderindex FROM DefinedTermBase WHERE uuid = '" + Rank.uuidSpecies + "')";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//unknown
		stepName = "Updater rankClass for unhandled ranks";
		query = " UPDATE DefinedTermBase dtb " + 
				" SET dtb.rankClass = '" + RankClass.Unknown.getKey() + "' " + 
				" WHERE dtb.DTYPE = 'Rank' AND dtb.rankClass IS NULL ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		
		
	}

	private void updateTermTypesForVocabularies( List<ISchemaUpdaterStep> stepList) {
		for (TermType termType : TermType.values()){
			updateTermTypeForVocabularies(stepList, termType);
		}
		
		String stepName = "Updater termType for NaturalLanguageTerms";
		String query = "UPDATE TermVocabulary voc " + 
				" SET voc.termType = '" + TermType.NaturalLanguageTerm.getKey() + "' " + 
				" WHERE voc.uuid = 'fdaba4b0-5c14-11df-a08a-0800200c9a66'";
		ISchemaUpdaterStep step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		stepName = "Updater termType for remaining vocabularies";
		query = "UPDATE TermVocabulary voc " + 
				" SET voc.termType = '"+ TermType.Unknown.getKey() +"' " + 
				" WHERE voc.termType IS NULL";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		
	}

	private void updateTermTypeForVocabularies(List<ISchemaUpdaterStep> stepList, TermType termType) {
		String stepName = "Updater vocabulary termType for " + termType.toString();
		String query = "UPDATE TermVocabulary voc " + 
				" SET voc.termType = '" + termType.getKey() + "' " + 
				" WHERE Exists (SELECT * FROM DefinedTermBase dtb WHERE dtb.termType = '" + termType.getKey() + "' AND dtb.vocabulary_id = voc.id)";
		ISchemaUpdaterStep step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
	}

	/**
	 * @param stepList
	 * @param stepName
	 */
	private void updateTermTypesForTerms(List<ISchemaUpdaterStep> stepList) {
		String stepName = "Update termType for NamedAreas";
		//NamedArea
		String query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.NamedArea.getKey() + "' " +
				" WHERE DTYPE = '" + NamedArea.class.getSimpleName() + "' OR DTYPE = 'TdwgArea' OR DTYPE = '"+ WaterbodyOrCountry.class.getSimpleName() + "' OR DTYPE = '" + Continent.class.getSimpleName() + "' ";
		ISchemaUpdaterStep step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		//Lanugage
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.Language.getKey() + "' " +
				" WHERE DTYPE = '" + Language.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//RANK
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.Rank.getKey() + "' " +
				" WHERE DTYPE = '" + Rank.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		//Feature
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.Feature.getKey() + "' " +
				" WHERE DTYPE = '" + Feature.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//AnnotationType
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.AnnotationType.getKey() + "' " +
				" WHERE DTYPE = '" + AnnotationType.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		//MarkerType
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.MarkerType.getKey() + "' " +
				" WHERE DTYPE = '" + MarkerType.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//ExtensionType
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.ExtensionType.getKey() + "' " +
				" WHERE DTYPE = '" + ExtensionType.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		//DerivationEventType
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.DerivationEventType.getKey() + "' " +
				" WHERE DTYPE = '" + DerivationEventType.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//PresenceAbsenceTerm
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.PresenceAbsenceTerm.getKey() + "' " +
				" WHERE DTYPE = 'PresenceTerm' OR DTYPE = 'AbsenceTerm'";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//NomenclaturalStatusType
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.NomenclaturalStatusType.getKey() + "' " +
				" WHERE DTYPE = '" + NomenclaturalStatusType.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//NameRelationshipType
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.NameRelationshipType.getKey() + "' " +
				" WHERE DTYPE = '" + NameRelationshipType.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//HybridRelationshipType
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.HybridRelationshipType.getKey() + "' " +
				" WHERE DTYPE = '" + HybridRelationshipType.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//SynonymRelationshipType
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.SynonymRelationshipType.getKey() + "' " +
				" WHERE DTYPE = '" + SynonymRelationshipType.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//TaxonRelationshipType
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.TaxonRelationshipType.getKey() + "' " +
				" WHERE DTYPE = '" + TaxonRelationshipType.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//NameTypeDesignationStatus
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.NameTypeDesignationStatus.getKey() + "' " +
				" WHERE DTYPE = '" + NameTypeDesignationStatus.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//SpecimenTypeDesignationStatus
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.SpecimenTypeDesignationStatus.getKey() + "' " +
				" WHERE DTYPE = '" + SpecimenTypeDesignationStatus.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//InstitutionType
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.InstitutionType.getKey() + "' " +
				" WHERE DTYPE = '" + InstitutionType.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//NamedAreaType
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.NamedAreaType.getKey() + "' " +
				" WHERE DTYPE = '" + NamedAreaType.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//NamedAreaLevel
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.NamedAreaLevel.getKey() + "' " +
				" WHERE DTYPE = '" + NamedAreaLevel.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//RightsType
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.RightsType.getKey() + "' " +
				" WHERE DTYPE = 'RightsType' OR DTYPE = 'RightsTerm' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//MeasurementUnit
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.MeasurementUnit.getKey() + "' " +
				" WHERE DTYPE = '" + MeasurementUnit.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//StatisticalMeasure
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.StatisticalMeasure.getKey() + "' " +
				" WHERE DTYPE = '" + StatisticalMeasure.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//PreservationMethod
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.PreservationMethod.getKey() + "' " +
				" WHERE DTYPE = '" + PreservationMethod.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//Modifier
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.Modifier.getKey() + "' " +
				" WHERE DTYPE = 'Modifier' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//Scope
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.Scope.getKey() + "' " +
				" WHERE DTYPE = 'Scope' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//Stage
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.Stage.getKey() + "' " +
				" WHERE DTYPE = 'Stage' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//Sex
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.Sex.getKey() + "' " +
				" WHERE DTYPE = 'Sex' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//ReferenceSystem
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.ReferenceSystem.getKey() + "' " +
				" WHERE DTYPE = '" + ReferenceSystem.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//State
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.State.getKey() + "' " +
				" WHERE DTYPE = '" + State.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//NaturalLanguageTerm
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.NaturalLanguageTerm.getKey() + "' " +
				" WHERE DTYPE = '" + NaturalLanguageTerm.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//TextFormat
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.TextFormat.getKey() + "' " +
				" WHERE DTYPE = '" + TextFormat.class.getSimpleName() + "' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		//DeterminationModifier
		query = " UPDATE DefinedTermBase " + 
				" SET termType = '" + TermType.DeterminationModifier.getKey() + "' " +
				" WHERE DTYPE = 'DeterminationModifier' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);

		
	}

	/**
	 * @param stepList
	 */
	private void updateDtypeOfDefinedTerms(List<ISchemaUpdaterStep> stepList) {

		//TODO update DTYPE for modifiers (Stage, Scope, Sex, DeterminationModifier, Modifier -> DefinedTerm)
		String stepName = "Update DTYPE for TDWG Areas";
		String query = " UPDATE DefinedTermBase " + 
				" SET DTYPE = 'DefinedTerm' " +
				" WHERE DTYPE = 'Stage' OR DTYPE = 'Scope' OR DTYPE = 'Sex' OR DTYPE = 'DeterminationModifier'  OR DTYPE = 'Modifier' ";
		ISchemaUpdaterStep step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		
		//TODO update DTYPE for TDWG Areas -> NamedArea
		stepName = "Update DTYPE for TDWG Areas";
		query = " UPDATE DefinedTermBase " + 
				" SET DTYPE = 'NamedArea' " +
				" WHERE DTYPE = 'TdwgArea' ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
	}

	/**
	 * @param stepList
	 */
	private void changeUriType(List<ISchemaUpdaterStep> stepList) {
		String stepName;
		String tableName;
		ISchemaUpdaterStep step;
		stepName = "Update uri to clob for DefinedTermBase";
		tableName = "DefinedTermBase";
		String oldColumnName = "uri";
		String newColumnName = oldColumnName;
		step = ColumnTypeChanger.NewClobInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		stepName = "Update uri to clob for TermVocabulary";
		tableName = "TermVocabulary";
		oldColumnName = "uri";
		newColumnName = oldColumnName;
		step = ColumnTypeChanger.NewClobInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//TODO are uri and termsourceuri needed ???
		stepName = "Update termsourceuri to clob for TermVocabulary";
		tableName = "TermVocabulary";
		oldColumnName = "termsourceuri";
		newColumnName = oldColumnName;
		step = ColumnTypeChanger.NewClobInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		stepName = "Update uri to clob for Reference";
		tableName = "Reference";
		oldColumnName = "uri";
		newColumnName = oldColumnName;
		step = ColumnTypeChanger.NewClobInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		stepName = "Update uri to clob for Rights";
		tableName = "Rights";
		oldColumnName = "uri";
		newColumnName = oldColumnName;
		step = ColumnTypeChanger.NewClobInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
		stepList.add(step);

		stepName = "Update uri to clob for MediaRepresentationPart";
		tableName = "MediaRepresentationPart";
		oldColumnName = "uri";
		newColumnName = oldColumnName;
		step = ColumnTypeChanger.NewClobInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//TODO still needed??
		stepName = "Update uri to clob for FeatureTree";
		tableName = "FeatureTree";
		oldColumnName = "uri";
		newColumnName = oldColumnName;
		step = ColumnTypeChanger.NewClobInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
		stepList.add(step);

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
				" SET ge.absoluteElevationMax = ge.absoluteElevation + ge.absoluteElevationError,  " +
				"     ge.absoluteElevation =  ge.absoluteElevation - ge.absoluteElevationError" +
				" WHERE ge.absoluteElevationError is not null ";
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		//TODO same for AUD
		
		//remove error column
		stepName = "Remove elevationErrorRadius column";
		tableName = "GatheringEvent";
		columnName = "absoluteElevationError";
		step = ColumnRemover.NewInstance(stepName, tableName, columnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//create column absoluteElevationText
		stepName = "Create absoluteElevationText column";
		tableName = "GatheringEvent";
		columnName = "absoluteElevationText";
		//TODO size
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, 255, INCLUDE_AUDIT);
		stepList.add(step);
		
		//retype distanceToGround 
		stepName = "Rname distanceToGround column";
		tableName = "GatheringEvent";
		String strNewColumnName = "distanceToGround";
		String strOldColumnName = "distanceToGround";
		step = ColumnTypeChanger.NewInt2DoubleInstance(stepName, tableName, strOldColumnName, strNewColumnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//create column distanceToGroundMax
		stepName = "Create distanceToGroundMax column";
		tableName = "GatheringEvent";
		columnName = "distanceToGroundMax";
		step = ColumnAdder.NewDoubleInstance(stepName, tableName, columnName, INCLUDE_AUDIT, false);
		stepList.add(step);
		
		
		//create column distanceToGroundText
		stepName = "Create distanceToGroundText column";
		tableName = "GatheringEvent";
		columnName = "distanceToGroundText";
		//TODO size
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, 255, INCLUDE_AUDIT);
		stepList.add(step);
		
		//retype distanceToGround 
		stepName = "Rname distanceToWaterSurface column";
		tableName = "GatheringEvent";
		strNewColumnName = "distanceToWaterSurface";
		strOldColumnName = "distanceToWaterSurface";
		step = ColumnTypeChanger.NewInt2DoubleInstance(stepName, tableName, strOldColumnName, strNewColumnName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//create column distanceToWaterSurface
		stepName = "Create distanceToWaterSurfaceMax column";
		tableName = "GatheringEvent";
		columnName = "distanceToWaterSurfaceMax";
		step = ColumnAdder.NewDoubleInstance(stepName, tableName, columnName, INCLUDE_AUDIT, false);
		stepList.add(step);
		
		
		//create column distanceToGroundText
		stepName = "Create distanceToWaterSurfaceText column";
		tableName = "GatheringEvent";
		columnName = "distanceToWaterSurfaceText";
		//TODO size
		step = ColumnAdder.NewStringInstance(stepName, tableName, columnName, 255, INCLUDE_AUDIT);
		stepList.add(step);

	}

	/**
	 * @param stepList
	 */
	private void updateOriginalSourceType(List<ISchemaUpdaterStep> stepList) {
		String stepName;
		String typeAttrName = "sourceType";
		ISchemaUpdaterStep step;
		stepName = "Update original source type column in OriginalSourceBase_AUD: set all to unknown";
		//all audits to unknown type
		String query = String.format("UPDATE OriginalSourceBase_AUD SET %s = '%s' ", typeAttrName, OriginalSourceType.Unknown.getKey());
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		 //all data to unknown
		stepName = "Update original source type column: set all to unknown";
		query = String.format("UPDATE OriginalSourceBase SET %s = '%s' ", typeAttrName, OriginalSourceType.Unknown.getKey());
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		 //all IMPORTS recognized by idInSOurce and by missing nameInSource
		stepName = "Update original source type column: set to 'import' where possible";
		query = String.format("UPDATE OriginalSourceBase SET %s = '%s' WHERE " +
				"((idInSource IS NOT NULL) OR (idNamespace IS NOT NULL))  AND " +
				"( nameUsedInSource_id IS NULL AND originalNameString IS NULL ) ", typeAttrName, OriginalSourceType.Import.getKey());
		step = SimpleSchemaUpdaterStep.NewInstance(stepName, query);
		stepList.add(step);
		
		 //all PRIMARY TAXONOMIC SOURCES recognized by missing idInSource and namespace and by existing citation
		stepName = "Update original source type column: set to 'primary taxonomic source' where possible";
		query = String.format("UPDATE OriginalSourceBase SET  %s = '%s' WHERE " +
				"(idInSource IS NULL AND idNamespace IS NULL) AND " +
				"( citation_id IS NOT NULL ) ", typeAttrName, OriginalSourceType.PrimaryTaxonomicSource.getKey());
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
