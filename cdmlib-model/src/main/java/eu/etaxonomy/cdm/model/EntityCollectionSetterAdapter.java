/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * In many cases the add*() and remove*() methods of entity classes contain important business
 * logic which would be short-circuited if the set*() method would be public. This adapter allows
 * providing setter methods to make the bean property writable.
 * <p>
 * The {{@link #setCollection(CdmBase, Collection)} method uses the add*() and remove*() methods
 * in order to update the collection field of the bean.
 * <p>
 * Usage example:
 * <pre>
 *   &#64;Transient
 *   &#64;Transient
 *   private EntityCollectionSetterAdapter<Team, Person> teamMembersSetterAdapter = new EntityCollectionSetterAdapter<Team, Person>(Team.class, Person.class, "teamMembers");
 *
 *   public void setTeamMembers(List<Person> teamMembers) throws SetterAdapterException {
 *       teamMembersSetterAdapter.setCollection(this, teamMembers);
 *   }
 </pre>
 *
 * see https://dev.e-taxonomy.eu/redmine/issues/7600
 *
 * @author a.kohlbecker
 * @since Nov 15, 2018
 *
 */
public class EntityCollectionSetterAdapter<CDM extends CdmBase, T extends CdmBase> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Class<T> propertyItemType;
    private Method getMethod;
    private Method addMethod;
    private Method removeMethod;
    private Exception getMethodException;
    private Exception addMethodException;
    private Exception removeMethodException;

    public EntityCollectionSetterAdapter(Class<CDM> beanClass, Class<T> propertyItemType, String propertyName){
        this(beanClass,
             propertyItemType,
             propertyName,
             "add" + StringUtils.capitalize(propertyName.substring(0, propertyName.length() - 1)),
             "remove" + StringUtils.capitalize(propertyName.substring(0, propertyName.length() - 1))
             );
    }

    public EntityCollectionSetterAdapter(Class<CDM> beanClass, Class<T> propertyItemType, String propertyName, String addMethodName, String removMethodName){

        this.propertyItemType = propertyItemType;
        try {
            getMethod = beanClass.getDeclaredMethod("get" + StringUtils.capitalize(propertyName));
        } catch (NoSuchMethodException | SecurityException e) {
            getMethodException = e;
        }
        try {
            addMethod = beanClass.getDeclaredMethod(addMethodName, propertyItemType);
        } catch (NoSuchMethodException | SecurityException e) {
            addMethodException = e;
        }
        try {
            removeMethod = beanClass.getDeclaredMethod(removMethodName, propertyItemType);
        } catch (NoSuchMethodException | SecurityException e) {
            removeMethodException = e;
        }
    }

    public void setCollection(CDM bean, Collection<T> items) throws SetterAdapterException {

        if(getMethodException != null){
            throw new SetterAdapterException("No getter method due to previous exception.", getMethodException);
        }
        if(addMethodException != null){
            throw new SetterAdapterException("No add method due to previous exception.", addMethodException);
        }
        if(removeMethodException != null){
            throw new SetterAdapterException("No remove method due to previous exception.", removeMethodException);
        }

        try{
            Collection<T> getterCollection = (Collection<T>) getMethod.invoke(bean);
            List<T> currentItems = new ArrayList<>(getterCollection);
            List<T> itemsSeen = new ArrayList<>();
            for(T a : items){
                if(a == null){
                    continue;
                }
                if(!currentItems.contains(a)){
                    addMethod.invoke(bean, a);
                }
                itemsSeen.add(a);
            }
            for(T a : currentItems){
                if(!itemsSeen.contains(a)){
                    removeMethod.invoke(bean, a);
                }
        }
        } catch(ClassCastException e){
            throw new SetterAdapterException("getter return type (" + getMethod.getReturnType() + ") incompatible with expected  property type " + propertyItemType, e);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new SetterAdapterException("error invoking method", e);
        }
    }

    public static class SetterAdapterException extends Exception {

        private static final long serialVersionUID = 1L;

        /**
         * @param message
         * @param cause
         */
        public SetterAdapterException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
