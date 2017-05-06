/**
 *
 */
package eu.etaxonomy.cdm.database.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Creates an MN table
 *
 * @author a.mueller
 */
public class MnTableCreator extends TableCreator {

	private String firstTableName;
	private String firstTableAlias;
    private String firstColumnName;
	private String secondTableName;
	private String secondTableAlias;
    private String secondColumnName;
	//is the MN table used for a list, if yes, a sortIndex column is needed
    //and the and the sortindex column needs to be in the key instead of second table column.
	private boolean isList;
	private boolean is1toM;

	public static MnTableCreator NewMnInstance(String stepName, String firstTableName, String secondTableName, boolean includeAudTable, boolean isList, boolean is1toM){
		MnTableCreator result = new MnTableCreator(stepName, firstTableName, null, null, secondTableName, null, null, new String[]{}, new String[]{}, null, null, includeAudTable, isList, is1toM, false, false, false);
		return result;
	}

	/**
	 *
	 * @param stepName The step name
	 * @param firstTableName The name of the first table
	 * @param firstTableAlias The alias for the first table as used in the MN table name
	 * @param secondTableName The name of the second table
	 * @param secondTableAlias The alias for the second table as used in the MN table name
	 * @param attributeName The name of the attribute pointing to the second table (this is used for the column name for the
	 * column pointing to the second table)
	 * @param includeAudTable <code>true</code> if also the Audit (_AUD) table should be created
	 * @param hasSortIndex by default <code>false</code> but true for {@link Map maps} (or maybe user defined MN-tables)
	 * @param secondTableInKey should the column that links to the second table also be in the key? This is by default
	 * <code>true</code> but for {@link List lists} should be <code>false</code>.
	 * @return
	 */
	public static MnTableCreator NewMnInstance(String stepName, String firstTableName, String firstTableAlias, String secondTableName, String secondTableAlias, String attributeName,
	        boolean includeAudTable, boolean isList, boolean is1toM){
		MnTableCreator result = new MnTableCreator(stepName, firstTableName, firstTableAlias, null, secondTableName, secondTableAlias, attributeName,
		        new String[]{}, new String[]{}, null, null,
		        includeAudTable, isList, is1toM, false, false, false);
		return result;
	}

	   /**
    *
    * @param stepName The step name
    * @param firstTableName The name of the first table
    * @param firstTableAlias The alias for the first table as used in the MN table name
    * @param firstColumnName The name of the attribute pointing to the first table (this is used for the column name for the
    *     column pointing to the first table)
    * @param secondTableName The name of the second table
    * @param secondTableAlias The alias for the second table as used in the MN table name
    * @param secondColumnName The name of the attribute pointing to the second table (this is used for the column name for the
    *    column pointing to the second table)
    * @param includeAudTable <code>true</code> if also the Audit (_AUD) table should be created
    * @param hasSortIndex by default <code>false</code> but true for {@link Map maps} (or maybe user defined MN-tables)
    * @param secondTableInKey should the column that links to the second table also be in the key? This is by default
    * <code>true</code> but for {@link List lists} should be <code>false</code>.
    * @return
    */
   public static MnTableCreator NewMnInstance(String stepName, String firstTableName, String firstTableAlias, String firstColumnName, String secondTableName, String secondTableAlias, String secondColumnName,
           boolean includeAudTable, boolean isList, boolean is1toM){
       MnTableCreator result = new MnTableCreator(stepName, firstTableName, firstTableAlias, firstColumnName, secondTableName, secondTableAlias, secondColumnName,
               new String[]{}, new String[]{}, null, null,
               includeAudTable, isList, is1toM, false, false, false);
       return result;
   }

// ****************************** CONSTRUCTOR *********************************/

	protected MnTableCreator(String stepName, String firstTableName, String firstTableAlias, String firstColumnName, String secondTableName, String secondTableAlias, String secondColumnName,
	        String[] columnNames, String[] columnTypes, List<Object> defaultValues, List<Boolean> isNull,
	        boolean includeAudTable, boolean isList, boolean is1toM,
	        boolean includeCdmBaseAttributes, boolean includeAnnotatableEntity, boolean includeIdentifiableEntity) {
		super(stepName, makeAlias(firstTableName, firstTableAlias) + "_" + makeAlias(secondTableName, secondTableAlias),
		        Arrays.asList(columnNames), Arrays.asList(columnTypes), defaultValues,
		        isNull,	new ArrayList<>(), includeAudTable,
		        includeCdmBaseAttributes, includeAnnotatableEntity, includeIdentifiableEntity, false);
		this.firstTableName = firstTableName;
		this.secondTableName = secondTableName;
		this.firstTableAlias = makeAlias(firstTableName, firstTableAlias) ;
		this.secondTableAlias = makeAlias(secondTableName, secondTableAlias) ;
		this.firstColumnName = (firstColumnName !=  null) ? firstColumnName : this.firstTableAlias;
        this.secondColumnName = (secondColumnName !=  null) ? secondColumnName : this.secondTableAlias;
        this.isList = isList;
        this.is1toM = is1toM;
		addMyColumns();
	}

    /**
     * @param secondTableName
     * @param secondTableAlias
     * @return
     */
    private static String makeAlias(String tableName, String alias) {
        return (alias !=  null) ? alias : tableName;
    }


	protected void addMyColumns(){
	    ColumnAdder firstColAdder = ColumnAdder.NewIntegerInstance(stepName, tableName, getFirstIdColumn(), false, true, this.firstTableName);
		this.columnAdders.add(firstColAdder);
		ColumnAdder secondColAdder = ColumnAdder.NewIntegerInstance(stepName, tableName, getSecondIdColumn(), false, true, this.secondTableName);
//		secondColAdder.addIndex(tableName+"_"+getSecondIdColumn(), null);
		this.columnAdders.add(secondColAdder);
		if (this.isList){
			this.columnAdders.add(ColumnAdder.NewIntegerInstance(stepName, tableName, "sortIndex", false, true, null));
		}
	}

	@Override
    protected String primaryKey(boolean isAudit){
		String result = "";
		if (! isAudit){
			result = getFirstIdColumn() + ",";
			result += (isList ? "sortIndex" : getSecondIdColumn());
		}else{
			result = "REV, " + primaryKey(false);
			//for AUDIT also the second table column is in PK
			result += (isList) ? ","+getSecondIdColumn() : "";
		}
		return result;
	}

	@Override
    protected String unique(boolean isAudit){
		if (! isAudit && is1toM){
			return getSecondIdColumn();
		}else{
			return null;
		}
	}

	private String getFirstIdColumn(){
		return this.firstColumnName + "_id";
	}

	private String getSecondIdColumn(){
		String result = this.secondColumnName.toLowerCase();

		if (this.secondColumnName.equalsIgnoreCase(this.secondTableName) ){
			if (! result.endsWith("s")){
				result += "s";
			}
		}
		result += "_id";
		return result;
	}
}
