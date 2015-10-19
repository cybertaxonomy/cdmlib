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

import java.net.URI;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.config.TermDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface ITermService extends IIdentifiableEntityService<DefinedTermBase> {

    /**
     * Returns a term according to it's uri
     * @param uri
     * @return
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
    public List<DefinedTermBase<?>> listByTermType(TermType termType, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

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
     * @param id idInVocabulary
     * @param vocabularyUuid uuid of vocabulary
     * @param clazz term clazz filter on certain term classes. May be <code>null</code> for no filter.
     * @return the term
     * @throws IllegalArgumentException if id or vocabularyUuid is <code>null</code>
     */
    public <TERM extends DefinedTermBase> TERM findByIdInVocabulary(String id, UUID vocabularyUuid, Class<TERM> clazz)
    	throws IllegalArgumentException;

    /**
     * @param termUuid
     * @param config
     * @return
     */
    public DeleteResult delete(UUID termUuid, TermDeletionConfigurator config);

    /**
     * @param label
     * @return
     */
    public Language getLanguageByLabel(String label);
}
