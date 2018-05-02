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
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ColumnTypeChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.database.update.TableDroper;
import eu.etaxonomy.cdm.database.update.v31_33.SchemaUpdater_33_331;

/**
 * @author a.mueller
 * @since Jan 14, 2014
 */
public class SchemaUpdater_331_34 extends SchemaUpdaterBase {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(SchemaUpdater_331_34.class);
    private static final String startSchemaVersion = "3.3.1.0.201401140000";
    private static final String endSchemaVersion = "3.4.0.0.201407010000";

    // ********************** FACTORY METHOD *************************************

    public static SchemaUpdater_331_34 NewInstance() {
        return new SchemaUpdater_331_34();
    }

    /**
     * @param startSchemaVersion
     * @param endSchemaVersion
     */
    protected SchemaUpdater_331_34() {
        super(startSchemaVersion, endSchemaVersion);
    }

    @Override
    protected List<ISchemaUpdaterStep> getUpdaterList() {

        String stepName;
        String tableName;
        ISchemaUpdaterStep step;
        String columnName;
        String newColumnName;
        String oldColumnName;

        List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();

        //TODO H2 / PostGreSQL / SQL Server
        //UserAccount unique
        stepName = "Update User unique indexes";
        tableName = "UserAccount";
        columnName = "username";
        step = UsernameConstraintUpdater.NewInstance(stepName, tableName, columnName);
        stepList.add(step);

        //TODO H2 / PostGreSQL / SQL Server
        //PermissionGroup unique
        stepName = "Update Group unique indexes";
        tableName = "PermissionGroup";
        columnName = "name";
        step = UsernameConstraintUpdater.NewInstance(stepName, tableName, columnName);
        stepList.add(step);

        //TODO H2 / PostGreSQL / SQL Server
        //GrantedAuthority unique
        stepName = "Update User unique indexes";
        tableName = "GrantedAuthorityImpl";
        columnName = "authority";
        step = UsernameConstraintUpdater.NewInstance(stepName, tableName, columnName);
        stepList.add(step);

        //TODO H2 / PostGreSQL / SQL Server
        stepName = "Add label column to derived unit";
        tableName = "SpecimenOrObservationBase";
        columnName = "originalLabelInfo";
        step = ColumnAdder.NewClobInstance(stepName, tableName, columnName, INCLUDE_AUDIT);
        stepList.add(step);


        //TODO test with data and H2 / PostGreSQL / SQL Server
        //set default value to true where required
        stepName = "Set publish to true if null";
        String query = " UPDATE @@TaxonBase@@ " +
                    " SET publish = @TRUE@ " +
                    " WHERE DTYPE IN ('Synonym') AND publish IS NULL ";
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, "TaxonBase", 99);
        stepList.add(step);


        addIdentifierTables(stepList);


        //remove series from Reference  #4293
        stepName = "Copy series to series part";
        String sql = " UPDATE Reference r " +
                " SET r.seriespart = r.series " +
                " WHERE r.series is NOT NULL AND r.seriesPart IS NULL ";
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, sql, "Reference", 99);
        stepList.add(step);

        stepName = "Set series to NULL";
        sql = " UPDATE Reference r " +
                " SET r.series = NULL " +
                " WHERE r.series = r.seriesPart ";
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, sql, "Reference", 99);
        stepList.add(step);

        //TODO check all series are null

        stepName = "Remove series column";
        tableName = "Reference";
        oldColumnName = "series";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //authorTeam -> authorship
        stepName = "Rename Reference.authorTeam column";
        tableName = "Reference";
        oldColumnName = "authorTeam_id";
        newColumnName = "authorship_id";
        step = ColumnNameChanger.NewIntegerInstance(stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);
        stepList.add(step);
