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
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.search.PartialBridge;
import eu.etaxonomy.cdm.jaxb.PartialAdapter;
import eu.etaxonomy.cdm.strategy.cache.common.TimePeriodPartialFormatter;

/**
 * @author m.doering
 * @since 08-Nov-2007 13:07:00
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
@MappedSuperclass
public class TimePeriod implements Cloneable, Serializable {

    private static final long serialVersionUID = 3405969418194981401L;
    private static final Logger logger = Logger.getLogger(TimePeriod.class);

    public static final DateTimeFieldType YEAR_TYPE = DateTimeFieldType.year();
    public static final DateTimeFieldType MONTH_TYPE = DateTimeFieldType.monthOfYear();
    public static final DateTimeFieldType DAY_TYPE = DateTimeFieldType.dayOfMonth();
    public static final DateTimeFieldType HOUR_TYPE = DateTimeFieldType.hourOfDay();
    public static final DateTimeFieldType MINUTE_TYPE = DateTimeFieldType.minuteOfHour();

    public static final Partial CONTINUED = new Partial
            (new DateTimeFieldType[]{YEAR_TYPE, MONTH_TYPE, DAY_TYPE},
             new int[]{9999, 11, 30});

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

//****************** PARTIAL CONVERTERS ******************/

    /**
     * Transforms a {@link Calendar} into a <code>Partial</code>
     * @param calendar
     * @return
     */
    public static Partial calendarToPartial(Calendar calendar){
        if (calendar == null){
            return null;
        }else{
            LocalDate ld = new LocalDate(calendar);
            Partial partial = new Partial(ld);
            return partial;
        }
    }

    /**
     * Transforms a {@link ReadableInstant} into a <code>Partial</code>
     */
    public static Partial readableInstantToPartial(ReadableInstant readableInstant){
        if (readableInstant == null){
            return null;
        }else{
            DateTime dt = readableInstant.toInstant().toDateTime();
            LocalDate ld = dt.toLocalDate();
            int hour = dt.hourOfDay().get();
            int minute = dt.minuteOfHour().get();
            Partial partial = new Partial(ld).with(HOUR_TYPE, hour).with(MINUTE_TYPE, minute);
            return partial;
        }
    }

    /**
     * Transforms a {@link Date} into a <code>Partial</code>.
     */
    public static Partial dateToPartial(Date date){
        //TODO conversion untested, implemented according to http://www.roseindia.net/java/java-conversion/datetocalender.shtml
        if (date != null){
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return calendarToPartial(cal);
        }else{
            return null;
        }
    }

    /**
     * Transforms an Integer into a <code>Partial</code> with the Integer value
     * being the year of the Partial.
     */
    public static Partial yearToPartial(Integer year){
        if (year != null){
            return new Partial().with(YEAR_TYPE, year);
        }else{
            return null;
        }
    }
    public static Partial monthToPartial(Integer month){
        if (month != null){
            return new Partial().with(MONTH_TYPE, month);
        }else{
            return null;
        }
    }
    public static Partial monthAndDayToPartial(Integer month, Integer day){
        if (month != null || day != null){
            Partial result = new Partial();
            if (month != null){
                result = result.with(MONTH_TYPE, month);
            }
            if (day != null){
                result = result.with(DAY_TYPE, day);
            }
            return result;
        }else{
            return null;
        }
    }

    public static Integer getPartialValue(Partial partial, DateTimeFieldType type){
        if (partial == null || ! partial.isSupported(type)){
            return null;
        }else{
            return partial.get(type);
        }
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
        }else if (timePeriod instanceof VerbatimTimePeriod){
            return (VerbatimTimePeriod)timePeriod;
        }else{
            VerbatimTimePeriod result = VerbatimTimePeriod.NewVerbatimInstance();
            copyCloned(timePeriod, result);
            return result;
        }
    }
    public VerbatimTimePeriod toVerbatim(){
        return toVerbatim(this);
    }

