/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

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

import org.apache.log4j.Logger;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.joda.time.ReadableInstant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.etaxonomy.cdm.format.common.ExtendedTimePeriodFormatter;
import eu.etaxonomy.cdm.hibernate.search.PartialBridge;
import eu.etaxonomy.cdm.jaxb.PartialAdapter;

/**
 * TimePeriod class with extended time period for "extreme" phases
 * like secondary flowering time.
 *
 * @author a.mueller
 * @since 28.04.2020
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VerbatimTimePeriod", propOrder = {
    "verbatimDate"
})
@XmlRootElement(name = "ExtendedTimePeriod")
@Embeddable
public class ExtendedTimePeriod extends TimePeriod {

    private static final long serialVersionUID = -6543644293635460526L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ExtendedTimePeriod.class);

    private static ExtendedTimePeriodFormatter formatter = ExtendedTimePeriodFormatter.NewDefaultInstance();

    @XmlElement(name = "ExtremeStart")
    @XmlJavaTypeAdapter(value = PartialAdapter.class)
    @Type(type="partialUserType")
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = PartialBridge.class)
    @JsonIgnore // currently used for swagger model scanner
    private Partial extremeStart;

    @XmlElement(name = "ExtremeEnd")
    @XmlJavaTypeAdapter(value = PartialAdapter.class)
    @Type(type="partialUserType")
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = PartialBridge.class)
    @JsonIgnore // currently used for swagger model scanner
    private Partial extremeEnd;

 // ********************** FACTORY METHODS **************************/

    public static final ExtendedTimePeriod NewExtendedInstance(){
         return new ExtendedTimePeriod();
     }

     public static final ExtendedTimePeriod NewExtendedInstance(Partial startDate){
         return new ExtendedTimePeriod(startDate, null, null, null, null);
     }

     public static final ExtendedTimePeriod NewExtendedInstance(Partial startDate, Partial endDate){
         return new ExtendedTimePeriod(startDate, endDate, null, null, null);
     }

     public static final ExtendedTimePeriod NewExtendedInstance(Partial startDate, Partial endDate, Partial extremeStartDate, Partial extremeStartEnd){
         return new ExtendedTimePeriod(startDate, endDate, null, extremeStartDate, extremeStartEnd);
     }

     public static final ExtendedTimePeriod NewExtendedYearInstance(Integer year){
         Integer endYear = null;
         return NewExtendedYearInstance(year, endYear, null, null);
     }

     public static final ExtendedTimePeriod NewExtendedYearInstance(Integer startYear, Integer endYear){
         return NewExtendedYearInstance(startYear, endYear, null, null);
     }

     public static final ExtendedTimePeriod NewExtendedYearInstance(Integer startYear, Integer endYear, Integer extremeStartYear, Integer extremeEndYear){
         return NewExtendedInstance(yearToPartial(startYear), yearToPartial(endYear), yearToPartial(extremeStartYear), yearToPartial(extremeEndYear));
     }

     public static final ExtendedTimePeriod NewExtendedMonthInstance(Integer startMonth, Integer endMonth){
         return NewExtendedMonthInstance(startMonth, endMonth, null, null);
     }

     public static final ExtendedTimePeriod NewExtendedMonthInstance(Integer startMonth, Integer endMonth, Integer extremeStartMonth, Integer extremeEndMonth){
         return NewExtendedInstance(monthToPartial(startMonth), monthToPartial(endMonth), monthToPartial(extremeStartMonth), monthToPartial(extremeEndMonth));
     }
     public static final ExtendedTimePeriod NewExtendedMonthAndDayInstance(Integer startMonth, Integer startDay, Integer endMonth, Integer endDay, Integer extremeStartMonth, Integer extremeStartDay, Integer extremeEndMonth, Integer extremeEndDay){
         return NewExtendedInstance(monthAndDayToPartial(startMonth,startDay), monthAndDayToPartial(endMonth,endDay), monthAndDayToPartial(extremeStartMonth,extremeStartDay), monthAndDayToPartial(extremeEndMonth,extremeEndDay));
     }

     /**
      * Factory method to create a TimePeriod from a <code>Calendar</code>. The Calendar is stored as the starting instant.
      * @return
      */
     public static final ExtendedTimePeriod NewExtendedInstance(Calendar startCalendar){
         return NewExtendedInstance(startCalendar, null, null, null);
     }

     /**
      * Factory method to create a TimePeriod from a starting and an ending <code>Calendar</code>.
      */
     public static final ExtendedTimePeriod NewExtendedInstance(Calendar startCalendar, Calendar endCalendar, Calendar extremeStartCalendar, Calendar extremeEndCalendar){
         return NewExtendedInstance(calendarToPartial(startCalendar), calendarToPartial(endCalendar), calendarToPartial(extremeStartCalendar), calendarToPartial(extremeEndCalendar));
     }

     /**
      * Factory method to create an ExtendedTimePeriod from a starting and an ending <code>Date</code>
      */
     public static final ExtendedTimePeriod NewExtendedInstance(Date startDate, Date endDate, Date extremeStartDate, Date extremeEndDate){
         return NewExtendedInstance(dateToPartial(startDate), dateToPartial(endDate), dateToPartial(extremeStartDate), dateToPartial(extremeEndDate));
     }

     /**
      * Factory method to create a TimePeriod from a <code>ReadableInstant</code>(e.g. <code>DateTime</code>).
      * The <code>ReadableInstant</code> is stored as the starting instant.
      */
     public static final ExtendedTimePeriod NewExtendedInstance(ReadableInstant readableInstant){
         return NewExtendedInstance(readableInstant, null, null, null);
     }

     /**
      * Factory method to create an ExtendedTimePeriod from a starting and an ending <code>ReadableInstant</code>(e.g. <code>DateTime</code>)
      * @return
      */
     public static final ExtendedTimePeriod NewExtendedInstance(ReadableInstant startInstant, ReadableInstant endInstant, ReadableInstant extremeStartInstant, ReadableInstant extremeEndInstant){
         return NewExtendedInstance(readableInstantToPartial(startInstant), readableInstantToPartial(endInstant), readableInstantToPartial(extremeStartInstant), readableInstantToPartial(extremeEndInstant));
     }


