/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author a.mueller
 * @since 02-Jul-2010 13:06:43
 *
 */
public class CacheUpdaterConfigurator implements Serializable{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CacheUpdaterConfigurator.class);


	private boolean updateCacheStrategies = false;

	private IProgressMonitor monitor;



    public static CacheUpdaterConfigurator NewInstance(){
		return new CacheUpdaterConfigurator(false);
	}

	/**
	 * Returns a new Configurator with all boolean values set to false
	 * @param allFalse
	 * @return
	 */
	public static CacheUpdaterConfigurator NewInstance(boolean allTrue){
		return new CacheUpdaterConfigurator(allTrue);
	}

	public static CacheUpdaterConfigurator NewInstance(List<Class<? extends IdentifiableEntity>> classList){
		CacheUpdaterConfigurator result = new CacheUpdaterConfigurator(true);
		result.setClassList(classList);
		return result;
	}

	public static CacheUpdaterConfigurator NewInstance(Collection<String> classListNames) throws ClassNotFoundException{
		CacheUpdaterConfigurator result = new CacheUpdaterConfigurator(true);
		List<Class<? extends IdentifiableEntity>> classList = new ArrayList<Class<? extends IdentifiableEntity>>();
		for (String className : classListNames){
			Class clazz = Class.forName(className);
			classList.add(clazz);
		}
		result.setClassList(classList);
		return result;
	}

	public static CacheUpdaterConfigurator NewInstance(Collection<String> classListNames, boolean doUpdateCacheStrategies) throws ClassNotFoundException{
		CacheUpdaterConfigurator result = new CacheUpdaterConfigurator( false);
		List<Class<? extends IdentifiableEntity>> classList = new ArrayList<Class<? extends IdentifiableEntity>>();
		for (String className : classListNames){
			Class clazz = Class.forName(className);
			classList.add(clazz);
		}
		result.setClassList(classList);
		result.setUpdateCacheStrategy(doUpdateCacheStrategies);
		return result;
	}

	/**
     * @return the monitor
     */
    public IProgressMonitor getMonitor() {
        return monitor;
    }

    /**
     * @param monitor the monitor to set
     */
    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }


	private List<Class<? extends IdentifiableEntity>> classList;

	private CacheUpdaterConfigurator(boolean allFalse){
		this.classList = new ArrayList<Class<? extends IdentifiableEntity>>();

	}



// **************** GETTER / SETTER ************************************


	public List<Class<? extends IdentifiableEntity>> getClassList(){
		return classList;
	}
	private void setClassList(List<Class<? extends IdentifiableEntity>> classList) {
		this.classList = classList;
	}






	public boolean doUpdateCacheStrategy() {
		return updateCacheStrategies;
	}

	public void setUpdateCacheStrategy(boolean doUpdateCacheStrategies){
		this.updateCacheStrategies = doUpdateCacheStrategies;
	}



}
