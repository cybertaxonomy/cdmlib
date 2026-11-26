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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBridgeRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.format.common.TimePeriodFormatter;
import eu.etaxonomy.cdm.hibernate.search.PartialBridge;
import eu.etaxonomy.cdm.jaxb.PartialAdapter;
import eu.etaxonomy.cdm.jpa.PartialConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author m.doering
 * @since 08-Nov-2007 13:07:00
 * @updated 05-Dec-2008 23:00:05
 * @updated 14-Jul-2013 move parser methods to TimePeriodParser
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimePeriodBase", propOrder = {
        "start",
        "end",
        "freeText"
})
@MappedSuperclass
@Audited
public abstract class TimePeriodBase implements Cloneable, Serializable, ICheckEmpty {

    private static final long serialVersionUID = 3405969418194981401L;
    private static final Logger logger = LogManager.getLogger();

    public static final String SEP = UTF8.EN_DASH.toString(); //maybe this will be moved to a formatter class in future

    public static final Partial CONTINUED = Partial.of(9999, 11, 30);

    protected static TimePeriodFormatter formatter = TimePeriodFormatter.NewDefaultInstance();

    @XmlElement(name = "Start")
    @XmlJavaTypeAdapter(value = PartialAdapter.class)
    @Convert(converter = PartialConverter.class)
    @KeywordField(valueBridge = @ValueBridgeRef(type = PartialBridge.class))
    @JsonIgnore // currently used for swagger model scanner
    protected Partial start;

    @XmlElement(name = "End")
    @XmlJavaTypeAdapter(value = PartialAdapter.class)
    @Convert(converter = PartialConverter.class)
    @KeywordField(valueBridge = @ValueBridgeRef(type = PartialBridge.class))
    @JsonIgnore // currently used for swagger model scanner
    protected Partial end;

    @XmlElement(name = "FreeText")
    protected String freeText;

//****************** PARTIAL CONVERTERS ******************/

    public static Partial zonedDateTimeToPartial(ZonedDateTime dateTime){
        if (dateTime == null) {
            return null;
        }
        // date fields only (same as former Calendar/LocalDate conversion)
        return Partial.fromLocalDate(dateTime.toLocalDate());
    }

