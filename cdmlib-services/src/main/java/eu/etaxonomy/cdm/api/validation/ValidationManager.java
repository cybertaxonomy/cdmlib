// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.validation;

import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultDao;
import eu.etaxonomy.cdm.persistence.hibernate.Level2ValidationEventListener;
import eu.etaxonomy.cdm.persistence.hibernate.Level3ValidationEventListener;
import eu.etaxonomy.cdm.persistence.validation.ValidationExecutor;

/**
 * This bean
 *
 * @author a.mueller
 * @date 09.01.2015
 *
 */
@Component
public class ValidationManager {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
//    IEntityValidationResultService validationService;
    IEntityValidationResultDao validationService;


    public void registerValidationListeners(){
        if (sessionFactory != null && sessionFactory instanceof SessionFactoryImpl){
            ServiceRegistryImplementor serviceRegistry = ((SessionFactoryImpl)sessionFactory).getServiceRegistry();

            final EventListenerRegistry eventRegistry = serviceRegistry.getService(EventListenerRegistry.class);

            //duplication strategy
//            eventRegistry.addDuplicationStrategy(CdmListenerDuplicationStrategy.NewInstance);


            ValidationExecutor validationExecutor = new ValidationExecutor();

            //level2
            Level2ValidationEventListener l2Listener = new Level2ValidationEventListener(validationService);
            l2Listener.setValidationExecutor(validationExecutor);

            //level3
            Level3ValidationEventListener l3Listener = new Level3ValidationEventListener(validationService);
            l3Listener.setValidationExecutor(validationExecutor);

            // prepend to register before or append to register after

            eventRegistry.appendListeners(EventType.POST_INSERT, /*new CdmPostDataChangeObservableListener(),*/ l2Listener, l3Listener);
            eventRegistry.appendListeners(EventType.POST_UPDATE, /*new CdmPostDataChangeObservableListener(),*/ l2Listener, l3Listener);
            eventRegistry.appendListeners(EventType.POST_DELETE, /*new CdmPostDataChangeObservableListener(),*/ l3Listener);

        }else{
            throw new RuntimeException("Session factory not available or not of type SessionFactoryImpl");
        }

    }

}
