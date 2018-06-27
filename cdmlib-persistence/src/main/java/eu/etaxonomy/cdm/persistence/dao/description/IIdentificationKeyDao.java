/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.description.IIdentificationKey;

/**
 * A read-only interface to allow querying across all IIdentificationKey instances, regardless of type
 * @author ben.clark
 * @version 1.0
 * @since 21-Dec-2009 13:48:10
 */
public interface IIdentificationKeyDao {


	/**
	 * Returns a sublist of IIdentificationKey instances stored in the database. A maximum
	 * of 'limit' objects are returned, starting at object with index 'start'.
	 *
	 * @param type
	 * @param limit
	 *            the maximum number of entities returned (can be null to return
	 *            all entities)
	 * @param start
	 * @return
	 * @throws DataAccessException
	 */
	public List<IIdentificationKey> list(Integer limit, Integer start, List<String> propertyPaths);

	/**
	 * Returns the number of objects of type IIdentificationKey
	 * @return
	 */
	public long count();

	/**
	 * Finds IdentificationKeys which cover the Taxon given as parameter
	 *
	 * @param taxon
	 *            The Taxon to search IdentificationKeys for
	 * @param type
	 *            may restrict the type to a specific implementation of
	 *            IIdentificationKey
	 * @param pageSize
	 *            The maximum number of objects returned (can be null for all
	 *            matching objects)
	 * @param pageNumber
	 *            The offset (in pageSize chunks) from the start of the result
	 *            set (0 - based, can be null, equivalent of starting at the
	 *            beginning of the recordset)
	 * @param propertyPaths
	 *            properties to be initialized
	 * @return a List of IdentificationKeys
	 */
	public <T extends IIdentificationKey> List<T> findByTaxonomicScope(
	        UUID taxonUuid, Class<T> type, Integer pageSize,
			Integer pageNumber, List<String> propertyPaths);

	/**
	 * Counts IdentificationKeys which cover the Taxon given as parameter
	 *
	 * @param taxon The Taxon to search IdentificationKeys for
	 * @param type may restrict the type to a specific implementation of
	 *            IIdentificationKey
	 * @return
	 */
	public <T extends IIdentificationKey> long countByTaxonomicScope(UUID taxonUuid, Class<T> type);
}
