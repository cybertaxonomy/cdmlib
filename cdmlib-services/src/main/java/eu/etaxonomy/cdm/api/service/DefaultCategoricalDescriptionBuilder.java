package eu.etaxonomy.cdm.api.service;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TextData;

public class DefaultCategoricalDescriptionBuilder extends AbstractCategoricalDescriptionBuilder{
	
	protected TextData doBuild(List<StateData> states, List<Language> languages){
		TextData textData = TextData.NewInstance();// TextData that will contain the description and the language corresponding
		StringBuilder CategoricalDescription = new StringBuilder();
		Language language = null;
		for (Iterator<StateData> sd = states.iterator() ; sd.hasNext() ;){
			StateData stateData = sd.next();
			State s = stateData.getState();
			Set<DefinedTerm> modifiers = stateData.getModifiers(); // the states and their according modifiers are simply concatenated one after the other
			for (Iterator<DefinedTerm> mod = modifiers.iterator() ; mod.hasNext() ;){
				DefinedTerm modifier = mod.next();
				CategoricalDescription.append(" " + getRightText(modifier.getPreferredRepresentation(languages)));
			}
			if(s != null) {
				CategoricalDescription.append(" " + getRightText(s.getPreferredRepresentation(languages)));
				if (language==null) {
					language = s.getPreferredRepresentation(languages).getLanguage();
				}
			}
			if (sd.hasNext()) CategoricalDescription.append(separator);
			
		}
		if (language==null) {
			language = Language.DEFAULT();
		}
		textData.putText(language, CategoricalDescription.toString());
		
		return textData;
	}

}
