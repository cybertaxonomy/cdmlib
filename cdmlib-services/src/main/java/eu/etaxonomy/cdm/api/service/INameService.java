/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.lucene.index.CorruptIndexException;
import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.api.service.config.NameDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.search.DocumentSearchResult;
import eu.etaxonomy.cdm.api.service.search.LuceneParseException;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

public interface INameService extends IIdentifiableEntityService<TaxonNameBase> {

	/**
	 * Deletes a name. Depening on the configurator state links to the name will either be
	 * deleted or throw exceptions.<BR>
	 * If name is <code>null</code> this method has no effect.
	 * @param name
	 * @param config
	 *
	 */
	public DeleteResult delete(UUID nameUUID, NameDeletionConfigurator config);

	/**
	 * Removes the given type designation from the given taxon name and deletes it from
	 * the database if it is not connected to any other name.
	 * If <code>typeDesignation</code> is <code>null</code> all type designations are deleted
	 * from the given taxon name. If <code>name</code> is <code>null</code> all names are removed from
	 * the given type designation. If both are <code>null</code> nothing happens.
	 * @param typeDesignation
	 * @param name
	 * @return
	 */
	public DeleteResult deleteTypeDesignation(TaxonNameBase name, TypeDesignationBase typeDesignation);

	/**
	 * Removes the given type designation from the given taxon name and deletes it from
	 * the database if it is not connected to any other name.
	 * If <code>typeDesignation</code> is <code>null</code> all type designations are deleted
	 * from the given taxon name. If <code>name</code> is <code>null</code> all names are removed from
	 * the given type designation. If both are <code>null</code> nothing happens.
	 * @param typeDesignation
	 * @param name
	 * @return
	 */
	public DeleteResult deleteTypeDesignation(UUID nameUuid, UUID typeDesignationUuid);


	/**
	 * Saves the given type designations.
	 * @param typeDesignationCollection
	 * @return
	 */
	public Map<UUID, TypeDesignationBase> saveTypeDesignationAll(Collection<TypeDesignationBase> typeDesignationCollection);

	public Map<UUID, ReferencedEntityBase> saveReferencedEntitiesAll(Collection<ReferencedEntityBase> referencedEntityCollection);

	/**
	 * Saves the given homotypical groups.
	 * @param homotypicalGroups
	 * @return
	 */
	public Map<UUID, HomotypicalGroup> saveAllHomotypicalGroups(Collection<HomotypicalGroup> homotypicalGroups);

	/**
	 * Returns all nomenclatural status.
	 * @param limit
	 * @param start
	 * @return
	 */
	public List<NomenclaturalStatus> getAllNomenclaturalStatus(int limit, int start);

	/**
	 * Returns all type designations.
	 * @param limit
	 * @param start
	 * @return
	 */
	public List<TypeDesignationBase> getAllTypeDesignations(int limit, int start);

	/**
	 * Returns all NonViralNames with a name cache that matches the given string
	 * @param name
	 * @return
	 */
	public List<TaxonNameBase> getNamesByNameCache(String nameCache);

	/**
	 * Returns all NonViralNames with a title cache that matches the given string
	 * using the given match mode and initialization strategy
	 *
	 * @param name
	 * @param matchMode
	 * @param propertyPaths
	 * @return
	 */
	public List<TaxonNameBase> findNamesByTitleCache(String titleCache, MatchMode matchMode, List<String> propertyPaths);

	/**
	 * Returns all NonViralNames with a name cache that matches the given string
	 * using the given match mode and initialization strategy
	 *
	 * @param name
	 * @param matchMode
	 * @param propertyPaths
	 * @return
	 */
	public List<TaxonNameBase> findNamesByNameCache(String nameCache, MatchMode matchMode, List<String> propertyPaths);

	/**
	 * @param name
	 * @param sessionObject An object that is attached to the session before executing the query
	 * @return
	 */
	public List getNamesByName(String name, CdmBase sessionObject);

	/**
	 * Fuzzy matching for the taxon name elements. The input name is first atomised using the {@link NonViralNameParserImpl}
	 * into its separate parts (genusOrUninomial,infraGenericEpithet,specificEpithet,infraGenericEpithet,authorshipCache).
	 * Each field is then matched separately with the same accuracy parameter.
	 *
	 * @param name taxon name to fuzzy match
	 * @param accuracy value > 0.0 and < 1.0 which determines the accuracy of the result.
	 * @param languages list of languages to consider when matching (currently not used)
	 * @param highlightFragments
	 * @param propertyPaths
	 * @param maxNoOfResults
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws ParseException
	 */
	public List<SearchResult<TaxonNameBase>> findByNameFuzzySearch(
            String name,
            float accuracy,
            List<Language> languages,
            boolean highlightFragments,
            List<String> propertyPaths,
            int maxNoOfResults) throws IOException, LuceneParseException;

