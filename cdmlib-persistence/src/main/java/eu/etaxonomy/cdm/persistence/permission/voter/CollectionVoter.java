package eu.etaxonomy.cdm.persistence.permission.voter;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;

public class CollectionVoter extends CdmPermissionVoter {

    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return Collection.class;
    }

    @Override
    public boolean isOrpahn(CdmBase object) {
        return false;
    }
}
