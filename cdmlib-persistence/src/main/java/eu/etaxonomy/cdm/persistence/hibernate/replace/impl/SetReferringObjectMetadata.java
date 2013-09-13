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

	public void replace(CdmBase referringObject, CdmBase x, CdmBase y)
			throws IllegalArgumentException, IllegalAccessException {
		Set<CdmBase> property = (Set<CdmBase>)field.get(referringObject);
        property.remove(x);
        if(y != null) {
            property.add(y);
        }
	}

}
