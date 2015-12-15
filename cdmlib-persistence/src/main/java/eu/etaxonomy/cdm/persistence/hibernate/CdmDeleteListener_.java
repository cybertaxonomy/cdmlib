/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.event.spi.DeleteEvent;
import org.hibernate.event.spi.DeleteEventListener;

import eu.etaxonomy.cdm.model.common.RelationshipBase;

/**
 * @author a.mueller
 * @created 04.03.2009
 * @version 1.0
 */
public class CdmDeleteListener implements DeleteEventListener {
    private static final long serialVersionUID = -5511287200489449838L;
    protected static final Logger logger = Logger.getLogger(CdmDeleteListener.class);

    @Override
    public void onDelete(DeleteEvent event) throws HibernateException {
        Object entity = event.getObject();
        if(entity != null && RelationshipBase.class.isAssignableFrom(entity.getClass())) {
            logger.info("Deleting " + entity);
            deleteRelationship(event, entity);
        }
    }

    @Override
    public void onDelete(DeleteEvent event, Set transientEntities)throws HibernateException {
        Object entity = event.getObject();
        if(entity != null && RelationshipBase.class.isAssignableFrom(entity.getClass())) {
            logger.info("Deleting " + entity);
            deleteRelationship(event, entity);
        }
    }

    /**
     * @param event
     * @param entity
     */
    private void deleteRelationship(DeleteEvent event, Object entity) {
        RelationshipBase relationshipEntity = (RelationshipBase)entity;
      /*  if (relationshipEntity.isRemoved()){
            Set<IRelated> deletedObjects = relationshipEntity.getDeletedObjects();
            for (IRelated rel : deletedObjects){
                if (rel != null){
                    logger.info("Updating related entity " + rel);
                    Object o = CdmBase.deproxy(rel, CdmBase.class);
                    EntityEntry entry = event.getSession().getPersistenceContext().getEntry(o);
                    if (entry == null){
//						System.out.println();
                    }
                    if (!entry.getStatus().equals(Status.DELETED)){
                        event.getSession().update(rel);
                    }
                }
            }
        }*/
    }
}
