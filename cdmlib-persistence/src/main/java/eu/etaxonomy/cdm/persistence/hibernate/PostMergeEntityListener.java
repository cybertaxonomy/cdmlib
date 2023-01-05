/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.MergeEventListener;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ITreeNode;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;

/**
 * @author cmathew
 * @since 23 Sep 2015
 */
public class PostMergeEntityListener implements MergeEventListener {

    private static final long serialVersionUID = 1565797119368313987L;
    private static final Logger logger = LogManager.getLogger();

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
        //Note AM: Added in context of working on #10101, it was unclear to me
        //         why this method was not implemented before. Debugging the merge process
        //         revealed that this method was primarily called on the first entity
        //         to merge (as copied already is not necessary then).
        //         So setting the ID and adding to the newEntitiesMap seems
        //         not to be implemented for entities handle only here.
        //         Needs intensive testing if there is not maybe a reason why the method
        //         was not implemented.
        onMerge(event, new HashMap<>());
    }

    @Override
    public void onMerge(MergeEvent event, Map copiedAlready) throws HibernateException {
        // any new entities are added to a map which is retrieved at the end of the
        // CdmEntityDaoBase.merge(T transientObject, boolean returnTransientEntity) call
        if(event.getOriginal() != null && CdmBase.class.isAssignableFrom(event.getOriginal().getClass()) &&
                event.getResult() != null && CdmBase.class.isAssignableFrom(event.getResult().getClass())) {
            CdmBase original = (CdmBase) event.getOriginal();
            CdmBase result = (CdmBase) event.getResult();
            handleTreeNodes(result, original, event, copiedAlready);
            if(original != null && Hibernate.isInitialized(original) && original.getId() == 0 &&
                    result != null && Hibernate.isInitialized(result) && result.getId() > 0) {
                //see IService#merge(detachedObject, returnTransientEntity)
                original.setId(result.getId());
                Set<CdmBase> newEntities = newEntitiesMap.get(event.getSession());
                if(newEntities != null) {
                    newEntities.add(result);
                }
            }
        }
    }

    private static void handleTreeNodes(CdmBase result, CdmBase original, MergeEvent event, Map copiedAlready) {
        if (original != null){
            Class<?> entityClazz = original.getClass();

            if (PolytomousKeyNode.class.isAssignableFrom(entityClazz)){
                //For some reason the children list needs to be read once
                //to guarantee that the sortindex starts with zero
                PolytomousKeyNode resultNode = (PolytomousKeyNode)result;
                resultNode.getChildren().size();

                // #10101 the following code tried to handle orphanRemoval for key nodes that were
                // really removed from the graph. Generally the removal worked but it was not possible at this
                // place to guarantee that the node which was removed from the parent was not used elsewhere
                // (has a new parent). For this one needs to retrieve the new state of the node (or of its new parent).
                // But this is not difficult or impossible at this point as the node is not part of the graph to
                // to be merged or if it is because its new parent is part of the graph also, it is not guaranteed
                // that it has already treated so far.
                // Maybe it can better be handled by another type of listener therefore I keep this code here.
                // See #10101 for further information on this issue and how it was solved.
                // The implementation was partly copied from https://stackoverflow.com/questions/812364/how-to-determine-collection-changes-in-a-hibernate-postupdateeventlistener

//                EventSource session = event.getSession();
//                PersistenceContext pc = session.getPersistenceContext();
//                CollectionEntry childrenEntry = pc.getCollectionEntry((PersistentCollection)resultNode.getChildren());
//                List<PolytomousKeyNode> childrenEntrySnapshot = (List<PolytomousKeyNode>)childrenEntry.getSnapshot();
//                if (childrenEntrySnapshot != null) {
//                    for (PolytomousKeyNode snapshotChild: childrenEntrySnapshot){
//                        if (!resultNode.getChildren().contains(snapshotChild)) {
//                            EntityEntry currentChild = pc.getEntry(snapshotChild);
//                            Object parent = currentChild == null ? null :
//                                currentChild.getLoadedValue("parent");
//                            if (parent == null || parent == resultNode) {
//                                session.delete(snapshotChild);
//                            }
//                        }
//                   }
//                }
            } else if (ITreeNode.class.isAssignableFrom(entityClazz)){ //TaxonNode or TermNode
                //See PolytomousKeyNode above
                //Not yet tested if necessary here, too.

                try {
                    ITreeNode<?> resultNode = (ITreeNode<?>)result;
                    resultNode.getChildNodes().size();
                } catch (Exception e) {
                    //#10101
                    //preliminary catched and logged as it seems to be the cause
                    //for failing TaxEditor tests in TaxonNameEditorTest
                    //methods
                    //    * addDeleteAddHomotypicSynonym,
                    //    * addDeleteAddHomotypicSynonymWithAnnotations
                    //    * addHeterotypicSynonym
                    //    * testAddHomotypicSynonym
                    //All due to failing to lazily initialize a collection of role: eu.etaxonomy.cdm.model.taxon.TaxonNode.childNodes, could not initialize proxy - no Session
                    //We need to check if this is an issue in the test behavior or in the solution itself.
                    //We could also try to at least add a check if the children list is attached to a session
                    //before initializing it.
                    //
                    logger.warn("Error in PostMergeEntityListener during handleTreeNodes: " + e.getMessage());
                }
            }
        }
    }
}