	/**
	 * Fuzzy matching for the taxon name elements using only the lucene index.
	 *
	 * The input name is first atomised using the {@link NonViralNameParserImpl}
	 * into its separate parts (genusOrUninomial,infraGenericEpithet,specificEpithet,infraGenericEpithet,authorshipCache).
	 * Each field is then matched separately with the same accuracy parameter.
	 *
	 * @param name taxon name to fuzzy match
	 * @param accuracy value > 0.0 and < 1.0 which determines the accuracy of the result.
	 * @param languages list of languages to consider when matching (currently not used)
	 * @param highlightFragments
	 * @param maxNoOfResults
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws ParseException
	 */
    public List<DocumentSearchResult> findByNameFuzzySearch(
            String name,
            float accuracy,
            List<Language> languages,
            boolean highlightFragments,
            int maxNoOfResults) throws IOException, LuceneParseException;

	/**
	 * Fuzzy matching against the name cache using only the lucene index.
	 *
	 *
	 * @param name taxon name to fuzzy match
	 * @param accuracy value > 0.0 and < 1.0 which determines the accuracy of the result.
	 * @param languages list of languages to consider when matching (currently not used)
	 * @param highlightFragments
	 * @param maxNoOfResults
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws ParseException
	 */
    public List<DocumentSearchResult> findByFuzzyNameCacheSearch(
            String name,
            float accuracy,
            List<Language> languages,
            boolean highlightFragments,
            int maxNoOfResults) throws IOException, LuceneParseException;

	/**
	 * Exact matching for the taxon name elements using only the lucene index.
	 *
	 * The input name is first atomised using the {@link NonViralNameParserImpl}
	 * into its separate parts (genusOrUninomial,infraGenericEpithet,specificEpithet,infraGenericEpithet,authorshipCache).
	 * Each field is then matched separately with the same accuracy parameter.
	 *
	 * @param name taxon name to fuzzy match
	 * @param wildcard boolean flag to indicate whether a wildcard '*' should be added at the end of the query
	 * @param languages list of languages to consider when matching (currently not used)
	 * @param highlightFragments
	 * @param maxNoOfResults
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws ParseException
	 */

    public List<DocumentSearchResult> findByNameExactSearch(
            String name,
            boolean wildcard,
            List<Language> languages,
            boolean highlightFragments,
            int maxNoOfResults) throws IOException, LuceneParseException;

	// TODO: Remove getNamesByName() methods. Use findNamesByTitle() instead.

    public List<HomotypicalGroup> getAllHomotypicalGroups(int limit, int start);

	@Deprecated
    public List<RelationshipBase> getAllRelationships(int limit, int start);


