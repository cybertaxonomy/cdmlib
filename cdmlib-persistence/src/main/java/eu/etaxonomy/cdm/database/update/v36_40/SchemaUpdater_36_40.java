// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v36_40;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableNameChanger;
import eu.etaxonomy.cdm.database.update.v35_36.SchemaUpdater_35_36;

/**
 * @author a.mueller
 * @created 16.04.2016
 */
public class SchemaUpdater_36_40 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_36_40.class);
	private static final String endSchemaVersion = "4.0.0.0.201604200000";
	private static final String startSchemaVersion = "3.6.0.0.201527040000";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_36_40 NewInstance() {
		return new SchemaUpdater_36_40();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_36_40() {
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
		String columnNames[];
		String referencedTables[];
		String columnTypes[];
//		boolean includeCdmBaseAttributes = false;

		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();

        //#5606
        //Add preferred stable URI to SpecimenOrObservation
        stepName = "Add preferred stable URI to SpecimenOrObservation";
        tableName = "SpecimenOrObservationBase";
        newColumnName = "preferredStableUri";
        step = ColumnAdder.NewClobInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //#5717
        //Add sec micro reference
        stepName = "Add secMicroReference to TaxonBase";
        tableName = "TaxonBase";
        newColumnName = "secMicroReference";
        step = ColumnAdder.NewStringInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //#5718
        //Remove autoincrement from AuditEvent.revisionnumber
        stepName = "Remove autoincrement from AuditEvent.revisionnumber";
//        String query = "ALTER TABLE @@AuditEvent@@ ALTER revisionnumber DROP DEFAULT";
//        step = SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepName, query, -99);
//        stepList.add(step);
        tableName = "AuditEvent";
        oldColumnName = "revisionnumber";
        newColumnName = "revisionnumberOld";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, ! INCLUDE_AUDIT);
        stepList.add(step);

        tableName = "AuditEvent";
        String columnName = oldColumnName;
        Integer defaultValue = null;
        boolean notNull = true;
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, ! INCLUDE_AUDIT, defaultValue, notNull);
        stepList.add(step);

        String query = "UPDATE @@AuditEvent@@ SET revisionnumber = revisionnumberOld";
        step = SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepName, query, -99);
        stepList.add(step);

        step = ColumnRemover.NewInstance(stepName, tableName, newColumnName, ! INCLUDE_AUDIT);
        stepList.add(step);

        //#5734
        //Add symbol to terms
        stepName = "Add symbols to terms";
        tableName = "DefinedTermBase";
        newColumnName = "symbol";
        step = ColumnAdder.NewStringInstance(stepName, tableName, newColumnName, 30, INCLUDE_AUDIT);
        stepList.add(step);

        stepName = "Update symbols for terms";
        query = "UPDATE @@DefinedTermBase@@ SET symbol = idInVocabulary WHERE idInVocabulary <> ''";
        step = SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepName, query, -99);
        stepList.add(step);


        //Add inverse symbol to terms
        stepName = "Add inverse symbol to terms";
        tableName = "DefinedTermBase";
        newColumnName = "inverseSymbol";
        step = ColumnAdder.NewStringInstance(stepName, tableName, newColumnName, 30, INCLUDE_AUDIT);
        stepList.add(step);

        stepName = "Update symbols for terms";
        query = "UPDATE DefinedTermBase dtb SET dtb.inverseSymbol = ( " +
            " SELECT  r.abbreviatedlabel " +
            " FROM RelationshipTermBase_inverseRepresentation MN " +
                " INNER JOIN Representation r ON r.id = MN.inverserepresentations_id " +
            " WHERE dtb.id = MN.DefinedTermBase_id AND r.abbreviatedlabel <> '' ) ";
        step = SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepName, query, -99);
        stepList.add(step);

        //#5369
        renameColumnsAccordingToHibernate5(stepList);

        //Update xxxObj_type  (#3701)
        step = ReferencedObjTypeUpdater.NewInstance();
        stepList.add(step);

        //remove bidirectionality from supplemental data #5743
        //annotation
        stepName = "Remove Annotation.annotatedObj_type";
        tableName = "Annotation";
        oldColumnName = "annotatedObj_type";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);
        stepList.add(step);

        stepName = "Remove Annotation.annotatedObj_id";
        tableName = "Annotation";
        oldColumnName = "annotatedObj_id";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);
        stepList.add(step);

        //marker
        stepName = "Remove Marker.markedObj_type";
        tableName = "Marker";
        oldColumnName = "markedObj_type";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);
        stepList.add(step);

        stepName = "Remove Marker.markedObj_id";
        tableName = "Marker";
        oldColumnName = "markedObj_id";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);
        stepList.add(step);

        //extension
        stepName = "Remove Extension.extendedObj_type";
        tableName = "Extension";
        oldColumnName = "extendedObj_type";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);
        stepList.add(step);

        stepName = "Remove Extension.extendedObj_id";
        tableName = "Extension";
        oldColumnName = "extendedObj_id";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);
        stepList.add(step);

        //sources
        stepName = "Remove OriginalSourceBase.sourcedObj_type";
        tableName = "OriginalSourceBase";
        oldColumnName = "sourcedObj_type";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);
        stepList.add(step);

        stepName = "Remove OriginalSourceBase.sourcedObj_id";
        tableName = "OriginalSourceBase";
        oldColumnName = "sourcedObj_id";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);
        stepList.add(step);

        //identifier
        stepName = "Remove Identifier.identifiedObj_type";
        tableName = "Identifier";
        oldColumnName = "identifiedObj_type";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);
        stepList.add(step);

        stepName = "Remove Identifier.identifiedObj_id";
        tableName = "Identifier";
        oldColumnName = "identifiedObj_id";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);
        stepList.add(step);

        return stepList;
	}


	//#5369
    private void renameColumnsAccordingToHibernate5(List<ISchemaUpdaterStep> stepList) {

        //AgenBase_AgentBase.AgentBase_ID  -> Team_ID
        String stepName = "Rename columns according to hibernate5";
        String tableName = "AgentBase_AgentBase";
        String oldColumnName = "agentbase_id";
        String newColumnName = "team_id";
        ISchemaUpdaterStep step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //DescriptionElementBase_LanguageString.DescriptionElementBase_ID -> TextData_ID
        stepName = "Rename DescriptionElementBase_LanguageString.DescriptionElementBase_ID";
        tableName = "DescriptionElementBase_LanguageString";
        oldColumnName = "descriptionElementBase_id";
        newColumnName = "textdata_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //AgentBase_DefinedTermBase.AgentBase_ID -> Institution_id
        stepName = "Rename AgentBase_DefinedTermBase.AgentBase_ID -> Institution_id";
        tableName = "AgentBase_DefinedTermBase";
        oldColumnName = "agentbase_id";
        newColumnName = "institution_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //PermissionGroup_GrantedAuthorityImpl.PermsiionsGroup_id -> Group_id
        stepName = "PermissionGroup_GrantedAuthorityImpl.PermsiionsGroup_id -> Group_id ";
        tableName = "PermissionGroup_GrantedAuthorityImpl";
        oldColumnName = "Permissiongroup_id";
        newColumnName = "group_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, ! INCLUDE_AUDIT);
        stepList.add(step);

        //UserAccount_GrantedAuthorityImpl.UserAccount_id -> User_id
        stepName = "UserAccount_GrantedAuthorityImpl.UserAccount_id -> User_id ";
        tableName = "UserAccount_GrantedAuthorityImpl";
        oldColumnName = "useraccount_id";
        newColumnName = "user_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, ! INCLUDE_AUDIT);
        stepList.add(step);

        //DefinedTermBase_RecommendedModifierEnumeration.DefinedTermBase_id -> Feature_id
        stepName = "DefinedTermBase_RecommendedModifierEnumeration.DefinedTermBase_id -> Feature_id";
        tableName = "DefinedTermBase_RecommendedModifierEnumeration";
        oldColumnName = "definedtermbase_id";
        newColumnName = "feature_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //DefinedTermBase_StatisticalMeasure.DefinedTermBase_id -> Feature_id
        stepName = "DefinedTermBase_StatisticalMeasure.DefinedTermBase_id -> Feature_id";
        tableName = "DefinedTermBase_StatisticalMeasure";
        oldColumnName = "definedtermbase_id";
        newColumnName = "feature_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //DefinedTermBase_SupportedCategoricalEnumeration.DefinedTermBase_id -> Feature_id
        stepName = "DefinedTermBase_SupportedCategoricalEnumeration.DefinedTermBase_id -> Feature_id";
        tableName = "DefinedTermBase_SupportedCategoricalEnumeration";
        oldColumnName = "definedtermbase_id";
        newColumnName = "feature_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //DefinedTermBase_MeasurementUnit.DefinedTermBase_id -> Feature_id
        stepName = "DefinedTermBase_MeasurementUnit.DefinedTermBase_id -> Feature_id";
        tableName = "DefinedTermBase_MeasurementUnit";
        oldColumnName = "definedtermbase_id";
        newColumnName = "feature_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //Media_TaxonBase.Media_id -> MediaKey_id
        stepName = "DefinedTermBase_MeasurementUnit.DefinedTermBase_id -> Feature_id";
        tableName = "Media_TaxonBase";
        oldColumnName = "media_id";
        newColumnName = "mediakey_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //Rename Media_TaxonBase -> MediaKey_CoveredTaxon
        stepName = "Rename Media_TaxonBase -> MediaKey_CoveredTaxon";
        String oldTableName = "Media_TaxonBase";
        String newTableName = "MediaKey_CoveredTaxon";
        step = TableNameChanger.NewInstance(stepName, oldTableName, newTableName, INCLUDE_AUDIT);
        stepList.add(step);

        //MediaKey_NamedArea.Media_id -> MediaKey_id
        stepName = "MediaKey_NamedArea.Media_id -> MediaKey_id";
        tableName = "MediaKey_NamedArea";
        oldColumnName = "media_id";
        newColumnName = "mediakey_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //MediaKey_Scope.Media_id -> MediaKey_id
        stepName = "MediaKey_Scope.Media_id -> MediaKey_id";
        tableName = "MediaKey_Scope";
        oldColumnName = "media_id";
        newColumnName = "mediakey_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //Rename MediaKey_Taxon -> MediaKey_TaxonScope
        stepName = "Rename MediaKey_Taxon -> MediaKey_TaxonScope";
        oldTableName = "MediaKey_Taxon";
        newTableName = "MediaKey_TaxonScope";
        step = TableNameChanger.NewInstance(stepName, oldTableName, newTableName, INCLUDE_AUDIT);
        stepList.add(step);

        //MediaKey_TaxonScope.taxon_id -> taxonomicScope_id
        stepName = "MediaKey_TaxonScope.taxon_id -> taxonomicScope_id";
        tableName = "MediaKey_TaxonScope";
        oldColumnName = "taxon_id";
        newColumnName = "taxonomicScope_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //Rename WorkingSet_TaxonBase -> MultiAccessKey_CoveredTaxon
        stepName = "Rename WorkingSet_TaxonBase -> MultiAccessKey_CoveredTaxon";
        oldTableName = "WorkingSet_TaxonBase";
        newTableName = "MultiAccessKey_CoveredTaxon";
        step = TableNameChanger.NewInstance(stepName, oldTableName, newTableName, INCLUDE_AUDIT);
        stepList.add(step);

        //MultiAccessKey_CoveredTaxon.WorkingSet_id -> MultiAccessKey_id
        stepName = "MultiAccessKey_CoveredTaxon.WorkingSet_id -> MultiAccessKey_id";
        tableName = "MultiAccessKey_CoveredTaxon";
        oldColumnName = "workingset_id";
        newColumnName = "multiaccesskey_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);
//         (also rename table)

        //MultiAccessKey_NamedArea.WorkingSet_id -> MultiAccessKey_id
        stepName = "MultiAccessKey_NamedArea.WorkingSet_id -> MultiAccessKey_id";
        tableName = "MultiAccessKey_NamedArea";
        oldColumnName = "workingset_id";
        newColumnName = "multiaccesskey_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //MultiAccessKey_Scope.WorkingSet_id -> MultiAccessKey_id
        stepName = "MultiAccessKey_Scope.WorkingSet_id -> MultiAccessKey_id ";
        tableName = "MultiAccessKey_Scope";
        oldColumnName = "workingset_id";
        newColumnName = "multiaccesskey_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //Rename MultiAccessKey_Taxon -> MultiAccessKey_TaxonScope
        stepName = "Rename MultiAccessKey_Taxon -> MultiAccessKey_TaxonScope";
        oldTableName = "MultiAccessKey_Taxon";
        newTableName = "MultiAccessKey_TaxonScope";
        step = TableNameChanger.NewInstance(stepName, oldTableName, newTableName, INCLUDE_AUDIT);
        stepList.add(step);

        //MultiAccessKey_TaxonScope.taxon_id -> taxonomicScope_id
        stepName = "MultiAccessKey_TaxonScope.taxon_id -> taxonomicScope_id";
        tableName = "MultiAccessKey_TaxonScope";
        oldColumnName = "taxon_id";
        newColumnName = "taxonomicScope_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //DescriptionBase_Scope.DescriptionBase_id -> TaxonDescription_id
        stepName = "DescriptionBase_Scope.DescriptionBase_id -> TaxonDescription_id";
        tableName = "DescriptionBase_Scope";
        oldColumnName = "descriptionbase_id";
        newColumnName = "taxondescription_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //DescriptionBase_GeoScope.DescriptionBase_id -> TaxonDescription_id
        stepName = "DescriptionBase_GeoScope.DescriptionBase_id -> TaxonDescription_id";
        tableName = "DescriptionBase_GeoScope";
        oldColumnName = "descriptionbase_id";
        newColumnName = "taxondescription_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //DefinedTermBase_Continent.DefinedTermBase_id -> Country_id
        stepName = "DefinedTermBase_Continent.DefinedTermBase_id -> Country_id";
        tableName = "DefinedTermBase_Continent";
        oldColumnName = "definedtermbase_id";
        newColumnName = "country_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //DefinedTermBase_Country.DefinedTermBase_id -> NamedArea_id
        stepName = "DefinedTermBase_Country.DefinedTermBase_id -> NamedArea_id";
        tableName = "DefinedTermBase_Country";
        oldColumnName = "definedtermbase_id";
        newColumnName = "namedarea_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //Media_Sequence.Media_id -> PhylogeneticTree_id
        stepName = "Media_Sequence.Media_id -> PhylogeneticTree_id";
        tableName = "Media_Sequence";
        oldColumnName = "media_id";
        newColumnName = "phylogenetictree_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);


        //RelationshipTermBase_inverseRepresentation.DefinedTermBase_id ->RelationshipTermBase_id
        stepName = "RelationshipTermBase_inverseRepresentation.DefinedTermBase_id ->RelationshipTermBase_id";
        tableName = "RelationshipTermBase_inverseRepresentation";
        oldColumnName = "definedtermbase_id";
        newColumnName = "relationshiptermbase_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //TaxonInteraction_LanguageString.DescriptionElementBase_id -> TaxonInteraction_id
        stepName = "TaxonInteraction_LanguageString.DescriptionElementBase_id -> TaxonInteraction_id";
        tableName = "TaxonInteraction_LanguageString";
        oldColumnName = "descriptionelementbase_id";
        newColumnName = "taxoninteraction_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //IndividualAssociation_LanguageString.DescriptionElementBase_id -> IndividualsAssociation_id
        stepName = "IndividualAssociation_LanguageString.DescriptionElementBase_id -> IndividualsAssociation_id";
        tableName = "IndividualAssociation_LanguageString";
        oldColumnName = "descriptionelementbase_id";
        newColumnName = "individualsassociation_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //Media_Representation.Media_id -> MediaKey_id
        stepName = "Media_Representation.Media_id -> MediaKey_id";
        tableName = "Media_Representation";
        oldColumnName = "media_id";
        newColumnName = "mediakey_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //RightsInfo_Annotation. RightsInfo_id->Rights_id (see AssociationOverrides on class level)
        stepName = "RightsInfo_Annotation. RightsInfo_id->Rights_id";
        tableName = "RightsInfo_Annotation";
        oldColumnName = "rightsinfo_id";
        newColumnName = "rights_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        // RightsInfo_Marker. RightsInfo_id->Rights_id (see AssociationOverrides on class level)
        stepName = "RightsInfo_Marker. RightsInfo_id->Rights_id";
        tableName = "RightsInfo_Marker";
        oldColumnName = "rightsinfo_id";
        newColumnName = "rights_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        // WorkingSet_DescriptionBase.workingSet_id->workingSets_id
        stepName = "WorkingSet_DescriptionBase.workingSet_id->workingSets_id";
        tableName = "WorkingSet_DescriptionBase";
        oldColumnName = "workingSet_id";
        newColumnName = "workingSets_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        // IndividualAssociation_LanguageString -> IndividualsAssociation_LanguageString
        stepName = "IndividualAssociation_LanguageString -> IndividualsAssociation_LanguageString";
        oldTableName = "IndividualAssociation_LanguageString";
        newTableName = "IndividualsAssociation_LanguageString";
        step = TableNameChanger.NewInstance(stepName, oldTableName, newTableName, INCLUDE_AUDIT);
        stepList.add(step);

        //Contact tables
        // AgentBase_contact_emailaddresses-> AgentBase_contact_emailAddresses
        //in 2 steps to avoid "table already exists" on non sensitive systems
        stepName = "AgentBase_contact_emailaddresses-> AgentBase_contact_emailAddresses(I)";
        oldTableName = "AgentBase_contact_emailaddresses";
        newTableName = "AgentBase_contact_emailaddresses2";
        step = TableNameChanger.NewInstance(stepName, oldTableName, newTableName, INCLUDE_AUDIT);
        stepList.add(step);
        stepName = "AgentBase_contact_emailaddresses-> AgentBase_contact_emailAddresses(II)";
        oldTableName = "AgentBase_contact_emailaddresses2";
        newTableName = "AgentBase_contact_emailAddresses";
        step = TableNameChanger.NewInstance(stepName, oldTableName, newTableName, INCLUDE_AUDIT);
        stepList.add(step);

        // AgentBase_contact_faxnumbers-> AgentBase_contact_faxNumbers
        stepName = "AgentBase_contact_faxnumbers-> AgentBase_contact_faxNumbers(I)";
        oldTableName = "AgentBase_contact_faxnumbers";
        newTableName = "AgentBase_contact_faxnumbers2";
        step = TableNameChanger.NewInstance(stepName, oldTableName, newTableName, INCLUDE_AUDIT);
        stepList.add(step);
        stepName = "AgentBase_contact_faxnumbers-> AgentBase_contact_faxNumbers(II)";
        oldTableName = "AgentBase_contact_faxnumbers2";
        newTableName = "AgentBase_contact_faxNumbers";
        step = TableNameChanger.NewInstance(stepName, oldTableName, newTableName, INCLUDE_AUDIT);
        stepList.add(step);

        // AgentBase_contact_phoneNumbers-> AgentBase_contact_phoneNumbers
        stepName = "AgentBase_contact_phonenumbers-> AgentBase_contact_phoneNumbers(I)";
        oldTableName = "AgentBase_contact_phonenumbers";
        newTableName = "AgentBase_contact_phonenumbers2";
        step = TableNameChanger.NewInstance(stepName, oldTableName, newTableName, INCLUDE_AUDIT);
        stepList.add(step);
        stepName = "AgentBase_contact_phonenumbers-> AgentBase_contact_phoneNumbers(II)";
        oldTableName = "AgentBase_contact_phonenumbers2";
        newTableName = "AgentBase_contact_phoneNumbers";
        step = TableNameChanger.NewInstance(stepName, oldTableName, newTableName, INCLUDE_AUDIT);
        stepList.add(step);
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_35_36.NewInstance();
	}

}
