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
 *
 */
public abstract class SchemaUpdaterStepBase implements ISchemaUpdaterStep {
	private static final Logger logger = Logger.getLogger(SchemaUpdaterStepBase.class);

	protected String stepName;

	private boolean ignoreErrors;


//************************ CONSTRUCTOR ***********************************/

	protected SchemaUpdaterStepBase(String stepName){
		this.setStepName(stepName);
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

	/**
	 * @param uuidLanguage
	 * @param datasource
	 * @param monitor
	 * @return
	 * @throws SQLException
	 */
	protected Integer getLanguageId(UUID uuidLanguage, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {

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
	 * {@inheritDoc}
	 */
	@Override
	public List<ISchemaUpdaterStep> getInnerSteps(){
		return new ArrayList<ISchemaUpdaterStep>();
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

	@Override
	public String toString(){
		if (StringUtils.isNotBlank(stepName)){
			return stepName;
		}else{
			return super.toString();
		}
	}

}
