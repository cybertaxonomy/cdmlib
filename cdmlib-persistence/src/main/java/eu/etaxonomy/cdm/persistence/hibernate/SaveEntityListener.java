/**
* Copyright (C) 2009 EDIT
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.etaxonomy.cdm.database.EvaluationFailedException;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.permission.AuthorityPermission;
import eu.etaxonomy.cdm.permission.CdmPermission;
import eu.etaxonomy.cdm.permission.CdmPermissionEvaluator;

public class SaveEntityListener implements SaveOrUpdateEventListener {
	private static final long serialVersionUID = -4295612947856041686L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SaveEntityListener.class);

	public void onSaveOrUpdate(SaveOrUpdateEvent event)	throws HibernateException {
		Object entity = event.getObject();
		
        if (entity != null){
        	
            Class<?> entityClazz = entity.getClass();
			if(ICdmBase.class.isAssignableFrom(entityClazz)) {
				
				ICdmBase cdmBase = (ICdmBase)entity;
				cdmBase.setCreated(new DateTime());
			
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
					User user = (User)authentication.getPrincipal();
					cdmBase.setCreatedBy(user);
					CdmPermissionEvaluator permissionEvaluator = new CdmPermissionEvaluator();
					
					
					if (!permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), entity, CdmPermission.CREATE)){
						
						throw new EvaluationFailedException("Permission evaluation failed for creating " + event.getEntity());
					 }
				
				}
			}
        }		
	}
}
