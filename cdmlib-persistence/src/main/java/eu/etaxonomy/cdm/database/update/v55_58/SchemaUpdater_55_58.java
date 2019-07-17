/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v55_58;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.database.update.TableNameChanger;
import eu.etaxonomy.cdm.database.update.v50_55.SchemaUpdater_50_55;

/**
 * @author a.mueller
 * @date 01.03.2019
 */
public class SchemaUpdater_55_58 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_55_58.class);

	private static final String startSchemaVersion = "5.5.0.0.20190221";
	private static final String endSchemaVersion = "5.8.0.0.201906020";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_55_58 NewInstance() {
		return new SchemaUpdater_55_58();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_55_58() {
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


        //TODO remove proparte and partial columns

		updateConceptRelationshipSymbolsAgain(stepList);

	   //******** FeatureNode to TermRelation ******/
       //#6794 rename FeatureNode to TermRelation
       stepName = "rename FeatureNode to TermRelation";
       String oldName = "FeatureNode";
       String newName = "TermRelation";
       boolean includeDtype = false;
       step = TableNameChanger.NewInstance(stepName, oldName, newName, INCLUDE_AUDIT, includeDtype);
       stepList.add(step);

       //#6794 add DTYPE to TermRelation
       stepName = "add DTYPE to TermRelation";
       tableName = "TermRelation";
       step = ColumnAdder.NewDTYPEInstance(stepName, tableName, "TermTreeNode", INCLUDE_AUDIT) ;
       stepList.add(step);

       //#6794 add root_id column to TermCollection
       stepName = "add toTerm_id column to TermRelation";
       tableName = "TermRelation";
       newColumnName = "toTerm_id";
       step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, "DefinedTermBase");
       stepList.add(step);

       //#6794 change featuretree_id to graph_id
//       stepName = "change featuretree_id to graph_id";
//       tableName = "TermRelation";
//       String oldColumnName = "featureTree_id";
//       newColumnName = "graph_id";
//       step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
//       stepList.add(step);


       //#6794 change feature_id to term_id
       stepName = "change feature_id to term_id";
       tableName = "TermRelation";
       String oldColumnName = "feature_id";
       newColumnName = "term_id";
       step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
       stepList.add(step);

       //#6794 rename FeatureNode_DefinedTermBase_InapplicableIf to TermTreeNode_DefinedTermBase_InapplicableIf
       stepName = "rename FeatureNode_DefinedTermBase_InapplicableIf to TermTreeNode_DefinedTermBase_InapplicableIf";
       oldName = "FeatureNode_DefinedTermBase_InapplicableIf";
       newName = "TermTreeNode_DefinedTermBase_InapplicableIf";
       includeDtype = false;
       step = TableNameChanger.NewInstance(stepName, oldName, newName, INCLUDE_AUDIT, includeDtype);
       stepList.add(step);

       //#6794 change FeatureNode_id to TermTreeNode_id in TermTreeNode_DefinedTermBase_InapplicableIf
       stepName = "change FeatureNode_id to TermTreeNode_id in TermTreeNode_DefinedTermBase_InapplicableIf";
       tableName = "TermTreeNode_DefinedTermBase_InapplicableIf";
       oldColumnName = "FeatureNode_id";
       newColumnName = "TermTreeNode_id";
       step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
       stepList.add(step);

       //#6794 rename FeatureNode_DefinedTermBase_OnlyApplicable to TermTreeNode_DefinedTermBase_OnlyApplicable
       stepName = "rename FeatureNode_DefinedTermBase_InapplicableIf to TermTreeNode_DefinedTermBase_InapplicableIf";
       oldName = "FeatureNode_DefinedTermBase_OnlyApplicable";
       newName = "TermTreeNode_DefinedTermBase_OnlyApplicable";
       includeDtype = false;
       step = TableNameChanger.NewInstance(stepName, oldName, newName, INCLUDE_AUDIT, includeDtype);
       stepList.add(step);

       //#6794 change FeatureNode_id to TermTreeNode_id in TermTreeNode_DefinedTermBase_OnlyApplicable
       stepName = "change FeatureNode_id to TermTreeNode_id in TermTreeNode_DefinedTermBase_OnlyApplicable";
       tableName = "TermTreeNode_DefinedTermBase_OnlyApplicable";
       oldColumnName = "FeatureNode_id";
       newColumnName = "TermTreeNode_id";
       step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
       stepList.add(step);

       //#6794
       renameTermVocToTermCollection(stepList);


       //#6794 add root_id column to TermCollection
       stepName = "add root_id column to TermCollection";
       tableName = "TermCollection";
       newColumnName = "root_id";
       step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, "TermRelation");
       stepList.add(step);

       //#6794
       stepName = "add graph_id column to TermRelation";
       tableName = "TermRelation";
       newColumnName = "graph_id";
       step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, "TermCollection");
       stepList.add(step);

       //#6794
       stepName = "change descriptiveSystem_id to descriptiveSystemOld_id";
       tableName = "DescriptiveDataSet";
       oldColumnName = "descriptiveSystem_id";
       newColumnName = "descriptiveSystemOld_id";
       step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
       stepList.add(step);

       //#6794
       stepName = "add descriptiveSystem_id column to DescriptiveDataSet";
       tableName = "DescriptiveDataSet";
       newColumnName = "descriptiveSystem_id";
       step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, "TermCollection");
       stepList.add(step);

       //#6794
       addBooleansToTermVocabulary(stepList);

       //#6794
       FeatureTreeMover.NewInstance(stepList);

       //#7470
       updateFreeTextTypeDesignation(stepList);

       return stepList;
	}

    /**
     * @param stepList
     */
    private void addBooleansToTermVocabulary(List<ISchemaUpdaterStep> stepList) {
        String stepName;
        String tableName;
        ISchemaUpdaterStep step;
        String newColumnName;
        //#6794 add allowDuplicates to TermCollection
           stepName = "Add allowDuplicates to TermCollection";
           tableName = "TermCollection";
           newColumnName = "allowDuplicates";
           step = ColumnAdder.NewBooleanInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, false);
           stepList.add(step);

           //#6794 add isFlat to TermCollection
           stepName = "Add isFlat to TermCollection";
           tableName = "TermCollection";
           newColumnName = "isFlat";
           step = ColumnAdder.NewBooleanInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, false);
           stepList.add(step);

           //#6794 add orderRelevant to TermCollection
           stepName = "Add orderRelevant to TermCollection";
           tableName = "TermCollection";
           newColumnName = "orderRelevant";
           step = ColumnAdder.NewBooleanInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, false);
           stepList.add(step);
    }

    /**
     * @param stepList
     */
    private void renameTermVocToTermCollection(List<ISchemaUpdaterStep> stepList) {
        //#6794 rename TermVocabulary to TermCollection
        String stepName = "rename TermVocabulary to TermCollection";
        String oldName = "TermVocabulary";
        String newName = "TermCollection";
        boolean includeDtype = false;
        ISchemaUpdaterStep step = TableNameChanger.NewInstance(stepName, oldName, newName, INCLUDE_AUDIT, includeDtype);
        stepList.add(step);

        //#6794 rename TermVocabulary_Annotation to TermCollection_Annotation
        stepName = "rename TermVocabulary_Annotation to TermCollection_Annotation";
        oldName = "TermVocabulary_Annotation";
        newName = "TermCollection_Annotation";
        includeDtype = false;
        step = TableNameChanger.NewInstance(stepName, oldName, newName, INCLUDE_AUDIT, includeDtype);
        stepList.add(step);

        //#6794 rename TermCollection_Annotation.TermVocabulary_id to TermCollection_id
        stepName = "rename TermCollection_Annotation.TermVocabulary_id to TermCollection_id";
        String tableName = "TermCollection_Annotation";
        String oldColumnName = "TermVocabulary_id";
        String newColumnName = "TermCollection_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //#6794 rename TermVocabulary_Credit to TermCollection_Credit
        stepName = "rename TermVocabulary_Credit to TermCollection_Credit";
        oldName = "TermVocabulary_Credit";
        newName = "TermCollection_Credit";
        includeDtype = false;
        step = TableNameChanger.NewInstance(stepName, oldName, newName, INCLUDE_AUDIT, includeDtype);
        stepList.add(step);

        //#6794 rename TermCollection_Credit.TermVocabulary_id to TermCollection_id
        stepName = "rename TermCollection_Credit.TermVocabulary_id to TermCollection_id";
        tableName = "TermCollection_Credit";
        oldColumnName = "TermVocabulary_id";
        newColumnName = "TermCollection_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //#6794 rename TermVocabulary_Extension to TermCollection_Extension
        stepName = "rename TermVocabulary_Extension to TermCollection_Extension";
        oldName = "TermVocabulary_Extension";
        newName = "TermCollection_Extension";
        includeDtype = false;
        step = TableNameChanger.NewInstance(stepName, oldName, newName, INCLUDE_AUDIT, includeDtype);
        stepList.add(step);

        //#6794 rename TermCollection_Extension.TermVocabulary_id to TermCollection_id
        stepName = "rename TermCollection_Extension.TermVocabulary_id to TermCollection_id";
        tableName = "TermCollection_Extension";
        oldColumnName = "TermVocabulary_id";
        newColumnName = "TermCollection_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //#6794 rename TermVocabulary_Identifier to TermCollection_Identifier
        stepName = "rename TermVocabulary_Identifier to TermCollection_Identifier";
        oldName = "TermVocabulary_Identifier";
        newName = "TermCollection_Identifier";
        includeDtype = false;
        step = TableNameChanger.NewInstance(stepName, oldName, newName, INCLUDE_AUDIT, includeDtype);
        stepList.add(step);

        //#6794 rename TermCollection_Identifier.TermVocabulary_id to TermCollection_id
        stepName = "rename TermCollection_Identifier.TermVocabulary_id to TermCollection_id";
        tableName = "TermCollection_Identifier";
        oldColumnName = "TermVocabulary_id";
        newColumnName = "TermCollection_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //#6794 rename TermVocabulary_Marker to TermCollection_Marker
        stepName = "rename TermVocabulary_Marker to TermCollection_Marker";
        oldName = "TermVocabulary_Marker";
        newName = "TermCollection_Marker";
        includeDtype = false;
        step = TableNameChanger.NewInstance(stepName, oldName, newName, INCLUDE_AUDIT, includeDtype);
        stepList.add(step);

        //#6794 rename TermCollection_Marker.TermVocabulary_id to TermCollection_id
        stepName = "rename TermCollection_Marker.TermVocabulary_id to TermCollection_id";
        tableName = "TermCollection_Marker";
        oldColumnName = "TermVocabulary_id";
        newColumnName = "TermCollection_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //#6794 rename TermVocabulary_OriginalSourceBase to TermCollection_OriginalSourceBase
        stepName = "rename TermVocabulary_OriginalSourceBase to TermCollection_OriginalSourceBase";
        oldName = "TermVocabulary_OriginalSourceBase";
        newName = "TermCollection_OriginalSourceBase";
        includeDtype = false;
        step = TableNameChanger.NewInstance(stepName, oldName, newName, INCLUDE_AUDIT, includeDtype);
        stepList.add(step);

        //#6794 rename TermCollection_OriginalSourceBase.TermVocabulary_id to TermCollection_id
        stepName = "rename TermCollection_OriginalSourceBase.TermVocabulary_id to TermCollection_id";
        tableName = "TermCollection_OriginalSourceBase";
        oldColumnName = "TermVocabulary_id";
        newColumnName = "TermCollection_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //#6794 rename TermVocabulary_Representation to TermCollection_Representation
        stepName = "rename TermVocabulary_Representation to TermCollection_Representation";
        oldName = "TermVocabulary_Representation";
        newName = "TermCollection_Representation";
        includeDtype = false;
        step = TableNameChanger.NewInstance(stepName, oldName, newName, INCLUDE_AUDIT, includeDtype);
        stepList.add(step);

        //#6794 rename TermCollection_Representation.TermVocabulary_id to TermCollection_id
        stepName = "rename TermCollection_Representation.TermVocabulary_id to TermCollection_id";
        tableName = "TermCollection_Representation";
        oldColumnName = "TermVocabulary_id";
        newColumnName = "TermCollection_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //#6794 rename TermVocabulary_RightsInfo to TermCollection_RightsInfo
        stepName = "rename TermVocabulary_RightsInfo to TermCollection_RightsInfo";
        oldName = "TermVocabulary_RightsInfo";
        newName = "TermCollection_RightsInfo";
        includeDtype = false;
        step = TableNameChanger.NewInstance(stepName, oldName, newName, INCLUDE_AUDIT, includeDtype);
        stepList.add(step);

        //#6794 rename TermCollection_RightsInfo.TermVocabulary_id to TermCollection_id
        stepName = "rename TermCollection_RightsInfo.TermVocabulary_id to TermCollection_id";
        tableName = "TermCollection_RightsInfo";
        oldColumnName = "TermVocabulary_id";
        newColumnName = "TermCollection_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);

    }

    /**
     * @param stepList
     */
    private void updateFreeTextTypeDesignation(List<ISchemaUpdaterStep> stepList) {
        String stepName = "Add isVerbatim to TypeDesignationBase";

        String tableName = "TypeDesignationBase";
        String newColumnName = "isVerbatim";
        ISchemaUpdaterStep step = ColumnAdder.NewBooleanInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, false);
        stepList.add(step);

        //add TypeDesignationBase_LanguageString
        stepName = "add TypeDesignationBase_LanguageString";
        tableName = "TypeDesignationBase_LanguageString";
        String[] columnNames = new String[]{"TextualTypeDesignation_id","text_id","text_mapkey_id"};
        String[] columnTypes = new String[]{"int","int","int"};
        String[] referencedTables = new String[]{"TypeDesignationBase","LanguageString","LanguageString"};
        TableCreator tableCreator = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables,
                INCLUDE_AUDIT, ! INCLUDE_CDM_BASE)
                .setPrimaryKeyParams("TextualTypeDesignation_id, text_id", "REV, TextualTypeDesignation_id, text_id, text_mapkey_id")
                .setUniqueParams("text_id", null);


        //TODO PK on ("TextualTypeDesignation_id","text_id")
        //TODO Unique key on "text_id")

        stepList.add(tableCreator);
    }

    //7514  the update in 50_55 was not yet correct
    private void updateConceptRelationshipSymbolsAgain(List<ISchemaUpdaterStep> stepList) {

        //Update misapplied name symbols
        String stepName = "Update misapplied name symbols again";
        String query = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol='"+UTF8.EM_DASH_DOUBLE+"' , inverseSymbol = '"+UTF8.EN_DASH+"' "
                + " WHERE uuid = '1ed87175-59dd-437e-959e-0d71583d8417' ";
        String tableName = "DefinedTermBase";
        ISchemaUpdaterStep step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, -99);
        stepList.add(step);

        //Update pro parte misapplied name symbols
        stepName = "Update pro parte misapplied name symbols again";
        query = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol='"+UTF8.EM_DASH_DOUBLE+"(p.p.)' , inverseSymbol = '"+UTF8.EN_DASH+"(p.p.)' "
                + " WHERE uuid = 'b59b4bd2-11ff-45d1-bae2-146efdeee206' ";
        tableName = "DefinedTermBase";
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, -99);
        stepList.add(step);

        //Update partial misapplied name symbols
        stepName = "Update partial misapplied name symbols again";
        query = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol='"+UTF8.EM_DASH_DOUBLE+"(part.)' , inverseSymbol = '"+UTF8.EN_DASH+"(part.)' "
                + " WHERE uuid = '859fb615-b0e8-440b-866e-8a19f493cd36' ";
        tableName = "DefinedTermBase";
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, -99);
        stepList.add(step);


    }


    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_50_55.NewInstance();
	}

}
