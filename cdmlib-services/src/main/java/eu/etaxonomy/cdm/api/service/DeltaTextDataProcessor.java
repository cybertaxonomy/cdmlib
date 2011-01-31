package eu.etaxonomy.cdm.api.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.common.Language;

import eu.etaxonomy.cdm.model.common.LanguageString;

public class DeltaTextDataProcessor implements INaturalLanguageTextDataProcessor {

	@Override
	public void process(TextData textData, TextData previousTextData) {
		boolean doItBetter = false;
		
		Map<Language,LanguageString> oldMultiLanguageText = previousTextData.getMultilanguageText();
		Map<Language,LanguageString> multiLanguageText = textData.getMultilanguageText();
		
		for (Language language : multiLanguageText.keySet()){
			LanguageString langString = multiLanguageText.get(language);
			String oldText = langString.getText();
			
			oldText = StringUtils.remove(oldText,oldMultiLanguageText.get(language).getText());
			
			if (doItBetter) { //TODO remove the text between brackets
				StringBuilder strbuilder = new StringBuilder();
				do	{
					strbuilder.append(StringUtils.substringBefore(oldText, "<"));
				}
				while (!(oldText=StringUtils.substringAfter(oldText, ">")).equals(""));
				StringUtils.substringBeforeLast(strbuilder.toString()," ");
			}
			else{
				oldText=StringUtils.replaceChars(oldText,"<>","");
			}
			
			textData.removeText(language);
			textData.putText(language, oldText);
			
		}
		
	}

}
