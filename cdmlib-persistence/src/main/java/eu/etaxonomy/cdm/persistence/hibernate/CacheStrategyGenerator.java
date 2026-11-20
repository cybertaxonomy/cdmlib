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
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.spi.SaveOrUpdateEventListener;

/**
 * Generates identifiable caches early in the persistence lifecycle so values
 * are present when Hibernate captures insert/update state.
 * <p>
 * Registered for legacy {@code SAVE}/{@code SAVE_UPDATE}/{@code UPDATE} and for
 * JPA {@code PERSIST}/{@code MERGE} (see {@link CdmListenerIntegrator}).
 *
 * @author a.mueller
 * @since 04.03.2009
 */
public class CacheStrategyGenerator
        implements SaveOrUpdateEventListener, MergeEventListener, PersistEventListener {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Override
    public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException {
        Object entity = event.getObject();
        saveOrUpdateOrMerge(entity);
    }

    @Override
    public void onPersist(PersistEvent event) throws HibernateException {
        saveOrUpdateOrMerge(event.getObject());
    }

    @Override
    public void onPersist(PersistEvent event, PersistContext createdAlready) throws HibernateException {
        // Cascading persist visits entities via this overload as well.
        saveOrUpdateOrMerge(event.getObject());
    }

    @Override
    public void onMerge(MergeEvent event) throws HibernateException {
        // Prefer regenerating on the incoming instance so merge copies caches
        // onto the managed result; also cover result when already set.
        saveOrUpdateOrMerge(event.getOriginal());
        if (event.getResult() != null && event.getResult() != event.getOriginal()) {
            saveOrUpdateOrMerge(event.getResult());
        }
    }

    @Override
    public void onMerge(MergeEvent event, MergeContext copiedAlready) throws HibernateException {
        onMerge(event);
    }

    private void saveOrUpdateOrMerge(Object entity) {
        CdmPreDataChangeListener.generateCaches(entity);
    }
}
