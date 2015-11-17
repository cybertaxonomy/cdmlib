// $Id$
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
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.hibernate.search.spatial.impl.Rectangle;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.api.service.config.IIdentifiableEntityServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.SpecimenDeleteConfigurator;
import eu.etaxonomy.cdm.api.service.dto.FieldUnitDTO;
import eu.etaxonomy.cdm.api.service.dto.PreservedSpecimenDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
public interface IOccurrenceService extends IIdentifiableEntityService<SpecimenOrObservationBase> {

    public Country getCountryByIso(String iso639);

    public List<Country> getCountryByName(String name);

    /**
     * Returns a paged list of occurrences that have been determined to belong
     * to the taxon concept determinedAs, optionally restricted to objects
     * belonging to a class that that extends SpecimenOrObservationBase. This
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
    public Pager<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> type, TaxonNameBase determinedAs, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Returns a List of Media that are associated with a given occurence
     *
     * @param occurence the occurence associated with these media
     * @param pageSize The maximum number of media returned (can be null for all related media)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager of media instances
     */
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
    public Pager<Media> getMediainHierarchy(SpecimenOrObservationBase rootOccurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns a count of determinations that have been made for a given occurence and for a given taxon concept
     *
     * @param occurence the occurence associated with these determinations (can be null for all occurrences)
     * @param taxonbase the taxon concept associated with these determinations (can be null for all taxon concepts)
     * @return a count of determination events
     */
    public int countDeterminations(SpecimenOrObservationBase occurence,TaxonBase taxonbase);

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
    public List<UuidAndTitleCache<DerivedUnit>> getDerivedUnitUuidAndTitleCache();

    public DerivedUnitFacade getDerivedUnitFacade(DerivedUnit derivedUnit, List<String> propertyPaths) throws DerivedUnitFacadeNotSupportedException;

    public List<DerivedUnitFacade> listDerivedUnitFacades(DescriptionBase description, List<String> propertyPaths);

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
            Taxon associatedTaxon, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Lists all instances of {@link FieldUnit} which are
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
     * @param associatedTaxon
     * @param Set<TaxonRelationshipVector> includeRelationships. TaxonRelationships will not be taken into account if this is <code>NULL</code>.
     * @param maxDepth TODO
     * @param pageSize
     * @param pageNumber
     * @param orderHints
     * @param propertyPaths
     * @return
     */
    public Collection<SpecimenOrObservationBase> listFieldUnitsByAssociatedTaxon(Taxon associatedTaxon, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * See {@link #listFieldUnitsByAssociatedTaxon(Set, Taxon, Integer, Integer, Integer, List, List)}
     */
    public Pager<SpecimenOrObservationBase> pageFieldUnitsByAssociatedTaxon(Set<TaxonRelationshipEdge> includeRelationships,
            Taxon associatedTaxon, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * See {@link #listByAssociatedTaxon(Class, Set, Taxon, Integer, Integer, Integer, List, List)}
     *
     * @param type
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
            Taxon associatedTaxon, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Retrieves all {@link FieldUnit}s for the {@link SpecimenOrObservationBase} with the given {@link UUID}.<br>
     * @param specimenUuid the UUID of the specimen
     * @return either a collection of FieldUnits this specimen was derived from, the FieldUnit itself
     * if this was a FieldUnit or an empty collection if no FieldUnits were found
     */
    public Collection<FieldUnit> getFieldUnits(UUID specimenUuid);

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
            String queryString, Rectangle boundingBox, List<Language> languages, boolean highlightFragments, Integer pageSize,
            Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws CorruptIndexException,
            IOException, ParseException;
    /**
     * See {@link #listByAssociatedTaxon(Class, Set, String, Integer, Integer, Integer, List, List)}
     *
     * @param type
     * @param includeRelationships
     * @param associatedTaxon
     * @param maxDepth
     * @param pageSize
     * @param pageNumber
     * @param orderHints
     * @param propertyPaths
     * @return a Pager
     */
    public <T extends SpecimenOrObservationBase> Pager<T>  pageByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            String taxonUUID, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

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
     * Assembles a {@link FieldUnitDTO} for the given field unit uuid which is associated to the {@link Taxon}.<br>
     * <br>
     * For the meaning of "associated" see also {@link #listFieldUnitsByAssociatedTaxon(Set, Taxon, Integer, Integer, Integer, List, List)}
     * @param fieldUnit
     * @param associatedTaxonUuid
     * @return a DTO with all the assembled information
     */
    public FieldUnitDTO assembleFieldUnitDTO(FieldUnit fieldUnit, UUID associatedTaxonUuid);

    /**
     * Assembles a {@link PreservedSpecimenDTO} for the given derived unit.
     * @param derivedUnit
     * @return a DTO with all the assembled information
     */
    public PreservedSpecimenDTO assemblePreservedSpecimenDTO(DerivedUnit derivedUnit);

    /**
     * Returns a collection of {@link ICdmBase}s that are not persisted via cascading when saving the given specimen (mostly DefinedTerms).
     * @param specimen the specimen that is checked for non-cascaded elements.
     * @return collection of non-cascaded element associated with the specimen
     */
    public Collection<ICdmBase> getNonCascadedAssociatedElements(SpecimenOrObservationBase<?> specimen);

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
     * Retrieves all associated taxa for the given specimen
     * @param specimen
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @return
     */
    public Collection<TaxonBase<?>> listAssociatedTaxa(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Retrieves all {@link SpecimenTypeDesignation}s which have the given specimen as a type specimen.
     * @param specimen the type specimen
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @return collection of all designations with the given type specimen
     */
    public Collection<SpecimenTypeDesignation> listTypeDesignations(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

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
     * @param specimen
     *            the specimen for which the character state description
     *            elements should be retrieved
     * @return a collection of all character state description elements for this
     *         specimen
     */
    public Collection<DescriptionElementBase> getCharacterDataForSpecimen(SpecimenOrObservationBase<?> specimen);

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
    public String getMostSignificantIdentifier(DerivedUnit derivedUnit);

    /**
     * Returns the number of specimens that match the given parameters
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
    public int countOccurrences(IIdentifiableEntityServiceConfigurator<SpecimenOrObservationBase> config);

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

}
