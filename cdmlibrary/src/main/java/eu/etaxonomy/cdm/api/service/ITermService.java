package eu.etaxonomy.cdm.api.service;

import java.util.List;

import javax.persistence.Transient;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

public interface ITermService extends IService<DefinedTermBase>{

	public abstract DefinedTermBase getTermByUri(String uri);
	
	public abstract List<DefinedTermBase> listTerms(String vocabularyUuid);

	public abstract List<TermVocabulary> listVocabularies(Class termClass);

}
