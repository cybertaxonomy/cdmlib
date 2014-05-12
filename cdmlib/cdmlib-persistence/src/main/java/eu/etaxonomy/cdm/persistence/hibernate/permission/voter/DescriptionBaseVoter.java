package eu.etaxonomy.cdm.persistence.hibernate.permission.voter;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;

public class DescriptionBaseVoter extends CdmPermissionVoter {

    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return DescriptionBase.class;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.hibernate.permission.voter.CdmPermissionVoter#isOrpahn(eu.etaxonomy.cdm.model.common.CdmBase)
     */
    @Override
    public boolean isOrpahn(CdmBase object) {
        if(object instanceof TaxonDescription){
            return ((TaxonDescription)object).getTaxon() == null;
        } else if (object instanceof TaxonNameDescription){
            return ((TaxonNameDescription)object).getTaxonName() == null;
        } else if (object instanceof SpecimenDescription){
            return ((SpecimenDescription)object).getDescribedSpecimenOrObservation() == null;
        }
        return false;
    }

}
