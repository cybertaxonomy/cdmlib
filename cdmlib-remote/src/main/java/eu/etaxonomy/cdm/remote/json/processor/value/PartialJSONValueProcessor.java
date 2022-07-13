/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.value;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Partial;
import org.joda.time.format.ISODateTimeFormat;

import eu.etaxonomy.cdm.hibernate.PartialUserType;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 * Partial time as four digit year, two digit month of year, and two digit day of month (yyyy-MM-dd).
 *
 * @author a.kohlbecker
 * @since 20.01.2008
 */
public class PartialJSONValueProcessor implements JsonValueProcessor {

    private static Logger logger = LogManager.getLogger(PartialJSONValueProcessor.class);

	@Override
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

	@Override
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
