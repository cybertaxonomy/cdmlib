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
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.RankClass;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * Creates a new term if a term with the same given uuid does not exist yet
 * @author a.mueller
 * @since 10.09.2010
 */
public class SingleTermUpdater extends SchemaUpdaterStepBase {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

// **************************** FACTORY METHODS ********************************/

	public static final SingleTermUpdater NewInstance(List<ISchemaUpdaterStep> stepList, String stepName, TermType termType, UUID uuidTerm, String idInVocabulary, String symbol,
	        String description,  String label, String abbrev, String dtype, UUID uuidVocabulary, UUID uuidLanguage, boolean isOrdered, UUID uuidAfterTerm){
		return new SingleTermUpdater(stepList, stepName, termType, uuidTerm, idInVocabulary, symbol,
		        description, label, abbrev, null, null, null, dtype, uuidVocabulary, uuidLanguage, isOrdered, uuidAfterTerm);
	}

    public static final SingleTermUpdater NewReverseInstance(List<ISchemaUpdaterStep> stepList, String stepName, TermType termType, UUID uuidTerm, String idInVocabulary, String symbol,
           String description,  String label, String abbrev, String reverseDescription, String reverseLabel, String reverseAbbrev,
           String dtype, UUID uuidVocabulary, UUID uuidLanguage, boolean isOrdered, UUID uuidAfterTerm){
        return new SingleTermUpdater(stepList, stepName, termType, uuidTerm, idInVocabulary,symbol,
                description, label, abbrev, reverseDescription, reverseLabel, reverseAbbrev,
                dtype, uuidVocabulary, uuidLanguage, isOrdered, uuidAfterTerm);
    }

// *************************** VARIABLES *****************************************/

	private final UUID uuidTerm ;
	private final String description;
	private final String label;
	private final String abbrev;
	private final String dtype;
	private final UUID uuidVocabulary;
	private final boolean isOrdered;
	private final UUID uuidAfterTerm;
	private final UUID uuidLanguage;
	private String reverseDescription;
	private String reverseLabel;
	private String reverseAbbrev;
	private RankClass rankClass;
	private final TermType termType;
	private final String idInVocabulary;
	private boolean symmetric = false;
	private boolean transitive = false;
	private String symbol;

// ***************************** CONSTRUCTOR ***************************************/