//*********************** CONSTRUCTOR *********************************/

    protected TimePeriod() {
        super();
    }
    protected TimePeriod(Partial startDate, Partial endDate, String freeText) {
        this.start = startDate;
        this.end = endDate;
        this.freeText = freeText;
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
        return isContinued() ? null : end;
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

    /**
     * Returns the continued flag (internally stored as a constant
     * far away date. {@link #CONTINUED}
     * @return
     */
    public boolean isContinued() {
        return CONTINUED.equals(end);
    }
    /**
     * Sets the (virtual) continued flag.<BR><BR>
     * NOTE: setting the flag to true, will remove an
     * existing end date.
     * @param isContinued
     */
    public void setContinued(boolean isContinued) {
        if (isContinued == true){
            this.end = CONTINUED;
        }else if (isContinued()){
            this.end = null;
        }
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
     * True, if there is no start date, no end date and no freetext representation.
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
        return getPartialValue(getEnd(), YEAR_TYPE);
    }

    @Transient
    public Integer getEndMonth(){
        return getPartialValue(getEnd(), MONTH_TYPE);
    }

    @Transient
    public Integer getEndDay(){
        return getPartialValue(getEnd(), DAY_TYPE);
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

    @Transient
    private TimePeriod setStartField(Integer value, DateTimeFieldType type)
            throws IndexOutOfBoundsException{
        start = setPartialField(start, value, type);
        return this;
    }

    @Transient
    private TimePeriod setEndField(Integer value, DateTimeFieldType type)
            throws IndexOutOfBoundsException{
        end = setPartialField(getEnd(), value, type);
        return this;
    }

    public static Partial setPartialField(Partial partial, Integer value, DateTimeFieldType type)
            throws IndexOutOfBoundsException{
        if (partial == null){
            partial = new Partial();
        }
        if (value == null){
            return partial.without(type);
        }else{
            checkFieldValues(value, type, partial);
            return partial.with(type, value);
        }
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
        }
        if (type.equals(DAY_TYPE)){
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
        if ( (value < 1 || value > max) ){
            throw new IndexOutOfBoundsException("Value must be between 1 and " +  max);
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
        if ( StringUtils.isNotBlank(this.getFreeText())){
            result = this.getFreeText();
        }else{
            result = getTimePeriod();
        }
        return result;
    }

    /**
     * Returns the concatenation of <code>start</code> and <code>end</code>
     */
    public String getTimePeriod(){
        String result = null;
        DateTimeFormatter formatter = TimePeriodPartialFormatter.NewInstance();
        String strStart = start != null ? start.toString(formatter): null;
        if (isContinued()){
            result = CdmUtils.concat("", strStart, "+");
        }else{
            String strEnd = end != null ? end.toString(formatter): null;
            result = CdmUtils.concat("-", strStart, strEnd);
        }

        return result;
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
        if (isContinued()){
            result += "+";
        }
        return result;
    }

//*********** EQUALS **********************************/

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
        //see comment in VerbatimTimePeriod#equals
        String thisVerbatimDate = (this instanceof VerbatimTimePeriod)?
                ((VerbatimTimePeriod)this).getVerbatimDate():null;
        String thatVerbatimDate = (obj instanceof VerbatimTimePeriod)?
                ((VerbatimTimePeriod)obj).getVerbatimDate():null;
        if (! CdmUtils.nullSafeEqual(thisVerbatimDate, thatVerbatimDate)){
            return false;
        }
        //see comment in ExtendedTimePeriod#equals
        Partial thisExtremeStart = (this instanceof ExtendedTimePeriod)?
                ((ExtendedTimePeriod)this).getExtremeStart():null;
        Partial thatExtremeStart = (obj instanceof ExtendedTimePeriod)?
                ((ExtendedTimePeriod)obj).getExtremeStart():null;
        if (! CdmUtils.nullSafeEqual(thisExtremeStart, thatExtremeStart)){
            return false;
        }

        Partial thisExtremeEnd = (this instanceof ExtendedTimePeriod)?
                ((ExtendedTimePeriod)this).getExtremeEnd():null;
        Partial thatExtremeEnd = (obj instanceof ExtendedTimePeriod)?
                ((ExtendedTimePeriod)obj).getExtremeEnd():null;
        if (! CdmUtils.nullSafeEqual(thisExtremeEnd, thatExtremeEnd)){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 7;
        hashCode = 29*hashCode +
                    (start == null? 33: start.hashCode()) +
                    (end == null? 39: end.hashCode()) +
                    (freeText == null? 41: freeText.hashCode());
        return hashCode;
    }

//*********** CLONE **********************************/

    @Override
    public TimePeriod clone()  {
        try {
            TimePeriod result = (TimePeriod)super.clone();
            copyCloned(this, result);
            return result;
        } catch (CloneNotSupportedException e) {
            logger.warn("Clone not supported exception. Should never occurr !!");
            return null;
        }
    }

    protected static void copyCloned(TimePeriod origin, TimePeriod target) {
        target.setStart(origin.start);   //DateTime is immutable
        target.setEnd(origin.end);
        target.setFreeText(origin.freeText);
    }

}
