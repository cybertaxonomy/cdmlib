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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableNameChanger;

/**
 * @author a.mueller
 * @since 16.04.2016
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
		String query;
		String newColumnName;
		String oldColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //#5606
        //Add preferred stable URI to SpecimenOrObservation
        stepName = "Add preferred stable URI to SpecimenOrObservation";
        tableName = "SpecimenOrObservationBase";
        newColumnName = "preferredStableUri";
        ColumnAdder.NewClobInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        //#5717
        //Add sec micro reference
        stepName = "Add secMicroReference to TaxonBase";
        tableName = "TaxonBase";
        newColumnName = "secMicroReference";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        //#5718
        //Remove autoincrement from AuditEvent.revisionnumber if necessary
        stepName = "Remove autoincrement from AuditEvent.revisionnumber";
        RevisionNumberUpdater.NewInstance(stepList, stepName);

        //#5734
        //Add symbol to terms
        stepName = "Add symbols to terms";
        tableName = "DefinedTermBase";
        newColumnName = "symbol";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, 30, INCLUDE_AUDIT);

        stepName = "Update symbols for terms";
        query = "UPDATE @@DefinedTermBase@@ SET symbol = idInVocabulary WHERE idInVocabulary <> '' AND termType IN ('PAT','TRT')";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, -99);

        //Add inverse symbol to terms
        stepName = "Add inverse symbol to terms";
        tableName = "DefinedTermBase";
        newColumnName = "inverseSymbol";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, 30, INCLUDE_AUDIT);

        stepName = "Update symbols for terms";
        query = "UPDATE DefinedTermBase dtb "
            + "SET inverseSymbol = ( " +
                " SELECT  r.abbreviatedlabel " +
                " FROM RelationshipTermBase_inverseRepresentation MN " +
                    " INNER JOIN Representation r ON r.id = MN.inverserepresentations_id " +
                " WHERE dtb.id = MN.DefinedTermBase_id AND r.abbreviatedlabel <> '' ) "
            + " WHERE termType IN ('PAT','TRT') ";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query, -99);

        //#5369
        renameColumnsAccordingToHibernate5(stepList);

        //Update xxxObj_type  (#3701)
        ReferencedObjTypeUpdater.NewInstance(stepList);

        //remove bidirectionality from supplemental data #5743
        //annotation
        stepName = "Remove Annotation.annotatedObj_type";
        tableName = "Annotation";
        oldColumnName = "annotatedObj_type";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);

        stepName = "Remove Annotation.annotatedObj_id";
        tableName = "Annotation";
        oldColumnName = "annotatedObj_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);

        //marker
        stepName = "Remove Marker.markedObj_type";
        tableName = "Marker";
        oldColumnName = "markedObj_type";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);

        stepName = "Remove Marker.markedObj_id";
        tableName = "Marker";
        oldColumnName = "markedObj_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);

        //extension
        stepName = "Remove Extension.extendedObj_type";
        tableName = "Extension";
        oldColumnName = "extendedObj_type";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);

        stepName = "Remove Extension.extendedObj_id";
        tableName = "Extension";
        oldColumnName = "extendedObj_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);

        //sources
        stepName = "Remove OriginalSourceBase.sourcedObj_type";
        tableName = "OriginalSourceBase";
        oldColumnName = "sourcedObj_type";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);

        stepName = "Remove OriginalSourceBase.sourcedObj_id";
        tableName = "OriginalSourceBase";
        oldColumnName = "sourcedObj_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);

        //identifier
        stepName = "Remove Identifier.identifiedObj_type";
        tableName = "Identifier";
        oldColumnName = "identifiedObj_type";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);

        stepName = "Remove Identifier.identifiedObj_id";
        tableName = "Identifier";
        oldColumnName = "identifiedObj_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, ! INCLUDE_AUDIT);

        return stepList;
	}


	//#5369
    private void renameColumnsAccordingToHibernate5(List<ISchemaUpdaterStep> stepList) {

        //AgenBase_AgentBase.AgentBase_ID  -> Team_ID
        String stepName = "Rename columns according to hibernate5";
        String tableName = "AgentBase_AgentBase";
        String oldColumnName = "agentbase_id";
        String newColumnName = "team_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //DescriptionElementBase_LanguageString.DescriptionElementBase_ID -> TextData_ID
        stepName = "Rename DescriptionElementBase_LanguageString.DescriptionElementBase_ID";
        tableName = "DescriptionElementBase_LanguageString";
        oldColumnName = "descriptionElementBase_id";
        newColumnName = "textdata_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //AgentBase_DefinedTermBase.AgentBase_ID -> Institution_id
        stepName = "Rename AgentBase_DefinedTermBase.AgentBase_ID -> Institution_id";
        tableName = "AgentBase_DefinedTermBase";
        oldColumnName = "agentbase_id";
        newColumnName = "institution_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //PermissionGroup_GrantedAuthorityImpl.PermsiionsGroup_id -> Group_id
        stepName = "PermissionGroup_GrantedAuthorityImpl.PermsiionsGroup_id -> Group_id ";
        tableName = "PermissionGroup_GrantedAuthorityImpl";
        oldColumnName = "Permissiongroup_id";
        newColumnName = "group_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, ! INCLUDE_AUDIT);

        //UserAccount_GrantedAuthorityImpl.UserAccount_id -> User_id
        stepName = "UserAccount_GrantedAuthorityImpl.UserAccount_id -> User_id ";
        tableName = "UserAccount_GrantedAuthorityImpl";
        oldColumnName = "useraccount_id";
        newColumnName = "user_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, ! INCLUDE_AUDIT);

        //DefinedTermBase_RecommendedModifierEnumeration.DefinedTermBase_id -> Feature_id
        stepName = "DefinedTermBase_RecommendedModifierEnumeration.DefinedTermBase_id -> Feature_id";
        tableName = "DefinedTermBase_RecommendedModifierEnumeration";
        oldColumnName = "definedtermbase_id";
        newColumnName = "feature_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //DefinedTermBase_StatisticalMeasure.DefinedTermBase_id -> Feature_id
        stepName = "DefinedTermBase_StatisticalMeasure.DefinedTermBase_id -> Feature_id";
        tableName = "DefinedTermBase_StatisticalMeasure";
        oldColumnName = "definedtermbase_id";
        newColumnName = "feature_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //DefinedTermBase_SupportedCategoricalEnumeration.DefinedTermBase_id -> Feature_id
        stepName = "DefinedTermBase_SupportedCategoricalEnumeration.DefinedTermBase_id -> Feature_id";
        tableName = "DefinedTermBase_SupportedCategoricalEnumeration";
        oldColumnName = "definedtermbase_id";
        newColumnName = "feature_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //DefinedTermBase_MeasurementUnit.DefinedTermBase_id -> Feature_id
        stepName = "DefinedTermBase_MeasurementUnit.DefinedTermBase_id -> Feature_id";
        tableName = "DefinedTermBase_MeasurementUnit";
        oldColumnName = "definedtermbase_id";
        newColumnName = "feature_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //Media_TaxonBase.Media_id -> MediaKey_id
        stepName = "Media_TaxonBase.DefinedTermBase_id -> Feature_id";
        tableName = "Media_TaxonBase";
        oldColumnName = "media_id";
        newColumnName = "mediakey_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //Rename Media_TaxonBase -> MediaKey_CoveredTaxon
        stepName = "Rename Media_TaxonBase -> MediaKey_CoveredTaxon";
        String oldTableName = "Media_TaxonBase";
        String newTableName = "MediaKey_CoveredTaxon";
        TableNameChanger.NewInstance(stepList, stepName, oldTableName, newTableName, INCLUDE_AUDIT);

        //MediaKey_NamedArea.Media_id -> MediaKey_id
        stepName = "MediaKey_NamedArea.Media_id -> MediaKey_id";
        tableName = "MediaKey_NamedArea";
        oldColumnName = "media_id";
        newColumnName = "mediakey_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //MediaKey_Scope.Media_id -> MediaKey_id
        stepName = "MediaKey_Scope.Media_id -> MediaKey_id";
        tableName = "MediaKey_Scope";
        oldColumnName = "media_id";
        newColumnName = "mediakey_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //Rename MediaKey_Taxon -> MediaKey_TaxonScope
        stepName = "Rename MediaKey_Taxon -> MediaKey_TaxonScope";
        oldTableName = "MediaKey_Taxon";
        newTableName = "MediaKey_TaxonScope";
        TableNameChanger.NewInstance(stepList, stepName, oldTableName, newTableName, INCLUDE_AUDIT);

        //MediaKey_TaxonScope.taxon_id -> taxonomicScope_id
        stepName = "MediaKey_TaxonScope.taxon_id -> taxonomicScope_id";
        tableName = "MediaKey_TaxonScope";
        oldColumnName = "taxon_id";
        newColumnName = "taxonomicScope_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //Rename WorkingSet_TaxonBase -> MultiAccessKey_CoveredTaxon
        stepName = "Rename WorkingSet_TaxonBase -> MultiAccessKey_CoveredTaxon";
        oldTableName = "WorkingSet_TaxonBase";
        newTableName = "MultiAccessKey_CoveredTaxon";
        TableNameChanger.NewInstance(stepList, stepName, oldTableName, newTableName, INCLUDE_AUDIT);

        //MultiAccessKey_CoveredTaxon.WorkingSet_id -> MultiAccessKey_id
        stepName = "MultiAccessKey_CoveredTaxon.WorkingSet_id -> MultiAccessKey_id";
        tableName = "MultiAccessKey_CoveredTaxon";
        oldColumnName = "workingset_id";
        newColumnName = "multiaccesskey_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
//         (also rename table)

        //MultiAccessKey_NamedArea.WorkingSet_id -> MultiAccessKey_id
        stepName = "MultiAccessKey_NamedArea.WorkingSet_id -> MultiAccessKey_id";
        tableName = "MultiAccessKey_NamedArea";
        oldColumnName = "workingset_id";
        newColumnName = "multiaccesskey_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //MultiAccessKey_Scope.WorkingSet_id -> MultiAccessKey_id
        stepName = "MultiAccessKey_Scope.WorkingSet_id -> MultiAccessKey_id ";
        tableName = "MultiAccessKey_Scope";
        oldColumnName = "workingset_id";
        newColumnName = "multiaccesskey_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //Rename MultiAccessKey_Taxon -> MultiAccessKey_TaxonScope
        stepName = "Rename MultiAccessKey_Taxon -> MultiAccessKey_TaxonScope";
        oldTableName = "MultiAccessKey_Taxon";
        newTableName = "MultiAccessKey_TaxonScope";
        TableNameChanger.NewInstance(stepList, stepName, oldTableName, newTableName, INCLUDE_AUDIT);

        //MultiAccessKey_TaxonScope.taxon_id -> taxonomicScope_id
        stepName = "MultiAccessKey_TaxonScope.taxon_id -> taxonomicScope_id";
        tableName = "MultiAccessKey_TaxonScope";
        oldColumnName = "taxon_id";
        newColumnName = "taxonomicScope_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //DescriptionBase_Scope.DescriptionBase_id -> TaxonDescription_id
        stepName = "DescriptionBase_Scope.DescriptionBase_id -> TaxonDescription_id";
        tableName = "DescriptionBase_Scope";
        oldColumnName = "descriptionbase_id";
        newColumnName = "taxondescription_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //DescriptionBase_GeoScope.DescriptionBase_id -> TaxonDescription_id
        stepName = "DescriptionBase_GeoScope.DescriptionBase_id -> TaxonDescription_id";
        tableName = "DescriptionBase_GeoScope";
        oldColumnName = "descriptionbase_id";
        newColumnName = "taxondescription_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //DefinedTermBase_Continent.DefinedTermBase_id -> Country_id
        stepName = "DefinedTermBase_Continent.DefinedTermBase_id -> Country_id";
        tableName = "DefinedTermBase_Continent";
        oldColumnName = "definedtermbase_id";
        newColumnName = "country_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //DefinedTermBase_Country.DefinedTermBase_id -> NamedArea_id
        stepName = "DefinedTermBase_Country.DefinedTermBase_id -> NamedArea_id";
        tableName = "DefinedTermBase_Country";
        oldColumnName = "definedtermbase_id";
        newColumnName = "namedarea_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //Media_Sequence.Media_id -> PhylogeneticTree_id
        stepName = "Media_Sequence.Media_id -> PhylogeneticTree_id";
        tableName = "Media_Sequence";
        oldColumnName = "media_id";
        newColumnName = "phylogenetictree_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //RelationshipTermBase_inverseRepresentation.DefinedTermBase_id ->RelationshipTermBase_id
        stepName = "RelationshipTermBase_inverseRepresentation.DefinedTermBase_id ->RelationshipTermBase_id";
        tableName = "RelationshipTermBase_inverseRepresentation";
        oldColumnName = "definedtermbase_id";
        newColumnName = "relationshiptermbase_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //TaxonInteraction_LanguageString.DescriptionElementBase_id -> TaxonInteraction_id
        stepName = "TaxonInteraction_LanguageString.DescriptionElementBase_id -> TaxonInteraction_id";
        tableName = "TaxonInteraction_LanguageString";
        oldColumnName = "descriptionelementbase_id";
        newColumnName = "taxoninteraction_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //IndividualAssociation_LanguageString.DescriptionElementBase_id -> IndividualsAssociation_id
        stepName = "IndividualAssociation_LanguageString.DescriptionElementBase_id -> IndividualsAssociation_id";
        tableName = "IndividualAssociation_LanguageString";
        oldColumnName = "descriptionelementbase_id";
        newColumnName = "individualsassociation_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //Media_Representation.Media_id -> MediaKey_id
        stepName = "Media_Representation.Media_id -> MediaKey_id";
        tableName = "Media_Representation";
        oldColumnName = "media_id";
        newColumnName = "mediakey_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //RightsInfo_Annotation. RightsInfo_id->Rights_id (see AssociationOverrides on class level)
        stepName = "RightsInfo_Annotation. RightsInfo_id->Rights_id";
        tableName = "RightsInfo_Annotation";
        oldColumnName = "rightsinfo_id";
        newColumnName = "rights_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        // RightsInfo_Marker. RightsInfo_id->Rights_id (see AssociationOverrides on class level)
        stepName = "RightsInfo_Marker. RightsInfo_id->Rights_id";
        tableName = "RightsInfo_Marker";
        oldColumnName = "rightsinfo_id";
        newColumnName = "rights_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        // WorkingSet_DescriptionBase.workingSet_id->workingSets_id
        stepName = "WorkingSet_DescriptionBase.workingSet_id->workingSets_id";
        tableName = "WorkingSet_DescriptionBase";
        oldColumnName = "workingSet_id";
        newColumnName = "workingSets_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        // IndividualAssociation_LanguageString -> IndividualsAssociation_LanguageString
        stepName = "IndividualAssociation_LanguageString -> IndividualsAssociation_LanguageString";
        oldTableName = "IndividualAssociation_LanguageString";
        newTableName = "IndividualsAssociation_LanguageString";
        TableNameChanger.NewInstance(stepList, stepName, oldTableName, newTableName, INCLUDE_AUDIT);

        //Contact tables
        // AgentBase_contact_emailaddresses-> AgentBase_contact_emailAddresses
        //in 2 steps to avoid "table already exists" on non sensitive systems
        stepName = "AgentBase_contact_emailaddresses-> AgentBase_contact_emailAddresses(I)";
        oldTableName = "AgentBase_contact_emailaddresses";
        newTableName = "AgentBase_contact_emailaddresses2";
        TableNameChanger.NewInstance(stepList, stepName, oldTableName, newTableName, INCLUDE_AUDIT);
        stepName = "AgentBase_contact_emailaddresses-> AgentBase_contact_emailAddresses(II)";
        oldTableName = "AgentBase_contact_emailaddresses2";
        newTableName = "AgentBase_contact_emailAddresses";
        TableNameChanger.NewInstance(stepList, stepName, oldTableName, newTableName, INCLUDE_AUDIT);

        // AgentBase_contact_faxnumbers-> AgentBase_contact_faxNumbers
        stepName = "AgentBase_contact_faxnumbers-> AgentBase_contact_faxNumbers(I)";
        oldTableName = "AgentBase_contact_faxnumbers";
        newTableName = "AgentBase_contact_faxnumbers2";
        TableNameChanger.NewInstance(stepList, stepName, oldTableName, newTableName, INCLUDE_AUDIT);
        stepName = "AgentBase_contact_faxnumbers-> AgentBase_contact_faxNumbers(II)";
        oldTableName = "AgentBase_contact_faxnumbers2";
        newTableName = "AgentBase_contact_faxNumbers";
        TableNameChanger.NewInstance(stepList, stepName, oldTableName, newTableName, INCLUDE_AUDIT);

        // AgentBase_contact_phoneNumbers-> AgentBase_contact_phoneNumbers
        stepName = "AgentBase_contact_phonenumbers-> AgentBase_contact_phoneNumbers(I)";
        oldTableName = "AgentBase_contact_phonenumbers";
        newTableName = "AgentBase_contact_phonenumbers2";
        TableNameChanger.NewInstance(stepList, stepName, oldTableName, newTableName, INCLUDE_AUDIT);
        stepName = "AgentBase_contact_phonenumbers-> AgentBase_contact_phoneNumbers(II)";
        oldTableName = "AgentBase_contact_phonenumbers2";
        newTableName = "AgentBase_contact_phoneNumbers";
        TableNameChanger.NewInstance(stepList, stepName, oldTableName, newTableName, INCLUDE_AUDIT);
    }

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_35_36.NewInstance();
	}

}
