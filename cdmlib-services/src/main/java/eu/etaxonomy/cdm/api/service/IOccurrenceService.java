/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.index.CorruptIndexException;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.FindOccurrencesConfigurator;
import eu.etaxonomy.cdm.api.service.config.IIdentifiableEntityServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.SpecimenDeleteConfigurator;
import eu.etaxonomy.cdm.api.service.dto.DerivedUnitDTO;
import eu.etaxonomy.cdm.api.service.dto.FieldUnitDTO;
import eu.etaxonomy.cdm.api.service.dto.MediaDTO;
import eu.etaxonomy.cdm.api.service.dto.RectangleDTO;
import eu.etaxonomy.cdm.api.service.dto.SpecimenOrObservationBaseDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.search.LuceneParseException;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.api.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @since 01.09.2008
 */
public interface IOccurrenceService
        extends IIdentifiableEntityService<SpecimenOrObservationBase> {

    /**
     * Returns a paged list of occurrences that have been determined to belong
     * to the taxon concept determinedAs, optionally restricted to objects
     * belonging to a class that extends SpecimenOrObservationBase. This
     * will also consider specimens that are determined as a taxon concept
     * belonging to the synonymy of the given taxon concept.
     * <p>
     * In contrast to {@link #listByAnyAssociation(Class, Taxon, List)} this
     * method only takes SpecimenOrObservationBase instances into account which
     * are actually determined as the taxon specified by
     * <code>determinedAs</code>.
     *
     * @param type
     *            The type of entities to return (can be null to count all
     *            entities of type <T>)
     * @param determinedAs
     *            the taxon concept that the occurrences have been determined to
     *            belong to
     * @param pageSize
     *            The maximum number of objects returned (can be null for all
     *            matching objects)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based, can be null, equivalent of starting at the
     *            beginning of the recordset)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths
     *            properties to be initialized
     * @return
     */
    public Pager<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> type, TaxonBase determinedAs, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Returns a paged list of occurrences that have been determined to belong
     * to the taxon name determinedAs, optionally restricted to objects
     * belonging to a class that that extends SpecimenOrObservationBase.
     * <p>
     * In contrast to {@link #listByAnyAssociation(Class, Taxon, List)} this
     * method only takes SpecimenOrObservationBase instances into account which
     * are actually determined as the taxon specified by
     * <code>determinedAs</code>.
     *
     * @param type
     *            The type of entities to return (can be null to count all
     *            entities of type <T>)
     * @param determinedAs
     *            the taxon name that the occurrences have been determined to
     *            belong to
     * @param pageSize
     *            The maximum number of objects returned (can be null for all
     *            matching objects)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based, can be null, equivalent of starting at the
     *            beginning of the recordset)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths
     *            properties to be initialized
     * @return
     */
    public Pager<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> type, TaxonName determinedAs, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Returns a List of Media that are associated with a given occurrence
     *
     * @param occurrence the occurrence associated with these media
     * @param pageSize The maximum number of media returned (can be null for all related media)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager of media instances
     */
    //TODO needed?
    public Pager<Media> getMedia(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns all media attached to this occurence and its children. Also takes
     * {@link MediaSpecimen} and molecular images into account.
     *
     * @param occurence the occurence and its children from which the media to get
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager of media instances
     */
    public Pager<Media> getMediaInHierarchy(SpecimenOrObservationBase<?> rootOccurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns a count of determinations that have been made for a given occurence and for a given taxon concept
     *
     * @param occurence the occurence associated with these determinations (can be null for all occurrences)
     * @param taxonbase the taxon concept associated with these determinations (can be null for all taxon concepts)
     * @return a count of determination events
     */
    public long countDeterminations(SpecimenOrObservationBase occurence,TaxonBase taxonbase);

    /**
     * Returns a List of determinations that have been made for a given occurence
     *
     * @param occurence the occurence associated with these determinations (can be null for all occurrences)
     * @param taxonbase the taxon concept associated with these determinations (can be null for all taxon concepts)
     * @param pageSize The maximum number of determinations returned (can be null for all related determinations)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a Pager of determination instances
     */
    public Pager<DeterminationEvent> getDeterminations(SpecimenOrObservationBase occurence, TaxonBase taxonBase, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns a list of derivation events that have involved creating new DerivedUnits from this occurence
     *
     * @param occurence the occurence that was a source of these derivation events
     * @param pageSize The maximum number of derivation events returned (can be null for all related derivation events)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a Pager of derivation events
     */
    public Pager<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns a Paged List of SpecimenOrObservationBase instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
     *
     * @param clazz filter the results by class (or pass null to return all SpecimenOrObservationBase instances)
     * @param queryString
     * @param pageSize The maximum number of occurrences returned (can be null for all matching occurrences)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths properties to be initialized
     * @return a Pager SpecimenOrObservationBase instances
     * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
     */
    @Override
    public Pager<SpecimenOrObservationBase> search(Class<? extends SpecimenOrObservationBase> clazz, String query, Integer pageSize,Integer pageNumber, List<OrderHint> orderHints,List<String> propertyPaths);

    /**
     * Retrieves the {@link UUID} and the string representation (title cache) of all
     * {@link FieldUnit}s found in the data base.
     * @return a list of {@link UuidAndTitleCache}
     */
    public List<UuidAndTitleCache<FieldUnit>> getFieldUnitUuidAndTitleCache();

    /**
     * Retrieves the {@link UUID} and the string representation (title cache) of all
     * {@link DerivedUnit}s found in the data base.
     * @return a list of {@link UuidAndTitleCache}
     */
    public List<UuidAndTitleCache<DerivedUnit>> getDerivedUnitUuidAndTitleCache(Integer limit, String pattern);

    public DerivedUnitFacade getDerivedUnitFacade(DerivedUnit derivedUnit, List<String> propertyPaths) throws DerivedUnitFacadeNotSupportedException;

    /**
     * Lists all instances of {@link SpecimenOrObservationBase} which are
     * associated with the <code>taxon</code> specified as parameter.
     * SpecimenOrObservationBase instances can be associated to taxa in multiple
     * ways, all these possible relations are taken into account:
     * <ul>
     * <li>The {@link IndividualsAssociation} elements in a
     * {@link TaxonDescription} contain {@link DerivedUnit}s</li>
     * <li>{@link SpecimenTypeDesignation}s may be associated with any
     * {@link HomotypicalGroup} related to the specific {@link Taxon}.</li>
     * <li>A {@link Taxon} may be referenced by the {@link DeterminationEvent}
     * of the {@link SpecimenOrObservationBase}</li>
     * </ul>
     * Further more there also can be taxa which are associated with the taxon
     * in question (parameter associatedTaxon) by {@link TaxonRelationship}s. If
     * the parameter <code>includeRelationships</code> is containing elements,
     * these according {@TaxonRelationshipType}s and
     * directional information will be used to collect further
     * {@link SpecimenOrObservationBase} instances found this way.
     *
     * @param <T>
     * @param type
     * @param associatedTaxon
     * @param Set<TaxonRelationshipVector> includeRelationships. TaxonRelationships will not be taken into account if this is <code>NULL</code>.
     * @param maxDepth TODO
     * @param pageSize
     * @param pageNumber
     * @param orderHints
     * @param propertyPaths
     * @return
     */
    public <T extends SpecimenOrObservationBase> List<T> listByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            Taxon associatedTaxon, boolean includeUnpublished, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * The method will search for specimen associated with the taxon nodes.<br>
     * It will search for 3 possible association types: <br>
     * - via IndividualAssociations of the taxon<br>
     *  - via TypeDesignations of the taxon name<br>
     *  - via Determinations of the taxon or taxon name<br>
     * <br>
     * more are covered in
     * {@link IOccurrenceService#findByTitle(IIdentifiableEntityServiceConfigurator)}
     * @param taxonNodeUuids
     *            a list of {@link UUID}s of the taxon nodes
     * @param limit
     * @param start
     * @return a collection of {@link SpecimenNodeWrapper} containing the
     *         {@link TaxonNode} and the corresponding {@link UuidAndTitleCache}
     *         object for the specimen found for this taxon node
     */
    public Collection<SpecimenNodeWrapper> listUuidAndTitleCacheByAssociatedTaxon(List<UUID> taxonNodeUuids,
            Integer limit, Integer start);

    /**
     * See {@link #listByAssociatedTaxon(Class, Set, Taxon, Integer, Integer, Integer, List, List)}
     *
     * @param type
     *  Restriction to subtype of <code>SpecimenOrObservationBase</code>, can be NULL.
     * @param includeRelationships
     * @param associatedTaxon
     * @param maxDepth
     * @param pageSize
     * @param pageNumber
     * @param orderHints
     * @param propertyPaths
     * @return a Pager
     */
    public <T extends SpecimenOrObservationBase> Pager<T> pageByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            Taxon associatedTaxon, boolean includeUnpublished, Integer maxDepth, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Retrieves all {@link FieldUnit}s for the {@link SpecimenOrObservationBase} with the given {@link UUID}.<br>
     * @param specimenUuid the UUID of the specimen
     * @param propertyPaths the property path
     * @return either a collection of FieldUnits this specimen was derived from, the FieldUnit itself
     * if this was a FieldUnit or an empty collection if no FieldUnits were found
     */
    public Collection<FieldUnit> findFieldUnits(UUID specimenUuid, List<String> propertyPaths);

    /**
     * Retrieves top most originals for a {@link SpecimenOrObservationBase} in the derivation graph by recursively
     * walking all {@link DerivationEvent}s.
     * In most cases item in the returned collection will be {@link FieldUnit FieldUnits} but also DerivedUnits are possible, in
     * cases where no FieledUnit exists.
     *
     * @param specimenUuid the UUID of the specimen
     * @param propertyPaths the property path
     * @return either a collection of root units
     */
    public Collection<SpecimenOrObservationBase> findRootUnits(UUID specimenUuid, List<String> propertyPaths);

    /**
     * @param clazz
     * @param queryString
     * @param languages
     * @param highlightFragments
     * @param pageSize
     * @param pageNumber
     * @param orderHints
     * @param propertyPaths
     * @return
     * @throws CorruptIndexException
     * @throws IOException
     * @throws ParseException
     */
    Pager<SearchResult<SpecimenOrObservationBase>> findByFullText(Class<? extends SpecimenOrObservationBase> clazz,
            String queryString, RectangleDTO boundingBox, List<Language> languages, boolean highlightFragments,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)
            throws IOException, LuceneParseException;

    /**
     * Moves the given {@link Sequence} from one {@link DnaSample} to another
     * @param from the DnaSample from which the sequence will be removed
     * @param to the DnaSample which to which the sequence will be added
     * @param sequence the Sequence to move
     * @return <code>true</code> if successfully moved, <code>false</code> otherwise
     */
    public UpdateResult moveSequence(DnaSample from, DnaSample to, Sequence sequence);


    /**
     * @param fromUuid
     * @param toUuid
     * @param sequenceUuid
     * @return
     */
    public UpdateResult moveSequence(UUID fromUuid, UUID toUuid, UUID sequenceUuid);

    /**
     * Moves the given {@link DerivedUnit} from one {@link SpecimenOrObservationBase} to another.
     * @param from the SpecimenOrObservationBase from which the DerivedUnit will be removed
     * @param to the SpecimenOrObservationBase to which the DerivedUnit will be added
     * @param derivate the DerivedUnit to move
     * @return <code>true</code> if successfully moved, <code>false</code> otherwise
     */
    public UpdateResult moveDerivate(UUID specimenFromUuid, UUID specimenToUuid, UUID derivateUuid);

    /**
     * @param from
     * @param to
     * @param derivate
     * @return
     */
    public boolean moveDerivate(SpecimenOrObservationBase<?> from, SpecimenOrObservationBase<?> to, DerivedUnit derivate);

    /**
     * Assembles a {@link FieldUnitDTO} for the given field unit.<br>
     *
     * @param fieldUnit
     * @return a DTO with all the assembled information
     */
    public FieldUnitDTO assembleFieldUnitDTO(FieldUnit fieldUnit);

    /**
     * Assembles a {@link DerivedUnitDTO} for the given derived unit.
     * @param derivedUnit
     * @return a DTO with all the assembled information
     */
    public SpecimenOrObservationBaseDTO assembleDerivedUnitDTO(DerivedUnit derivedUnit);

    /**
     * Deletes the specified specimen according to the setting in the {@link SpecimenDeleteConfigurator}.<br>
     * @param specimen the specimen which shoul be deleted
     * @param config specifies options if and how the specimen should be deleted like e.g. including all
     * of its children
     * @return the {@link DeleteResult} which holds information about the outcome of this operation
     */
    public DeleteResult delete(SpecimenOrObservationBase<?> specimen, SpecimenDeleteConfigurator config);

    /**
     * Deletes the specified specimen belonging to the given {@link UUID}
     * according to the setting in the {@link SpecimenDeleteConfigurator}.
     *
     * @param specimen
     *            the specimen which shoul be deleted
     * @param config
     *            specifies options if and how the specimen should be deleted
     *            like e.g. including all of its children
     * @return the {@link DeleteResult} which holds information about the
     *         outcome of this operation
     */
    public DeleteResult delete(UUID specimenUuid, SpecimenDeleteConfigurator config);

    /**
     * Retrieves all {@link IndividualsAssociation} with the given specimen.<br>
     * @param specimen the specimen for which the associations are retrieved
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @return collection of all associations
     */
    public Collection<IndividualsAssociation> listIndividualsAssociations(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);


    /**
     * Retrieves all taxa linked via {@link IndividualsAssociation} with the given specimen.<br>
     * @param specimen the specimen which is linked to the taxa
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @return a collection of associated taxa
     */
    public Collection<TaxonBase<?>> listIndividualsAssociationTaxa(SpecimenOrObservationBase<?> specimen, boolean includeUnpublished,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Retrieves all associated taxa for the given specimen (via type designations, determination, individuals associations)
     */
    public Collection<TaxonBase<?>> listAssociatedTaxa(SpecimenOrObservationBase<?> specimen, boolean includeUnpublished,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Retrieves all taxa that the given specimen is determined as
     * @param specimen
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @return collection of all taxa the given specimen is determined as
     */
    public Collection<TaxonBase<?>> listDeterminedTaxa(SpecimenOrObservationBase<?> specimen, boolean includeUnpublished,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Retrieves all {@link DeterminationEvent}s which have the given specimen set as identified unit.
     * @param specimen
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @return collection of all determination events with the given specimen
     */
    public Collection<DeterminationEvent> listDeterminationEvents(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Retrieves all taxa with a {@link SpecimenTypeDesignation} with the given specimen as a type specimen.
     * @param specimen the type specimen
     * @param specimen
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @return a collection of all taxa where the given specimen is the type specimen
     */
    public Collection<TaxonBase<?>> listTypeDesignationTaxa(DerivedUnit specimen, boolean includeUnpublished,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Retrieves all {@link SpecimenTypeDesignation}s which have the given specimens as a type specimen.
     * @param specimens the type specimens
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @return map of all designations with the given type specimens
     */
    //TODO needed?
    public Map<DerivedUnit, Collection<SpecimenTypeDesignation>> listTypeDesignations(Collection<DerivedUnit> specimens, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Retrieves all {@link SpecimenTypeDesignation}s which have the given specimen as a type specimen.
     * @param specimen the type specimen
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @return collection of all designations with the given type specimen
     */
    public Collection<SpecimenTypeDesignation> listTypeDesignations(DerivedUnit specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Retrieves all {@link DescriptionBase}s that have the given specimen set as described specimen.
     * @param specimen the described specimen
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @return collection of all descriptions with the given described specimen
     */
    public Collection<DescriptionBase<?>> listDescriptionsWithDescriptionSpecimen(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Gets all description elements that are used for describing the character
     * states of the given specimen
     *
     * @param specimenUuid
     *            the specimen {@link UUID} for which the character state description
     *            elements should be retrieved
     * @return a collection of all character state description elements for this
     *         specimen
     */
    public Collection<DescriptionElementBase> getCharacterDataForSpecimen(UUID specimenUuid);

    /**
     * Returns the most significant identifier for the given {@link DerivedUnit}.
     * @param derivedUnit the derived unit to check
     * @return the identifier string
     */

    public String getMostSignificantIdentifier(UUID derivedUnit);

    /**
     * Returns the number of specimens that match the given parameters
     * <br>
     * <b>NOTE - issue #6484:</b> the parameters {@link FindOccurrencesConfigurator#getAssignmentStatus()}
     * and {@link FindOccurrencesConfigurator#isRetrieveIndirectlyAssociatedSpecimens()} are not evaluated
     * in the count method
     * @param clazz the class to match
     * @param queryString the queryString to match
     * @param type the {@link SpecimenOrObservationType} to match
     * @param associatedTaxon the taxon these specimens are in any way associated to via
     * determination, type designations, individuals associations, etc.
     * @param matchmode determines how the query string should be matched
     * @param limit
     *            the maximum number of entities returned (can be null to return
     *            all entities)
     * @param start
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @return the number of found specimens
     */
    public long countOccurrences(IIdentifiableEntityServiceConfigurator<SpecimenOrObservationBase> config);

    /**
     * Return the all {@link SpecimenOrObservationBase}s of the complete
     * derivative hierarchy i.e. all parent and child derivatives and the given
     * specimen itself.
     *
     * @param specimen
     *            a specimen or observation
     * @return the derivative hierarchy as an unordered list of all specimens or observation
     */
    public List<SpecimenOrObservationBase<?>> getAllHierarchyDerivatives(SpecimenOrObservationBase<?> specimen);

    /**
     * Returns all child derivatives of the given specimen.
     * @param specimen a specimen or observation
     * @return an unordered list of all child derivatives
     */
    public List<DerivedUnit> getAllChildDerivatives(SpecimenOrObservationBase<?> specimen);

    /**
     * Returns all child derivatives of the given specimen.
     * @param specimen the UUID of a specimen or observation
     * @return an unordered list of all child derivatives
     */
    public List<DerivedUnit> getAllChildDerivatives(UUID specimenUuid);

    /**
     * Returns all {@link FieldUnit}s that are referencing this {@link GatheringEvent}
     * @param gatheringEventUuid the {@link UUID} of the gathering event
     * @return a list of field units referencing the gathering event
     */
    public List<FieldUnit> findFieldUnitsForGatheringEvent(UUID gatheringEventUuid);

    /**
     * Returns a list of {@link UuidAndTitleCache} for the specimens found with the
     * given configurator
     * @param config the configurator for the search
     * @return a list of UuidAndTitleCache object
     */
    @Transactional(readOnly = true)
    public Pager<UuidAndTitleCache<SpecimenOrObservationBase>> findByTitleUuidAndTitleCache(
            FindOccurrencesConfigurator config);

    /**
     * Returns a list of {@link DerivedUnitDTO} for the specimens found with the
     * given configurator
     * @param config the configurator for the search
     * @return a list of {@link DerivedUnitDTO} object
     */
    @Transactional(readOnly = true)
    public List<DerivedUnitDTO> findByTitleDerivedUnitDTO(
            FindOccurrencesConfigurator config);

    /**
     * Collects the <code>FieldUnits</code> which are at the root of the derivation event
     * graph in which the {@link DnaSample} with the specified <code>accessionNumberString</code>
     * is found.
     */
    public SpecimenOrObservationBaseDTO findByGeneticAccessionNumber(String dnaAccessionNumber, List<OrderHint> orderHints);

    /**
     * Recursively searches all {@link DerivationEvent}s to find all "originals" ({@link SpecimenOrObservationBase})
     * from which this DerivedUnit was derived until all FieldUnits are found.
     * <p>
     * <b>NOTE:</b> The recursive search still is a bit incomplete and may miss originals in the rare case where a
     * derivative has more than one original. (see https://dev.e-taxonomy.eu/redmine/issues/9253)
     *
     * @param derivedUnitDTO
     *      The DerivedUnitDTO to start the search from.
     * @param alreadyCollectedSpecimen
     *      A map to hold all originals that have been sees during the recursive walk.
     * @return
     *      The collection of all Field Units that are accessible from the derivative from where the search was started.
     */
    public Collection<SpecimenOrObservationBaseDTO> findRootUnitDTOs(UUID unitUUID);

    /**
     * Finds the units which are associated to a taxon
     * (<code>associatedTaxonUuid</code>) and returns all related root units
     * with the derivation branches up to the derivatives associated with the
     * taxon and the full derivation sub-tree of those.
     * <p>
     * Requirements as stated in the below linked tickets:
     * <ol>
     * <li>The derivation trees (derivatives, sub-derivatives,
     * sub-sub-derivatives, ....) of each of the root units will be included in
     * the result items in
     * ({@link SpecimenOrObservationBaseDTO#getDerivatives()})</li>
     * <li>.... should contain all derivates of a derivate determined to the
     * taxon or its name and all elements should be displayed only once.
     * ....</li>
     * <li>... also sollen beim DerivateTree eigentlich auch nur die Derivate
     * angezeigt werden, die über Taxon Association oder Determination an einem
     * Taxon oder Namen hängen und ihre direkten Eltern und Kinder (+
     * KindesKinder…).
     * <li>Also im Endeffekt muss man die Derivate raus suchen, die eine
     * Assoziation zu dem Taxon haben und dann die direkten Vorgänger und die
     * Nachfolger finden. Andere Derivate, die von Origin Derivaten abstammen
     * würden erstmal nicht dazugehören, außer sie sind ebenfalls mit dem Taxon
     * assoziiert....</li>
     * <ul>
     *
     * Related tickets:
     *
     * <ul>
     * <li>https://dev.e-taxonomy.eu/redmine/issues/7599</li>
     * <li>https://dev.e-taxonomy.eu/redmine/issues/9216</li>
     * </ul>
     *
     * @param includedRelationships
     *  TODO
     * @param associatedTaxonUuid
     *  The uuid of the taxon for which associated derivatives are to be found.
     * @param propertyPaths
     *  The bean initialization strategy
     * @return
     *  The list of root units with fully or partially assembled derivation graph.
     */
    public List<SpecimenOrObservationBaseDTO> listRootUnitDTOsByAssociatedTaxon(Set<TaxonRelationshipEdge> includedRelationships,
            UUID associatedTaxonUuid, boolean includeUnpublished, List<String> propertyPaths);

    /**
     * Lists all root units which are
     * associated <b>directly or indirectly</b>with the <code>taxon</code> specified
     * as parameter. "Indirectly" means that a sub derivate of the FieldUnit is
     * directly associated with the given taxon.
     * SpecimenOrObservationBase instances can be associated to taxa in multiple
     * ways, all these possible relations are taken into account:
     * <ul>
     * <li>The {@link IndividualsAssociation} elements in a
     * {@link TaxonDescription} contain {@link DerivedUnit}s</li>
     * <li>{@link SpecimenTypeDesignation}s may be associated with any
     * {@link HomotypicalGroup} related to the specific {@link Taxon}.</li>
     * <li>A {@link Taxon} may be referenced by the {@link DeterminationEvent}
     * of the {@link SpecimenOrObservationBase}</li>
     * </ul>
     * Further more there also can be taxa which are associated with the taxon
     * in question (parameter associatedTaxon) by {@link TaxonRelationship}s. If
     * the parameter <code>includeRelationships</code> is containing elements,
     * these according {@TaxonRelationshipType}s and
     * directional information will be used to collect further
     * {@link SpecimenOrObservationBase} instances found this way.
     *
     * @param <T>
     * @param type
     *  Restriction to a specific subtype, may be null.
     * @param associatedTaxon
     * @param Set<TaxonRelationshipVector> includeRelationships. TaxonRelationships will not be taken into account if this is <code>NULL</code>.
     * @param maxDepth TODO
     * @param pageSize
     * @param pageNumber
     * @param orderHints
     * @param propertyPaths
     * @return
     */
    public <T extends SpecimenOrObservationBase> Collection<T> listRootUnitsByAssociatedTaxon(
            Class<T> type, Taxon associatedTaxon, boolean includeUnpublished, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * See {@link #listFieldUnitsByAssociatedTaxon(Set, Taxon, Integer, Integer, Integer, List, List)}
     */
    public <T extends SpecimenOrObservationBase> Pager<T> pageRootUnitsByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            Taxon associatedTaxon, boolean includeUnpublished, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    public List<Point> findPointsForFieldUnitList(List<UUID> fieldUnitUuids);

    /**
     * Load the FieldUnitDTO for the given <code>derivedUnitUuid</code> with all intermediate derivatives and {@link eu.etaxonomy.cdm.api.service.dto.GatheringEventDTO}
     */
    public FieldUnitDTO loadFieldUnitDTO(UUID derivedUnitUuid);

    public Pager<MediaDTO> getMediaDTOs(SpecimenOrObservationBase<?> occurence, Integer pageSize, Integer pageNumber);

    public Pager<Media> getMediaInHierarchy(SpecimenOrObservationBase<?> rootOccurence, boolean collectOriginalMedia, boolean collectDerivativeMedia,
			Integer pageSize, Integer pageNumber, List<String> propertyPaths);
}