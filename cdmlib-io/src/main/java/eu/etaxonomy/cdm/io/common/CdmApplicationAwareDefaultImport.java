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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionEvaluator;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */

@Component("defaultImport")
public class CdmApplicationAwareDefaultImport<T extends IImportConfigurator> implements ICdmImporter<T>, ApplicationContextAware {
    private static final Logger logger = Logger.getLogger(CdmApplicationAwareDefaultImport.class);

    protected ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    //Constants
    final boolean OBLIGATORY = true;
    final boolean FACULTATIVE = false;
    final int modCount = 1000;

    IService<CdmBase> service = null;

    //different type of stores that are used by the known imports
    Map<String, MapWrapper<? extends CdmBase>> stores = new HashMap<>();

    public CdmApplicationAwareDefaultImport(){

    	stores.put(ICdmIO.TEAM_STORE, new MapWrapper<>(service));
        stores.put(ICdmIO.REFERENCE_STORE, new MapWrapper<>(service));
        stores.put(ICdmIO.NOMREF_STORE, new MapWrapper<>(service));
        stores.put(ICdmIO.TAXONNAME_STORE, new MapWrapper<>(service));
        stores.put(ICdmIO.TAXON_STORE, new MapWrapper<>(service));
        stores.put(ICdmIO.SPECIMEN_STORE, new MapWrapper<>(service));
    }


    @Override
    public ImportResult invoke(IImportConfigurator config){
        ImportResult result = new ImportResult();
        if (config.getCheck().equals(IImportConfigurator.CHECK.CHECK_ONLY)){
            boolean success =  doCheck(config);
            if (! success){
                result.setAborted();
            }
        }else if (config.getCheck().equals(IImportConfigurator.CHECK.CHECK_AND_IMPORT)){
            boolean success = doCheck(config);
            if (success){
                result = doImport(config);
            }else{
                result.setAborted();
            }
        }else if (config.getCheck().equals(IImportConfigurator.CHECK.IMPORT_WITHOUT_CHECK)){
            result = doImport(config);
        }else{
            String message = "Unknown CHECK type";
            logger.error(message);
            result.addError(message);
        }
        return result;
    }


//    public ImportResult execute(IImportConfigurator config){
//        ImportResult result = new ImportResult();
//        if (config.getCheck().equals(IImportConfigurator.CHECK.CHECK_ONLY)){
//            result.setSuccess(doCheck(config));
//        }else if (config.getCheck().equals(IImportConfigurator.CHECK.CHECK_AND_IMPORT)){
//            boolean success =  doCheck(config);
//            if(success) {
//               result = doImport(config);
//            }
//            result.setSuccess(success);
//        } else if (config.getCheck().equals(IImportConfigurator.CHECK.IMPORT_WITHOUT_CHECK)){
//            result = doImport(config);
//        } else{
//            logger.error("Unknown CHECK type");
//            return null;
//        }
//        return result;
//    }


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
        for (Class<ICdmImport> ioClass: config.getIoClassList()){
            try {
                String ioBeanName = getComponentBeanName(ioClass);
                ICdmIO cdmIo = applicationContext.getBean(ioBeanName, ICdmIO.class);
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
    protected <S extends IImportConfigurator>  ImportResult doImport(S config){
        ImportResult result = new ImportResult();
        //validate
        if (config == null){
            String message = "Configuration is null";
            logger.error(message);
            result.addError(message);
            result.setAborted();
            return result;
        }

        if (! config.isValid()){
            String message = "Configuration is not valid";
            logger.error(message);
            result.addError(message);
            result.setAborted();
            return result;
        }

        config.getSourceReference();
        logger.info("Start import from Source '"+ config.getSourceNameString() + "' to destination '" + config.getDestinationNameString() + "'");

        ImportStateBase state = config.getNewState();
        state.initialize(config);

        CdmPermissionEvaluator permissionEval = applicationContext.getBean("cdmPermissionEvaluator", CdmPermissionEvaluator.class);

        state.setSuccess(true);
        //do invoke for each class
        for (Class<ICdmImport> ioClass: config.getIoClassList()){
            try {
                String ioBeanName = getComponentBeanName(ioClass);
                ICdmImport cdmIo = applicationContext.getBean(ioBeanName, ICdmImport.class);
                if (cdmIo != null){
                    registerObservers(config, cdmIo);
                    state.setCurrentIO(cdmIo);
                    result = cdmIo.invoke(state);
                    unRegisterObservers(config, cdmIo);
                }else{
                    String message = "cdmIO was null";
                    logger.error(message);
                    result.addError(message);
                }
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
                result.addException(e);
            }
        }

        logger.info("End import from source '" + config.getSourceNameString()
                + "' to destination '" + config.getDestinationNameString() + "'"
                + "("+ result.toString() + ")"
                ) ;
        return result;
    }

    /**
     * Returns the name of a component bean. If the name is defined in the Component annotation this name is returned.
     * Otherwise the class name is returned with starting lower case.
     * @param ioClass
     * @return
     * @throws IllegalArgumentException if the class does not have a "Component" annotation
     */
    public static String getComponentBeanName(Class<ICdmImport> ioClass) throws IllegalArgumentException {
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

    public void authenticate(IImportConfigurator config) {
        UsernamePasswordAuthenticationToken token = config.getAuthenticationToken();
        if (token != null){
            SecurityContext context = SecurityContextHolder.getContext();

            AuthenticationManager authenticationManager = applicationContext.getBean("authenticationManager", AuthenticationManager.class);;
            Authentication authentication = authenticationManager.authenticate(token);
            context.setAuthentication(authentication);
        }else{
        	logger.warn("No authentication token available");
        }

    }

}
