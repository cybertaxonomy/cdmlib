/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Types;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.io.common.DbExportBase;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 12.05.2009
 * @version 1.0
 */
public class MethodMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer> {
	private static final Logger logger = Logger.getLogger(MethodMapper.class);
	
	private Method method;
	private Class<?>[] parameterTypes;
	
	public static <T extends DbExportBase> MethodMapper NewInstance(String dbAttributeString, DbExportBase exportBase){
		String methodName = "get" + dbAttributeString;
		return NewInstance(dbAttributeString, exportBase, methodName);
	}
	
	public static <T extends DbExportBase> MethodMapper NewInstance(String dbAttributeString, DbExportBase exportBase, Class<?>... parameterTypes){
		String methodName = "get" + dbAttributeString;
		return new MethodMapper(dbAttributeString, exportBase.getClass(), methodName, parameterTypes);
	}
	
	public static <T extends DbExportBase> MethodMapper NewInstance(String dbAttributeString, DbExportBase exportBase, String methodName){
		Class<?> parameterTypes = exportBase.getStandardMethodParameter();
		MethodMapper result = new MethodMapper(dbAttributeString, exportBase.getClass(), methodName, parameterTypes);
		return result;
	}

	public static <T extends DbExportBase> MethodMapper NewInstance(String dbAttributeString, Class<?> clazz, String methodName, Class<?>... parameterTypes){
		MethodMapper result = new MethodMapper(dbAttributeString, clazz, methodName, parameterTypes);
		return result;
	}
	
	public static <T extends DbExportBase> MethodMapper NewInstance(String dbAttributeString, Class<?> clazz, String methodName, Class<?> parameterType1, Class<?> parameterType2){
		MethodMapper result = new MethodMapper(dbAttributeString, clazz, methodName, parameterType1,parameterType2);
		return result;
	}
	
	/**
	 * @param parameterTypes 
	 * @param dbIdAttributString
	 */
	protected MethodMapper(String dbAttributeString, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		super(null, dbAttributeString, null);
		try {
			this.parameterTypes = parameterTypes;
			method = clazz.getDeclaredMethod(methodName, parameterTypes);
			method.setAccessible(true);
		} catch (SecurityException e) {
			logger.error("SecurityException", e);
		} catch (NoSuchMethodException e) {
			logger.error("NoSuchMethodException", e);
		}
	}
	
	@Override
	public Class<?> getTypeClass() {
		return method.getReturnType();
	}
	
	@Override
	protected Object getValue(CdmBase cdmBase) {
		try{	
			if (this.parameterTypes.length > 1 && DbExportStateBase.class.isAssignableFrom(parameterTypes[1])){
				return method.invoke(null, cdmBase, getState());
			}else{
				return method.invoke(null, cdmBase);
			}
			
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException: " + e.getMessage() + " when invoking MethodMapper " +  this.toString());
			return false;
		} catch (InvocationTargetException e) {
			logger.error("InvocationTargetException: " + e.getMessage() + " when invoking MethodMapper " +  this.toString());
			return false;
		} catch (Exception e) {
			logger.error("Any Exception: " + e.getMessage() + " when invoking MethodMapper " +  this.toString());
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected int getSqlType() {
		Class<?> returnType = method.getReturnType();
		if (returnType == Integer.class){
			return Types.INTEGER;
		}else if (returnType == String.class){
			return Types.VARCHAR;
		}else if (returnType == Boolean.class){
			return Types.BOOLEAN;
		}else if (returnType == DateTime.class){
			return Types.DATE;
		}else{
			logger.warn("Return type not supported yet: " + returnType.getSimpleName());
			throw new IllegalArgumentException("Return type not supported yet: " + returnType.getSimpleName());
		}
	}
	
}
