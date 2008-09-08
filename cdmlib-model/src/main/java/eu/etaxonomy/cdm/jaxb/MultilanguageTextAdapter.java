/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author a.babadshanjan
 * @created 02.09.2008
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;

public class MultilanguageTextAdapter extends XmlAdapter<MultilanguageText, java.util.Map<Language, LanguageString>> {

	@Override
	public MultilanguageText marshal(Map<Language, LanguageString> value)
			throws Exception {
		
		MultilanguageText multilanguageText = new MultilanguageText();

		for(Language language : value.keySet()) {
			multilanguageText.add(value.get(language));
		}

		return multilanguageText;
	}

    @Override
	public Map<Language, LanguageString> unmarshal(MultilanguageText value)
			throws Exception {
		
//		Map<Language, LanguageString> map = new ConcurrentHashMap<Language, LanguageString>();
//		
//		for(Language language : value.keySet()) {
//			map.put(language, value.get(language));
//		}
//		
//		return map;
    	
    	return value;
	}
}
