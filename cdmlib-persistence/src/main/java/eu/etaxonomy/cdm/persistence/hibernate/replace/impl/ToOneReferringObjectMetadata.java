/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate.replace.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import eu.etaxonomy.cdm.model.common.CdmBase;

public class ToOneReferringObjectMetadata extends ReferringObjectMetadataImpl {

	public ToOneReferringObjectMetadata(Class fromClass, String propertyName,
			Class<? extends CdmBase> toClass) throws SecurityException,
			NoSuchFieldException {
		super(fromClass, propertyName, toClass);
	}

	@Override
    public List<CdmBase> getReferringObjects(CdmBase x, Session session) {
		Criteria criteria = session.createCriteria(type);
        criteria.add(Restrictions.eq(fieldName,x));
        @SuppressWarnings("unchecked")
        List<CdmBase> result = criteria.list();
        return result;
	}

	@Override
    public void replace(CdmBase referringObject, CdmBase x, CdmBase y) throws IllegalArgumentException, IllegalAccessException {
	    field.set(referringObject,y);
	}
}
