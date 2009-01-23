/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.name;

import java.util.List;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

/**
 * @author a.mueller
 *
 */
public interface ITaxonNameDao extends IIdentifiableDao<TaxonNameBase> {
	
	/**
	 * Return a count of names related to this name, optionally filtered 
	 * by relationship type
	 * 
	 * @param name the name
	 * @param type the relationship type (or null to return all relationships) 
	 * @return a count of NameRelationship instances
	 */
	public int countRelatedNames(TaxonNameBase name, NameRelationshipType type);
	
	/**
	 * Return a List of relationships related to this name, optionally filtered 
	 * by relationship type
	 * 
	 * @param name the name
	 * @param type the relationship type (or null to return all relationships) 
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of NameRelationship instances
	 */
	public List<NameRelationship> getRelatedNames(TaxonNameBase name,  NameRelationshipType type, Integer pageSize, Integer pageNumber);
	
	/**
	 * Return a count of hybrids related to this name, optionally filtered 
	 * by hybrid relationship type
	 * 
	 * @param name the name
	 * @param type the hybrid relationship type (or null to return all hybrid) 
	 * @return a count of HybridRelationship instances
	 */
	public int countHybridNames(BotanicalName name, HybridRelationshipType type);
	
	/**
	 * Return a List of hybrids related to this name, optionally filtered 
	 * by hybrid relationship type
	 * 
	 * @param name the name
	 * @param type the hybrid relationship type (or null to return all hybrids) 
	 * @param pageSize The maximum number of hybrid relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of HybridRelationship instances
	 */
	public List<HybridRelationship> getHybridNames(BotanicalName name, HybridRelationshipType type, Integer pageSize, Integer pageNumber);

	/**
	 * Return a count of types related to this name, optionally filtered 
	 * by type designation status
	 * 
	 * @param name the name
	 * @param status the type designation status (or null to return all types) 
	 * @return a count of TypeDesignationBase instances
	 */
	public int countTypeDesignations(TaxonNameBase name, TypeDesignationStatus status);
	
	/**
	 * Return a List of types related to this name, optionally filtered 
	 * by type designation status
	 * 
	 * @param name the name
	 * @param status the type designation status (or null to return all types) 
	 * @param pageSize The maximum number of types returned (can be null for all types)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of TypeDesignationBase instances
	 */
	public List<TypeDesignationBase> getTypeDesignations(TaxonNameBase name, TypeDesignationStatus status, Integer pageSize, Integer pageNumber);

	/**
	 * Returns a List of TaxonNameBase instances that match the properties passed
	 * 
	 * @param uninomial
	 * @param infraGenericEpithet
	 * @param specificEpithet
	 * @param infraspecificEpithet
	 * @param rank
	 * @param pageSize The maximum number of names returned (can be null for all names)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of TaxonNameBase instances
	 */
	public List<TaxonNameBase> searchNames(String uninomial, String infraGenericEpithet, String specificEpithet, String infraspecificEpithet, Rank rank, Integer pageSize, Integer pageNumber);

	/**
	 * Returns a count of TaxonNameBase instances that match the properties passed
	 * 
	 * @param uninomial
	 * @param infraGenericEpithet
	 * @param specificEpithet
	 * @param infraspecificEpithet
	 * @param rank
	 * @return a count of TaxonNameBase instances
	 */
	public int countNames(String uninomial, String infraGenericEpithet, String specificEpithet, String infraspecificEpithet, Rank rank);

	/**
	 * Returns a List of TaxonNameBase instances which nameCache matches the query string
	 * 
	 * @param queryString
	 * @param pageSize The maximum number of names returned (can be null for all names)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of TaxonNameBase instances
	 */
	public List<TaxonNameBase<?,?>> searchNames(String queryString, Integer pageSize, Integer pageNumber);

	/**
	 * Returns a count of TaxonNameBase instances which nameCache matches the String queryString
	 * 
	 * @param queryString
	 * @return a count of TaxonNameBase instances
	 */
	public int countNames(String queryString);
}
