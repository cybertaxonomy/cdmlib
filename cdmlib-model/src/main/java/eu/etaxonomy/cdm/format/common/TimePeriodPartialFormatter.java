/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.format.common;

import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormatter;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * Formatter for {@link TimePeriod} instances.
 * @author a.mueller
 * @since 14-Jul-2013 (formerly inner class in {@link TimePeriod})
 */
public class TimePeriodPartialFormatter extends DateTimeFormatter{

    private final String ENDASH = TimePeriod.SEP;

	public static TimePeriodPartialFormatter NewInstance(){
		return new TimePeriodPartialFormatter();
	}

	protected TimePeriodPartialFormatter(){
		super(null, null);
	}

	@Override
    public String print(ReadablePartial partial){

	    String yearStr = (partial.isSupported(TimePeriod.YEAR_TYPE))? String.valueOf(partial.get(TimePeriod.YEAR_TYPE)):null;
		Month month = (partial.isSupported(TimePeriod.MONTH_TYPE))?
		        Month.valueOf(partial.get(TimePeriod.MONTH_TYPE)):null;
		String monthStr = month != null? month.abbrev():null;
		String dayStr = (partial.isSupported(TimePeriod.DAY_TYPE))? String.valueOf(partial.get(TimePeriod.DAY_TYPE)):null;

		if (dayStr == null && monthStr == null && yearStr == null){
		    return ENDASH;
		}
		if (dayStr != null && monthStr == null && yearStr != null){
		    monthStr = "MMM";
        }

		String result = CdmUtils.concat(" ", dayStr, monthStr, yearStr);

		return result;
	}

}
