/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v24_30;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * @author a.mueller
 * @since 16.09.2010
 *
 */
public class FeatureNodeTreeColumnUpdater
        extends SchemaUpdaterStepBase{

    private static final Logger logger = LogManager.getLogger();

	private String treeTableName;
	private String nodeTableName;
	private boolean includeAudTable;

	public static final FeatureNodeTreeColumnUpdater NewInstance(List<ISchemaUpdaterStep> stepList, String stepName, boolean includeAudTable){
		return new FeatureNodeTreeColumnUpdater(stepList, stepName, includeAudTable);
	}

	protected FeatureNodeTreeColumnUpdater(List<ISchemaUpdaterStep> stepList, String stepName,  boolean includeAudTable) {
		super(stepList, stepName);
		this.treeTableName = "FeatureTree";
		this.nodeTableName = "FeatureNode";
		this.includeAudTable = includeAudTable;
	}

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {
        updateTree(caseType.replaceTableNames(treeTableName),
                caseType.replaceTableNames(nodeTableName), datasource, monitor, result);
		if (includeAudTable){
			String aud = "_AUD";
			updateTree(caseType.replaceTableNames(treeTableName + aud),
			        caseType.replaceTableNames(nodeTableName + aud), datasource, monitor, result);
		}
		return;
	}

	private void updateTree(String treeTableName, String nodeTableName, ICdmDataSource datasource,
	        IProgressMonitor monitor, SchemaUpdateResult result) {
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
			return;
		}catch(Exception e){
			monitor.warning(e.getMessage(), e);
			logger.error(e.getMessage());
			result.addException(e, e.getMessage(), "FeatureNodeTreeColumnUpdater.updateTree");
			return;
		}
	}

}
