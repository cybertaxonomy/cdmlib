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

    public boolean propertyChanged(String propertyName){
        int i = 0;
        for(String p : propertyNames){
            if(p.equals(propertyName)){
                return !Objects.equals(currentState[i], previousState[i]);
            }
            i++;
        }
        throw new PropertyNotFoundException("The property " + propertyName + " does not exist in " + entity.getClass());
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
