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

import eu.etaxonomy.cdm.model.common.ExtendedTimePeriod;
import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * @author a.mueller
 * @since 27.06.2020
 */
public class ExtendedTimePeriodFormatter {

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
            TimePeriodPartialFormatter formatter = TimePeriodPartialFormatter.INSTANCE();
            if (tp.getExtremeStart() != null){
                result = "(" + formatter.print(tp.getExtremeStart()) + TimePeriod.SEP + ")" + result;
            }
            if (tp.getExtremeEnd() != null){
                result += "(" + TimePeriod.SEP + formatter.print(tp.getExtremeEnd())+")";
            }
            return result;
        }
    }
}
