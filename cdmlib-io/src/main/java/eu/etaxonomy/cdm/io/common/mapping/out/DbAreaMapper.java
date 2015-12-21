// $Id$
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
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * Maps an area to a database key or cache field.
 * @author a.mueller
 * @created 06.02.2012
 */
public class DbAreaMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	private static final Logger logger = Logger.getLogger(DbAreaMapper.class);

	private Method method;
	private boolean isCache;
	private Class<? extends CdmBase> cdmClass;

	public static DbAreaMapper NewInstance(Class<? extends CdmBase> cdmClass, String cdmAttribute, String dbAttributeString, boolean isCache){
		String methodName = "get" + cdmAttribute;
		return new DbAreaMapper(cdmClass, methodName,  (Class<?>[])null, cdmAttribute, dbAttributeString, isCache, null);
	}

	/**
	 * @param clazz
	 * @param parameterTypes
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	protected DbAreaMapper(Class<? extends CdmBase> clazz, String methodName, Class<?>[] parameterTypes, String cdmAttribute, String dbAttributeString, boolean isCache, Object defaultValue) {
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
				NamedArea area = (NamedArea)method.invoke(cdmBase, (Object[])null);
				IExportTransformer transformer = getState().getTransformer();
				if (isCache){
					return transformer.getCacheByNamedArea(area);
				}else{
					return transformer.getKeyByNamedArea(area);
				}
			} catch (Exception e) {
				logger.error("Exception when invoking method: " + e.getMessage());
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
