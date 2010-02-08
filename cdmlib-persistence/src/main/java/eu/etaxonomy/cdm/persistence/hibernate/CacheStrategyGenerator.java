/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.SaveOrUpdateEventListener;
import org.joda.time.DateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.persistence.dao.hibernate.HibernateProxyHelperExtended;

/**
 * @author a.mueller
 * @created 04.03.2009
 * @version 1.0
 */
public class CacheStrategyGenerator implements SaveOrUpdateEventListener {
	private static final long serialVersionUID = -5511287200489449838L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CacheStrategyGenerator.class);

    public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException {
        Object entity = event.getObject();
        if (entity != null){
            Class<?> entityClazz = entity.getClass();
            if(ICdmBase.class.isAssignableFrom(entityClazz)) {
	            ICdmBase cdmBase = (ICdmBase)entity;
            	cdmBase = (ICdmBase)HibernateProxyHelperExtended.getProxyTarget(cdmBase);  //needed for debugging of integration tests that are in error by mistake 
            	if(cdmBase.getId() == 0) {
				    if (cdmBase.getCreated() == null){
				    	cdmBase.setCreated(new DateTime());
					}
					Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				    if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
				      User user = (User)authentication.getPrincipal();
				      cdmBase.setCreatedBy(user);
				    }
				}
	          }

        	//non-viral-name caches
        	if(NonViralName.class.isAssignableFrom(entityClazz)) {
        		NonViralName<?> nonViralName = (NonViralName<?>)entity;
        		nonViralName.getAuthorshipCache();
        		nonViralName.getNameCache();
        		nonViralName.getTitleCache();
        		nonViralName.getFullTitleCache();
        	//team-or-person caches
            }else if(TeamOrPersonBase.class.isAssignableFrom(entityClazz)){
            	TeamOrPersonBase teamOrPerson = (TeamOrPersonBase)entity;
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
 
            //title cache
            }else if(IdentifiableEntity.class.isAssignableFrom(entityClazz)) {
        		IdentifiableEntity identifiableEntity = (IdentifiableEntity)entity;
        		identifiableEntity.getTitleCache();
            }
        	
        }
    }
}
