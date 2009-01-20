package eu.etaxonomy.cdm.model.common.init;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

public interface ITermLoader {
	
	public <T extends DefinedTermBase> TermVocabulary<T> loadTerms(Class<T> clazz, Map<UUID,DefinedTermBase> terms);

}
