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
import eu.etaxonomy.cdm.model.common.MultilanguageSet;

public class MultilanguageSetAdapter extends XmlAdapter<MultilanguageSet, java.util.Map<Language, LanguageString>> {

	@Override
	public MultilanguageSet marshal(Map<Language, LanguageString> value)
			throws Exception {
		
		MultilanguageSet multilanguageSet = new MultilanguageSet();

		for(Language language : value.keySet()) {
			multilanguageSet.add(value.get(language));
		}

		return multilanguageSet;
	}

    @Override
	public Map<Language, LanguageString> unmarshal(MultilanguageSet value)
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
