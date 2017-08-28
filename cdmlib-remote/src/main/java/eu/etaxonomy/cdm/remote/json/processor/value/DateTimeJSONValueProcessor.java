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

import org.apache.log4j.Logger;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;


/**
 * @author n.hoffmann
 * @created 24.07.2008
 * @version 1.0
 */
public class DateTimeJSONValueProcessor implements JsonValueProcessor {
	private static Logger logger = Logger
			.getLogger(DateTimeJSONValueProcessor.class);

	private static DateTimeFormatter iso8601Format = DateTimeFormatter.ISO_DATE_TIME;

	/* (non-Javadoc)
	 * @see net.sf.json.processors.JsonValueProcessor#processArrayValue(java.lang.Object, net.sf.json.JsonConfig)
	 */
	@Override
    public Object processArrayValue(Object object, JsonConfig jsonConfig) {
		ZonedDateTime dateTime = (ZonedDateTime) object;
        return dateTime.format(iso8601Format);
	}

	/* (non-Javadoc)
	 * @see net.sf.json.processors.JsonValueProcessor#processObjectValue(java.lang.String, java.lang.Object, net.sf.json.JsonConfig)
	 */
	@Override
    public Object processObjectValue(String key, Object object,
			JsonConfig jsonConfig) {
		ZonedDateTime dateTime = (ZonedDateTime) object;
        return dateTime.format(iso8601Format);
	}
}
