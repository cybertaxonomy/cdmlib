package eu.etaxonomy.cdm.persistence.hibernate.replace.impl;

import java.util.List;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.hibernate.replace.ReferringObjectMetadata;

public class ListReferringObjectMetadata extends ToManyReferringObjectMetadata
		implements ReferringObjectMetadata {

	public ListReferringObjectMetadata(Class fromClass, String propertyName,
			Class<? extends CdmBase> toClass) throws SecurityException,
			NoSuchFieldException {
		super(fromClass, propertyName, toClass);
	}

	public void replace(CdmBase referringObject, CdmBase x, CdmBase y)
			throws IllegalArgumentException, IllegalAccessException {
		List<CdmBase> property = (List<CdmBase>)field.get(referringObject);
		if(y != null) {
		    for(CdmBase c : property) {
			    if(x.equals(c)) {
				    int index = property.indexOf(c);
				    property.set(index, y);
			    }
		    }
		} else {
			while(property.contains(x)) {
				property.remove(x);
			}
		}
	}
}
