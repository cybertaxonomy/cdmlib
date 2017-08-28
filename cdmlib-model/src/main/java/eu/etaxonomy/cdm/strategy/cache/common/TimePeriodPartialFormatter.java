/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.common;



import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;

import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * Formatter for {@link TimePeriod} instances.
 * @author a.mueller
 * @created 14-Jul-2013 (formerly inner class in TimePeriod)
 *
 */
public class TimePeriodPartialFormatter {
    DateTimeFormatter innerFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	public static TimePeriodPartialFormatter NewInstance(){
		return new TimePeriodPartialFormatter();
	}




    public String print(Temporal partial){
		//TODO
		String result = "";
		String year = (partial.isSupported(ChronoField.YEAR))? String.valueOf(partial.get(ChronoField.YEAR)):null;
		String month = (partial.isSupported(ChronoField.MONTH_OF_YEAR))? String.valueOf(partial.get(ChronoField.MONTH_OF_YEAR)):null;;
		String day = (partial.isSupported(ChronoField.DAY_OF_MONTH))? String.valueOf(partial.get(ChronoField.DAY_OF_MONTH)):null;;

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
