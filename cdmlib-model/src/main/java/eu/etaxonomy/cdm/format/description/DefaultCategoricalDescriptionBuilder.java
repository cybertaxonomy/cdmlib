/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;

public class DefaultCategoricalDescriptionBuilder extends CategoricalDescriptionBuilderBase{

	@Override
    protected TextData doBuild(List<StateData> states, List<Language> languages){
		TextData textData = TextData.NewInstance();// TextData that will contain the description and the language corresponding
		StringBuilder categoricalDescription = new StringBuilder();
		Language language = null;
		for (Iterator<StateData> sd = states.iterator() ; sd.hasNext() ;){
			StateData stateData = sd.next();
			DefinedTermBase<?> state = stateData.getState();
			if(state != null && language == null) {
			    language = state.getPreferredRepresentation(languages).getLanguage();
			}
			if (language == null) {
			    language = Language.DEFAULT();
			}
			if(stateData.getModifyingText()!=null && stateData.getModifyingText().get(language)!=null){
			    LanguageString modyfingText = stateData.getModifyingText().get(language);
			    categoricalDescription.append(modyfingText.getText());
			}
			Set<DefinedTerm> modifiers = stateData.getModifiers(); // the states and their according modifiers are simply concatenated one after the other
			for (Iterator<DefinedTerm> mod = modifiers.iterator() ; mod.hasNext() ;){
				DefinedTerm modifier = mod.next();
				categoricalDescription.append(" " + getRightText(modifier.getPreferredRepresentation(languages), false));
			}
			if(state != null){
			    categoricalDescription.append(" " + getRightText(state.getPreferredRepresentation(languages), stateData.isUsePlural()));
			}
			if(stateData.getCount()!=null){
			    categoricalDescription.append(" ("+stateData.getCount()+")");
			}
			if (sd.hasNext()) {
                categoricalDescription.append(separator);
            }

		}
		textData.putText(language, categoricalDescription.toString());

		return textData;
	}
}