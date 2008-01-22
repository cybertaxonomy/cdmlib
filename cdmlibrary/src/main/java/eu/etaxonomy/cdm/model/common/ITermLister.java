package eu.etaxonomy.cdm.model.common;

import java.util.List;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;

public interface ITermLister {

		public abstract List<DefinedTermBase> listTerms();
	
}
