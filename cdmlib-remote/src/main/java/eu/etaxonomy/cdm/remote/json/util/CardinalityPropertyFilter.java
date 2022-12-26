/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.json.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import net.sf.json.util.PropertyFilter;

public class CardinalityPropertyFilter implements PropertyFilter {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    boolean includeToOneRelations = true;

    boolean includeToManyRelations = true;

    Set<String> exceptions = new HashSet<>();

    public void setExceptions(Set<String> exceptions) {
        this.exceptions = exceptions;
    }

    public void setIncludeToOneRelations(boolean includeToOneRelations) {
        this.includeToOneRelations = includeToOneRelations;
    }

    public void setIncludeToManyRelations(boolean includeToManyRelations) {
        this.includeToManyRelations = includeToManyRelations;
    }

    @Override
    public boolean apply(Object source, String name, Object value) {
        Class<?> valueType;

        if(value == null){
            valueType = org.springframework.beans.BeanUtils.findPropertyType(name, new Class[] {source.getClass()});
            if(valueType == null){
                // something went wrong, so better include the property
                return false;
            }
        } else {
            valueType = value.getClass();
        }

        if(CdmBase.class.isAssignableFrom(valueType)){
            if(!includeToOneRelations
                    && !exceptions.contains(source.getClass().getSimpleName() + "." + name)){
                return true;
            }
        } else if(Collection.class.isAssignableFrom(valueType) || Map.class.isAssignableFrom(valueType)){
            if(!includeToManyRelations
                    && !exceptions.contains(source.getClass().getSimpleName() + "." + name)
                    && CdmBase.class.isAssignableFrom(source.getClass())){
                return true;
            }
        }
        return false;
    }
}