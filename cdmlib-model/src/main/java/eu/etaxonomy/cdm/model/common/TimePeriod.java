/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Partial;
import org.joda.time.ReadableInstant;

import jakarta.persistence.Embeddable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author m.doering
 * @since 08-Nov-2007 13:07:00
 * @updated 05-Dec-2008 23:00:05
 * @updated 14-Jul-2013 move parser methods to TimePeriodParser
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimePeriod")
@XmlRootElement(name = "TimePeriod")
@Embeddable
public class TimePeriod extends TimePeriodBase implements Cloneable, Serializable, ICheckEmpty {

    private static final long serialVersionUID = 3405969418194981401L;
    private static final Logger logger = LogManager.getLogger();

// ********************** FACTORY METHODS **************************/

    public static final TimePeriod NewInstance(){
        return new TimePeriod();
    }

    public static final TimePeriod NewInstance(Partial startDate){
        return new TimePeriod(startDate, null, null);
    }

    public static final TimePeriod NewInstance(Partial startDate, Partial endDate){
        return new TimePeriod(startDate, endDate, null);
    }

    public static final TimePeriod NewInstance(Integer year){
        Integer endYear = null;
        return NewInstance(year, endYear);
    }

    public static final TimePeriod NewInstance(Integer startYear, Integer endYear){
        return new TimePeriod(yearToPartial(startYear), yearToPartial(endYear), null);
    }

    /**
     * Factory method to create a TimePeriod from a <code>Calendar</code>. The Calendar is stored as the starting instant.
     * @return
     */
    public static final TimePeriod NewInstance(Calendar startCalendar){
        return NewInstance(startCalendar, null);
    }

    public static final TimePeriod NewInstance(ZonedDateTime startDate){
        return TimePeriod.NewInstance(startDate, null);
    }

    /**
     * Factory method to create a TimePeriod from a <code>ReadableInstant</code>(e.g. <code>DateTime</code>).
     * The <code>ReadableInstant</code> is stored as the starting instant.
     * @return
     */
    public static final TimePeriod NewInstance(ReadableInstant readableInstant){
        return NewInstance(readableInstant, null);
    }

    /**
     * Factory method to create a TimePeriod from a starting and an ending <code>Calendar</code>
     * @return
     */
    public static final TimePeriod NewInstance(ZonedDateTime startDateTime, ZonedDateTime endDateTime){
        return new TimePeriod(zonedDateTimeToPartial(startDateTime), zonedDateTimeToPartial(endDateTime), null);
    }

    /**
     * Factory method to create a TimePeriod from a starting and an ending <code>Calendar</code>
     * @return
     */
    public static final TimePeriod NewInstance(Calendar startCalendar, Calendar endCalendar){
        return new TimePeriod(calendarToPartial(startCalendar), calendarToPartial(endCalendar), null);
    }

    /**
     * Factory method to create a TimePeriod from a starting and an ending <code>Date</code>
     * @return TimePeriod
     */
    public static final TimePeriod NewInstance(Date startDate, Date endDate){
        return NewInstance(dateToPartial(startDate), dateToPartial(endDate));
    }

    /**
     * Factory method to create a TimePeriod from a starting and an ending <code>ReadableInstant</code>(e.g. <code>DateTime</code>)
     * @return
     */
    public static final TimePeriod NewInstance(ReadableInstant startInstant, ReadableInstant endInstant){
        return new TimePeriod(readableInstantToPartial(startInstant), readableInstantToPartial(endInstant), null);
    }

//****************** TIME PERIOD CONVERTERS ******************/

    public static TimePeriod fromVerbatim(VerbatimTimePeriod verbatimTimePeriod){
        if (verbatimTimePeriod == null){
            return null;
        }
        TimePeriod result = TimePeriod.NewInstance();
        copyCloned(verbatimTimePeriod, result);
        if (StringUtils.isNotBlank(verbatimTimePeriod.getVerbatimDate()) &&
              StringUtils.isBlank(result.getFreeText())){
          result.setFreeText(verbatimTimePeriod.toString());
        }
        return result;
    }
    public static VerbatimTimePeriod toVerbatim(TimePeriod timePeriod){
        if (timePeriod == null){
            return null;
        }else{
            VerbatimTimePeriod result = VerbatimTimePeriod.NewVerbatimInstance();
            copyCloned(timePeriod, result);
            return result;
        }
    }
    @Override
    public VerbatimTimePeriod toVerbatim(){
        return toVerbatim(this);
    }

//*************************** IS BLANK ******************************/

    public static boolean isBlank(TimePeriod timePeriod) {
        return timePeriod == null || timePeriod.isEmpty();
    }

//*********************** CONSTRUCTOR *********************************/

    protected TimePeriod() {}

    protected TimePeriod(Partial startDate, Partial endDate, String freeText) {
        this.start = startDate;
        this.end = endDate;
        this.freeText = freeText;
    }

//**************************** to String ****************************************


    /**
     * True, if there is no start date, no end date and no freetext representation.
     */
    @Override
    public boolean checkEmpty() {
        //TODO unify isEmpty && checkEmpty
        return isEmpty();
    }

    //*********** CLONE **********************************/

    @Override
    public TimePeriod clone()  {
        TimePeriod result = (TimePeriod)super.clone();
        return result;
    }


}