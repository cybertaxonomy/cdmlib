/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 24.02.2010
 * @version 1.0
 */
public class DbImportTruncatedStringMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, CdmBase>{
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportTruncatedStringMapper.class);
	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 * @return
	 */
	public static DbImportTruncatedStringMapper NewInstance (String dbAttributeString, String cdmAttributeString, String longTextAttribute) {
		boolean obligatory = false;
		Object defaultValue = null;
		return new DbImportTruncatedStringMapper(dbAttributeString, cdmAttributeString, defaultValue, obligatory, longTextAttribute);
	}
	
	
//	/**
//	 * @param cdmAttributeString
//	 * @param dbAttributString
//	 * @param defaultValue
//	 */
//	public static DbImportTruncatedStringMapper NewInstance (String dbAttributString, String cdmAttributeString, Object defaultValue) {
//		boolean obligatory = false;
//		return new  DbImportTruncatedStringMapper(dbAttributString, cdmAttributeString, defaultValue, obligatory);
//	}
//
//	/**
//	 * @param cdmAttributeString
//	 * @param dbAttributString
//	 * @param defaultValue
//	 */
//	public static DbImportTruncatedStringMapper NewInstance (String dbAttributeString, String cdmAttributeString, Object defaultValue, boolean obligatory) {
//		return new  DbImportTruncatedStringMapper(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
//	}

	private String longTextAttribute;
	private int truncatedLength = 255;
	private Method longTextMethod;
	
	/**
	 * @param cdmAttributeString
	 * @param dbAttributString
	 * @param defaultValue
	 */
	protected DbImportTruncatedStringMapper(String dbAttributeString, String cdmAttributeString, Object defaultValue, boolean obligatory, String longTextAttribute) {
		super(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
		this.longTextAttribute = longTextAttribute;
	}
	
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbSingleAttributeImportMapperBase#initialize(eu.etaxonomy.cdm.io.common.DbImportStateBase, java.lang.Class)
	 */
	@Override
	public void initialize(DbImportStateBase<?,?> state, Class<? extends CdmBase> destinationClass) {
		super.initialize(state, destinationClass);
		Class<?> parameterType = getTypeClass();
		String methodName = ImportHelper.getSetterMethodName(parameterType, longTextAttribute);
		try {
			longTextMethod = targetClass.getMethod(methodName, parameterType);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbSingleAttributeImportMapperBase#doInvoke(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	protected CdmBase doInvoke(CdmBase cdmBase, Object value) throws SQLException {
		CdmBase result;
		String strValue = (String)value;
		if (strValue != null && strValue.length()>255){
			return invokeTruncatedValue(cdmBase, value);
		}else{
			return super.doInvoke(cdmBase, value);
		}
	}




	/**
	 * @param cdmBase
	 * @param value
	 * @throws SQLException
	 */
	private CdmBase invokeTruncatedValue(CdmBase cdmBase, Object value) throws SQLException {
		CdmBase result;
		String truncatedValue = ((String)value).substring(0, truncatedLength - 3) + "...";
		result = super.doInvoke(cdmBase, truncatedValue);
		try {
			longTextMethod.invoke(cdmBase, value);
			return result;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Problems when invoking long text method");
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class getTypeClass() {
		return String.class;
	}
	

}
