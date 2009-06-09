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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.Partial;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormatter;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.PartialBridge;
import eu.etaxonomy.cdm.jaxb.PartialAdapter;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:00
 * @updated 05-Dec-2008 23:00:05
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
	private static final Logger logger = Logger.getLogger(TimePeriod.class);
	private static final DateTimeFieldType monthType = DateTimeFieldType.monthOfYear();
	private static final DateTimeFieldType yearType = DateTimeFieldType.year();
	private static final DateTimeFieldType dayType = DateTimeFieldType.dayOfMonth();
	
	
	@XmlElement(name = "Start")
	@XmlJavaTypeAdapter(value = PartialAdapter.class)
	@Type(type="partialUserType")
	@Field(index = org.hibernate.search.annotations.Index.UN_TOKENIZED)
	@FieldBridge(impl = PartialBridge.class)
	private Partial start;
	
	@XmlElement(name = "End")
	@XmlJavaTypeAdapter(value = PartialAdapter.class)
	@Type(type="partialUserType")
	@Field(index = org.hibernate.search.annotations.Index.UN_TOKENIZED)
	@FieldBridge(impl = PartialBridge.class)
	private Partial end;

	
	@XmlElement(name = "FreeText")
	private String freeText;
	
	
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
			startDate = new Partial().with(yearType, startYear);
		}
		if (endYear != null){
			endDate = new Partial().with(yearType, endYear);
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
	
	
	public Partial getStart() {
		return start;
	}
	
	public void setStart(Partial start) {
		this.start = start;
	}
	
	public Partial getEnd() {
		return end;
	}
	
	public void setEnd(Partial end) {
		this.end = end;
	}
	
	/**
	 * @return the freeText
	 */
	protected String getFreeText() {
		return freeText;
	}


	/**
	 * @param freeText the freeText to set
	 */
	protected void setFreeText(String freeText) {
		this.freeText = freeText;
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
		return getPartialValue(start, yearType);
	}
	
	@Transient
	public Integer getStartMonth(){
		return getPartialValue(start, monthType);
	}

	@Transient
	public Integer getStartDay(){
		return getPartialValue(start, dayType);
	}

	@Transient
	public Integer getEndYear(){
		return getPartialValue(end, yearType);
	}

	@Transient
	public Integer getEndMonth(){
		return getPartialValue(end, monthType);
	}

	@Transient
	public Integer getEndDay(){
		return getPartialValue(end, dayType);
	}
	
	private Integer getPartialValue(Partial partial, DateTimeFieldType type){
		if (partial == null || ! partial.isSupported(type)){
			return null;
		}else{
			return partial.get(type);
		}
		
	}
	
	public TimePeriod setStartYear(Integer year){
		return setStartField(year, yearType);
	}
	
	public TimePeriod setStartMonth(Integer month) throws IndexOutOfBoundsException{
		return setStartField(month, monthType);
	}

	public TimePeriod setStartDay(Integer day) throws IndexOutOfBoundsException{
		return setStartField(day, dayType);
	}
	
	public TimePeriod setEndYear(Integer year){
		return setEndField(year, yearType);
	}

	public TimePeriod setEndMonth(Integer month) throws IndexOutOfBoundsException{
		return setEndField(month, monthType);
	}

	public TimePeriod setEndDay(Integer day) throws IndexOutOfBoundsException{
		return setEndField(day, dayType);
	}
	
	private TimePeriod setStartField(Integer value, DateTimeFieldType type) 
			throws IndexOutOfBoundsException{
		initStart();
		if (value == null){
			start = start.without(type);
		}else{
			checkFieldValues(value, type, start);
			start = this.start.with(type, value);
		}
		return this;
	}

	private TimePeriod setEndField(Integer value, DateTimeFieldType type)
			throws IndexOutOfBoundsException{
		initEnd();
		if (value == null){
			end = end.without(type);
		}else{
			checkFieldValues(value, type, end);
			end = this.end.with(type, value);
		}
		return this;
	}
	
	/**
	 * Throws an IndexOutOfBoundsException if the value does not have a valid value
	 * (e.g. month > 12, month < 1, day > 31, etc.)
	 * @param value
	 * @param type
	 * @throws IndexOutOfBoundsException
	 */
	private void checkFieldValues(Integer value, DateTimeFieldType type, Partial partial)
			throws IndexOutOfBoundsException{
		int max = 9999999;
		if (type.equals(monthType)){
			max = 12;
		}
		if (type.equals(dayType)){
			max = 31;
			Integer month = null;
			if (partial.isSupported(monthType)){
				month = partial.get(monthType);
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
	
	
	//patter for first year in string;
	private static Pattern firstYearPattern =  Pattern.compile("\\d{4}");
	//case "1806"[1807];
	private static Pattern uncorrectYearPatter =  Pattern.compile("\"\\d{4}\"\\s*\\[\\d{4}\\]");
	//case fl. 1806 or c. 1806 or fl. 1806?
	private static Pattern prefixedYearPattern =  Pattern.compile("fl\\.\\s*\\d{4}\\??");
	
	public static TimePeriod parseString(String strPeriod) throws IllegalArgumentException{
		//FIXME until now only quick and dirty and wrong
		TimePeriod result = null;
		if (strPeriod == null){
			return result;
		}
		strPeriod = strPeriod.trim();
		result = TimePeriod.NewInstance();
		result.setFreeText(strPeriod);
		
		//case "1806"[1807];
		if (uncorrectYearPatter.matcher(strPeriod).matches()){
			String realYear = strPeriod.split("\\[")[1];
			realYear = realYear.replace("]", "");
			result.setStartYear(Integer.valueOf(realYear));
		//case fl. 1806 or c. 1806 or fl. 1806?
		}else if(prefixedYearPattern.matcher(strPeriod).matches()){
			Matcher firstYearMatcher = firstYearPattern.matcher(strPeriod);
			firstYearMatcher.find();
			String firstYear = firstYearMatcher.group();
			result.setStartYear(Integer.valueOf(firstYear));
		}else{
			String[] years = strPeriod.split("-");
			Partial dtStart = null;
			Partial dtEnd = null;
			
			if (years.length > 2 || years.length <= 0){
				throw new IllegalArgumentException("More than 1 '-' in period String: " + strPeriod);
			}else {
				try {
					//start
					if (! CdmUtils.isEmpty(years[0])){
						dtStart = parseSingleDate(years[0]);
					}
					
					//end
					if (years.length >= 2 && ! CdmUtils.isEmpty(years[1])){
						if (years[1].length()==2 && dtStart != null && dtStart.isSupported(DateTimeFieldType.year())){
							years[1] = String.valueOf(dtStart.get(DateTimeFieldType.year())/100) + years[1];
						}
						dtEnd = parseSingleDate(years[1]);
					}
					result = TimePeriod.NewInstance(dtStart, dtEnd);
				} catch (IllegalArgumentException e) {
					logger.warn(e.getMessage());
				}
			}
		}
		return result;
	}
	
	
	protected static Partial parseSingleDate(String singleDateString) throws IllegalArgumentException{
		//FIXME until now only quick and dirty and incomplete
		Partial partial =  new Partial();
		if (CdmUtils.isNumeric(singleDateString)){
			try {
				Integer year = Integer.valueOf(singleDateString.trim());
				if (year > 1750 && year < 2100){
					partial = partial.with(yearType, year);
				}else{
					logger.warn("Not a valid taxonomic year: " + year + ". Year must be between 1750 and 2100");
				}
			} catch (NumberFormatException e) {
				logger.debug("Not a Integer format in getCalendar()");
				throw new IllegalArgumentException(e);
			}
		}else{
			throw new IllegalArgumentException("Until now only years can be parsed as single dates. But date is: " + singleDateString);
		}
		return partial;

	}
	
	
	
	private class TimePeriodPartialFormatter extends DateTimeFormatter{
		private TimePeriodPartialFormatter(){
			super(null, null);
		}
		public String print(ReadablePartial partial){
			//TODO
			String result = "";
			String year = (partial.isSupported(yearType))? String.valueOf(partial.get(yearType)):null;
			String month = (partial.isSupported(monthType))? String.valueOf(partial.get(monthType)):null;;
			String day = (partial.isSupported(dayType))? String.valueOf(partial.get(dayType)):null;;
			
			if (month !=null){
				if (year == null){
					year = "xxxx";
				}
			}
			if (day != null){
				if (month == null){
					month = "xx";
				}
				if (year == null){
					year = "xxxx";
				}
			}
			result = (day != null)? day + "." : "";
			result += (month != null)? month + "." : "";
			result += (year != null)? year : "";
			
			return result;
		}
		
	}
	
	public String toString(){
		String result = null;
		DateTimeFormatter formatter = new TimePeriodPartialFormatter();
		if (! CdmUtils.isEmpty(this.getFreeText())){
			result = this.getFreeText();
		}else{
			String strStart = start != null? start.toString(formatter): null;
			String strEnd = end != null? end.toString(formatter): null;
			result = CdmUtils.concat("-", strStart, strEnd);
		}
		return result;
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