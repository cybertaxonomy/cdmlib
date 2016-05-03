/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.jaxb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;

/**
 * @author a.babadshanjan
 * @version 1.0
 */
public class MultilanguageTextAdapter extends XmlAdapter<MultilanguageTextElement, Map<Language, LanguageString>> {

	@Override
	public MultilanguageTextElement marshal(Map<Language, LanguageString> value)
			throws Exception {
		
		MultilanguageTextElement multilanguageTextElement = new MultilanguageTextElement();
		
		for(Language l : value.keySet()) {
			multilanguageTextElement.getLanguageString().add(value.get(l));
		}
		return multilanguageTextElement;
	}

    @Override
	public Map<Language, LanguageString> unmarshal(MultilanguageTextElement value)
			throws Exception {
		
        Map<Language,LanguageString> map = new ConcurrentHashMap<Language, LanguageString>();
		
		for(LanguageString l : value.getLanguageString()) {
			map.put(l.getLanguage(), l);
		}
		return map;
	}
}
