/**
 * 
 */
package eu.etaxonomy.cdm.database.update;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

/**
 * @author a.mueller
 *
 */
public class MapTableCreator extends TableCreator {

	private String firstTableName;
	private String firstTableAlias;
	private String secondTableAlias;
	private String secondTableName;
	private String mapKeyTableName;
	
	public static MapTableCreator NewMapTableInstance(String stepName, String firstTableName, String firstTableAlias, String secondTableName, String secondTableAlias, String mapKeyTableName, boolean includeAudTable){
		MapTableCreator result = new MapTableCreator(stepName, firstTableName, firstTableAlias, secondTableName, secondTableAlias, mapKeyTableName, includeAudTable);
		return result;
	}

	
	protected MapTableCreator(String stepName, String firstTableName, String firstTableAlias, String secondTableName, String secondTableAlias, String mapKeyTableName, boolean includeAudTable) {
		super(stepName, firstTableName + "_" + secondTableName, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<Object>(), new ArrayList<Boolean>(), new ArrayList<String>(), includeAudTable, false, false, false);
		this.firstTableName = firstTableName;
		this.secondTableName = secondTableName;
		this.firstTableAlias = (firstTableAlias != null )? firstTableAlias : firstTableName ;
		this.secondTableAlias = (secondTableAlias !=  null)? secondTableAlias : secondTableName ;
		this.mapKeyTableName = mapKeyTableName;
		addMyColumns();
	}

	
	protected void addMyColumns(){
		this.columnAdders.add(ColumnAdder.NewIntegerInstance(stepName, tableName, getFirstIdColumn(), false, true, firstTableName));
		this.columnAdders.add(ColumnAdder.NewIntegerInstance(stepName, tableName, getSecondIdColumn(), false, true, secondTableName));
		this.columnAdders.add(ColumnAdder.NewIntegerInstance(stepName, tableName, getMapKeyColumn(), false, true, mapKeyTableName));
	}

	protected String primaryKey(boolean isAudit){
		String result = "";
		if (! isAudit){
			result = getFirstIdColumn() + ",";
			result += getMapKeyColumn() + ",";
		}else{
			result = "REV, " + primaryKey(false) + ",";
			result += getSecondIdColumn() + ",";
		}
		result = StringUtils.chomp(result.trim(), ",");
		return result;
	}
	
	protected String unique(boolean isAudit){
		if (! isAudit){
			return getSecondIdColumn();
		}else{
			return null;
		}
	}
	
	private String getFirstIdColumn(){
		return this.firstTableAlias + "_id";
	}
	
	private String getSecondIdColumn(){
		String result = this.secondTableAlias.toLowerCase();
		
		if (this.secondTableAlias.equalsIgnoreCase(this.secondTableName) ){
			if (! result.endsWith("s")){
				result += "s";
			}
		}
		result += "_id";
		return result;
	}
	
	private String getMapKeyColumn(){
		String result = getSecondIdColumn();
		result = result.replace("_id", "_mapkey_id");
		return result;
	}
}
