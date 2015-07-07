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

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 04.03.2009
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

    /* (non-Javadoc)
     * @see org.hibernate.event.spi.MergeEventListener#onMerge(org.hibernate.event.spi.MergeEvent)
     */
    @Override
    public void onMerge(MergeEvent event) throws HibernateException {
        Object entity = event.getOriginal();
        saveOrUpdateOrMerge(entity);
    }

    /* (non-Javadoc)
     * @see org.hibernate.event.spi.MergeEventListener#onMerge(org.hibernate.event.spi.MergeEvent, java.util.Map)
     */
    @Override
    public void onMerge(MergeEvent event, Map copiedAlready) throws HibernateException {

    }

    private void saveOrUpdateOrMerge(Object entity) {
        if (entity != null){
            Class<?> entityClazz = entity.getClass();

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
                Reference<?> ref = (Reference<?>)entity;
                ref.getAbbrevTitleCache();
                ref.getTitleCache();
                //title cache
            }else if(IdentifiableEntity.class.isAssignableFrom(entityClazz)) {
                IdentifiableEntity<?> identifiableEntity = (IdentifiableEntity)entity;
                identifiableEntity.getTitleCache();
            }else if(Amplification.class.isAssignableFrom(entityClazz)) {
                Amplification amplification = (Amplification)entity;
                amplification.updateCache();
            }

        }
    }
}
