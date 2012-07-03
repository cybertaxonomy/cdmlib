/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */

@Component("defaultImport")
public class CdmApplicationAwareDefaultImport<T extends IImportConfigurator> implements ICdmImporter<T>, ApplicationContextAware {
	private static final Logger logger = Logger.getLogger(CdmApplicationAwareDefaultImport.class);

	protected ApplicationContext applicationContext;
	
	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}


	//Constants
	final boolean OBLIGATORY = true; 
	final boolean FACULTATIVE = false; 
	final int modCount = 1000;

	IService<CdmBase> service = null;
	
	//different type of stores that are used by the known imports
	Map<String, MapWrapper<? extends CdmBase>> stores = new HashMap<String, MapWrapper<? extends CdmBase>>();

	public CdmApplicationAwareDefaultImport(){
		
		
		stores.put(ICdmIO.PERSON_STORE, new MapWrapper<Person>(service));
		stores.put(ICdmIO.TEAM_STORE, new MapWrapper<TeamOrPersonBase<?>>(service));
		stores.put(ICdmIO.REFERENCE_STORE, new MapWrapper<Reference>(service));
		stores.put(ICdmIO.NOMREF_STORE, new MapWrapper<Reference>(service));
		stores.put(ICdmIO.NOMREF_DETAIL_STORE, new MapWrapper<Reference>(service));
		stores.put(ICdmIO.REF_DETAIL_STORE, new MapWrapper<Reference>(service));
		stores.put(ICdmIO.TAXONNAME_STORE, new MapWrapper<TaxonNameBase<?,?>>(service));
		stores.put(ICdmIO.TAXON_STORE, new MapWrapper<TaxonBase>(service));
		stores.put(ICdmIO.SPECIMEN_STORE, new MapWrapper<Specimen>(service));
	}
	
	public boolean invoke(IImportConfigurator config){
		if (config.getCheck().equals(IImportConfigurator.CHECK.CHECK_ONLY)){
			return doCheck(config);
		}else if (config.getCheck().equals(IImportConfigurator.CHECK.CHECK_AND_IMPORT)){
			doCheck(config);
			return doImport(config);
		}else if (config.getCheck().equals(IImportConfigurator.CHECK.IMPORT_WITHOUT_CHECK)){
			return doImport(config);
		}else{
			logger.error("Unknown CHECK type");
			return false;
		}
	}
	
	
	@SuppressWarnings("unchecked")
	protected <S extends IImportConfigurator> boolean doCheck(S  config){
		boolean result = true;
		
		//check
		if (config == null){
			logger.warn("CdmImportConfiguration is null");
			return false;
		}
		System.out.println("Start checking Source ("+ config.getSourceNameString() + ") ...");
		if (! config.isValid()){
			logger.warn("CdmImportConfiguration is not valid");
			return false;
		}
		
		ImportStateBase state = config.getNewState();
		state.initialize(config);
		
		//do check for each class
		for (Class<ICdmIO> ioClass: config.getIoClassList()){
			try {
				String ioBeanName = getComponentBeanName(ioClass);
				ICdmIO cdmIo = (ICdmIO)applicationContext.getBean(ioBeanName, ICdmIO.class);
				if (cdmIo != null){
					registerObservers(config, cdmIo);
					state.setCurrentIO(cdmIo);
					result &= cdmIo.check(state);
					unRegisterObservers(config, cdmIo);
				}else{
					logger.error("cdmIO was null");
					result = false;
				}
			} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
					result = false;
			}
		}
		
		//return
		System.out.println("End checking Source ("+ config.getSourceNameString() + ") for import to Cdm");
		return result;

	}
	
	private void registerObservers(IImportConfigurator config, ICdmIO io){
		for (IIoObserver observer : config.getObservers()){
			io.addObserver(observer);
		}
	}
	
	private void unRegisterObservers(IImportConfigurator config, ICdmIO io){
		for (IIoObserver observer : config.getObservers()){
			io.removeObserver(observer);
		}
	}
	
	
	/**
	 * Executes the whole 
	 */
	protected <S extends IImportConfigurator>  boolean doImport(S config){
		boolean result = true;
		//validate
		if (config == null){
			logger.warn("Configuration is null");
			return false;
		}else if (! config.isValid()){
			logger.warn("Configuration is not valid");
			return false;
		}
				
		config.getSourceReference();
		logger.info("Start import from Source '"+ config.getSourceNameString() + "' to destination '" + config.getDestinationNameString() + "'");
		
		
		ImportStateBase state = config.getNewState();
		state.initialize(config);

		state.setSuccess(true);
		//do invoke for each class
		for (Class<ICdmIO> ioClass: config.getIoClassList()){
			try {
				String ioBeanName = getComponentBeanName(ioClass);
				ICdmIO cdmIo = (ICdmIO)applicationContext.getBean(ioBeanName, ICdmIO.class);
				if (cdmIo != null){
					registerObservers(config, cdmIo);
					state.setCurrentIO(cdmIo);
					result &= cdmIo.invoke(state);
					unRegisterObservers(config, cdmIo);
				}else{
					logger.error("cdmIO was null");
					result = false;
				}
			} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
					result = false;
			}
		}
		
		//do invoke for each class
//		for (String ioBean: config.getIoBeans()){
//			try {
//				ICdmIO<S> cdmIo = (ICdmIO<S>)applicationContext.getBean(ioBean, ICdmIO.class);
//				if (cdmIo != null){
//					result &= cdmIo.invoke(config, stores);
//				}else{
//					logger.error("cdmIO was null");
//					result = false;
//				}
//			} catch (Exception e) {
//					logger.error(e);
//					e.printStackTrace();
//					result = false;
//			}
//			
//		}
		
		logger.info("End import from source '" + config.getSourceNameString() 
				+ "' to destination '" + config.getDestinationNameString() + "'"+
				(result? "(successful)":"(with errors)")) ;
		return result;
	}

	/**
	 * Returns the name of a component bean. If the name is defined in the Component annotation this name is returned.
	 * Otherwise the class name is returned with starting lower case.
	 * @param ioClass
	 * @return
	 * @throws IllegalArgumentException if the class does not have a "Component" annotation
	 */
	public static String getComponentBeanName(Class<ICdmIO> ioClass) throws IllegalArgumentException {
		Component component = ioClass.getAnnotation(Component.class);
		if (component == null){
			throw new IllegalArgumentException("Class " + ioClass.getName() + " is missing a @Component annotation." );
		}
		String ioBean = component.value();
		if ("".equals(ioBean)){
			ioBean = ioClass.getSimpleName();
			ioBean = ioBean.substring(0, 1).toLowerCase() + ioBean.substring(1); //make camelcase
		}
		return ioBean;
	}

}