//
        //remove CDM_VIEW #4316
        stepName = "Remove CDM_VIEW_CDM_VIEW table";
        tableName = "CDM_VIEW_CDM_VIEW";
        boolean ifExists = true;
        step = TableDroper.NewInstance(stepName, tableName, ! INCLUDE_AUDIT, ifExists);
        stepList.add(step);

        stepName = "Remove CDM_VIEW table";
        tableName = "CDM_VIEW";
        ifExists = true;
        step = TableDroper.NewInstance(stepName, tableName, ! INCLUDE_AUDIT, ifExists);
        stepList.add(step);


        //TODO not null on username, groupname and authority  #4382

        //DnaQuality #4434
        //Identifier
        stepName = "Create dna quality";
        boolean includeCdmBaseAttributes = true;
        tableName = "DnaQuality";
        String[] columnNames = new String[]{"purificationmethod","concentration","ratioofabsorbance260_230", "ratioofabsorbance260_280","qualitycheckdate","concentrationunit_id","qualityterm_id"};
        String[] columnTypes = new String[]{"string_255","double","double","double","datetime","int","int"};
        String[] referencedTables = new String[]{null,null,null,null,null,"DefinedTermBase","DefinedTermBase"};
        step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        stepList.add(step);

        //DnaQuality in TissueSample
        //TODO H2 / PostGreSQL / SQL Server
        stepName = "Add foreign key to dna quality";
        tableName = "SpecimenOrObservationBase";
        newColumnName = "dnaQuality_id";
        boolean notNull = false;
        String referencedTable = "DnaQuality";
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);
        stepList.add(step);

        //time scope for classifications
        //TODO H2 / PostGreSQL / SQL Server
        stepName = "Add time scope (start) for classifications";
        tableName = "Classification";
        newColumnName = "timeperiod_start";
        int length = 255;
        step = ColumnAdder.NewStringInstance(stepName, tableName, newColumnName, length, INCLUDE_AUDIT);
        stepList.add(step);

        //TODO H2 / PostGreSQL / SQL Server
        stepName = "Add time scope (end) for classifications";
        tableName = "Classification";
        newColumnName = "timeperiod_end";
        length = 255;
        step = ColumnAdder.NewStringInstance(stepName, tableName, newColumnName, length, INCLUDE_AUDIT);
        stepList.add(step);

        //TODO H2 / PostGreSQL / SQL Server
        stepName = "Add time scope (freetext) for classifications";
        tableName = "Classification";
        newColumnName = "timeperiod_freetext";
        length = 255;
        step = ColumnAdder.NewStringInstance(stepName, tableName, newColumnName, length, INCLUDE_AUDIT);
        stepList.add(step);

        //Classification_GeoScope
        stepName = "Create Classification_GeoScope table";
        includeCdmBaseAttributes = false;
        tableName = "Classification_GeoScope";
        columnNames = new String[]{"Classification_id","geoScopes_id"};
        columnTypes = new String[]{"int","int"};
        referencedTables = new String[]{"Classification","DefinedTermBase"};
        TableCreator creator = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        creator.setPrimaryKeyParams("Classification_id,geoScopes_id", "REV,Classification_id,geoScopes_id");
        stepList.add(creator);

        //Classification_Description
        stepName = "Create Classification_Description table";
        includeCdmBaseAttributes = false;
        tableName = "Classification_Description";
        columnNames = new String[]{"Classification_id","description_id","description_mapkey_id"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"Classification","LanguageString","DefinedTermBase"};
        creator = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        creator.setPrimaryKeyParams("Classification_id", "REV,Classification_id,description_id,description_mapkey_id");
        stepList.add(creator);

        //Primer.sequence type  #4139
        stepName = "Add sequence string column to primer";
        tableName = "Primer";
        newColumnName = "sequence_string";
        step = ColumnAdder.NewClobInstance(stepName, tableName, newColumnName,
                INCLUDE_AUDIT);
        stepList.add(step);

        //Primer.sequence length #4139
        stepName = "Add sequence length column to primer";
        tableName = "Primer";
        newColumnName = "sequence_length";
        notNull = false;
        referencedTable = null;
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, null, notNull);
        stepList.add(step);

        //EntityValidation
        stepName = "Create EntityValidation table";
        includeCdmBaseAttributes = true;
        tableName = "EntityValidation";
        columnNames = new String[]{"crudeventtype","userfriendlydescription","userfriendlytypename",
                "validatedentityclass","validatedentityid","validatedentityuuid"};
        columnTypes = new String[]{"string_255","string_255","string_255","string_255","int","string_36"};
        referencedTables = new String[]{null,null,null,null,null,null};
        creator = TableCreator.NewNonVersionableInstance(stepName, tableName, columnNames, columnTypes, referencedTables);
        stepList.add(creator);

        //EntityConstraintViolation
        stepName = "Create EntityConstraintViolation table";
        includeCdmBaseAttributes = true;
        tableName = "EntityConstraintViolation";
        columnNames = new String[]{"invalidvalue","message","propertypath","severity","userfriendlyfieldname",
                "validator","entityvalidationresult_id"};
        columnTypes = new String[]{"string_255","string_255","string_255","string_255","string_255","string_255","int"};
        referencedTables = new String[]{null,null,null,null,null,null,"EntityValidation"};
        creator = TableCreator.NewNonVersionableInstance(stepName, tableName, columnNames, columnTypes, referencedTables);
        stepList.add(creator);

        //make OriginalSourceBase.sourceType allow NULL
        stepName = "Remove NOT NULL from sourceType";
        tableName = "OriginalSourceBase_AUD";
        oldColumnName = "sourceType";
