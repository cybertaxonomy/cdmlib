/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.persister.entity.EntityPersister;

import eu.etaxonomy.cdm.config.CdmHibernateListenerConfiguration;
import eu.etaxonomy.cdm.model.name.NomenclaturalSource;
import eu.etaxonomy.cdm.model.name.TaxonName;

/**
 * The {@code TaxonGraphHibernateListener} it the implementation of the
 * according interface. The listener in initially empty and thus will do nothing
 * unless configured with the {@link TaxonGraphBeforeTransactionCompleteProces}
 *
 * @see <a href=
 *      "https://dev.e-taxonomy.eu/redmine/issues/7648">https://dev.e-taxonomy.eu/redmine/issues/7648</a>
 * @see {@link TaxonGraphBeforeTransactionCompleteProcess}
 * @see {@link CdmHibernateListenerConfiguration}
 *
 * @author a.kohlbecker
 * @since Sep 27, 2018
 *
 */
public class TaxonGraphHibernateListener implements ITaxonGraphHibernateListener {

    private static final long serialVersionUID = 5062518307839173935L;

    private Map<Class<? extends BeforeTransactionCompletionProcess>, ProcessConstructorData<? extends BeforeTransactionCompletionProcess>> beforeTransactionCompletionProcessTypes = new HashMap<>();

    @Override
    public void registerProcessClass(Class<? extends BeforeTransactionCompletionProcess> processClass, Object[] constructorArgs, Class<?>[] paramterTypes) throws NoSuchMethodException, SecurityException{
        if(constructorArgs == null){
            constructorArgs = new Object[]{};
        }
       beforeTransactionCompletionProcessTypes.put(processClass, new ProcessConstructorData(processClass, constructorArgs, paramterTypes));
    }

    @Override
    public void unRegisterProcessClass(Class<? extends BeforeTransactionCompletionProcess> processClass){
        beforeTransactionCompletionProcessTypes.remove(processClass);
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {

        if(event.getEntity() instanceof TaxonName || event.getEntity() instanceof NomenclaturalSource){
            for(Class<? extends BeforeTransactionCompletionProcess> type : beforeTransactionCompletionProcessTypes.keySet()){
                try {
                    ProcessConstructorData<? extends BeforeTransactionCompletionProcess> pcd = beforeTransactionCompletionProcessTypes.get(type);
                    BeforeTransactionCompletionProcess processorInstance = pcd.postUpdateEventConstructor.newInstance(pcd.buildConstructorArgs(event));
                    event.getSession().getActionQueue().registerProcess(processorInstance);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | SecurityException e) {
                    Logger.getLogger(TaxonGraphHibernateListener.class).error("Error creating new instance of " + type.toString(), e);
                }
            }
        }
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {

        if(event.getEntity() instanceof TaxonName || event.getEntity() instanceof NomenclaturalSource){
            for(Class<? extends BeforeTransactionCompletionProcess> type : beforeTransactionCompletionProcessTypes.keySet()){
                try {
                    ProcessConstructorData<? extends BeforeTransactionCompletionProcess> pcd = beforeTransactionCompletionProcessTypes.get(type);
                    BeforeTransactionCompletionProcess processorInstance = pcd.postInsertEventConstructor.newInstance(pcd.buildConstructorArgs(event));
                    event.getSession().getActionQueue().registerProcess(processorInstance);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | SecurityException e) {
                    Logger.getLogger(TaxonGraphHibernateListener.class).error("Error creating new instance of " + type.toString(), e);
                }
            }
        }
    }

    @Override
    public boolean onPreDelete(PreDeleteEvent event) {

        if(event.getEntity() instanceof TaxonName || event.getEntity() instanceof NomenclaturalSource){
            for(Class<? extends BeforeTransactionCompletionProcess> type : beforeTransactionCompletionProcessTypes.keySet()){
                try {
                    ProcessConstructorData<? extends BeforeTransactionCompletionProcess> pcd = beforeTransactionCompletionProcessTypes.get(type);
                    BeforeTransactionCompletionProcess processorInstance = pcd.preDeleteEventConstructor.newInstance(pcd.buildConstructorArgs(event));
                    event.getSession().getActionQueue().registerProcess(processorInstance);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | SecurityException e) {
                    Logger.getLogger(TaxonGraphHibernateListener.class).error("Error creating new instance of " + type.toString(), e);
                }
            }
        }
        return false; // no veto
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return true;
    }

    class ProcessConstructorData<T extends BeforeTransactionCompletionProcess> {

        Constructor<T> postInsertEventConstructor;
        Constructor<T> postUpdateEventConstructor;
        Constructor<T> preDeleteEventConstructor;
        Object[] constructorArgs;

        public ProcessConstructorData(Class<T> type, Object[] constructorArgs, Class<?>[] paramterTypes) throws NoSuchMethodException, SecurityException {

            this.constructorArgs = constructorArgs;

            Class<?>[] postInsertEventConstructorArgTypes = new Class<?>[constructorArgs.length + 1];
            Class<?>[] postUpdateEventConstructorArgTypes = new Class<?>[constructorArgs.length + 1];
            Class<?>[] preDeleteEventConstructorArgTypes = new Class<?>[constructorArgs.length + 1];
            postInsertEventConstructorArgTypes[0] = PostInsertEvent.class;
            postUpdateEventConstructorArgTypes[0] = PostUpdateEvent.class;
            preDeleteEventConstructorArgTypes[0] = PreDeleteEvent.class;
            int i = 1;
            for(Class<?> ptype : paramterTypes){
                postInsertEventConstructorArgTypes[i] = ptype;
                postUpdateEventConstructorArgTypes[i] = ptype;
                preDeleteEventConstructorArgTypes[i] = ptype;
            }
            postInsertEventConstructor = type.getConstructor(postInsertEventConstructorArgTypes);
            postUpdateEventConstructor = type.getConstructor(postUpdateEventConstructorArgTypes);
            preDeleteEventConstructor = type.getConstructor(preDeleteEventConstructorArgTypes);
        }

        public Object[] buildConstructorArgs(Object firstArg){
            Object[] cargs = new Object[constructorArgs.length + 1];
            cargs[0] = firstArg;
            int i = 1;
            for(Object arg : constructorArgs){
                cargs[i++] = arg;
            }
            return cargs;
        }
    }
}
