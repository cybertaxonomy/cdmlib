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
import eu.etaxonomy.cdm.model.name.Registration;

/**
 * @author a.kohlbecker
 * @date Feb 24, 2014
 *
 */
public class RegistrationVoter extends CdmPermissionVoter {

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.hibernate.permission.voter.CdmPermissionVoter#getResponsibilityClass()
     */
    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return Registration.class;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.hibernate.permission.voter.CdmPermissionVoter#isOrpahn(eu.etaxonomy.cdm.model.common.CdmBase)
     */
    @Override
    public boolean isOrpahn(CdmBase object) {
        return ((Registration)object).getTypeDesignations().size() > 0 && ((Registration)object).getName() == null;
    }

}
