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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.config.IFindTaxaAndNamesConfigurator;
import eu.etaxonomy.cdm.api.service.config.IncludedTaxonConfiguration;
import eu.etaxonomy.cdm.api.service.config.MatchingTaxonConfigurator;
import eu.etaxonomy.cdm.api.service.config.SynonymDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.dto.IdentifiedEntityDTO;
import eu.etaxonomy.cdm.api.service.dto.IncludedTaxaDTO;
import eu.etaxonomy.cdm.api.service.dto.MarkedEntityDTO;
import eu.etaxonomy.cdm.api.service.exception.DataChangeNoRollbackException;
import eu.etaxonomy.cdm.api.service.exception.HomotypicalGroupChangeException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.search.LuceneMultiSearchException;
import eu.etaxonomy.cdm.api.service.search.LuceneParseException;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.exception.UnpublishedException;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
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
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.TaxonTitleType;


public interface ITaxonService
             extends IIdentifiableEntityService<TaxonBase>, IPublishableService<TaxonBase>{

    /**
     * {@inheritDoc}
     * <BR><BR>
     * NOTE: Also taxa with <code>publish=false</code> are returned.
     */
    @Override
    public TaxonBase load(UUID uuid, List<String> propertyPaths);

    /**
     * Returns a list of taxa that matches the name string and the sec reference
     * @param name the name string to search for
     * @param sec the taxons sec reference
     * @return a list of taxa matching the name and the sec reference
     */
    public List<TaxonBase> searchByName(String name, boolean includeUnpublished, Reference sec);

    /**
     * Swaps given synonym and accepted taxon.
     * In particular:
     * <ul>
     * 		<li>A new accepted taxon with the synonyms name is created</li>
     * 		<li>The synonym is deleted from the old accepted taxons synonym list</li>
     * 		<li>A new synonym with the name of the old accepted taxon is created</li>
     * 		<li>The newly created synonym get related to the newly created accepted taxon</li>
     * </ul>
     *
     * @param synonym
     * @param acceptedTaxon
     * @return
     */
    public UpdateResult swapSynonymAndAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon);

    /**
     * Changes a synonym into an accepted taxon and removes
     * the synonym relationship to the given accepted taxon.
     * Other synonyms homotypic to the synonym to change are
     * moved to the same new accepted taxon as homotypic
     * synonyms. The new accepted taxon has the same name and
     * the same sec reference as the old synonym.<BR>
     * If the given accepted taxon and the synonym are homotypic
     * to each other an exception may be thrown as taxonomically it doesn't
     * make sense to have two accepted taxa in the same homotypic group
     * but also it is than difficult to decide how to handle other names
     * in the homotypic group. It is up to the implementing class to
     * handle this situation via an exception or in another way.
     *
     *
     * @param synonym
     * 				the synonym to change into an accepted taxon
     * @param acceptedTaxon
     * 				an accepted taxon, the synonym had a relationship to
     * @param deleteSynonym
     * 			if true the method tries to delete the old synonym from the database
     * @return
     * 			the newly created accepted taxon
     * @throws IllegalArgumentException
     * 			if the given accepted taxon and the synonym are homotypic
     * 		    to each other an exception may be thrown as taxonomically it doesn't
     * 			make sense to have two accepted taxa in the same homotypic group
     *          but also it is than difficult to decide how to handle other names
     *          in the homotypic group. It is up to the implementing class to
     *          handle this situation via an exception or in another way.
     */
    public UpdateResult changeSynonymToAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon, boolean deleteSynonym) throws HomotypicalGroupChangeException;

    /**
     * @param synonymUuid
     * @param acceptedTaxonUuid
     * @param newParentNodeUuid
     * @param deleteSynonym
     * @return
     * @throws HomotypicalGroupChangeException
     */
    public UpdateResult changeSynonymToAcceptedTaxon(UUID synonymUuid, UUID acceptedTaxonUuid, UUID newParentNodeUuid,
            boolean deleteSynonym)
            throws HomotypicalGroupChangeException;

    /**
     * Change a synonym into a related concept
     *
     * @param synonym
     * 				the synonym to change into the concept taxon
     * @param toTaxon
     * 				the taxon the newly created concept should be related to
     * @param taxonRelationshipType
     * 				the type of relationship
     * @param reference
     * @param microReference
     * @return
     * 				update result with the newly created concept
     */
    public UpdateResult changeSynonymToRelatedTaxon(Synonym synonym, Taxon toTaxon, TaxonRelationshipType taxonRelationshipType, Reference reference, String microReference);

    /**
     *
     * Change a related concept into synonym
     *
     * @param synonym
     * 				the concept taxon to change into a synonym
     * @param toTaxon
     * 				the taxon the newly created synonym should be related to
     * @param oldRelationshipType
     *              the type of old concept relationship
     * @param synonymRelationshipType
     * 				the type of new synonym relationship
     *
     * @return
     * 				update result with the newly created synonym
     * @throws DataChangeNoRollbackException
     */
  public 	UpdateResult changeRelatedTaxonToSynonym(Taxon fromTaxon, Taxon toTaxon,
             TaxonRelationshipType oldRelationshipType,
            SynonymType synonymType) throws DataChangeNoRollbackException;

    /**
     * Changes the homotypic group of a synonym into the new homotypic group.
     * All relations to taxa are updated correctly depending on the homotypic
     * group of the accepted taxon. <BR>
     * All existing basionym relationships to and from this name are removed.<BR>
     * If the parameter <code>targetTaxon</code> is defined, the synonym is
     * added to this taxon irrespctive of if it has been related to this
     * taxon before.<BR>
     * If <code>setBasionymRelationIfApplicable</code> is true a basionym relationship
     * between the existing basionym(s) of the new homotypic group and the synonyms name
     * is added.<BR>
     *
     * @param synonym
     * @param newHomotypicalGroup
     * @param taxon
     * @param setBasionymRelationIfApplicable
     */
    public void changeHomotypicalGroupOfSynonym(Synonym synonym, HomotypicalGroup newHomotypicalGroup,
            Taxon targetTaxon, boolean setBasionymRelationIfApplicable);

    /**
     * See {@link #moveSynonymToAnotherTaxon(Synonym, Taxon, boolean, SynonymType, Reference, String, boolean)}
     * @param oldSynonym
     * @param newTaxon
     * @param moveHomotypicGroup
     * @param newSynonymType
     * @return
     * @throws HomotypicalGroupChangeException
     */
    public UpdateResult moveSynonymToAnotherTaxon(Synonym oldSynonym, Taxon newTaxon, boolean moveHomotypicGroup,
            SynonymType newSynonymType) throws HomotypicalGroupChangeException;


    /**
     * Moves a synonym to another taxon and removes the old synonym relationship.
     *
     * @param oldSynonym the old synonym to move.
     * @param newTaxon the taxon the synonym will be moved to
     * @param moveHomotypicGroup if the synonym belongs to a homotypic group with other synonyms and
     * 		<code>moveHomotypicGroup</code> is <code>true</code> all these synonyms are moved to the new taxon,
     * 		if <code>false</code> a {@link HomotypicalGroupChangeException} is thrown.
     * 		<code>moveHomotypicGroup</code> has no effect if the synonym is the only synonym in it's homotypic group.
     * @param newSynonymType the synonym type of the new synonyms. Default is
     * 		{@link SynonymType#HETEROTYPIC_SYNONYM_OF() heterotypic}.
     * @param newSecundum The secundum for the new synonyms).
     * @param newSecundumDetail The secundum micro reference for the new synonym(s).
     * @param keepSecundumIfUndefined if no <code>newSecundum</code> and/or no <code>newSecundumDetail</code>
     * 		is defined they are taken from the old synonym(s) if <code>keepSecundumIfUndefined</code> is
     * 		<code>true</code>. If <code>false</code> the secundum and the secundum detail will be taken
     * 		only from the <code>newSecundum</code> and <code>newSecundumDetail</code> even if they are
     *      undefined (<code>null</code>).
     * @return The new synonym relationship. If <code>moveHomotypicGroup</code> is <code>true</code> additionally
     * 		created new synonym relationships must be retrieved separately from the new taxon.
     * @throws HomotypicalGroupChangeException Exception is thrown if (1) synonym is homotypic to the old accepted taxon or
     * 		(2) synonym is in homotypic group with other synonyms and <code>moveHomotypicGroup</code> is false
     */
    public UpdateResult moveSynonymToAnotherTaxon(Synonym oldSynonym, Taxon newTaxon, boolean moveHomotypicGroup,
            SynonymType newSynonymType, Reference newSecundum,
            String newSecundumDetail, boolean keepSecundumIfUndefined) throws HomotypicalGroupChangeException;


    /**
     * @param oldSynonym
     * @param newTaxonUUID
     * @param moveHomotypicGroup
     * @param newSynonymType
     * @param reference
     * @param referenceDetail
     * @param keepReference
     * @return
     * @throws HomotypicalGroupChangeException
     *
     * @see {@link #moveSynonymToAnotherTaxon(Synonym, Taxon, boolean, SynonymType, Reference, String, boolean)}
     */
    public UpdateResult moveSynonymToAnotherTaxon(Synonym oldSynonym,
            UUID newTaxonUUID, boolean moveHomotypicGroup,
            SynonymType newSynonymType,
            Reference newSecundum, String newSecundumDetail, boolean keepSecundumIfUndefined)
            throws HomotypicalGroupChangeException;

    /**
     * Returns the TaxonRelationships (of where relationship.type == type, if this argument is supplied)
     * where the supplied taxon is relatedTo.
     *
     * @param taxon The taxon that is relatedTo
     * @param type The type of TaxonRelationship (can be null)
     * @param includeUnpublished should unpublished related taxa also be returned?
     * @param pageSize The maximum number of relationships returned (can be null for all relationships)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param orderHints Properties to order by
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a List of TaxonRelationship instances
     */
    public List<TaxonRelationship> listToTaxonRelationships(Taxon taxon, TaxonRelationshipType type,
            boolean includeUnpublished, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);


    /**
     * Returns the TaxonRelationships (of where relationship.type == type, if this arguement is supplied)
     * where the supplied taxon is relatedTo.
     *
     * @param taxon The taxon that is relatedTo
     * @param type The type of TaxonRelationship (can be null)
     * @param includeUnpublished should unpublished related taxa also be returned?
     * @param pageSize The maximum number of relationships returned (can be null for all relationships)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param orderHints Properties to order by
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager of TaxonRelationship instances
     */
    public Pager<TaxonRelationship> pageToTaxonRelationships(Taxon taxon, TaxonRelationshipType type,
            boolean includeUnpublished, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Returns the TaxonRelationships (of where relationship.type == type, if this argument is supplied)
     * where the supplied taxon is relatedFrom.
     *
     * @param taxon The taxon that is relatedFrom
     * @param type The type of TaxonRelationship (can be null)
     * @param includeUnpublished should unpublished related taxa also be returned?
     * @param pageSize The maximum number of relationships returned (can be null for all relationships)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param orderHints Properties to order by
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a List of TaxonRelationship instances
     */
    public List<TaxonRelationship> listFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type,
            boolean includeUnpublished, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);


    /**
     * Returns the TaxonRelationships (of where relationship.type == type, if this argument is supplied)
     * where the supplied taxon is relatedFrom.
     *
     * @param taxon The taxon that is relatedFrom
     * @param type The type of TaxonRelationship (can be null)
     * @param includeUnpublished should unpublished related taxa also be returned?
     * @param pageSize The maximum number of relationships returned (can be null for all relationships)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param orderHints Properties to order by
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager of TaxonRelationship instances
     */
    public Pager<TaxonRelationship> pageFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type,
            boolean includeUnpublished, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * lists all taxa which are related to the <code>taxon</code> given as
     * parameter.
     *
     * @param taxon
     * @param includeRelationships
     * @param maxDepth
     *            <code>1</code> for one level, <code>null</code> for infinite
     *            depth, <code>0</code> will completely omit collecting related taxa.
     * @param limit
     * @param start
     * @param propertyPaths
     * @return
     */
    public Set<Taxon> listRelatedTaxa(Taxon taxon, Set<TaxonRelationshipEdge> includeRelationships, Integer maxDepth,
            boolean includeUnpublished, Integer limit, Integer start, List<String> propertyPaths);


    /**
     * Returns all or a page of all taxon concept relationships in the database.
     * The result can be filtered by relationship types.
     *
     * @param types The taxon relationship type filter, if <code>null</code> no filter is set, if empty the result will also be empty
     * @param pageSize the page size
     * @param pageStart the number of the start page
     * @param orderHints the order hints
     * @param propertyPaths the property path to initialize the resulting objects
     * @return list of taxon relationships matching the filter criteria
     */
    public List<TaxonRelationship> listTaxonRelationships(Set<TaxonRelationshipType> types,
            Integer pageSize, Integer pageStart, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Lists all classifications the given taxon/synonym is used in{@link Synonym}
     *
     * @param taxonBase
     * @param limit
     * @param start
     * @param propertyPaths
     * @return
     */
    public List<Classification> listClassifications(TaxonBase taxonBase, Integer limit, Integer start, List<String> propertyPaths);

    /**
     * Returns the Synonyms (with the given synonym relationship type, if this argument is supplied)
     * that do have the supplied taxon as accepted taxon.
     *
     * @param taxon The accepted taxon
     * @param type The type of Synonym (can be null)
     * @param pageSize The maximum number of synonyms returned (can be null for returning synonyms)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * * @param orderHints Properties to order by
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager of {@link Synonym} instances
     */
    public Pager<Synonym> getSynonyms(Taxon taxon, SynonymType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * This method returns in the first entry the list of synonyms of the
     * homotypic group of the accepted taxon. All other entries represent the lists of heterotypic
     * synonym groups. For detailed information about these 2 groups see
     * {@link #getHomotypicSynonymsByHomotypicGroup(Taxon, List)} and
     * {@link #getHeterotypicSynonymyGroups(Taxon, List)}
     *
     * @see			#getSynonyms()
     * @see			SynonymType#HETEROTYPIC_SYNONYM_OF()
     * @see			eu.etaxonomy.cdm.model.name.HomotypicalGroup

     * @param taxon the accepted taxon
     * @param propertyPaths the property path
     * @return the list of groups of synonyms
     */
    public List<List<Synonym>> getSynonymsByHomotypicGroup(Taxon taxon, List<String> propertyPaths);


    /**
     * Returns the list of all synonyms that share the same homotypical group with the given taxon.
     * Only those homotypic synonyms are returned that do have a synonym relationship with the accepted taxon.
     * @param taxon
     * @param propertyPaths
     * @return
     */
    public List<Synonym> getHomotypicSynonymsByHomotypicGroup(Taxon taxon, List<String> propertyPaths);

    /**
     * Returns the ordered list of all {@link eu.etaxonomy.cdm.model.name.HomotypicalGroup homotypical groups}
     * that contain {@link Synonym synonyms} that are heterotypic to the given taxon.
     * {@link eu.etaxonomy.cdm.model.name.TaxonName Taxon names} of heterotypic synonyms
     * belong to a homotypical group which cannot be the homotypical group to which the
     * taxon name of the given taxon belongs. This method does not return the homotypic group the given
     * taxon belongs to.<BR>
     * This method does neglect the type of synonym relationship that is defined between the given taxon
     * and the synonym. So the synonym relationship may be homotypic however a synonym is returned
     * in one of the result lists as long as the synonym does not belong to the same homotypic group as
     * the given taxon.<BR>
     * The list returned is ordered according to the date of publication of the
     * first published name within each homotypical group.
     *
     * @see			#getHeterotypicSynonymyGroups()
     * @see			#getSynonyms()
     * @see			SynonymType#HETEROTYPIC_SYNONYM_OF()
     * @see			eu.etaxonomy.cdm.model.name.HomotypicalGroup

     * @param taxon
     * @param propertyPaths
     * @return
     */
    public List<List<Synonym>> getHeterotypicSynonymyGroups(Taxon taxon, List<String> propertyPaths);

    /**
     * Returns a Paged List of TaxonBase instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
     *
     * @param clazz filter the results by class (or pass null to return all TaxonBase instances)
     * @param queryString
     * @param pageSize The maximum number of taxa returned (can be null for all matching taxa)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths properties to be initialized
     * @return a Pager Taxon instances
     * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
     */
    @Override
    public Pager<TaxonBase> search(Class<? extends TaxonBase> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Returns a list of TaxonBase instances where the
     * taxon.name properties match the parameters passed. In order to search for any string value, pass '*', passing the string value of
     * <i>null</i> will search for those taxa with a value of null in that field
     *
     * @param clazz optionally filter by class (can be null to return all taxa)
     * @param uninomial
     * @param infragenericEpithet
     * @param specificEpithet
     * @param infraspecificEpithet
     * @param rank
     * @param pageSize The maximum number of taxa returned (can be null for all matching taxa)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a list of TaxonBase instances
     */
    public Pager<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String uninomial, String infragenericEpithet, String specificEpithet, String infraspecificEpithet, String authorship, Rank rank, Integer pageSize, Integer pageNumber);

    /**
     * Returns a list of TaxonBase instances where the
     * taxon.name properties match the parameters passed. In order to search for any string value, pass '*', passing the string value of
     * <i>null</i> will search for those taxa with a value of null in that field
     *
     * @param clazz optionally filter by class
     * @param uninomial
     * @param infragenericEpithet
     * @param specificEpithet
     * @param infraspecificEpithet
     * @param rank
     * @param pageSize The maximum number of taxa returned (can be null for all matching taxa)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a List of TaxonBase instances
     */
    public List<TaxonBase> listTaxaByName(Class<? extends TaxonBase> clazz, String uninomial, String infragenericEpithet, String specificEpithet, String infraspecificEpithet, String authorship, Rank rank, Integer pageSize, Integer pageNumber);

    /**
     * Returns a list of IdentifiableEntity instances (in particular, TaxonName and TaxonBase instances)
     * that match the properties specified in the configurator.
     * @param configurator
     * @return
     */
    public Pager<IdentifiableEntity> findTaxaAndNames(IFindTaxaAndNamesConfigurator configurator);

    /**
     * performes a union searches for TaxonBase instances on all available
     * free text indexes. At the time of writing this documentation it combines
     * {@link #findByDescriptionElementFullText(Class, String, Classification, List, List, boolean, Integer, Integer, List, List)}
     * and {@link #findByFullText(Class, String, Classification, List, boolean, Integer, Integer, List, List)
     *
     * @param queryString
     *            the query string
     * @param classification
     *            Additional filter criterion: If a taxonomic classification
     *            three is specified here the result set will only contain taxa
     *            of the given classification
     * @param languages
     *            Additional filter criterion: Search only in these languages.
     *            Not all text fields in the cdm model are multilingual, thus
     *            this setting will only apply to the multilingiual fields.
     *            Other fields are searched nevertheless if this parameter is
     *            set or not.
     * @param highlightFragments
     *            TODO
     * @param pageSize
     *            The maximum number of objects returned (can be null for all
     *            objects)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths
     *            properties to initialize - see
     *            {@link IBeanInitializer#initialize(Object, List)}
     * @return a paged list of instances of type T matching the queryString and
     *         the additional filter criteria
     * @return
     * @throws LuceneCorruptIndexException
     * @throws IOException
     * @throws LuceneParseException
     * @throws LuceneMultiSearchException
     * @deprecated this search should fully be covered by the new method
     *      {@link #findTaxaAndNamesByFullText(EnumSet, String, Classification, Set, List, boolean, Integer, Integer, List, List)}
     *      , maybe we should rename this latter method to give it a more meaningful name
     */
    @Deprecated
    public Pager<SearchResult<TaxonBase>> findByEverythingFullText(String queryString,
            Classification classification, boolean includeUnpublished, List<Language> languages, boolean highlightFragments,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws IOException, LuceneParseException, LuceneMultiSearchException;

    /**
     * Searches for TaxonBase instances using the TaxonBase free text index.
     *
     * <h4>This is an experimental feature, it may be moved, modified, or even
     * removed in future releases!!!</h4>
     *
     * @param clazz
     *            Additional filter criterion: The specific TaxonBase subclass
     *            to search for
     * @param queryString
     *            the query string
     * @param classification
     *            Additional filter criterion: If a taxonomic classification
     *            three is specified here the result set will only contain taxa
     *            of the given classification
     * @param languages
     *            Additional filter criterion: Search only in these languages.
     *            Not all text fields in the cdm model are multilingual, thus
     *            this setting will only apply to the multilingiual fields.
     *            Other fields are searched nevertheless if this parameter is
     *            set or not.
     * @param highlightFragments
     *            TODO
     * @param pageSize
     *            The maximum number of objects returned (can be null for all
     *            objects)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths
     *            properties to initialize - see
     *            {@link IBeanInitializer#initialize(Object, List)}
     * @return a paged list of instances of type T matching the queryString and
     *         the additional filter criteria
     * @throws LuceneCorruptIndexException
     * @throws IOException
     * @throws LuceneParseException
     */
    public Pager<SearchResult<TaxonBase>> findByFullText(Class<? extends TaxonBase> clazz, String queryString,
            Classification classification, boolean includeUnpublished, List<Language> languages,
            boolean highlightFragments, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) throws IOException, LuceneParseException;


    /**
     * @param areaFilter
     * @param statusFilter
     * @param classification
     *            Additional filter criterion: If a taxonomic classification
     *            three is specified here the result set will only contain taxa
     *            of the given classification
     * @param highlightFragments
     * @param pageSize
     *            The maximum number of objects returned (can be null for all
     *            objects)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPath
     *            Common properties to initialize the instances of the
     *            CDM types ({@link Taxon} and {@link Synonym}
     *            this method can return - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a paged list of instances of {@link Taxon} instances
     * @throws IOException
     * @throws LuceneParseException
     */
    public Pager<SearchResult<TaxonBase>> findByDistribution(List<NamedArea> areaFilter, List<PresenceAbsenceTerm> statusFilter,
            Classification classification,
            Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths) throws IOException, LuceneParseException;

    /**
     * Searches for TaxonBase instances using the TaxonBase free text index.
     *
     *
     *
     * <h4>This is an experimental feature, it may be moved, modified, or even
     * removed in future releases!!!</h4>
     * @param searchModes
     *            Additional filter criterion: defaults to [doTaxa] if set null
     * @param queryString
     *            the query string
     * @param classification
     *            Additional filter criterion: If a taxonomic classification
     *            three is specified here the result set will only contain taxa
     *            of the given classification
     * @param namedAreas
     * @param languages
     *            Additional filter criterion: Search only in these languages.
     *            Not all text fields in the cdm model are multilingual, thus
     *            this setting will only apply to the multilingiual fields.
     *            Other fields are searched nevertheless if this parameter is
     *            set or not.
     * @param highlightFragments
     *            TODO
     * @param pageSize
     *            The maximum number of objects returned (can be null for all
     *            objects)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPath
     *            Common properties to initialize the instances of the
     *            CDM types ({@link Taxon} and {@link Synonym}
     *            this method can return - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a paged list of instances of {@link Taxon}, {@link Synonym}, matching the queryString and
     *         the additional filter criteria
     * @throws LuceneCorruptIndexException
     * @throws IOException
     * @throws LuceneParseException
     * @throws LuceneMultiSearchException
     */
    public Pager<SearchResult<TaxonBase>> findTaxaAndNamesByFullText(
            EnumSet<TaxaAndNamesSearchMode> searchModes,
            String queryString, Classification classification, Set<NamedArea> namedAreas, Set<PresenceAbsenceTerm> distributionStatus,
            List<Language> languages, boolean highlightFragments, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) throws IOException, LuceneParseException, LuceneMultiSearchException;

    /**
     * Searches for TaxonBase instances by using the DescriptionElement free text index.
     *
     * <h4>This is an experimental feature, it may be moved, modified, or even
     * removed in future releases!!!</h4>
     *
     * @param clazz
     *            Additional filter criterion:
     * @param queryString
     *            the query string to filter by
     * @param classification
     *            Additional filter criterion: If a taxonomic classification
     *            three is specified here the result set will only contain taxa
     *            of the given classification
     * @param features
     *            TODO
     * @param languages
     *            Additional filter criterion: Search only in these languages.
     *            Not all text fields in the cdm model are multilingual, thus
     *            this setting will only apply to the multilingiual fields.
     *            Other fields are searched nevertheless if this parameter is
     *            set or not.
     * @param highlightFragments
     *            TODO
     * @param pageSize
     *            The maximum number of objects returned (can be null for all
     *            objects)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths
     *            properties to initialize - see
     *            {@link IBeanInitializer#initialize(Object, List)}
     * @return a paged list of instances of type T matching the queryString and
     *         the additional filter criteria
     * @throws IOException
     * @throws LuceneCorruptIndexException
     * @throws LuceneParseException
     */
    public Pager<SearchResult<TaxonBase>> findByDescriptionElementFullText(Class<? extends DescriptionElementBase> clazz, String queryString, Classification classification, List<Feature> features, List<Language> languages, boolean highlightFragments, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws IOException, LuceneParseException;


    /**
     *
     * @param taxon
     * @param size
     * @param height
     * @param widthOrDuration
     * @param mimeTypes
     * @return
     *
     * @deprecated use {@link #listMedia(Taxon, Set, boolean, boolean, List)} instead
     */
    @Deprecated
    public List<MediaRepresentation> getAllMedia(Taxon taxon, int size, int height, int widthOrDuration, String[] mimeTypes);


    /**
     * Lists all Media found in an any TaxonDescription associated with this
     * taxon.
     *
     * @param taxon
     * @param includeRelationships
     *            the given list of TaxonRelationshipEdges will be taken into
     *            account when retrieving media associated with the given taxon.
     *            Can be NULL.
     * @param limitToGalleries
     *            whether to take only TaxonDescription into account which are
     *            marked as gallery
     * @return
     * @deprecated use {@link #listMedia(Taxon, Set, boolean, boolean, List)} instead
     */
    @Deprecated
    public List<Media> listTaxonDescriptionMedia(Taxon taxon, Set<TaxonRelationshipEdge> includeRelationships, boolean limitToGalleries, List<String> propertyPath);

    /**
     * Lists all Media found in an any TaxonDescription, NameDescription,
     * SpecimenOrObservationBase, DnaSample Chromatograms, etc. associated with this taxon.
     *
     * @param taxon
     * @param includeRelationships
     *            the given list of TaxonRelationshipEdges will be taken into
     *            account when retrieving media associated with the given taxon.
     *            Can be NULL.
     * @param limitToGalleries
     *            whether to take only descriptions into account which are
     *            marked as gallery, can be NULL
     * @param includeTaxonDescriptions
     *            whether to take TaxonDescriptions into account, can be NULL
     * @param includeOccurrences
     *          whether to take TaxonDescriptions into account, can be NULL
     * @param includeTaxonNameDescriptions
     *       whether to take TaxonNameDescriptions into account, can be NULL
     * @param propertyPath
     * @return
     */
    public List<Media> listMedia(Taxon taxon, Set<TaxonRelationshipEdge> includeRelationships,
            Boolean limitToGalleries, Boolean includeTaxonDescriptions, Boolean includeOccurrences,
            Boolean includeTaxonNameDescriptions, List<String> propertyPath);

    public List<TaxonBase> findTaxaByID(Set<Integer> listOfIDs);

    /**
     * Returns the TaxonBase with the given UUID
     * using the given match mode and initialization strategy
     *
     * @param uuid
     * @param propertyPaths
     * @return
     */
    public TaxonBase findTaxonByUuid(UUID uuid, List<String> propertyPaths);

    /**
     * Counts the number of synonyms
     * @param onlyAttachedToTaxon if <code>true</code> only those synonyms being attached to
     * an accepted taxon are counted
     * @return the number of synonyms
     */
    public long countSynonyms(boolean onlyAttachedToTaxon);

    public List<TaxonName> findIdenticalTaxonNames(List<String> propertyPath);

    public List<TaxonName> findIdenticalTaxonNameIds(List<String> propertyPath);
//
//    public String getPhylumName(TaxonName name);

    /**
     * Returns all {@link Taxon taxa} which are {@link TaxonRelationshipType#CONGRUENT_TO() congruent} or
     * {@link TaxonRelationshipType#INCLUDES() included} in the taxon represented by the given taxon uuid.
     * The result also returns the path to these taxa represented by the uuids of
     * the {@link TaxonRelationshipType taxon relationships types} and doubtful information.
     * If classificationUuids is set only taxa of classifications are returned which are included
     * in the given {@link Classification classifications}. ALso the path to these taxa may not include
     * taxa from other classifications.
     * @param taxonUuid uuid of the original taxon
     * @param classificationUuids List of uuids of classifications used as a filter
     * @param includeDoubtful set to <code>true</code> if also doubtfully included taxa should be included in the result
     * @return a DTO which includes a list of taxa with the pathes from the original taxon to the given taxon as well
     * as doubtful and date information. The original taxon is included in the result.
     */
    public IncludedTaxaDTO listIncludedTaxa(UUID taxonUuid, IncludedTaxonConfiguration configuration);


   /**
     * Removes a synonym.<BR><BR>
     *
     * In detail it removes
     *  <li>the synonym concept</li>
     *  <BR><BR>
     *  If <code>removeNameIfPossible</code> is true
     *  it also removes the synonym name if it is not used in any other context
     *  (part of a concept, in DescriptionElementSource, part of a name relationship, used inline, ...)<BR><BR>
     *  If <code>newHomotypicGroupIfNeeded</code> is <code>true</code> and the synonym name
     *  is not deleted and the name is homotypic to the taxon
     *  the name is moved to a new homotypic group.<BR><BR>
     *
     *  If synonym is <code>null</code> the method has no effect.
     *
     * @param taxon
     * @param synonym
     * @param removeNameIfPossible
     * @return deleteResult
     *
     */
    public DeleteResult deleteSynonym(Synonym synonym, SynonymDeletionConfigurator config);

    /**
     * Removes a synonym.
     *
     * The method essentially loads the synonym and calls the
     * {@link #deleteSynonym(Synonym, SynonymDeletionConfigurator) deleteSynonym} method
     *
     * @param synonymUuid
     * @param config
     * @return
     */
    public DeleteResult deleteSynonym(UUID synonymUuid, SynonymDeletionConfigurator config);

    /**
     * @param tnb
     * @return
     */
    public Taxon findBestMatchingTaxon(String taxonName);

    public Taxon findBestMatchingTaxon(MatchingTaxonConfigurator config);

    public Synonym findBestMatchingSynonym(String taxonName, boolean includeUnpublished);

     public List<UuidAndTitleCache<? extends IdentifiableEntity>> findTaxaAndNamesForEditor(IFindTaxaAndNamesConfigurator configurator);

    /**
     * Creates the specified inferred synonyms for the taxon in the classification, but do not insert it to the database
     * @param taxon
     * @param tree
     * @return list of inferred synonyms
     */
    public List<Synonym> createInferredSynonyms(Taxon taxon, Classification tree, SynonymType type, boolean doWithMisappliedNames);

    /**
     * Creates all inferred synonyms for the taxon in the classification, but do not insert it to the database
     * @param taxon
     * @param tree
     * @param iDatabase
     * @return list of inferred synonyms
     */
    public List<Synonym>  createAllInferredSynonyms(Taxon taxon, Classification tree, boolean doWithMisappliedNames);

    public Taxon findAcceptedTaxonFor(UUID synonymUuid, UUID classificationUuid, boolean includeUnpublished,
            List<String> propertyPaths) throws UnpublishedException;

    public List<TaxonBase> findTaxaByName(MatchingTaxonConfigurator config);


    /**
     * @param clazz the optional {@link TaxonBase} subclass
     * @param identifier the identifier string
     * @param identifierType the identifier type
     * @param subtreeFilter filter on a classification subtree (TaxonNode)
     * @param matchmode the match mode for the identifier string
     * @param includeEntity should the taxon as an object be included in the result
     * @param pageSize page size
     * @param pageNumber page number
     * @param propertyPaths property path for initializing the returned taxon object (requires includeEntity=true)
     * @return the resulting {@link IdentifiedEntityDTO} pager
     * @see IIdentifiableEntityService#findByIdentifier(Class, String, DefinedTerm, MatchMode, boolean, Integer, Integer, List)
     */
    public <S extends TaxonBase> Pager<IdentifiedEntityDTO<S>> findByIdentifier(
			Class<S> clazz, String identifier, DefinedTerm identifierType, TaxonNode subtreeFilter,
			MatchMode matchmode, boolean includeEntity, Integer pageSize,
			Integer pageNumber,	List<String> propertyPaths);

    /**
     * Returns a pager for {@link MarkedEntityDTO DTOs} that hold the marker including type, title and uuid
     * and the according {@link TaxonBase} information (uuid, title and the taxon object itself (optional)).
     *
     * @param clazz The optional {@link TaxonBase} subclass
     * @param markerType the obligatory marker type, if not given, the results will always be empty
     * @param markerValue the optional
     * @param subtreeFilter filter on a classification subtree (TaxonNode)
     * @param includeEntity should the taxon as an object be included in the result
     * @param titleType which label to give the returned entity, taxon.titleCache, name.titleCache or name.nameCache
     * @param pageSize page size
     * @param pageNumber page number
     * @param propertyPaths property path for initializing the returned taxon object (requires includeEntity=true)
     * @return the resulting {@link MarkedEntityDTO} pager
     * @see IIdentifiableEntityService#findByMarker(Class, MarkerType, Boolean, boolean, Integer, Integer, List)
     */
    public <S extends TaxonBase> Pager<MarkedEntityDTO<S>> findByMarker(
            Class<S> clazz, MarkerType markerType, Boolean markerValue,
            TaxonNode subtreeFilter, boolean includeEntity, TaxonTitleType titleType,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * @param synonymUUid
     * @param acceptedTaxonUuid
     * @return
     */
    public UpdateResult swapSynonymAndAcceptedTaxon(UUID synonymUUid, UUID acceptedTaxonUuid);

    /**
     * @param taxonUuid
     * @param config
     * @param classificationUuid
     * @return
     */
    public DeleteResult deleteTaxon(UUID taxonUuid, TaxonDeletionConfigurator config, UUID classificationUuid);


	public UpdateResult moveFactualDateToAnotherTaxon(UUID fromTaxonUuid,
			UUID toTaxonUuid);


    /**
     * @param synonymUuid
     * @param toTaxonUuid
     * @param taxonRelationshipType
     * @param citation
     * @param microcitation
     * @return
     */
    public UpdateResult changeSynonymToRelatedTaxon(UUID synonymUuid, UUID toTaxonUuid, TaxonRelationshipType taxonRelationshipType,
            Reference citation, String microcitation);

    /**
     * @param fromTaxonUuid
     * @param toTaxonUuid
     * @param oldRelationshipType
     * @param synonymType
     * @return
     * @throws DataChangeNoRollbackException
     */
    public UpdateResult changeRelatedTaxonToSynonym(UUID fromTaxonUuid, UUID toTaxonUuid,
            TaxonRelationshipType oldRelationshipType, SynonymType synonymType) throws DataChangeNoRollbackException;





}
