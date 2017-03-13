/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.persistence.dao.common.IGrantedAuthorityDao;

@Repository
public class GrantedAuthorityDaoImpl extends CdmEntityDaoBase<GrantedAuthorityImpl> implements
		IGrantedAuthorityDao {

	public GrantedAuthorityDaoImpl() {
		super(GrantedAuthorityImpl.class);
	}

	@Override
    public GrantedAuthorityImpl findAuthorityString(String authorityString){
	    GrantedAuthorityImpl result = null;
	    Query query = getSession().createQuery("select ga from GrantedAuthorityImpl ga where ga.authority = :authority");
        query.setParameter("authority",authorityString);

        result = (GrantedAuthorityImpl)query.uniqueResult();


        return result;

	}
}
