/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common.mapping;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 26.02.2010
 */
public class DbImportObjectMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, CdmBase> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private String relatedObjectNamespace;

	public static DbImportObjectMapper NewInstance(String dbAttributString, String cdmAttributeString, String relatedObjectNamespace){
		return new DbImportObjectMapper(dbAttributString, cdmAttributeString, relatedObjectNamespace);
	}

	protected DbImportObjectMapper(String dbAttributString, String cdmAttributeString, String relatedObjectNamespace) {
		super(dbAttributString, cdmAttributeString);
		this.relatedObjectNamespace = relatedObjectNamespace;
	}

	@Override
	protected Object getValue(ResultSet rs) throws SQLException {
		Object result;
		Object dbValue = getDbValue(rs);
		String id = String.valueOf(dbValue);
		DbImportStateBase state = importMapperHelper.getState();
		result = state.getRelatedObject(relatedObjectNamespace, id);
		return result;
	}

	@Override
	public Class getTypeClass() {
		String getterMethodName = ImportHelper.getGetterMethodName(getDestinationAttribute(), false);
		Method method;
		try {
			method = targetClass.getMethod(getterMethodName, null);
		} catch (Exception e) {
			throw new RuntimeException("parameter type for DbImportObjectMapper could not be determined :"+  getterMethodName);
		}
		Class<?> returnType = method.getReturnType();
		return returnType;
	}
}