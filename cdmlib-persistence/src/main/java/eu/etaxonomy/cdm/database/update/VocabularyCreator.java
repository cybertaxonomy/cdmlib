/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermType;

/**
 * @author a.mueller
 \* @since 10.09.2010
 *
 */
public class VocabularyCreator extends SchemaUpdaterStepBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(VocabularyCreator.class);

// **************************** STATIC METHODS ********************************/

	public static final VocabularyCreator NewVocabularyInstance(UUID uuidVocabulary, String description,  String label, String abbrev, boolean isOrdered, Class<?> termclass, TermType termType){
		String stepName = makeStepName(label);
		return new VocabularyCreator(stepName, uuidVocabulary, description, label, abbrev, isOrdered, termclass, termType);
	}

// *************************** VARIABLES *****************************************/
	private UUID uuidVocabulary;
	private String description;
	private String label;
	private String abbrev;
	private boolean isOrdered;
	private Class<?> termClass;
	private TermType termType;

// ***************************** CONSTRUCTOR ***************************************/

	private VocabularyCreator(String stepName, UUID uuidVocabulary, String description, String label, String abbrev, boolean isOrdered, Class<?> termClass, TermType termType) {
		super(stepName);
		this.uuidVocabulary = uuidVocabulary;
		this.description = description;
		this.abbrev = abbrev;
		this.label = label;
		this.isOrdered = isOrdered;
		this.termClass = termClass;
		this.termType = termType;
	}

// ******************************* METHODS *************************************************/

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {
        ResultSet rs;

		String sqlCheckTermExists = " SELECT count(*) as n FROM @@TermVocabulary@@ WHERE uuid = '" + uuidVocabulary + "'";
		Long n = (Long)datasource.getSingleValue(caseType.replaceTableNames(sqlCheckTermExists));
		if (n != 0){
		    String message = "Vocabulary already exists: " + label + "(" + uuidVocabulary + ")";
			monitor.warning(message);
			result.addError(message, this, "invoke");
			return;
		}


		//vocId
		Integer vocId;
		String sqlMaxId = " SELECT max(id)+1 as maxId FROM " + caseType.transformTo("TermVocabulary");
		rs = datasource.executeQuery(sqlMaxId);
		if (rs.next()){
			vocId = rs.getInt("maxId");
		}else{
			String message = "No vocabularies do exist yet. Can't create vocabulary!";
			monitor.warning(message);
			result.addError(message, this, "invoke");
            return;
		}


		String id = Integer.toString(vocId);
		String created  = getNowString();
		String dtype;
		if (isOrdered){
			dtype = "OrderedTermVocabulary";  //TODO case required here?
		}else{
			dtype = "TermVocabulary";
		}
		String titleCache = (StringUtils.isNotBlank(label))? label : ( (StringUtils.isNotBlank(abbrev))? abbrev : description );
		String protectedTitleCache = getBoolean(false, datasource);
		String termSourceUri = termClass.getCanonicalName();
		String sqlInsertTerm = " INSERT INTO @@TermVocabulary@@ (DTYPE, id, uuid, created, protectedtitlecache, titleCache, termsourceuri, termType)" +
				"VALUES ('" + dtype + "', " + id + ", '" + uuidVocabulary + "', '" + created + "', " + protectedTitleCache + ", '" + titleCache + "', '" + termSourceUri + "', '" + termType.getKey() + "')";
		datasource.executeUpdate(caseType.replaceTableNames(sqlInsertTerm));

		//language id
		int langId;
		String uuidLanguage = Language.uuidEnglish.toString();
		String sqlLangId = " SELECT id FROM @@DefinedTermBase@@ WHERE uuid = '" + uuidLanguage + "'";
		rs = datasource.executeQuery(caseType.replaceTableNames(sqlLangId));
		if (rs.next()){
			langId = rs.getInt("id");
		}else{
			String message = "Term for default language (English) not  does not exist!";
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
			String message = "No representations do exist yet. Can't update terms!";
			monitor.warning(message);
			result.addError(message, this, "invoke");
            return;
		}

		UUID uuidRepresentation = UUID.randomUUID();
		String sqlInsertRepresentation = " INSERT INTO @@Representation@@ (id, created, uuid, text, label, abbreviatedlabel, language_id) " +
				"VALUES (" + repId + ", '" + created + "', '" + uuidRepresentation + "', '" + description +  "', '" + label +  "'," + nullSafeString(abbrev) +  ", " + langId + ")";

		datasource.executeUpdate(caseType.replaceTableNames(sqlInsertRepresentation));

		//Vocabulary_representation
		String sqlInsertMN = "INSERT INTO @@TermVocabulary_Representation@@ (TermVocabulary_id, representations_id) " +
				" VALUES ("+ vocId +"," +repId+ " )";

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
		String stepName = "Create new vocabulary '"+ label + "'";
		return stepName;
	}

}
