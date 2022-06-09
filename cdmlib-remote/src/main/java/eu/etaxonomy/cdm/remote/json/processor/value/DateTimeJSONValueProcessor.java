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
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 * @author n.hoffmann
 * @since 24.07.2008
 */
public class DateTimeJSONValueProcessor implements JsonValueProcessor {

	@SuppressWarnings("unused")
    private static Logger logger = LogManager.getLogger(DateTimeJSONValueProcessor.class);

	private static DateTimeFormatter iso8601Format = ISODateTimeFormat.dateTime();

	@Override
    public Object processArrayValue(Object object, JsonConfig jsonConfig) {
		DateTime dateTime = (DateTime) object;
        return formatDateTime(object);
	}

	@Override
    public Object processObjectValue(String key, Object object,
			JsonConfig jsonConfig) {
	    return formatDateTime(object);
	}

    public Object formatDateTime(Object object) {
        if(object != null){
	        DateTime dateTime = (DateTime) object;
	        // WARNING! null means now!
	        return DateTimeJSONValueProcessor.iso8601Format.print(dateTime);
	    } else {
	        return null;
	    }
    }
}