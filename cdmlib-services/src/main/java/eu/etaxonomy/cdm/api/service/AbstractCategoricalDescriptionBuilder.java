package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TextData;

public abstract class AbstractCategoricalDescriptionBuilder extends DescriptionBuilder<CategoricalData>{
	
	public TextData build(CategoricalData data, List<Language> languages) {
		   return doBuild(data.getStates(), languages);
		 }

	protected abstract TextData doBuild(List<StateData> stateDatas, List<Language> languages);

}
