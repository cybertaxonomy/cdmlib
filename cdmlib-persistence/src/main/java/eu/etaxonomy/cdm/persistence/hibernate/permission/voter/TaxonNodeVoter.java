// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission.voter;

import java.util.Collection;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author andreas
 * @date Sep 4, 2012
 *
 */
public class TaxonNodeVoter extends CdmPermissionVoter {

    /* (non-Javadoc)
     * @see org.springframework.security.access.AccessDecisionVoter#vote(org.springframework.security.core.Authentication, java.lang.Object, java.util.Collection)
     */
    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.access.AccessDecisionVoter#supports(java.lang.Class)
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isInstance(TaxonNode.class);
    }

}
