// $Id$
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

import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DbExportBase;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.IOriginalSource;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
//TODO remove ANNOTATABLE by ISourcable (but this is not CDMBase yet therefore not trivial
public class DbImportMethodMapperBase<CDMBASE extends VersionableEntity, STATE extends DbImportStateBase<?,?>> extends DbImportMultiAttributeMapperBase<CDMBASE, STATE>  {
	private static final Logger logger = Logger.getLogger(DbImportMethodMapperBase.class);
	
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

	public static <T extends DbImportStateBase> DbImportMethodMapperBase NewInstance(Class<?> clazz, String methodName, Class parameterTypes){
		DbImportMethodMapperBase result = new DbImportMethodMapperBase(clazz, null, methodName, parameterTypes);
		return result;
	}
	
	public static <T extends DbImportStateBase> DbImportMethodMapperBase NewInstance(Object objectToInvoke, String methodName, Class<?> parameterType1, Class<?> parameterType2){
		DbImportMethodMapperBase result = new DbImportMethodMapperBase(objectToInvoke.getClass(), objectToInvoke, methodName, parameterType1,parameterType2);
		return result;
	}
	
//********************************* CONSTRUCTOR ****************************************/
	
	/**
	 * @param clazz
	 * @param methodName
	 * @param parameterTypes
	 */
	protected DbImportMethodMapperBase(Class<?> clazz, Object objectToInoke, String methodName, Class<?>... parameterTypes) {
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
			CDMBASE result = (CDMBASE)method.invoke(objectToInvoke, rs, getState());
	//		}else{
	//			return (CDMBASE)method.invoke(null, rs);
	//		}
			
	//		CDMBASE result = doInvoke(rs, result);
			return result;
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException: " + e.getLocalizedMessage());
			return null;
		} catch (InvocationTargetException e) {
			logger.error("InvocationTargetException: " + e.getLocalizedMessage());
			return null;
		}
	}
	
	
	/**
	 * Returns the transformer from the configuration
	 * @return
	 */
	protected IInputTransformer getTransformer(){
		return getState().getConfig().getTransformer();
	}
	
}
