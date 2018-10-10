/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.config;

import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.etaxonomy.cdm.persistence.hibernate.TaxonGraphHibernateListener;

/**
 * @author a.kohlbecker
 * @since Oct 10, 2018
 *
 */
@Configuration
public class CdmHibernateListenerConfiguration {

    @Autowired
    SessionFactory sessionFactory;

    @Bean
    public TaxonGraphHibernateListener taxonGraphHibernateListener(){

        TaxonGraphHibernateListener taxonGraphHibernateListener = new TaxonGraphHibernateListener();
        EventListenerRegistry listenerRegistry = ((SessionFactoryImpl) sessionFactory).getServiceRegistry().getService(
                EventListenerRegistry.class);

        listenerRegistry.appendListeners(EventType.POST_UPDATE, taxonGraphHibernateListener);
        listenerRegistry.appendListeners(EventType.POST_INSERT, taxonGraphHibernateListener);
        return taxonGraphHibernateListener;
    }

}