//		query = "ALTER TABLE OriginalSourceBase_AUD	" +
//				" CHANGE COLUMN sourceType sourceType VARCHAR(4) NULL ";
        step = ColumnTypeChanger.NewStringSizeInstance(stepName, tableName, oldColumnName, 4, ! INCLUDE_AUDIT);
        stepList.add(step);


        //remove sequence_id column  //we do not move data as we do not expect data available yet #4139
        //we put this to the end as it seems to fail with INNODB
        stepName = "Remove sequence_id column from primer";
        tableName = "Primer";
        oldColumnName = "sequence_id";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, INCLUDE_AUDIT);
        stepList.add(step);


//	WE REMOVED THIS FROM THE SCRIPT BECAUSE IT FAILS WITH INNODB
//		//change size of AgentBase_contact_urls.contact_urls_element  #3920
//		stepName = "Change length of AgentBase_contact_urls.contact_urls_element";
//		tableName = "AgentBase_contact_urls";
//		columnName = "contact_urls_element";
//		step = ColumnTypeChanger.NewStringSizeInstance(stepName, tableName,
//				columnName, 330, INCLUDE_AUDIT);
////		stepList.add(step);


        return stepList;

    }

    private void addIdentifierTables(List<ISchemaUpdaterStep> stepList) {

        //Identifier
        String stepName = "Create Identifier table";
        boolean includeCdmBaseAttributes = true;
        String tableName = "Identifier";
        String[] columnNames = new String[]{"identifier","identifiedObj_type", "identifiedObj_id","type_id"};
        String[] columnTypes = new String[]{"string_800","string_255","int","int"};
        String[] referencedTables = new String[]{null,null,null,"DefinedTermBase"};
        TableCreator step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        stepList.add(step);

        //AgentBase_Identifier
        stepName = "Create AgentBase_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "AgentBase_Identifier";
        columnNames = new String[]{"AgentBase_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"AgentBase","Identifier",null};
        step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("AgentBase_id,identifiers_id", "REV,AgentBase_id,identifiers_id");
        stepList.add(step);

        //Classification_Identifier
        stepName = "Create Classification_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "Classification_Identifier";
        columnNames = new String[]{"Classification_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"Classification","Identifier",null};
        step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("Classification_id,identifiers_id", "REV,Classification_id,identifiers_id");
        stepList.add(step);

        //Collection_Identifier
        stepName = "Create Collection_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "Collection_Identifier";
        columnNames = new String[]{"Collection_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"Collection","Identifier",null};
        step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("Collection_id,identifiers_id", "REV,Collection_id,identifiers_id");
        stepList.add(step);

        //DefinedTermBase_Identifier
        stepName = "Create DefinedTermBase_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "DefinedTermBase_Identifier";
        columnNames = new String[]{"DefinedTermBase_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"DefinedTermBase","Identifier",null};
        step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("DefinedTermBase_id,identifiers_id", "REV,DefinedTermBase_id,identifiers_id");
        stepList.add(step);

        //DescriptionBase_Identifier
        stepName = "Create DescriptionBase_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "DescriptionBase_Identifier";
        columnNames = new String[]{"DescriptionBase_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"DescriptionBase","Identifier",null};
        step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("DescriptionBase_id,identifiers_id", "REV,DescriptionBase_id,identifiers_id");
        stepList.add(step);

        //FeatureTree_Identifier
        stepName = "Create FeatureTree_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "FeatureTree_Identifier";
        columnNames = new String[]{"FeatureTree_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"FeatureTree","Identifier",null};
        step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("FeatureTree_id,identifiers_id", "REV,FeatureTree_id,identifiers_id");
        stepList.add(step);

        //Media_Identifier
        stepName = "Create Media_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "Media_Identifier";
        columnNames = new String[]{"Media_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"Media","Identifier",null};
        step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("Media_id,identifiers_id", "REV,Media_id,identifiers_id");
        stepList.add(step);

        //PolytomousKey_Identifier
        stepName = "Create PolytomousKey_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "PolytomousKey_Identifier";
        columnNames = new String[]{"PolytomousKey_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"PolytomousKey","Identifier",null};
        step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("PolytomousKey_id,identifiers_id", "REV,PolytomousKey_id,identifiers_id");
        stepList.add(step);

        //Reference_Identifier
        stepName = "Create Reference_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "Reference_Identifier";
        columnNames = new String[]{"Reference_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"Reference","Identifier",null};
        step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("Reference_id,identifiers_id", "REV,Reference_id,identifiers_id");
        stepList.add(step);

        //SpecimenOrObservationBase_Identifier
        stepName = "Create SpecimenOrObservationBase_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "SpecimenOrObservationBase_Identifier";
        columnNames = new String[]{"SpecimenOrObservationBase_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"SpecimenOrObservationBase","Identifier",null};
        step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("SpecimenOrObservationBase_id,identifiers_id", "REV,SpecimenOrObservationBase_id,identifiers_id");
        stepList.add(step);

        //TaxonBase_Identifier
        stepName = "Create TaxonBase_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "TaxonBase_Identifier";
        columnNames = new String[]{"TaxonBase_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"TaxonBase","Identifier",null};
        step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("TaxonBase_id,identifiers_id", "REV,TaxonBase_id,identifiers_id");
        stepList.add(step);

        //TaxonNameBase_Identifier
        stepName = "Create TaxonNameBase_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "TaxonNameBase_Identifier";
        columnNames = new String[]{"TaxonNameBase_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"TaxonNameBase","Identifier",null};
        step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("TaxonNameBase_id,identifiers_id", "REV,TaxonNameBase_id,identifiers_id");
        stepList.add(step);

        //TermVocabulary_Identifier
        stepName = "Create TermVocabulary_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "TermVocabulary_Identifier";
        columnNames = new String[]{"TermVocabulary_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"TermVocabulary","Identifier",null};
        step = TableCreator.NewInstance(stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("TermVocabulary_id,identifiers_id", "REV,TermVocabulary_id,identifiers_id");
        stepList.add(step);

    }

    @Override
    public ISchemaUpdater getNextUpdater() {
        return SchemaUpdater_34_341.NewInstance();
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_33_331.NewInstance();
    }

}
