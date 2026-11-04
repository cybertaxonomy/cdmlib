/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import jakarta.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Exposes the Hibernate {@link SessionFactory} that backs a JPA
 * {@link EntityManagerFactory}. Under Hibernate ORM 6 the SessionFactory
 * implements EntityManagerFactory; {@link #getObject()} returns that same
 * instance via {@link EntityManagerFactory#unwrap(Class)}.
 * <p>
 * Used so existing CDM code can keep injecting {@code sessionFactory} while
 * the persistence unit is bootstrapped via
 * {@link org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean}.
 */
public class SessionFactoryFromEntityManagerFactoryBean implements FactoryBean<SessionFactory> {

    private EntityManagerFactory entityManagerFactory;

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public SessionFactory getObject() {
        return entityManagerFactory.unwrap(SessionFactory.class);
    }

    @Override
    public Class<?> getObjectType() {
        return SessionFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
