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
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author cmathew
 * @date 23 Sep 2015
 *
 */
public class PostMergeEntityListener implements MergeEventListener {
    private static final long serialVersionUID = 1565797119368313987L;

    private static Map<Session, Set<CdmBase>> newEntitiesMap = new ConcurrentHashMap<>();

    public static void addSession(Session session) {
        newEntitiesMap.put(session, new HashSet<>());
    }

    public static void removeSession(Session session) {
        newEntitiesMap.remove(session);
    }

    public static Set<CdmBase> getNewEntities(Session session) {
        return newEntitiesMap.get(session);
    }

    @Override
    public void onMerge(MergeEvent event) throws HibernateException {
        Object entity = event.getEntity();

    }

    @Override
    public void onMerge(MergeEvent event, Map copiedAlready) throws HibernateException {
        // any new entities are added to a map which is retrieved at the end of the
        // CdmEntityDaoBase.merge(T transientObject, boolean returnTransientEntity) call
        if(event.getOriginal() != null && CdmBase.class.isAssignableFrom(event.getOriginal().getClass()) &&
                event.getResult() != null && CdmBase.class.isAssignableFrom(event.getResult().getClass())) {
            CdmBase original = (CdmBase) event.getOriginal();
            CdmBase result = (CdmBase) event.getResult();
            removeNullFromCollections(result);
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

    /**
     * @param entity
     */
    private static void removeNullFromCollections(Object entity) {
        if (entity != null){
            Class<?> entityClazz = entity.getClass();

            if (TaxonNode.class.isAssignableFrom(entityClazz)){
                TaxonNode node = (TaxonNode)entity;
                node.removeNullValueFromChildren();
            } else if (PolytomousKeyNode.class.isAssignableFrom(entityClazz)){
                PolytomousKeyNode node = (PolytomousKeyNode) entity;
                if (node.getChildren() != null && Hibernate.isInitialized(node.getChildren()) ){
                    node.removeNullValueFromChildren();

                }

            }   else if(FeatureTree.class.isAssignableFrom(entityClazz)){

                FeatureTree tree = (FeatureTree)entity;
                for (FeatureNode node:tree.getRootChildren()){
                    node.removeNullValueFromChildren();
                    if (node.getChildNodes() != null){
                        for (FeatureNode childNode: node.getChildNodes()){
                            removeNullFromCollections(childNode);
                        }
                    }

                }
            } else if (FeatureNode.class.isAssignableFrom(entityClazz)){
                FeatureNode node = (FeatureNode)entity;
                node.removeNullValueFromChildren();
            }

        }

    }

}
