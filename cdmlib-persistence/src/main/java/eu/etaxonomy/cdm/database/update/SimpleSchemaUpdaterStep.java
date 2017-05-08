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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * This class represents one step in a schema update.
 * @author a.mueller
 * @date 13.09.2010
 *
 */
public class SimpleSchemaUpdaterStep extends SchemaUpdaterStepBase<SimpleSchemaUpdaterStep>
                implements ISchemaUpdaterStep, ITermUpdaterStep{
	private static final Logger logger = Logger.getLogger(SimpleSchemaUpdaterStep.class);

	private final Map<DatabaseTypeEnum, String> queryMap = new HashMap<DatabaseTypeEnum, String>();
	private final Map<DatabaseTypeEnum, String> auditQueryMap = new HashMap<DatabaseTypeEnum, String>();

	private boolean includeAudit = false;
//	private String tableName;

// *************************** FACTORY ********************************/

	/**
	 * Deprecated method
	 * @deprecated use  {@link #NewNonAuditedInstance(String, String)},
	 * {@link #NewAuditedInstance(String, String, boolean, String)},
	 * or {@link #NewExplicitAuditedInstance(String, String, String)} instead
	 */
	@Deprecated
	public static SimpleSchemaUpdaterStep NewInstance(String stepName, String defaultQuery, int adapt){
		return new SimpleSchemaUpdaterStep(stepName, defaultQuery, false, null, null);
	}

	/**
     * Simple schema updater with update query only for non_AUD tables.
	 *
	 * @param stepName step name
	 * @param defaultQuery the query
	 * @param adapt preliminary
	 * @return
	 */
	public static SimpleSchemaUpdaterStep NewNonAuditedInstance(String stepName, String defaultQuery, int adapt){
		return new SimpleSchemaUpdaterStep(stepName, defaultQuery, false, null, null);
	}

	/**
	 * Simple schema updater with update query for AUD and non_AUD tables.
     *
     * @param stepName Step name
	 * @param defaultQuery query
	 * @param nonAuditedTableName the name of the non audited table. E.g. TaxonName
	 *     (while TaxonName_AUD is the audited table
	 * @param adapt preliminary
	 * @return
	 */
	public static SimpleSchemaUpdaterStep NewAuditedInstance(String stepName, String defaultQuery, String nonAuditedTableName, int adapt){
		boolean audit = StringUtils.isNotBlank(nonAuditedTableName);
		return new SimpleSchemaUpdaterStep(stepName, defaultQuery, audit, nonAuditedTableName, null);
	}

	/**
	 * Simple schema updater with an explicit query for AUD table.
	 * @param stepName step name
	 * @param defaultQuery the non_AUD update query
	 * @param defaultQueryForAuditedTables the AUD update query
	 * @param adapt preliminary
	 * @return
	 */
	public static SimpleSchemaUpdaterStep NewExplicitAuditedInstance(String stepName, String defaultQuery, String defaultQueryForAuditedTables, int adapt){
		boolean audit = StringUtils.isNotBlank(defaultQueryForAuditedTables);
		return new SimpleSchemaUpdaterStep(stepName, defaultQuery, audit, null, defaultQueryForAuditedTables);
	}


//************************ CONSTRUCTOR ***********************************/
	private SimpleSchemaUpdaterStep(String stepName, String defaultQuery, boolean includeAudit, String tableName, String defaultQueryForAuditedTables){
		super(stepName);
		this.includeAudit = includeAudit;
		queryMap.put(null, defaultQuery);

		if (includeAudit){
			if (StringUtils.isNotBlank(defaultQueryForAuditedTables)){
				auditQueryMap.put(null, defaultQueryForAuditedTables);
			}else if (StringUtils.isNotBlank(tableName)){
				setDefaultAuditing(tableName);
			}
		}
	}

// *************************** INVOKE *****************************



	@Override
	public Integer invoke (ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType){
		boolean result = true;

		//non audit
		result &= invokeQueryMap(datasource, queryMap, caseType); ;
		//audit
		if (this.includeAudit){
			result &= invokeQueryMap(datasource, auditQueryMap, caseType);
		}else{
			logger.info("SimpleSchemaUpdaterStep non Audited");
		}

		return (result == true )? 0 : null;
	}

	private boolean invokeQueryMap(ICdmDataSource datasource, Map<DatabaseTypeEnum, String> queryMap, CaseType caseType) {
		boolean result = true;
		String query = queryMap.get(datasource.getDatabaseType());
		if (query == null){
			query = queryMap.get(null);
		}
		if (query != null){
			query = doReplacements(query, caseType, datasource);
			result = executeQuery(datasource, query);
		}else{
			//TODO exception ?
			logger.warn("No query found to execute");
		}
		return result;
	}

	private String doReplacements(String query, CaseType caseType, ICdmDataSource datasource) {
		query = caseType.replaceTableNames(query);
		query = query.replaceAll("@FALSE@", getBoolean(false, datasource));
		query = query.replaceAll("@TRUE@", getBoolean(true, datasource));
		return query;
	}

	private boolean executeQuery(ICdmDataSource datasource,  String replacedQuery) {
		try {
			datasource.executeUpdate(replacedQuery);
		} catch (SQLException e) {
			logger.error(e);
			return false;
		}
		return true;
	}

	private void makeAuditedQuery(DatabaseTypeEnum dbType, String tableName, boolean addTable){
		String auditQuery = addTable? auditQueryMap.get(dbType) : queryMap.get(dbType);
		if (StringUtils.isBlank(auditQuery)){
			throw new IllegalArgumentException("Non-audit query must not be blank");
		}
	    auditQuery = auditQuery.replace("@@" + tableName + "@@", "@@" + tableName + "_AUD@@");
		//TODO warning if nothing changed
		this.auditQueryMap.put(dbType, auditQuery);
		this.includeAudit = true;
	}

//********************************* DELEGATES *********************************/

	/**
	 * For certain database types one may define special queries.<BR>
	 * Don't forget to put case-mask (@@) for table names and also
	 * add AUD query if required.
	 * @param dbType database type
	 * @param query query to use for the given database type.
	 * @return this schema updater step
     * @see #putAudited(DatabaseTypeEnum, String)
	 */
	public SimpleSchemaUpdaterStep put(DatabaseTypeEnum dbType, String query) {
		queryMap.put(dbType, query);
		return this;
	}

	/**
     * For certain database types one may define special queries.
     * This is for the AUD query.<BR>
     * Don't forget to put case-mask (@@) for table names
     * @param dbType database type
     * @param query query to use for the given database type.
     * @return this schema updater step
     * @see #put(DatabaseTypeEnum, String)
     */
    public SimpleSchemaUpdaterStep putAudited(DatabaseTypeEnum dbType, String query) {
        auditQueryMap.put(dbType, query);
        return this;
    }

	/**
	 * Defines the non audited table name for computing the audited query.
	 * @param nonAuditedTableName uncased table name that is to be audited
	 * @return the step
	 */
	public SimpleSchemaUpdaterStep setDefaultAuditing(String nonAuditedTableName){
		if (StringUtils.isBlank(nonAuditedTableName)){
			throw new IllegalArgumentException("TableName must not be blank");
		}
		makeAuditedQuery(null, nonAuditedTableName, false);
		return this;
	}

	 /**
     * Defines a further non audited table name for computing the audited query.
     * Requires at least one non audited table name to be defined already.
     * @param nonAuditedTableName non-cased table name that is to be audited
     * @return the step
     */
    public SimpleSchemaUpdaterStep addDefaultAuditing(String nonAuditedTableName){
        if (StringUtils.isBlank(nonAuditedTableName)){
            throw new IllegalArgumentException("TableName must not be blank");
        }
        makeAuditedQuery(null, nonAuditedTableName, true);
        return this;
    }

}
