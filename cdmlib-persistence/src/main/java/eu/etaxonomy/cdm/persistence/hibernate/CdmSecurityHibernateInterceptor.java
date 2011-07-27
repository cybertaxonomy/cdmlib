
package eu.etaxonomy.cdm.persistence.hibernate;



import java.io.Serializable;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


import eu.etaxonomy.cdm.permission.CdmPermission;
import eu.etaxonomy.cdm.permission.CdmPermissionEvaluator;
@Component
public class CdmSecurityHibernateInterceptor extends EmptyInterceptor {
	private static final Logger logger = Logger
			.getLogger(CdmSecurityHibernateInterceptor.class);
	
	
	public boolean onSave(Object entity,
            Serializable id,
            Object[] state,
            String[] propertyNames,
            Type[] type) {
		
		CdmPermissionEvaluator permissionEvaluator = new CdmPermissionEvaluator();
		if (SecurityContextHolder.getContext().getAuthentication() != null){
			return permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), entity, CdmPermission.CREATE);}
		else return true;
		
	}
	public boolean onFlushDirty(Object entity,
            Serializable id,
            Object[] currentState,
            Object[] previousState,
            String[] propertyNames,
            Type[] types) {
		
		CdmPermissionEvaluator permissionEvaluator = new CdmPermissionEvaluator();
		if (SecurityContextHolder.getContext().getAuthentication() != null){
			return permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), entity, CdmPermission.UPDATE);}
		else return true;
		
	}
	
}
