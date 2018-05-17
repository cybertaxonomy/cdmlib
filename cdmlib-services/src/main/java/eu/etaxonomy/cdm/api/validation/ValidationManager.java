/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.validation;

import javax.annotation.PostConstruct;

import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.validation.batch.BatchValidator;
import eu.etaxonomy.cdm.api.validation.batch.ValidationScheduler;
import eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationCrudJdbcImpl;
import eu.etaxonomy.cdm.persistence.hibernate.Level2ValidationEventListener;
import eu.etaxonomy.cdm.persistence.hibernate.Level3ValidationEventListener;
import eu.etaxonomy.cdm.persistence.validation.ValidationExecutor;

/**
 * This bean
 *
 * @author a.mueller
 * @since 09.01.2015
 *
 */
@Component
@Lazy(false)
public class ValidationManager {

    private boolean validationEnabled = true;
    private final boolean batchValidationEnabled = true;

    private boolean level2Enabled = false;
    private boolean level3Enabled = false;

    private boolean isInitialized  = false;

    private Level2ValidationEventListener l2Listener;
    private Level3ValidationEventListener l3Listener;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private BatchValidator batchValidator;

    @Autowired
    @Qualifier("cdmRepository")
    private ICdmRepository cdmRepository;

    @Autowired
//    IEntityValidationService validationService;
//    IEntityValidationCrud validationService;
    private EntityValidationCrudJdbcImpl validationDao;

//    private TaskExecutor taskExecutor;

    private TaskScheduler scheduler;

    @PostConstruct
    public void initializeManager(){
        registerValidationListeners();
        initTaskExecutor();
    }


    /**
     *
     */
    private void initTaskExecutor() {
//        taskExecutor = new ThreadPoolTaskExecutor();
        ValidationScheduler validationScheduler = new ValidationScheduler();
        validationScheduler.initialize();
        scheduler = validationScheduler;
        scheduler.scheduleWithFixedDelay(batchValidator, 5000);
        //TODO how to disable scheduling if not wanted for a certain time

    }

    public void startBatchValidation(){
        batchValidator.run();
    }


    public void registerValidationListeners(){
        if (!isInitialized){
            if (sessionFactory != null && sessionFactory instanceof SessionFactoryImpl){
                ServiceRegistryImplementor serviceRegistry = ((SessionFactoryImpl)sessionFactory).getServiceRegistry();

                final EventListenerRegistry eventRegistry = serviceRegistry.getService(EventListenerRegistry.class);

                //duplication strategy
    //            eventRegistry.addDuplicationStrategy(CdmListenerDuplicationStrategy.NewInstance);
                eventRegistry.getEventListenerGroup(EventType.POST_INSERT);

                ValidationExecutor validationExecutor = new ValidationExecutor();

                //level2
                l2Listener = new Level2ValidationEventListener(validationDao);
                l2Listener.setValidationExecutor(validationExecutor);

                //level3
                l3Listener = new Level3TransactionalValidationEventListener(cdmRepository, validationDao);
                l3Listener.setValidationExecutor(validationExecutor);

                // prepend to register before or append to register after

                eventRegistry.appendListeners(EventType.POST_INSERT, l2Listener , l3Listener);
                eventRegistry.appendListeners(EventType.POST_UPDATE, l2Listener , l3Listener);
                //TODO don't we need l2Listener validation also for deleting the results?
                eventRegistry.appendListeners(EventType.POST_DELETE, l3Listener);

                isInitialized = true;

            }else{
                throw new RuntimeException("Session factory not available or not of type SessionFactoryImpl");
            }
        }
    }

    //for future use
    private void enableLevel2Listener(boolean enabled){
        level2Enabled = enabled;
        l2Listener.setEnabled(level2Enabled && validationEnabled);
    }

    private void enableLevel3Listener(boolean enabled){
        level3Enabled = enabled;
        l3Listener.setEnabled(level3Enabled && validationEnabled);
    }

    private void enableValidation(boolean enabled){
        validationEnabled = enabled;
        l2Listener.setEnabled(level2Enabled);
        l3Listener.setEnabled(level3Enabled);
    }

}
