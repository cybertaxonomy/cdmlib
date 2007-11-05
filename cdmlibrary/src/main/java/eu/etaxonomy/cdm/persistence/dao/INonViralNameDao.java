/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao;

import java.util.List;
import eu.etaxonomy.cdm.model.name.NonViralName;

/**
 * @author a.mueller
 *
 */
public interface INonViralNameDao extends IDao<NonViralName,Integer> {
	
	/* any nice thing to find*/
	public List<NonViralName> getRelatedNames(Integer id);
	
	/* all names*/
	public List<NonViralName> getAllNames();
	
	/* some names*/
	public List<NonViralName> getNamesByName(String name);
	
	
}
