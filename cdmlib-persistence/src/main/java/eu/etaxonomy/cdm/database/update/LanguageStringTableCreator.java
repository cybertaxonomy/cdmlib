/**
 *
 */
package eu.etaxonomy.cdm.database.update;

/**
 * @author a.mueller
 *
 */
public class LanguageStringTableCreator extends MapTableCreator {

	public static LanguageStringTableCreator NewLanguageStringInstance(String stepName, String tableName, String attributeName, boolean includeAudTable){
		LanguageStringTableCreator result = new LanguageStringTableCreator(stepName, tableName, tableName, attributeName, includeAudTable);
		return result;
	}


	protected LanguageStringTableCreator(String stepName, String firstTableName, String firstTableAlias, String attributeName, boolean includeAudTable) {
		super(stepName, firstTableName, firstTableAlias, "LanguageString", attributeName, "DefinedTermBase", includeAudTable);
	}

//	@Override
//    protected String primaryKey(boolean isAudit){
//		String result = "";
//		if (! isAudit){
//			result = getFirstIdColumn() + ",";
//			result += getMapKeyColumn() + ",";
//		}else{
//			result = "REV, " + primaryKey(false) + ",";
//			result += getSecondIdColumn() + ",";
//		}
//		result = StringUtils.chomp(result.trim(), ",");
//		return result;
//	}

}
