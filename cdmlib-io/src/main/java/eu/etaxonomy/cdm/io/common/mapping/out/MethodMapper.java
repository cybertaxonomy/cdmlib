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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.io.common.DbExportBase;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public class MethodMapper
            extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>>
            implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer> {

    private static final Logger logger = LogManager.getLogger();

	private Method method;
	private Class<?>[] parameterTypes;

	public static <T extends DbExportBase> MethodMapper NewInstance(String dbAttributeString, DbExportBase exportBase){

	    String methodName = getMethodName(dbAttributeString);
		return NewInstance(dbAttributeString, exportBase, methodName);
	}

    public static <T extends DbExportBase> MethodMapper NewInstance(String dbAttributeString, Class<?> clazz, DbExportBase exportBase){

        String methodName = getMethodName(dbAttributeString);
        Class<?> parameterTypes = exportBase.getStandardMethodParameter();
        return new MethodMapper(dbAttributeString, clazz, methodName, parameterTypes);
    }

	public static <T extends DbExportBase> MethodMapper NewInstance(String dbAttributeString, DbExportBase exportBase,
	        Class<?>... parameterTypes){

	    String methodName = getMethodName(dbAttributeString);
		return new MethodMapper(dbAttributeString, exportBase.getClass(), methodName, parameterTypes);
	}

	public static <T extends DbExportBase> MethodMapper NewInstance(String dbAttributeString, DbExportBase exportBase,
	        String methodName){

	    Class<?> parameterTypes = exportBase.getStandardMethodParameter();
		MethodMapper result = new MethodMapper(dbAttributeString, exportBase.getClass(), methodName, parameterTypes);
		return result;
	}

	public static <T extends DbExportBase> MethodMapper NewInstance(String dbAttributeString, Class<?> clazz,
	        Class<?>... parameterTypes){

	    String methodName = getMethodName(dbAttributeString);
        MethodMapper result = new MethodMapper(dbAttributeString, clazz, methodName, parameterTypes);
        return result;
    }

	public static <T extends DbExportBase> MethodMapper NewInstance(String dbAttributeString, Class<?> clazz,
	        String methodName, Class<?>... parameterTypes){

	    MethodMapper result = new MethodMapper(dbAttributeString, clazz, methodName, parameterTypes);
		return result;
	}

	public static <T extends DbExportBase> MethodMapper NewInstance(String dbAttributeString, Class<?> clazz,
	        String methodName, Class<?> parameterType1, Class<?> parameterType2){

	    MethodMapper result = new MethodMapper(dbAttributeString, clazz, methodName, parameterType1, parameterType2);
		return result;
	}

	/**
	 * @param dbAttributeString the database attribute name
	 * @param clazz the class that holds the method to call
	 * @param methodName the method name
	 * @param parameterTypes the parameter types to pass to the method
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

    private static String getMethodName(String dbAttributeString) {
        String methodName = "get" + dbAttributeString;
        return methodName;
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
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			logger.error("InvocationTargetException: " + e.getMessage() + " when invoking MethodMapper " +  this.toString());
			throw new RuntimeException(e);
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
