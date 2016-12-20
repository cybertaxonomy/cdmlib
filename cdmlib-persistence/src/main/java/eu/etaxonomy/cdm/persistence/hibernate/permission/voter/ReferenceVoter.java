/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission.voter;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.kohlbecker
 * @date Feb 24, 2014
 *
 */
public class ReferenceVoter extends CdmPermissionVoter {

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.hibernate.permission.voter.CdmPermissionVoter#getResponsibilityClass()
     */
    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return Reference.class;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.hibernate.permission.voter.CdmPermissionVoter#isOrpahn(eu.etaxonomy.cdm.model.common.CdmBase)
     */
    @Override
    public boolean isOrpahn(CdmBase object) {
        // we always return true here to allow deleting the reference
        return true;
    }

}
