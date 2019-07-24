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
import eu.etaxonomy.cdm.database.update.IndexAdder;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.database.update.TableDroper;
import eu.etaxonomy.cdm.database.update.v31_33.SchemaUpdater_33_331;

/**
 * @author a.mueller
 * @since Jan 14, 2014
 * @see IndexAdder
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
        String columnName;
        String newColumnName;
        String oldColumnName;

        List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //TODO H2 / PostGreSQL / SQL Server
        //UserAccount unique
        stepName = "Update User unique indexes";
        tableName = "UserAccount";
        columnName = "username";
        UsernameConstraintUpdater.NewInstance(stepList, stepName, tableName, columnName);

        //TODO H2 / PostGreSQL / SQL Server
        //PermissionGroup unique
        stepName = "Update Group unique indexes";
        tableName = "PermissionGroup";
        columnName = "name";
        UsernameConstraintUpdater.NewInstance(stepList, stepName, tableName, columnName);

        //TODO H2 / PostGreSQL / SQL Server
        //GrantedAuthority unique
        stepName = "Update User unique indexes";
        tableName = "GrantedAuthorityImpl";
        columnName = "authority";
        UsernameConstraintUpdater.NewInstance(stepList, stepName, tableName, columnName);

        //TODO H2 / PostGreSQL / SQL Server
        stepName = "Add label column to derived unit";
        tableName = "SpecimenOrObservationBase";
        columnName = "originalLabelInfo";
        ColumnAdder.NewClobInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        //TODO test with data and H2 / PostGreSQL / SQL Server
        //set default value to true where required
        stepName = "Set publish to true if null";
        String query = " UPDATE @@TaxonBase@@ " +
                    " SET publish = @TRUE@ " +
                    " WHERE DTYPE IN ('Synonym') AND publish IS NULL ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, "TaxonBase", 99);

        addIdentifierTables(stepList);


        //remove series from Reference  #4293
        stepName = "Copy series to series part";
        String sql = " UPDATE Reference r " +
                " SET r.seriespart = r.series " +
                " WHERE r.series is NOT NULL AND r.seriesPart IS NULL ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, "Reference", 99);

        stepName = "Set series to NULL";
        sql = " UPDATE Reference r " +
                " SET r.series = NULL " +
                " WHERE r.series = r.seriesPart ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, "Reference", 99);

        //TODO check all series are null

        stepName = "Remove series column";
        tableName = "Reference";
        oldColumnName = "series";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //authorTeam -> authorship
        stepName = "Rename Reference.authorTeam column";
        tableName = "Reference";
        oldColumnName = "authorTeam_id";
        newColumnName = "authorship_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //remove CDM_VIEW #4316
        stepName = "Remove CDM_VIEW_CDM_VIEW table";
        tableName = "CDM_VIEW_CDM_VIEW";
        boolean ifExists = true;
        TableDroper.NewInstance(stepList, stepName, tableName, ! INCLUDE_AUDIT, ifExists);

        stepName = "Remove CDM_VIEW table";
        tableName = "CDM_VIEW";
        ifExists = true;
        TableDroper.NewInstance(stepList, stepName, tableName, ! INCLUDE_AUDIT, ifExists);

        //TODO not null on username, groupname and authority  #4382

        //DnaQuality #4434
        //Identifier
        stepName = "Create dna quality";
        boolean includeCdmBaseAttributes = true;
        tableName = "DnaQuality";
        String[] columnNames = new String[]{"purificationmethod","concentration","ratioofabsorbance260_230", "ratioofabsorbance260_280","qualitycheckdate","concentrationunit_id","qualityterm_id"};
        String[] columnTypes = new String[]{"string_255","double","double","double","datetime","int","int"};
        String[] referencedTables = new String[]{null,null,null,null,null,"DefinedTermBase","DefinedTermBase"};
        TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);

        //DnaQuality in TissueSample
        //TODO H2 / PostGreSQL / SQL Server
        stepName = "Add foreign key to dna quality";
        tableName = "SpecimenOrObservationBase";
        newColumnName = "dnaQuality_id";
        boolean notNull = false;
        String referencedTable = "DnaQuality";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, notNull, referencedTable);

        //time scope for classifications
        //TODO H2 / PostGreSQL / SQL Server
        stepName = "Add time scope (start) for classifications";
        tableName = "Classification";
        newColumnName = "timeperiod_start";
        int length = 255;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

        //TODO H2 / PostGreSQL / SQL Server
        stepName = "Add time scope (end) for classifications";
        tableName = "Classification";
        newColumnName = "timeperiod_end";
        length = 255;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

        //TODO H2 / PostGreSQL / SQL Server
        stepName = "Add time scope (freetext) for classifications";
        tableName = "Classification";
        newColumnName = "timeperiod_freetext";
        length = 255;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

        //Classification_GeoScope
        stepName = "Create Classification_GeoScope table";
        includeCdmBaseAttributes = false;
        tableName = "Classification_GeoScope";
        columnNames = new String[]{"Classification_id","geoScopes_id"};
        columnTypes = new String[]{"int","int"};
        referencedTables = new String[]{"Classification","DefinedTermBase"};
        TableCreator creator = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        creator.setPrimaryKeyParams("Classification_id,geoScopes_id", "REV,Classification_id,geoScopes_id");

        //Classification_Description
        stepName = "Create Classification_Description table";
        includeCdmBaseAttributes = false;
        tableName = "Classification_Description";
        columnNames = new String[]{"Classification_id","description_id","description_mapkey_id"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"Classification","LanguageString","DefinedTermBase"};
        creator = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        creator.setPrimaryKeyParams("Classification_id", "REV,Classification_id,description_id,description_mapkey_id");

        //Primer.sequence type  #4139
        stepName = "Add sequence string column to primer";
        tableName = "Primer";
        newColumnName = "sequence_string";
        ColumnAdder.NewClobInstance(stepList, stepName, tableName, newColumnName,
                INCLUDE_AUDIT);

        //Primer.sequence length #4139
        stepName = "Add sequence length column to primer";
        tableName = "Primer";
        newColumnName = "sequence_length";
        notNull = false;
        referencedTable = null;
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, null, notNull);

        //EntityValidation
        stepName = "Create EntityValidation table";
        includeCdmBaseAttributes = true;
        tableName = "EntityValidation";
        columnNames = new String[]{"crudeventtype","userfriendlydescription","userfriendlytypename",
                "validatedentityclass","validatedentityid","validatedentityuuid"};
        columnTypes = new String[]{"string_255","string_255","string_255","string_255","int","string_36"};
        referencedTables = new String[]{null,null,null,null,null,null};
        creator = TableCreator.NewNonVersionableInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables);

        //EntityConstraintViolation
        stepName = "Create EntityConstraintViolation table";
        includeCdmBaseAttributes = true;
        tableName = "EntityConstraintViolation";
        columnNames = new String[]{"invalidvalue","message","propertypath","severity","userfriendlyfieldname",
                "validator","entityvalidationresult_id"};
        columnTypes = new String[]{"string_255","string_255","string_255","string_255","string_255","string_255","int"};
        referencedTables = new String[]{null,null,null,null,null,null,"EntityValidation"};
        creator = TableCreator.NewNonVersionableInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables);

        //make OriginalSourceBase.sourceType allow NULL
        stepName = "Remove NOT NULL from sourceType";
        tableName = "OriginalSourceBase_AUD";
        oldColumnName = "sourceType";
//		query = "ALTER TABLE OriginalSourceBase_AUD	" +
//				" CHANGE COLUMN sourceType sourceType VARCHAR(4) NULL ";
        ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName, oldColumnName, 4, ! INCLUDE_AUDIT);


        //remove sequence_id column  //we do not move data as we do not expect data available yet #4139
        //we put this to the end as it seems to fail with INNODB
        stepName = "Remove sequence_id column from primer";
        tableName = "Primer";
        oldColumnName = "sequence_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);


//	WE REMOVED THIS FROM THE SCRIPT BECAUSE IT FAILS WITH INNODB
//		//change size of AgentBase_contact_urls.contact_urls_element  #3920
//		stepName = "Change length of AgentBase_contact_urls.contact_urls_element";
//		tableName = "AgentBase_contact_urls";
//		columnName = "contact_urls_element";
//		ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName,
//				columnName, 330, INCLUDE_AUDIT);

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
        TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);

        //AgentBase_Identifier
        stepName = "Create AgentBase_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "AgentBase_Identifier";
        columnNames = new String[]{"AgentBase_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"AgentBase","Identifier",null};
        TableCreator step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("AgentBase_id,identifiers_id", "REV,AgentBase_id,identifiers_id");

        //Classification_Identifier
        stepName = "Create Classification_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "Classification_Identifier";
        columnNames = new String[]{"Classification_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"Classification","Identifier",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("Classification_id,identifiers_id", "REV,Classification_id,identifiers_id");

        //Collection_Identifier
        stepName = "Create Collection_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "Collection_Identifier";
        columnNames = new String[]{"Collection_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"Collection","Identifier",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("Collection_id,identifiers_id", "REV,Collection_id,identifiers_id");

        //DefinedTermBase_Identifier
        stepName = "Create DefinedTermBase_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "DefinedTermBase_Identifier";
        columnNames = new String[]{"DefinedTermBase_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"DefinedTermBase","Identifier",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("DefinedTermBase_id,identifiers_id", "REV,DefinedTermBase_id,identifiers_id");

        //DescriptionBase_Identifier
        stepName = "Create DescriptionBase_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "DescriptionBase_Identifier";
        columnNames = new String[]{"DescriptionBase_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"DescriptionBase","Identifier",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("DescriptionBase_id,identifiers_id", "REV,DescriptionBase_id,identifiers_id");

        //FeatureTree_Identifier
        stepName = "Create FeatureTree_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "FeatureTree_Identifier";
        columnNames = new String[]{"FeatureTree_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"FeatureTree","Identifier",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("FeatureTree_id,identifiers_id", "REV,FeatureTree_id,identifiers_id");

        //Media_Identifier
        stepName = "Create Media_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "Media_Identifier";
        columnNames = new String[]{"Media_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"Media","Identifier",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("Media_id,identifiers_id", "REV,Media_id,identifiers_id");

        //PolytomousKey_Identifier
        stepName = "Create PolytomousKey_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "PolytomousKey_Identifier";
        columnNames = new String[]{"PolytomousKey_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"PolytomousKey","Identifier",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("PolytomousKey_id,identifiers_id", "REV,PolytomousKey_id,identifiers_id");

        //Reference_Identifier
        stepName = "Create Reference_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "Reference_Identifier";
        columnNames = new String[]{"Reference_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"Reference","Identifier",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("Reference_id,identifiers_id", "REV,Reference_id,identifiers_id");

        //SpecimenOrObservationBase_Identifier
        stepName = "Create SpecimenOrObservationBase_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "SpecimenOrObservationBase_Identifier";
        columnNames = new String[]{"SpecimenOrObservationBase_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"SpecimenOrObservationBase","Identifier",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("SpecimenOrObservationBase_id,identifiers_id", "REV,SpecimenOrObservationBase_id,identifiers_id");

        //TaxonBase_Identifier
        stepName = "Create TaxonBase_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "TaxonBase_Identifier";
        columnNames = new String[]{"TaxonBase_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"TaxonBase","Identifier",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("TaxonBase_id,identifiers_id", "REV,TaxonBase_id,identifiers_id");

        //TaxonNameBase_Identifier
        stepName = "Create TaxonNameBase_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "TaxonNameBase_Identifier";
        columnNames = new String[]{"TaxonNameBase_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"TaxonNameBase","Identifier",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("TaxonNameBase_id,identifiers_id", "REV,TaxonNameBase_id,identifiers_id");

        //TermVocabulary_Identifier
        stepName = "Create TermVocabulary_Identifier table";
        includeCdmBaseAttributes = false;
        tableName = "TermVocabulary_Identifier";
        columnNames = new String[]{"TermVocabulary_id","identifiers_id","sortIndex"};
        columnTypes = new String[]{"int","int","int"};
        referencedTables = new String[]{"TermVocabulary","Identifier",null};
        step = TableCreator.NewInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes);
        step.setPrimaryKeyParams("TermVocabulary_id,identifiers_id", "REV,TermVocabulary_id,identifiers_id");

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
