/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.editor.term;

import eu.etaxonomy.cdm.model.name.RegistrationStatus;

/**
 * @author a.kohlbecker
 * @since Jul 13, 2018
 *
 */
public class RegistrationStatusList extends EnumTermList<RegistrationStatus> {

    /**
     *
     */
    public RegistrationStatusList() {
        super(RegistrationStatus.class);
    }

    private static final long serialVersionUID = 8936508095351613591L;

}
