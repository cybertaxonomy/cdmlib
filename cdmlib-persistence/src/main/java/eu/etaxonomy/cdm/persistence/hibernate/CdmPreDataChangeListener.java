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
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author cmathew
 * @date 7 Jul 2015
 *
 */
public class CdmPreDataChangeListener implements PreInsertEventListener, PreUpdateEventListener {
    private static final long serialVersionUID = -7581071903134036209L;

    static String sep = ITreeNode.separator;
    static String pref = ITreeNode.treePrefix;

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
        } finally {
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
        } finally {
            return false;
        }

    }

    //from former SaveOrUpdateOrMergeEntityListener
    public static void insertUpdateMerge(Object entity){
        if(entity != null && CdmBase.class.isAssignableFrom(entity.getClass())){
            cacheDeterminationNames(entity);
            generateCaches(entity);

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


    public static void generateCaches(Object entity){
        if (entity != null){
            Class<?> entityClazz = entity.getClass();

            if (IdentifiableEntity.class.isAssignableFrom(entityClazz)){
                IdentifiableEntity<?> identifiableEntity = (IdentifiableEntity)entity;
                //non-viral-name caches
                if(NonViralName.class.isAssignableFrom(entityClazz)) {
                    NonViralName<?> nonViralName = (NonViralName<?>)entity;
                    nonViralName.getAuthorshipCache();
                    nonViralName.getNameCache();
                    nonViralName.getTitleCache();
                    nonViralName.getFullTitleCache();
                    //team-or-person caches
                }else if(TeamOrPersonBase.class.isAssignableFrom(entityClazz)){
                    TeamOrPersonBase<?> teamOrPerson = (TeamOrPersonBase<?>)entity;
                    String nomTitle = teamOrPerson.getNomenclaturalTitle();
                    if (teamOrPerson instanceof Team){
                        Team team =CdmBase.deproxy(teamOrPerson, Team.class);
                        team.setNomenclaturalTitle(nomTitle, team.isProtectedNomenclaturalTitleCache()); //nomTitle is not necessarily cached when it is created
                    }else{
                        teamOrPerson.setNomenclaturalTitle(nomTitle);
                    }
                    String titleCache = teamOrPerson.getTitleCache();
                    if (! teamOrPerson.isProtectedTitleCache()){
                        teamOrPerson.setTitleCache(titleCache, false);
                    }

                    //reference caches
                }else if(Reference.class.isAssignableFrom(entityClazz)){
                    Reference ref = (Reference)entity;
                    ref.getAbbrevTitleCache();
                    ref.getTitleCache();
                }else { //any other
                    //title cache
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
