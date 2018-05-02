/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.name;

import java.util.List;

import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;

/**
 * @author a.babadshanjan
 * @since 24.09.2008
 */
public interface IHomotypicalGroupDao extends ICdmEntityDao<HomotypicalGroup> {
	
	/**
	 * Return a List of types related to the given HomotypicalGroup
	 * , optionally filtered by type designation status
	 * 
	 * @param homotypicalGroup
	 *            the homotypicalGroup
	 * @param type
	 * 			  limit the result set to a specific subtype of TypeDesignationBase, may be null
	 * @param status
	 *            the type designation status (or null to return all types)
	 * @param pageSize
	 *            The maximum number of types returned (can be null for all
	 *            types)
	 * @param pageNumber
	 *            The offset (in pageSize chunks) from the start of the result
	 *            set (0 - based)
	 * @param propertyPaths
	 * @return a List of TypeDesignationBase instances
	 */
	public <T extends TypeDesignationBase> List<T> getTypeDesignations(HomotypicalGroup homotypicalGroup, 
			Class<T> type,
			TypeDesignationStatusBase status, Integer pageSize, Integer pageNumber,
			List<String> propertyPaths);

}
