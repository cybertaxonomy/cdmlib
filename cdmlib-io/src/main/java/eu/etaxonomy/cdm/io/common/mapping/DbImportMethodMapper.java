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

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
//TODO remove ANNOTATABLE by ISourcable (but this is not CDMBase yet therefore not trivial
public class DbImportMethodMapper<CDMBASE extends VersionableEntity, STATE extends DbImportStateBase<?,?>> extends DbImportMultiAttributeMapperBase<CDMBASE, STATE>  {
	private static final Logger logger = Logger.getLogger(DbImportMethodMapper.class);
	
	//******************************* ATTRIBUTES ***************************************/
	
	private Method method;
	private Class<?>[] parameterTypes;
	private Object objectToInvoke;
	

// **************************** FACTORY METHODS ***************************************************/

//	public static <T extends DbImportStateBase> DbImportMethodMapperBase NewInstance(DbImportStateBase importBase, String methodName){
//		
////		Class<?> parameterTypes = importBase.getStandardMethodParameter();
//		Class<?> parameterType1 = ResultSet.class;
//		Class<?> parameterType2 = DbImportStateBase.class;
//		
//		DbImportMethodMapperBase result = new DbImportMethodMapperBase(importBase.getClass(), methodName, parameterType1, parameterType2);
//		return result;
//	}

	public static <T extends DbImportStateBase<?,?>> DbImportMethodMapper NewInstance(Class<?> clazz, String methodName, Class parameterTypes){
		DbImportMethodMapper<?,T> result = new DbImportMethodMapper<VersionableEntity,T>(clazz, null, methodName, parameterTypes);
		return result;
	}
	
	public static <T extends DbImportStateBase<?,?>> DbImportMethodMapper NewInstance(Object objectToInvoke, String methodName, Class<?> parameterType1, Class<?> parameterType2){
		DbImportMethodMapper<?,?> result = new DbImportMethodMapper<VersionableEntity, T>(objectToInvoke.getClass(), objectToInvoke, methodName, parameterType1,parameterType2);
		return result;
	}
	
	public static <T extends DbImportStateBase<?,?>> DbImportMethodMapper NewInstance(Object objectToInvoke, String methodName, Class<?>... parameterTypes){
		DbImportMethodMapper<?,?> result = new DbImportMethodMapper<VersionableEntity,T>(objectToInvoke.getClass(), objectToInvoke, methodName, parameterTypes);
		return result;
	}
	
//********************************* CONSTRUCTOR ****************************************/
	
	/**
	 * @param clazz
	 * @param methodName
	 * @param parameterTypes
	 */
	protected DbImportMethodMapper(Class<?> clazz, Object objectToInoke, String methodName, Class<?>... parameterTypes) {
		super();
		this.objectToInvoke = objectToInoke;
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
//************************************ METHODS *******************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper#initialize(eu.etaxonomy.cdm.io.common.DbImportStateBase, java.lang.Class)
	 */
	public void initialize(STATE state, Class<? extends CdmBase> destinationClass) {
		super.initialize(state, destinationClass);
		//initialize when this logging is not needed anymore
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper#invoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public CDMBASE invoke(ResultSet rs, CDMBASE cdmBase) throws SQLException {
		try{
	//		if (this.parameterTypes.length > 1 && DbExportStateBase.class.isAssignableFrom(parameterTypes[1])){
			getState().addRelatedObject(getState().CURRENT_OBJECT_NAMESPACE, getState().CURRENT_OBJECT_ID, cdmBase);
			CDMBASE result = (CDMBASE)method.invoke(objectToInvoke, rs, getState());
	//		}else{
	//			return (CDMBASE)method.invoke(null, rs);
	//		}
			
	//		CDMBASE result = doInvoke(rs, result);
			return result;
		} catch (InvocationTargetException e) {
			logger.error("InvocationTargetException: " + e.getLocalizedMessage() + " in method " + this.method.getName());
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException: " + e.getLocalizedMessage());
			return null;
		}
	}
	
	
	/**
	 * Returns the transformer from the configuration
	 * @return
	 */
	protected IInputTransformer getTransformer(){
		return getState().getTransformer();
	}
	
}
