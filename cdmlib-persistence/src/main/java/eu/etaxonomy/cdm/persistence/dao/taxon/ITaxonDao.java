/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.taxon;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.IPublishableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.NameSearchOrder;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.TaxonTitleType;

/**
 * @author a.mueller
 */
public interface ITaxonDao
          extends IIdentifiableDao<TaxonBase>, ITitledDao<TaxonBase>, IPublishableDao<TaxonBase> {


    /**
     * {@inheritDoc}
     * <BR><BR>
     * NOTE: Also taxa with <code>publish=false</code> are returned.
     */
    @Override
    public TaxonBase load(UUID uuid, List<String> propertyPaths);

    /**
     * Returns a list of TaxonBase instances where the taxon.titleCache property matches the name parameter,
     * and taxon.sec matches the sec parameter.
     * @param name
     * @param sec
     * @return
     */
    public List<? extends TaxonBase> getTaxaByName(String name, boolean includeUnpublished, Reference sec);

    /**
     * Returns a list of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false)
     * where the taxonBase.name.nameCache property matches the String queryString, and taxon.sec matches the sec parameter.
     */
    public <S extends TaxonBase> List<S> getTaxaByName(String queryString, Boolean accepted, boolean includeUnpublished, Reference sec);

    /**
     * Returns a list of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false)
     * where the taxonBase.name.nameCache property matches the String queryString.
     *
     * Note: The search result includes a search on titleCache (with {@link MatchMode#BEGINNING} or {@link MatchMode#ANYWHERE} )
     * for all records with protected titleCache (see #9561). Maybe this should be parameterized in future.
     *
     * @return list of found taxa
     */
    public List<TaxonBase> getTaxaByName(boolean doTaxa, boolean doSynonyms, boolean doMisappliedNames, boolean doCommonNames,
            boolean includeAuthors, String queryString, Classification classification, TaxonNode subtree,
            MatchMode matchMode, Set<NamedArea> namedAreas, boolean includeUnpublished,
            NameSearchOrder order, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    public long countTaxaByName(boolean doTaxa, boolean doSynonyms, boolean doMisappliedNames, boolean doCommonNames,
            boolean doIncludeAuthors, String queryString, Classification classification, TaxonNode subtree,
            MatchMode matchMode, Set<NamedArea> namedAreas, boolean includeUnpublished);


    /**
     * Returns a count of TaxonBase instances where the
     * taxon.name properties match the parameters passed.
     *
     * @return a count of TaxonBase instances
     */
    public long countTaxaByName(Class <? extends TaxonBase> clazz, String uninomial, String infragenericEpithet,String specificEpithet,
            String infraspecificEpithet, String authorshipCache, Rank rank);

    /**
     * Returns a list of TaxonBase instances where the
     * taxon.name properties match the parameters passed. In order to search for any string value, pass '*', passing the string value of
     * <i>null</i> will search for those taxa with a value of null in that field
     * <p>
     * Compare with
     * {@link #findByName(String, MatchMode, int, int, boolean)}
     * which searches for {@link TaxonName}<strong><code>.titleCache</code>
     * </strong>
     *
     * @param doTaxa
     * @param doSynonyms
     * @param uninomial
     * @param infragenericEpithet
     * @param specificEpithet
     * @param infraspecificEpithet
     * @param rank
     * @param authorshipCache
     * @param pageSize The maximum number of taxa returned (can be null for all matching taxa)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a list of TaxonBase instances
     */
    public <T extends TaxonBase> List<T> findTaxaByName(Class<T> clazz, String uninomial, String infragenericEpithet, String specificEpithet,
            String infraspecificEpithet, String authorshipCache, Rank rank, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Find taxa by searching for Taxa and Synonyms where the
     * {@link TaxonName}<strong><code>.titleCache</code></strong> matches
     * the name specified as queryString <code>taxonName</code>
     * <P>
     * Compare with
     * {@link #findTaxaByName(Class, String, String, String, String, Rank, Integer, Integer)}
     * which searches for {@link TaxonName}<strong><code>.nameCache</code>
     * </strong>
     * @param queryString
     *            the taqxon Name to search for
     * @param classification TODO
     * @param matchMode
     * @param namedAreas TODO
     * @param pageNumber
     * @param pageSize
     * @param onlyAcccepted
     * @return
     */
    public List<TaxonBase> findByNameTitleCache(boolean doTaxa, boolean doSynonyms, boolean includeUnpublished,
            String queryString, Classification classification, TaxonNode subtree, MatchMode matchMode, Set<NamedArea> namedAreas,
            NameSearchOrder order, Integer pageNumber, Integer pageSize, List<String> propertyPaths) ;

    /**
     * Returns a taxon corresponding to the given uuid
     *
     * @param uuid
     * 			The uuid of the taxon requested
     * @param criteria
     * 			Custom criteria to be added to the default list of applied criteria.
     * @param propertyPaths
     *
     * @return
     */
    public TaxonBase findByUuid(UUID uuid, List<Criterion> criteria, List<String> propertyPaths);

    /**
     * Returns a list of Taxon entities corresponding to the given uuid list.
     * @param uuids
     * @param criteria
     * @param propertyPaths
     * @return
     */
    public List<? extends TaxonBase> findByUuids(List<UUID> uuids, List<Criterion> criteria, List<String> propertyPaths);

    /**
     * @param queryString
     * @param classification
     * @param matchMode
     * @param namedAreas
     * @param pageSize
     * @param pageNumber
     * @param propertyPaths
     * @return A List matching Taxa
     */
    public List<Taxon> getTaxaByCommonName(String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageSize,
            Integer pageNumber, List<String> propertyPaths);

    /**
     * Counts the number of synonyms
     * @param onlyAttachedToTaxon if <code>true</code> only those synonyms being attached to
     * an accepted taxon are counted
     * @return the number of synonyms
     */
    public long countSynonyms(boolean onlyAttachedToTaxon);

    public long countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted);

    public long countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted, List<Criterion> criteria);

    /**
     * Returns a count of the TaxonRelationships (of where relationship.type ==
     * type, if this argument is supplied) where the supplied taxon either is
     * relatedFrom or relatedTo depending on the <code>direction</code>
     * parameter.
     *
     * @param taxon
     *            The taxon that is relatedFrom
     * @param type
     *            The type of TaxonRelationship (can be null)
     * @param direction
     *            specifies the direction of the relationship
     * @return the number of TaxonRelationship instances
     */
    public long countTaxonRelationships(Taxon taxon, TaxonRelationshipType type,
            boolean includeUnpublished, Direction direction);
    public long countTaxonRelationships(Taxon taxon, Set<TaxonRelationshipType> types,
            boolean includeUnpublished, Direction direction);

    /**
     * Returns the TaxonRelationships (of where relationship.type == type, if
     * this argument is supplied) where the supplied taxon either is
     * relatedFrom or relatedTo depending on the <code>direction</code>
     * parameter.
     *
     * @param taxon
     *            The taxon that is relatedTo
     * @param type
     *            The type of TaxonRelationship (can be null)
     * @param pageSize
     *            The maximum number of relationships returned (can be null for
     *            all relationships)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based)
     * @param orderHints
     *            Properties to order by
     * @param propertyPaths
     *            Properties to initialize in the returned entities, following
     *            the syntax described in
     *            {@link IBeanInitializer#initialize(Object, List)}
     * @param direction
     *            specifies the direction of the relationship
     * @return a List of TaxonRelationship instances
     */
    public List<TaxonRelationship> getTaxonRelationships(Taxon taxon,
            TaxonRelationshipType type, boolean includeUnpublished,
            Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths,
            Direction direction);

    public List<TaxonRelationship> getTaxonRelationships(Taxon taxon,
            Set<TaxonRelationshipType> type, boolean includeUnpublished,
            Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths,
            Direction direction);

    /**
     * Returns a count of the Synonyms (where relationship.type == type,
     *  if this argument is supplied) where the supplied taxon is relatedTo.
     *
     * @param taxon The taxon that is relatedTo
     * @param type The type of Synonym (can be null)
     * @return the number of Synonym instances
     */
    public long countSynonyms(Taxon taxon, SynonymType type);

    /**
     * Returns the Synonyms (of where relationship.type == type, if this argument is supplied)
     * that do have the supplied taxon as accepted taxon.
     *
     * @param taxon The accepted taxon
     * @param type The type of synonym (can be null)
     * @param pageSize The maximum number of synonyms returned (can be null for all synonyms)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * * @param orderHints Properties to order by
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a {@link List} of {@link Synonym} instances
     */
    public List<Synonym> getSynonyms(Taxon taxon, SynonymType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Returns a count of the synonyms (where relationship.type == type,
     * if this argument is supplied) which do have an accepted taxon.
     *
     * @param synonym The synonym that is relatedFrom
     * @param type The type of Synonym (can be null)
     * @return the number of Synonym instances
     */
    public long countSynonyms(Synonym synonym, SynonymType type);

    public long countTaxaByCommonName(String searchString,
            Classification classification, MatchMode matchMode,
            Set<NamedArea> namedAreas);

    /**
     * see service layer documentation
     */
    public Map<String, Map<UUID,Set<TaxonName>>> findIdenticalNames(List<UUID> sourceRefUuids, List<String> propertyPaths);

    public List<UuidAndTitleCache<? extends IdentifiableEntity>> getTaxaByNameForEditor(boolean doTaxa, boolean doSynonyms, boolean doNamesWithoutTaxa,
            boolean doMisappliedNames, boolean doCommonNames, boolean includeUnpublished, boolean includeAuthors,
            String queryString, Classification classification, TaxonNode subtree,
            MatchMode matchMode, Set<NamedArea> namedAreas, NameSearchOrder order);

    public List<String> taxaByNameNotInDB(List<String> taxonNames);

    /**
     * This method was originally required when synonyms still had a synonym relationship
     * to taxa and could belong to multiple taxa. Now the method might be obsolete.
     * @param synonym
     * @param classificationFilter
     * @param propertyPaths
     * @see #countAcceptedTaxonFor(Synonym, Classification)
     */
    public Taxon acceptedTaxonFor(Synonym synonym, Classification classificationFilter, List<String> propertyPaths);

    /**
     * This method was originally required when synonyms still had a synonym relationship
     * to taxa and could belong to multiple taxa. Now the method might be obsolete.@param synonym
     * @param classificationFilter
     *
     * @see #acceptedTaxonFor(Synonym, Classification, Integer, Integer, List, List)
     */
    public long countAcceptedTaxonFor(Synonym synonym, Classification classificationFilter);

	public List<UuidAndTitleCache<Taxon>> getTaxaByCommonNameForEditor(
			String titleSearchStringSqlized, Classification classification,
			MatchMode matchMode, Set<NamedArea> namedAreas);

	public <S extends TaxonBase> List<Object[]> findByIdentifier(Class<S> clazz, String identifier,
	        IdentifierType identifierType, TaxonNode subtreeFilter, MatchMode matchmode,
			boolean includeEntity, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

	/**
	 * Counts all taxa which match the given identifier (identifier type, identifier string and match mode).
	 * Optionally a subtreefilter can be defined.
	 *
	 * @param clazz optional, the TaxonBase subclass
	 * @param identifier the identifier string
	 * @param identifierType the identifier type
     * @param matchmode the match mode for the identifier string
	 * @param subtreeFilter the subtree filter as taxon node
	 * @return
	 */
	public <S extends TaxonBase> long countByIdentifier(Class<S> clazz,
			String identifier, IdentifierType identifierType, TaxonNode subtreeFilter, MatchMode matchmode);

	/**
     * Counts all taxa which have the given marker of type markerType and with value markerValue.
     * Additionally an optional subtreefilter can be defined.
     *
     * @param clazz
     * @param markerType
     * @param markerValue
     * @param subtreeFilter
     * @return
     */
    public <S extends TaxonBase> long countByMarker(Class<S> clazz, MarkerType markerType,
            Boolean markerValue, TaxonNode subtreeFilter);

    public <S extends TaxonBase> List<Object[]> findByMarker(Class<S> clazz, MarkerType markerType,
            Boolean markerValue, TaxonNode subtreeFilter, boolean includeEntity, TaxonTitleType titleType,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    public long countTaxonRelationships(Set<TaxonRelationshipType> types);

    public List<TaxonRelationship> getTaxonRelationships(Set<TaxonRelationshipType> types,
            Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths);

    public <S extends TaxonBase> List<S> list(Class<S> type, List<Restriction<?>> restrictions, Integer limit, Integer start,
            List<OrderHint> orderHints, List<String> propertyPaths, boolean includePublished);

    long count(Class<? extends TaxonBase> type, List<Restriction<?>> restrictions, boolean includePublished);

}
