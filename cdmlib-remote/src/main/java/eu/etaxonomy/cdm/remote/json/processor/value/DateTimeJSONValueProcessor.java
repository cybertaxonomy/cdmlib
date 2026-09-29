/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.value;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 * @author n.hoffmann
 * @since 24.07.2008
 */
public class DateTimeJSONValueProcessor implements JsonValueProcessor {

	private static final DateTimeFormatter ISO8601_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	@Override
    public Object processArrayValue(Object object, JsonConfig jsonConfig) {
		ZonedDateTime dateTime = (ZonedDateTime) object;
        return formatDateTime(dateTime);
	}

	@Override
    public Object processObjectValue(String key, Object object,
			JsonConfig jsonConfig) {
	    return formatDateTime((ZonedDateTime)object);
	}

    Object formatDateTime(ZonedDateTime object) {
        if(object != null){
	        ZonedDateTime dateTime = object;
	        // WARNING! null means now!
	        return ISO8601_FORMAT.format(dateTime);
	    } else {
	        return null;
	    }
    }
}