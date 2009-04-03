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
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

public interface ITermService extends IService<DefinedTermBase> {

	public DefinedTermBase getTermByUri(String uri);

	// FIXME candidate for harmonization?
	public DefinedTermBase getTermByUuid(UUID uuid);

	/**
	 * FIXME candidate for harmonization?
	 * @param limit
	 * @param start
	 * @return
	 */
	public List<DefinedTermBase> getAllDefinedTerms(int limit, int start);

	public TermVocabulary getVocabulary(UUID vocabularyUuid);
	
	public Set<TermVocabulary> listVocabularies(Class termClass);

	/**
	 * Returns Language Vocabulary
	 * @return
	 */
	public TermVocabulary<Language> getLanguageVocabulary();

	public UUID saveTermVocabulary(TermVocabulary termVocabulary);

	/**
	 * FIXME candidate for harmonization?
	 * @param termBase
	 * @return
	 */
	public UUID saveTerm(DefinedTermBase termBase);

	/**
	 * FIXME candidate for harmonization?
	 * @param termBaseCollection
	 * @return
	 */
	public Map<UUID, DefinedTermBase> saveTermsAll(Collection<? extends DefinedTermBase> termBaseCollection);

	public List<TermVocabulary<DefinedTermBase>> getAllTermVocabularies(int limit, int start);

	public Map<UUID, TermVocabulary<DefinedTermBase>> 
	       saveTermVocabulariesAll(Collection<TermVocabulary<DefinedTermBase>> termVocabularies);

	public List<Representation> getAllRepresentations(int limit, int start);
	
	public Map<UUID, LanguageStringBase> saveLanguageDataAll(Collection<LanguageStringBase> languageData);
	
	public UUID saveLanguageData(LanguageStringBase languageData);
	
	public List<LanguageString> getAllLanguageStrings(int limit, int start);
	
	public Map<UUID, LanguageStringBase> 
	       saveLanguageStringBasesAll(Collection<LanguageStringBase> languageStringBases);
	
	public Language getLanguageByIso(String iso639);
	
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
	public Pager<NamedArea> list(NamedAreaLevel level, NamedAreaType type, Integer pageSize, Integer pageNumber);
	
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
	 * @return a Pager of DefinedTerms
	 */
	public <T extends DefinedTermBase> Pager<T> getPartOf(Set<T> definedTerms, Integer pageSize, Integer pageNumber);
	
	/**
	 * Return a paged list of terms which are part of the terms supplied
	 * 
	 * @param definedTerms the set of terms which include the terms of interest 
	 * @param pageSize The maximum number of terms returned (can be null for all terms)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager of DefinedTerms
	 */
	public <T extends DefinedTermBase> Pager<T> getIncludes(Set<T> definedTerms, Integer pageSize, Integer pageNumber);
}
