/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate.replace.impl;

import java.util.Set;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.hibernate.replace.ReferringObjectMetadata;

public class SetReferringObjectMetadata extends ToManyReferringObjectMetadata
		implements ReferringObjectMetadata {

	public SetReferringObjectMetadata(Class fromClass, String propertyName,
			Class<? extends CdmBase> toClass) throws SecurityException,
			NoSuchFieldException {
		super(fromClass, propertyName, toClass);
	}

	@Override
    public void replace(CdmBase referringObject, CdmBase x, CdmBase y)
			throws IllegalArgumentException, IllegalAccessException {
		Set<CdmBase> property = (Set<CdmBase>)field.get(referringObject);
        property.remove(x);
        if(y != null) {
            property.add(y);
        }
	}
}
