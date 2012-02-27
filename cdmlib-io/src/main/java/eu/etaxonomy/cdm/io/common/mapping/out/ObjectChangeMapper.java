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
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.CdmAttributeMapperBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class ObjectChangeMapper extends CdmAttributeMapperBase implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer> {
	private static final Logger logger = Logger.getLogger(ObjectChangeMapper.class);

	private DbExportStateBase<?, IExportTransformer> state;  //for possible later use
	
	private String cdmAttribute;
	
	private Method method;
//	private Class<?>[] parameterTypes;
	
	public static ObjectChangeMapper NewInstance(Class<? extends CdmBase>  oldClass, Class<? extends CdmBase> newClass, String cdmAttribute){
		String methodName = "get" + cdmAttribute;
		return new ObjectChangeMapper(oldClass, methodName, (Class<?>[])null);
	}
	
	/**
	 * @param parameterTypes 
	 * @param dbIdAttributString
	 */
	protected ObjectChangeMapper(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		try {
//			this.parameterTypes = parameterTypes;
			method = clazz.getDeclaredMethod(methodName, parameterTypes);
			method.setAccessible(true);
		} catch (SecurityException e) {
			logger.error("SecurityException", e);
		} catch (NoSuchMethodException e) {
			logger.error("NoSuchMethodException", e);
		}
	}
	
	
	@Override
	public void initialize(PreparedStatement stmt, IndexCounter index, DbExportStateBase<?, IExportTransformer> state, String tableName) {
		this.state = state;
	}

	public boolean invoke(CdmBase cdmBase) {
		throw new RuntimeException("Invoke must not be called for " + this.getClass().getSimpleName() + ".  Return type is still incompatible. Use getNewObject instead.");
	}

	public CdmBase getNewObject(CdmBase oldCdmBase){
		try {
			return  (CdmBase)method.invoke(oldCdmBase, (Object[])null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Set<String> getSourceAttributes() {
		Set<String> result = new HashSet<String>();
		if (cdmAttribute != null){
			result.add(cdmAttribute);
		}
		return result;
	}

	@Override
	public Set<String> getDestinationAttributes() {
		return new HashSet<String>();
	}

	@Override
	public List<String> getSourceAttributeList() {
		ArrayList<String> result = new ArrayList<String>();
		if (cdmAttribute != null){
			result.add(cdmAttribute);
		}
		return result;
	}

	@Override
	public List<String> getDestinationAttributeList() {
		return new ArrayList<String>();
	}



}
