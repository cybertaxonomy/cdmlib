/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.json.processor.value;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

import org.apache.log4j.Logger;
import org.joda.time.Partial;
import org.joda.time.format.ISODateTimeFormat;

import eu.etaxonomy.cdm.hibernate.PartialUserType;

/**
 * Partial time as four digit year, two digit month of year, and two digit day of month (yyyy-MM-dd).
 * @author a.kohlbecker
 * @since 20.01.2008
 * @version 1.0
 */
public class PartialJSONValueProcessor implements JsonValueProcessor {
	private static Logger logger = Logger.getLogger(PartialJSONValueProcessor.class);

	
	/* (non-Javadoc)
	 * @see net.sf.json.processors.JsonValueProcessor#processArrayValue(java.lang.Object, net.sf.json.JsonConfig)
	 */
	public Object processArrayValue(Object object, JsonConfig jsonConfig) {
		if(object == null){
			return "";
		}
		Partial partial = (Partial) object;
		if (logger.isDebugEnabled()) {
			logger.debug("processArrayValue of joda.time.DateTime: " + PartialUserType.partialToString(partial));
		}
        return partial.toString(ISODateTimeFormat.date());
	}

	/* (non-Javadoc)
	 * @see net.sf.json.processors.JsonValueProcessor#processObjectValue(java.lang.String, java.lang.Object, net.sf.json.JsonConfig)
	 */
	public Object processObjectValue(String key, Object object, JsonConfig jsonConfig) {
		if(object == null){
			return "";
		}
		Partial partial = (Partial) object;
		if (logger.isDebugEnabled()) {
			logger.debug("processObjectValue of joda.time.DateTime: " + PartialUserType.partialToString(partial));
		}
		return partial.toString(ISODateTimeFormat.date());
	}
}
