/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.term;

import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.metadata.NamedAreaSearchField;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IDefinedTermDao
        extends IIdentifiableDao<DefinedTermBase>, ITitledDao<DefinedTermBase>{

	/**
	 * @param iso639 a two or three letter language code according to iso639-1 or iso639-2
	 * @return the Language or null
	 */
	//TODO refactor typo:
	public Language getLanguageByIso(String iso639);

	public List<Language> getLanguagesByIso(List<String> iso639List);

	public List<Language> getLanguagesByLocale(Enumeration<Locale> locales);

	 /**
     * Returns the country with the isoCode iso639, works only with string length 2 or 3
     *
     * @param iso639 the isoCode of the searched country
     *
     * @return country with isoCode iso639
     */
	public Country getCountryByIso(String iso639);

	public <TYPE extends DefinedTermBase> List<TYPE> getDefinedTermByRepresentationText(String text, Class<TYPE> clazz );

	public <TYPE extends DefinedTermBase> List<TYPE> getDefinedTermByRepresentationText(String text, Class<TYPE> clazz, Integer pageSize,Integer  pageNumber);

	public long countDefinedTermByRepresentationText(String text, Class<? extends DefinedTermBase> clazz);

	public <TYPE extends DefinedTermBase> List<TYPE> getDefinedTermByRepresentationAbbrev(String text, Class<TYPE> clazz, Integer pageSize,Integer  pageNumber);

	public long countDefinedTermByRepresentationAbbrev(String text, Class<? extends DefinedTermBase> clazz);

    /**
     * Returns a List of Media that represent a given DefinedTerm instance
     *
	 * @param definedTerm the definedTerm represented by these media
	 * @param pageSize The maximum number of media returned (can be null for all related media)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a List of media instances
     */
	public List<Media> getMedia(DefinedTermBase definedTerm, Integer pageSize, Integer pageNumber);

	/**
	 * Returns a count of the Media that represent a given
	 * DefinedTermBase instance
	 *
	 * @param definedTerm the definedTerm represented by these media
	 * @return a count of Media entities
	 */
	public long countMedia(DefinedTermBase definedTerm);

	/**
	 * Returns a List of NamedArea instances (optionally filtered by type or level)
	 *
	 * @param level restrict the result set to named areas of a certain level (can be null)
	 * @param type restrict the result set to named areas of a certain type (can be null)
	 * @param pageSize The maximum number of namedAreas returned (can be null for all named areas)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of named areas
	 */
	public List<NamedArea> list(NamedAreaLevel level, NamedAreaType type, Integer pageSize, Integer pageNumber);

	/**
	 * @param level
	 * @param type
	 * @param pageSize
	 * @param pageNumber
	 * @param orderHints
	 * @param propertyPaths
	 * @return
	 */
	public List<NamedArea> list(NamedAreaLevel level, NamedAreaType type, Integer pageSize, Integer pageNumber,  List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Returns a count of NamedArea instances (optionally filtered by type or level)
	 *
	 * @param level restrict the result set to named areas of a certain level (can be null)
	 * @param type restrict the result set to named areas of a certain type (can be null)
	 * @return a count of named areas
	 */
	public long count(NamedAreaLevel level, NamedAreaType type);

	/**
	 * Return a list of terms which are specializations of a given definedTerm
	 *
	 * @param definedTerm The term which is a generalization of the terms returned
	 * @param pageSize The maximum number of terms returned (can be null for all specializations)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of DefinedTerms
	 */
	public <T extends DefinedTermBase> List<T> getGeneralizationOf(T definedTerm, Integer pageSize, Integer pageNumber);

	/**
	 * Return a count of terms which are specializations of a given definedTerm
	 *
	 * @param definedTerm The term which is a generalization of the terms returned
	 * @return a count of DefinedTerms
	 */
	public <T extends DefinedTermBase> long countGeneralizationOf(T definedTerm);

	/**
	 * Return a List of distinct terms which include the terms supplied
	 *
	 * @param definedTerms the set of terms which are part of the terms of interest
	 * @param pageSize The maximum number of terms returned (can be null for all terms)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a List of DefinedTerms
	 */
	public <T extends DefinedTermBase> List<T> getPartOf(Set<T> definedTerms, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

	/**
	 * Return a count of distinct terms which include the terms supplied
	 *
	 * @param definedTerms the set of terms which are part of the terms of interest
	 * @return a count of DefinedTerms
	 */
	public <T extends DefinedTermBase> long countPartOf(Set<T> definedTerms);

	/**
	 * Return a List of terms which are part of the terms supplied
	 *
	 * @param definedTerms the collection of terms which include the terms of interest
	 * @param pageSize The maximum number of terms returned (can be null for all terms)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a List of DefinedTerms
	 */
	public <T extends DefinedTermBase> List<T> getIncludes(Collection<T> definedTerms, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

	/**
	 * Return a count of terms which are part of the terms supplied
	 *
	 * @param definedTerms the set of terms which include the terms of interest
	 * @return a count of DefinedTerms
	 */
	public <T extends DefinedTermBase> long countIncludes(Collection<T> definedTerms);

	public DefinedTermBase findByUri(URI uri);

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

	public <TERM extends DefinedTermBase> List<TERM> listByTermClass(Class<TERM> clazz, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Returns a term or a list of terms depending of the label/id used in its vocabulary.
	 * @param idInVoc
	 * @param vocUuid
	 * @param clazz
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 */
	public <TERM extends DefinedTermBase> List<TERM> getDefinedTermByIdInVocabulary(String idInVoc, UUID vocUuid, Class<TERM> clazz, Integer pageSize, Integer pageNumber);

    public List<NamedArea> listNamedArea(List<TermVocabulary> vocs, Integer limit,
            String pattern);

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
     * Returns a collection of {@link TermDto}s that match the given search parameters.
     * @param title  the term label that the terms have to match
     * @param termType the termType that the terms have to match
     * @return a collection of matching term DTOs
     */
    public Collection<TermDto> findByTitleAsDto(String title, TermType termType);

    /**
     * Returns a collection of {@link TermDto}s that match the given search parameters.
     * @param uri the {@link URI} that the terms have to match
     * @param termLabel  the term label that the terms have to match
     * @param termType the termType that the terms have to match
     * @return a collection of matching term DTOs
     */
    public Collection<TermDto> findByUriAsDto(URI uri, String termLabel, TermType termType);

    /**
     * Returns the number of terms in the vocabularies vocs filteres by the given pattern
     * @param vocs
     * @param pattern
     * @return
     */
    public long count(List<TermVocabulary> vocs, String pattern);

    /**
     * @param vocs
     * @param pageNumber
     * @param limit
     * @param pattern
     * @return
     */
    public List<NamedArea> listNamedArea(List<TermVocabulary> vocs, Integer pageNumber, Integer limit, String pattern, MatchMode matchMode);

    /**
     * @param vocs
     * @param limit
     * @param pattern
     * @return
     */
    public List<NamedArea> listNamedAreaByAbbrev(List<TermVocabulary> vocs, Integer pageNumber, Integer limit, String pattern, MatchMode matchmode, NamedAreaSearchField type);

    /**
     * @param vocs
     * @param limit
     * @param pattern
     * @return
     */
    public List<NamedArea> listNamedAreaByAbbrev(List<TermVocabulary> vocs, Integer limit, String pattern, NamedAreaSearchField type);

    /**
     * Returns all states for all supportedCategoricalEnumeration of this categorical feature
     * @param featureUuid the feature which has to support categorical data
     * @return list of all supported states
     */
    public List<TermDto> getSupportedStatesForFeature(UUID featureUuid);

    public Collection<TermDto> findByUUIDsAsDto(List<UUID> uuidList);

    public Collection<TermDto> findByTypeAsDto(TermType termType);

    public Collection<TermDto> findFeatureByUUIDsAsDto(List<UUID> uuidList);

    public Collection<TermDto> findFeatureByTitleAsDto(String pattern);

}
