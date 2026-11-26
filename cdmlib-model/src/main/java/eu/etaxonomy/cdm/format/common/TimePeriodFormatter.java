/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.common;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.Partial;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.TimePeriodBase;

/**
 * @author a.mueller
 * @since 27.06.2020
 */
public class TimePeriodFormatter {

    private TimePeriodPartialFormatter partialFormatter = TimePeriodPartialFormatter.INSTANCE();

    public static final TimePeriodFormatter NewDefaultInstance(){
        return new TimePeriodFormatter();
    }

    private TimePeriodFormatter(){}

    public String format (TimePeriodBase timePeriod){
        String result = null;
        if (StringUtils.isNotBlank(timePeriod.getFreeText())){
            result = timePeriod.getFreeText();
        }else{
            result = getTimePeriod(timePeriod);
        }
        return result;
    }

    /**
     * Returns the concatenation of <code>start</code> and <code>end</code>
     */
    public String getTimePeriod(TimePeriodBase tp){
        String result = null;
        if (tp.isContinued()){
            String strStart = tp.getStart() != null ? partialFormatter.print(tp.getStart()): null;
            result = CdmUtils.concat("", strStart, "+");
        }else{
            Partial start = tp.getStart();
            Partial end = tp.getEnd();
            if (start != null && end != null){
                if (start.hasYear() && end.hasYear()
                        && Objects.equals(start.getYear(), end.getYear())){
                    if (!start.hasMonth() && !start.hasDay() && !start.hasHour() && !start.hasMinute()){
                        end = null;
                    }else{
                        start = start.withoutYear();
                    }
                }
                if (end != null && !start.hasYear() && start.hasMonth() &&
                        end.hasMonth() && Objects.equals(start.getMonth(), end.getMonth())){
                    start = start.withoutMonth();
                }
            }
            @SuppressWarnings("null")
            String strStart = isNotEmpty(start) ? partialFormatter.print(start): null;
            @SuppressWarnings("null")
            String strEnd = isNotEmpty(end) ? partialFormatter.print(end): null;
            result = CdmUtils.concat(TimePeriod.SEP, strStart, strEnd);
        }

        return result;
    }

    private boolean isNotEmpty(Partial partial) {
        return partial != null && !partial.isEmpty();
    }

    public String getYear(TimePeriodBase tp){
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
