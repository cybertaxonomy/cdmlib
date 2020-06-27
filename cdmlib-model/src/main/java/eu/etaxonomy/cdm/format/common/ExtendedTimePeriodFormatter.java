/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.common;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeFieldType;
import org.joda.time.format.DateTimeFormatter;

import eu.etaxonomy.cdm.model.common.ExtendedTimePeriod;
import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * @author a.mueller
 * @since 27.06.2020
 */
public class ExtendedTimePeriodFormatter {

    public static final DateTimeFieldType YEAR_TYPE = DateTimeFieldType.year();
    public static final DateTimeFieldType MONTH_TYPE = DateTimeFieldType.monthOfYear();
    public static final DateTimeFieldType DAY_TYPE = DateTimeFieldType.dayOfMonth();
    public static final DateTimeFieldType HOUR_TYPE = DateTimeFieldType.hourOfDay();
    public static final DateTimeFieldType MINUTE_TYPE = DateTimeFieldType.minuteOfHour();

    private static final TimePeriodFormatter timePeriodFormatter = TimePeriodFormatter.NewDefaultInstance();

    public static final ExtendedTimePeriodFormatter NewDefaultInstance(){
        return new ExtendedTimePeriodFormatter();
    }

    private ExtendedTimePeriodFormatter(){}

    public String format (ExtendedTimePeriod tp){
        if (StringUtils.isNotBlank(tp.getFreeText())){
            return tp.getFreeText();
        }else{
            String result = timePeriodFormatter.getTimePeriod(tp);
            DateTimeFormatter formatter = TimePeriodPartialFormatter.NewInstance();
            if (tp.getExtremeStart() != null){
                result = "(" + tp.getExtremeStart().toString(formatter) + TimePeriod.SEP + ")" + result;
            }
            if (tp.getExtremeEnd() != null){
                result += "(" + TimePeriod.SEP + tp.getExtremeEnd().toString(formatter)+")";
            }
            return result;
        }
    }
}
