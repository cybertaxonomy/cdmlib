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
	public static final DateTimeFieldType MONTH_TYPE = DateTimeFieldType.monthOfYear();
	public static final DateTimeFieldType YEAR_TYPE = DateTimeFieldType.year();
	public static final DateTimeFieldType DAY_TYPE = DateTimeFieldType.dayOfMonth();
	
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
	
	/**
	 * True, if there is no start date and no end date and no freetext representation exists.
	 * @return
	 */
	@Transient
	public boolean isEmpty(){
		if (CdmUtils.isEmpty(this.getFreeText()) && start == null  && end == null ){
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
	
	public static Integer getPartialValue(Partial partial, DateTimeFieldType type){
		if (partial == null || ! partial.isSupported(type)){
			return null;
		}else{
			return partial.get(type);
		}
		
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
		if (value == null){
			return partial.without(type);
		}else{
			checkFieldValues(value, type, partial);
			return partial.with(type, value);
		}
	}
	
	private TimePeriod setStartField(Integer value, DateTimeFieldType type) 
			throws IndexOutOfBoundsException{
		start = setPartialField(start, value, type);
		return this;
	}

	private TimePeriod setEndField(Integer value, DateTimeFieldType type)
			throws IndexOutOfBoundsException{
		end = setPartialField(end, value, type);
		return this;
	}
	
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
	private static final Pattern firstYearPattern =  Pattern.compile("\\d{4}");
	//case "1806"[1807];
	private static final Pattern uncorrectYearPatter =  Pattern.compile("\"\\d{4}\"\\s*\\[\\d{4}\\]");
	//case fl. 1806 or c. 1806 or fl. 1806?
	private static final Pattern prefixedYearPattern =  Pattern.compile("(fl|c)\\.\\s*\\d{4}(\\s*-\\s*\\d{4})?\\??");
	//standard
	private static final Pattern standardPattern =  Pattern.compile("\\s*\\d{2,4}(\\s*-(\\s*\\d{2,4})?)?");
	private static final String strDotDate = "[0-3]?\\d\\.[01]?\\d\\.\\d{4,4}";
	private static final String strDotDatePeriodPattern = String.format("%s(\\s*-\\s*%s?)?", strDotDate, strDotDate);
	private static final Pattern dotDatePattern =  Pattern.compile(strDotDatePeriodPattern);
	
	
	public static TimePeriod parseString(TimePeriod timePeriod, String periodString){
		//TODO move to parser class
		//TODO until now only quick and dirty (and partly wrong)
		TimePeriod result = timePeriod;
		
		if(timePeriod == null){
			return timePeriod;
		}
		
		if (periodString == null){
			return result;
		}
		periodString = periodString.trim();
		
		result.setFreeText(null);
		
		//case "1806"[1807];
		if (uncorrectYearPatter.matcher(periodString).matches()){
			result.setFreeText(periodString);
			String realYear = periodString.split("\\[")[1];
			realYear = realYear.replace("]", "");
			result.setStartYear(Integer.valueOf(realYear));
			result.setFreeText(periodString);
		//case fl. 1806 or c. 1806 or fl. 1806?
		}else if(prefixedYearPattern.matcher(periodString).matches()){
			result.setFreeText(periodString);
			Matcher yearMatcher = firstYearPattern.matcher(periodString);
			yearMatcher.find();
			String startYear = yearMatcher.group();
			result.setStartYear(Integer.valueOf(startYear));
			if (yearMatcher.find()){
				String endYear = yearMatcher.group();
				result.setEndYear(Integer.valueOf(endYear));
			}
		}else if (dotDatePattern.matcher(periodString).matches()){
			parseDotDatePattern(periodString, result);
		}else if (standardPattern.matcher(periodString).matches()){
			parseStandardPattern(periodString, result);
		}else{
			result.setFreeText(periodString);
		}
		return result;
	}

	/**
	 * @param periodString
	 * @param result
	 */
	private static void parseDotDatePattern(String periodString,TimePeriod result) {
		String[] dates = periodString.split("-");
		Partial dtStart = null;
		Partial dtEnd = null;
		
		if (dates.length > 2 || dates.length <= 0){
			logger.warn("More than 1 '-' in period String: " + periodString);
			result.setFreeText(periodString);
		}else {
			try {
				//start
				if (! CdmUtils.isEmpty(dates[0])){
					dtStart = parseSingleDotDate(dates[0].trim());
				}
				
				//end
				if (dates.length >= 2 && ! CdmUtils.isEmpty(dates[1])){
					dtEnd = parseSingleDotDate(dates[1].trim());
				}
				
				result.setStart(dtStart);
				result.setEnd(dtEnd);
			} catch (IllegalArgumentException e) {
				//logger.warn(e.getMessage());
				result.setFreeText(periodString);
			}
		}
	}
	
	
	/**
	 * @param periodString
	 * @param result
	 */
	private static void parseStandardPattern(String periodString,
			TimePeriod result) {
		String[] years = periodString.split("-");
		Partial dtStart = null;
		Partial dtEnd = null;
		
		if (years.length > 2 || years.length <= 0){
			logger.warn("More than 1 '-' in period String: " + periodString);
		}else {
			try {
				//start
				if (! CdmUtils.isEmpty(years[0])){
					dtStart = parseSingleDate(years[0].trim());
				}
				
				//end
				if (years.length >= 2 && ! CdmUtils.isEmpty(years[1])){
					years[1] = years[1].trim();
					if (years[1].length()==2 && dtStart != null && dtStart.isSupported(DateTimeFieldType.year())){
						years[1] = String.valueOf(dtStart.get(DateTimeFieldType.year())/100) + years[1];
					}
					dtEnd = parseSingleDate(years[1]);
				}
				
				result.setStart(dtStart);
				result.setEnd(dtEnd);
			} catch (IllegalArgumentException e) {
				//logger.warn(e.getMessage());
				result.setFreeText(periodString);
			}
		}
	}
	
	public static TimePeriod parseString(String strPeriod) {
		TimePeriod timePeriod = TimePeriod.NewInstance();
		return parseString(timePeriod, strPeriod);
	}
	
	
	protected static Partial parseSingleDate(String singleDateString) throws IllegalArgumentException{
		//FIXME until now only quick and dirty and incomplete
		Partial partial =  new Partial();
		singleDateString = singleDateString.trim();
		if (CdmUtils.isNumeric(singleDateString)){
			try {
				Integer year = Integer.valueOf(singleDateString.trim());
				if (year < 1000 && year > 2100){
					logger.warn("Not a valid year: " + year + ". Year must be between 1000 and 2100");
				}else if (year < 1700 && year > 2100){
					logger.warn("Not a valid taxonomic year: " + year + ". Year must be between 1750 and 2100");
					partial = partial.with(YEAR_TYPE, year);
				}else{
					partial = partial.with(YEAR_TYPE, year);
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
	
	protected static Partial parseSingleDotDate(String singleDateString) throws IllegalArgumentException{
		Partial partial =  new Partial();
		singleDateString = singleDateString.trim();
		String[] split = singleDateString.split("\\.");
		int length = split.length;
		if (length > 3){
			throw new IllegalArgumentException(String.format("More than 2 dots in date '%s'", singleDateString));
		}
		String strYear = split[split.length-1];
		String strMonth = length >= 2? split[split.length-2]: null;
		String strDay = length >= 3? split[split.length-3]: null;
		
		
		try {
			Integer year = Integer.valueOf(strYear.trim());
			Integer month = Integer.valueOf(strMonth.trim());
			Integer day = Integer.valueOf(strDay.trim());
			if (year < 1000 && year > 2100){
				logger.warn("Not a valid year: " + year + ". Year must be between 1000 and 2100");
			}else if (year < 1700 && year > 2100){
				logger.warn("Not a valid taxonomic year: " + year + ". Year must be between 1750 and 2100");
				partial = partial.with(YEAR_TYPE, year);
			}else{
				partial = partial.with(YEAR_TYPE, year);
			}
			if (month != null && month != 0){
				partial = partial.with(MONTH_TYPE, month);
			}
			if (day != null && day != 0){
				partial = partial.with(DAY_TYPE, day);
			}
		} catch (NumberFormatException e) {
			logger.debug("Not a Integer format somewhere in " + singleDateString);
			throw new IllegalArgumentException(e);
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
			String year = (partial.isSupported(YEAR_TYPE))? String.valueOf(partial.get(YEAR_TYPE)):null;
			String month = (partial.isSupported(MONTH_TYPE))? String.valueOf(partial.get(MONTH_TYPE)):null;;
			String day = (partial.isSupported(DAY_TYPE))? String.valueOf(partial.get(DAY_TYPE)):null;;
			
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
	
//**************************** to String ****************************************	
	
	/** 
	 * Returns the {@link #getFreeText()} value if free text is not <code>null</code>.
	 * Otherwise the concatenation of <code>start</code> and <code>end</code> is returned. 
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String result = null;
		DateTimeFormatter formatter = new TimePeriodPartialFormatter();
		if ( CdmUtils.isNotEmpty(this.getFreeText())){
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