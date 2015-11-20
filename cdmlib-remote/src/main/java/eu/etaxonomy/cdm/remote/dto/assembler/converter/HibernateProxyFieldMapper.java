// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.assembler.converter;

import org.dozer.CustomFieldMapper;
import org.dozer.classmap.ClassMap;
import org.dozer.fieldmap.FieldMap;
import org.hibernate.Hibernate;
import org.hibernate.SessionException;
import org.hibernate.envers.internal.entities.mapper.relation.lazy.proxy.CollectionProxy;



public class HibernateProxyFieldMapper implements CustomFieldMapper {


	@Override
    public boolean mapField(Object source, Object destination, Object sourceFieldValue, ClassMap classMap, FieldMap fieldMapping) {

		if(sourceFieldValue instanceof CollectionProxy) {
			try {
				((CollectionProxy)sourceFieldValue).hashCode();
			} catch(SessionException se) { // currently no way to tell if is initialized
				return true;
			}
		    return false;
		} else if(Hibernate.isInitialized(sourceFieldValue)) {
		    return false;
		}else {
			return true;
		}

	}
}
