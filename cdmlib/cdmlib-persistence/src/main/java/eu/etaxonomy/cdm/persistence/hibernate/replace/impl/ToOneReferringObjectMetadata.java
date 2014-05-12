package eu.etaxonomy.cdm.persistence.hibernate.replace.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Criteria;

import eu.etaxonomy.cdm.model.common.CdmBase;

public class ToOneReferringObjectMetadata extends ReferringObjectMetadataImpl {

	
	public ToOneReferringObjectMetadata(Class fromClass, String propertyName,
			Class<? extends CdmBase> toClass) throws SecurityException,
			NoSuchFieldException {
		super(fromClass, propertyName, toClass);
	}

	public List<CdmBase> getReferringObjects(CdmBase x, Session session) {
		Criteria criteria = session.createCriteria(type);
        criteria.add(Restrictions.eq(fieldName,x));
        return (List<CdmBase>)criteria.list();
	}

	public void replace(CdmBase referringObject, CdmBase x, CdmBase y) throws IllegalArgumentException, IllegalAccessException {

	    field.set(referringObject,y);
	}

}
