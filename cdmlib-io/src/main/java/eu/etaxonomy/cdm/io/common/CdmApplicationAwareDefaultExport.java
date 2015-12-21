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
import eu.etaxonomy.cdm.io.common.IExportConfigurator.TARGET;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * This class is an default exporter class that is a spring bean and therefore it knows all other IO classes that are beans
 *
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */

@Component("defaultExport")
public class CdmApplicationAwareDefaultExport<T extends IExportConfigurator> implements ICdmExporter<T>, ApplicationContextAware {
	private static final Logger logger = Logger.getLogger(CdmApplicationAwareDefaultExport.class);

	protected ApplicationContext applicationContext;

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
    public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

//	DbExportStateBase<T> state;


	//Constants
	final static boolean OBLIGATORY = true;
	final static boolean FACULTATIVE = false;
	final int modCount = 1000;

	private final IService service = null;

	//different type of stores that are used by the known imports
	Map<String, MapWrapper<? extends CdmBase>> stores = new HashMap<String, MapWrapper<? extends CdmBase>>();

	public CdmApplicationAwareDefaultExport(){
		stores.put(ICdmIO.TEAM_STORE, new MapWrapper<TeamOrPersonBase>(service));
		stores.put(ICdmIO.REFERENCE_STORE, new MapWrapper<Reference>(service));
		stores.put(ICdmIO.NOMREF_STORE, new MapWrapper<Reference>(service));
		stores.put(ICdmIO.TAXONNAME_STORE, new MapWrapper<TaxonNameBase>(service));
		stores.put(ICdmIO.TAXON_STORE, new MapWrapper<TaxonBase>(service));
		stores.put(ICdmIO.SPECIMEN_STORE, new MapWrapper<DerivedUnit>(service));
	}


	@Override
    public boolean invoke(IExportConfigurator config){
		if (config.getCheck().equals(IExportConfigurator.CHECK.CHECK_ONLY)){
			return doCheck(config);
		}else if (config.getCheck().equals(IExportConfigurator.CHECK.CHECK_AND_EXPORT)){
			doCheck(config);
			return doExport(config);
		}else if (config.getCheck().equals(IExportConfigurator.CHECK.EXPORT_WITHOUT_CHECK)){
			return doExport(config);
		}else{
			logger.error("Unknown CHECK type");
			return false;
		}
	}


    public ExportResult execute(IExportConfigurator config) {
	    ExportResult result = new ExportResult();
	    if (config.getCheck().equals(IExportConfigurator.CHECK.CHECK_ONLY)){
            result.setSuccess(doCheck(config));
        } else if (config.getCheck().equals(IExportConfigurator.CHECK.CHECK_AND_EXPORT)){
            boolean success =  doCheck(config);
            if(success) {
               success = doExport(config, result);
            }
            result.setSuccess(success);

        } else if (config.getCheck().equals(IExportConfigurator.CHECK.EXPORT_WITHOUT_CHECK)){
            doExport(config, result);
        } else{
            logger.error("Unknown CHECK type");
            return null;
        }
	    return result;
	}

	@SuppressWarnings("unchecked")
	protected <S extends IExportConfigurator> boolean doCheck(S  config){
		boolean result = true;

		//check
		if (config == null){
			logger.warn("CdmExportConfiguration is null");
			return false;
		}else if (! config.isValid()){
			logger.warn("CdmExportConfiguration is not valid");
			return false;
		}
		System.out.println("Start checking Source ("+ config.getSourceNameString() + ") ...");

		ExportStateBase state = config.getNewState();
		state.initialize(config);

		//do check for each class
		for (Class<ICdmExport> ioClass: config.getIoClassList()){
			try {
				String ioBeanName = getComponentBeanName(ioClass);
				ICdmIO cdmIo = applicationContext.getBean(ioBeanName, ICdmIO.class);
				if (cdmIo != null){
					registerObservers(config, cdmIo);
					result &= cdmIo.check(state);
					unRegisterObservers(config, cdmIo);
				}else{
					logger.error("cdmIO for class " + (ioClass == null ? "(null)" : ioClass.getSimpleName()) + " was null");
					result = false;
				}
			} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
					result = false;
			}
		}

		//return
		System.out.println("End checking Source ("+ config.getSourceNameString() + ") for export from Cdm");
		return result;

	}


	private void registerObservers(IExportConfigurator config, ICdmIO io){
		for (IIoObserver observer : config.getObservers()){
			io.addObserver(observer);
		}
	}

	private void unRegisterObservers(IExportConfigurator config, ICdmIO io){
		for (IIoObserver observer : config.getObservers()){
			io.removeObserver(observer);
		}
	}

	protected <CONFIG extends IExportConfigurator>  boolean doExport(CONFIG config) {
	    ExportResult exportResult = new ExportResult();
	    return doExport(config, exportResult);

	}

	/**
	 * Executes the whole
	 */
	protected <CONFIG extends IExportConfigurator>  boolean doExport(CONFIG config, ExportResult exportResult){
		boolean result = true;
		//validate
		if (config == null){
			logger.warn("Configuration is null");
			exportResult.setSuccess(false);
			return false;
		}else if (! config.isValid()){
			logger.warn("Configuration is not valid");
			exportResult.setSuccess(false);
			return false;
		}

		System.out.println("Start export from source '" + config.getSourceNameString()
				+ "' to destination '" + config.getDestinationNameString() + "'");

		ExportStateBase state = config.getNewState();
		state.initialize(config);

		//do invoke for each class
		for (Class<ICdmExport> ioClass: config.getIoClassList()){
			try {
				String ioBeanName = getComponentBeanName(ioClass);
				ICdmExport cdmIo = applicationContext.getBean(ioBeanName, ICdmExport.class);
				if (cdmIo != null){
					//result &= cdmIo.invoke(config, stores);
					state.setCurrentIO(cdmIo);
					result &= cdmIo.invoke(state);
					if (config.getTarget().equals(TARGET.EXPORT_DATA)){
					    exportResult.addExportData(cdmIo.getByteArray());
					}
//					IoState<S> state = null;
//					result &= cdmIo.invoke(state);
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

		System.out.println("End export from source '" + config.getSourceNameString()
				+ "' to destination '" + config.getDestinationNameString() + "' " +
				(result? "(successful)":"(with errors)")) ;
		exportResult.setSuccess(result);
		return result;
	}

	private String getComponentBeanName(Class<ICdmExport> ioClass){
		Component component = ioClass.getAnnotation(Component.class);
		String ioBean = component.value();
		if ("".equals(ioBean)){
			ioBean = ioClass.getSimpleName();
			ioBean = ioBean.substring(0, 1).toLowerCase() + ioBean.substring(1); //make camelcase
		}
		return ioBean;
	}

}
