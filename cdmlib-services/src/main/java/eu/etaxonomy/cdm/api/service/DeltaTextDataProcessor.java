/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.TextData;

/**
 * @author m.venin
 * @since 2010-12-02
 */
public class DeltaTextDataProcessor implements INaturalLanguageTextDataProcessor {

	@Override
	public void process(TextData textData, TextData previousTextData) {
		boolean doItBetter = false;

		Map<Language,LanguageString> previousMultiLanguageText = previousTextData.getMultilanguageText();
		Map<Language,LanguageString> multiLanguageText = textData.getMultilanguageText();

		for (Language language : multiLanguageText.keySet()){
			LanguageString langString = multiLanguageText.get(language);
			String text = langString.getText();

			LanguageString tmpMultilangText = previousMultiLanguageText.get(language);
			if( tmpMultilangText != null){
				text = StringUtils.remove(text, tmpMultilangText.getText());
			}

			if (doItBetter) { //TODO remove the text between brackets
				StringBuilder strbuilder = new StringBuilder();
				do	{
					strbuilder.append(StringUtils.substringBefore(text, "<"));
				}
				while (!(text=StringUtils.substringAfter(text, ">")).equals(""));
				StringUtils.substringBeforeLast(strbuilder.toString()," ");
			}
			else{
				text=StringUtils.replaceChars(text,"<>","");
			}

			textData.removeText(language);
			textData.putText(language, text);
		}
	}
}
