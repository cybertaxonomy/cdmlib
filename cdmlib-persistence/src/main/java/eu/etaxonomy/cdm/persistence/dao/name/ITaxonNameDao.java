/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dao.name;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 *
 */
public interface ITaxonNameDao extends IIdentifiableDao<TaxonName> {

	/**
	 * Return a count of names related to or from this name, optionally filtered
	 * by relationship type. The direction of the relationships taken in to account is depending on
	 * the <code>direction</code> parameter.
	 *
	 * @param name
	 *            the name
	 * @param direction
	 *            specifies the direction of the relationship
	 * @param type
	 *            the relationship type (or null to return all relationships)
	 * @return a count of NameRelationship instances
	 */
	public int countNameRelationships(TaxonName name, NameRelationship.Direction direction, NameRelationshipType type);

	/**
	 * Return a List of relationships related to or from this name, optionally filtered
	 * by relationship type. The direction of the relationships taken in to account is depending on
	 * the <code>direction</code> parameter.
	 * If both name and direction is null all name relationships will be returned.
	 *
	 * @param name
	 *            the name
	 * @param direction
	 *            specifies the direction of the relationship, may be null to return all relationships
	 * @param type
	 *            the relationship type (or null to return all relationships)
	 * @param pageSize
	 *            The maximum number of relationships returned (can be null for
	 *            all relationships)
	 * @param pageNumber
	 *            The offset (in pageSize chunks) from the start of the result
	 *            set (0 - based) of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a List of NameRelationship instances
	 */
	public List<NameRelationship> getNameRelationships(TaxonName name, NameRelationship.Direction direction,
			NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
			List<String> propertyPaths);

	/**
	 * Return a count of hybrids related to this name, optionally filtered by
	 * hybrid relationship type
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the hybrid relationship type (or null to return all hybrid)
	 * @return a count of HybridRelationship instances
	 */
	public int countHybridNames(INonViralName name, HybridRelationshipType type);

