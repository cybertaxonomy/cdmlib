/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.common;

import org.joda.time.ReadablePartial;

import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * @author k.luther
 * @since Jun 15, 2020
 */
public class ExtendedTimePeriodPartialFormatter extends TimePeriodPartialFormatter {

    public static ExtendedTimePeriodPartialFormatter NewInstance(){
        return new ExtendedTimePeriodPartialFormatter();
    }

    private ExtendedTimePeriodPartialFormatter(){
        super();
    }

    @Override
    public String print(ReadablePartial partial){
        //TODO
        String result = "";
        String year = (partial.isSupported(TimePeriod.YEAR_TYPE))? String.valueOf(partial.get(TimePeriod.YEAR_TYPE)):null;
        String month = (partial.isSupported(TimePeriod.MONTH_TYPE))? String.valueOf(partial.get(TimePeriod.MONTH_TYPE)):null;;
        String day = (partial.isSupported(TimePeriod.DAY_TYPE))? String.valueOf(partial.get(TimePeriod.DAY_TYPE)):null;;


        if (day != null){
            if (month == null){
                month = "xx";
            }

        }
        result = (day != null)? day + "." : "";
        result += (month != null)? month + "." : "";
        result += (year != null)? year : "";

        return result;
    }


}
