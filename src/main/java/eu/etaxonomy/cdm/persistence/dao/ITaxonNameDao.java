/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao;

import java.util.List;
import eu.etaxonomy.cdm.model.name.TaxonName;

/**
 * @author a.mueller
 *
 */
public interface ITaxonNameDao extends IDao<TaxonName,Integer> {
	
	/* any nice thing to find*/
	public List<TaxonName> getRelatedNames(Integer id);
	
	/* all names*/
	public List<TaxonName> getAllNames();
	
	/* some names*/
	public List<TaxonName> getNamesByName(String name);
	
	
}
