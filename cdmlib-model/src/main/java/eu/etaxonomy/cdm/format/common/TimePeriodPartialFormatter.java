/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.common;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.Partial;
import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * Formatter for {@link TimePeriod} instances.
 * @author a.mueller
 * @since 14-Jul-2013 (formerly inner class in {@link TimePeriod})
 */
public class TimePeriodPartialFormatter {

    private final String ENDASH = TimePeriod.SEP;

    private static TimePeriodPartialFormatter instance = new TimePeriodPartialFormatter();

    public static TimePeriodPartialFormatter INSTANCE(){
        return instance;
    }

    public static TimePeriodPartialFormatter NewInstance(){
		return new TimePeriodPartialFormatter();
	}

	protected TimePeriodPartialFormatter(){
	}

    public String print(Partial partial){

	    String yearStr = partial.hasYear() ? String.valueOf(partial.getYear()) : null;
		Month month = partial.hasMonth() ? Month.valueOf(partial.getMonth()) : null;
		String monthStr = month != null? month.abbrev():null;
		String dayStr = partial.hasDay() ? String.valueOf(partial.getDay()) : null;

		if (dayStr == null && monthStr == null && yearStr == null){
		    return ENDASH;
		}
		if (dayStr != null && monthStr == null && yearStr != null){
		    monthStr = "MMM";
        }

		String result = CdmUtils.concat(" ", dayStr, monthStr, yearStr);

		return result;
	}

	public String printSortableDateString(Partial partial) {
        String yearStr = partial.hasYear() ? String.valueOf(partial.getYear()) : "zzzz";
        String monthStr = partial.hasMonth() ? String.valueOf(partial.getMonth()) : "zz";
        String dayStr = partial.hasDay() ? String.valueOf(partial.getDay()) : "zz";
        String result = CdmUtils.concat("-", yearStr, monthStr, dayStr);
        return result;
	}
}
