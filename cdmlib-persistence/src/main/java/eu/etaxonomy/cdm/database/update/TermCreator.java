/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * Creates a defined term in a given vocabulary with a default language representation.
 *
 * Note: this should be handled with care as it is still kind of under construction.
 *
 * @author muellera
 * @since 02.06.2024
 */
public class TermCreator extends SchemaUpdaterStepBase{

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

// **************************** STATIC METHODS ********************************/

    public static final TermCreator NewTermInstance(List<ISchemaUpdaterStep> stepList,
            UUID uuidTerm, UUID uuidVocabulary,
            String description,  String label, String abbrev, boolean isOrdered,
            String idInVocabulary, String symbol1, String symbol2,
            Class<?> termclass, TermType termType){

        String stepName = makeStepName(label);
        return new TermCreator(stepList, stepName, uuidTerm, uuidVocabulary,
                description, label, abbrev, isOrdered,
                idInVocabulary, symbol1, symbol2,
                termclass, termType);
    }

// *************************** VARIABLES *****************************************/

    private UUID uuidVocabulary;
    private UUID uuidTerm;
    private String description;
    private String label;
    private String abbrev;
    private Class<?> termClass;
    private TermType termType;
    private String idInVocabulary;
    private String symbol1;
    private String symbol2;
    private String titleCache;

// ***************************** CONSTRUCTOR ***************************************/

    private TermCreator(List<ISchemaUpdaterStep> stepList, String stepName,
            UUID uuidTerm, UUID uuidVocabulary, String description, String label, String abbrev,
            boolean isOrdered, String idInVocabulary, String symbol1, String symbol2,
            Class<?> termClass, TermType termType) {

        super(stepList, stepName);
        this.uuidTerm = uuidTerm;
        this.uuidVocabulary = uuidVocabulary;
        this.description = description;
        this.abbrev = abbrev;
        this.label = label;
        this.idInVocabulary = idInVocabulary;
        this.symbol1 = symbol1;
        this.symbol2 = symbol2;
        this.termClass = termClass;
        this.termType = termType;
    }

// ******************************* METHODS *************************************************/

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {
        ResultSet rs;

        //check if term exists
        String sqlCheckTermExists = " SELECT count(*) as n FROM @@DefinedTermBase@@ WHERE uuid = '" + uuidTerm + "'";
        Long n = (Long)datasource.getSingleValue(caseType.replaceTableNames(sqlCheckTermExists));
        if (n != 0){
            String message = "Term exists already: " + label + "(" + uuidTerm + ")";
            monitor.warning(message);
            result.addWarning(message, this, "invoke");
            return;
        }

        //termId
        Integer termId;
        String sqlMaxId = " SELECT max(id)+1 as maxId FROM " + caseType.transformTo("DefinedTermBase");
        rs = datasource.executeQuery(sqlMaxId);
        if (rs.next()){
            termId = rs.getInt("maxId");
        }else{
            String message = "No vocabularies do exist yet. Can't create vocabulary!";
            monitor.warning(message);
            result.addError(message, this, "invoke");
            return;
        }

        //vocId
        String sqlVocId = " SELECT id FROM @@TermCollection@@ WHERE uuid = '"+uuidVocabulary+"'";
        Integer vocId = (Integer)datasource.getSingleValue(caseType.replaceTableNames(sqlVocId));

        String id = Integer.toString(termId);
        String created = getNowString();
        String dtype = termClass.getSimpleName();
        String titleCache = this.titleCache != null ? this.titleCache :
            (StringUtils.isNotBlank(label))? label : ( (StringUtils.isNotBlank(abbrev))? abbrev : description );
        String protectedTitleCache = getBoolean(this.titleCache != null, datasource);
        String termSourceUri = null; //termClass.getCanonicalName();
        String sqlInsertTerm = " INSERT INTO @@DefinedTermBase@@ (DTYPE, id, uuid, created, protectedtitlecache, "
                  + "titleCache, uri, termType, vocabulary_id, idInVocabulary, symbol, symbol2, availableFor)" +
                "VALUES ('" + dtype + "', " + id + ", '" + uuidTerm + "', '" + created + "', " + protectedTitleCache +
                  ", " + nullSafeString(titleCache) + "," +nullSafeString(termSourceUri) + ",'" + termType.getKey() + "'," + vocId +
                  "," + nullSafeString(idInVocabulary) +"," + nullSafeString(symbol1) + "," + nullSafeString(symbol2) +
                  "," +  nullSafeString("#") + ")";
        datasource.executeUpdate(caseType.replaceTableNames(sqlInsertTerm));

        //language id
        int langId;
        String uuidLanguage = Language.uuidEnglish.toString();
        String sqlLangId = " SELECT id FROM @@DefinedTermBase@@ WHERE uuid = '" + uuidLanguage + "'";
        rs = datasource.executeQuery(caseType.replaceTableNames(sqlLangId));
        if (rs.next()){
            langId = rs.getInt("id");
        }else{
            String message = "Term for default language (English) not  does not exist! Can't add representation.";
            monitor.warning(message);
            result.addError(message, this, "invoke");
            return;
        }

        //representation
        int repId;
        sqlMaxId = " SELECT max(id)+1 as maxId FROM " + caseType.transformTo("Representation");
        rs = datasource.executeQuery(sqlMaxId);
        if (rs.next()){
            repId = rs.getInt("maxId");
        }else{
            String message = "No representations do exist yet. Can't add representation!";
            monitor.warning(message);
            result.addError(message, this, "invoke");
            return;
        }

        UUID uuidRepresentation = UUID.randomUUID();
        String sqlInsertRepresentation = " INSERT INTO @@Representation@@ (id, created, uuid, text, label, abbreviatedlabel, language_id) " +
                "VALUES (" + repId + ", '" + created + "', '" + uuidRepresentation + "'," + nullSafeString(description) +  ", '" + label +  "'," + nullSafeString(abbrev) +  ", " + langId + ")";

        datasource.executeUpdate(caseType.replaceTableNames(sqlInsertRepresentation));

        //Vocabulary_representation
        String sqlInsertMN = "INSERT INTO @@DefinedTermBase_Representation@@ (DefinedTermBase_id, representations_id) " +
                " VALUES ("+ termId +"," +repId+ " )";

        datasource.executeUpdate(caseType.replaceTableNames(sqlInsertMN));

        return;
    }

    private String nullSafeString(String abbrev) {
        if (abbrev == null){
            return "NULL";
        }else{
            return "'" + abbrev + "'";
        }
    }

    private static String makeStepName(String label) {
        String stepName = "Create new term '"+ label + "'";
        return stepName;
    }
}