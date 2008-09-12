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
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

public interface ITermService extends IService<DefinedTermBase> {

	public abstract DefinedTermBase getTermByUri(String uri);

	public abstract DefinedTermBase getTermByUuid(UUID uuid);

	/**
	 * @param limit
	 * @param start
	 * @return
	 */
	public abstract List<DefinedTermBase> getAllDefinedTerms(int limit, int start);

	public abstract TermVocabulary getVocabulary(UUID vocabularyUuid);
	
	public abstract Set<TermVocabulary> listVocabularies(Class termClass);


	/**
	 * @param termBase
	 * @return
	 */
	public abstract UUID saveTerm(DefinedTermBase termBase);

	public Map<UUID, DefinedTermBase> saveTermsAll(Collection<DefinedTermBase> termBaseCollection);

	public List<TermVocabulary<DefinedTermBase>> getAllTermVocabularies(int limit, int start);

	public Map<UUID, TermVocabulary<DefinedTermBase>> 
	       saveTermVocabulariesAll(Collection<TermVocabulary<DefinedTermBase>> termVocabularies);

	public Map<UUID, Representation> saveRepresentationsAll(Collection<Representation> representations);
	
	public void saveLanguageDataAll(Collection<VersionableEntity> languageData);

	public List<Representation> getAllRepresentations(int limit, int start);
		
}
