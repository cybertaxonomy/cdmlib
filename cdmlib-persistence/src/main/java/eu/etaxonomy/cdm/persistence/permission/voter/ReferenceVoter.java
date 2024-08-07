/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.permission.voter;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.kohlbecker
 * @since Feb 24, 2014
 */
public class ReferenceVoter extends CdmPermissionVoter {

    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return Reference.class;
    }

    @Override
    public boolean isOrpahn(CdmBase object) {
        // References must never be considered orphan. So they must not be deleted without explicit DELETE permission
        return false;
    }
}