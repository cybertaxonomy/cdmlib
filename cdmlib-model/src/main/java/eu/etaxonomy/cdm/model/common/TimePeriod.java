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
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.Partial;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormatter;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.search.PartialBridge;
import eu.etaxonomy.cdm.jaxb.PartialAdapter;
import eu.etaxonomy.cdm.strategy.cache.common.TimePeriodPartialFormatter;

/**
 * @author m.doering
 * @created 08-Nov-2007 13:07:00
 * @updated 05-Dec-2008 23:00:05
 * @updated 14-Jul-2013 move parser methods to TimePeriodParser
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimePeriod", propOrder = {
    "start",
    "end",
    "freeText"
})
@XmlRootElement(name = "TimePeriod")
@Embeddable
public class TimePeriod implements Cloneable, Serializable {
    private static final long serialVersionUID = 3405969418194981401L;
    private static final Logger logger = Logger.getLogger(TimePeriod.class);
    public static final DateTimeFieldType MONTH_TYPE = DateTimeFieldType.monthOfYear();
    public static final DateTimeFieldType YEAR_TYPE = DateTimeFieldType.year();
    public static final DateTimeFieldType DAY_TYPE = DateTimeFieldType.dayOfMonth();

    @XmlElement(name = "Start")
    @XmlJavaTypeAdapter(value = PartialAdapter.class)
    @Type(type="partialUserType")
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = PartialBridge.class)
    @JsonIgnore // currently used for swagger model scanner
    private Partial start;

    @XmlElement(name = "End")
    @XmlJavaTypeAdapter(value = PartialAdapter.class)
    @Type(type="partialUserType")
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = PartialBridge.class)
    @JsonIgnore // currently used for swagger model scanner
    private Partial end;


    @XmlElement(name = "FreeText")
    private String freeText;

// ********************** FACTORY METHODS **************************/

    /**
     * Factory method
     * @return
     */
    public static TimePeriod NewInstance(){
        return new TimePeriod();
    }


    /**
     * Factory method
     * @return
     */
    public static TimePeriod NewInstance(Partial startDate){
        return new TimePeriod(startDate);
    }


    /**
     * Factory method
     * @return
     */
    public static TimePeriod NewInstance(Partial startDate, Partial endDate){
        return new TimePeriod(startDate, endDate);
    }


    /**
     * Factory method
     * @return
     */
    public static TimePeriod NewInstance(Integer year){
        Integer endYear = null;
        return NewInstance(year, endYear);
    }

    /**
     * Factory method
     * @return
     */
    public static TimePeriod NewInstance(Integer startYear, Integer endYear){
        Partial startDate = null;
        Partial endDate = null;
        if (startYear != null){
            startDate = new Partial().with(YEAR_TYPE, startYear);
        }
        if (endYear != null){
            endDate = new Partial().with(YEAR_TYPE, endYear);
        }
        return new TimePeriod(startDate, endDate);
    }



    /**
     * Factory method to create a TimePeriod from a <code>Calendar</code>. The Calendar is stored as the starting instant.
     * @return
     */
    public static TimePeriod NewInstance(Calendar startCalendar){
        return NewInstance(startCalendar, null);
    }

    /**
     * Factory method to create a TimePeriod from a <code>ReadableInstant</code>(e.g. <code>DateTime</code>).
     * The <code>ReadableInstant</code> is stored as the starting instant.
     * @return
     */
    public static TimePeriod NewInstance(ReadableInstant readableInstant){
        return NewInstance(readableInstant, null);
    }

    /**
     * Factory method to create a TimePeriod from a starting and an ending <code>Calendar</code>
     * @return
     */
    public static TimePeriod NewInstance(Calendar startCalendar, Calendar endCalendar){
        Partial startDate = null;
        Partial endDate = null;
        if (startCalendar != null){
            startDate = calendarToPartial(startCalendar);
        }
        if (endCalendar != null){
            endDate = calendarToPartial(endCalendar);
        }
        return new TimePeriod(startDate, endDate);
    }

    /**
     * Factory method to create a TimePeriod from a starting and an ending <code>Date</code>
     * @return TimePeriod
     */
    public static TimePeriod NewInstance(Date startDate, Date endDate){
        //TODO conversion untested, implemented according to http://www.roseindia.net/java/java-conversion/datetocalender.shtml
        Calendar calStart = null;
        Calendar calEnd = null;
        if (startDate != null){
            calStart = Calendar.getInstance();
            calStart.setTime(startDate);
        }
        if (endDate != null){
            calEnd = Calendar.getInstance();
            calEnd.setTime(endDate);
        }
        return NewInstance(calStart, calEnd);
    }


    /**
     * Factory method to create a TimePeriod from a starting and an ending <code>ReadableInstant</code>(e.g. <code>DateTime</code>)
     * @return
     */
    public static TimePeriod NewInstance(ReadableInstant startInstant, ReadableInstant endInstant){
        Partial startDate = null;
        Partial endDate = null;
        if (startInstant != null){
            startDate = readableInstantToPartial(startInstant);
        }
        if (endInstant != null){
            endDate = readableInstantToPartial(endInstant);
        }
        return new TimePeriod(startDate, endDate);
    }

