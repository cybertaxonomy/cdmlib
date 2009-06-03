package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.event.MergeEvent;
import org.joda.time.DateTime;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.NonViralName;

public class MergeEntityListener implements
		org.hibernate.event.MergeEventListener {

	public void onMerge(MergeEvent event) throws HibernateException {
		Object entity = event.getOriginal();
        if (entity != null){
            Class<?> entityClazz = entity.getClass();
            if(ICdmBase.class.isAssignableFrom(entityClazz)) {
	            ICdmBase cdmBase = (ICdmBase)entity;
                if(cdmBase.getId() == 0) {
                	cdmBase.setCreated(new DateTime());
  				    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
  				    if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
  				      User user = (User)authentication.getPrincipal();
  				      cdmBase.setCreatedBy(user);
  				    }
                  } else if(VersionableEntity.class.isAssignableFrom(entityClazz)) {
    			    VersionableEntity versionableEntity = (VersionableEntity)cdmBase;
    			    versionableEntity.setUpdated(new DateTime());
    			    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    			    if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
    			      User user = (User)authentication.getPrincipal();
    			      versionableEntity.setUpdatedBy(user);
    			    } 
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

	public void onMerge(MergeEvent arg0, Map arg1) throws HibernateException {
		// TODO Auto-generated method stub

	}

}
