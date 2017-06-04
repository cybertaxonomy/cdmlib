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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.RankClass;

/**
 * Creates a new term if a term with the same given uuid does not exist yet
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class SingleTermUpdater extends SchemaUpdaterStepBase implements ITermUpdaterStep{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SingleTermUpdater.class);

	/**
	 * @Deprecated use {@link #NewInstance(String, TermType, UUID, String, String, String, String, UUID, UUID, boolean, UUID)} instead
	 */
	@Deprecated
	public static final SingleTermUpdater NewInstance(String stepName, UUID uuidTerm, String description,  String label, String abbrev, String dtype, UUID uuidVocabulary, UUID uuidLanguage, boolean isOrdered, UUID uuidAfterTerm){
		return new SingleTermUpdater(stepName, null, uuidTerm, null, description, label, abbrev, dtype, uuidVocabulary, uuidLanguage, isOrdered, uuidAfterTerm);
	}

	public static final SingleTermUpdater NewInstance(String stepName, TermType termType, UUID uuidTerm, String idInVocabulary, String description,  String label, String abbrev, String dtype, UUID uuidVocabulary, UUID uuidLanguage, boolean isOrdered, UUID uuidAfterTerm){
		return new SingleTermUpdater(stepName, termType, uuidTerm, idInVocabulary, description, label, abbrev, dtype, uuidVocabulary, uuidLanguage, isOrdered, uuidAfterTerm);
	}


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



	private SingleTermUpdater(String stepName, TermType termType, UUID uuidTerm, String idInVocabulary, String description, String label, String abbrev, String dtype, UUID uuidVocabulary, UUID uuidLanguage, boolean isOrdered, UUID uuidAfterTerm) {
		super(stepName);
		this.termType = termType;
		this.idInVocabulary = idInVocabulary;
		this.abbrev = abbrev;
		this.description = description;
		this.dtype = dtype;
		this.label = label;
		this.isOrdered = isOrdered;
		this.uuidTerm = uuidTerm;
		this.uuidVocabulary = uuidVocabulary;
		this.uuidAfterTerm = uuidAfterTerm;
		this.uuidLanguage = uuidLanguage;
	}

	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException{
 		String sqlCheckTermExists = " SELECT count(*) as n " +
 				" FROM " + caseType.transformTo("DefinedTermBase") +
 				" WHERE uuid = '" + uuidTerm + "'";
		Long n = (Long)datasource.getSingleValue(sqlCheckTermExists);
		if (n != 0){
			monitor.warning("Term already exists: " + label + "(" + uuidTerm + ")");
			return -1;
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
			String warning = "Vocabulary ( "+ uuidVocabulary +" ) for term does not exist!";
			monitor.warning(warning);
			return null;
		}

		Integer termId;
		String sqlMaxId = " SELECT max(id)+1 as maxId FROM " + caseType.transformTo("DefinedTermBase");
		rs = datasource.executeQuery(sqlMaxId);
		if (rs.next()){
			termId = rs.getInt("maxId");
		}else{
			String warning = "No defined terms do exist yet. Can't update terms!";
			monitor.warning(warning);
			return null;
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
		String sqlInsertTerm = " INSERT INTO @@DefinedTermBase@@ (DTYPE, id, uuid, created, termtype, idInVocabulary, protectedtitlecache, titleCache, orderindex, defaultcolor, vocabulary_id)" +
				"VALUES ('" + dtype + "', " + id + ", '" + uuidTerm + "', '" + created + "', '" + termType.getKey() + "', " + idInVocStr +  ", " + protectedTitleCache + ", '" + titleCache + "', " + orderIndex + ", " + defaultColor + ", " + vocId + ")";

		datasource.executeUpdate(caseType.replaceTableNames(sqlInsertTerm));

		updateFeatureTerms(termId, datasource, monitor, caseType);
		updateRelationshipTerms(termId, datasource, monitor, caseType);
		updateRanks(termId, datasource, monitor, caseType);

//
//		INSERT INTO DefinedTermBase (DTYPE, id, uuid, created, protectedtitlecache, titleCache, orderindex, defaultcolor, vocabulary_id)
//		SELECT 'ReferenceSystem' ,  (@defTermId := max(id)+1)  as maxId , '1bb67042-2814-4b09-9e76-c8c1e68aa281', '2010-06-01 10:15:00', b'0', 'Google Earth', null, null, @refSysVocId
//		FROM DefinedTermBase ;
//

		//language id
		Integer langId = getLanguageId(uuidLanguage, datasource, monitor, caseType);
		if (langId == null){
			return null;
		}

		//representation
		int repId;
		sqlMaxId = " SELECT max(id)+1 as maxId FROM " + caseType.transformTo("Representation");
		rs = datasource.executeQuery(sqlMaxId);
		if (rs.next()){
			repId = rs.getInt("maxId");
		}else{
			String warning = "No representations do exist yet. Can't update terms!";
			monitor.warning(warning);
			return null;
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

			String sqlReverseInsertMN = "INSERT INTO @@RelationshipTermBase_inverseRepresentation@@ (DefinedTermBase_id, inverserepresentations_id) " +
					" VALUES ("+ termId +"," +reverseRepId+ " )";

			datasource.executeUpdate(caseType.replaceTableNames(sqlReverseInsertMN));
		}

		return termId;
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
