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

import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 * @author a.kohlbecker
 * @since 23.06.2010
 */
public class MapJSONValueProcessor implements JsonValueProcessor {

	@Override
	public Object processArrayValue(Object value, JsonConfig jsonConfig) {

    	Map<?,?> map= (Map<?,?>)value;
		if( ! map.isEmpty()){
		    JSONObject json = new JSONObject();
		    Object firstKey = map.keySet().iterator().next();
		    if(firstKey instanceof TypedEntityReference){
		        for(Object key : map.keySet()){
		            json.element(key.toString(), map.get(key), jsonConfig);
		        }
		    } else {
				for (Object val : map.values()){
					if(val instanceof LanguageString){
						json.element(((LanguageString)val).getLanguageLabel(), val, jsonConfig);
					} else {
						return JSONObject.fromObject(value, jsonConfig);
					}
				}
		    }
			return json;
		}
		return JSONObject.fromObject(value, jsonConfig);
	}

	@Override
	public Object processObjectValue(String key, Object value,
			JsonConfig jsonConfig) {
		return processArrayValue(value, jsonConfig);
	}
}