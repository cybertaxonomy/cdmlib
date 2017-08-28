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
import java.time.temporal.Temporal;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;


/**
 * @author a.mueller
 * @created 23.07.2008
 * @version 1.0
 */
public class DateTimeAdapter extends XmlAdapter<String, Temporal> {
	private static final Logger logger = Logger.getLogger(DateTimeAdapter.class);

	@Override
	public String marshal(Temporal dateTime) throws Exception {
		if (logger.isDebugEnabled()){logger.debug("marshal");}
		if(dateTime == null) {
			return null;
		} else {
		    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
		    if (dateTime instanceof ZonedDateTime){
		        return ((ZonedDateTime)dateTime).format(dateTimeFormatter);
		    }
		    return null;
		}
	}

	@Override
	public Temporal unmarshal(String value) throws Exception {
		if (logger.isDebugEnabled()){logger.debug("unmarshal");}
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
		Temporal dateTime = (Temporal) formatter.parse(value);
		return dateTime;
	}
}
