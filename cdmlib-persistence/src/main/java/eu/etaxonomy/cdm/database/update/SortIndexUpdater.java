// $Id$
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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @date 16.09.2010
 *
 */
public class SortIndexUpdater extends SchemaUpdaterStepBase implements ISchemaUpdaterStep {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SortIndexUpdater.class);
	
	private String tableName;
	private String sortIndexColumn;
	private String parentColumn;
	private boolean includeAudTable;
	private Integer baseValue = 0;
	
	public static final SortIndexUpdater NewInstance(String stepName, String tableName, String parentColumn, String sortIndexColumn, boolean includeAudTable){
		return new SortIndexUpdater(stepName, tableName, parentColumn, sortIndexColumn, includeAudTable, 0);
	}
	
	protected SortIndexUpdater(String stepName, String tableName, String parentColumn, String sortIndexColumn, boolean includeAudTable, Integer baseValue) {
		super(stepName);
		this.tableName = tableName;
		this.parentColumn = parentColumn;
		this.sortIndexColumn = sortIndexColumn;
		this.includeAudTable = includeAudTable;
		this.baseValue = baseValue;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase#invoke(eu.etaxonomy.cdm.database.ICdmDataSource, eu.etaxonomy.cdm.common.IProgressMonitor)
	 */
	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		boolean result = true;
		result &= addColumn(tableName, datasource, monitor);
		if (includeAudTable){
			String aud = "_AUD";
			result &= addColumn(tableName + aud, datasource, monitor);
		}
		return (result == true )? 0 : null;
	}

	private boolean addColumn(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		String resulsetQuery = "SELECT id, @parentColumn FROM @tableName ORDER BY @parentColumn, id";
		resulsetQuery = resulsetQuery.replace("@tableName", tableName);
		resulsetQuery = resulsetQuery.replace("@parentColumn", parentColumn);

		ResultSet rs = datasource.executeQuery(resulsetQuery);
		Integer index = baseValue;
		Integer oldParentId = -1;
		while (rs.next()){
			int id = rs.getInt("id");
			Object parentId = rs.getObject(parentColumn);
			if (parentId != null){
				if (oldParentId != Integer.valueOf(parentId.toString())){
					index = baseValue;
					oldParentId = Integer.valueOf(parentId.toString());
				}
				String updateQuery = "UPDATE @tableName SET @sortIndexColumn = @index WHERE id = " + id;
				updateQuery = updateQuery.replace("@tableName", tableName);
				updateQuery = updateQuery.replace("@sortIndexColumn", sortIndexColumn);
				updateQuery = updateQuery.replace("@index", index.toString());
				datasource.executeUpdate(updateQuery);
				index++;
			}else{
				index = baseValue;
			}
		}

		
		return true;
	}

}
