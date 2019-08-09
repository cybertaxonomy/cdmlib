/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.permission;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.permission.ICdmAuthorityDao;

@Repository
public class CdmAuthorityDaoImpl
        extends CdmEntityDaoBase<CdmAuthority>
        implements ICdmAuthorityDao {

	public CdmAuthorityDaoImpl() {
		super(CdmAuthority.class);
	}

//	@Override
//    public GrantedAuthorityImpl findAuthorityString(String authorityString){
//	    GrantedAuthorityImpl result = null;
//	    Query query = getSession().createQuery("select ga from GrantedAuthorityImpl ga where ga.authority = :authority");
//        query.setParameter("authority",authorityString);
//
//        result = (GrantedAuthorityImpl)query.uniqueResult();
//
//
//        return result;
//
//	}
}
