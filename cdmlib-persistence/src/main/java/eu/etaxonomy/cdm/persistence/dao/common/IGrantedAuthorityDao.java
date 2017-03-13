/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.common;

import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;

public interface IGrantedAuthorityDao extends ICdmEntityDao<GrantedAuthorityImpl> {

    public GrantedAuthorityImpl findAuthorityString(String authorityString);

}
