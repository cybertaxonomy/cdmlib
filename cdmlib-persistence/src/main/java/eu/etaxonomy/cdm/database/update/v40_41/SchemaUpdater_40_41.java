// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v40_41;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SortIndexUpdater;
import eu.etaxonomy.cdm.database.update.TableDroper;
import eu.etaxonomy.cdm.database.update.v36_40.SchemaUpdater_36_40;

/**
 * @author a.mueller
 * @created 16.04.2016
 */
public class SchemaUpdater_40_41 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_40_41.class);
	private static final String endSchemaVersion = "4.1.0.0.201607300000";
	private static final String startSchemaVersion = "4.0.0.0.201604200000";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_40_41 NewInstance() {
		return new SchemaUpdater_40_41();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_40_41() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		ISchemaUpdaterStep step;
		String query;
		String newColumnName;
		String oldColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();


        //#5970
        //Implement allowOverride in CdmPreference
        stepName = "Add allowOverride in CdmPreference";
        tableName = "CdmPreference";
        newColumnName = "allowOverride";
        step = ColumnAdder.NewBooleanInstance(stepName, tableName, newColumnName, ! INCLUDE_AUDIT, false);
        stepList.add(step);

        //#5875
        //Implement isDefault to DescriptionBase
        stepName = "Add isDefault in DescriptionBase";
        tableName = "DescriptionBase";
        newColumnName = "isDefault";
        step = ColumnAdder.NewBooleanInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, false);
        stepList.add(step);

        //#5826
        //Cleanup empty name descriptions
        stepName = "Cleanup empty name descriptions";
        query = " DELETE FROM @@DescriptionBase@@ db " +
                 " WHERE db.DTYPE = 'TaxonNameDescription' " +
                 " AND NOT EXISTS (SELECT * FROM @@DescriptionElementBase@@ deb WHERE deb.indescription_id = db.id )";
        SimpleSchemaUpdaterStep simpleStep = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, "DescriptionBase", -99);
        stepList.add(simpleStep);

        //#5921
        //UPDATE congruent symbol in DefinedTermBase
        stepName = "UPDATE congruent symbol in DefinedTermBase";
        query = " UPDATE @@DefinedTermBase@@ "
                + " SET idInVocabulary = Replace(idInVocabulary, '\u2245', '\u225c'), symbol = Replace(symbol, '\u2245', '\u225c'), inverseSymbol = Replace(inverseSymbol, '\u2245', '\u225c')"
                + " WHERE DTYPE like 'TaxonRel%' "
                + "     AND (idInVocabulary like '%\u2245%' OR symbol like '%\u2245%' OR inverseSymbol like '%\u2245%' )";
        simpleStep = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, "DefinedTermBase", -99);
        stepList.add(simpleStep);

        //#5921
        //UPDATE congruent symbol in Representations
        stepName = "UPDATE congruent symbol in Representations";
        query = " UPDATE @@Representation@@ "
                + " SET abbreviatedLabel = Replace(abbreviatedLabel, '\u2245', '\u225c') "
                + " WHERE (abbreviatedLabel like '%\u2245%' )";
        simpleStep = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, "Representation", -99);
        stepList.add(simpleStep);



        //#5976
        //update sortindex on FeatureNode children
        stepName = "Update sort index on FeatureNode children";
        tableName = "FeatureNode";
        String parentIdColumn = "parent_id";
        String sortIndexColumn = "sortIndex";
        SortIndexUpdater updateSortIndex = SortIndexUpdater.NewInstance(stepName, tableName, "parent_fk", sortIndexColumn, INCLUDE_AUDIT);
        stepList.add(updateSortIndex);

        //#5976
        // update sortindex for TaxonNodes
        stepName = "Update sort index on TaxonNode children";
        tableName = "TaxonNode";
        parentIdColumn = "parent_id";
        sortIndexColumn = "sortIndex";
        updateSortIndex = SortIndexUpdater.NewInstance(
                stepName, tableName, parentIdColumn, sortIndexColumn,
                INCLUDE_AUDIT);
        stepList.add(updateSortIndex);

        //#5976
        // TODO?: update sortindex for PolytomousKeyNodes

        //#3925
        //excluded to TaxonNode
        stepName = "Add excluded to TaxonNode";
        tableName = "TaxonNode";
        newColumnName = "excluded";
        step = ColumnAdder.NewBooleanInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, false);
        stepList.add(step);


        //#3925
        //Mode excluded from Taxon to TaxonNode
        stepName = "Move excluded from Taxon to TaxonNode";
        query = "UPDATE @@TaxonNode@@ tn "
                + " SET excluded = (SELECT exluded FROM @@TaxonBase@@ tb "
                +       " INNER JOIN @@TaxonNode@@ tn2 ON tn2.taxon_id = tb.id "
                +       " WHERE tn.id = tn2.id ";
        simpleStep = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, "TaxonNode", -99);
        stepList.add(simpleStep);

        //#3925
        //excluded to TaxonNode
        stepName = "Remove excluded from TaxonBase";
        tableName = "TaxonBase";
        oldColumnName = "excluded";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //5778
        //update PresenceAbsenceTerm symbols
        updatePresenceAbsenceTermSymbols(stepList);

        //6089
        //Remove taxonomicParentCache from Taxon
        stepName = "Remove taxonomicParentCache from Taxon";
        tableName = "TaxonBase";
        oldColumnName = "taxonomicParentCache";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //6089
        //Remove taxonomicChildrenCount from Taxon
        stepName = "Remove taxonomicChildrenCount from Taxon";
        tableName = "TaxonBase";
        oldColumnName = "taxonomicChildrenCount";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //#5974 Remove synonym relationships
        removeSynonymRelationships_5974(stepList);


        return stepList;
    }

	private void removeSynonymRelationships_5974(List<ISchemaUpdaterStep> stepList) {
	    //add partial to Synonym
        String stepName = "Add partial to Synonym";
        String tableName = "TaxonBase";
        String newColumnName = "partial";
        ISchemaUpdaterStep step = ColumnAdder.NewBooleanInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, false);
        stepList.add(step);

	    //add proParte to Synonym
        stepName = "Add proParte to Synonym";
        tableName = "TaxonBase";
        newColumnName = "proParte";
        step = ColumnAdder.NewBooleanInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, false);
        stepList.add(step);

	    //add type to Synonym
        stepName = "Add type to Synonym";
        tableName = "TaxonBase";
        newColumnName = "type_id";
        String referencedTable = "DefinedTermBase";
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
        stepList.add(step);

	    //add acceptedTaxon_id to Synonym
        stepName = "Add acceptedTaxon to Synonym";
        tableName = "TaxonBase";
        newColumnName = "acceptedTaxon_id";
        referencedTable = "TaxonBase";
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
        stepList.add(step);

	    //move data
        //update pro parte
        stepName = "Update proParte";
        String updateSql = "UPDATE %%TaxonBase%% syn " +
                " SET proParte=1 =(SELECT proParte FROM %%SynonymRelationship%% sr WHERE sr.relatedFrom_id = syn.id)";
