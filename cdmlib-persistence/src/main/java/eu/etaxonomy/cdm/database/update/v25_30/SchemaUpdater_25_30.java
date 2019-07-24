/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.update.v25_30;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.MapTableCreator;
import eu.etaxonomy.cdm.database.update.MnTableCreator;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.database.update.TableDroper;
import eu.etaxonomy.cdm.database.update.TableNameChanger;
import eu.etaxonomy.cdm.database.update.v24_25.SchemaUpdater_24_25;
import eu.etaxonomy.cdm.database.update.v30_31.SchemaUpdater_30_301;


/**
 * @author a.mueller
 * @since Nov 08, 2010
 */
public class SchemaUpdater_25_30 extends SchemaUpdaterBase {


	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_25_30.class);
	private static final String startSchemaVersion = "2.5.0.0.201009211255";
	private static final String endSchemaVersion = "3.0.0.0.201011090000";

// ********************** FACTORY METHOD *******************************************

	public static SchemaUpdater_25_30 NewInstance(){
		return new SchemaUpdater_25_30();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_25_30() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();
		String stepName;

		//add feature tree attribute to feature node table
		stepName = "Add feature tree addtribue to feature node";
		//TODO defaultValue & not null
		ColumnAdder.NewIntegerInstance(stepList, stepName, "FeatureNode", "FeatureTree_id", INCLUDE_AUDIT, false, "FeatureTree");

		//compute feature tree column
		stepName = "Update feature node tree column";
		FeatureNodeTreeColumnUpdater fntcu = FeatureNodeTreeColumnUpdater.NewInstance(stepList, stepName, INCLUDE_AUDIT);

		//Key statement
		stepName = "Create KeyStatement tables";
		TableCreator.NewInstance(stepList, stepName, "KeyStatement", new String[]{}, new String[]{}, new String[]{}, INCLUDE_AUDIT, INCLUDE_CDM_BASE);

		//KeyStatement_LanguageString
		stepName = "Create KeyStatement label";
		MapTableCreator.NewMapTableInstance(stepList, stepName,  "KeyStatement", null,  "LanguageString", "label", "DefinedTermBase", SchemaUpdaterBase.INCLUDE_AUDIT);


		//PolytomousKey
		stepName = "Create PolytomousKey tables";
		TableCreator.NewIdentifiableInstance(stepList, stepName, "PolytomousKey", new String[]{"root_id"}, new String[]{"int"}, new String[]{"PolytomousKeyNode"}, INCLUDE_AUDIT);

		//create table PolytomousKeyNode_PolytomousKeyNode_AUD (REV integer not null, parent_id integer not null, id integer not null, sortIndex integer not null, revtype tinyint, primary key (REV, parent_id, id, sortIndex)) ENGINE=MYISAM DEFAULT CHARSET=utf8
		TableCreator.NewInstance(stepList, stepName, "PolytomousKeyNode_PolytomousKeyNode_AUD", new String[]{"REV", "parent_id", "id", "sortIndex", "revtype"}, new String[]{"int","int","int","int","tinyint"}, new String[]{null, "PolytomousKeyNode", null, null, null},! INCLUDE_AUDIT, ! INCLUDE_CDM_BASE)
		        .setPrimaryKeyParams("REV, parent_id, id, sortIndex", null)
		        .setUniqueParams(null, null);

		//covered taxa
		stepName= "Add polytomous key covered taxa";
		MnTableCreator.NewMnInstance(stepList, stepName, "PolytomousKey", null, "TaxonBase", null, "coveredtaxa", SchemaUpdaterBase.INCLUDE_AUDIT, false, false);

		//Polytomous key node
		stepName = "Create PolytomousKeyNode tables";
		TableCreator.NewInstance(stepList, stepName, "PolytomousKeyNode", new String[]{"nodeNumber", "sortindex", "key_id", "othernode_id", "question_id", "statement_id", "feature_id", "subkey_id" , "taxon_id", "parent_id"}, new String[]{"int", "int", "int", "int", "int","int", "int", "int", "int", "int"}, new String[]{null, null, "PolytomousKey", "PolytomousKeyNode", "KeyStatement", "KeyStatement", "DefinedTermBase", "PolytomousKey" , "TaxonBase", "PolytomousKeyNode"}, INCLUDE_AUDIT, INCLUDE_CDM_BASE);

		//modifying text
		stepName = "Create PolytomousKeyNode modifying text";
		MapTableCreator.NewMapTableInstance(stepList, stepName,  "PolytomousKeyNode", null,  "LanguageString", "modifyingtext", "DefinedTermBase", SchemaUpdaterBase.INCLUDE_AUDIT);

		//rename named area featureTree_id
		stepName = "Rename polytomouskey_namedarea.featureTree_id -> polytomouskey_id";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "PolytomousKey_NamedArea", "FeatureTree_id", "PolytomousKey_id", INCLUDE_AUDIT);

		//rename polytomouskey_scope featureTree_id
		stepName = "Rename polytomouskey_scope.featureTree_id -> polytomouskey_id";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "PolytomousKey_Scope", "FeatureTree_id", "PolytomousKey_id", INCLUDE_AUDIT);

		//move PolytomousKey data to new tables
		stepName = "Move polytomous key data from feature tree to polytomous key";
		PolytomousKeyDataMover dataMover = PolytomousKeyDataMover.NewInstance(stepList, stepName, INCLUDE_AUDIT);

		//remove DTYPE from feature node
		stepName = "Remove feature tree DTYPE column";
		ColumnRemover.NewInstance(stepList, stepName, "FeatureTree", "DTYPE", INCLUDE_AUDIT);

		//remove feature node taxon column
		stepName = "Remove feature node taxon column";
		ColumnRemover.NewInstance(stepList, stepName, "FeatureNode", "taxon_id", INCLUDE_AUDIT);

		//Remove featureNode_representation
		stepName = "Remove FeatureNode_Representation MN";
		TableDroper.NewInstance(stepList, stepName, "FeatureNode_Representation", INCLUDE_AUDIT);

		//add exsiccatum
		stepName = "Add exsiccatum to specimen";
		ColumnAdder.NewStringInstance(stepList, stepName, "SpecimenOrObservationBase", "exsiccatum", INCLUDE_AUDIT);

		//add primary collector
		stepName = "Add primary collector to field unit";
		ColumnAdder.NewIntegerInstance(stepList, stepName, "SpecimenOrObservationBase", "primaryCollector_id", INCLUDE_AUDIT, false, "AgentBase");

		//taxonomic tree -> classification
		stepName = "Rename taxonomic tree to classification";
		TableNameChanger.NewInstance(stepList, stepName, "TaxonomicTree", "Classification", INCLUDE_AUDIT);

		//TaxonomicTree_Annotation -> classification_Annotation
		stepName = "Rename TaxonomicTree_Annotation to Classification_Annotation";
		TableNameChanger.NewInstance(stepList, stepName, "TaxonomicTree_Annotation", "Classification_Annotation", INCLUDE_AUDIT);

		stepName = "Rename taxonomicTree_id column in Classification_Annotation";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "Classification_Annotation", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);


		//TaxonomicTree_Credit -> classification_Credit
		stepName = "Rename TaxonomicTree_Credit to Classification_Credit";
		TableNameChanger.NewInstance(stepList, stepName, "TaxonomicTree_Credit", "Classification_Credit", INCLUDE_AUDIT);

		stepName = "Rename taxonomicTree_id column in Classification_Credit";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "Classification_Credit", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);

		//TaxonomicTree_Extension -> classification_Extension
		stepName = "Rename TaxonomicTree_Extension to Classification_Extension";
		TableNameChanger.NewInstance(stepList, stepName, "TaxonomicTree_Extension", "Classification_Extension", INCLUDE_AUDIT);

		stepName = "Rename taxonomicTree_id column in Classification_Extension";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "Classification_Extension", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);


		//TaxonomicTree_Marker -> classification_Marker
		stepName = "Rename TaxonomicTree_Marker to Classification_Marker";
		TableNameChanger.NewInstance(stepList, stepName, "TaxonomicTree_Marker", "Classification_Marker", INCLUDE_AUDIT);

		stepName = "Rename taxonomicTree_id column in Classification_Marker";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "Classification_Marker", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);


		//TaxonomicTree_OriginalSourceBase -> classification_OriginalSourceBase
		stepName = "Rename TaxonomicTree_OriginalSourceBase to Classification_OriginalSourceBase";
		TableNameChanger.NewInstance(stepList, stepName, "TaxonomicTree_OriginalSourceBase", "Classification_OriginalSourceBase", INCLUDE_AUDIT);

		stepName = "Rename taxonomicTree_id column in Classification_OriginalSourceBase";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "Classification_OriginalSourceBase", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);


		//TaxonomicTree_Rights -> classification_Rights
		stepName = "Rename TaxonomicTree_Rights to Classification_Rights";
		TableNameChanger.NewInstance(stepList, stepName, "TaxonomicTree_Rights", "Classification_Rights", INCLUDE_AUDIT);

		stepName = "Rename taxonomicTree_id column in Classification_Rights";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "Classification_Rights", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);


		//TaxonomicTree_TaxonNode -> classification_TaxonNode
		stepName = "Rename TaxonomicTree_TaxonNode to Classification_TaxonNode";
		TableNameChanger.NewInstance(stepList, stepName, "TaxonomicTree_TaxonNode", "Classification_TaxonNode", INCLUDE_AUDIT);

		stepName = "Rename taxonomicTree_id column in Classification_TaxonNode";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "Classification_TaxonNode", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);

		//Rename taxonomictree column in TaxonNode
		stepName = "Rename taxonomicTree_id column in TaxonNode";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "TaxonNode", "taxonomicTree_id", "classification_id", INCLUDE_AUDIT);

		//Rename description_id column in SpecimenOrObservationBase_LanguageString
		stepName = "Rename description column in SpecimenOrObservationBase_LanguageString";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "SpecimenOrObservationBase_LanguageString", "description_id", "definition_id", INCLUDE_AUDIT);

		//Rename description_mapkey_id column in SpecimenOrObservationBase_LanguageString
		stepName = "Rename description column in SpecimenOrObservationBase_LanguageString";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "SpecimenOrObservationBase_LanguageString", "description_mapkey_id", "definition_mapkey_id", INCLUDE_AUDIT);

		//Rename derivationevent_id column in SpecimenOrObservationBase
		stepName = "Rename derivationevent_id column in SpecimenOrObservationBase";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "SpecimenOrObservationBase", "derivationevent_id", "derivedfrom_id", INCLUDE_AUDIT);

		//Rename taxonName_fk column in TaxonBase
		stepName = "Rename taxonName_fk column in TaxonBase";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "TaxonBase", "taxonName_fk", "name_id", INCLUDE_AUDIT);

		//Rename taxonName_fk column in DescriptionBase
		stepName = "Rename taxonName_fk column in DescriptionBase";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "DescriptionBase", "taxonName_fk", "taxonName_id", INCLUDE_AUDIT);

		//Rename taxon_fk column in DescriptionBase
		stepName = "Rename taxon_fk column in DescriptionBase";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "DescriptionBase", "taxon_fk", "taxon_id", INCLUDE_AUDIT);

		//Rename parent_fk column in FeatureNode
		stepName = "Rename parent_fk column in FeatureNode";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "FeatureNode", "parent_fk", "parent_id", INCLUDE_AUDIT);

		//Rename polytomousKey_fk column in PolytomousKey_Taxon
		stepName = "Rename polytomousKey_fk column in PolytomousKey_Taxon";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "PolytomousKey_Taxon", "polytomousKey_fk", "polytomousKey_id", INCLUDE_AUDIT);

		//Rename taxon_fk column in PolytomousKey_Taxon
		stepName = "Rename taxon_fk column in PolytomousKey_Taxon";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "PolytomousKey_Taxon", "taxon_fk", "taxon_id", INCLUDE_AUDIT);


		//Rename mediaKey_fk column in MediaKey_Taxon
		stepName = "Rename mediaKey_fk column in MediaKey_Taxon";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "MediaKey_Taxon", "mediaKey_fk", "mediaKey_id", INCLUDE_AUDIT);

		//Rename taxon_fk column in MediaKey_Taxon
		stepName = "Rename taxon_fk column in MediaKey_Taxon";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "MediaKey_Taxon", "taxon_fk", "taxon_id", INCLUDE_AUDIT);


		//Rename multiAccessKey_fk column in MultiAccessKey_Taxon
		stepName = "Rename multiAccessKey_fk column in MultiAccessKey_Taxon";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "MultiAccessKey_Taxon", "multiAccessKey_fk", "multiAccessKey_id", INCLUDE_AUDIT);

		//Rename taxon_fk column in MultiAccessKey_Taxon
		stepName = "Rename taxon_fk column in MultiAccessKey_Taxon";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, "MultiAccessKey_Taxon", "taxon_fk", "taxon_id", INCLUDE_AUDIT);

		//add the table hibernate_sequences
		stepName = "Add the table hibernate_sequences to store the table specific sequences in";
		SequenceTableCreator.NewInstance(stepList, stepName);

		return stepList;
	}

	@Override
	public ISchemaUpdater getNextUpdater() {
		return SchemaUpdater_30_301.NewInstance();
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_24_25.NewInstance();
	}

}
