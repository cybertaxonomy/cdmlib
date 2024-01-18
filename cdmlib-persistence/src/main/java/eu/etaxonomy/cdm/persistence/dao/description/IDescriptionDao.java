/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;
import eu.etaxonomy.cdm.persistence.dto.DescriptionBaseDto;
import eu.etaxonomy.cdm.persistence.dto.SortableTaxonNodeQueryResult;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IDescriptionDao extends IIdentifiableDao<DescriptionBase> {

    /**
     * List the descriptions of type <TYPE>, filtered using the following parameters
     *
     * @param type The type of description returned (Taxon, TaxonName, or Specimen)
     * @param hasMedia Restrict the description to those that do (true) or don't (false) contain <i>elements</i> that have one or more media (can be null)
     * @param hasText Restrict the description to those that do (true) or don't (false) contain TextData <i>elements</i> that have some textual content (can be null)
     * @param feature Restrict the description to those <i>elements</i> which are scoped by one of the Features passed (can be null or empty)
     * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param orderHints may be null
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a List of DescriptionBase instances
     */
     public List<DescriptionBase> listDescriptions(Class<? extends DescriptionBase> type, Boolean hasMedia, Boolean hasText, Set<Feature> feature, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Count the descriptions of type <TYPE>, filtered using the following parameters
     *
     * @param type The type of description returned (Taxon, TaxonName, or Specimen)
     * @param hasMedia Restrict the description to those that do (true) or don't (false) contain <i>elements</i> that have one or more media (can be null)
     * @param hasText Restrict the description to those that do (true) or don't (false) contain TextData <i>elements</i> that have some textual content (can be null)
     * @param feature Restrict the description to those <i>elements</i> which are scoped by one of the Features passed (can be null or empty)
     * @return a count of DescriptionBase instances
     */
     public long countDescriptions(Class<? extends DescriptionBase> type, Boolean hasImages, Boolean hasText, Set<Feature> feature);

    /**
     * Returns a count of TaxonDescription instances, optionally filtered by parameters passed to this method
     *
     * @param taxon Restrict the results to those descriptions that refer to a specific taxon (can be null for all TaxonDescription instances)
     * @param scopes Restrict the results to those descriptions which are scoped by one of the Scope instances passed (can be null or empty)
     * @param geographicalScope Restrict the results to those descriptions which have a geographical scope that overlaps with the NamedArea instances passed (can be null or empty)
     * @param markerType Restrict the results to those descriptions which are marked as true by one of the given marker types (can be null or empty)
     * @param descriptionTypes Restrict the results to those descriptions of the given types (can be null or empty)
     * @return a count of TaxonDescription instances
     */
    public long countTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes, Set<NamedArea> geographicalScope, Set<MarkerType> markerType, Set<DescriptionType> descriptionTypes);

    /**
     * Returns description elements of type <TYPE>, belonging to a given
     * description, optionally filtered by one or more features
     *
     * @param description
     *            The description which these description elements belong to
     *            (can be null to count all description elements)
     * @param descriptionType
     *            A filter DescriptionElements which belong to of a specific class
     *            of Descriptions
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
     */
    public <T extends DescriptionElementBase> List<T> getDescriptionElements(
            DescriptionBase description, Class<? extends DescriptionBase> descriptionType,
            Set<Feature> features, Class<T> type, boolean includeUnpublished,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns a count of description elements of type <TYPE>, belonging to a
     * given description, optionally filtered by one or more features
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
     *            The type of description
     * @return a count of DescriptionElementBase instances
     */
    public <T extends DescriptionElementBase> long countDescriptionElements(
            DescriptionBase description, Class<? extends DescriptionBase> descriptionType, Set<Feature> features, Class<T> type, boolean includeUnpublished);

    /**
     * Returns a List of TaxonDescription instances, optionally filtered by parameters passed to this method
     *
     * @param taxon The taxon which the description refers to (can be null for all TaxonDescription instances)
     * @param scopes Restrict the results to those descriptions which are scoped by one of the Scope instances passed (can be null or empty)
     * @param geographicalScope Restrict the results to those descriptions which have a geographical scope that overlaps with the NamedArea instances passed (can be null or empty)
     * @param markerTypes Restrict the results to those descriptions which are marked as true by one of the given marker types (can be null or empty)
     * @param descriptionTypes Restrict the results to those descriptions of the given types (can be null or empty)
     * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a List of TaxonDescription instances
     */
    List<TaxonDescription> listTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes, Set<NamedArea> geographicalScope, Set<MarkerType> markerTypes, Set<DescriptionType> descriptionTypes, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns a List of Media instances, optionally filtered by parameters passed to this method.
     * Maybe in future a similar method is implemented in {@link IMediaDao} which allows more
     * media sources to be included.
     *
     * @param taxonUuid The taxon uuid of the taxon which the description refers to (can be null for all TaxonDescription instances)
     * @param restrictToGalleries if true, only returns media from TaxonDescriptions with isImageGallery = true
     * @param markerTypes Restrict the results to those descriptions which are marked as true by one of the given marker types (can be null or empty)
     * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a List of Media instances
     */
    List<Media> listTaxonDescriptionMedia(UUID taxonUuid, Boolean restrictToGalleries, Set<MarkerType> markerTypes, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns a count of Media instances, optionally filtered by parameters passed to this method
     * Maybe in future a similar method is implemented in {@link IMediaDao} which allows more
     * media sources to be included.
     *
     * @param taxonUuid The taxon uuid of the taxon which the description refers to (can be null for all TaxonDescription instances)
     * @param restrictToGalleries if true, only returns media from TaxonDescriptions with isImageGallery = true
     * @param markerTypes Restrict the results to those descriptions which are marked as true by one of the given marker types (can be null or empty)
     * @return a count of Media instances
     */
    int countTaxonDescriptionMedia(UUID taxonUuid, Boolean restrictToGalleries, Set<MarkerType> markerTypes);


    /**
     * Returns a List of TaxonNameDescription instances, optionally filtered by the name which they refer to
     *
     * @param name Restrict the results to those descripDescriptionElementBasetions that refer to a specific name (can be null for all TaxonNameDescription instances)
     * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link IBeanInitializer#initialize(Object, List)}
     * @return a List of TaxonName instances
     */
    List<TaxonNameDescription> getTaxonNameDescriptions(TaxonName name, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns a count of TaxonNameDescription instances, optionally filtered by the name which they refer to
     *
     * @param name Restrict the results to those descriptions that refer to a specific name (can be null for all TaxonNameDescription instances)
     * @return a count of TaxonName instances
     */
    public long countTaxonNameDescriptions(TaxonName name);

    /**
     * @param taxon
     * @param features
     *            Restrict the results to those description elements which are
     *            scoped by one of the Features passed (can be null or empty)
     * @param type A filter for DescriptionElements of a specific class
     * @param pageSize
     * @param pageNumber
     * @param propertyPaths
     * @return the list of matching DescriptionElementBase instances
     */
    <T extends DescriptionElementBase> List<T> getDescriptionElementForTaxon(UUID taxonUuid,
            Set<Feature> features, Class<T> type, boolean includeUnpublished,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Return a list of ids of the specimens attached to a taxon via IndividualsAssociations
     */
    public List<Integer> getIndividualAssociationSpecimenIDs(UUID taxonUuid,
            Set<Feature> features, boolean includeUnpublished,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * @param taxon
     * @param features
     *            Restrict the results to those description elements which are
     *            scoped by one of the Features passed (can be null or empty)
     * @param type A filter for DescriptionElements of a specific class
     * @return the count of matching TaxonDescription instances
     */
    public <T extends DescriptionElementBase> long countDescriptionElementForTaxon(UUID taxonUuid,
            Set<Feature> features, Class<T> type, boolean includeUnpublished);

    /**
     * Method to list all {@link NamedAreas} instances which are currently used
     * by {@link Distribution} elements.
     * @param includeAllParents if set to true all parent areas will be included in the result set
     * @param pageSize
     * @param pageNumber
     *
     * @return
     */
    List<TermDto> listNamedAreasInUse(boolean includeAllParents, Integer pageSize, Integer pageNumber);

    /**
     * @param specimenUuid
     * @param classificationUuid
     * @return
     */
    List<SortableTaxonNodeQueryResult> getNodeOfIndividualAssociationForSpecimen(UUID specimenUuid, UUID classificationUuid);


    DescriptionBaseDto loadDto(UUID descriptionUuid);

    List<DescriptionBaseDto> loadDtos(Set<UUID> descriptionUuids);
}
