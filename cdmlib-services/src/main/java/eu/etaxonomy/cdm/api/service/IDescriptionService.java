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
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.dto.TaxonDistributionDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.DescriptionBaseDto;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IDescriptionService extends IIdentifiableEntityService<DescriptionBase> {

    /**
     * @return
     * @deprecated use TermService#getVocabulary(VocabularyType) instead
     */
    @Deprecated
    public TermVocabulary<Feature> getDefaultFeatureVocabulary();

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
    public long count(Class<? extends DescriptionBase> type, Boolean hasImages, Boolean hasText, Set<Feature> feature);

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
     * @param descriptionTypes Restrict the results to those descriptions of the given types (can be null or empty)
     * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager containing TaxonDescription instances
     */
    public Pager<TaxonDescription> pageTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes, Set<NamedArea> geographicalScope, Set<MarkerType> markerTypes, Set<DescriptionType> descriptionTypes, Integer pageSize, Integer pageNumber, List<String> propertyPaths);


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
    public List<TaxonDescription> listTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes, Set<NamedArea> geographicalScope, Set<MarkerType> markerTypes, Set<DescriptionType> descriptionTypes, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

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
     * @return a Pager containing TaxonName instances
     *
     * FIXME candidate for harmonization - rename to pageTaxonNameDescriptions
     */
    public Pager<TaxonNameDescription> getTaxonNameDescriptions(TaxonName name, Integer pageSize, Integer pageNumber, List<String> propertyPaths);


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
    public <S extends DescriptionElementBase> Pager<S> searchElements(Class<S> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

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
      * Generate a string representation of the structured <code>description</code> supplied in natural language
      * The <code>featureTree</code> will be used to structure the NaturalLanguageDescription.
      * This method does not require a initialization strategy so there is no <code>propertyPaths</code> parameter.
      */
    public String generateNaturalLanguageDescription(TermTree featureTree,TaxonDescription description, List<Language> preferredLanguages, String separator);

    /**
     * Preliminary method to test whether a description contains structured data.
     * @deprecated The means of determining this fact may change soon, so this method is
                    annotated as being deprecated.
     * @param description
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
    public UpdateResult moveDescriptionElementsToDescription(Collection<DescriptionElementBase> descriptionElements, DescriptionBase targetDescription, boolean isPaste, boolean setNameInSource);

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

	/**
	 * Deletes the description and prior removes it from taxon, specimen or descriptive dataset.
	 */
	public DeleteResult deleteDescription(DescriptionBase<?> description);

	public DeleteResult deleteDescription(UUID descriptionUuid);

    public UpdateResult moveTaxonDescriptions(Taxon sourceTaxon, Taxon targetTaxon, boolean setNameInSource);

    public UpdateResult moveTaxonDescriptions(UUID sourceTaxonUuid, UUID targetTaxonUuid, boolean setNameInSource);

    public UpdateResult moveDescriptionElementsToDescription(Set<UUID> descriptionElementUUIDs, UUID targetDescriptionUuid,
            boolean isCopy, boolean setNameInSource);

    public UpdateResult moveDescriptionElementsToDescription(Set<UUID> descriptionElementUUIDs, UUID targetTaxonUuid,
            String moveMessage, boolean isCopy, boolean setNameInSource, boolean useDefaultDescription);

    public UpdateResult moveTaxonDescription(UUID descriptionUuid, UUID targetTaxonUuid, boolean setNameInSource);

    public List<MergeResult<DescriptionBase>> mergeDescriptionElements(Collection<TaxonDistributionDTO> descriptionElements,
            boolean returnTransientEntity);

    public UpdateResult mergeDescriptions(Collection<DescriptionBaseDto> descriptions, UUID descriptiveDataSetUuid);

    public UpdateResult moveDescriptionElementsToDescription(Set<UUID> descriptionElementUUIDs,
            DescriptionBase targetDescription, boolean isCopy, boolean setNameInSource);

    public DeleteResult isDeletable(UUID descriptionUuid);

    public DescriptionBaseDto loadDto(UUID descriptionUuid);
    public List<DescriptionBaseDto> loadDtos(Set<UUID> descriptionUuid);

    public List<DescriptionBaseDto> loadDtosForTaxon(UUID taxonUuid);

    /**
     * Find the taxon node for the taxon associated to the specimen in classification with classificationUuid
     */
    public TaxonNodeDto findTaxonNodeDtoForIndividualAssociation(UUID specimenUuid, UUID classificationUuid);

}