/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.Calendar;

import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Partial;
import org.joda.time.ReadableInstant;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:00
 */
@Embeddable
public class TimePeriod implements Cloneable{
	private static final Logger logger = Logger.getLogger(TimePeriod.class);
	
	
	private Partial start;
	private Partial end;

	
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
			startDate = new Partial().with(DateTimeFieldType.year(), startYear);
		}
		if (endYear != null){
			endDate = new Partial().with(DateTimeFieldType.year(), endYear);
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

	
	/**
	 * Transforms a <code>Calendar</code> into a <code>Partial</code>
	 * @param calendar
	 * @return
	 */
	public static Partial calendarToPartial(Calendar calendar){
		LocalDate ld = new LocalDate(calendar);
		Partial partial = new Partial(ld);
		return partial;
	}
	
	/**
	 * Transforms a <code>Calendar</code> into a <code>Partial</code>
	 * @param calendar
	 * @return
	 */
	public static Partial readableInstantToPartial(ReadableInstant readableInstant){
		DateTime dt = readableInstant.toInstant().toDateTime();
		LocalDate ld = dt.toLocalDate();
		Partial partial = new Partial(ld);
		return partial;
	}
	
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

	//@Temporal(TemporalType.TIMESTAMP)
	@Type(type="partialUserType")
	public Partial getStart() {
		return start;
	}
	public void setStart(Partial start) {
		this.start = start;
	}
	
	//@Temporal(TemporalType.TIMESTAMP)
	@Type(type="partialUserType")
	public Partial getEnd() {
		return end;
	}
	public void setEnd(Partial end) {
		this.end = end;
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
		return getPartialValue(start, DateTimeFieldType.year());
	}
	
	@Transient
	public Integer getStartMonth(){
		return getPartialValue(start, DateTimeFieldType.monthOfYear());
	}

	@Transient
	public Integer getStartDay(){
		return getPartialValue(start, DateTimeFieldType.dayOfMonth());
	}

	@Transient
	public Integer getEndYear(){
		return getPartialValue(end, DateTimeFieldType.year());
	}

	@Transient
	public Integer getEndMonth(){
		return getPartialValue(end, DateTimeFieldType.monthOfYear());
	}

	@Transient
	public Integer getEndDay(){
		return getPartialValue(end, DateTimeFieldType.dayOfMonth());
	}
	
	
	@Transient
	private Integer getPartialValue(Partial partial, DateTimeFieldType type){
		if (partial == null || ! partial.isSupported(type)){
			return null;
		}else{
			return partial.get(type);
		}
		
	}
	
	public TimePeriod setStartYear(Integer year){
		return setStartField(year, DateTimeFieldType.year());
	}
	
	public TimePeriod setStartMonth(Integer month) throws IndexOutOfBoundsException{
		return setStartField(month, DateTimeFieldType.monthOfYear());
	}

	public TimePeriod setStartDay(Integer day) throws IndexOutOfBoundsException{
		return setStartField(day, DateTimeFieldType.dayOfMonth());
	}
	
	public TimePeriod setEndYear(Integer year){
		return setEndField(year, DateTimeFieldType.year());
	}

	public TimePeriod setEndMonth(Integer month) throws IndexOutOfBoundsException{
		return setEndField(month, DateTimeFieldType.monthOfYear());
	}

	public TimePeriod setEndDay(Integer day) throws IndexOutOfBoundsException{
		return setEndField(day, DateTimeFieldType.dayOfMonth());
	}
	
	private TimePeriod setStartField(Integer value, DateTimeFieldType type) throws IndexOutOfBoundsException{
		initStart();
		if (value == null){
			start = start.without(type);
		}
		int max = 9999999;
		if (type.equals(DateTimeFieldType.monthOfYear())){
			max = 31;
		}
		if (type.equals(DateTimeFieldType.dayOfMonth())){
			max = 12;
		}
		if (value < 1 || value > max ){
			throw new IndexOutOfBoundsException("Value must be between 1 and " +  max);
		}
		start = this.start.with(type, value);
		return this;
	}

	private TimePeriod setEndField(Integer value, DateTimeFieldType type){
		initEnd();
		if (value == null){
			end = end.without(type);
		}
		int max = 9999999;
		if (type.equals(DateTimeFieldType.monthOfYear())){
			max = 31;
		}
		if (type.equals(DateTimeFieldType.dayOfMonth())){
			max = 12;
		}
		if (value < 1 || value > max ){
			throw new IndexOutOfBoundsException("Value must be between 1 and " +  max);
		}
		end = this.end.with(type, value);
		return this;
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
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Clone not supported exception. Should never occurr !!");
			return null;
		}
	}
	
}