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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import eu.etaxonomy.cdm.model.common.CdmBase;

public abstract class ToManyReferringObjectMetadata
            extends ReferringObjectMetadataImpl {

	public ToManyReferringObjectMetadata(Class fromClass, String propertyName,
			Class<? extends CdmBase> toClass) throws SecurityException,
			NoSuchFieldException {
		super(fromClass, propertyName, toClass);
	}

	@Override
    public List<? extends CdmBase> getReferringObjects(CdmBase x, Session session) {

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<? extends CdmBase> cq = cb.createQuery(type);
        Root<? extends CdmBase> root = cq.from(type);

        Set<Integer> idValues = new HashSet<>();
        idValues.add(x.getId());

        cq.select((Root)root);
        Join<Object, Object> join = root.join(this.fieldName, JoinType.INNER);
        cq.where(join.get("id").in(idValues));
        List<? extends CdmBase> result = session.createQuery(cq).getResultList();

        return result;
    }
}