    /**
     * Transforms a {@link Calendar} into a <code>Partial</code>
     */
    public static Partial calendarToPartial(Calendar calendar){
        if (calendar == null){
            return null;
        }else{
            LocalDate ld = LocalDate.of(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));
            return Partial.fromLocalDate(ld);
        }
    }

    /**
     * Transforms a {@link ZonedDateTime} into a <code>Partial</code> including hour and minute.
     */
    public static Partial readableInstantToPartial(ZonedDateTime dateTime){
        return Partial.fromZonedDateTime(dateTime);
    }

    /**
     * Transforms an {@link Instant} into a <code>Partial</code> (UTC) including hour and minute.
     */
    public static Partial readableInstantToPartial(Instant instant){
        return Partial.fromInstant(instant);
    }

    /**
     * Transforms a {@link Date} into a <code>Partial</code> including hour and minute (system default zone).
     */
    public static Partial readableInstantToPartial(Date date){
        if (date == null) {
            return null;
        }
        return Partial.fromZonedDateTime(date.toInstant().atZone(ZoneId.systemDefault()));
    }

    /**
     * Transforms a {@link Date} into a <code>Partial</code> (date fields only).
     */
    public static Partial dateToPartial(Date date){
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
        return year == null ? null : Partial.ofYear(year);
    }
    public static Partial monthToPartial(Integer month){
        return month == null ? null : Partial.ofMonth(month);
    }
    public static Partial monthAndDayToPartial(Integer month, Integer day){
        if (month == null && day == null){
            return null;
        }
        Partial result = new Partial();
        if (month != null){
            result = result.withMonth(month);
        }
        if (day != null){
            result = result.withDay(day);
        }
        return result;
    }

    public static Integer getYear(Partial partial){
        return partial == null ? null : partial.getYear();
    }
    public static Integer getMonth(Partial partial){
        return partial == null ? null : partial.getMonth();
    }
    public static Integer getDay(Partial partial){
        return partial == null ? null : partial.getDay();
    }
    public static Integer getHour(Partial partial){
        return partial == null ? null : partial.getHour();
    }
    public static Integer getMinute(Partial partial){
        return partial == null ? null : partial.getMinute();
    }

    /**
     * Sets or clears the year on {@code partial}. Null {@code year} clears the field;
     * null {@code partial} starts from an empty {@link Partial}.
     */
    public static Partial withYear(Partial partial, Integer year){
        if (partial == null){
            partial = new Partial();
        }
        return partial.withYear(year);
    }

    /**
     * Sets or clears the month on {@code partial} (validated 1–12).
     * Null {@code month} clears the field; null {@code partial} starts empty.
     */
    public static Partial withMonth(Partial partial, Integer month)
            throws IndexOutOfBoundsException{
        if (partial == null){
            partial = new Partial();
        }
        if (month == null){
            return partial.withoutMonth();
        }
        checkMonth(month);
        return partial.withMonth(month);
    }

    /**
     * Sets or clears the day on {@code partial} (validated against month if present).
     * Null {@code day} clears the field; null {@code partial} starts empty.
     */
    public static Partial withDay(Partial partial, Integer day)
            throws IndexOutOfBoundsException{
        if (partial == null){
            partial = new Partial();
        }
        if (day == null){
            return partial.withoutDay();
        }
        checkDay(day, partial);
        return partial.withDay(day);
    }

    public static ZonedDateTime zonedDateTimeFromPartial(Partial partial,
            ZonedDateTime defaultDateTime) {

        if (partial == null) {
            return defaultDateTime;
        }
        defaultDateTime = defaultDateTime == null ? ZonedDateTime.now() : defaultDateTime;

        ZonedDateTime result = defaultDateTime;

        if (partial.hasYear()) {
            result = result.with(ChronoField.YEAR, partial.getYear());
        }
        if (partial.hasMonth()) {
            result = result.with(ChronoField.MONTH_OF_YEAR, partial.getMonth());
        }
        if (partial.hasDay()) {
            result = result.with(ChronoField.DAY_OF_MONTH, partial.getDay());
        }
        if (partial.hasHour()) {
            result = result.with(ChronoField.HOUR_OF_DAY, partial.getHour());
        }
        if (partial.hasMinute()) {
            result = result.with(ChronoField.MINUTE_OF_HOUR, partial.getMinute());
        }
        if (partial.hasSecond()) {
            result = result.with(ChronoField.SECOND_OF_MINUTE, partial.getSecond());
        }
        if (partial.hasMillis()) {
            result = result.with(ChronoField.MILLI_OF_SECOND, partial.getMillis());
        }

        return result;
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
    public static VerbatimTimePeriod toVerbatim(TimePeriodBase timePeriod){
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

//*************************** IS BLANK ******************************/

    public static boolean isBlank(TimePeriodBase timePeriod) {
        return timePeriod == null || timePeriod.isEmpty();
    }

//*********************** CONSTRUCTOR *********************************/

    protected TimePeriodBase() {
        super();
    }
    protected TimePeriodBase(Partial startDate, Partial endDate, String freeText) {
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
        if (StringUtils.isBlank(this.getFreeText()) && isEmpty(start) && isEmpty(end)){
            return true;
        }else{
            return false;
        }
    }

    @Transient
    public Integer getStartYear(){
        return getYear(start);
    }

    @Transient
    public Integer getStartMonth(){
        return getMonth(start);
    }

    @Transient
    public Integer getStartDay(){
        return getDay(start);
    }

    @Transient
    public Integer getEndYear(){
        return getYear(getEnd());
    }

    @Transient
    public Integer getEndMonth(){
        return getMonth(getEnd());
    }

    @Transient
    public Integer getEndDay(){
        return getDay(getEnd());
    }

    public TimePeriodBase setStartYear(Integer year){
        start = withYear(start, year);
        return this;
    }

    public TimePeriodBase setStartMonth(Integer month) throws IndexOutOfBoundsException{
        start = withMonth(start, month);
        return this;
    }

    public TimePeriodBase setStartDay(Integer day) throws IndexOutOfBoundsException{
        start = withDay(start, day);
        return this;
    }

    public TimePeriodBase setEndYear(Integer year){
        end = withYear(getEnd(), year);
        return this;
    }

    public TimePeriodBase setEndMonth(Integer month) throws IndexOutOfBoundsException{
        end = withMonth(getEnd(), month);
        return this;
    }

    public TimePeriodBase setEndDay(Integer day) throws IndexOutOfBoundsException{
        end = withDay(getEnd(), day);
        return this;
    }


// ******************************** internal methods *******************************/

    private static void checkMonth(Integer month) throws IndexOutOfBoundsException{
        if (month < 1 || month > 12){
            throw new IndexOutOfBoundsException("Value must be between 1 and 12");
        }
    }

    /**
     * Throws an IndexOutOfBoundsException if the day is not valid
     * (e.g. day > 31, day < 1, or day beyond month length when month is set).
     */
    private static void checkDay(Integer day, Partial partial) throws IndexOutOfBoundsException{
        int max = 31;
        Integer month = partial.getMonth();
        if (month != null){
            if (month == 2){
                max = 29;
            }else if (month == 4 || month == 6 || month == 9 || month == 11){
                max = 30;
            }
        }
        if (day < 1 || day > max){
            throw new IndexOutOfBoundsException("Value must be between 1 and " + max);
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
        return formatter.format(this);
    }

    /**
     * Returns the concatenation of <code>start</code> and <code>end</code>
     */
    public String getTimePeriod(){
        return formatter.getTimePeriod(this);
    }

    @Transient
    public String getYear(){
        return formatter.getYear(this);
    }

    /**
     * True, if there is no start date, no end date and no freetext representation.
     */
    @Override
    public boolean checkEmpty() {
        //TODO unify isEmpty && checkEmpty
        return isEmpty();
    }

    protected boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }

    protected boolean isEmpty(Partial partial) {
        return partial == null || partial.isEmpty();
    }

//*********** EQUALS **********************************/

    @Override
    public boolean equals(Object obj) {
        if (obj == null){
            return false;
        }
        if (! (obj instanceof TimePeriodBase)){
            return false;
        }
        TimePeriodBase that = (TimePeriodBase)obj;

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

    /**
     * Tests, if time period 1 and time period 2 are equal with <code>null</code> and
     * <code>empty</code> time periods all handled similar.<BR><BR>
     * <code>
     * equalsNullAndEmptySafe(null, null)  = true<BR>
     * equalsNullAndEmptySafe(null, empty)  = true<BR>
     * equalsNullAndEmptySafe(empty, null)  = true<BR>
     * equalsNullAndEmptySafe(empty, empty)  = true<BR>
     * equalsNullAndEmptySafe(null, not-empty)  = false<BR>
     * equalsNullAndEmptySafe(empty, not-empty)  = false<BR>
     * equalsNullAndEmptySafe(not-empty, null)  = false<BR>
     * equalsNullAndEmptySafe(not-empty, empty)  = false<BR>
     * equalsNullAndEmptySafe(not-empty1, not-empty2)  = not-empty.equals(not-empty2)<BR>
     * </code>
     */
    @SuppressWarnings("null")
    public static boolean equalsNullAndEmptySafe(TimePeriodBase timePeriod1, TimePeriodBase timePeriod2) {
        boolean tp1Empty = timePeriod1 == null || timePeriod1.isEmpty();
        boolean tp2Empty = timePeriod2 == null || timePeriod2.isEmpty();
        if (tp1Empty && tp2Empty) {
            return true;
        }else if (tp1Empty || tp2Empty) {
            return false;
        }else {
            return timePeriod1.equals(timePeriod2);
        }
    }

//*********** CLONE **********************************/

    @Override
    public TimePeriodBase clone()  {
        try {
            TimePeriodBase result = (TimePeriodBase)super.clone();
            copyCloned(this, result);
            return result;
        } catch (CloneNotSupportedException e) {
            logger.warn("Clone not supported exception. Should never occurr !!");
            return null;
        }
    }

    protected static void copyCloned(TimePeriodBase origin, TimePeriodBase target) {
        target.setStart(origin.start);   //DateTime is immutable
        target.setEnd(origin.end);
        target.setFreeText(origin.freeText);
    }
}
