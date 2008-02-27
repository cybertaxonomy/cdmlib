package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

public interface ITermService extends IService<DefinedTermBase> {

	public abstract DefinedTermBase getTermByUri(String uri);
	
	public abstract List<DefinedTermBase> listTerms(UUID vocabularyUuid);

	public abstract List<TermVocabulary> listVocabularies(Class termClass);

}
