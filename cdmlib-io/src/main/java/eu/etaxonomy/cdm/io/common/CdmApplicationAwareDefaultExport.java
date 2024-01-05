/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * This class is an default exporter class that is a spring bean and therefore it knows all other IO classes that are beans
 *
 * @author a.mueller
 * @since 20.06.2008
 */
@Component("defaultExport")
public class CdmApplicationAwareDefaultExport<T extends IExportConfigurator>
        implements ICdmExporter<T>, ApplicationContextAware {

    private static final Logger logger = LogManager.getLogger();

	protected ApplicationContext applicationContext;

	@Override
    public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	//Constants
	final static boolean OBLIGATORY = true;
	final static boolean FACULTATIVE = false;
	final int modCount = 1000;

	private final IService service = null;

	//different type of stores that are used by the known imports
	Map<String, MapWrapper<? extends CdmBase>> stores = new HashMap<>();

	public CdmApplicationAwareDefaultExport(){
		stores.put(ICdmIO.TEAM_STORE, new MapWrapper<>(service));
		stores.put(ICdmIO.REFERENCE_STORE, new MapWrapper<>(service));
		stores.put(ICdmIO.NOMREF_STORE, new MapWrapper<>(service));
		stores.put(ICdmIO.TAXONNAME_STORE, new MapWrapper<>(service));
		stores.put(ICdmIO.TAXON_STORE, new MapWrapper<>(service));
		stores.put(ICdmIO.SPECIMEN_STORE, new MapWrapper<>(service));
	}


	@Override
    public ExportResult invoke(T config){
	    ExportResult result;
	    if (config.getCheck().equals(IExportConfigurator.CHECK.CHECK_ONLY)){
		    result = ExportResult.NewInstance(config.getResultType());
		    boolean success =  doCheck(config);
		    if (! success){
		        result.setAborted();
		    }
		}else if (config.getCheck().equals(IExportConfigurator.CHECK.CHECK_AND_EXPORT)){
		    boolean success = doCheck(config);
		    if (success){
		        result = doExport(config);
		    }else{
		        result = ExportResult.NewInstance(config.getResultType());
	            result.setAborted();
		    }
		}else if (config.getCheck().equals(IExportConfigurator.CHECK.EXPORT_WITHOUT_CHECK)){
			result = doExport(config);
		}else{
		    result = ExportResult.NewInstance(config.getResultType());
            String message = "Unknown CHECK type";
            logger.error(message);
            result.addError(message);
 		}
		return result;
	}


    public ExportResult execute(T config) {
	    ExportResult result = ExportResult.NewInstance(config.getResultType());
	    if (config.getCheck().equals(IExportConfigurator.CHECK.CHECK_ONLY)){
	        boolean success =  doCheck(config);
            if (! success){
                result.setAborted();
            }
        } else if (config.getCheck().equals(IExportConfigurator.CHECK.CHECK_AND_EXPORT)){
            boolean success = doCheck(config);
            if (success){
                result = doExport(config);
            }else{
                result.setAborted();
            }
        } else if (config.getCheck().equals(IExportConfigurator.CHECK.EXPORT_WITHOUT_CHECK)){
            result = doExport(config);
        } else{
            String message = "Unknown CHECK type";
            logger.error(message);
            result.addError(message);
        }
	    return result;
	}

	@SuppressWarnings("unchecked")
	protected <S extends IExportConfigurator> boolean doCheck(S  config){

	    boolean result = true;

		//check
		if (config == null){
			logger.warn("CdmExportConfiguration is null");
//			result.setState(ExportResultState.ABORTED);
			return false;
		}else if (! config.isValid()){
			logger.warn("CdmExportConfiguration is not valid");
//			result.setState(ExportResultState.ABORTED);
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
				    String message = "cdmIO for class " + (ioClass == null ? "(null)" : ioClass.getSimpleName()) + " was null";
					logger.error(message);
					return false;
				}
			} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
					return false;
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



	/**
	 * Executes the whole
	 */
	protected <CONFIG extends T>  ExportResult doExport(CONFIG config){
		//validate
		if (config == null){
		    ExportResult result = ExportResult.NewInstance(null);
		    String message = "Configuration is null";
			logger.error(message);
			result.addError(message);
			result.setAborted();
			return result;
		}
		ExportResult result = ExportResult.NewInstance(config.getResultType());
		if (! config.isValid()){
			String message = "Configuration is not valid";
		    logger.error(message);
		    result.addError(message);
			result.setAborted();
			return result;
		}

		ExportStateBase state = config.getNewState();
		state.initialize(config);
		state.setResult(result);

		List<ICdmExport> ioList = makeIoList(state, config);

		List<Integer> stepCounts = countSteps(state, ioList);
		Integer totalCount = stepCounts.get(stepCounts.size()-1);
		config.getProgressMonitor().beginTask(config.getUserFriendlyIOName() != null? config.getUserFriendlyIOName():"Start Export", totalCount);
		config.getProgressMonitor().worked(1);
		IProgressMonitor parentMonitor = SubProgressMonitor
		        .NewStarted(config.getProgressMonitor(), 99, "Process data", totalCount);

		//do invoke for each class
		for (int i = 0; i< ioList.size(); i++){
		    @SuppressWarnings("rawtypes")
            ICdmExport export = ioList.get(i);
		    Integer counts = stepCounts.get(i);
			try {
			    String ioName = export.getClass().getSimpleName();
			    SubProgressMonitor ioMonitor = SubProgressMonitor
			            .NewStarted(parentMonitor, counts, ioName, counts );
//			    state.getConfig().setProgressMonitor(ioMonitor);
			    state.setCurrentIO(export);
				export.invoke(state);
				ioMonitor.done();
			} catch (Exception e) {
				String message = "Unexpected exception in " + export.getClass().getSimpleName()+ ": " + e.getMessage();
				logger.error(message);
				e.printStackTrace();
		        result.addException(e, message);
			}
		}

		System.out.println("End export from source '" + config.getSourceNameString()
				+ "' to destination '" + config.getDestinationNameString() + "' "
				+ "("+ result.toString() + ")"
				) ;
		return result;
	}

    private List<Integer> countSteps(ExportStateBase state, List<ICdmExport> ioList) {
        //do invoke for each class
        List<Integer> result = new ArrayList<>();
        int sum = 0;
        for (ICdmExport export: ioList){
            int count = 1;
            try {
//                state.setCurrentIO(export);
                count = ((Long)export.countSteps(state)).intValue();
            } catch (Exception e) {
                String message = "Unexpected exception when count steps for progress monitoring " + export.getClass().getSimpleName()+ ": " + e.getMessage();
                logger.error(message);
                e.printStackTrace();
                state.getResult().addException(e, message);
            }
            result.add(count);
            sum += count;
        }
        result.add(sum);
        return result;
    }

    private <CONFIG extends T>  List<ICdmExport> makeIoList(ExportStateBase state, CONFIG config) {

        List<ICdmExport> result = new ArrayList<>();

        for (Class<ICdmExport> ioClass: config.getIoClassList()){
            try {
                String ioBeanName = getComponentBeanName(ioClass);
                ICdmExport cdmIo = applicationContext.getBean(ioBeanName, ICdmExport.class);
                if (cdmIo != null){
                    result.add(cdmIo);
                }else{
                    String message = "cdmIO was null: " + ioBeanName;
                    logger.error(message);
                    state.getResult().addError(message);
                }
            } catch (Exception e) {
                    String message = "Unexpected exception in " + ioClass.getSimpleName()+ ": " + e.getMessage();
                    logger.error(message);
                    e.printStackTrace();
                    state.getResult().addException(e, message);
            }
        }
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
