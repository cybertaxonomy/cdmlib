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
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.NonViralName;

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
            Class entityClazz = entity.getClass();
            
            if(ICdmBase.class.isAssignableFrom(entityClazz)) {
              ICdmBase cdmBase = (ICdmBase)entity;
  			  cdmBase.setCreated(new DateTime());
  			  Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
  			  if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
  			    User user = (User)authentication.getPrincipal();
  			    cdmBase.setCreatedBy(user);
  			  }
            }
        	//title cache
        	if(IdentifiableEntity.class.isAssignableFrom(entityClazz)) {
        		IdentifiableEntity identifiableEntity = (IdentifiableEntity)entity;
        		identifiableEntity.getTitleCache();
            }
        	
        	//non-viral-name caches
        	if(NonViralName.class.isAssignableFrom(entityClazz)) {
        		NonViralName nonViralName = (NonViralName)entity;
        		nonViralName.getFullTitleCache();
        		nonViralName.getAuthorshipCache();
        		nonViralName.getNameCache();
            }
        	
        	
        }
        

    }
}
