/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.common;

import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormatter;

import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * Formatter for {@link TimePeriod} instances.
 * @author a.mueller
 * @created 14-Jul-2013 (formerly inner class in TimePeriod)
 *
 */
public class TimePeriodPartialFormatter extends DateTimeFormatter{
	
	public static TimePeriodPartialFormatter NewInstance(){
		return new TimePeriodPartialFormatter();
	}
	
	private TimePeriodPartialFormatter(){
		super(null, null);
	}
	
	@Override
    public String print(ReadablePartial partial){
		//TODO
		String result = "";
		String year = (partial.isSupported(TimePeriod.YEAR_TYPE))? String.valueOf(partial.get(TimePeriod.YEAR_TYPE)):null;
		String month = (partial.isSupported(TimePeriod.MONTH_TYPE))? String.valueOf(partial.get(TimePeriod.MONTH_TYPE)):null;;
		String day = (partial.isSupported(TimePeriod.DAY_TYPE))? String.valueOf(partial.get(TimePeriod.DAY_TYPE)):null;;

		if (month !=null){
			if (year == null){
				year = "xxxx";
			}
		}
		if (day != null){
			if (month == null){
				month = "xx";
			}
			if (year == null){
				year = "xxxx";
			}
		}
		result = (day != null)? day + "." : "";
		result += (month != null)? month + "." : "";
		result += (year != null)? year : "";

		return result;
	}

}
