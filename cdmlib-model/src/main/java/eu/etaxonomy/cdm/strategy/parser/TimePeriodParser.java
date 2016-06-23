/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * Class for parsing all types of date string to TimePeriod
 * @author a.mueller
 * @created 14-Jul-2013
 */
public class TimePeriodParser {
	private static final Logger logger = Logger.getLogger(TimePeriodParser.class);

	//patter for first year in string;
	private static final Pattern firstYearPattern =  Pattern.compile("\\d{4}");
	//case "1806"[1807];
	private static final Pattern uncorrectYearPatter = Pattern.compile(NonViralNameParserImplRegExBase.incorrectYearPhrase);
//OLD	        Pattern.compile("[\""+UTF8.ENGLISH_QUOT_START+"]\\d{4}[\""+UTF8.ENGLISH_QUOT_END+"]\\s*\\[\\d{4}\\]");

	//case fl. 1806 or c. 1806 or fl. 1806?
	private static final Pattern prefixedYearPattern =  Pattern.compile("(fl|c)\\.\\s*\\d{4}(\\s*-\\s*\\d{4})?\\??");
	//standard
	private static final Pattern standardPattern =  Pattern.compile("\\s*\\d{2,4}(\\s*-(\\s*\\d{2,4})?)?");
	private static final String strDotDate = "[0-3]?\\d\\.[01]?\\d\\.\\d{4,4}";
	private static final String strDotDatePeriodPattern = String.format("%s(\\s*-\\s*%s?)?", strDotDate, strDotDate);
	private static final Pattern dotDatePattern =  Pattern.compile(strDotDatePeriodPattern);
	private static final Pattern lifeSpanPattern =  Pattern.compile(String.format("%s--%s", firstYearPattern, firstYearPattern));


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
		}else if (lifeSpanPattern.matcher(periodString).matches()){
			parseLifeSpanPattern(periodString, result);
		}else if (standardPattern.matcher(periodString).matches()){
			parseStandardPattern(periodString, result);
//TODO first check ambiguity of parser results e.g. for 7/12/11
//			}else if (isDateString(periodString)){
//				String[] startEnd = makeStartEnd(periodString);
//				String start = startEnd[0];
//				DateTime startDateTime = dateStringParse(start, true);
//				result.setStart(startDateTime);
//				if (startEnd.length > 1){
//					DateTime endDateTime = dateStringParse(startEnd[1], true);
//					;
//					result.setEnd(endDateTime.toLocalDate());
//				}

		}else{
			result.setFreeText(periodString);
		}
		return result;
	}

	private static boolean isDateString(String periodString) {
		String[] startEnd = makeStartEnd(periodString);
		String start = startEnd[0];
		DateTime startDateTime = dateStringParse(start, true);
		if (startDateTime == null){
			return false;
		}
		if (startEnd.length > 1){
			DateTime endDateTime = dateStringParse(startEnd[1], true);
			if (endDateTime != null){
				return true;
			}
		}
		return false;
	}


	/**
	 * @param periodString
	 * @return
	 */
	private static String[] makeStartEnd(String periodString) {
		String[] startEnd = new String[]{periodString};
		if (periodString.contains("-") && periodString.matches("^-{2,}-^-{2,}")){
			startEnd = periodString.split("-");
		}
		return startEnd;
	}


	private static DateTime dateStringParse(String string, boolean strict) {
		DateFormat dateFormat = DateFormat.getDateInstance();
		ParsePosition pos = new ParsePosition(0);
		Date a = dateFormat.parse(string, pos);
		if (a == null || pos.getIndex() != string.length()){
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(a);
		DateTime result = new DateTime(cal);
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
				if (! StringUtils.isBlank(dates[0])){
					dtStart = parseSingleDotDate(dates[0].trim());
				}

				//end
				if (dates.length >= 2 && ! StringUtils.isBlank(dates[1])){
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

	private static void parseLifeSpanPattern(String periodString, TimePeriod result) {

		try{
			String[] years = periodString.split("--");
			String start = years[0];
			String end = years[1];

			result.setStartYear(Integer.valueOf(start));
			result.setEndYear(Integer.valueOf(end));
		} catch (Exception e) {
			//logger.warn(e.getMessage());
			result.setFreeText(periodString);
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
					partial = partial.with(TimePeriod.YEAR_TYPE, year);
				}else{
					partial = partial.with(TimePeriod.YEAR_TYPE, year);
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
				partial = partial.with(TimePeriod.YEAR_TYPE, year);
			}else{
				partial = partial.with(TimePeriod.YEAR_TYPE, year);
			}
			if (month != null && month != 0){
				partial = partial.with(TimePeriod.MONTH_TYPE, month);
			}
			if (day != null && day != 0){
				partial = partial.with(TimePeriod.DAY_TYPE, day);
			}
		} catch (NumberFormatException e) {
			logger.debug("Not a Integer format somewhere in " + singleDateString);
			throw new IllegalArgumentException(e);
		}
		return partial;

	}

}