//*********************** CONSTRUCTOR *********************************/

    protected ExtendedTimePeriod() {
        super();
    }
    private ExtendedTimePeriod(Partial startDate, Partial endDate, String freeText,
            Partial extremeStartDate, Partial extremeEndDate) {
        super(startDate, endDate, freeText);
        this.extremeStart = extremeStartDate;
        this.extremeEnd = extremeEndDate;
    }

// ***************************** GETTER /SETTER *********************/

    public Partial getExtremeStart() {
        return extremeStart;
    }
    public void setExtremeStart(Partial extremeStart) {
        this.extremeStart = extremeStart;
    }

    public Partial getExtremeEnd() {
        return isContinued() ? null : extremeEnd;
    }
    public void setExtremeEnd(Partial extremeEnd) {
        this.extremeEnd = extremeEnd;
    }

    @Override
    public void setContinued(boolean isContinued) {
        super.setContinued(isContinued);
        if (isContinued == true){
            this.extremeEnd = CONTINUED;
        }else if (isContinued()){
            this.extremeEnd = null;
        }
    }

// ************************************ TRANSIENT **************************/

    /**
     * <code>true</code>, if there is no start date, no end date and no freetext representation.
     * Also there must be no extreme start or end date or freetext representation.
     */
    @Override
    @Transient
    public boolean isEmpty(){
        boolean result = super.isEmpty();
        if (result == true && isEmpty(extremeStart) && isEmpty(extremeEnd)){
            return true;
        }else{
            return false;
        }
    }

    @Transient
    public Integer getExtremeStartYear(){
        return getPartialValue(extremeStart, YEAR_TYPE);
    }

    @Transient
    public Integer getExtremeStartMonth(){
        return getPartialValue(extremeStart, MONTH_TYPE);
    }

    @Transient
    public Integer getExtremeStartDay(){
        return getPartialValue(extremeStart, DAY_TYPE);
    }

    @Transient
    public Integer getExtremeEndYear(){
        return getPartialValue(getExtremeEnd(), YEAR_TYPE);
    }

    @Transient
    public Integer getExtremeEndMonth(){
        return getPartialValue(getExtremeEnd(), MONTH_TYPE);
    }

    @Transient
    public Integer getExtremeEndDay(){
        return getPartialValue(getExtremeEnd(), DAY_TYPE);
    }

    public TimePeriod setExtremeStartYear(Integer year){
        return setExtremeStartField(year, YEAR_TYPE);
    }

    public TimePeriod setExtremeStartMonth(Integer month) throws IndexOutOfBoundsException{
        return setExtremeStartField(month, MONTH_TYPE);
    }

    public TimePeriod setExtremeStartDay(Integer day) throws IndexOutOfBoundsException{
        return setExtremeStartField(day, DAY_TYPE);
    }

    public TimePeriod setExtremeEndYear(Integer year){
        return setExtremeEndField(year, YEAR_TYPE);
    }

    public TimePeriod setExtremeEndMonth(Integer month) throws IndexOutOfBoundsException{
        return setExtremeEndField(month, MONTH_TYPE);
    }

    public TimePeriod setExtremeEndDay(Integer day) throws IndexOutOfBoundsException{
        return setExtremeEndField(day, DAY_TYPE);
    }

    @Transient
    private TimePeriod setExtremeStartField(Integer value, DateTimeFieldType type)
            throws IndexOutOfBoundsException{
        extremeStart = setPartialField(extremeStart, value, type);
        return this;
    }

    @Transient
    private TimePeriod setExtremeEndField(Integer value, DateTimeFieldType type)
            throws IndexOutOfBoundsException{
        extremeEnd = setPartialField(getExtremeEnd(), value, type);
        return this;
    }

//*********** EQUALS **********************************/

    //we want ExtendedTimePeriod and TimePeriod to be equal
    //if both are equal in the TimePeriod part and if
    //ExtendedTimePeriod has no verbatimDate defined

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        hashCode += (extremeStart == null? 43: extremeStart.hashCode()) +
                (extremeEnd == null? 47: extremeEnd.hashCode());
        return hashCode;
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
         return formatter.format(this);
     }

//*********** CLONE **********************************/

    @Override
    public ExtendedTimePeriod clone()  {
            ExtendedTimePeriod result = (ExtendedTimePeriod)super.clone();
            return result;
    }
}