	/**
	 * Return a List of hybrids related to this name, optionally filtered by
	 * hybrid relationship type
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the hybrid relationship type (or null to return all hybrids)
	 * @param pageSize
	 *            The maximum number of hybrid relationships returned (can be
	 *            null for all relationships)
	 * @param pageNumber
	 *            The offset (in pageSize chunks) from the start of the result
	 *            set (0 - based)
	 * @return a List of HybridRelationship instances
	 */
	public List<HybridRelationship> getHybridNames(INonViralName name, HybridRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Return a count of types related to this name, optionally filtered by type
	 * designation status
	 *
	 * @param name
	 *            the name
	 * @param status
	 *            the type designation status (or null to return all types)
	 * @return a count of TypeDesignationBase instances
	 */
	public int countTypeDesignations(TaxonName name,
			SpecimenTypeDesignationStatus status);

	/**
	 * Return a List of types related to this name, optionally filtered by type
	 * designation status
	 *
	 * @param name
	 *            the name
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
	public <T extends TypeDesignationBase> List<T> getTypeDesignations(TaxonName name,
			Class<T> type,
			TypeDesignationStatusBase status, Integer pageSize, Integer pageNumber,
			List<String> propertyPaths);

	/**
	 * Return a List of types related to this name, optionally filtered by type
	 * designation status
	 *
	 * @param name
	 *            the name
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
	 * @deprecated use {@link #getTypeDesignations(TaxonName, Class, TypeDesignationStatusBase, Integer, Integer, List)} instead
	 */
	@Deprecated
	public List<TypeDesignationBase> getTypeDesignations(TaxonName name,
			TypeDesignationStatusBase status, Integer pageSize, Integer pageNumber,
			List<String> propertyPaths);

	/**
	 * Returns a List of TaxonName instances that match the properties
	 * passed
	 *
	 * @param uninomial
	 * @param infraGenericEpithet
	 * @param specificEpithet
	 * @param infraspecificEpithet
	 * @param rank
	 * @param pageSize
	 *            The maximum number of names returned (can be null for all
	 *            names)
	 * @param pageNumber
	 *            The offset (in pageSize chunks) from the start of the result
	 *            set (0 - based)
	 * @param propertyPaths
	 * @param orderHints
	 * @return a List of TaxonName instances
	 */
	public List<TaxonName> searchNames(String uninomial,
			String infraGenericEpithet, String specificEpithet,
			String infraspecificEpithet, Rank rank, Integer pageSize,
			Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Returns a count of TaxonName instances that match the properties
	 * passed
	 *
	 * @param uninomial
	 * @param infraGenericEpithet
	 * @param specificEpithet
	 * @param infraspecificEpithet
	 * @param rank
	 * @return a count of TaxonName instances
	 */
	public int countNames(String uninomial, String infraGenericEpithet,
			String specificEpithet, String infraspecificEpithet, Rank rank);

	/**
	 * Returns a count of TaxonName instances that match the properties passed
	 *
	 * @param queryString
	 * @param matchMode
	 * @param criteria
	 */
	public int countNames(String queryString, MatchMode matchMode, List<Criterion> criteria);

	/**
	 * Returns a List of TaxonName instances which nameCache matches the
	 * query string
	 *
	 * @param queryString
	 * @param pageSize
	 *            The maximum number of names returned (can be null for all
	 *            names)
	 * @param pageNumber
	 *            The offset (in pageSize chunks) from the start of the result
	 *            set (0 - based)
	 * @return a List of TaxonName instances
	 */
	public List<TaxonName<?, ?>> searchNames(String queryString,
			Integer pageSize, Integer pageNumber);



	/**
	 * Returns a count of TaxonName instances which nameCache matches the
	 * String queryString
	 *
	 * @param queryString
	 * @return a count of TaxonName instances
	 */
	public int countNames(String queryString);

	/**
	 * @param queryString
	 * @param matchmode
	 * @param pageSize
	 * @param pageNumber
	 * @param criteria
	 * @param propertyPaths TODO
	 * @return
	 */
	public List<? extends TaxonName<?, ?>> findByName(boolean doIncludeAuthors,
	        String queryString,
			MatchMode matchmode, Integer pageSize, Integer pageNumber,
			List<Criterion> criteria, List<String> propertyPaths);

	/**
	 * @param queryString
	 * @param matchmode
	 * @param pageSize
	 * @param pageNumber
	 * @param criteria
	 * @param propertyPaths TODO
	 * @return
	 */
	public List<? extends TaxonName<?, ?>> findByTitle(String queryString,
			MatchMode matchmode, Integer pageSize, Integer pageNumber,
			List<Criterion> criteria, List<String> propertyPaths);

	/**
	 * Returns a taxon name corresponding to the given uuid
	 *
	 * @param uuid
	 * 			The uuid of the taxon name requested
	 * @param criteria
	 * 			Custom criteria to be added to the default list of applied criteria.
	 * @param propertyPaths
	 *
	 * @return
	 */
	public TaxonName<?, ?> findByUuid(UUID uuid, List<Criterion> criteria, List<String> propertyPaths);

	/**
	 * @param queryString
	 * @param matchmode
	 * @param criteria
	 * @return
	 */
	public Integer countByName(String queryString,
			MatchMode matchmode, List<Criterion> criteria);

	public List<RelationshipBase> getAllRelationships(Integer limit, Integer start);

	public List<UuidAndTitleCache> getUuidAndTitleCacheOfNames(Integer limit, String pattern);

	/**
	 * @param clazz
	 * @param queryString
	 * @param matchmode
	 * @param pageSize
	 * @param pageNumber
	 * @param criteria
	 * @param orderHints
	 * @param propertyPaths TODO
	 * @return
	 */
	public List<TaxonName> findByName(Class<? extends TaxonName> clazz,	String queryString, MatchMode matchmode, List<Criterion> criteria,Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths);

	/**
	 * @param clazz
	 * @param queryString
	 * @param matchmode
	 * @param criteria
	 * @return
	 */
	public long countByName(Class<? extends TaxonName> clazz, String queryString, MatchMode matchmode, List<Criterion> criteria);

	public IZoologicalName findZoologicalNameByUUID(UUID uuid);

	List<HashMap<String, String>> getNameRecords();
}
