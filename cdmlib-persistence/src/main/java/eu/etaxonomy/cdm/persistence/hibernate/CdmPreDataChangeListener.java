/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.joda.time.DateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.ITreeNode;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author cmathew
 * @since 7 Jul 2015
 */
public class CdmPreDataChangeListener
        implements PreInsertEventListener, PreUpdateEventListener {

    private static final long serialVersionUID = -7581071903134036209L;

    private static final Logger logger = LogManager.getLogger();

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        try {
            Object entity = event.getEntity();
            if (VersionableEntity.class.isAssignableFrom(entity.getClass())) {
                VersionableEntity versionableEntity = (VersionableEntity)entity;
                versionableEntity.setUpdated(new DateTime());
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
                    User user = (User)authentication.getPrincipal();
                    versionableEntity.setUpdatedBy(user);
                }
            }
            insertUpdateMerge(event.getEntity());
        } catch(Exception e) {
            logger.warn("There was an exception in onPreUpdate(): " + e.getMessage());
        }finally {
            return false;
        }
    }

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        try {
            Object entity = event.getEntity();
            Class<?> entityClazz = entity.getClass();
            if(ICdmBase.class.isAssignableFrom(entityClazz)) {
                ICdmBase cdmBase = (ICdmBase)entity;

                if (cdmBase.getCreated() == null){
                    cdmBase.setCreated(new DateTime());
                }
                if(cdmBase.getCreatedBy() == null) {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
                        User user = (User)authentication.getPrincipal();
                        cdmBase.setCreatedBy(user);
                    }
                }
            }
            insertUpdateMerge(entity);
        } catch(Exception e) {
            logger.warn("There was an exception in onPreInsert(): " + e.getMessage());
        }finally {
            return false;
        }
    }

    //from former SaveOrUpdateEntityListener
    public void insertUpdateMerge(Object entity){
        if(entity != null && CdmBase.class.isAssignableFrom(entity.getClass())){
            cacheDeterminationNames(entity);
            generateCaches(entity);
            //moved to CdmPreDataChangeListener
            if (entity instanceof ITreeNode) {
                    ITreeNode<?> node = (ITreeNode<?>)entity;
                    reindex(node);
                }
        }
    }

    static String sep = ITreeNode.separator;
    static String pref = ITreeNode.treePrefix;

    private <T extends ITreeNode> void reindex(T node) {
        String oldChildIndex = node.treeIndex();
        ITreeNode<?> parent = node.getParent();
        String parentIndex = (parent == null) ? (sep + pref + node.treeId() + sep)  : parent.treeIndex();  //TODO
        if (node.getId() > 0){   //TODO
            String newChildIndex = parentIndex + node.getId() + sep;
            if (oldChildIndex == null || ! oldChildIndex.equals(newChildIndex)){
                node.setTreeIndex(newChildIndex);

                //TODO this is a greedy implementation, better use update by replace string
                //either using and improving the below code or by using native SQL
                //The current approach may run out of memory for large descendant sets.
                List<T> childNodes = node.getChildNodes();
                for (T child : childNodes){
                    if (child != null && ! child.equals(node)){  //node should not be it's own child, however just in case
                        reindex(child);
                    }
                }

                //          String className = event.getEntityName();
                //                  String updateQuery = " UPDATE %s tn " +
                //                          " SET tn.treeIndex = Replace(tn.treeIndex, '%s', '%s') " +
                //                          " WHERE tn.id <> "+ node.getId()+" ";
                //                  updateQuery = String.format(updateQuery, className, oldChildIndex, parentIndex);  //dummy
                //                  System.out.println(updateQuery);
                //                  EventSource session = event.getSession();
                //                  Query query = session.createQuery(updateQuery);
                //                  query.executeUpdate();
            }
        }
    }

    private static void cacheDeterminationNames(Object entity) {
        if (entity instanceof DeterminationEvent) {
            DeterminationEvent detEv = (DeterminationEvent)entity;
            if (detEv.getTaxon() != null && detEv.getTaxonName() == null && detEv.getTaxon().getName() != null){
                detEv.setTaxonName(detEv.getTaxon().getName());
            }
        }
    }

    //TODO shouldn't we use the "updateCaches" method instead?
    public static void generateCaches(Object entity){
        if (entity != null){
            entity = CdmBase.deproxy(entity);
            Class<?> entityClazz = entity.getClass();

            if (IdentifiableEntity.class.isAssignableFrom(entityClazz)){
                IdentifiableEntity<?> identifiableEntity = (IdentifiableEntity<?>)entity;
                if(TaxonName.class.isAssignableFrom(entityClazz)) {
                    //non-viral-name caches  (was NonViralName)
                    TaxonName nonViralName = (TaxonName)entity;
                    nonViralName.getAuthorshipCache();
                    nonViralName.getNameCache();
                    nonViralName.getTitleCache();
                    nonViralName.getFullTitleCache();
                }else if(TeamOrPersonBase.class.isAssignableFrom(entityClazz)){
                    //team-or-person caches
                    TeamOrPersonBase<?> teamOrPerson = (TeamOrPersonBase<?>)entity;
                    if (teamOrPerson.isInstanceOf(Team.class)){
                        Team team = CdmBase.deproxy(teamOrPerson, Team.class);
                        if (!team.isProtectedNomenclaturalTitleCache()){
                            team.setNomenclaturalTitleCache(null, false);
                        }
                        if (!team.isProtectedCollectorTitleCache()){
                            team.setCollectorTitleCache(null, false);
                        }
                    }
                    teamOrPerson.getNomenclaturalTitleCache();
                    teamOrPerson.getCollectorTitleCache();
                    if (! teamOrPerson.isProtectedTitleCache()){
                        teamOrPerson.setTitleCache(teamOrPerson.generateTitle(), false);
                    }
                    //if the above is changed in future, change also in ImportDeduplicationHelper.initAuthorTitleCaches
                }else if(Reference.class.isAssignableFrom(entityClazz)){
                    //reference caches
                    Reference ref = (Reference)entity;
                    ref.getAbbrevTitleCache();
                    ref.getTitleCache();
                }else if (SpecimenOrObservationBase.class.isAssignableFrom(entityClazz)){
                    //specimen
                    SpecimenOrObservationBase<?> specimen = (SpecimenOrObservationBase<?>)entity;
                    if (!specimen.isProtectedTitleCache()){
                        specimen.setTitleCache(specimen.generateTitle(), false);
                    }
                //any other
                }   else{
                   // identifiableEntity.setTitleCache(identifiableEntity.generateTitle(), identifiableEntity.isProtectedTitleCache());
                    identifiableEntity.getTitleCache();
                }

                //titleCache should never be empty, even if protected #5763, #5849
                if (identifiableEntity.isProtectedTitleCache() && identifiableEntity.hasEmptyTitleCache()){
                    identifiableEntity.setTitleCache(null, false);
                    identifiableEntity.getTitleCache();
                }
                if (identifiableEntity.hasEmptyTitleCache()){
                    identifiableEntity.setTitleCache(identifiableEntity.toString(), false);
                }
            }else if(Amplification.class.isAssignableFrom(entityClazz)) {
                Amplification amplification = (Amplification)entity;
                amplification.updateCache();
            }
        }
    }
}