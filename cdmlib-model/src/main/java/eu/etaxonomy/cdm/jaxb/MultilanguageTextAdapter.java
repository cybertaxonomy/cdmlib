/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.jaxb;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;

public class MultilanguageTextAdapter extends XmlAdapter<MultilanguageTextHelper, Map<Language, LanguageString>> {
//public class MultilanguageTextAdapter extends XmlAdapter<MultilanguageText, Map<Language, LanguageString>> {

	@Override
	public MultilanguageTextHelper marshal(Map<Language, LanguageString> value)
			throws Exception {
		
//    	MultilanguageText multilanguageText = new MultilanguageText();
//    	multilanguageText.putAll(value);
//    	

		MultilanguageTextHelper multilanguageTextHelper = new MultilanguageTextHelper();
		
		for(Language language : value.keySet()) {
			multilanguageTextHelper.setLanguage(language);
			multilanguageTextHelper.setLanguageString(value.get(language));
			
//			multilanguageText.add(value.get(language));
		}

		return multilanguageTextHelper;
	}

    @Override
	public Map<Language, LanguageString> unmarshal(MultilanguageTextHelper value)
//	public Map<Language, LanguageString> unmarshal(MultilanguageText value)
			throws Exception {
		
		Map<Language, LanguageString> map = new HashMap<Language, LanguageString>();
		
		map.put(value.getLanguage(), value.getLanguageString());
		
		return map;
	}
}
