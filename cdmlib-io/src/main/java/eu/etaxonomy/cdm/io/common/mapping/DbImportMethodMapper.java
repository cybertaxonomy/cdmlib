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

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * @author a.mueller
 * @since 12.05.2009
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

	public static <T extends DbImportStateBase<?,?>> DbImportMethodMapper NewDefaultInstance(CdmImportBase<?, ?> cdmImport, String methodName,
	        Class<? extends DbImportStateBase> importStateClass){
	    DbImportMethodMapper<?,?> result = new DbImportMethodMapper<VersionableEntity, T>(cdmImport.getClass(), cdmImport, methodName, ResultSet.class, importStateClass);
	    return result;
	}

	public static <T extends DbImportStateBase<?,?>> DbImportMethodMapper NewInstance(Class<?> clazz, String methodName, Class parameterType){
		DbImportMethodMapper<?,T> result = new DbImportMethodMapper<VersionableEntity,T>(clazz, null, methodName, parameterType);
		return result;
	}


//	public static <T extends DbImportStateBase<?,?>> DbImportMethodMapper NewInstance(Object objectToInvoke, String methodName, Class<?> parameterType1, Class<?> parameterType2){
//		DbImportMethodMapper<?,?> result = new DbImportMethodMapper<VersionableEntity, T>(objectToInvoke.getClass(), objectToInvoke, methodName, parameterType1,parameterType2);
//		return result;
//	}

	@Deprecated //not sure if it works correctly any more, was already commented but is used in CentralAfricaFernsXXXImport
	//can be removed if this dependency is removed
	public static <T extends DbImportStateBase<?,?>> DbImportMethodMapper<VersionableEntity,T> NewInstance(Object objectToInvoke, String methodName, Class<?>... parameterTypes){
		DbImportMethodMapper<VersionableEntity,T> result = new DbImportMethodMapper<VersionableEntity,T>(objectToInvoke.getClass(), objectToInvoke, methodName, parameterTypes);
		return result;
	}

//********************************* CONSTRUCTOR ****************************************/

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

	@Override
    public void initialize(STATE state, Class<? extends CdmBase> destinationClass) {
		super.initialize(state, destinationClass);
		//initialize when this logging is not needed anymore
	}

	@Override
    public CDMBASE invoke(ResultSet rs, CDMBASE cdmBase) throws SQLException {
		try{
            //		if (this.parameterTypes.length > 1 && DbExportStateBase.class.isAssignableFrom(parameterTypes[1])){
			getState().addRelatedObject(DbImportStateBase.CURRENT_OBJECT_NAMESPACE,
			        DbImportStateBase.CURRENT_OBJECT_ID, cdmBase);
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
	 */
	protected IInputTransformer getTransformer(){
		return getState().getTransformer();
	}
}
