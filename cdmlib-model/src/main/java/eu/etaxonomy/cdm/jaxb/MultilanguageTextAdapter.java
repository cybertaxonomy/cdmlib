/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.jaxb;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;

/**
 * @author a.babadshanjan
 * @version 1.0
 */
public class MultilanguageTextAdapter extends XmlAdapter<MultilanguageTextHelper, Map<Language, LanguageString>> {

	@Override
	public MultilanguageTextHelper marshal(Map<Language, LanguageString> value)
			throws Exception {
		
		MultilanguageTextHelper multilanguageTextHelper = new MultilanguageTextHelper();
		
		for(Language language : value.keySet()) {
			multilanguageTextHelper.setLanguage(language);
			multilanguageTextHelper.setLanguageString(value.get(language));
		}

		return multilanguageTextHelper;
	}

    @Override
	public Map<Language, LanguageString> unmarshal(MultilanguageTextHelper value)
			throws Exception {
		
		Map<Language, LanguageString> map = new HashMap<Language, LanguageString>();
		
		map.put(value.getLanguage(), value.getLanguageString());
		
		return map;
	}
}
