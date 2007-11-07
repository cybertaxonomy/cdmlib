/**
 * 
 */
package eu.etaxonomy.cdm.strategy;

import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * @author a.mueller
 *
 */
public interface INameCacheStrategy extends IStrategy {
	
	//returns the composed name string without author or year
	public String getNameCache(VersionableEntity object);
	
	//returns the composed name string with author and/or year
	public String getFullNameCache(VersionableEntity object);
	
}
