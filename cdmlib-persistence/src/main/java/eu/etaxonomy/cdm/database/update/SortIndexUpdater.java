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
 */
public class SortIndexUpdater extends SchemaUpdaterStepBase<SortIndexUpdater> {
	private static final Logger logger = Logger.getLogger(SortIndexUpdater.class);

	private final String tableName;
	private final String sortIndexColumn;
	private final String parentColumn;
	private String idColumn = "id";
	private String currentSortColumn = "id";
	private final boolean includeAudTable;
	private Integer baseValue = 0;

	public static final SortIndexUpdater NewInstance(String stepName, String tableName, String parentColumn, String sortIndexColumn, boolean includeAudTable){
		return new SortIndexUpdater(stepName, tableName, parentColumn, sortIndexColumn, "id", "id", includeAudTable, 0);
	}

	public static final SortIndexUpdater NewInstance(String stepName, String tableName, String parentColumn, String sortIndexColumn, String idColumn, boolean includeAudTable){
		return new SortIndexUpdater(stepName, tableName, parentColumn,sortIndexColumn, idColumn, idColumn, includeAudTable, 0);
	}

    /**
     * Returns an SortIndexUpdater that updates an existing sortindex which might have missing sortindex numbers in between.
     *
     */
    public static final SortIndexUpdater NewUpdateExistingSortindexInstance(String stepName, String tableName, String parentColumn, String sortIndexColumn, boolean includeAudTable){
        return new SortIndexUpdater(stepName, tableName, parentColumn,sortIndexColumn, "id", sortIndexColumn, includeAudTable, 0);
    }


	protected SortIndexUpdater(String stepName, String tableName, String parentColumn, String sortIndexColumn, String idColumn, String currentSortColumn, boolean includeAudTable, Integer baseValue) {
		super(stepName);
		this.tableName = tableName;
		this.parentColumn = parentColumn;
		this.sortIndexColumn = sortIndexColumn;
		this.idColumn = idColumn;
		this.currentSortColumn = currentSortColumn;
		this.includeAudTable = includeAudTable;
		this.baseValue = baseValue;
	}

	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		boolean result = true;
		result &= addColumn(caseType.transformTo(tableName), datasource);
		if (includeAudTable){
			String aud = "_AUD";
			result &= addColumn(caseType.transformTo(tableName + aud), datasource);
		}
		return (result == true )? 0 : null;
	}

	private boolean addColumn(String tableName, ICdmDataSource datasource) throws SQLException {
		//Note: caseType not required here
		Map<Integer, Set<Integer>> indexMap = makeIndexMap(tableName, datasource);

		updateIndices(tableName, datasource, indexMap);

		return true;
	}


	/**
	 * For each (new) sortIndex value the according record ids are computed.
	 * This allows updating all records sortindex by sortindex.
	 * @param tableName
	 * @param datasource
	 * @return
	 * @throws SQLException
	 */
	private Map<Integer, Set<Integer>> makeIndexMap(String tableName, ICdmDataSource datasource) throws SQLException {
		String resultsetQuery = "SELECT @id as id, @parentColumn " +
				" FROM @tableName " +
				" WHERE @parentColumn IS NOT NULL " +
				" ORDER BY @parentColumn, @sorted";
		resultsetQuery = resultsetQuery.replace("@id", idColumn);
		resultsetQuery = resultsetQuery.replace("@tableName", tableName);
		resultsetQuery = resultsetQuery.replace("@parentColumn", parentColumn);
		resultsetQuery = resultsetQuery.replace("@sorted", currentSortColumn);

		ResultSet rs = datasource.executeQuery(resultsetQuery);
		Integer index = baseValue;
		int oldParentId = -1;


		//increase index with each row, set to 0 if parent is not the same as the previous one
		Map<Integer, Set<Integer>> indexMap = new HashMap<>();
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
			set = new HashSet<>();
			indexMap.put(index, set);
		}
		set.add(id);
	}

}
