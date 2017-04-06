/**
 *
 */
package eu.etaxonomy.cdm.database.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Creates an MN table
 *
 * @author a.mueller
 */
public class MnTableCreator extends TableCreator {

	private String firstTableName;
	private String firstTableAlias;
	private String secondTableName;
	private String secondTableAlias;
	//is a sort index column needed.
	private boolean hasSortIndex;
	//is the key of the MN table including the FK to the second table?
	private boolean secondTableInKey;

	public static MnTableCreator NewMnInstance(String stepName, String firstTableName, String secondTableName, boolean includeAudTable){
		MnTableCreator result = new MnTableCreator(stepName, firstTableName, null, secondTableName, null, new String[]{}, new String[]{}, null, null, includeAudTable, false, true, false, false, false);
		return result;
	}

	public static MnTableCreator NewMnInstance(String stepName, String firstTableName, String firstTableAlias, String secondTableName, String secondTableAlias, boolean includeAudTable, boolean hasSortIndex, boolean secondTableInKey){
		MnTableCreator result = new MnTableCreator(stepName, firstTableName, firstTableAlias, secondTableName, secondTableAlias, new String[]{}, new String[]{}, null, null, includeAudTable, hasSortIndex, secondTableInKey, false, false, false);
		return result;
	}


	protected MnTableCreator(String stepName, String firstTableName, String firstTableAlias, String secondTableName, String secondTableAlias, String[] columnNames, String[] columnTypes,
			List<Object> defaultValues, List<Boolean> isNull, boolean includeAudTable, boolean hasSortIndex, boolean secondTableInKey, boolean includeCdmBaseAttributes,boolean includeAnnotatableEntity, boolean includeIdentifiableEntity) {
		super(stepName, firstTableName + "_" + secondTableName, Arrays.asList(columnNames), Arrays.asList(columnTypes), defaultValues, isNull,	new ArrayList<String>(), includeAudTable, includeCdmBaseAttributes, includeAnnotatableEntity, includeIdentifiableEntity, false);
		this.firstTableName = firstTableName;
		this.secondTableName = secondTableName;
		this.firstTableAlias = (firstTableAlias != null )? firstTableAlias : firstTableName ;
		this.secondTableAlias = (secondTableAlias !=  null)? secondTableAlias : secondTableName ;
		this.hasSortIndex = hasSortIndex;
		this.secondTableInKey = secondTableInKey;
		addMyColumns();
	}


	protected void addMyColumns(){
		this.columnAdders.add(ColumnAdder.NewIntegerInstance(stepName, tableName, getFirstIdColumn(), false, true, this.firstTableName));
		this.columnAdders.add(ColumnAdder.NewIntegerInstance(stepName, tableName, getSecondIdColumn(), false, true, this.secondTableName));
		if (this.hasSortIndex){
			this.columnAdders.add(ColumnAdder.NewIntegerInstance(stepName, tableName, "sortIndex", false, true, null));
		}

	}

	@Override
    protected String primaryKey(boolean isAudit){
		String result = "";
		if (! isAudit){
			result = getFirstIdColumn() + ",";
			result += secondTableInKey ? getSecondIdColumn() + "," : "";
			result += hasSortIndex ? "sortIndex," : "";
		}else{
			result = "REV, " + primaryKey(false);
			result += (!secondTableInKey) ? ","+getSecondIdColumn() + "," : "";
		}
		result = StringUtils.chomp(result.trim(), ",");
		return result;
	}

	@Override
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
}
