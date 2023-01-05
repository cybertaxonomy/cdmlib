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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @since 16.09.2010
 */
public class SortIndexUpdater extends SchemaUpdaterStepBase {

    private static final Logger logger = LogManager.getLogger();

	private final String tableName;
    private final String sortIndexColumn;
	private final String parentColumn;
    private String idColumn = "id";
	private String currentSortColumn = "id";
	private final boolean includeAudTable;
	private Integer baseValue = 0;

	public static final SortIndexUpdater NewInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String parentColumn, String sortIndexColumn, boolean includeAudTable){
		return new SortIndexUpdater(stepList, stepName, tableName, parentColumn, sortIndexColumn, "id", "id", includeAudTable, 0);
	}

	public static final SortIndexUpdater NewInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String parentColumn, String sortIndexColumn, String idColumn, boolean includeAudTable){
		return new SortIndexUpdater(stepList, stepName, tableName, parentColumn, sortIndexColumn, idColumn, idColumn, includeAudTable, 0);
	}

    /**
     * Returns an SortIndexUpdater that updates an existing sort index which might have missing sortindex numbers in between.
     */
    public static final SortIndexUpdater NewUpdateExistingSortindexInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String parentColumn, String sortIndexColumn, boolean includeAudTable){
        return new SortIndexUpdater(stepList, stepName, tableName, parentColumn, sortIndexColumn, "id", sortIndexColumn, includeAudTable, 0);
    }

	protected SortIndexUpdater(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String parentColumn, String sortIndexColumn, String idColumn, String currentSortColumn, boolean includeAudTable, Integer baseValue) {
		super(stepList, stepName);
		this.tableName = tableName;
		this.parentColumn = parentColumn;
		this.sortIndexColumn = sortIndexColumn;
		this.idColumn = idColumn;
		this.currentSortColumn = currentSortColumn;
		this.includeAudTable = includeAudTable;
		this.baseValue = baseValue;
	}

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {
		addColumn(tableName, datasource, result);
		if (includeAudTable){
			String aud = "_AUD";
			addColumn(caseType.transformTo(tableName + aud), datasource, result);
		}
		return;
	}

	private void addColumn( String tableName, ICdmDataSource datasource, SchemaUpdateResult result) throws SQLException {
		//Note: caseType not required here
	    Map<Integer, Set<Integer>> indexMap ;
	    if (tableName == null){
	        tableName = this.tableName;
	    }
	    indexMap = makeIndexMap(tableName, datasource);
	    updateIndices(tableName, datasource, indexMap);

		return;
	}

	public String createIndexMapQuery(){
	       String resultsetQuery = "SELECT @id as id, @parentColumn " +
	                " FROM @tableName " +
	                " WHERE @parentColumn IS NOT NULL " +
	                " ORDER BY @parentColumn, @sorted";
	        resultsetQuery = resultsetQuery.replace("@id", idColumn);
	        resultsetQuery = resultsetQuery.replace("@tableName", tableName);
	        resultsetQuery = resultsetQuery.replace("@parentColumn", parentColumn);
	        resultsetQuery = resultsetQuery.replace("@sorted", currentSortColumn);
	        return resultsetQuery;
	}

	/**
	 * For each (new) sortIndex value the according record ids are computed.
	 * This allows updating all records sortindex by sortindex.
	 * @param tableName
	 * @param datasource
	 * @return
	 * @throws SQLException
	 */
	private Map<Integer, Set<Integer>> makeIndexMap(String tableName, ICdmDataSource datasource) throws NumberFormatException, SQLException {
	    String resultsetQuery = createIndexMapQuery();

		ResultSet rs = datasource.executeQuery(resultsetQuery);
		List<Integer[]> result = new ArrayList<>();
		while (rs.next()){
		    int id = rs.getInt("id");
            Object oParentId = rs.getObject(parentColumn);
            if (oParentId != null){
                int parentId = Integer.valueOf(oParentId.toString());
                result.add(new Integer[]{id,parentId});
            }
		}
        return makeIndexMap( result);

		//increase index with each row, set to 0 if parent is not the same as the previous one
	}

	public Map<Integer, Set<Integer>> makeIndexMap(List<Integer[]> oldIndexMap) throws NumberFormatException{
	    int oldParentId = -1;
	    Integer index = baseValue;
	    Map<Integer, Set<Integer>> indexMap = new HashMap<>();
        for (Integer[] entry: oldIndexMap){
            int id = entry[0];
            Integer oParentId = entry[1];
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
//          System.out.println(oParentId + "," + id+","+ index+";");
        }
        return indexMap;
	}

	public String createUpdateIndicesQuery(String tableName, Integer index, String idSetString){
	    if (tableName == null){
	        tableName = this.tableName;
	    }
	    String updateQuery = "UPDATE @tableName SET @sortIndexColumn = @index WHERE @id IN (@idList) ";
        updateQuery = updateQuery.replace("@tableName", tableName);
        updateQuery = updateQuery.replace("@sortIndexColumn", sortIndexColumn);
        updateQuery = updateQuery.replace("@index", index.toString());
        updateQuery = updateQuery.replace("@idList", idSetString);
        updateQuery = updateQuery.replace("@id", idColumn);
        return updateQuery;
	}

	private void updateIndices(String tableName, ICdmDataSource datasource, Map<Integer, Set<Integer>> indexMap)
			throws SQLException {
	    String updateQuery ;
		for (Integer index :  indexMap.keySet()){
			Set<Integer> set = indexMap.get(index);
			String idSetString = makeIdSetString(set);

			updateQuery = createUpdateIndicesQuery(tableName, index, idSetString);
			datasource.executeUpdate(updateQuery);
		}
	}

	public static String makeIdSetString(Set<Integer> set) {
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

	public String getChildrenCountQuery(){

        String countSelect = "SELECT COUNT(child.id) as realCount, parent.countChildren as countChildren, parent.id as parentID FROM @tableName child RIGHT JOIN @tableName parent ON child.parent_id = parent.id GROUP BY parent.id";
        countSelect = countSelect.replace("@tableName", tableName);
        return countSelect;
	}

	public String getUpdateChildrenCountQuery(int count, int id){
        String countUpdate =
                  " UPDATE @tableName "
                + " SET countChildren = " + count
                + " WHERE id = " + id;
        countUpdate = countUpdate.replace("@tableName", tableName);
        return countUpdate;
    }

	private void updateChildrenCount(String select, ICdmDataSource datasource) throws SQLException{

	    ResultSet rs = datasource.executeQuery(select);

        int count ;
        int countChildren;
        int parentId;
        while (rs.next()){
            count = rs.getInt("realCount");
            countChildren = rs.getInt("countChildren");
            parentId = rs.getInt("parentID");
            if (count != countChildren){
                String updateQuery = getUpdateChildrenCountQuery(count, parentId);
                datasource.executeUpdate(updateQuery);
            }
        }
	}

    public String getTableName() {
        return tableName;
    }
}