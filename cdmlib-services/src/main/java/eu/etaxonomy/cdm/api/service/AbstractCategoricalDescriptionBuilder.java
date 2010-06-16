package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TextData;

public abstract class AbstractCategoricalDescriptionBuilder extends DescriptionBuilder<CategoricalData>{
	
	public TextData build(CategoricalData data) {
		   return doBuild(data.getStates());
		 }

	protected abstract TextData doBuild(List<StateData> stateDatas);

}
