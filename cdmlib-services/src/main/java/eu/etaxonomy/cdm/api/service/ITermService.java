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
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.TermServiceImpl.TermMovePosition;
import eu.etaxonomy.cdm.api.service.config.TermDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.metadata.TermSearchField;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface ITermService extends IIdentifiableEntityService<DefinedTermBase> {

    /**
     * Returns a term according to it's uri
     */
    public DefinedTermBase getByUri(URI uri);

    public UUID saveLanguageData(LanguageStringBase languageData);

    public List<LanguageString> getAllLanguageStrings(int limit, int start);

    public List<Representation> getAllRepresentations(int limit, int start);

    public Language getLanguageByIso(String iso639);

    public List<Language> getLanguagesByLocale(Enumeration<Locale> locales);

    public NamedArea getAreaByTdwgAbbreviation(String tdwgAbbreviation);

     /**
     * Returns a paged list of Media that represent a given DefinedTerm instance
     *
     * @param definedTerm the definedTerm represented by these media
     * @param pageSize The maximum number of media returned (can be null for all related media)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a Pager of media instances
     */
    public Pager<Media> getMedia(DefinedTermBase definedTerm, Integer pageSize, Integer pageNumber);

    /**
     * Returns a paged list of NamedArea instances (optionally filtered by type or level)
     *
     * @param level restrict the result set to named areas of a certain level (can be null)
     * @param type restrict the result set to named areas of a certain type (can be null)
     * @param pageSize The maximum number of namedAreas returned (can be null for all named areas)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a Pager of named areas
     */
    public Pager<NamedArea> list(NamedAreaLevel level, NamedAreaType type, Integer pageSize, Integer pageNumber,  List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Return a paged list of terms which are specializations of a given definedTerm
     *
     * @param definedTerm The term which is a generalization of the terms returned
     * @param pageSize The maximum number of terms returned (can be null for all specializations)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a Pager of DefinedTerms
     */
    public <T extends DefinedTermBase> Pager<T> getGeneralizationOf(T definedTerm, Integer pageSize, Integer pageNumber);

    /**
     * Return a paged list of distinct terms which include the terms supplied
     *
     * @param definedTerms the set of terms which are part of the terms of interest
     * @param pageSize The maximum number of terms returned (can be null for all terms)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager of DefinedTerms
     */
    public <T extends DefinedTermBase> Pager<T> getPartOf(Set<T> definedTerms, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Return a paged list of terms which are part of the terms supplied
     *
     * @param definedTerms the set of terms which include the terms of interest
     * @param pageSize The maximum number of terms returned (can be null for all terms)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager of DefinedTerms
     */
    public <T extends DefinedTermBase> Pager<T> getIncludes(Collection<T> definedTerms, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Return a paged list of terms which have representations that match the supplied string in the text (description)
     *
     * @param label a string to match (exactly)
     * @param pageSize The maximum number of terms returned (can be null for all terms)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a Pager of DefinedTerms
     */
    public <T extends DefinedTermBase> Pager<T> findByRepresentationText(String label, Class<T> clazz,  Integer pageSize, Integer pageNumber);

    /**
     * Return a paged list of terms which have representations that match the supplied string in the abbreviated label
     *
     * @param label a string to match (exactly)
     * @param pageSize The maximum number of terms returned (can be null for all terms)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a Pager of DefinedTerms
     */
    public <T extends DefinedTermBase> Pager<T> findByRepresentationAbbreviation(String abbrev, Class<T> clazz, Integer pageSize, Integer pageNumber);

    /**
     * Retrieves all {@link DefinedTermBase}s with the given {@link TermType}
     * @param termType the term type to filter the terms
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @return a list containing the terms
     */
    public <T extends DefinedTermBase> List<T> listByTermType(TermType termType, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Delete the given term according to the given delete configuration.
     * In case a problem occurrs while deleting the term the result will reflect this
     * via its status.
     * @param term the term to delete
     * @param config the configurator
     * @return DeleteResult which holds the status of the deletion.
     */
    public DeleteResult delete(DefinedTermBase term, TermDeletionConfigurator config);

    /**
     * Returns the term with the given idInVocabulary for the given vocabulary.
     * If the same idInVocabulary exists with same vocabulary for multiple terms (though this is against the general
     * contract of idInVocabulary) always the same term should be returned.
     *
     * @param id idInVocabulary
     * @param vocabularyUuid uuid of vocabulary
     * @param clazz term clazz filter on certain term classes. May be <code>null</code> for no filter.
     * @return the term
     * @throws IllegalArgumentException if id or vocabularyUuid is <code>null</code>
     * @see #findUuidByIdInVocabulary(String, UUID, Class)
     */
    public <TERM extends DefinedTermBase> TERM findByIdInVocabulary(String id, UUID vocabularyUuid, Class<TERM> clazz)
    	throws IllegalArgumentException;

    /**
     * Returns the UUID of the term with the given idInVocabulary for the given vocabulary.
     * If the same idInVocabulary exists with same vocabulary for multiple terms (though this is against the general
     * contract of idInVocabulary) always the same term should be returned.
     *
     * @param id idInVocabulary
     * @param vocabularyUuid uuid of vocabulary
     * @param clazz term clazz filter on certain term classes. May be <code>null</code> for no filter.
     * @return the term
     * @throws IllegalArgumentException if id or vocabularyUuid is <code>null</code>
     * @see #findByIdInVocabulary(String, UUID, Class)
     */
    public UUID findUuidByIdInVocabulary(String id, UUID vocabularyUuid, Class<? extends DefinedTermBase<?>> clazz)
            throws IllegalArgumentException;

    public DeleteResult delete(UUID termUuid, TermDeletionConfigurator config);

    public DeleteResult delete(List<UUID> termUuids, TermDeletionConfigurator config);

    public DeleteResult deleteTerms(List<DefinedTermBase> terms, TermDeletionConfigurator config);

    public Language getLanguageByLabel(String label);

    public Map<UUID, Representation> saveOrUpdateRepresentations(Collection<Representation> representations);

    /**
     * Returns all terms that are included in the given parent term resp. a part of the given term.
     * @param parentTerm the parent term
     * @return a collection of included terms
     */
    public Collection<TermDto> getIncludesAsDto(TermDto parentTerm);

    /**
     * Returns all terms that the given term is a generalization of resp. that are a kind of the given term
     * @param parentTerm the parent term
     * @return a collection of included terms
     */
    public Collection<TermDto> getKindOfsAsDto(TermDto parentTerm);

    /**
     * Move the given term to the given parent
     * @param termDto the {@link TermDto} of the term to move
     * @param parentUuid the {@link UUID} of the new parent term
     * @param termMovePosition enum to specify the position for {@link DefinedTermBase}s in an {@link OrderedTermVocabulary}
     */
    public UpdateResult moveTerm(TermDto termDto, UUID parentUuid, TermMovePosition termMovePosition);

    /**
     * Move the given term to the given parent
     * @param termDto the {@link TermDto} of the term to move
     * @param parentUuid the {@link UUID} of the new parent term
     */
    public UpdateResult moveTerm(TermDto termDto, UUID parentUuid);

    /**
     * Creates a new term as a child of the given parent.
     * @param termType the {@link TermType} of the term to create
     * @param parentUuid the {@link UUID} of the parent term
     * @param isKindOf if <code>true</code> the term will be added via a
     * kindOf relation. Otherwise it will added via a partOf relation
     * @return the new term
     */
    public TermDto addNewTerm(TermType termType, UUID parentUuid, boolean isKindOf, Language lang);


    /**
     * Returns a collection of {@link TermDto}s that match the given search parameters.
     * @param title  the term label that the terms have to match
     * @param termType the termType that the terms have to match
     * @return a collection of matching term DTOs
     */
    public Collection<TermDto> findByTitleAsDtoWithVocDto(String title, TermType termType);

    /**
     * Returns a collection of {@link TermDto}s that match the given search parameters.
     * @param uri the {@link URI} that the terms have to match
     * @param termLabel  the term label that the terms have to match
     * @param termType the termType that the terms have to match
     * @return a collection of matching term DTOs
     */
    public Collection<TermDto> findByUriAsDto(URI uri, String termLabel, TermType termType);

    /**
     * Returns a list of {@link UuidAndTitleCache} of named areas with titleCache or specified search field
     * matches search parameter
     */
    public <S extends DefinedTermBase> List<UuidAndTitleCache<S>> getUuidAndTitleCache(
            Class<S> clazz, List<? extends TermCollection> vocs,
            Integer limit, String pattern, Language lang, TermSearchField type);

    /**
     * Returns a list of {@link TermDto} of terms with uuid matches one of uuids in list
     * @param uuidList
     * @return
     */
    public Collection<TermDto> findByUUIDsAsDto(List<UUID> uuidList);


    public Collection<TermDto> findFeatureByUUIDsAsDto(List<UUID> uuidList);

    public Collection<TermDto> findFeatureByTitleAsDto(String title);

    public Country getCountryByIso(String iso639);

    public List<Country> getCountryByName(String name);

    /**
     * Returns a map of {@link UUID} and {@link TermDto} of terms with uuid matches one of uuids in list
     */
    public Map<UUID, TermDto> findFeatureByUUIDsAsDtos(List<UUID> uuidList);
}