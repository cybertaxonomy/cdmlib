/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.common;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.joda.time.format.DateTimeFormatter;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * @author a.mueller
 * @since 27.06.2020
 */
public class TimePeriodFormatter {

    public static final DateTimeFieldType YEAR_TYPE = DateTimeFieldType.year();
    public static final DateTimeFieldType MONTH_TYPE = DateTimeFieldType.monthOfYear();
    public static final DateTimeFieldType DAY_TYPE = DateTimeFieldType.dayOfMonth();
    public static final DateTimeFieldType HOUR_TYPE = DateTimeFieldType.hourOfDay();
    public static final DateTimeFieldType MINUTE_TYPE = DateTimeFieldType.minuteOfHour();

    public static final TimePeriodFormatter NewDefaultInstance(){
        return new TimePeriodFormatter();
    }

    private TimePeriodFormatter(){}

    public String format (TimePeriod timePeriod){
        String result = null;
        if ( StringUtils.isNotBlank(timePeriod.getFreeText())){
            result = timePeriod.getFreeText();
        }else{
            result = getTimePeriod(timePeriod);
        }
        return result;
    }

    /**
     * Returns the concatenation of <code>start</code> and <code>end</code>
     */
    public String getTimePeriod(TimePeriod tp){
        String result = null;
        DateTimeFormatter formatter = TimePeriodPartialFormatter.NewInstance();
        if (tp.isContinued()){
            String strStart = tp.getStart() != null ? tp.getStart().toString(formatter): null;
            result = CdmUtils.concat("", strStart, "+");
        }else{
            Partial start = tp.getStart();
            Partial end = tp.getEnd();
            if (start != null && end != null){
                if (start.isSupported(YEAR_TYPE) && end.isSupported(YEAR_TYPE)
                        && start.get(YEAR_TYPE) == end.get(YEAR_TYPE)){
                    if (start.getFields().length == 1){
                        end = null;
                    }else{
                        start = start.without(YEAR_TYPE);
                    }
                }
                if (end != null && !start.isSupported(YEAR_TYPE) && start.isSupported(MONTH_TYPE) &&
                        end.isSupported(MONTH_TYPE) && start.get(MONTH_TYPE) == end.get(MONTH_TYPE)){
                    start = start.without(MONTH_TYPE);
                }
            }
            @SuppressWarnings("null")
            String strStart = isNotEmpty(start) ? start.toString(formatter): null;
            @SuppressWarnings("null")
            String strEnd = isNotEmpty(end) ? end.toString(formatter): null;
            result = CdmUtils.concat(TimePeriod.SEP, strStart, strEnd);
        }

        return result;
    }

    private boolean isNotEmpty(Partial partial) {
        return partial != null && partial.getFields().length>0;
    }

    public String getYear(TimePeriod tp){
        String result = "";
        if (tp.getStartYear() != null){
            result += String.valueOf(tp.getStartYear());
            if (tp.getEndYear() != null && !tp.getStartYear().equals(tp.getEndYear())){
                result += TimePeriod.SEP + String.valueOf(tp.getEndYear());
            }
        }else{
            if (tp.getEndYear() != null){
                result += String.valueOf(tp.getEndYear());
            }
        }
        if (tp.isContinued()){
            result += "+";
        }
        return result;
    }
}
