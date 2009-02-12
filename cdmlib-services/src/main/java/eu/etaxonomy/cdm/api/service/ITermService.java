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

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

public interface ITermService extends IService<DefinedTermBase> {

	public abstract DefinedTermBase getTermByUri(String uri);

	// FIXME candidate for harmonization?
	public abstract DefinedTermBase getTermByUuid(UUID uuid);

	/**
	 * FIXME candidate for harmonization?
	 * @param limit
	 * @param start
	 * @return
	 */
	public abstract List<DefinedTermBase> getAllDefinedTerms(int limit, int start);

	public abstract TermVocabulary getVocabulary(UUID vocabularyUuid);
	
	public abstract Set<TermVocabulary> listVocabularies(Class termClass);


	/**
	 * FIXME candidate for harmonization?
	 * @param termBase
	 * @return
	 */
	public abstract UUID saveTerm(DefinedTermBase termBase);

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

}
