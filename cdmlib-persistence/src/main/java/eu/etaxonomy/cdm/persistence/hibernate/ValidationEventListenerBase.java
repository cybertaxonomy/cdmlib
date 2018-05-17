/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidation;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationCrud;
import eu.etaxonomy.cdm.persistence.validation.EntityValidationTaskBase;
import eu.etaxonomy.cdm.persistence.validation.ValidationExecutor;

/**
 * @author a.mueller
 * @since 13.02.2015
 *
 */
@SuppressWarnings("serial")
public abstract class ValidationEventListenerBase implements PostInsertEventListener, PostUpdateEventListener{
    private static final Logger logger = Logger.getLogger(ValidationEventListenerBase.class);

    private ValidationExecutor validationExecutor;

    private final IEntityValidationCrud dao;

    private boolean enabled = true;

    protected ValidationEventListenerBase(IEntityValidationCrud dao){
        this.dao = dao;
    }

    public ValidationExecutor getValidationExecutor(){
        return validationExecutor;
    }

    public void setValidationExecutor(ValidationExecutor validationExecutor){
        this.validationExecutor = validationExecutor;
    }

    @Override
    public void onPostInsert(PostInsertEvent event){
        validate(event.getEntity(), CRUDEventType.INSERT);
    }


    @Override
    public void onPostUpdate(PostUpdateEvent event){
        validate(event.getEntity(), CRUDEventType.UPDATE);
    }

    /**
     * @return the dao
     */
    protected IEntityValidationCrud getDao() {
        return dao;
    }


    protected void validate(Object object, CRUDEventType trigger){
        if (!enabled){
            if(logger.isDebugEnabled()){logger.debug("Pass " + levelString() + " validator as it is disabled");}
            return;
        }

        if (object == null) {
            if(logger.isDebugEnabled()){logger.debug("Nothing to validate (entity is null)");}
            return;
        }


        try {
           if (!(object instanceof CdmBase)) {
                if (object.getClass() != HashMap.class) {
                    if(logger.isDebugEnabled()){ logger.debug(levelString() + " validation bypassed for entities of type " + object.getClass().getName());}
                }
                return;
            }
            if (object instanceof EntityValidation || object instanceof EntityConstraintViolation) {
                if(logger.isDebugEnabled()){ logger.debug(levelString() + " validation bypassed for entities of type " + object.getClass().getName() + ". We do not validate validation result entities themselves");}
                return;
            }
            //validate
            ICdmBase entity = HibernateProxyHelper.deproxy(object, CdmBase.class) ;
            EntityValidationTaskBase task = createValidationTask(entity, trigger);
            getValidationExecutor().execute(task);
        }
        catch (Throwable t) {
            logger.error("Failed applying " + levelString() + " validation to " + object.toString(), t);
        }
    }

    protected abstract EntityValidationTaskBase createValidationTask(ICdmBase entity, CRUDEventType trigger);

    protected abstract String levelString();

    @Deprecated  //this is in test mode, may be removed in future
    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

}
