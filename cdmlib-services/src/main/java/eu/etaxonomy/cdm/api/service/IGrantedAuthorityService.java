/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;

/**
 * @author andreas kohlbecker
 * @since Sep 13, 2012
 *
 */
public interface IGrantedAuthorityService extends IService<GrantedAuthorityImpl> {

    public GrantedAuthorityImpl findAuthorityString(String authorityString);

}
