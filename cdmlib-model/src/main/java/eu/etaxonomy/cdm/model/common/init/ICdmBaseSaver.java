package eu.etaxonomy.cdm.model.common.init;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

public interface ICdmBaseSaver {
	
	public void saveOrUpdate(TermVocabulary<DefinedTermBase> vocabulary);
}