	/**
	 * Return a List of name relationships in which this name is related to
	 * another name, optionally filtered by relationship type
	 *
	 * @param name
	 *            the name on either the <i>"from side"</i> or on the
	 *            <i>"to side"</i> of the relationship, depending on the
	 *            <code>direction</code> of the relationship.
	 * @param direction
	 *            the direction of the NameRelationship, may be null to return all relationships
	 * @param type
	 *            the relationship type (or null to return all relationships)
	 * @param pageSize
	 *            The maximum number of relationships returned (can be null for
	 *            all relationships)
	 * @param pageNumber
	 *            The offset (in pageSize chunks) from the start of the result
	 *            set (0 - based)
	 * @param orderHints
	 *            may be null
	 * @param propertyPaths
	 *            properties to initialize - see
	 *            {@link IBeanInitializer#initialize(Object, List)}
	 * @return a Pager of NameRelationship instances
	 */
	public List<NameRelationship> listNameRelationships(TaxonNameBase name,  NameRelationship.Direction direction, NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Return a List of name relationships in which this name is related to another name, optionally filtered
	 * by relationship type
	 *
	 * @param name the name on the <i>"from side"</i> of the relationship
	 * @param direction the direction of the NameRelationship
	 * @param type the relationship type (or null to return all relationships)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a Pager of NameRelationship instances
	 */
	public Pager<NameRelationship> pageNameRelationships(TaxonNameBase name,  NameRelationship.Direction direction, NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Return a List of relationships in which this name is related to another name, optionally filtered
	 * by relationship type
	 *
	 * @param name the name on the <i>"from side"</i> of the relationship
	 * @param type the relationship type (or null to return all relationships)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a Pager of NameRelationship instances
	 * @deprecated use {@link #listNameRelationships(TaxonNameBase, eu.etaxonomy.cdm.model.common.RelationshipBase.Direction, NameRelationshipType, Integer, Integer, List, List)} instead
	 */
	@Deprecated
	public List<NameRelationship> listFromNameRelationships(TaxonNameBase name,  NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Return a List of relationships in which this name is related to another name, optionally filtered
	 * by relationship type
	 *
	 * @param name the name on the <i>"from side"</i> of the relationship
	 * @param type the relationship type (or null to return all relationships)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a Pager of NameRelationship instances
	 * @deprecated use {@link #pageNameRelationships(TaxonNameBase, eu.etaxonomy.cdm.model.common.RelationshipBase.Direction, NameRelationshipType, Integer, Integer, List, List)} instead
	 */
	@Deprecated
	public Pager<NameRelationship> pageFromNameRelationships(TaxonNameBase name,  NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Return a List of relationships in which another name is related to this name, optionally filtered
	 * by relationship type
	 *
	 * @param name the name on the <i>"to side"</i> of the relationship
	 * @param type the relationship type (or null to return all relationships)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a Pager of NameRelationship instances
	 * @deprecated use {@link #listNameRelationships(TaxonNameBase, eu.etaxonomy.cdm.model.common.RelationshipBase.Direction, NameRelationshipType, Integer, Integer, List, List)} instead
	 */
	@Deprecated
	public List<NameRelationship> listToNameRelationships(TaxonNameBase name,  NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Return a List of relationships in which another name is related to this name, optionally filtered
	 * by relationship type
	 *
	 * @param name the name on the <i>"to side"</i> of the relationship
	 * @param type the relationship type (or null to return all relationships)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a Pager of NameRelationship instances
	 * @deprecated use {@link #pageNameRelationships(TaxonNameBase, eu.etaxonomy.cdm.model.common.RelationshipBase.Direction, NameRelationshipType, Integer, Integer, List, List)} instead
	 */
	@Deprecated
	public Pager<NameRelationship> pageToNameRelationships(TaxonNameBase name,  NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);


	/**
	 * Return a List of hybrids related to this name, optionally filtered
	 * by hybrid relationship type
	 *
	 * @param name the name
	 * @param type the hybrid relationship type (or null to return all hybrids)
	 * @param pageSize The maximum number of hybrid relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a Pager of HybridRelationship instances
	 */
	public Pager<HybridRelationship> getHybridNames(INonViralName name, HybridRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Return a List of types related to this name, optionally filtered
	 * by type designation status
	 *
	 * @param name the name
	 * @param status the type designation status (or null to return all types)
	 * @param pageSize The maximum number of types returned (can be null for all types)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager of TypeDesignationBase instances
	 */
	public Pager<TypeDesignationBase> getTypeDesignations(TaxonNameBase name,
			SpecimenTypeDesignationStatus status, Integer pageSize, Integer pageNumber);

	public Pager<TypeDesignationBase> getTypeDesignations(TaxonNameBase name,
			SpecimenTypeDesignationStatus status, Integer pageSize, Integer pageNumber, List<String> propertyPaths);


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
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a Pager of TaxonNameBase instances
	 */
	public Pager<TaxonNameBase> searchNames(String uninomial, String infraGenericEpithet, String specificEpithet, String infraspecificEpithet, Rank rank, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Returns a Paged List of TaxonNameBase instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
	 *
	 * @param clazz filter the results by class (or pass null to return all TaxonNameBase instances)
	 * @param queryString
	 * @param pageSize The maximum number of names returned (can be null for all matching names)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths properties to be initialized
	 * @return a Pager TaxonNameBase instances
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	@Override
    public Pager<TaxonNameBase> search(Class<? extends TaxonNameBase> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Returns a map that holds uuid, titleCache pairs of all names in the current database
	 *
	 * @return
	 * 			a <code>Map</code> containing uuid and titleCache of names
	 */
	public List<UuidAndTitleCache> getUuidAndTitleCacheOfNames(Integer limit, String pattern);

	/**
	 * Return a Pager of names matching the given query string, optionally filtered by class, optionally with a particular MatchMode
	 *
	 * @param clazz filter by class - can be null to include all instances of type T
	 * @param queryString the query string to filter by
	 * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
	 * @param criteria additional criteria to filter by
	 * @param pageSize The maximum number of objects returned (can be null for all objects)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @return a paged list of instances of type T matching the queryString
	 */
    public Pager<TaxonNameBase> findByName(Class<? extends TaxonNameBase> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Returns a homotypical group with the given UUID or null if not homotypical group exists with that UUID
     *
     * @param uuid the uuid of the homotypical group
     * @return a homotypical group
     */
    public HomotypicalGroup findHomotypicalGroup(UUID uuid);

    /**
     * @param uuid
     * @return
     */
    public List<TaggedText> getTaggedName(UUID uuid);

    /**
     * @param nameUuid
     * @return
     */
    public UpdateResult setAsGroupsBasionym(UUID nameUuid);

	public List<HashMap<String, String>> getNameRecords();

    /**
     * @param name
     * @param config
     * @return
     */
    DeleteResult delete(TaxonNameBase name, NameDeletionConfigurator config);


}
