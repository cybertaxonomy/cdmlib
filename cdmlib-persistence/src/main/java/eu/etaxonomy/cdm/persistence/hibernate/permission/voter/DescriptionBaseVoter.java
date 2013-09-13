package eu.etaxonomy.cdm.persistence.hibernate.permission.voter;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;

public class DescriptionBaseVoter extends CdmPermissionVoter {

    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return DescriptionBase.class;
    }

}
