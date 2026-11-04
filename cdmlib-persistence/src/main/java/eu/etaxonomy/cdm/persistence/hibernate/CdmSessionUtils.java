/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.persistence.EntityManager;

/**
 * Resolves the persistence context that participates in the current Spring
 * transaction. Prefer the JPA {@link EntityManagerHolder} bound by
 * {@code JpaTransactionManager}; fall back to {@link SessionHolder} /
 * {@link SessionFactory#getCurrentSession()} for native Hibernate TX setups.
 */
public final class CdmSessionUtils {

    private CdmSessionUtils() {
    }

    /**
     * Transaction-bound {@link EntityManager} (no Hibernate {@link Session} unwrap).
     * Prefer this when only JPA API is needed ({@code flush}, {@code clear}, …).
     */
    //can be replaced by Spring internal EntityManagerFactoryUtils.getTransactionalEntityManager(emf)
    //in case we do not want to support the fallbacks anymore
    public static EntityManager getTransactionalEntityManager(SessionFactory sessionFactory) {
        Object resource = TransactionSynchronizationManager.getResource(sessionFactory);
        if (resource instanceof EntityManagerHolder) {
            EntityManagerHolder emHolder = (EntityManagerHolder) resource;
            EntityManager em = emHolder.getEntityManager();
            if (em != null && em.isOpen()) {
                return em;
            }
        }
        if (resource instanceof SessionHolder) {
            return ((SessionHolder) resource).getSession();
        }
        try {
            return sessionFactory.getCurrentSession();
        } catch (HibernateException e) {
            throw new HibernateException(
                    "No transactional Session/EntityManager bound for " + sessionFactory
                            + ". Ensure JpaTransactionManager (or HibernateTransactionManager) "
                            + "is active before DAO access.", e);
        }
    }

    public static Session getTransactionalSession(SessionFactory sessionFactory) {
        EntityManager em = getTransactionalEntityManager(sessionFactory);
        return em instanceof Session ? (Session) em : em.unwrap(Session.class);
    }
}
