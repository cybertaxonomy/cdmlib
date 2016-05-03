// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v31_33;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ITermUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * Creates empty root nodes for each classification. Replacing MN tables for classification root nodes.
 * For single use in {@link SchemaUpdater_33_331}
 * @author a.mueller
 * @date 15.12.2013
 */
public class ClassificationRootNodeUpdater extends SchemaUpdaterStepBase<ClassificationRootNodeUpdater> implements ITermUpdaterStep{
	private static final Logger logger = Logger.getLogger(ClassificationRootNodeUpdater.class);

	private static final String stepName = "Update Classification Root Nodes";
	
// **************************** STATIC METHODS ********************************/

	public static final ClassificationRootNodeUpdater NewInstance(){
		return new ClassificationRootNodeUpdater(stepName);	
	}

	protected ClassificationRootNodeUpdater(String stepName) {
		super(stepName);
	}

	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		
		try {
			
			// for each classification
			String sql = " SELECT id FROM " + caseType.transformTo("Classification") + " ORDER BY id ";
			ResultSet rs = datasource.executeQuery(sql);
			while (rs.next()){
				Number classificationId = rs.getInt("id");
				
				//getMaxId in TaxonNode
				sql = " SELECT max(id) FROM " + caseType.transformTo("TaxonNode");
				Number maxId = ((Number)datasource.getSingleValue(sql));
				maxId = maxId == null ? 1 : ((Integer)maxId + 1);
				
				//count children
				sql = " SELECT count(*) as n " +
						" FROM @@Classification_TaxonNode@@ MN " +
						" WHERE MN.Classification_id = " + classificationId; 
				Number countChildren = (Number)datasource.getSingleValue(caseType.replaceTableNames(sql));
				
				//create root node
				sql = " INSERT INTO @@TaxonNode@@ (id, created, createdby_id , uuid, countchildren, classification_id, parent_id, taxon_id, treeIndex, sortIndex ) "+ 
						" VALUES (%d, '%s', null, '%s', %d, %d, NULL, NULL, '#c%d#%d', NULL) ";
				sql = String.format(sql, 
						maxId, this.getNowString(), UUID.randomUUID(), countChildren, classificationId, classificationId, maxId);
				datasource.executeUpdate(caseType.replaceTableNames(sql));
				
				//create fks to new root node
				sql = " UPDATE @@Classification@@ " +
						" SET rootnode_id = " +  maxId +
						" WHERE id = " + classificationId;
				datasource.executeUpdate(caseType.replaceTableNames(sql));
				
//				//update current root nodes (parent) 
//				sql = " UPDATE @@TaxonNode@@ " +
//						" SET parent_id = " +  maxId +
//						" WHERE c.id = " + classificationId + " AND parent_id IS NULL";
//				datasource.executeUpdate(caseType.replaceTableNames(sql));
				
				//update sort index and parent_id 
				sql = " UPDATE @@TaxonNode@@ " +
						" SET sortIndex = (SELECT sortIndex FROM @@Classification_TaxonNode@@ MN WHERE MN.rootnodes_id = TaxonNode.id AND MN.Classification_id = @@TaxonNode@@.classification_id)," +
							" parent_id = " + maxId +
						" WHERE EXISTS (SELECT * FROM @@Classification_TaxonNode@@ MN2 WHERE MN2.Classification_id = %d AND MN2.rootnodes_id = @@TaxonNode@@.id )";
				sql = String.format(sql, classificationId);
				datasource.executeUpdate(caseType.replaceTableNames(sql));
				
			}
			
			//run treeindex creator
			// remove old MN table
			//done in Schema updater
			
			return 0;

		} catch (Exception e) {
			monitor.warning(e.getMessage(), e);
			logger.warn(e.getMessage());
			return null;
		}
	}

}
