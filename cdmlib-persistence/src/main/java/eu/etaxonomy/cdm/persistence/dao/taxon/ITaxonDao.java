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
import java.util.Set;
import java.util.UUID;

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 *
 */
public interface ITaxonDao extends IIdentifiableDao<TaxonBase>, ITitledDao<TaxonBase> {

    /**
     * Returns a list of TaxonBase instances where the taxon.titleCache property matches the name parameter,
     * and taxon.sec matches the sec parameter.
     * @param name
     * @param sec
     * @return
     */
    public List<TaxonBase> getTaxaByName(String name, Reference sec);

    /**
     * Returns a list of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false)
     * where the taxonBase.name.nameCache property matches the String queryString, and taxon.sec matches the sec parameter.
     * @param name
     * @param sec
     * @return
     */
    public List<TaxonBase> getTaxaByName(String queryString, Boolean accepted, Reference sec);

    /**
     * Returns a list of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false)
     * where the taxonBase.name.nameCache property matches the String queryString.
     * @param queryString
     * @param matchMode
     * @param accepted
     * @param pageSize
     * @param pageNumber
     * @return
     */
    public List<TaxonBase> getTaxaByName(String queryString, MatchMode matchMode,
            Boolean accepted, Integer pageSize, Integer pageNumber);


    /**
     * Returns a list of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false)
     * where the taxonBase.name.nameCache property matches the String queryString.
     * @param doTaxa
     * @param doSynonyms
     * @param queryString
     * @param classification TODO
     * @param matchMode
     * @param namedAreas TODO
     * @param pageSize
     * @param pageNumber
     * @param propertyPaths TODO
     * @return list of found taxa
     */
    public List<TaxonBase> getTaxaByName(boolean doTaxa, boolean doSynonyms, boolean doMisappliedNames, String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * @param doTaxa
     * @param doSynonyms
     * @param queryString
     * @param classification TODO
     * @param matchMode
     * @param namedAreas
     * @param pageSize
     * @param pageNumber
     * @param propertyPaths
     * @return
     */
    public long countTaxaByName(boolean doTaxa, boolean doSynonyms, boolean doMisappliedNames, String queryString, Classification classification,

            MatchMode matchMode, Set<NamedArea> namedAreas);

//	/**
//	 * @param queryString
//	 * @param matchMode
//	 * @param accepted
//	 * @return
//	 */
//	public Integer countTaxaByName(String queryString, MatchMode matchMode,
//			Boolean accepted);

//	/**
//	 * Returns a count of TaxonBase instances where the
//	 * taxon.name properties match the parameters passed.
//	 *
//	 * @param queryString search string
//	 * @param matchMode way how search string shall be matched: exact, beginning, or anywhere
//	 * @param selectModel all taxon base, taxa, or synonyms
//	 */
//	public Integer countTaxaByName(String queryString, MatchMode matchMode, SelectMode selectMode);

    /**
     * Returns a count of TaxonBase instances where the
     * taxon.name properties match the parameters passed.
     *
     * @param doTaxa
     * @param doSynonyms
     * @param uninomial
     * @param infragenericEpithet
     * @param specificEpithet
     * @param infraspecificEpithet
     * @param rank
     * @return a count of TaxonBase instances
     */
    public int countTaxaByName(Class <? extends TaxonBase> clazz, String uninomial, String infragenericEpithet,String specificEpithet, String infraspecificEpithet, Rank rank);

    /**
     * Returns a list of TaxonBase instances where the
     * taxon.name properties match the parameters passed. In order to search for any string value, pass '*', passing the string value of
     * <i>null</i> will search for those taxa with a value of null in that field
     * <p>
     * Compare with
     * {@link #findByName(String, MatchMode, int, int, boolean)}
     * which searches for {@link TaxonNameBase}<strong><code>.titleCache</code>
     * </strong>
     *
     * @param doTaxa
     * @param doSynonyms
     * @param uninomial
     * @param infragenericEpithet
     * @param specificEpithet
     * @param infraspecificEpithet
     * @param rank
     * @param pageSize The maximum number of taxa returned (can be null for all matching taxa)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a list of TaxonBase instances
     */
    public List<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String uninomial, String infragenericEpithet, String specificEpithet, String infraspecificEpithet, String authorship, Rank rank, Integer pageSize, Integer pageNumber);

    /**
     * Find taxa by searching for Taxa and Synonyms where the
     * {@link TaxonNameBase}<strong><code>.titleCache</code></strong> matches
     * the name specified as queryString <code>taxonName</code>
     * <P>
     * Compare with
     * {@link #findTaxaByName(Class, String, String, String, String, Rank, Integer, Integer)}
     * which searches for {@link TaxonNameBase}<strong><code>.nameCache</code>
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
    public List<TaxonBase> findByNameTitleCache(boolean doTaxa, boolean doSynonyms, String queryString, Classification classification, MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageNumber, Integer pageSize, List<String> propertyPaths) ;

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
     * TODO necessary?
     * @param pagesize max maximum number of returned taxa
     * @param page page to start, with 0 being first page
     * @return
     */
    public List<TaxonBase> getAllTaxonBases(Integer pagesize, Integer page);


    /**
     * @param limit
     * @param start
     * @return
     */
    public List<Taxon> getAllTaxa(Integer limit, Integer start);

    /**
     * @param limit
     * @param start
     * @return
     */
    public List<Synonym> getAllSynonyms(Integer limit, Integer start);

    public List<RelationshipBase> getAllRelationships(Integer limit, Integer start);

