/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.MergeEventListener;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.spi.SaveOrUpdateEventListener;

/**
 * @author a.mueller
 * @since 04.03.2009
 */
public class CacheStrategyGenerator implements SaveOrUpdateEventListener, MergeEventListener {
    private static final long serialVersionUID = -5511287200489449838L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(CacheStrategyGenerator.class);

    @Override
    public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException {
        Object entity = event.getObject();
        saveOrUpdateOrMerge(entity);
    }

    @Override
    public void onMerge(MergeEvent event) throws HibernateException {
        Object entity = event.getOriginal();
        saveOrUpdateOrMerge(entity);
    }

    @Override
    public void onMerge(MergeEvent event, Map copiedAlready) throws HibernateException {

    }

    private void saveOrUpdateOrMerge(Object entity) {
        CdmPreDataChangeListener.generateCaches(entity);
    }
}
