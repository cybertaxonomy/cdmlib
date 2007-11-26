/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao;

import java.util.List;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 *
 */
public interface ITaxonDao extends IDao<TaxonBase> {
	
	public List<TaxonBase> getTaxaByName(String name, ReferenceBase sec);
	
	public List<Taxon> getRootTaxa(ReferenceBase sec);
}
