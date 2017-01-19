/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.lang.reflect.Method;
import java.sql.Types;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * Maps a language to a database key or cache field.
 * @author a.mueller
 * @created 06.02.2012
 */
public class DbLanguageMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	private static final Logger logger = Logger.getLogger(DbLanguageMapper.class);

	private Method method;
	private boolean isCache;
	private Class<? extends CdmBase> cdmClass;

	/**
	 * @param cdmClass The class in which "getLanguage" is implemented
	 * @param cdmAttribute cdm attribute holding the language information
	 * @param dbAttributeString
	 * @param isCache
	 * @return
	 */
	public static DbLanguageMapper NewInstance(Class<? extends CdmBase> cdmClass, String cdmAttribute, String dbAttributeString, boolean isCache){
		String methodName = "get" + cdmAttribute;
		return new DbLanguageMapper(cdmClass, methodName,  (Class<?>[])null, cdmAttribute, dbAttributeString, isCache, null);
	}

	/**
	 * @param clazz
	 * @param parameterTypes
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	protected DbLanguageMapper(Class<? extends CdmBase> clazz, String methodName, Class<?>[] parameterTypes, String cdmAttribute, String dbAttributeString, boolean isCache, Object defaultValue) {
		super(cdmAttribute, dbAttributeString, defaultValue);
		cdmClass =  clazz;
		this.isCache = isCache;
		try {
//			this.parameterTypes = parameterTypes;
			method = clazz.getDeclaredMethod(methodName, parameterTypes);
			method.setAccessible(true);
			cdmClass =  clazz;
		} catch (NoSuchMethodException e) {
			try {
				method = clazz.getMethod(methodName, parameterTypes);
			} catch (NoSuchMethodException e1) {
				logger.error("NoSuchMethodException", e);
				return;
			}
		} catch (SecurityException e) {
			logger.error("SecurityException", e);
			return;
		}
		method.setAccessible(true);
		cdmClass =  clazz;
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		if (cdmBase.isInstanceOf(cdmClass)){
			try {
				Language language = (Language)method.invoke(cdmBase, null);
				IExportTransformer transformer = getState().getTransformer();
				if (isCache){
					return transformer.getCacheByLanguage(language);
				}else{
					return transformer.getKeyByLanguage(language);
				}
			} catch (Exception e) {
				logger.error("Exception when invoking method: " + e.getLocalizedMessage());
				return null;
			}

		}else{
			throw new ClassCastException("CdmBase for "+this.getClass().getName() +" must be of type "+cdmClass.getName()+", but was " + cdmBase.getClass());
		}
	}

	@Override
	protected int getSqlType() {
		if (isCache){
			return Types.VARCHAR;
		}else{
			return Types.INTEGER;
		}
	}

	@Override
	public Class<?> getTypeClass() {
		if (isCache){
			return String.class;
		}else{
			return Integer.class;
		}
	}
}
