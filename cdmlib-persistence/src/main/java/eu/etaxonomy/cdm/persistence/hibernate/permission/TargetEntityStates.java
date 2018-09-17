/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission;

import java.util.Objects;

import org.hibernate.PropertyNotFoundException;
import org.hibernate.type.Type;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @since Jul 6, 2018
 *
 */
public class TargetEntityStates {

    CdmBase entity;
    Object[] currentState;
    Object[] previousState;
    String[] propertyNames;
    Type[] types;
    /**
     * @param entity
     * @param currentState
     * @param previousState
     * @param propertyNames
     * @param types
     */
    public TargetEntityStates(CdmBase entity, Object[] currentState, Object[] previousState, String[] propertyNames,
            Type[] types) {

        this.entity = entity;
        this.currentState = currentState;
        this.previousState = previousState;
        this.propertyNames = propertyNames;
        this.types = types;
    }


    public TargetEntityStates(CdmBase entity){
        this.entity = entity;
    }

    /**
     * @return the entity
     */
    public CdmBase getEntity() {
        return entity;
    }

    public boolean hasPreviousState() {
        return previousState != null;
    }

    /**
     * Compares the current state of the entity property (state being persisted) with the previous state
     * (state to be overwritten in the storage) and returns <code>true</code> in case there is a previous
     * state and the new state is different.
     *
     * @param propertyName
     * @return
     */
    public boolean propertyChanged(String propertyName){
        if(propertyNames == null){
            // usually during a save or delete operation
            return false;
        }
        if(!hasPreviousState()){
            // should be covered by propertyNames == null but this check seems to be nececary in rare situations
            // see the NPE stack strace in #7702 for an example
            return false;
        }
        int i = 0;
        for(String p : propertyNames){
            if(p.equals(propertyName)){
                return !Objects.equals(currentState[i], previousState[i]);
            }
            i++;
        }
        throw new PropertyNotFoundException("The property " + propertyName + " does not exist in " + entity.getClass());
    }

    public <T> T previousPropertyState(String propertyName, Class<T> propertyType){
        Object value = previousPropertyState(propertyName);
        if(value == null){
            return null;
        } else {
            return propertyType.cast(value);
        }
    }

    public Object previousPropertyState(String propertyName){
        int i = 0;
        for(String p : propertyNames){
            if(p.equals(propertyName)){
                return previousState[i];
            }
            i++;
        }
        throw new PropertyNotFoundException("The property " + propertyName + " does not exist in " + entity.getClass());
    }

    public Object currentPropertyState(String propertyName){
        int i = 0;
        for(String p : propertyNames){
            if(p.equals(propertyName)){
                return currentState[i];
            }
            i++;
        }
        throw new PropertyNotFoundException("The property " + propertyName + " does not exist in " + entity.getClass());
    }

}
