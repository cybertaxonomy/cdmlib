/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.event.spi.MergeContext;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.MergeEventListener;
import org.hibernate.event.spi.PersistContext;
import org.hibernate.event.spi.PersistEvent;
import org.hibernate.event.spi.PersistEventListener;

/**
 * Generates identifiable caches early in the persistence lifecycle so values
 * are present when Hibernate captures insert/update state.
 * <p>
 * See also {@link CdmListenerIntegrator}.
 *
 * @author a.mueller
 * @since 04.03.2009
 */
public class CacheStrategyGeneratorListener
        implements MergeEventListener, PersistEventListener {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Override
    public void onPersist(PersistEvent event) throws HibernateException {
        persistOrMerge(event.getObject());
    }

    @Override
    public void onPersist(PersistEvent event, PersistContext createdAlready) throws HibernateException {
        // Cascading persist visits entities via this overload as well.
        persistOrMerge(event.getObject());
    }

    @Override
    public void onMerge(MergeEvent event) throws HibernateException {
        // Always update caches for (session bound) merge result
        // If no result exists (e.g. because original was not yet persisted
        // update the original. Always updating the original may lead to LIEs.
        if (event.getResult() != null && event.getResult() != event.getOriginal()) {
            persistOrMerge(event.getResult());
        }else {
            persistOrMerge(event.getOriginal());
        }
    }

    @Override  //for cascading merges
    public void onMerge(MergeEvent event, MergeContext copiedAlready) throws HibernateException {
        onMerge(event);  //copiedAlready not needed here, so we link to onMerge(event)
    }

    private void persistOrMerge(Object entity) {
        CdmPreDataChangeListener.generateCaches(entity);
    }
}