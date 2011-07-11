// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v25_30;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * @author a.mueller
 * @date 16.09.2010
 *
 */
public class PolytomousKeyDataMover extends SchemaUpdaterStepBase implements ISchemaUpdaterStep {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PolytomousKeyDataMover.class);
	
	private String featureTreeTableName;
	private String featureNodeTableName;
	private String polytomousKeyTableName;
	private String polytomousKeyNodeTableName;
	private boolean includeAudTable;
	
	public static final PolytomousKeyDataMover NewInstance(String stepName, boolean includeAudTable){
		return new PolytomousKeyDataMover(stepName, includeAudTable);
	}
	
	protected PolytomousKeyDataMover(String stepName,  boolean includeAudTable) {
		super(stepName);
		this.featureTreeTableName = "FeatureTree";
		this.featureNodeTableName = "FeatureNode";
		this.polytomousKeyTableName = "PolytomousKey";
		this.polytomousKeyNodeTableName = "PolytomousKeyNode";
		this.includeAudTable = includeAudTable;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase#invoke(eu.etaxonomy.cdm.database.ICdmDataSource, eu.etaxonomy.cdm.common.IProgressMonitor)
	 */
	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		boolean result = true;
		result &= movePolytomousKeys(featureTreeTableName, featureNodeTableName, polytomousKeyTableName, polytomousKeyNodeTableName, datasource, monitor, false);
		if (includeAudTable){
			String aud = "_AUD";
			result &= movePolytomousKeys(featureTreeTableName + aud, featureNodeTableName + aud, polytomousKeyTableName + aud, polytomousKeyNodeTableName + aud, datasource, monitor, true);
		}
		return (result == true )? 0 : null;
	}

	private boolean movePolytomousKeys(String featureTreeTableName, String featureNodeTableName, String polytomousKeyTableName, String polytomousKeyNodeTableName, ICdmDataSource datasource, IProgressMonitor monitor, boolean isAudit) throws SQLException {
		movePolytomousKey(featureTreeTableName, polytomousKeyTableName, datasource, isAudit);
		movePolytomousKeyMns(featureTreeTableName, polytomousKeyTableName, datasource, isAudit);
		movePolytomousKeyNodes(featureTreeTableName, featureNodeTableName, polytomousKeyNodeTableName, datasource, isAudit);

		moveQuestions(featureNodeTableName, polytomousKeyNodeTableName, datasource, isAudit);
		
		deleteOldData(datasource, isAudit);
		return true;
	}


	private void moveQuestions(String featureNodeTableName, String polytomousKeyNodeTableName, ICdmDataSource datasource, boolean isAudit) throws SQLException {
		String aud = "";
		String audValue = "";
		String audParam = "";
		if (isAudit){
			aud = "_AUD";
			audValue = ",@REV, @revtype";
			audParam = ", REV, revtype";
		}
		//For each Question 
		String questionSql = " SELECT * " + 
			" FROM FeatureNode_Representation@_aud mn INNER JOIN Representation@_aud r ON mn.questions_id = r.id ";
		questionSql = questionSql.replace("@_aud", aud);
		ResultSet rs = datasource.executeQuery(questionSql);
		while (rs.next()){
			
			//Created KeyStatement
			String updateQuery = " INSERT INTO KeyStatement@_aud (id, uuid, created, updated, createdby_id, updatedby_id @audParam)" + 
				" VALUES (@id, @uuid, @createdWhen, @updatedWhen, @createdby_id, @updatedby_id @audValue)";
			updateQuery = updateQuery.replace("@audValue", audValue);
			updateQuery = updateQuery.replace("@id", rs.getObject("FeatureNode_id").toString()); //use feature node id for key statement id 
			updateQuery = updateQuery.replace("@uuid", UUID.randomUUID().toString()); //use random uuid
			updateQuery = updateQuery.replace("@createdWhen", nullSafeString(rs.getString("created")));
			updateQuery = updateQuery.replace("@updatedWhen", nullSafeString((rs.getString("updated"))));
			updateQuery = updateQuery.replace("@createdby_id", nullSafe(rs.getObject("createdby_id")));
			updateQuery = updateQuery.replace("@updatedby_id", nullSafe(rs.getObject("updatedby_id")));
			if (isAudit){
				updateQuery = updateQuery.replace("@REV", nullSafe(rs.getObject("r.REV")));
				updateQuery = updateQuery.replace("@revtype", nullSafe(rs.getObject("r.revtype")));
			}
			updateQuery = updateQuery.replace("@_aud", aud);
			updateQuery = updateQuery.replace("@audParam", audParam);
			datasource.executeUpdate(updateQuery);
			
			//create entry in Language String
			updateQuery = " INSERT INTO LanguageString@_aud (id, created, uuid, updated, text, createdby_id, updatedby_id, language_id @audParam) " + 
				" VALUES (@id, @createdWhen, @uuid, @updatedWhen, @text, @createdby_id, @updatedby_id, @language_id @audValue)";
			updateQuery = updateQuery.replace("@audValue", audValue);
			updateQuery = updateQuery.replace("@id", rs.getObject("id").toString());
			updateQuery = updateQuery.replace("@createdWhen", nullSafeString(rs.getString("created")));
			updateQuery = updateQuery.replace("@updatedWhen", nullSafeString(rs.getString("updated")));
			updateQuery = updateQuery.replace("@createdby_id", nullSafe(rs.getObject("createdby_id")));
			updateQuery = updateQuery.replace("@updatedby_id", nullSafe(rs.getObject("updatedby_id")));
			updateQuery = updateQuery.replace("@uuid", nullSafeString(rs.getString("uuid")));
			updateQuery = updateQuery.replace("@text", nullSafeString(rs.getString("text")));
			updateQuery = updateQuery.replace("@language_id", nullSafe(rs.getObject("language_id")));
			if (isAudit){
				updateQuery = updateQuery.replace("@REV", nullSafe(rs.getObject("r.REV")));
				updateQuery = updateQuery.replace("@revtype", nullSafe(rs.getObject("r.revtype")));
			}
			updateQuery = updateQuery.replace("@_aud", aud);
			updateQuery = updateQuery.replace("@audParam", audParam);
			datasource.executeUpdate(updateQuery);
					
			//create entry in KeyStatement_LanguageString
			updateQuery = " INSERT INTO KeyStatement_LanguageString@_aud (KeyStatement_id, label_id, label_mapkey_id @audParam) " + 
				" VALUES (@keystatement_id, @languagestring_id, @language_id @audValue) ";
			updateQuery = updateQuery.replace("@audValue", audValue);
			updateQuery = updateQuery.replace("@keystatement_id", nullSafe(rs.getObject("FeatureNode_id")));
			updateQuery = updateQuery.replace("@languagestring_id", nullSafe(rs.getObject("id")));
			updateQuery = updateQuery.replace("@language_id", nullSafe(rs.getObject("language_id")));
			if (isAudit){
				updateQuery = updateQuery.replace("@REV", nullSafe(rs.getObject("r.REV")));
				updateQuery = updateQuery.replace("@revtype", nullSafe(rs.getObject("r.revtype")));
			}
			updateQuery = updateQuery.replace("@_aud", aud);
			updateQuery = updateQuery.replace("@audParam", audParam);
			datasource.executeUpdate(updateQuery);
			
			//link polytomouskeynode statement to KeyStatement
			updateQuery = " UPDATE PolytomousKeyNode@_aud " + 
					" SET statement_id = id " + 
					" WHERE id = @id ";
			updateQuery = updateQuery.replace("@id", nullSafe(rs.getObject("FeatureNode_id")));
			updateQuery = updateQuery.replace("@_aud", aud);
			datasource.executeUpdate(updateQuery);
			
		}
		
			
		
		
//		// move representations
//		String languageStringTable = "LanguageString" +  (isAudit ? "_AUD" : "");
//		String representationTable = "Representation" + (isAudit ? "_AUD" : "");
//		String oldMnTable = "featuretree_representation" + (isAudit ? "_AUD" : "");
//		String newMnTable = "KeyStatement_LanguageString" + (isAudit ? "_AUD" : "");
//		String keyStatementTable = "KeyStatement" + (isAudit ? "_AUD" : "");
//		String audit = "";
//		String rAudit = "";
//		if (isAudit){
//			audit = ", REV, revtype";
//			rAudit = ", r.REV, r.revtype";
//		}
//		
//		
//		String updateQuery = "INSERT INTO @languageStringTable (id, created, uuid, updated, text, createdby_id, updatedby_id, language_id @audit) " + 
//				" SELECT id, created, uuid, updated, text, createdby_id, updatedby_id, language_id @rAudit " +
//				" FROM @representationTable r INNER JOIN @oldMnTable fr ON fr.representations_id = r.id " + 
//				" WHERE (1=1) ";
//		updateQuery = updateQuery.replace("@languageStringTable", languageStringTable);
//		updateQuery = updateQuery.replace("@representationTable", representationTable);
//		updateQuery = updateQuery.replace("@oldMnTable", oldMnTable);
//		updateQuery = updateQuery.replace("@audit", audit);
//		updateQuery = updateQuery.replace("@nAudit", rAudit);
//		System.out.println(updateQuery);
//		datasource.executeUpdate(updateQuery);
//		
//		//key statement
//		audit = "";
//		rAudit = "";
//		updateQuery = "INSERT INTO @keyStatementTable (id, created, uuid, updated, createdby_id, updatedby_id @audit) " + 
//			" SELECT r.id, r.created, r.uuid, r.updated, r.createdby_id, r.updatedby_id @rAudit " + 
//			" FROM @oldMnTable mn INNER JOIN representationTable r ";
//		updateQuery = updateQuery.replace("@keyStatementTable", keyStatementTable);
//		updateQuery = updateQuery.replace("@oldMnTable", oldMnTable);
//		updateQuery = updateQuery.replace("@audit", audit);
//		updateQuery = updateQuery.replace("@rAudit", rAudit);
//		
//		System.out.println(updateQuery);
//		datasource.executeUpdate(updateQuery);
//		
//		
//		//move relation
//		audit = "";
//		updateQuery = "INSERT INTO @newMnTable (KeyStatement_id, label_id, label_mapkey_id @audit) " + 
//			" SELECT FeatureNode_id, questions_id @audit, language_id " +
//			" FROM @oldMnTable fr INNER JOIN @representationTable r " + 
//			" WHERE (1=1) ";
//		updateQuery = updateQuery.replace("@audit", audit);
//		updateQuery = updateQuery.replace("@representationTable", representationTable);
//		updateQuery = updateQuery.replace("@oldMnTable", oldMnTable);
//		updateQuery = updateQuery.replace("@newMnTable", newMnTable);
//		System.out.println(updateQuery);
//		datasource.executeUpdate(updateQuery);
//		
//		//link polytoumous key node statement to keyStatement
//		updateQuery = "UPDATE @polytomousKeyNodeTable pkn" + 
//			" SET statement_id = (SELECT r.id FROM @representationTable r WHERE r.id = pkn.statement_id) " + 
//			" WHERE pkn.id ";
		
	}
	
	private String nullSafeString(Object object){
		if (object == null){
			return "NULL";
		}else{
			String result = object.toString().replace("'", "''");
			return "'" + result + "'";
		}
	}

	private String nullSafe(Object object) {
		if (object == null){
			return "NULL";
		}else{
			return object.toString();
		}
	}

	private void deleteOldData(ICdmDataSource datasource, boolean isAudit) {
		String updateQuery; 
		String featureNodeTable = "FeatureNode" +  (isAudit ? "_AUD" : "");
		String featureTreeTable = "FeatureTree" + (isAudit ? "_AUD" : "");
		String representationTable = "Representation" + (isAudit ? "_AUD" : "");
		String oldMnTable = "FeatureNode_Representation" + (isAudit ? "_AUD" : "");
		
//		statements
		updateQuery = " DELETE FROM @representationTable WHERE id IN (SELECT questions_id FROM @oldMnTable)";
		updateQuery = updateQuery.replace("@representationTable", representationTable);
		updateQuery = updateQuery.replace("@oldMnTable", oldMnTable);
		logger.debug(updateQuery);
		datasource.executeUpdate(updateQuery);
		
//		feature nodes
		updateQuery = " DELETE FROM @featureNodeTable WHERE featuretree_id IN (SELECT t.id FROM @featureTreeTable t WHERE t.DTYPE = 'PolytomousKey' )";
		updateQuery = updateQuery.replace("@featureNodeTable", featureNodeTable);
		updateQuery = updateQuery.replace("@featureTreeTable", featureTreeTable);
		logger.debug(updateQuery);
		datasource.executeUpdate(updateQuery);
		
		//trees
		updateQuery = " DELETE FROM @featureTreeTable WHERE DTYPE = 'PolytomousKey' " ;
		updateQuery = updateQuery.replace("@featureTreeTable", featureTreeTable);
		logger.debug(updateQuery);
		datasource.executeUpdate(updateQuery);
		
		
	}

	private void movePolytomousKeyNodes(String featureTreeTableName,
			String featureNodeTableName, String polytomousKeyNodeTableName,
			ICdmDataSource datasource, boolean isAudit) {
		String updateQuery;
		
		//PolytomousKey node
		updateQuery = " INSERT INTO @polytomousKeyNodeTableName(id, created, uuid, updated, sortindex, createdby_id, updatedby_id, feature_id, parent_id, taxon_id, key_id @audit) " + 
				" SELECT n.id, n.created, n.uuid, n.updated, n.sortindex, n.createdby_id, n.updatedby_id, n.feature_id, n.parent_fk, n.taxon_id, n.featuretree_id @nAudit" + 
				" FROM @featureNodeTableName n INNER JOIN @featureTreeTableName t ON n.featuretree_id = t.id" +
				" WHERE t.DTYPE = 'PolytomousKey'";
		updateQuery = updateQuery.replace("@polytomousKeyNodeTableName", polytomousKeyNodeTableName);
		updateQuery = updateQuery.replace("@featureNodeTableName", featureNodeTableName);
		updateQuery = updateQuery.replace("@featureTreeTableName", featureTreeTableName);
		
		String audit = "";
		String nAudit = "";
		if (isAudit){
			audit = ", REV, revtype";
			nAudit = ", n.REV, n.revtype";
		}
		updateQuery = updateQuery.replace("@audit", audit);
		updateQuery = updateQuery.replace("@nAudit", nAudit);
		logger.debug(updateQuery);
		datasource.executeUpdate(updateQuery);
	}

	private void movePolytomousKeyMns(String featureTreeTableName, String polytomousKeyTableName, ICdmDataSource datasource, boolean isAudit) {
		//PolytomousKey MN update
		updateMnTables(featureTreeTableName, polytomousKeyTableName, "Annotation", null, datasource, isAudit, false);
		updateMnTables(featureTreeTableName, polytomousKeyTableName, "Credit", null, datasource, isAudit, true);
		updateMnTables(featureTreeTableName, polytomousKeyTableName, "Extension", null, datasource, isAudit, false);
		updateMnTables(featureTreeTableName, polytomousKeyTableName, "Marker", null, datasource, isAudit, false);
		updateMnTables(featureTreeTableName, polytomousKeyTableName, "OriginalSourceBase", "Sources", datasource, isAudit, false);
		updateMnTables(featureTreeTableName, polytomousKeyTableName, "Rights", "Rights", datasource, isAudit, false);
	}

	private void movePolytomousKey(String featureTreeTableName,
			String polytomousKeyTableName, ICdmDataSource datasource,
			boolean isAudit) {
		//PolytomousKey
		//TODO monitor polytomous keys with uri for data loss
		
		String updateQuery = " INSERT INTO @polytomousKeyTableName(id, created, uuid, updated, lsid_authority, lsid_lsid, lsid_namespace, lsid_object, lsid_revision, protectedtitlecache, titleCache, createdby_id, updatedby_id, root_id @audit) " + 
					" SELECT id, created, uuid, updated, lsid_authority, lsid_lsid, lsid_namespace, lsid_object, lsid_revision, protectedtitlecache, titleCache, createdby_id, updatedby_id, root_id @audit" + 
					" FROM @featureTreeTableName WHERE DTYPE = 'PolytomousKey'";
		updateQuery = updateQuery.replace("@polytomousKeyTableName", polytomousKeyTableName);
		updateQuery = updateQuery.replace("@featureTreeTableName", featureTreeTableName);
		String audit = "";
		if (isAudit){
			audit = ", REV, revtype";
		}
		updateQuery = updateQuery.replace("@audit", audit);
		datasource.executeUpdate(updateQuery);
	}

	private void updateMnTables(String featureTreeTableName, String polytomousKeyTableName, String attributeName, String attributePluralString, ICdmDataSource datasource, boolean isAudit, boolean hasSortIndex) {
		String updateQuery;
		String audit;
		if (isAudit){
			featureTreeTableName = featureTreeTableName.replace("_AUD", "");
			polytomousKeyTableName = polytomousKeyTableName.replace("_AUD", "");
		}
		String newMnTable = polytomousKeyTableName + "_" + attributeName + (isAudit? "_AUD" : "");
		String oldMnTable = featureTreeTableName + "_" + attributeName + (isAudit? "_AUD" : "");
		String pluralIdAttribute = ((attributePluralString == null) ? attributeName + "s" : attributePluralString)  + "_id";
		String sortIndex = (hasSortIndex ? ", sortIndex" : "");
		updateQuery = " INSERT INTO @newMnTable(PolytomousKey_id, @pluralIdAttribute @sortIndex @audit) " + 
					" SELECT FeatureTree_id, @pluralIdAttribute @sortIndex @audit" + 
					" FROM @oldMnTable mn INNER JOIN @featureTreeTableName pk ON mn.FeatureTree_id = pk.id " +
					" WHERE pk.DTYPE = 'PolytomousKey'";
		updateQuery = updateQuery.replace("@polytomousKeyTableName", polytomousKeyTableName);
		updateQuery = updateQuery.replace("@featureTreeTableName", featureTreeTableName);
		updateQuery = updateQuery.replace("@newMnTable", newMnTable);
		updateQuery = updateQuery.replace("@oldMnTable", oldMnTable);
		updateQuery = updateQuery.replace("@pluralIdAttribute", pluralIdAttribute);
		updateQuery = updateQuery.replace("@sortIndex", sortIndex);
		audit = "";
		if (isAudit){
			audit = ", REV, revtype";
		}
		updateQuery = updateQuery.replace("@audit", audit);
		logger.debug(updateQuery);
		datasource.executeUpdate(updateQuery);
	}

}
