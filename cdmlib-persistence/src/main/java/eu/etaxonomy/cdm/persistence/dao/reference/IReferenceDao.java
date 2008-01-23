/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.reference;

import java.util.List;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;

/**
 * @author a.mueller
 *
 */
public interface IReferenceDao extends IIdentifiableDao<ReferenceBase>, ITitledDao<ReferenceBase> {
	
}
