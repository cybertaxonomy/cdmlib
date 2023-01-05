/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.value;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 * @author a.kohlbecker
 * @since 20.01.2009
 */
public class CalendarJSONValueProcessor implements JsonValueProcessor {

	private static final Logger logger = LogManager.getLogger();

	private static SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

	@Override
    public Object processArrayValue(Object object, JsonConfig jsonConfig) {
		if(object == null){
			return "";
		}
		Calendar calendar = (Calendar) object;
		if (logger.isDebugEnabled()) {
			logger.debug("processArrayValue of java.util.Calendar: " + CalendarJSONValueProcessor.iso8601Format.format(calendar.getTime()));
		}
        return CalendarJSONValueProcessor.iso8601Format.format(calendar.getTime());
	}

	@Override
    public Object processObjectValue(String key, Object object, JsonConfig jsonConfig) {
		if(object == null){
			return "";
		}
		Calendar dateTime = (Calendar) object;
		if (logger.isDebugEnabled()) {
			logger.debug("processObjectValue of java.util.Calendar: " + CalendarJSONValueProcessor.iso8601Format.format(dateTime.getTime()));
		}
        return CalendarJSONValueProcessor.iso8601Format.format(dateTime.getTime());
	}
}