//****************** CONVERTERS ******************/

    /**
     * Transforms a {@link Calendar} into a <code>Partial</code>
     * @param calendar
     * @return
     */
    public static Partial calendarToPartial(Calendar calendar){
        LocalDate ld = new LocalDate(calendar);
        Partial partial = new Partial(ld);
        return partial;
    }

    /**
     * Transforms a {@link ReadableInstant} into a <code>Partial</code>
     * @param calendar
     * @return
     */
    public static Partial readableInstantToPartial(ReadableInstant readableInstant){
        DateTime dt = readableInstant.toInstant().toDateTime();
        LocalDate ld = dt.toLocalDate();
        Partial partial = new Partial(ld);
        return partial;
    }


    public static Integer getPartialValue(Partial partial, DateTimeFieldType type){
        if (partial == null || ! partial.isSupported(type)){
            return null;
        }else{
            return partial.get(type);
        }
    }



//*********************** CONSTRUCTOR *********************************/

    /**
     * Constructor
     */
    protected TimePeriod() {
        super();
    }
    public TimePeriod(Partial startDate) {
        start=startDate;
    }
    public TimePeriod(Partial startDate, Partial endDate) {
        start=startDate;
        end=endDate;
    }

//******************* GETTER / SETTER ************************************/


    @JsonIgnore // currently used for swagger model scanner
    public Partial getStart() {
        return start;
    }

    public void setStart(Partial start) {
        this.start = start;
    }


    @JsonIgnore // currently used for swagger model scanner
    public Partial getEnd() {
        return end;
    }

    public void setEnd(Partial end) {
        this.end = end;
    }

    /**
     * For time periods that need to store more information than the one
     * that can be stored in <code>start</code> and <code>end</code>.
     * If free text is not <code>null</null> {@link #toString()} will always
     * return the free text value.
     * <BR>Use {@link #toString()} for public use.
     * @return the freeText
     */
    public String getFreeText() {
        return freeText;
    }


    /**
     * Use {@link #parseSingleDate(String)} for public use.
     * @param freeText the freeText to set
     */
    public void setFreeText(String freeText) {
        this.freeText = freeText;
    }


//******************* Transient METHODS ************************************/

    /**
     * True, if this time period represents a period not a single point in time.
     * This is by definition, that the time period has a start and an end value,
     * and both have a year value that is not null
     * @return
     */
    @Transient
    public boolean isPeriod(){
        if (getStartYear() != null && getEndYear() != null ){
            return true;
        }else{
            return false;
        }
    }

    /**
     * True, if there is no start date and no end date and no freetext representation exists.
     * @return
     */
    @Transient
    public boolean isEmpty(){
        if (StringUtils.isBlank(this.getFreeText()) && start == null  && end == null ){
            return true;
        }else{
            return false;
        }
    }



    @Transient
    public String getYear(){
        String result = "";
        if (getStartYear() != null){
            result += String.valueOf(getStartYear());
            if (getEndYear() != null){
                result += "-" + String.valueOf(getEndYear());
            }
        }else{
            if (getEndYear() != null){
                result += String.valueOf(getEndYear());
            }
        }
        return result;
    }

    @Transient
    public Integer getStartYear(){
        return getPartialValue(start, YEAR_TYPE);
    }

    @Transient
    public Integer getStartMonth(){
        return getPartialValue(start, MONTH_TYPE);
    }

    @Transient
    public Integer getStartDay(){
        return getPartialValue(start, DAY_TYPE);
    }

    @Transient
    public Integer getEndYear(){
        return getPartialValue(end, YEAR_TYPE);
    }

    @Transient
    public Integer getEndMonth(){
        return getPartialValue(end, MONTH_TYPE);
    }

    @Transient
    public Integer getEndDay(){
        return getPartialValue(end, DAY_TYPE);
    }

    public TimePeriod setStartYear(Integer year){
        return setStartField(year, YEAR_TYPE);
    }

    public TimePeriod setStartMonth(Integer month) throws IndexOutOfBoundsException{
        return setStartField(month, MONTH_TYPE);
    }

    public TimePeriod setStartDay(Integer day) throws IndexOutOfBoundsException{
        return setStartField(day, DAY_TYPE);
    }

    public TimePeriod setEndYear(Integer year){
        return setEndField(year, YEAR_TYPE);
    }

    public TimePeriod setEndMonth(Integer month) throws IndexOutOfBoundsException{
        return setEndField(month, MONTH_TYPE);
    }

    public TimePeriod setEndDay(Integer day) throws IndexOutOfBoundsException{
        return setEndField(day, DAY_TYPE);
    }

    public static Partial setPartialField(Partial partial, Integer value, DateTimeFieldType type)
            throws IndexOutOfBoundsException{
        if (partial == null){
            partial = new Partial();
        }
        if (value == null || value == 0){
            return partial.without(type);
        }else{
            checkFieldValues(value, type, partial);
            return partial.with(type, value);
        }
    }

    @Transient
    private TimePeriod setStartField(Integer value, DateTimeFieldType type)
            throws IndexOutOfBoundsException{
        start = setPartialField(start, value, type);
        return this;
    }

    @Transient
    private TimePeriod setEndField(Integer value, DateTimeFieldType type)
            throws IndexOutOfBoundsException{
        end = setPartialField(end, value, type);
        return this;
    }

