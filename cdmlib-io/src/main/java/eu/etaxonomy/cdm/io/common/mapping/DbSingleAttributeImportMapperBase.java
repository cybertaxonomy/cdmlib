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
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.DatabaseTypeNotSupportedException;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 12.05.2009
 * @version 1.0
 */
public abstract class DbSingleAttributeImportMapperBase<STATE extends DbImportStateBase<?,?>, CDM_BASE extends CdmBase> extends CdmSingleAttributeMapperBase implements IDbImportMapper<STATE, CDM_BASE>  {
	private static final Logger logger = Logger.getLogger(DbSingleAttributeImportMapperBase.class);
	
	protected DbImportMapperBase<STATE> importMapperHelper = new DbImportMapperBase<STATE>();
//	private Integer precision = null;
	protected boolean obligatory = true;
	protected boolean ignore = false;;

	protected Method destinationMethod = null;
	protected Class<?> targetClass;
	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	protected DbSingleAttributeImportMapperBase(String dbAttributString, String cdmAttributeString) {
		super(dbAttributString, cdmAttributeString);
	}
	
	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	protected DbSingleAttributeImportMapperBase(String dbAttributString, String cdmAttributeString, Object defaultValue) {
		super(dbAttributString, cdmAttributeString, defaultValue);
	}
	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	protected DbSingleAttributeImportMapperBase(String dbAttributeString, String cdmAttributeString, Object defaultValue, boolean obligatory) {
		super(dbAttributeString, cdmAttributeString, defaultValue);
		this.obligatory = obligatory;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper#initialize(eu.etaxonomy.cdm.io.common.DbImportStateBase, java.lang.String)
	 */
	public void initialize(STATE state, Class<? extends CdmBase> destinationClass) {
		importMapperHelper.initialize(state, destinationClass);
		try {
			targetClass = getTargetClass(destinationClass);
			Class<?> parameterType = getTypeClass();
			String methodName = getMethodName(parameterType);
			destinationMethod = targetClass.getMethod(methodName, parameterType);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * @param destinationClass
	 * @return
		 * @throws NoSuchMethodException 
		 * @throws SecurityException 
		 * @throws NoSuchMethodException 
	 */
	protected Class getTargetClass(Class<?> destinationClass) throws SecurityException, NoSuchMethodException{
		Class result = destinationClass;
		String destinationAttribute = getDestinationAttribute();
		if (destinationAttribute == null){
			return null;
		}
		String[] splits = destinationAttribute.split("\\.");
		//for all prefixes 
		for (int i = 0; i < splits.length - 1; i++){
			String split = splits[i];
			String castedResultClass = getCastedResultClass(split);
			split = removeCast(split);
			String methodName = ImportHelper.getGetterMethodName(split, false);
			Method getterMethod;
			try {
				getterMethod = result.getMethod(methodName, null);
			} catch (NoSuchMethodException e1) {
				throw e1;
			}
			result = getterMethod.getReturnType();
			if (castedResultClass != null){
				try {
					//casting works only if the subclass is in the same package
					String packageName = result.getPackage().getName();
					castedResultClass = packageName + "." + castedResultClass;
					result = Class.forName(castedResultClass);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					//result = null;
				} 
			}
		}
		return result;
	}


	/**
	 * @param split
	 * @return
	 */
	private String removeCast(String split) {
		int index = split.lastIndexOf(")");
		if (split.length() > index){
			split = split.substring(index + 1);
		}else{
			split = "";
		}
		return split;
	}


	/**
	 * @param split
	 * @return
	 */
	private String getCastedResultClass(String split) {
		String castString = null;
		split = split.trim();
		int index = split.lastIndexOf(")");
		if (split.startsWith("(") && index > -1 ){
			castString = split.substring(1, index);
			castString = castString.trim();
		}
		return castString;
	}


	/**
	 * @param clazz 
	 * @return
	 */
	private String getMethodName(Class clazz) {
		String cdmAttributeName = getTargetClassAttribute(getDestinationAttribute());
		String result = ImportHelper.getSetterMethodName(clazz, cdmAttributeName);
		return result;
	}


	/**
	 * @param destinationAttribute
	 * @return
	 */
	private String getTargetClassAttribute(String destinationAttribute) {
		if (destinationAttribute == null){
			return null;
		}
		String[] splitted = destinationAttribute.split("\\.");
		String result = splitted[splitted.length - 1];  //get last attribute
		return result;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.IDbExportMapper#invoke(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public CDM_BASE invoke(ResultSet rs, CDM_BASE cdmBase) throws SQLException {
		if (ignore){
			return cdmBase;
		}
		Object dbValue = getValue(rs);
		
//		String dbValue = rs.getString(getSourceAttribute());
		return doInvoke(cdmBase, dbValue);
	}
	
	protected CDM_BASE doInvoke(CDM_BASE cdmBase, Object value) throws SQLException {
		Method method = getMethod();
		try {
			Object objectToInvoke = getObjectToInvoke(cdmBase);
			if (objectToInvoke == null){
				logger.warn("No object defined for invoke. Method will not be invoked");
			}else{
				method.invoke(objectToInvoke, value);
			}
			return cdmBase;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Problems when invoking target method");
	}
	
	/**
	 * @param cdmBase
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private Object getObjectToInvoke(CDM_BASE cdmBase) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object objectToInvoke = cdmBase;
		String destinationAttribute = getDestinationAttribute();
		String[] splits = destinationAttribute.split("\\.");
		for (int i = 0; i < splits.length - 1; i++){
			String split = splits[i];
			split = removeCast(split);
			String methodName = ImportHelper.getGetterMethodName(split, false);
			Method method = objectToInvoke.getClass().getMethod(methodName, null);
			objectToInvoke = method.invoke(cdmBase, null);
		}
		return objectToInvoke;
	}


	/**
	 * @return
	 */
	private Method getMethod() {
		if (destinationMethod != null){
			return destinationMethod;
		}else{
			throw new RuntimeException("Missing destinationMethod not yet implemented");
		}
	}


	protected Object getValue(ResultSet rs) throws SQLException{
		return getDbValue(rs);
	}
	
	/**
	 * Returns the database value for the attribute
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected Object getDbValue(ResultSet rs) throws SQLException{
		String columnLabel = getSourceAttribute();
		Object value = rs.getObject(columnLabel);
		return value;
	}

	
//	/**
//	 * @return the index
//	 */
//	public int getIndex() {
//		return exportMapperHelper.getIndex();
//	}
	
	/**
	 * @return the state
	 */
	protected STATE getState() {
		return importMapperHelper.getState();
	}
	
	
	/**
	 * @return the state
	 */
	protected String getTableName() {
		return importMapperHelper.getTableName();
	}
	
	protected boolean checkDbColumnExists() throws DatabaseTypeNotSupportedException{
//		//TODO remove cast
//		Source source = (Source)getState().getConfig().getSource();
//		String tableName = getTableName();
//		String attributeName = getSourceAttribute();
//		return source.checkColumnExists(tableName, attributeName);
		//TODO not possible as long as tableName is not initialized
		return true;
	}
	
//	protected int getPrecision(){
//		return this.precision;
//	}
	
	protected int getDbColumnIntegerInfo(String selectPart){
		//TODO remove cast
		Source source = (Source)getState().getConfig().getSource();
		String strQuery = "SELECT  " + selectPart + " as result" +
				" FROM sysobjects AS t " +
				" INNER JOIN syscolumns AS c ON t.id = c.id " +
				" WHERE (t.xtype = 'U') AND " + 
				" (t.name = '" + getTableName() + "') AND " + 
				" (c.name = '" + getSourceAttribute() + "')";
		ResultSet rs = source.getResultSet(strQuery) ;		
		int n;
		try {
			rs.next();
			n = rs.getInt("result");
			return n;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
			
	}
	

	/**
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected String getStringDbValue(ResultSet rs, String attribute) throws SQLException {
		Object oId = rs.getObject(attribute);
		String id = String.valueOf(oId);
		return id;
	}
//	public String toString(){
//		String sourceAtt = CdmUtils.Nz(getSourceAttribute());
//		String destAtt = CdmUtils.Nz(getDestinationAttribute());
//		return this.getClass().getSimpleName() +"[" + sourceAtt + "->" + destAtt + "]";
//	}
	
}
