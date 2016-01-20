/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * Tables may be stored in different cases: CamelCase (preferred), upper case (capital), or lower case,
 * depending on the database preferences.
 * This enumeration is defining the case used.
 *
 * @author a.mueller
 *
 */
public enum CaseType {
	CamelCase,
	UpperCase,
	LowerCase;

	/**
	 * Transforms the camel case table name to the required case
	 * @param camelCaseTableName
	 * @return the transformed table name
	 */
	public String transformTo(String camelCaseTableName){
		if (camelCaseTableName == null){
			return null;
		}else if (this == CamelCase){
			return camelCaseTableName;
		}else if (this == UpperCase){
			return camelCaseTableName.toUpperCase(Locale.ENGLISH);
		}else if (this == LowerCase){
			return camelCaseTableName.toLowerCase(Locale.ENGLISH);
		}else{
			throw new RuntimeException("Unhandled CaseType: " + this);
		}
	}


    /**
     * Defines the CaseType (camel, upper, lower) of a datasource depending on the case used for the CdmMetaData table.
     * @param datasource the datasource
     * @return the CaseType used
     */
    public static CaseType caseTypeOfDatasource(ICdmDataSource datasource) {
		String sql = "SELECT value FROM ";

    	try {
			datasource.executeQuery(sql +  "CdmMetaData");
			return CaseType.CamelCase;
		} catch (SQLException e) {
			try {
				datasource.executeQuery(sql+  "CDMMETADATA");
				return CaseType.UpperCase;
			} catch (SQLException e1) {
				try {
					datasource.executeQuery(sql+ "cdmmetadata");
				} catch (SQLException e2) {
					throw new RuntimeException("Case type (camel, upper, lower) of the database could be defined. Maybe the CdmMetaData table is missing in the datasource", e2);
				}
				return CaseType.LowerCase;
			}
		}
	}


	/**
	 * Replaces all words marked with @@ at the beginning and end by the correctly cased name.
	 * E.g. it replaces <i>@@CdmMetaData@@</i> by <i>cdmmetadata</i> if {@link CaseType} is
	 * {@link CaseType#LowerCase}
	 * @param sql the original sql string with masked table names
	 * @return the corrected sql string
	 */
	public String replaceTableNames(String sql) {
		Pattern pattern = Pattern.compile("@@[a-zA-Z_]+@@");

		Matcher matcher = pattern.matcher(sql);
		while (matcher.find()){
			String newName = transformTo(matcher.group().replaceAll("@", ""));
			sql = sql.replace(matcher.group(), newName);
			matcher = pattern.matcher(sql);
		}

		return sql;
	}

}
