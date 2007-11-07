/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao;

import java.util.List;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * @author a.mueller
 *
 */
public interface ITaxonNameDao extends IDao<TaxonNameBase,Integer> {
	
	/* any nice thing to find*/
	public List<TaxonNameBase> getRelatedNames(Integer id);
	
	/* all names*/
	public List<TaxonNameBase> getAllNames();
	
	/* some names*/
	public List<TaxonNameBase> getNamesByName(String name);
	
	
}
