// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v26_30;

import java.sql.SQLException;

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


	private void moveQuestions(String featureNodeTableName, String polytomousKeyNodeTableName, ICdmDataSource datasource, boolean isAudit) {
		// move representations
		String languageStringTable = "LanguageString" +  (isAudit ? "_AUD" : "");
		String representationTable = "Representation" + (isAudit ? "_AUD" : "");
		String oldMnTable = "featuretree_representation" + (isAudit ? "_AUD" : "");
		String newMnTable = "KeyStatement_LanguageString" + (isAudit ? "_AUD" : "");
		String keyStatementTable = "KeyStatement" + (isAudit ? "_AUD" : "");
		String audit = "";
		String rAudit = "";
		if (isAudit){
			audit = ", REV, revtype";
			rAudit = ", r.REV, r.revtype";
		}
		
		
		String updateQuery = "INSERT INTO @languageStringTable (id, created, uuid, updated, text, createdby_id, updatedby_id, language_id @audit) " + 
				" SELECT id, created, uuid, updated, text, createdby_id, updatedby_id, language_id @rAudit " +
				" FROM @representationTable r INNER JOIN @oldMnTable fr ON fr.representations_id = r.id " + 
				" WHERE (1=1) ";
		updateQuery = updateQuery.replace("@languageStringTable", languageStringTable);
		updateQuery = updateQuery.replace("@representationTable", representationTable);
		updateQuery = updateQuery.replace("@oldMnTable", oldMnTable);
		updateQuery = updateQuery.replace("@audit", audit);
		updateQuery = updateQuery.replace("@nAudit", rAudit);
		System.out.println(updateQuery);
		datasource.executeUpdate(updateQuery);
		
		//key statement
		updateQuery = "INSERT INTO @keyStatementTable (id, created, uuid, updated, createdby_id, updatedby_id @audit) " + 
			" SELECT r.id, r.created, r.uuid, r.updated, r.createdby_id, r.updatedby_id @rAudit " + 
			" FROM @oldMnTable mn INNER JOIN representationTable r ";
		updateQuery = updateQuery.replace("@keyStatementTable", keyStatementTable);
		updateQuery = updateQuery.replace("@oldMnTable", oldMnTable);
		updateQuery = updateQuery.replace("@audit", audit);
		updateQuery = updateQuery.replace("@rAudit", rAudit);
		
		System.out.println(updateQuery);
		datasource.executeUpdate(updateQuery);
		
		
		//move relation
		updateQuery = "INSERT INTO @newMnTable (KeyStatement_id, label_id, label_mapkey_id @audit) " + 
			" SELECT FeatureNode_id, questions_id @audit, language_id " +
			" FROM @oldMnTable fr INNER JOIN @representationTable r " + 
			" WHERE (1=1) ";
		updateQuery = updateQuery.replace("@audit", audit);
		updateQuery = updateQuery.replace("@representationTable", representationTable);
		updateQuery = updateQuery.replace("@oldMnTable", oldMnTable);
		updateQuery = updateQuery.replace("@newMnTable", newMnTable);
		System.out.println(updateQuery);
		datasource.executeUpdate(updateQuery);
		
		//link polytoumous key node statement to keyStatement
		updateQuery = "UPDATE @polytomousKeyNodeTable pkn" + 
			" SET statement_id = (SELECT r.id FROM @representationTable r WHERE r.id = pkn.statement_id) " + 
			" WHERE pkn.id ";
		
	}
	

	private void deleteOldData(ICdmDataSource datasource, boolean isAudit) {
		String updateQuery; 
		String featureNodeTable = "FeatureNode" +  (isAudit ? "_AUD" : "");
		String featureTreeTable = "FeatureTree" + (isAudit ? "_AUD" : "");
		String representationTable = "Representation" + (isAudit ? "_AUD" : "");
		String oldMnTable = "featuretree_representation" + (isAudit ? "_AUD" : "");
		
//		statements
		updateQuery = " DELETE FROM @representationTable WHERE id IN (SELECT question_id FROM @oldMnTable)";
		updateQuery = updateQuery.replace("@representationTable", representationTable);
		updateQuery = updateQuery.replace("@oldMnTable", oldMnTable);
		System.out.println(updateQuery);
		datasource.executeUpdate(updateQuery);
		
//		feature nodes
		updateQuery = " DELETE FROM @featureNodeTable WHERE id IN (SELECT fn.id FROM @featureNodeTable fn INNER JOIN @featureTreeTable t ON fn.featuretree_id = t.id WHERE t.DTYPE = 'PolytomousKey' )";
		updateQuery = updateQuery.replace("@featureNodeTable", featureNodeTable);
		updateQuery = updateQuery.replace("@featureTreeTable", featureTreeTable);
		System.out.println(updateQuery);
		datasource.executeUpdate(updateQuery);
		
		//trees
		updateQuery = " DELETE FROM @featureTreeTable t WHERE t.DTYPE = 'PolytomousKey' " ;
		updateQuery = updateQuery.replace("@featureTreeTable", featureTreeTable);
		System.out.println(updateQuery);
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
		System.out.println(updateQuery);
		datasource.executeUpdate(updateQuery);
	}

	private void movePolytomousKeyMns(String featureTreeTableName, String polytomousKeyTableName, ICdmDataSource datasource, boolean isAudit) {
		//PolytomousKey MN update
		updateMnTables(featureTreeTableName, polytomousKeyTableName, "annotation", null, datasource, isAudit, false);
		updateMnTables(featureTreeTableName, polytomousKeyTableName, "credit", null, datasource, isAudit, true);
		updateMnTables(featureTreeTableName, polytomousKeyTableName, "extension", null, datasource, isAudit, false);
		updateMnTables(featureTreeTableName, polytomousKeyTableName, "marker", null, datasource, isAudit, false);
		updateMnTables(featureTreeTableName, polytomousKeyTableName, "originalsourcebase", "sources", datasource, isAudit, false);
		updateMnTables(featureTreeTableName, polytomousKeyTableName, "rights", "rights", datasource, isAudit, false);
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
		System.out.println(updateQuery);
		datasource.executeUpdate(updateQuery);
	}

}
