/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v512_515;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.database.update.v515_518.SchemaUpdater_5152_5180;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 10.06.2020
 */
public class SchemaUpdater_5151_5152 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5151_5152.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_15_01;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_15_02;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5151_5152 NewInstance() {
		return new SchemaUpdater_5151_5152();
	}

	protected SchemaUpdater_5151_5152() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {
//
		String stepName;
		String tableName;
		String newColumnName;
		String referencedTable;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		addExternalLinkTables(stepList);

		//#9067
		stepName = "Add link to Media";
		tableName = "Media";
		newColumnName = "link_id";
		referencedTable = "ExternalLink";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //9004
        stepName = "Add source to TaxonNode";
        tableName = "TaxonNode";
        newColumnName = "source_id";
        referencedTable = "OriginalSourceBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        Reference2SourceMover.NewInstance(stepList, tableName, "referenceForParentChildRelation_id", "microReferenceForParentChildRelation", "source_id");

        //6581
        stepName = "Add source to Classification";
        tableName = "Classification";
        newColumnName = "source_id";
        referencedTable = "OriginalSourceBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //6581
        stepName = "Add source to NomenclaturalStatus";
        tableName = "NomenclaturalStatus";
        newColumnName = "source_id";
        referencedTable = "OriginalSourceBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //6581
        stepName = "Add source to TypeDesignationBase";
        tableName = "TypeDesignationBase";
        newColumnName = "source_id";
        referencedTable = "OriginalSourceBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //6581
        stepName = "Add source to TaxonRelationship";
        tableName = "TaxonRelationship";
        newColumnName = "source_id";
        referencedTable = "OriginalSourceBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //6581
        stepName = "Add source to NameRelationship";
        tableName = "NameRelationship";
        newColumnName = "source_id";
        referencedTable = "OriginalSourceBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //6581
        stepName = "Add source to HybridRelationship";
        tableName = "HybridRelationship";
        newColumnName = "source_id";
        referencedTable = "OriginalSourceBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        return stepList;
    }

    private void addExternalLinkTables(List<ISchemaUpdaterStep> stepList) {

        //Table itself exists already
//        String stepName = "Create Identifier table";
//        boolean includeCdmBaseAttributes = true;
//        String tableName = "ExternalLink";
//        String[] columnNames = new String[]{"identifier","identifiedObj_type", "identifiedObj_id","type_id"};
//        String[] columnTypes = new String[]{"string_800","string_255","int","int"};
//        String[] referencedTables = new String[]{null,null,null,"DefinedTermBase"};
//        TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);

        //AgentBase_ExternalLink
        String stepName = "Create AgentBase_ExternalLink table";
        boolean includeCdmBaseAttributes = false;
        String tableName = "AgentBase_ExternalLink";
        String[] columnNames = new String[]{"AgentBase_id","links_id","sortIndex"};
        String[] columnTypes = new String[]{"int","int","int"};
        String[] referencedTables = new String[]{"AgentBase","ExternalLink",null};
        TableCreator step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("AgentBase_id,links_id", "REV,AgentBase_id,links_id");

        //Classification_ExternalLink
        stepName = "Create Classification_ExternalLink table";
        includeCdmBaseAttributes = false;
        tableName = "Classification_ExternalLink";
        columnNames = new String[]{"Classification_id","links_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"Classification","ExternalLink",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("Classification_id,links_id", "REV,Classification_id,links_id");

        //Collection_ExternalLink
        stepName = "Create Collection_ExternalLink table";
        includeCdmBaseAttributes = false;
        tableName = "Collection_ExternalLink";
        columnNames = new String[]{"Collection_id","links_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"Collection","ExternalLink",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("Collection_id,links_id", "REV,Collection_id,links_id");

        //DefinedTermBase_ExternalLink
        stepName = "Create DefinedTermBase_ExternalLink table";
        includeCdmBaseAttributes = false;
        tableName = "DefinedTermBase_ExternalLink";
        columnNames = new String[]{"DefinedTermBase_id","links_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"DefinedTermBase","ExternalLink",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("DefinedTermBase_id,links_id", "REV,DefinedTermBase_id,links_id");

        //DescriptionBase_ExternalLink
        stepName = "Create DescriptionBase_ExternalLink table";
        includeCdmBaseAttributes = false;
        tableName = "DescriptionBase_ExternalLink";
        columnNames = new String[]{"DescriptionBase_id","links_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"DescriptionBase","ExternalLink",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("DescriptionBase_id,links_id", "REV,DescriptionBase_id,links_id");

        //DescriptiveDataSet_ExternalLink
        stepName = "Create DescriptiveDataSet_ExternalLink table";
        includeCdmBaseAttributes = false;
        tableName = "DescriptiveDataSet_ExternalLink";
        columnNames = new String[]{"DescriptiveDataSet_id","links_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"DescriptiveDataSet","ExternalLink",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("DescriptiveDataSet_id,links_id", "REV,DescriptiveDataSet_id,links_id");

        //Media_ExternalLink
        stepName = "Create Media_ExternalLink table";
        includeCdmBaseAttributes = false;
        tableName = "Media_ExternalLink";
        columnNames = new String[]{"Media_id","links_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"Media","ExternalLink",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("Media_id,links_id", "REV,Media_id,links_id");

        //PolytomousKey_ExternalLink
        stepName = "Create PolytomousKey_ExternalLink table";
        includeCdmBaseAttributes = false;
        tableName = "PolytomousKey_ExternalLink";
        columnNames = new String[]{"PolytomousKey_id","links_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"PolytomousKey","ExternalLink",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("PolytomousKey_id,links_id", "REV,PolytomousKey_id,links_id");

        //Reference_ExternalLink
        stepName = "Create Reference_ExternalLink table";
        includeCdmBaseAttributes = false;
        tableName = "Reference_ExternalLink";
        columnNames = new String[]{"Reference_id","links_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"Reference","ExternalLink",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("Reference_id,links_id", "REV,Reference_id,links_id");

        //SpecimenOrObservationBase_ExternalLink
        stepName = "Create SpecimenOrObservationBase_ExternalLink table";
        includeCdmBaseAttributes = false;
        tableName = "SpecimenOrObservationBase_ExternalLink";
        columnNames = new String[]{"SpecimenOrObservationBase_id","links_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"SpecimenOrObservationBase","ExternalLink",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("SpecimenOrObservationBase_id,links_id", "REV,SpecimenOrObservationBase_id,links_id");

        //TaxonBase_ExternalLink
        stepName = "Create TaxonBase_ExternalLink table";
        includeCdmBaseAttributes = false;
        tableName = "TaxonBase_ExternalLink";
        columnNames = new String[]{"TaxonBase_id","links_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"TaxonBase","ExternalLink",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("TaxonBase_id,links_id", "REV,TaxonBase_id,links_id");

        //TaxonName_ExternalLink
        stepName = "Create TaxonName_ExternalLink table";
        includeCdmBaseAttributes = false;
        tableName = "TaxonName_ExternalLink";
        columnNames = new String[]{"TaxonName_id","links_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"TaxonName","ExternalLink",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("TaxonName_id,links_id", "REV,TaxonName_id,links_id");

        //TermCollection_ExternalLink
        stepName = "Create TermCollection_ExternalLink table";
        includeCdmBaseAttributes = false;
        tableName = "TermCollection_ExternalLink";
        columnNames = new String[]{"TermCollection_id","links_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"TermCollection","ExternalLink",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("TermCollection_id,links_id", "REV,TermCollection_id,links_id");
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5150_5151.NewInstance();
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return SchemaUpdater_5152_5180.NewInstance();
	}
}
