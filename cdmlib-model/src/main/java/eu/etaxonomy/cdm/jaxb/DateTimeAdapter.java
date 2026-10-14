/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.jaxb;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author a.mueller
 * @since 23.07.2008
 */
public class DateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {

    private static final Logger logger = LogManager.getLogger();

	@Override
	public String marshal(ZonedDateTime dateTime) throws Exception {
		if (logger.isDebugEnabled()){logger.debug("marshal");}
		if(dateTime == null) {
			return null;
		} else {
//		    DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime();
		    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
		    return dateTimeFormatter.format(dateTime);
		}
	}

	@Override
	public ZonedDateTime unmarshal(String value) throws Exception {
		if (logger.isDebugEnabled()){logger.debug("unmarshal");}
//		return ISODateTimeFormat.dateTimeParser().parseDateTime(value);
		return ZonedDateTime.parse(value);
//		return ZonedDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC));
	}
}
