// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.utility.DescriptionUtility;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IDescriptionService extends IIdentifiableEntityService<DescriptionBase> {

    /**
     *
     * @return
     * @deprecated use TermService#getVocabulary(VocabularyType) instead
     */
    @Deprecated
    public TermVocabulary<Feature> getDefaultFeatureVocabulary();

    /**
     * @deprecated use TermService#getVocabulary(VocabularyType) instead
     */
    @Deprecated
    public TermVocabulary<Feature> getFeatureVocabulary(UUID uuid);

    /**
     * Gets a DescriptionElementBase instance matching the supplied uuid
     *
     * @param uuid the uuid of the DescriptionElement of interest
     * @return a DescriptionElement, or null if the DescriptionElement does not exist
     */
    public DescriptionElementBase getDescriptionElementByUuid(UUID uuid);

    /**
     * Loads and existing DescriptionElementBase instance matching the supplied uuid,
     * and recursively initializes all bean properties given in the
     * <code>propertyPaths</code> parameter.
     * <p>
     * For detailed description and examples <b>please refer to:</b>
     * {@link IBeanInitializer#initialize(Object, List)}
     *
     * @param uuid the uuid of the DescriptionElement of interest
     * @return a DescriptionElement, or null if the DescriptionElement does not exist
     */
    public DescriptionElementBase loadDescriptionElement(UUID uuid,List<String> propertyPaths);

    /**
     * Persists a <code>DescriptionElementBase</code>
     * @param descriptionElement
     * @return
     */
    public UUID saveDescriptionElement(DescriptionElementBase descriptionElement);

    /**
     * Persists a collection of <code>DescriptionElementBase</code>
     * @param descriptionElements
     * @return
     */
    public Map<UUID, DescriptionElementBase> saveDescriptionElement(Collection<DescriptionElementBase> descriptionElements);

    /**
     * Delete an existing description element
     *
     * @param descriptionElement the description element to be deleted
     * @return the unique identifier of the deleted entity
     */
    public UUID deleteDescriptionElement(DescriptionElementBase descriptionElement);

    public UUID deleteDescriptionElement(UUID descriptionElementUuid);

    /**
     * List the descriptions of type <T>, filtered using the following parameters
     *
     * @param type The type of description returned (Taxon, TaxonName, or Specimen)
     * @param hasMedia Restrict the description to those that do (true) or don't (false) contain <i>elements</i> that have one or more media (can be null)
     * @param hasText Restrict the description to those that do (true) or don't (false) contain TextData <i>elements</i> that have some textual content (can be null)
     * @param feature Restrict the description to those <i>elements</i> which are scoped by one of the Features passed (can be null or empty)
     * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param orderHints may be null
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager containing DescriptionBase instances
     */
    public Pager<DescriptionBase> page(Class<? extends DescriptionBase> type, Boolean hasMedia, Boolean hasText, Set<Feature> feature, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Count the descriptions of type <TYPE>, filtered using the following parameters
     *
     * @param type The type of description returned (Taxon, TaxonName, or Specimen)
     * @param hasMedia Restrict the description to those that do (true) or don't (false) contain <i>elements</i> that have one or more media (can be null)
     * @param hasText Restrict the description to those that do (true) or don't (false) contain TextData <i>elements</i> that have some textual content (can be null)
     * @param feature Restrict the description to those <i>elements</i> which are scoped by one of the Features passed (can be null or empty)
     * @return a count of DescriptionBase instances
     */
    public int count(Class<? extends DescriptionBase> type, Boolean hasImages, Boolean hasText, Set<Feature> feature);

    /**
     * Returns description elements of type <TYPE>, belonging to a given
     * description, optionally filtered by one or more features
     *
     * @param description
     *            The description which these description elements belong to
     *            (can be null to count all description elements)
     * @param descriptionType
     *            A filter DescriptionElements which belong to of a specific
     *            class of Descriptions
     * @param features
     *            Restrict the results to those description elements which are
     *            scoped by one of the Features passed (can be null or empty)
     * @param type
     *            A filter for DescriptionElements of a specific class
     * @param pageSize
     *            The maximum number of description elements returned (can be
     *            null for all description elements)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based)
     * @param propertyPaths
     *            Properties to initialize in the returned entities, following
     *            the syntax described in
     *            {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager containing DescriptionElementBase instances

     * @deprecated use
     *             {@link #pageDescriptionElements(DescriptionBase, Set, Class, Integer, Integer, List)}
     *             instead
     */
    @Deprecated
    public <T extends DescriptionElementBase> Pager<T> getDescriptionElements(DescriptionBase description,Set<Feature> features, Class<T> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths);


    /**
     * Returns description elements of type <TYPE>, belonging to a given
     * description, optionally filtered by one or more features
     *
     * @param description
     *            The description which these description elements belong to
     *            (can be null to count all description elements)
     * @param descriptionType
     *            A filter DescriptionElements which belong to of a specific
     *            class of Descriptions
     * @param features
     *            Restrict the results to those description elements which are
     *            scoped by one of the Features passed (can be null or empty)
     * @param type
     *            A filter for DescriptionElements of a specific class
     * @param pageSize
     *            The maximum number of description elements returned (can be
     *            null for all description elements)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based)
     * @param propertyPaths
     *            Properties to initialize in the returned entities, following
     *            the syntax described in
     *            {@link IBeanInitializer#initialize(Object, List)}
     *
     * @return a Pager containing DescriptionElementBase instances
     */
    public <T extends DescriptionElementBase> Pager<T> pageDescriptionElements(DescriptionBase description, Class<? extends DescriptionBase> descriptionType, Set<Feature> features, Class<T> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns description elements of type <TYPE>, belonging to a given
     * description, optionally filtered by one or more features
     *
     * @param description
     *            The description which these description elements belong to
     *            (can be null to count all description elements)
     * @param features
     *            Restrict the results to those description elements which are
     *            scoped by one of the Features passed (can be null or empty)
     * @param type
     *            A filter for DescriptionElements of a specific class
     * @param pageSize
     *            The maximum number of description elements returned (can be
     *            null for all description elements)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based)
     * @param propertyPaths
     *            Properties to initialize in the returned entities, following
     *            the syntax described in
     *            {@link IBeanInitializer#initialize(Object, List)}
     * @return a List of DescriptionElementBase instances
     * @deprecated use {@link #listDescriptionElements(DescriptionBase, Class, Set, Class, Integer, Integer, List)} instead
     */
    @Deprecated
    public <T extends DescriptionElementBase> List<T> listDescriptionElements(DescriptionBase description,Set<Feature> features, Class<T> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns description elements of type <TYPE>, belonging to a given
     * description, optionally filtered by one or more features
     *
     * @param description
     *            The description which these description elements belong to
     *            (can be null to count all description elements)
     * @param features
     *            Restrict the results to those description elements which are
     *            scoped by one of the Features passed (can be null or empty)
     * @param type
     *            A filter DescriptionElements of a for specific class
     * @param pageSize
     *            The maximum number of description elements returned (can be
     *            null for all description elements)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based)
     * @param propertyPaths
     *            Properties to initialize in the returned entities, following
     *            the syntax described in
     *            {@link IBeanInitializer#initialize(Object, List)}
     * @return a List of DescriptionElementBase instances
     */
    public <T extends DescriptionElementBase> List<T> listDescriptionElements(DescriptionBase description, Class<? extends DescriptionBase> descriptionType, Set<Feature> features, Class<T> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Return a Pager containing Annotation entities belonging to the DescriptionElementBase instance supplied, optionally filtered by MarkerType
     * @param annotatedObj The object that "owns" the annotations returned
     * @param status Only return annotations which are marked with a Marker of this type (can be null to return all annotations)
     * @param pageSize The maximum number of terms returned (can be null for all annotations)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param orderHints may be null
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager of Annotation entities
     */
    public Pager<Annotation> getDescriptionElementAnnotations(DescriptionElementBase annotatedObj, MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);


    /**
     * Returns a List of TaxonDescription instances, optionally filtered by parameters passed to this method
     *
     * @param taxon The taxon which the description refers to (can be null for all TaxonDescription instances)
     * @param scopes Restrict the results to those descriptions which are scoped by one of the Scope instances passed (can be null or empty)
     * @param geographicalScope Restrict the results to those descriptions which have a geographical scope that overlaps with the NamedArea instances passed (can be null or empty)
     * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager containing TaxonDescription instances
     *
     * @see #pageMarkedTaxonDescriptions(Taxon, Set, Set, Set, Integer, Integer, List)
     */
    public Pager<TaxonDescription> pageTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes, Set<NamedArea> geographicalScope, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns a List of TaxonDescription instances, optionally filtered by parameters passed to this method
     *
     * @param taxon The taxon which the description refers to (can be null for all TaxonDescription instances)
     * @param scopes Restrict the results to those descriptions which are scoped by one of the Scope instances passed (can be null or empty)
     * @param geographicalScope Restrict the results to those descriptions which have a geographical scope that overlaps with the NamedArea instances passed (can be null or empty)
     * @param markerType Restrict the results to those descriptions which are marked as true by one of the given marker types (can be null or empty)
     * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager containing TaxonDescription instances
     */
    public Pager<TaxonDescription> pageTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes, Set<NamedArea> geographicalScope, Set<MarkerType> markerTypes, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * @see {@link #pageTaxonDescriptions(Taxon, Set, Set, Integer, Integer, List)}
     *
     * @param taxon
     * @param scopes
     * @param geographicalScope
     * @param pageSize
     * @param pageNumber
     * @param propertyPaths
     * @return
     */
    public List<TaxonDescription> listTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes, Set<NamedArea> geographicalScope, Integer pageSize, Integer pageNumber, List<String> propertyPaths);



    /**
     * @see {@link #pageMarkedTaxonDescriptions(Taxon, Set, Set, Set, Integer, Integer, List)}
     *
     * @param taxon
     * @param scopes
     * @param geographicalScope
     * @param pageSize
     * @param pageNumber
     * @param propertyPaths
     * @return
     */
    public List<TaxonDescription> listTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes, Set<NamedArea> geographicalScope, Set<MarkerType> markerTypes, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns all {@link Media} attached to a taxon via TaxonDescription.elements.media.
     * @param taxonUuid the taxons uuid, if null media for all taxa are returned
     * @param limitToGalleries if true only media in TaxonDescriptions with imageGallery flag=true are returned
     * @param markerTypes only media for TaxonDescriptions with marker of type markerType and marker.flag=true are returned, one matching marker type is sufficient
     * @param pageSize
     * @param pageNumber
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return
     */
    public List<Media> listTaxonDescriptionMedia(UUID taxonUuid, boolean limitToGalleries, Set<MarkerType> markerTypes, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns count for all {@link Media} attached to a taxon via TaxonDescription.elements.media.
     * @param taxonUuid the taxons uuid, if null media for all taxa are returned
     * @param limitToGalleries if true only media in TaxonDescriptions with imageGallery flag=true are returned
     * @param markerTypes only media for TaxonDescriptions with marker of type markerType and marker.flag=true are returned, one matching marker type is sufficient
     * @return
     */
    public int countTaxonDescriptionMedia(UUID taxonUuid, boolean limitToGalleries, Set<MarkerType> markerTypes);



    /**
     * Returns a List of TaxonNameDescription instances, optionally filtered by the name which they refer to
     *
     * @param name Restrict the results to those descriptions that refer to a specific name (can be null for all TaxonNameDescription instances)
     * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager containing TaxonNameBase instances
     *
     * FIXME candidate for harmonization - rename to pageTaxonNameDescriptions
     */
    public Pager<TaxonNameDescription> getTaxonNameDescriptions(TaxonNameBase name, Integer pageSize, Integer pageNumber, List<String> propertyPaths);


    /**
     * Returns a List of distinct TaxonDescription instances which have Distribution elements that refer to one of the NamedArea instances passed (optionally
     * filtered by a type of PresenceAbsenceTerm e.g. PRESENT / ABSENT / NATIVE / CULTIVATED etc)
     *
     * @param namedAreas The set of NamedArea instances
     * @param presence Restrict the descriptions to those which have Distribution elements are of this status (can be null)
     * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager containing TaxonDescription instances
     */
    public Pager<TaxonDescription> searchDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTerm presence, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Returns a Paged List of DescriptionElementBase instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
     *
     * @param clazz filter the results by class (or pass null to return all DescriptionElementBase instances)
     * @param queryString
     * @param pageSize The maximum number of descriptionElements returned (can be null for all matching descriptionElements)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths properties to be initialized
     * @return a Pager DescriptionElementBase instances
     * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
     */
    public Pager<DescriptionElementBase> searchElements(Class<? extends DescriptionElementBase> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Returns a List of Media that are associated with a given description element
     *
     * @param descriptionElement the description element associated with these media
     * @param pageSize The maximum number of media returned (can be null for all related media)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager containing media instances
     *
     * FIXME candidate for harmonization - rename to pageMedia
     */
    public Pager<Media> getMedia(DescriptionElementBase descriptionElement, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Provides access to all DescriptionElements associated with the given Taxon
     * via a TaxonDescrition.
     *
     * @param taxon
     *            The Taxon to return Description elements for
     * @param features
     *            Restrict the results to those description elements which are
     *            scoped by one of the Features passed (can be null or empty)
     * @param type
     *            A filter for DescriptionElements of a specific class
     * @param pageSize
     *            The maximum number of description elements returned (can be
     *            null for all description elements)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based)
     * @param propertyPaths
     *            Properties to initialize in the returned entities, following
     *            the syntax described in
     *            {@link IBeanInitializer#initialize(Object, List)}
     * @return a List containing all matching DescriptionElementBase instances
     *
     * @deprecated use {@link #listDescriptionElementsForTaxon(Taxon, Set, Class, Integer, Integer, List)} instead
     */
    @Deprecated
    public <T extends DescriptionElementBase> List<T>  getDescriptionElementsForTaxon(Taxon taxon, Set<Feature> features, Class<T> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Provides access to all DescriptionElements associated with the given Taxon
     * via a TaxonDescrition.
     *
     * @param taxon
     *            The Taxon to return Description elements for
     * @param features
     *            Restrict the results to those description elements which are
     *            scoped by one of the Features passed (can be null or empty)
     * @param type
     *            A filter for DescriptionElements of a specific class
     * @param pageSize
     *            The maximum number of description elements returned (can be
     *            null for all description elements)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based)
     * @param propertyPaths
     *            Properties to initialize in the returned entities, following
     *            the syntax described in
     *            {@link IBeanInitializer#initialize(Object, List)}
     * @return a List containing all matching DescriptionElementBase instances
     *
     */
    public <T extends DescriptionElementBase> List<T>  listDescriptionElementsForTaxon(Taxon taxon, Set<Feature> features, Class<T> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Provides access to all DescriptionElements associated with the given Taxon
     * via a TaxonDescrition.
     *
     * @param taxon
     *            The Taxon to return Description elements for
     * @param features
     *            Restrict the results to those description elements which are
     *            scoped by one of the Features passed (can be null or empty)
     * @param type
     *            A filter for DescriptionElements of a specific class
     * @param pageSize
     *            The maximum number of description elements returned
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based)
     * @param propertyPaths
     *            Properties to initialize in the returned entities, following
     *            the syntax described in
     *            {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager for all matching DescriptionElementBase instances
     *
     */
    public <T extends DescriptionElementBase> Pager<T>  pageDescriptionElementsForTaxon(Taxon taxon, Set<Feature> features, Class<T> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths);


    /**
     * @param taxonDescriptions
     * @param subAreaPreference
     *            enables the <b>Sub area preference rule</b> if set to true,
     *            see {@link DescriptionUtility#filterDistributions(Collection,
     *            boolean, boolean}

     * @param statusOrderPreference
     *            enables the <b>Status order preference rule</b> if set to
     *            true, see {@link
     *            DescriptionUtility#filterDistributions(Collection, boolean,
     *            boolean}
     * @param hideMarkedAreas
     *            distributions where the area has a {@link Marker} with one of
     *            the specified {@link MarkerType}s will be skipped, see
     *            {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean, Set)}
     * @param omitLevels
     *            A Set NamedArea levels to omit - optional
     * @param propertyPaths
     *            the initialization strategy
     *
     * @return
     * @deprecated use {@link IEditGeoService#composeDistributionInfoFor()} instead
     */
    @Deprecated
    public DistributionTree getOrderedDistributions(
            Set<TaxonDescription> taxonDescriptions,
            boolean subAreaPreference,
            boolean statusOrderPreference,
            Set<MarkerType> hideMarkedAreas,
            Set<NamedAreaLevel> omitLevels, List<String> propertyPaths);

    /**
      * Generate a string representation of the structured <code>description</code> supplied in natural language
      * The <code>featureTree</code> will be used to structure the NaturalLanguageDescription.
      * This method does not require a initialization strategy so there is no <code>propertyPaths</code> parameter.
      * @param featureTree
      * @param description
      * @param preferredLanguages
      * @param separator
      * @return
      */
    public String generateNaturalLanguageDescription(FeatureTree featureTree,TaxonDescription description, List<Language> preferredLanguages, String separator);

    /**
     * Preliminary method to test whether a description contains structured data.
     * @deprecated The means of determining this fact may change soon, so this method is
                    annotated as being deprecated.
     * @param description
     * @return
     */
    @Deprecated
    public boolean hasStructuredData(DescriptionBase<?> description);

    /**
     * Add the collection of {@link DescriptionElementBase description elements} to the <code>targetDescription</code>.
     * Remove the description elements from the description they are currently associated with.
     *
     * @param descriptionElements
     * @param targetDescription
     * @param isPaste if true, the elements are only copied (cloned) and not removed from the
     * 	old description
     * @return
     */
    public UpdateResult moveDescriptionElementsToDescription(Collection<DescriptionElementBase> descriptionElements, DescriptionBase targetDescription, boolean isPaste);

    /**
     * Pager method to get all {@link NamedAreas} instances which are currently used
     * by {@link Distribution} elements.
     *
     * @param pageSize
     *            The maximum number of description elements returned
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based)
     * @param propertyPaths
     *            Properties to initialize in the returned entities, following
     *            the syntax described in
     *            {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager for all NamedAreas instances which are currently in use.
     *
     */
    public Pager<TermDto> pageNamedAreasInUse(boolean includeAllParents, Integer pageSize,
            Integer pageNumber);

	public DeleteResult deleteDescription(DescriptionBase description);

	public DeleteResult deleteDescription(UUID descriptionUuid);

    /**
     * @param sourceTaxon
     * @param targetTaxon
     * @return
     */
    public UpdateResult moveTaxonDescriptions(Taxon sourceTaxon, Taxon targetTaxon);

    /**
     * @param sourceTaxonUuid
     * @param targetTaxonUuid
     * @return
     */
    public UpdateResult moveTaxonDescriptions(UUID sourceTaxonUuid, UUID targetTaxonUuid);


    /**
     * @param descriptionElementUUIDs
     * @param targetDescriptionUuid
     * @param isCopy
     * @return
     */
    public UpdateResult moveDescriptionElementsToDescription(Set<UUID> descriptionElementUUIDs, UUID targetDescriptionUuid,
            boolean isCopy);

    /**
     * @param descriptionElementUUIDs
     * @param targetTaxonUuid
     * @param moveMessage
     * @param isCopy
     * @return
     */
    public UpdateResult moveDescriptionElementsToDescription(Set<UUID> descriptionElementUUIDs, UUID targetTaxonUuid,
            String moveMessage, boolean isCopy);

    /**
     * @param descriptionUUID
     * @param targetTaxonUuid
     * @return
     */
    public UpdateResult moveTaxonDescription(UUID descriptionUuid, UUID targetTaxonUuid);
}