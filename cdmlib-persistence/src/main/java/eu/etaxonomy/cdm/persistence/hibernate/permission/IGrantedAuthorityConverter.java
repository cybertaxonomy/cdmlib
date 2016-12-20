/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission;

import sun.security.provider.PolicyParser.ParsingException;
import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;

/**
 * @author a.kohlbecker
 * @date Oct 15, 2012
 *
 */
public interface IGrantedAuthorityConverter {

    /**
     * @return
     * @throws ParsingException
     */
    public abstract GrantedAuthorityImpl asNewGrantedAuthority() throws ParsingException;

}
