/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.value;

import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import eu.etaxonomy.cdm.model.common.LanguageString;

/**
 * @author a.kohlbecker
 * @date 23.06.2010
 *
 */
public class MapJSONValueProcessor implements JsonValueProcessor {

	

	/* (non-Javadoc)
	 * @see net.sf.json.processors.JsonValueProcessor#processArrayValue(java.lang.Object, net.sf.json.JsonConfig)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object processArrayValue(Object value, JsonConfig jsonConfig) {
		if(value instanceof Map){ // TODO move type check into vlaue processor matcher
			Map map= (Map)value;
			if( ! map.isEmpty()){
				
				//Map<String, LanguageString> returnMap = new HashMap<String, LanguageString>(map.size());
				JSONObject json = new JSONObject();
				for (Object val : map.values()){
					if(val instanceof LanguageString){
						//returnMap.put(((LanguageString)val).getLanguageLabel(), (LanguageString) val);
						json.element(((LanguageString)val).getLanguageLabel(), val, jsonConfig);
					} else {
						return JSONObject.fromObject(value, jsonConfig);
					}
						
				}
				return json;
			}
		}
		return JSONObject.fromObject(value, jsonConfig);
	}

	/* (non-Javadoc)
	 * @see net.sf.json.processors.JsonValueProcessor#processObjectValue(java.lang.String, java.lang.Object, net.sf.json.JsonConfig)
	 */
	@Override
	public Object processObjectValue(String key, Object value,
			JsonConfig jsonConfig) {
		return processArrayValue(value, jsonConfig);
	}

}
