package eu.etaxonomy.cdm.api.service;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TextData;

public class MicroFormatCategoricalDescriptionBuilder extends AbstractCategoricalDescriptionBuilder{

	private final String spanEnd = "</span>";

	@Override
    protected TextData doBuild(List<StateData> states, List<Language> languages){
		TextData textData = TextData.NewInstance();// TextData that will contain the description and the language corresponding
		StringBuilder categoricalDescription = new StringBuilder();
		Language language = null;
		for (Iterator<StateData> sd = states.iterator() ; sd.hasNext() ;){
			StateData stateData = sd.next();
			State s = stateData.getState();
			if(s != null && language==null) {
                language = s.getPreferredRepresentation(languages).getLanguage();
            }
            if (language==null) {
                language = Language.DEFAULT();
            }
            if(stateData.getModifyingText()!=null && stateData.getModifyingText().get(language)!=null){
                LanguageString modyfingText = stateData.getModifyingText().get(language);
                categoricalDescription.append(spanClass("modifier") + modyfingText.getText() + spanEnd);
            }
			Set<DefinedTerm> modifiers = stateData.getModifiers(); // the states and their according modifiers are simply written one after the other
			for (Iterator<DefinedTerm> mod = modifiers.iterator() ; mod.hasNext() ;){
				DefinedTerm modifier = mod.next();
				categoricalDescription.append(" " + spanClass("modifier") + modifier.getPreferredRepresentation(languages).getLabel() + spanEnd);
			}
			categoricalDescription.append(" " + spanClass("state") + s.getPreferredRepresentation(languages).getLabel() + spanEnd);
			if (sd.hasNext()) {
                categoricalDescription.append(',');
            }
			if (language==null) {
				language = s.getPreferredRepresentation(languages).getLanguage(); // TODO What if there are different languages ?
			}
		}
		textData.putText(language, categoricalDescription.toString());

		return textData;
	}

	protected String buildFeature(Feature feature, boolean doItBetter){
		if (feature==null || feature.getLabel()==null) {
            return "";
        } else {
			if (doItBetter) {
				String betterString = StringUtils.substringBefore(feature.getLabel(), "<");
				return (spanClass("feature") + StringUtils.removeEnd(betterString, " ") + spanEnd);
			} else {
                return (spanClass("feature") + feature.getLabel() + spanEnd);
            }
		}
	}

	private String spanClass(String classString){
		return("<span class=\""+classString+"\">");
	}
}
