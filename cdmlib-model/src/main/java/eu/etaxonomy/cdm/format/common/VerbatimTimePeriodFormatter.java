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

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;

/**
 * @author a.mueller
 * @since 27.06.2020
 */
public class VerbatimTimePeriodFormatter {

    public static final DateTimeFieldType YEAR_TYPE = DateTimeFieldType.year();
    public static final DateTimeFieldType MONTH_TYPE = DateTimeFieldType.monthOfYear();
    public static final DateTimeFieldType DAY_TYPE = DateTimeFieldType.dayOfMonth();
    public static final DateTimeFieldType HOUR_TYPE = DateTimeFieldType.hourOfDay();
    public static final DateTimeFieldType MINUTE_TYPE = DateTimeFieldType.minuteOfHour();

    private static final TimePeriodFormatter timePeriodFormatter = TimePeriodFormatter.NewDefaultInstance();

    public static final VerbatimTimePeriodFormatter NewDefaultInstance(){
        return new VerbatimTimePeriodFormatter();
    }

    private VerbatimTimePeriodFormatter(){}

    public String format (VerbatimTimePeriod tp){
        String result = timePeriodFormatter.format(tp);
        if (StringUtils.isNotBlank(tp.getVerbatimDate()) && StringUtils.isBlank(tp.getFreeText())){
            result = CdmUtils.concat(" ", result, "[\""+tp.getVerbatimDate()+"\"]");
        }
        return result;
    }
}
