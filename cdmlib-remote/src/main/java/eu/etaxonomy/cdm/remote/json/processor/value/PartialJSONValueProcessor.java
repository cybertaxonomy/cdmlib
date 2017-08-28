/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.json.processor.value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.hibernate.PartialUserType;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 * Partial time as four digit year, two digit month of year, and two digit day of month (yyyy-MM-dd).
 * @author a.kohlbecker
 * @created 20.01.2008
 * @version 1.0
 */
public class PartialJSONValueProcessor implements JsonValueProcessor {
	private static Logger logger = Logger.getLogger(PartialJSONValueProcessor.class);


	/* (non-Javadoc)
	 * @see net.sf.json.processors.JsonValueProcessor#processArrayValue(java.lang.Object, net.sf.json.JsonConfig)
	 */
	@Override
    public Object processArrayValue(Object object, JsonConfig jsonConfig) {
		if(object == null){
			return "";
		}
		LocalDate partial = (LocalDate) object;
		if (logger.isDebugEnabled()) {
			logger.debug("processArrayValue of ZonedDateTime: " + PartialUserType.partialToString(partial));
		}
        return partial.format(DateTimeFormatter.ISO_DATE_TIME);
	}

	/* (non-Javadoc)
	 * @see net.sf.json.processors.JsonValueProcessor#processObjectValue(java.lang.String, java.lang.Object, net.sf.json.JsonConfig)
	 */
	@Override
    public Object processObjectValue(String key, Object object, JsonConfig jsonConfig) {
		if(object == null){
			return "";
		}
		LocalDate partial = (LocalDate) object;
		if (logger.isDebugEnabled()) {
			logger.debug("processObjectValue of java.time.ZonedDateTime: " + PartialUserType.partialToString(partial));
		}
		return partial.format(DateTimeFormatter.ISO_DATE_TIME);
	}
}
