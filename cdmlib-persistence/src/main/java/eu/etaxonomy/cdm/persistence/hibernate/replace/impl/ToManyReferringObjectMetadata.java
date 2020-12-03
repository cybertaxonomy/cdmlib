/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate.replace.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.hibernate.replace.ReferringObjectMetadata;

public abstract class ToManyReferringObjectMetadata extends ReferringObjectMetadataImpl
		implements ReferringObjectMetadata {

	public ToManyReferringObjectMetadata(Class fromClass, String propertyName,
			Class<? extends CdmBase> toClass) throws SecurityException,
			NoSuchFieldException {
		super(fromClass, propertyName, toClass);
	}

	@Override
    public List<CdmBase> getReferringObjects(CdmBase x, Session session) {
		Criteria criteria = session.createCriteria(type);
		Set<Integer> values = new HashSet<Integer>();
		values.add(x.getId());
        criteria.createCriteria(this.fieldName).add(Restrictions.in("id", values));

        @SuppressWarnings("unchecked")
        List<CdmBase> result = criteria.list();
        return result;
    }
}
