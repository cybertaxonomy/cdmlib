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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * @author a.mueller
 * @date 16.09.2010
 *
 */
public class FeatureNodeTreeColumnUpdater extends SchemaUpdaterStepBase implements ISchemaUpdaterStep {
	private static final Logger logger = Logger.getLogger(FeatureNodeTreeColumnUpdater.class);

	private String treeTableName;
	private String nodeTableName;
	private boolean includeAudTable;

	public static final FeatureNodeTreeColumnUpdater NewInstance(String stepName, boolean includeAudTable){
		return new FeatureNodeTreeColumnUpdater(stepName, includeAudTable);
	}

	protected FeatureNodeTreeColumnUpdater(String stepName,  boolean includeAudTable) {
		super(stepName);
		this.treeTableName = "FeatureTree";
		this.nodeTableName = "FeatureNode";
		this.includeAudTable = includeAudTable;
	}

	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		boolean result = true;
		result &= updateTree(caseType.replaceTableNames(treeTableName), caseType.replaceTableNames(nodeTableName), datasource, monitor);
		if (includeAudTable){
			String aud = "_AUD";
			result &= updateTree(caseType.replaceTableNames(treeTableName + aud), caseType.replaceTableNames(nodeTableName + aud), datasource, monitor);
		}
		return (result == true )? 0 : null;
	}

	private boolean updateTree(String treeTableName, String nodeTableName, ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		try{
			String resulsetQuery = "SELECT id, root_id FROM @treeTableName ORDER BY id";
			resulsetQuery = resulsetQuery.replace("@treeTableName", treeTableName);

			ResultSet rs = datasource.executeQuery(resulsetQuery);
			while (rs.next()){
				Integer treeId = rs.getInt("id");
				Integer rootId = rs.getInt("root_id");
				String updateQuery = "UPDATE @nodeTableName SET featuretree_id = @treeId WHERE id = @rootId";
				updateQuery = updateQuery.replace("@nodeTableName", nodeTableName);
				updateQuery = updateQuery.replace("@treeId", treeId.toString());
				updateQuery = updateQuery.replace("@rootId", rootId.toString());
				datasource.executeUpdate(updateQuery);
			}

			String countQuery = "SELECT count(*) FROM @nodeTableName WHERE featuretree_id IS NULL";
			countQuery = countQuery.replace("@nodeTableName", nodeTableName);
			Long countMissingTrees = (Long)datasource.getSingleValue(countQuery);
			while (countMissingTrees > 0){
				//FIXME this is not ANSI-SQL and will run only in MySQL
				String updateQuery = "UPDATE @nodeTableName AS child INNER JOIN @nodeTableName AS parent ON child.parent_fk = parent.id " +
						"SET child.featuretree_id = parent.featuretree_id WHERE child.featuretree_id IS NULL";
				updateQuery = updateQuery.replace("@nodeTableName", nodeTableName);
	//			updateQuery = updateQuery.replace("@treeId", treeId.toString());

				datasource.executeUpdate(updateQuery);
				Long oldCountMissingTrees = countMissingTrees;
				countMissingTrees = (Long)datasource.getSingleValue(countQuery);
				if (oldCountMissingTrees.equals(countMissingTrees)){
					throw new RuntimeException("No row updated in FeatureNodeTreeColumnUpdater. Throw exception to avoid infinite loop");
				}
			}
			return true;
		}catch(Exception e){
			monitor.warning(e.getMessage(), e);
			logger.error(e.getMessage());
			return false;
		}
	}

}
