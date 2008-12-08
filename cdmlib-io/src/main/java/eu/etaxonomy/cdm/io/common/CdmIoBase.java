/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author a.mueller
 * @created 01.07.2008
 * @version 1.0
 */
public abstract class CdmIoBase<T extends IIoConfigurator> implements ICdmIO<T> {
	private static Logger logger = Logger.getLogger(CdmIoBase.class);

	protected String ioName = null;

	
	/**
	 * 
	 */
	public CdmIoBase() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ICdmIO#check(eu.etaxonomy.cdm.io.common.IIoConfigurator)
	 */
	public boolean check(T config) {
		if (isIgnore(config)){
			logger.warn("No check for " + ioName + " (ignored)");
			return true;
		}else{
			return doCheck(config);
		}
	}
	
	protected abstract boolean doCheck(T config);


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ICdmIO#invoke(eu.etaxonomy.cdm.io.common.IIoConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	public boolean invoke(T config,
//	public boolean invoke(IIoConfigurator config,
			Map stores) {
		if (isIgnore(config)){
			logger.warn("No invoke for " + ioName + " (ignored)");
			return true;
		}else{
			return doInvoke(config, stores);
		}
	}
	
	protected abstract boolean doInvoke(T config,
			Map<String, MapWrapper<? extends CdmBase>> stores);

	
	protected abstract boolean isIgnore(T config);

	protected <T extends CdmBase> T getInstance(Class<? extends T> clazz){
		T result = null;
		try {
			Constructor<? extends T> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			result = constructor.newInstance();
		} catch (InstantiationException e) {
			logger.error("Class " + clazz.getSimpleName()+" could not be instantiated. Class = " );
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			logger.error("Constructor of class "+clazz.getSimpleName()+" could not be accessed." );
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			logger.error("SecurityException for Constructor of class "+clazz.getSimpleName()+"." );
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			logger.error("Empty Constructor does not exist for class "+clazz.getSimpleName()+"." );
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			logger.error("Empty Constructor could not be invoked for class "+clazz.getSimpleName()+"." );
			e.printStackTrace();
		}
		return result;
	}
	

}
