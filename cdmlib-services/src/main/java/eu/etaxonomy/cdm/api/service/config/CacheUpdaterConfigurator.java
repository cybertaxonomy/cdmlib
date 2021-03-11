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
 * @since 02-Jul-2010
 */
public class CacheUpdaterConfigurator implements Serializable{

    private static final long serialVersionUID = 6102562152485923714L;
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CacheUpdaterConfigurator.class);

	private List<Class<? extends IdentifiableEntity>> classList;
	private IProgressMonitor monitor;
	private boolean updateCacheStrategies = false;


//******************** CONSTRUCTOR *********************************/

    public static CacheUpdaterConfigurator NewInstance(){
		return new CacheUpdaterConfigurator(false);
	}

	/**
	 * Returns a new configurator with all boolean values set to false
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
		@SuppressWarnings("rawtypes")
        List<Class<? extends IdentifiableEntity>> classList = new ArrayList<>();
		for (String className : classListNames){
			@SuppressWarnings({ "rawtypes", "unchecked" })
            Class<? extends IdentifiableEntity> clazz = (Class<? extends IdentifiableEntity>) Class.forName(className);
			classList.add(clazz);
		}
		result.setClassList(classList);
		return result;
	}

	@SuppressWarnings("unchecked")
    public static CacheUpdaterConfigurator NewInstance(Collection<String> classListNames, boolean doUpdateCacheStrategies) throws ClassNotFoundException{
		CacheUpdaterConfigurator result = new CacheUpdaterConfigurator( false);
		@SuppressWarnings({ "rawtypes" })
        List<Class<? extends IdentifiableEntity>> classList = new ArrayList<>();
		for (String className : classListNames){
			@SuppressWarnings("rawtypes")
            Class clazz = Class.forName(className);
			classList.add(clazz);
		}
		result.setClassList(classList);
		result.setUpdateCacheStrategy(doUpdateCacheStrategies);
		return result;
	}

// ********************* CONSTRUCTOR ******************************/

	private CacheUpdaterConfigurator(boolean allFalse){
		this.classList = new ArrayList<>();
	}

// **************** GETTER / SETTER **********************************/


	public List<Class<? extends IdentifiableEntity>> getClassList(){
		return classList;
	}
	private void setClassList(List<Class<? extends IdentifiableEntity>> classList) {
		this.classList = classList;
	}

	public boolean isUpdateCacheStrategy() {
		return updateCacheStrategies;
	}
	public void setUpdateCacheStrategy(boolean doUpdateCacheStrategies){
		this.updateCacheStrategies = doUpdateCacheStrategies;
	}

    public IProgressMonitor getMonitor() {
        return monitor;
    }
    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }
}