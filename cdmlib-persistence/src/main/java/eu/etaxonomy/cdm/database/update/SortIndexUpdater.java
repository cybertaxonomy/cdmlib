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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @date 16.09.2010
 *
 */
public class SortIndexUpdater extends SchemaUpdaterStepBase<SortIndexUpdater> implements ISchemaUpdaterStep {
	private static final Logger logger = Logger.getLogger(SortIndexUpdater.class);
	
	private String tableName;
	private String sortIndexColumn;
	private String parentColumn;
	private String idColumn = "id";
	private boolean includeAudTable;
	private Integer baseValue = 0;
	
	public static final SortIndexUpdater NewInstance(String stepName, String tableName, String parentColumn, String sortIndexColumn, boolean includeAudTable){
		return new SortIndexUpdater(stepName, tableName, parentColumn, sortIndexColumn, "id", includeAudTable, 0);
	}

	public static final SortIndexUpdater NewInstance(String stepName, String tableName, String parentColumn, String sortIndexColumn, String idColumn, boolean includeAudTable){
		return new SortIndexUpdater(stepName, tableName, parentColumn,sortIndexColumn, idColumn, includeAudTable, 0);
	}

	
	protected SortIndexUpdater(String stepName, String tableName, String parentColumn, String sortIndexColumn, String idColumn, boolean includeAudTable, Integer baseValue) {
		super(stepName);
		this.tableName = tableName;
		this.parentColumn = parentColumn;
		this.sortIndexColumn = sortIndexColumn;
		this.idColumn = idColumn;
		this.includeAudTable = includeAudTable;
		this.baseValue = baseValue;
	}

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
		
		Map<Integer, Set<Integer>> indexMap = makeIndexMap(datasource);
		
		updateIndices(tableName, datasource, indexMap);
		
		return true;
	}

	/**
	 * @param datasource 
	 * @param rs
	 * @param index
	 * @param oldParentId
	 * @return
	 * @throws SQLException
	 */
	private Map<Integer, Set<Integer>> makeIndexMap(ICdmDataSource datasource) throws SQLException {
		String resulsetQuery = "SELECT @id as id, @parentColumn " +
				" FROM @tableName " +
				" WHERE @parentColumn IS NOT NULL " + 
				" ORDER BY @parentColumn,    @id";
		resulsetQuery = resulsetQuery.replace("@tableName", tableName);
		resulsetQuery = resulsetQuery.replace("@parentColumn", parentColumn);
		resulsetQuery = resulsetQuery.replace("@id", idColumn);

		ResultSet rs = datasource.executeQuery(resulsetQuery);
		Integer index = baseValue;
		int oldParentId = -1;
		
		
		//increase index with each row, set to 0 if parent is not the same as the previous one
		Map<Integer, Set<Integer>> indexMap = new HashMap<Integer, Set<Integer>>(); 
		while (rs.next() ){
			int id = rs.getInt("id");
			Object oParentId = rs.getObject(parentColumn);
			if (oParentId != null){
				int parentId = Integer.valueOf(oParentId.toString());
				if (oldParentId != parentId){
					index = baseValue;
					oldParentId = parentId;
				}else{
					index++;
				}
				putIndex(id, index, indexMap);
			}else{
				logger.warn("This should not happen");
				index = baseValue;
			}
//			System.out.println(oParentId + "," + id+","+ index+";");
		}
		return indexMap;
	}

	private void updateIndices(String tableName, ICdmDataSource datasource, Map<Integer, Set<Integer>> indexMap)
			throws SQLException {
		for (Integer index :  indexMap.keySet()){
			Set<Integer> set = indexMap.get(index);
			String idSetString = makeIdSetString(set);
			
			String updateQuery = "UPDATE @tableName SET @sortIndexColumn = @index WHERE @id IN (@idList) ";
			updateQuery = updateQuery.replace("@tableName", tableName);
			updateQuery = updateQuery.replace("@sortIndexColumn", sortIndexColumn);
			updateQuery = updateQuery.replace("@index", index.toString());
			updateQuery = updateQuery.replace("@idList", idSetString);
			updateQuery = updateQuery.replace("@id", idColumn);
			datasource.executeUpdate(updateQuery);
		}
	}

	private static String makeIdSetString(Set<Integer> set) {
		StringBuffer result = new StringBuffer(set.size() * 5);
		for (Integer id:set){
			result.append(id + ",");
		}
		return result.substring(0, result.length() - 1);
	}

	/**
	 * Adds the id to the index (each id is attached to an (sort)index) 
	 */
	private void putIndex(Integer id, Integer index, Map<Integer, Set<Integer>> indexMap) {
		Set<Integer> set = indexMap.get(index);
		if (set == null){
			set = new HashSet<Integer>();
			indexMap.put(index, set);
		}
		set.add(id);
	}

}