//        -- WHERE EXISTS (SELECT proParte FROM SynonymRelationship sr WHERE sr.relatedFrom_id = syn.id AND sr.proParte = 1);
        String updateSqlAud = updateSql.replace("TaxonBase", "TaxonBase_AUD").replace("SynonymRelationship", "SynonymRelationship_AUD");
        step = SimpleSchemaUpdaterStep.NewExplicitAuditedInstance(stepName, updateSql, updateSqlAud, -99);
        stepList.add(step);

        //update partial
        stepName = "Update partial";
        updateSql = "UPDATE @@TaxonBase@@ syn " +
                " SET partial=(SELECT partial FROM @@SynonymRelationship@@ sr WHERE sr.relatedFrom_id = syn.id) ";
        updateSqlAud = updateSql.replace("TaxonBase", "TaxonBase_AUD").replace("SynonymRelationship", "SynonymRelationship_AUD");

        //update synonym type
        stepName = "Update Synonym type";
        updateSql = "UPDATE @@TaxonBase@@ syn " +
                " SET type_id=(SELECT type_id FROM @@SynonymRelationship@@ sr WHERE sr.relatedFrom_id = syn.id)";
        updateSqlAud = updateSql.replace("TaxonBase", "TaxonBase_AUD").replace("SynonymRelationship", "SynonymRelationship_AUD");

        //update acceptedTaxon_id
        stepName = "Update acceptedTaxon_id";
        updateSql = "UPDATE @@TaxonBase@@ syn " +
                " SET acceptedTaxon_id=(SELECT relatedTo_id FROM @@SynonymRelationship@@ sr WHERE sr.relatedFrom_id = syn.id)";
        updateSqlAud = updateSql.replace("TaxonBase", "TaxonBase_AUD").replace("SynonymRelationship", "SynonymRelationship_AUD");
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, updateSql, updateSqlAud, -99);
        stepList.add(step);

	    //rename SynonymRelationshipType to SynonymType in DefinedTermBase.DTYPE
        stepName = "Rename SynonymRelationshipType to SynonymType in DefinedTermBase.DTYPE";
        updateSql = "UPDATE DefinedTermBase SET DTYPE='SynonymType' WHERE DTYPE='SynonymRelationshipType'";
        String nonAuditedTableName = "DefinedTermBase";
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, updateSql, nonAuditedTableName, -99);
        stepList.add(step);

        //rename SynonymRelationshipType to SynonymType in DefinedTermBase.titleCache
        stepName = "Rename SynonymRelationshipType to SynonymType in DefinedTermBase.titleCache";
        updateSql = "UPDATE DefinedTermBase SET titleCache='SynonymType' WHERE titleCache='SynonymRelationshipType'";
        nonAuditedTableName = "DefinedTermBase";
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, updateSql, nonAuditedTableName, -99);
        stepList.add(step);

        //rename SynonymRelationshipType to SynonymType in Representation labels
        stepName = "Rename SynonymRelationshipType to SynonymType in Representation labels";
        updateSql = "UPDATE Representation SET label='SynonymType' WHERE label='SynonymRelationshipType'";
        nonAuditedTableName = "Representation";
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, updateSql, nonAuditedTableName, -99);
        stepList.add(step);

        //rename SynonymRelationshipType to SynonymType in Representation text
        stepName = "Rename SynonymRelationshipType to SynonymType in Representation text";
        updateSql = "UPDATE Representation SET text='SynonymType' WHERE text='SynonymRelationshipType'";
        nonAuditedTableName = "Representation";
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, updateSql, nonAuditedTableName, -99);
        stepList.add(step);

	    //remove SynonymRelationship_Annotation
        stepName = "Remove SynonymRelationship_Annotation table";
        tableName = "SynonymRelationship_Annotation";
        step = TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
        stepList.add(step);

        //remove SynonymRelationship_Marker
        stepName = "Remove SynonymRelationship_Marker table";
        tableName = "SynonymRelationship_Marker";
        step = TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
        stepList.add(step);

        //remove SynonymRelationship table
        stepName = "Remove synonym relationship table";
        tableName = "SynonymRelationship";
        step = TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
        stepList.add(step);
	}

    /**
     * @param stepList
     */
    private void updatePresenceAbsenceTermSymbols(List<ISchemaUpdaterStep> stepList) {
        String enDash = UTF8.EN_DASH.toString();

        //endemic
        updateSinglePATsymbol(stepList, "c3ee7048-15b7-4be1-b687-9ce9c1a669d6", "e", "" + UTF8.BLACK_CIRCLE);
        //present
        updateSinglePATsymbol(stepList, "cef81d25-501c-48d8-bbea-542ec50de2c2", "p", "");
        //doubtfully present
        updateSinglePATsymbol(stepList, "75a60279-a4c2-4f53-bc57-466028a4b3db", "pd", "?");
        //native
        updateSinglePATsymbol(stepList, "ddeac4f2-d8fa-43b8-ad7e-ca13abdd32c7", "n", "");
        //native: doubtfully native
        updateSinglePATsymbol(stepList, "310373bf-7df4-4d02-8cb3-bcc7448805fc", "nd", "d");
        //introduced: naturalized
        updateSinglePATsymbol(stepList, "e191e89a-a751-4b0c-b883-7f1de70915c9", "in", "n");
        //introduced: adventitious (casual)
        updateSinglePATsymbol(stepList, "42946bd6-9c22-45ad-a910-7427e8f60bfd", "ia", "a");
        //naturalised
        updateSinglePATsymbol(stepList, "4e04990a-66fe-4fdf-856c-f40772fbcf0a", "na", "n");
        //native: presence questionable
        updateSinglePATsymbol(stepList, "925662c1-bb10-459a-8c53-da5a738ac770", "nq", "?");
        //introduced: presence questionable
        updateSinglePATsymbol(stepList, "83eb0aa0-1a45-495a-a3ca-bf6958b74366", "iq", "?");
        //introduced: cultivated
        updateSinglePATsymbol(stepList, "fac8c347-8262-44a1-b0a4-db4de451c021", "ic", "c");
        //cultivated, presence questionable
        updateSinglePATsymbol(stepList, "4f31bfc8-3058-4d83-aea5-3a1fe9773f9f", "cq", "?c");
        //absent
        updateSinglePATsymbol(stepList, "59709861-f7d9-41f9-bb21-92559cedd598", "a", enDash);
        //reported in error
        updateSinglePATsymbol(stepList, "38604788-cf05-4607-b155-86db456f7680", "f", enDash);
        //native: reported in error
        updateSinglePATsymbol(stepList, "aeec2947-2700-4623-8e32-9e3a430569d1", "if", enDash);
        //cultivated: reported in error
        updateSinglePATsymbol(stepList, "9d4d3431-177a-4abe-8e4b-1558573169d6", "cf", enDash);

    }

    /**
     * @param uuid the uuid
     * @param oldSymbol
     * @param newSybol
     */
    private void updateSinglePATsymbol(List<ISchemaUpdaterStep> stepList,
            String uuid, String oldSymbol, String newSymbol) {
        String stepName = "Update single symbol for PresenceAbsenceTerm " + uuid;
        String query = "UPDATE @@DefinedTermBase@@ dtb "
                + " SET symbol = '" + newSymbol + "'"
                + " WHERE uuid = '" + uuid + "' AND symbol = '" + oldSymbol + "'" ;
        SimpleSchemaUpdaterStep simpleStep = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, "DefinedTermBase", -99);
        stepList.add(simpleStep);
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_36_40.NewInstance();
	}

}
