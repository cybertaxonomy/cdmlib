/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author a.mueller
 * @since 23.07.2008
 * @version 1.0
 */
public class DateTimeAdapter extends XmlAdapter<String, DateTime> {
	private static final Logger logger = Logger.getLogger(DateTimeAdapter.class);

	@Override
	public String marshal(DateTime dateTime) throws Exception {
		if (logger.isDebugEnabled()){logger.debug("marshal");}
		if(dateTime == null) {
			return null;
		} else {
		    DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime();
		    return dateTimeFormatter.print(dateTime);
		}
	}

	@Override
	public DateTime unmarshal(String value) throws Exception {
		if (logger.isDebugEnabled()){logger.debug("unmarshal");}
		return ISODateTimeFormat.dateTimeParser().parseDateTime(value);
	}
}
