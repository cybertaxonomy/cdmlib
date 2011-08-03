package eu.etaxonomy.cdm.persistence.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.SaveOrUpdateEventListener;
import org.joda.time.DateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.etaxonomy.cdm.database.EvaluationFailedException;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.permission.CdmPermission;
import eu.etaxonomy.cdm.permission.CdmPermissionEvaluator;

public class SaveOrUpdateEntityListener implements SaveOrUpdateEventListener {

	public void onSaveOrUpdate(SaveOrUpdateEvent event)
			throws HibernateException {
		Object entity = event.getObject();
		if(entity != null && CdmBase.class.isAssignableFrom(entity.getClass())){
			
			CdmPermissionEvaluator permissionEvaluator = new CdmPermissionEvaluator();
			if (SecurityContextHolder.getContext().getAuthentication()!= null){
				if (!permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), entity, CdmPermission.CREATE)){
					throw new EvaluationFailedException("Permission evaluation failed for " + event.getEntity());
				}
			}
			if (VersionableEntity.class.isAssignableFrom(entity.getClass())) {
				VersionableEntity versionableEntity = (VersionableEntity)entity;
				if (versionableEntity.getId()== 0){
					
					versionableEntity.setUpdated(new DateTime());
					Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
					if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
						User user = (User)authentication.getPrincipal();
						versionableEntity.setUpdatedBy(user);
					} 
				}
			}
		}
	}

}
