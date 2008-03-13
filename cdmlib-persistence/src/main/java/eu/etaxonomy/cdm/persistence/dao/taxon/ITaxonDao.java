/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.taxon;

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
public interface ITaxonDao extends IIdentifiableDao<TaxonBase>, ITitledDao<TaxonBase> {
	
	public List<TaxonBase> getTaxaByName(String name, ReferenceBase sec);
	
	public List<Taxon> getRootTaxa(ReferenceBase sec);

	/**
	 * @param pagesize max maximum number of returned taxa
	 * @param page page to start, with 0 being first page 
	 * @return
	 */
	public List<TaxonBase> getAllTaxa(Integer pagesize, Integer page);
}
