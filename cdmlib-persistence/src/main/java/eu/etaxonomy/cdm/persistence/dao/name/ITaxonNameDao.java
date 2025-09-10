/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dao.name;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.TaxonNameParts;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
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
	public long countNameRelationships(TaxonName name, NameRelationship.Direction direction, NameRelationshipType type);

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
	public long countHybridNames(INonViralName name, HybridRelationshipType type);

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
	public long countTypeDesignations(TaxonName name,
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
			TypeDesignationStatusBase<?> status, Integer pageSize, Integer pageNumber,
			List<String> propertyPaths);

	/**
	 * Return a list ids of specimens that are type specimens for the given name
	 * @param name
	 * @param status
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 */
	public List<Integer> getTypeSpecimenIdsForTaxonName(TaxonName name,
	        TypeDesignationStatusBase<?> status, Integer pageSize, Integer pageNumber);

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
	public long countNames(String uninomial, String infraGenericEpithet,
			String specificEpithet, String infraspecificEpithet, Rank rank);

	/**
	 * Returns a count of TaxonName instances that match the properties passed
	 *
	 * @param queryString
	 * @param matchMode
	 * @param criteria
	 */
	public long countNames(String queryString, MatchMode matchMode, List<Criterion> criteria);

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
	public List<TaxonName> searchNames(String queryString,
			Integer pageSize, Integer pageNumber);



	/**
	 * Returns a count of TaxonName instances which nameCache matches the
	 * String queryString
	 *
	 * @param queryString
	 * @return a count of TaxonName instances
	 */
	public long countNames(String queryString);

	/**
	 * @param queryString
	 * @param matchmode
	 * @param pageSize
	 * @param pageNumber
	 * @param criteria
	 * @param propertyPaths TODO
	 * @return
	 */
	public List<TaxonName> findByName(boolean doIncludeAuthors,
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
	public List<TaxonName> findByFullTitle(String queryString,
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
    public List<TaxonName> findByTitle(String queryString,
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
	public TaxonName findByUuid(UUID uuid, List<Criterion> criteria, List<String> propertyPaths);

	/**
	 * @param queryString
	 * @param matchmode
	 * @param criteria
	 * @return
	 */
	public Integer countByName(String queryString,
			MatchMode matchmode, List<Criterion> criteria);

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
	public List<TaxonName> findByName(Class<TaxonName> clazz, String queryString,
	        MatchMode matchmode, List<Criterion> criteria,Integer pageSize, Integer pageNumber,
	        List<OrderHint> orderHints,	List<String> propertyPaths);

	/**
	 * @param clazz
	 * @param queryString
	 * @param matchmode
	 * @param criteria
	 * @return
	 */
	public long countByName(Class<TaxonName> clazz, String queryString, MatchMode matchmode, List<Criterion> criteria);

	public TaxonName findZoologicalNameByUUID(UUID uuid);

	List<HashMap<String, String>> getNameRecords();

	/**
	 * Supports using wildcards in the query parameters.
	 * If a name part passed to the method contains the
	 * asterisk character ('*') it will be translated into '%' the related field is search with a LIKE clause.
	 * <p>
	 * A query parameter which is passed as <code>NULL</code> value will be ignored.
	 * A parameter passed as {@link Optional} object containing a <code>NULL</code> value will be used
	 * to filter select taxon names where the according field is <code>null</code>.
	 *
	 * @param genusOrUninomial
	 * @param infraGenericEpithet
	 * @param specificEpithet
	 * @param infraSpecificEpithet
	 * @param rank
	 *     Only name having the specified rank are taken into account.
	 * @param excludedNamesUuids
     *     Names to be excluded from the result set
	 * @return
	 */
	public List<TaxonNameParts> findTaxonNameParts(Optional<String> genusOrUninomial, Optional<String> infraGenericEpithet, Optional<String> specificEpithet,
	        Optional<String> infraSpecificEpithet, Rank rank, Collection<UUID> excludedNamesUuids, Integer pageSize, Integer pageIndex, List<OrderHint> orderHints);
    /**
     * Count method complementing {@link #findTaxonNameParts(Optional, Optional, Optional, Optional, Rank)}
     *
     * @param genusOrUninomial
     * @param infraGenericEpithet
     * @param specificEpithet
     * @param infraSpecificEpithet
     * @param rank
     *     Only name having the specified rank are taken into account.
     * @param excludedNamesUuids
     *     Names to be excluded from the result set
     * @return
     */
    public long countTaxonNameParts(Optional<String> genusOrUninomial, Optional<String> infraGenericEpithet, Optional<String> specificEpithet, Optional<String> infraSpecificEpithet,
            Rank rank, Collection<UUID> excludedNames);

    public <S extends TaxonName>List<S> list(Class<S> type, List<Restriction<?>> restrictions, Integer limit, Integer start,
            List<OrderHint> orderHints, List<String> propertyPaths, boolean includePublished);

    long count(Class<? extends TaxonName> type, List<Restriction<?>> restrictions, boolean includePublished);

    /**
     * Returns the number of name relationships of the given name relationship types or
     * all types if types is <code>null</code>.
     * @param types
     * @return the number of name relationships
     */
    public long countNameRelationships(Set<NameRelationshipType> types);

    public List<NameRelationship> getNameRelationships(Set<NameRelationshipType> types, Integer pageSize,
            Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Returns the number of hybrid relationships of the given hybrid relationship types or
     * all types if types is <code>null</code>.
     * @param types
     * @return the number of hybrid relationships
     */
    public long countHybridRelationships(Set<HybridRelationshipType> types);

    public List<HybridRelationship> getHybridRelationships(Set<HybridRelationshipType> types, Integer pageSize,
            Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    public long countByFullTitle(Class<TaxonName> clazz, String queryString, MatchMode matchmode,
            List<Criterion> criteria);

    /**
     * Returns a list of distinct {@link String}s containing all values for TaxonName.genusOrUninomial
     * in the database. The result may be filtered on {@link String}s that match the given <code>pattern</code>
     * parameter. The pattern understands * or % for a general wildcard and _ or ? for single character wildcard.
     * Also a maximum and/minimum rank may be given.
     *
     * @param param the genusOrUninomial pattern to search for
     * @param maxRank the maximum rank of the names checked
     * @param minRank the maximum rank of the names checked
     * @return
     */
    public List<String> distinctGenusOrUninomial(String pattern, Rank maxRank, Rank minRank);

    public List<TypeDesignationBase<?>> getAllTypeDesignations(Integer limit, Integer start);

    public List<TypeDesignationStatusBase> getTypeDesignationStatusInUse();
}
