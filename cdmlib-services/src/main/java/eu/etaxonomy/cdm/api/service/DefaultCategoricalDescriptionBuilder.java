package eu.etaxonomy.cdm.api.service;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Modifier;
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
			Set<Modifier> modifiers = stateData.getModifiers(); // the states and their according modifiers are simply written one after the other
			for (Iterator<Modifier> mod = modifiers.iterator() ; mod.hasNext() ;){
				Modifier modifier = mod.next();
				CategoricalDescription.append(" " + modifier.getPreferredRepresentation(languages).getLabel());
			}
			CategoricalDescription.append(" " + s.getPreferredRepresentation(languages).getLabel());
			if (language==null) {
				language = s.getPreferredRepresentation(languages).getLanguage(); // TODO What if there are different languages ?
			}
		}
		if (language==null) {
			language = Language.DEFAULT();
		}
		textData.putText(CategoricalDescription.toString(), language);
		
		return textData;
	}

}
