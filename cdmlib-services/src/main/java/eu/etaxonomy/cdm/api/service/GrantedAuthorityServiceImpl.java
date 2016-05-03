// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.persistence.dao.common.IGrantedAuthorityDao;

/**
 * @author andreas kohlbecker
 * @date Sep 13, 2012
 *
 */
@Service
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
public class GrantedAuthorityServiceImpl extends ServiceBase<GrantedAuthorityImpl, IGrantedAuthorityDao> implements IGrantedAuthorityService {

    @Override
    @Autowired
    protected void setDao(IGrantedAuthorityDao dao) {
        this.dao = dao;
    }


}
