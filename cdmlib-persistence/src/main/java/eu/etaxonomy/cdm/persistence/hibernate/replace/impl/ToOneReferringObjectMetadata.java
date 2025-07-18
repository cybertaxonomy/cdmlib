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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import eu.etaxonomy.cdm.model.common.CdmBase;

public class ToOneReferringObjectMetadata extends ReferringObjectMetadataImpl {

	public ToOneReferringObjectMetadata(Class fromClass, String propertyName,
			Class<? extends CdmBase> toClass) throws SecurityException,
			NoSuchFieldException {
		super(fromClass, propertyName, toClass);
	}

	@Override
    public List<? extends CdmBase> getReferringObjects(CdmBase x, Session session) {

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<? extends CdmBase> cq = cb.createQuery(type);
        Root<? extends CdmBase> root = cq.from(type);

        cq.select((Root)root);
        cq.where(cb.equal(root.get("fieldName"), x));
        List<? extends CdmBase> result = session.createQuery(cq).getResultList();
        return result;
	}

	@Override
    public void replace(CdmBase referringObject, CdmBase x, CdmBase y) throws IllegalArgumentException, IllegalAccessException {
	    field.set(referringObject,y);
	}
}
