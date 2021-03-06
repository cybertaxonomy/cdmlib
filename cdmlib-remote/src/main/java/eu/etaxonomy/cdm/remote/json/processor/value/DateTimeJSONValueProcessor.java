/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.json.processor.value;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 * @author n.hoffmann
 * @since 24.07.2008
 * @version 1.0
 */
public class DateTimeJSONValueProcessor implements JsonValueProcessor {
	private static Logger logger = Logger
			.getLogger(DateTimeJSONValueProcessor.class);

	private static DateTimeFormatter iso8601Format = ISODateTimeFormat.dateTime();

	/* (non-Javadoc)
	 * @see net.sf.json.processors.JsonValueProcessor#processArrayValue(java.lang.Object, net.sf.json.JsonConfig)
	 */
	@Override
    public Object processArrayValue(Object object, JsonConfig jsonConfig) {
		DateTime dateTime = (DateTime) object;
        return formatDateTime(object);
	}

	/* (non-Javadoc)
	 * @see net.sf.json.processors.JsonValueProcessor#processObjectValue(java.lang.String, java.lang.Object, net.sf.json.JsonConfig)
	 */
	@Override
    public Object processObjectValue(String key, Object object,
			JsonConfig jsonConfig) {
	    return formatDateTime(object);
	}

    /**
     * @param object
     * @return
     */
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
