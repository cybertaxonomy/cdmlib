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

import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * @author cmathew
 * @date 7 Jul 2015
 *
 */
public class CdmPreDataChangeObservableListener implements PreInsertEventListener, PreUpdateEventListener {

    /* (non-Javadoc)
     * @see org.hibernate.event.spi.PreUpdateEventListener#onPreUpdate(org.hibernate.event.spi.PreUpdateEvent)
     */
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
        } finally {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.hibernate.event.spi.PreInsertEventListener#onPreInsert(org.hibernate.event.spi.PreInsertEvent)
     */
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
        } finally {
            return false;
        }

    }

}
