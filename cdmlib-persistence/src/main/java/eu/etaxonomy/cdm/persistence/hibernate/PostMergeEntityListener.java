// $Id$
/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.MergeEventListener;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author cmathew
 * @date 23 Sep 2015
 *
 */
public class PostMergeEntityListener implements MergeEventListener {

    private static Map<Session, Set<CdmBase>> newEntitiesMap = new ConcurrentHashMap<Session, Set<CdmBase>>();

    public static void addSession(Session session) {
        newEntitiesMap.put(session, new HashSet<CdmBase>());
    }

    public static void removeSession(Session session) {
        newEntitiesMap.remove(session);
    }

    public static Set<CdmBase> getNewEntities(Session session) {
        return newEntitiesMap.get(session);
    }


    /* (non-Javadoc)
     * @see org.hibernate.event.spi.MergeEventListener#onMerge(org.hibernate.event.spi.MergeEvent)
     */
    @Override
    public void onMerge(MergeEvent event) throws HibernateException {

    }

    /* (non-Javadoc)
     * @see org.hibernate.event.spi.MergeEventListener#onMerge(org.hibernate.event.spi.MergeEvent, java.util.Map)
     */
    @Override
    public void onMerge(MergeEvent event, Map copiedAlready) throws HibernateException {
        // any new entities are added to a map which is retrieved at the end of the
        // CdmEntityDaoBase.merge(T transientObject, boolean returnTransientEntity) call
        if(event.getOriginal() != null && CdmBase.class.isAssignableFrom(event.getOriginal().getClass()) &&
                event.getResult() != null && CdmBase.class.isAssignableFrom(event.getResult().getClass())) {
            CdmBase original = (CdmBase) event.getOriginal();
            CdmBase result = (CdmBase) event.getResult();
            if(original != null && Hibernate.isInitialized(original) && original.getId() == 0 &&
                    result != null && Hibernate.isInitialized(result) && result.getId() > 0) {
                original.setId(result.getId());
                Set<CdmBase> newEntities = newEntitiesMap.get(event.getSession());
                if(newEntities != null) {
                    newEntities.add(result);
                }
            }
        }
    }

}
