/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.application;

import org.springframework.security.core.GrantedAuthority;

/**
 * @author a.kohlbecker
 * @since Oct 10, 2018
 *
 */
public abstract class AbstractRunAs extends RunAsAuthenticator implements IRunAs {

    public abstract GrantedAuthority runAsGrantedAuthority();

    @Override
    public void apply() {
        super.runAsAuthentication(runAsGrantedAuthority());

    }

    @Override
    public void restore() {
       super.restoreAuthentication();
    }

}