    public int countAllRelationships();

    /**
     * @param queryString
     * @param matchMode
     * @param onlyAcccepted
     * @return
     */
    public int countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted);

    /**
     * @param queryString
     * @param matchMode
     * @param onlyAcccepted
     * @param criteria
     * @return
     */
    public int countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted, List<Criterion> criteria);

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
    public int countTaxonRelationships(Taxon taxon, TaxonRelationshipType type,
            Direction direction);

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
            TaxonRelationshipType type, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths,
            Direction direction);

    /**
     * Returns a count of the SynonymRelationships (of where relationship.type == type,
     *  if this arguement is supplied) where the supplied taxon is relatedTo.
     *
     * @param taxon The taxon that is relatedTo
     * @param type The type of SynonymRelationship (can be null)
     * @return the number of SynonymRelationship instances
     */
    public int countSynonyms(Taxon taxon, SynonymRelationshipType type);

    /**
     * Returns the SynonymRelationships (of where relationship.type == type, if this arguement is supplied)
     * where the supplied taxon is relatedTo.
     *
     * @param taxon The taxon that is relatedTo
     * @param type The type of SynonymRelationship (can be null)
     * @param pageSize The maximum number of relationships returned (can be null for all relationships)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * * @param orderHints Properties to order by
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a List of SynonymRelationship instances
     */
    public List<SynonymRelationship> getSynonyms(Taxon taxon, SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Returns a count of the SynonymRelationships (of where relationship.type == type,
     *  if this arguement is supplied) where the supplied synonym is relatedFrom.
     *
     * @param taxon The synonym that is relatedFrom
     * @param type The type of SynonymRelationship (can be null)
     * @return the number of SynonymRelationship instances
     */
    public int countSynonyms(Synonym synonym, SynonymRelationshipType type);

    /**
     * Returns the SynonymRelationships (of where relationship.type == type, if this arguement is supplied)
     * where the supplied synonym is relatedFrom.
     *
     * @param taxon The synonym that is relatedFrom
     * @param type The type of SynonymRelationship (can be null)
     * @param pageSize The maximum number of relationships returned (can be null for all relationships)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * * @param orderHints Properties to order by
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a List of SynonymRelationship instances
     */
    public List<SynonymRelationship> getSynonyms(Synonym synoynm, SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);


    /**
     * Creates all inferred synonyms for the species in the tree and insert it to the database
     * @param tree
     * @return List of inferred synonyms
     */
    //public List<Synonym> insertAllInferredSynonymy(Classification tree);



    public List<TaxonNameBase> findIdenticalTaxonNames(List<String> propertyPath);
    public String getPhylumName(TaxonNameBase name);

    public long countTaxaByCommonName(String searchString,
            Classification classification, MatchMode matchMode,
            Set<NamedArea> namedAreas);

    /**
     * Deletes all synonym relationships of a given synonym.
     * If taxon is given only those relationships to the taxon
     * are deleted.
     * @param synonym the synonym
     * @param taxon the taxon, may be <code>null</code>
     * @return
     * @deprecated This method must no longer being used since the
     *             SynonymRelationship is annotated at the {@link Taxon} and at
     *             the {@link Synonym} with <code>orphanDelete=true</code>. Just
     *             remove the from and to entities from the relationship and
     *             hibernate will care for the deletion. Using this method can cause
     *             <code>StaleStateException</code> (see http://dev.e-taxonomy.eu/trac/ticket/3797)
     */
    @Deprecated
    public long deleteSynonymRelationships(Synonym syn, Taxon taxon);

    public List<UUID> findIdenticalTaxonNameIds(List<String> propertyPath);

    public List<TaxonNameBase> findIdenticalNamesNew(List <String> propertyPaths);


    public Integer countSynonymRelationships(TaxonBase taxonBase,
            SynonymRelationshipType type, Direction relatedfrom);

    public List<SynonymRelationship> getSynonymRelationships(TaxonBase taxonBase,
            SynonymRelationshipType type, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths,
            Direction relatedfrom);

    /**
     * @return
     */
    public List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCacheTaxon(Integer limit, String pattern);

    /**
     * @return
     */
    public List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCacheSynonym(Integer limit, String pattern);

    public List<UuidAndTitleCache<IdentifiableEntity>> getTaxaByNameForEditor(boolean doTaxa, boolean doSynonyms, boolean doNamesWithoutTaxa, boolean doMisappliedNames, String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas);

    public List<String> taxaByNameNotInDB(List<String> taxonNames);

    public List<Taxon> listAcceptedTaxaFor(Synonym synonym, Classification classificationFilter, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths);

    public long countAcceptedTaxaFor(Synonym synonym, Classification classificationFilter);

	public List<UuidAndTitleCache<IdentifiableEntity>> getTaxaByCommonNameForEditor(
			String titleSearchStringSqlized, Classification classification,
			MatchMode matchMode, Set namedAreas);

	public <S extends TaxonBase> List<Object[]> findByIdentifier(Class<S> clazz, String identifier,
			DefinedTerm identifierType, TaxonNode subtreeFilter, MatchMode matchmode,
			boolean includeEntity, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

	public <S extends TaxonBase> int countByIdentifier(Class<S> clazz,
			String identifier, DefinedTerm identifierType, TaxonNode subtreeFilter, MatchMode matchmode);

    /**
     * @param classification
     * @param excludeUuid
     * @param limit
     * @param pattern
     * @return
     */
	public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(
            Classification classification, Integer limit, String pattern);

}
