// $Id: InitializedHibernatePropertyFilter.java 5473 2009-03-25 13:42:07Z a.kohlbecker $
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

import net.sf.json.util.PropertyFilter;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;

public class CardinalityPropertyFilter implements PropertyFilter {

	private static final Logger logger = Logger.getLogger(CardinalityPropertyFilter.class);
	
	boolean includeToOneRelations = true;
	
	boolean includeToManyRelations = true;
	
	Set<String> exceptions = new HashSet<String>();
	
	public void setExceptions(Set<String> exceptions) {
		this.exceptions = exceptions;
	}

	public void setIncludeToOneRelations(boolean includeToOneRelations) {
		this.includeToOneRelations = includeToOneRelations;
	}

	public void setIncludeToManyRelations(boolean includeToManyRelations) {
		this.includeToManyRelations = includeToManyRelations;
	}

	public boolean apply(Object source, String name, Object value) {
		if(value == null){
			return false;
		}
		if(CdmBase.class.isAssignableFrom(value.getClass())){
        	if(!includeToOneRelations 
        			&& !exceptions.contains(source.getClass().getSimpleName() + "." + name)){
        		return true;
        	}
        } else if(Collection.class.isAssignableFrom(value.getClass()) || Map.class.isAssignableFrom(value.getClass())){
        	if(!includeToManyRelations 
        			&& !exceptions.contains(source.getClass().getSimpleName() + "." + name) 
        			&& CdmBase.class.isAssignableFrom(source.getClass())){
        		return true;
        	}
        }
		return false;
	}
}