	private SingleTermUpdater(List<ISchemaUpdaterStep> stepList, String stepName, TermType termType, UUID uuidTerm, String idInVocabulary, String symbol,
	        String description, String label, String abbrev, String reverseDescription, String reverseLabel, String reverseAbbrev,
	        String dtype, UUID uuidVocabulary, UUID uuidLanguage, boolean isOrdered, UUID uuidAfterTerm) {
		super(stepList, stepName);
		this.termType = termType;
		this.idInVocabulary = idInVocabulary;
		this.symbol = symbol;
		this.abbrev = abbrev;
		this.description = description;
		this.dtype = dtype;
		this.label = label;
		this.isOrdered = isOrdered;
		this.uuidTerm = uuidTerm;
		this.uuidVocabulary = uuidVocabulary;
		this.uuidAfterTerm = uuidAfterTerm;
		this.uuidLanguage = uuidLanguage;
		this.reverseDescription = reverseDescription;
		this.reverseLabel = reverseLabel;
		this.reverseAbbrev = reverseAbbrev;
	}

// ******************************* METHODS *************************************************/

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {
		String sqlCheckTermExists = " SELECT count(*) as n " +
 				" FROM " + caseType.transformTo("DefinedTermBase") +
 				" WHERE uuid = '" + uuidTerm + "'";
		Long n = (Long)datasource.getSingleValue(sqlCheckTermExists);
		if (n != 0){
		    String message ="Term already exists: " + label + "(" + uuidTerm + ")";
			monitor.warning(message);
			result.addWarning(message, (String)null, getStepName());
			return;
		}

		//vocabulary id
		int vocId;
		String sqlVocId = " SELECT id " +
				" FROM  " + caseType.transformTo("TermVocabulary") +
				" WHERE uuid = '" + uuidVocabulary + "'";
		ResultSet rs = datasource.executeQuery(sqlVocId);
		if (rs.next()){
			vocId = rs.getInt("id");
		}else{
			String message = "Vocabulary ( "+ uuidVocabulary +" ) for term does not exist!";
			monitor.warning(message);
			result.addError(message, getStepName() + ", SingleTermUpdater.invoke");
            return;
		}

		String sqlMaxId;
        Integer termId = getMaxId(datasource, monitor, caseType, result);
        if (termId == null){
            return;
        }

		String id = Integer.toString(termId);
		String created = getNowString();
		String defaultColor = "null";
		String protectedTitleCache = getBoolean(false, datasource);
		String orderIndex;
		if (isOrdered){
			orderIndex = getOrderIndex(datasource, vocId, monitor, caseType);
		}else{
			orderIndex = "null";
		}
		String titleCache = label != null ? label : (abbrev != null ? abbrev : description );
		String idInVocStr = idInVocabulary == null ? "NULL" : "'" + idInVocabulary + "'";
		String symbol = this.symbol == null ? "NULL" : "'" + this.symbol + "'";
		String sqlInsertTerm = " INSERT INTO @@DefinedTermBase@@ (DTYPE, id, uuid, created, termtype, idInVocabulary, symbol, protectedtitlecache, titleCache, orderindex, defaultcolor, vocabulary_id)" +
				"VALUES ('" + dtype + "', " + id + ", '" + uuidTerm + "', '" + created + "', '" + termType.getKey() + "', " + idInVocStr + ", " + symbol +  ", " + protectedTitleCache + ", '" + titleCache + "', " + orderIndex + ", " + defaultColor + ", " + vocId + ")";
		sqlInsertTerm = caseType.replaceTableNames(sqlInsertTerm);
		datasource.executeUpdate(caseType.replaceTableNames(sqlInsertTerm));

		updateFeatureTerms(termId, datasource, monitor, caseType);
		updateRelationshipTerms(termId, datasource, monitor, caseType);
		updateRanks(termId, datasource, monitor, caseType);

		//language id
		Integer langId = getLanguageId(uuidLanguage, datasource, monitor, caseType);
		if (langId == null){
			String message = "LangId is null";
			result.addWarning(message);
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

		//standard representation
		UUID uuidRepresentation = UUID.randomUUID();
		String sqlInsertRepresentation = " INSERT INTO @@Representation@@ (id, created, uuid, text, label, abbreviatedlabel, language_id) " +
				"VALUES (" + repId + ", '" + created + "', '" + uuidRepresentation + "', " + nullSafeStr(description) +  ", " +nullSafeStr( label) +  ", " + nullSafeStr(abbrev) +  ", " + langId + ")";

		datasource.executeUpdate(caseType.replaceTableNames(sqlInsertRepresentation));

		String sqlInsertMN = "INSERT INTO @@DefinedTermBase_Representation@@ (DefinedTermBase_id, representations_id) " +
				" VALUES ("+ termId +"," +repId+ " )";

		datasource.executeUpdate(caseType.replaceTableNames(sqlInsertMN));

		//reverse representation
		if (hasReverseRepresentation()){
			int reverseRepId = repId + 1;
			UUID uuidReverseRepresentation = UUID.randomUUID();
			String sqlInsertReverseRepresentation = " INSERT INTO @@Representation@@ (id, created, uuid, text, label, abbreviatedlabel, language_id) " +
					"VALUES (" + reverseRepId + ", '" + created + "', '" + uuidReverseRepresentation + "', " + nullSafeStr(reverseDescription) +  ", " + nullSafeStr(reverseLabel) +  ",  " + nullSafeStr(reverseAbbrev) +  ", " + langId + ")";

			datasource.executeUpdate(caseType.replaceTableNames(sqlInsertReverseRepresentation));

			String sqlReverseInsertMN = "INSERT INTO @@TermBase_inverseRepresentation@@ (term_id, inverserepresentations_id) " +
					" VALUES ("+ termId +"," +reverseRepId+ " )";

			datasource.executeUpdate(caseType.replaceTableNames(sqlReverseInsertMN));
		}

		return;
	}

    /**
     * @param datasource
     * @param monitor
     * @param caseType
     * @param result
     * @return
     * @throws SQLException
     */
    protected Integer getMaxId(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {

//       For some very strange reason max(id) gave back a wrong result when
//        executed here while updateing remote-webapp H2 test database, therefore
//        we took this workaround which worked. Can be removed when it does not
//        appear anymore

//        String sqlMaxId = " SELECT max(id)+1 as maxId"
//                + " FROM " + caseType.transformTo("DefinedTermBase");
        String sqlMaxId = " SELECT id  as maxId"
                + " FROM " + caseType.transformTo("DefinedTermBase") +
                 " ORDER BY id DESC ";
		ResultSet rs = datasource.executeQuery(sqlMaxId);
		while (rs.next()){
		    Integer termId = rs.getInt("maxId");
		    System.out.println(termId);
		    return termId +1;
		}
		String message = "No defined terms do exist yet. Can't update terms!";
		monitor.warning(message);
		result.addError(message, getStepName() + ", SingleTermUpdater.invoke");
        return null;
    }

	private String nullSafeStr(String str) {
		if (str == null){
			return " NULL ";
		}else{
			return "'" + str + "'";
		}
	}

	private void updateFeatureTerms(Integer termId, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		if (dtype.equals(Feature.class.getSimpleName())){
			String sqlUpdate = "UPDATE  " + caseType.transformTo("DefinedTermBase") +
				" SET " +
				" supportscategoricaldata = " + getBoolean(false, datasource) + ", " +
				" supportscommontaxonname = " + getBoolean(false, datasource) + ", " +
				" supportsdistribution = " + getBoolean(false, datasource) + ", " +
				" supportsindividualassociation = " + getBoolean(false, datasource) + ", " +
				" supportsquantitativedata = " + getBoolean(false, datasource) + ", " +
				" supportstaxoninteraction = " + getBoolean(false, datasource) + ", " +
				" supportstextdata = " + getBoolean(true, datasource) +  " " +
				" WHERE id = " + termId;
			datasource.executeUpdate(sqlUpdate);
		}
	}

	private void updateRelationshipTerms(Integer termId, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		if (dtype.contains("Relationship")){
			String sqlUpdate = "UPDATE "  + caseType.transformTo("DefinedTermBase") +
				" SET " +
				" symmetrical = " + getBoolean(symmetric, datasource) + ", " +
				" transitive = " + getBoolean(transitive, datasource) + " " +
				" WHERE id = " + termId;
			datasource.executeUpdate(sqlUpdate);
		}
	}

	private void updateRanks(Integer termId, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		if (dtype.equals(Rank.class.getSimpleName())){
			String sqlUpdate = "UPDATE " + caseType.transformTo("DefinedTermBase") +
				" SET rankClass = '" + rankClass.getKey() + "'" +
				" WHERE id = " + termId;
			datasource.executeUpdate(sqlUpdate);
		}
	}

	public SingleTermUpdater setRankClass(RankClass rankClass) {
		this.rankClass = rankClass;
		return this;
	}



	/**
	 * @param datasource
	 * @param vocId
	 * @param monitor
	 * @param caseType
	 * @return
	 * @throws SQLException
	 */
	private String getOrderIndex(ICdmDataSource datasource, int vocId, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		ResultSet rs;
		Integer intOrderIndex = null;
		if (uuidAfterTerm == null){
			return "1";
		}
		String sqlOrderIndex = " SELECT orderindex FROM %s WHERE uuid = '%s' AND vocabulary_id = %d ";
		sqlOrderIndex = String.format(sqlOrderIndex, caseType.transformTo("DefinedTermBase"), uuidAfterTerm.toString(), vocId);
		rs = datasource.executeQuery(sqlOrderIndex);
		if (rs.next()){
			intOrderIndex = rs.getInt("orderindex") + 1;

			String sqlUpdateLowerTerms = "UPDATE %s SET orderindex = orderindex + 1 WHERE vocabulary_id = %d AND orderindex >= %d ";
			sqlUpdateLowerTerms = String.format(sqlUpdateLowerTerms, caseType.transformTo("DefinedTermBase"), vocId, intOrderIndex );
			datasource.executeUpdate(sqlUpdateLowerTerms);
		}else{
			String warning = "The previous term has not been found in vocabulary. Put term to the end";
			monitor.warning(warning);
		}
		if (intOrderIndex == null){
			String sqlMaxOrderIndex = " SELECT max(orderindex) FROM %s WHERE vocabulary_id = %d";
			sqlMaxOrderIndex = String.format(sqlMaxOrderIndex, caseType.transformTo("DefinedTermBase"), vocId);
			intOrderIndex = (Integer)datasource.getSingleValue(sqlMaxOrderIndex);
			if (intOrderIndex != null){
				intOrderIndex++;
			}else{
				String warning = "No term was found in vocabulary or vocabulary does not exist. Use order index '0'.";
				monitor.warning(warning);
				intOrderIndex =0;
			}
		}

		return intOrderIndex.toString();
	}


	private boolean hasReverseRepresentation() {
		return  reverseLabel != null ||  reverseDescription != null ||  reverseAbbrev != null;
	}

	public SingleTermUpdater setReverseRepresentation(String reverseDescription, String reverseLabel, String reverseAbbrev) {
		this.reverseLabel = reverseLabel;
		this.reverseDescription = reverseDescription;
		this.reverseAbbrev = reverseAbbrev;
		return this;
	}

	public SingleTermUpdater setSymmetricTransitiv(boolean symmetric, boolean transitive){
		this.symmetric = symmetric;
		this.transitive = transitive;
		return this;
	}

}
