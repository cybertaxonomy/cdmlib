package eu.etaxonomy.cdm.persistence.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.SaveOrUpdateEventListener;
import org.joda.time.DateTime;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

public class UpdateEntityListener implements SaveOrUpdateEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4295612947856041686L;

	public void onSaveOrUpdate(SaveOrUpdateEvent event)
			throws HibernateException {
		Object entity = event.getObject();
		if(entity != null && VersionableEntity.class.isAssignableFrom(entity.getClass())) {
			VersionableEntity versionableEntity = (VersionableEntity)entity;
			versionableEntity.setUpdated(new DateTime());
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
			  User user = (User)authentication.getPrincipal();
			  versionableEntity.setUpdatedBy(user);
			} 
		}
	}
}
