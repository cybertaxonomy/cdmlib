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
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @since 13.09.2010
 */
public abstract class SchemaUpdaterStepBase implements ISchemaUpdaterStep {
	private static final Logger logger = Logger.getLogger(SchemaUpdaterStepBase.class);

	protected String stepName;

	private boolean ignoreErrors;

//************************ CONSTRUCTOR ***********************************/

	protected <T extends ISchemaUpdaterStep> SchemaUpdaterStepBase(List<T> stepList, String stepName){
		this.setStepName(stepName);
		if (stepList != null){
		    stepList.add((T)this);
		}
	}

	@Override
	public abstract void invoke (ICdmDataSource datasource, IProgressMonitor monitor,
	        CaseType caseType, SchemaUpdateResult result) throws SQLException;

	@Override
	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

	@Override
	public String getStepName() {
		return stepName;
	}

	protected String getBoolean(boolean value, ICdmDataSource datasource) {

		String result;
		DatabaseTypeEnum type = datasource.getDatabaseType();
		//TODO use
//		type.getHibernateDialect().toBooleanValueString(bool);
		int intValue = value == true? 1 : 0;
		if (type.equals(DatabaseTypeEnum.MySQL)){
			result = "b'"+intValue+"'";
		}else if (type.equals(DatabaseTypeEnum.PostgreSQL)){
			result = "'"+intValue+"'";
		}else if (type.equals(DatabaseTypeEnum.H2)){
			result = value == true ? "TRUE" : "FALSE";
		}else if (type.equals(DatabaseTypeEnum.SqlServer2005)){
			logger.warn("SQLServer boolean not tested yet");
			result = "b'"+intValue+"'";
		}else{
			throw new RuntimeException("Database type not supported for boolean" + type.getName());
		}
		return result;
	}

	protected Integer getEnglishLanguageId(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		return getLanguageId(Language.uuidEnglish, datasource, monitor, caseType);
	}

	protected Integer getLanguageId(UUID uuidLanguage, ICdmDataSource datasource,
	        IProgressMonitor monitor, CaseType caseType) throws SQLException {

		ResultSet rs;
		Integer langId = null;
		String sqlLangId = " SELECT id FROM %s WHERE uuid = '%s'";
		sqlLangId = String.format(sqlLangId, caseType.transformTo("DefinedTermBase"), uuidLanguage.toString() );
		rs = datasource.executeQuery(sqlLangId);
		if (rs.next()){
			langId = rs.getInt("id");
		}else{
			String warning = "Term for language (" +  uuidLanguage + ") does not exist!";
			monitor.warning(warning);
		}
		return langId;
	}

    /**
     * Returns the smallest next free id, if includeAudit is <code>true</code> the audit
     * table is also considered in computation
     * @throws NumberFormatException
     * @throws SQLException
     */
    protected int getMaxId1(ICdmDataSource datasource, String tableName, boolean includeAudit, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {

        return getMaxIdentifier(datasource, tableName, "id", includeAudit, monitor, caseType, result);
    }

    protected int getMaxIdentifier(ICdmDataSource datasource, String tableName, String idAttrName, boolean includeAudit, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {

        String sql = "SELECT max("+idAttrName+") FROM " +caseType.transformTo(tableName);
        Integer maxId = getInteger(datasource, sql, 0);

        Integer maxIdAud = -1;
        if(includeAudit){
            sql = "SELECT max("+idAttrName+") FROM " +caseType.transformTo(tableName + "_AUD");
            maxIdAud = getInteger(datasource, sql, 0);
        }
        return Math.max(maxId, maxIdAud) + 1;
    }


    /**
     * Creates a new entry in the AuditEvent table
     * @return the revision number of the the new audit event
     */
    protected long createAuditEvent(ICdmDataSource datasource, CaseType caseType, IProgressMonitor monitor,
            SchemaUpdateResult result) throws SQLException {
        String sql;
        long rev;
        sql = "INSERT INTO @@AuditEvent@@ (revisionnumber, date, timestamp, uuid) "
                + " VALUES (%d, '%s', %d, '%s') ";
        int newId = this.getMaxIdentifier(datasource, "AuditEvent", "revisionNumber", false, monitor, caseType, result);
        long timeStamp = System.currentTimeMillis();
        sql = caseType.replaceTableNames(String.format(sql, newId, this.getNowString(), timeStamp, UUID.randomUUID()));
        datasource.executeUpdate(sql);
        rev =  newId;
        return rev;
    }

    private Integer getInteger(ICdmDataSource datasource, String sql, int nullReplace) throws SQLException {
        Object value = datasource.getSingleValue(sql);
        if (value == null){
            return nullReplace;
        }else{
            return Integer.valueOf(value.toString());
        }
    }

    @Override
	public List<ISchemaUpdaterStep> getInnerSteps(){
		return new ArrayList<>();
	}

	@Override
	public boolean isIgnoreErrors() {
		return ignoreErrors;
	}
	@Override
	public void setIgnoreErrors(boolean ignoreErrors) {
		this.ignoreErrors = ignoreErrors;
	}


	/**
	 * Returns a time string with date and time (without millis) that
	 * can be used as a time string for database insert and update
	 * @return
	 */
	protected String getNowString() {
		return DateTime.now().toString("YYYY-MM-dd HH:mm:ss");
	}

    protected String nullSafeParam(String param) {
        return param == null ? "NULL" : "'" + param.replace("'", "''") + "'";
    }

    protected Integer nullSafeInt(ResultSet rs, String columnName) throws SQLException {
        Object intObject = rs.getObject(columnName);
        if (intObject == null){
            return null;
        }else{
            return Integer.valueOf(intObject.toString());
        }
    }

    protected boolean isNotBlank(String str) {
        return StringUtils.isNotBlank(str);
    }

    protected boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }

	@Override
	public String toString(){
		if (StringUtils.isNotBlank(stepName)){
			return stepName;
		}else{
			return super.toString();
		}
	}

}