// ******************************** internal methods *******************************/

    /**
     * Throws an IndexOutOfBoundsException if the value does not have a valid value
     * (e.g. month > 12, month < 1, day > 31, etc.)
     * @param value
     * @param type
     * @throws IndexOutOfBoundsException
     */
    private static void checkFieldValues(Integer value, DateTimeFieldType type, Partial partial)
            throws IndexOutOfBoundsException{
        int max = 9999999;
        if (type.equals(MONTH_TYPE)){
            max = 12;
            if (value == 0 && partial.indexOf(DAY_TYPE) == -1){
                return;
            }
        }
        if (type.equals(DAY_TYPE)){
            if (value == 0){
                return;
            }
            max = 31;
            Integer month = null;
            if (partial.isSupported(MONTH_TYPE)){
                month = partial.get(MONTH_TYPE);
            }
            if (month != null){
                if (month == 2){
                    max = 29;
                }else if (month == 4 ||month == 6 ||month == 9 ||month == 11){
                    max = 30;
                }
            }
        }
        if (type.equals(YEAR_TYPE)){
            if (value == 0 && partial.getValue(partial.indexOf(MONTH_TYPE)) == 0){
                return;
            }
        }

        if ( (value < 1 || value > max) ){
            throw new IndexOutOfBoundsException("Value must be between 1 and " +  max);
        }
    }

    private void initStart(){
        if (start == null){
            start = new Partial();
        }
    }

    private void initEnd(){
        if (end == null){
            end = new Partial();
        }
    }


//**************************** to String ****************************************

    /**
     * Returns the {@link #getFreeText()} value if free text is not <code>null</code>.
     * Otherwise the concatenation of <code>start</code> and <code>end</code> is returned.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(){
        String result = null;
        DateTimeFormatter formatter = TimePeriodPartialFormatter.NewInstance();
        if ( StringUtils.isNotBlank(this.getFreeText())){
            result = this.getFreeText();
        }else{
            String strStart = start != null ? start.toString(formatter): null;
            String strEnd = end != null ? end.toString(formatter): null;
            result = CdmUtils.concat("-", strStart, strEnd);
        }
        return result;
    }

//*********** EQUALS **********************************/


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null){
            return false;
        }
        if (! (obj instanceof TimePeriod)){
            return false;
        }
        TimePeriod that = (TimePeriod)obj;

        if (! CdmUtils.nullSafeEqual(this.start, that.start)){
            return false;
        }
        if (! CdmUtils.nullSafeEqual(this.end, that.end)){
            return false;
        }
        if (! CdmUtils.nullSafeEqual(this.freeText, that.freeText)){
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hashCode = 7;
        hashCode = 29*hashCode +
                    (start== null? 33: start.hashCode()) +
                    (end== null? 39: end.hashCode()) +
                    (freeText== null? 41: freeText.hashCode());
        return super.hashCode();
    }


//*********** CLONE **********************************/


    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone()  {
        try {
            TimePeriod result = (TimePeriod)super.clone();
            result.setStart(this.start);   //DateTime is immutable
            result.setEnd(this.end);
            result.setFreeText(this.freeText);
            return result;
        } catch (CloneNotSupportedException e) {
            logger.warn("Clone not supported exception. Should never occurr !!");
            return null;
        }
    }


}