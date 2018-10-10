/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;

import eu.etaxonomy.cdm.model.name.TaxonName;

/**
 * @author a.kohlbecker
 * @since Sep 27, 2018
 *
 */
public class TaxonGraphHibernateListener implements ITaxonGraphHibernateListener {

    private static final long serialVersionUID = 5062518307839173935L;

    private Set<Class<? extends BeforeTransactionCompletionProcess>> beforeTransactionCompletionProcessTypes = new HashSet<>();

    @Override
    public boolean registerProcessClass(Class<? extends BeforeTransactionCompletionProcess> processClass){
       return beforeTransactionCompletionProcessTypes.add(processClass);
    }

    @Override
    public boolean unRegisterProcessClass(Class<? extends BeforeTransactionCompletionProcess> processClass){
        return beforeTransactionCompletionProcessTypes.remove(processClass);
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {

        if(event.getEntity() instanceof TaxonName){
            for(Class<? extends BeforeTransactionCompletionProcess> type : beforeTransactionCompletionProcessTypes){
                try {
                    BeforeTransactionCompletionProcess processorInstance = type.getConstructor(PostUpdateEvent.class).newInstance(event);
                    event.getSession().getActionQueue().registerProcess(processorInstance);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    Logger.getLogger(TaxonGraphHibernateListener.class).error("Error creating new instance of " + type.toString(), e);
                }
            }
        }
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {

        if(event.getEntity() instanceof TaxonName){
            for(Class<? extends BeforeTransactionCompletionProcess> type : beforeTransactionCompletionProcessTypes){
                try {
                    BeforeTransactionCompletionProcess processorInstance = type.getConstructor(PostInsertEvent.class).newInstance(event);
                    event.getSession().getActionQueue().registerProcess(processorInstance);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    Logger.getLogger(TaxonGraphHibernateListener.class).error("Error creating new instance of " + type.toString(), e);
                }
            }
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return true;
    }

}
