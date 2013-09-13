package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.description.IIdentificationKey;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

public interface IIdentificationKeyService {
	
	/**
	 * Returns a paged list of IIdentificationKey entities sorted by creation date, newest first
	 * 
	 * @param pageSize The maximum number of objects returned (can be null for all matching objects)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based, 
	 *                   can be null, equivalent of starting at the beginning of the recordset)
	 * @param propertyPaths properties to be initialized
	 * @return a pager of IdentificationKeys
	 */
	public Pager<IIdentificationKey> page(Integer pageSize, Integer pageNumber, List<String> propertyPaths);

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
	 * @return a pager of IdentificationKeys
	 */
	public <T extends IIdentificationKey> Pager<T> findKeysConvering(
			TaxonBase taxon, Class<T> type, Integer pageSize,
			Integer pageNumber, List<String> propertyPaths);

}
