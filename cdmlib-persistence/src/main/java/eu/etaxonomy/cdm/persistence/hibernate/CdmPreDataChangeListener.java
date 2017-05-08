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
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author cmathew
 * @date 7 Jul 2015
 *
 */
public class CdmPreDataChangeListener
        implements PreInsertEventListener, PreUpdateEventListener {

    private static final long serialVersionUID = -7581071903134036209L;

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
                    String nomTitle = teamOrPerson.getNomenclaturalTitle();
                    if (teamOrPerson instanceof Team){
                        Team team = (Team)teamOrPerson;
                        //nomTitle is not necessarily cached when it is created
                        team.setNomenclaturalTitle(nomTitle, team.isProtectedNomenclaturalTitleCache());
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
                //specimen
                }else if (SpecimenOrObservationBase.class.isAssignableFrom(entityClazz)){
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
