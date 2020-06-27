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
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;

/**
 * Class for parsing all types of date string to TimePeriod
 * @author a.mueller
 * @since 14-Jul-2013
 */
public class TimePeriodParser {
	private static final Logger logger = Logger.getLogger(TimePeriodParser.class);

	private static final String dotOrWs = "(\\.\\s*|\\s+)";
	private static final String dashOrWs = "(-"+TimePeriod.SEP+"\\s*|\\s+)";

	public static final String SEP = "(-|"+TimePeriod.SEP+"|"+UTF8.EN_DASH + ")";

	//patter for first year in string;
	private static final Pattern firstYearPattern =  Pattern.compile("\\d{4}");
	private static final String strDay = "[0-3]?\\d";
	//case "1806"[1807];
//	private static final Pattern uncorrectYearPatter = Pattern.compile(NonViralNameParserImplRegExBase.incorrectYearPhrase);

	//case fl. 1806 or c. 1806 or fl. 1806?
	private static final Pattern prefixedYearPattern =  Pattern.compile("(fl|c)\\.\\s*\\d{4}(\\s*-\\s*\\d{4})?\\??");
	//standard
	private static final Pattern standardPattern =  Pattern.compile("\\s*\\d{2,4}(\\s*-(\\s*\\d{2,4})?|\\+)?");
	private static final String strDotDate = strDay + "\\.[01]?\\d\\.\\d{4,4}";
	private static final String strDotDatePeriodPattern = String.format("%s(\\s*-\\s*%s|\\+)?", strDotDate, strDotDate);
	private static final Pattern dotDatePattern =  Pattern.compile(strDotDatePeriodPattern);
	private static final String strSlashDate = strDay + "\\/[01]?\\d\\/\\d{4,4}";
	private static final String strSlashDatePeriodPattern = String.format("%s(\\s*-\\s*%s|\\+)?", strSlashDate, strSlashDate);
	private static final Pattern slashDatePattern =  Pattern.compile(strSlashDatePeriodPattern);
	private static final Pattern lifeSpanPattern =  Pattern.compile(String.format("%s--%s", firstYearPattern, firstYearPattern));
	private static final String strMonthes = "((Jan|Feb|Aug|Sept?|Oct(ober)?|Nov|Dec)\\.?|(Mar(ch)?|Apr(il)?|Ma(yi)|June?|July?))";
	public static final String strDateWithMonthes = "("+ strDay + dotOrWs + ")?" + strMonthes + dotOrWs + "\\d{4,4}\\+?";
	public static final String strStartDateWithMonthes = "(" + strDay + "|(" + strDay + dotOrWs + ")?" + strMonthes + ")(" + dotOrWs + "\\d{4,4})?";
	public static final String strDateWithMonthesPeriod = "("+strStartDateWithMonthes +SEP+")?" + strDateWithMonthes;
    private static final Pattern dateWithMonthNamePattern = Pattern.compile(strDateWithMonthesPeriod);
    private static final String strDateYearMonthDay = "(\\d{4,4}" + dashOrWs + ")?" + strMonthes + "(" + dashOrWs + "[0-3]?\\d)?\\+?";
	private static final Pattern dateYearMonthDayPattern = Pattern.compile(strDateYearMonthDay);

	public static <T extends TimePeriod> T parseString(T timePeriod, String periodString){
		//TODO until now only quick and dirty (and partly wrong)
		T result = timePeriod;

		if(timePeriod == null){
			return timePeriod;
		}

		if (periodString == null){
			return result;
		}
		periodString = periodString.trim();

		result.setFreeText(null);

		//case "1806"[1807];  => TODO this should (and is?) handled in parse verbatim, should be removed here
//		if (uncorrectYearPatter.matcher(periodString).matches()){
//			result.setFreeText(periodString);
//			String realYear = periodString.split("\\[")[1];
//			realYear = realYear.replace("]", "");
//			result.setStartYear(Integer.valueOf(realYear));
//			result.setFreeText(periodString);
//	    }else
		//case fl. 1806 or c. 1806 or fl. 1806?  => TODO questionable if this should really be handled here, fl. probably stands for flowering and is not part of the date but of the date  context. What stands "c." for? Used by Markup import?
		if(prefixedYearPattern.matcher(periodString).matches()){
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
		}else if (slashDatePattern.matcher(periodString).matches()){
            parseSlashDatePattern(periodString, result);
        }else if (dateWithMonthNamePattern.matcher(periodString).matches()){
            parseDateWithMonthName(periodString, result);
        }else if (dateYearMonthDayPattern.matcher(periodString).matches()){
            parseDateYearMonthDay(periodString, result);
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

		}else if (isEnglishParsable(periodString)){
		    TimePeriod enDate = parseEnglishDate(periodString, null);
		    result.setStart(enDate.getStart());
		}else if (periodString.contains("T00:00:00")){
		    result = parseString(timePeriod, periodString.replace("T00:00:00", ""));
	    }else{
			result.setFreeText(periodString);
		}
		return result;
	}

    private static boolean isEnglishParsable(String periodString) {
        try {
            TimePeriod en = parseEnglishDate(periodString, null);
            if (en.getFreeText() == null){
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            return false;
        }
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

    private static void parseSlashDatePattern(String periodString, TimePeriod result) {
        String[] dates = periodString.split("-");
        Partial dtStart = null;
        Partial dtEnd = null;

        if (dates.length > 2 || dates.length <= 0){
            logger.warn("More than 1 '-' in period String: " + periodString);
            result.setFreeText(periodString);
        }else {
            try {
                dtEnd = handleContinued(dates, dtEnd);
                //start
                if (isNotBlank(dates[0])){
                    dtStart = parseSingleSlashDate(dates[0].trim());
                }

                //end
                if (dates.length >= 2 && isNotBlank(dates[1])){
                    dtEnd = parseSingleSlashDate(dates[1].trim());
                }

                result.setStart(dtStart);
                result.setEnd(dtEnd);
            } catch (IllegalArgumentException e) {
                //logger.warn(e.getMessage());
                result.setFreeText(periodString);
            }
        }
    }

	private static void parseDotDatePattern(String periodString,TimePeriod result) {
		String[] dates = periodString.split("-");
		Partial dtStart = null;
		Partial dtEnd = null;

		if (dates.length > 2 || dates.length <= 0){
			logger.warn("More than 1 '-' in period String: " + periodString);
			result.setFreeText(periodString);
		}else {
			try {
			    dtEnd = handleContinued(dates, dtEnd);
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

    /**
     * Checks if dates is a "continued" date (e.g. 2017+).
     * If yes, dtEnd is returned as {@link TimePeriod#CONTINUED} and dates[0] is shortened by "+".
     */
    protected static Partial handleContinued(String[] dates, Partial dtEnd) {
        if (dates.length == 1 && dates[0].endsWith("+") && dates[0].length()>1){
            dates[0] = dates[0].substring(0, dates[0].length()-1).trim();
            dtEnd = TimePeriod.CONTINUED;
        }
        return dtEnd;
    }

    private static void parseDateWithMonthName(String dateString, TimePeriod result) {
        String[] periods = dateString.split(SEP);
        if (periods.length > 2){
            logger.info("More than 2 periods in date string to parse: " + dateString);
            result.setFreeText(dateString);
        }else{
            if (periods[0].endsWith("+")){
                periods[0] = periods[0].substring(0, periods[0].length()-1).trim();
                result.setContinued(true);
            }
            Partial start = dateWithMonthPartial(periods[0]);
            Partial end = periods.length < 2? null : dateWithMonthPartial(periods[1]);
            if(start == null || (periods.length == 2 && end == null)){
                result.setFreeText(dateString);
            }else if (end != null){
                if (end.isSupported(TimePeriod.YEAR_TYPE)&& !start.isSupported(TimePeriod.YEAR_TYPE)){
                    start = start.with(TimePeriod.YEAR_TYPE, end.get(TimePeriod.YEAR_TYPE));
                }
                if(start.isSupported(TimePeriod.YEAR_TYPE)&& end.isSupported(TimePeriod.YEAR_TYPE) &&
                        (start.get(TimePeriod.YEAR_TYPE) == end.get(TimePeriod.YEAR_TYPE)) ||
                        (!start.isSupported(TimePeriod.YEAR_TYPE)&& !end.isSupported(TimePeriod.YEAR_TYPE))){
                    if (!start.isSupported(TimePeriod.MONTH_TYPE)&& end.isSupported(TimePeriod.MONTH_TYPE)){
                        start = start.with(TimePeriod.MONTH_TYPE, end.get(TimePeriod.MONTH_TYPE));
                    }
                }
            }
            result.setStart(start);
            result.setEnd(end);
        }
    }

    private static Partial dateWithMonthPartial(String dateString) {
        String[] dates = dateString.split("(\\.|\\s+)+");

        if (dates.length > 3){
            logger.info("More than 3 date parts in date string: " + dateString);
            return null;
        }else {
            String strDay2 = null;
            String strMonth = null;
            String strYear = null;
            int index = 0;
            boolean hasDay = dates[0].matches(strDay);
            if (hasDay){
                strDay2 = dates[index++];
            }
            if(dates.length > index){
                boolean isYear = dates[index].matches("\\d{4}");
                if(!isYear){
                    strMonth = dates[index++];
                }
                if(dates.length > index){
                    strYear = dates[index];
                }
            }
            try {
                Integer year = strYear == null ? null : Integer.valueOf(strYear.trim());
                Integer month = strMonth == null ? null : monthNrFromName(strMonth.trim());
                Integer day = strDay2 == null ? null : Integer.valueOf(strDay2.trim());

                Partial partial = makePartialFromDateParts(year, month, day);
                return partial;
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    private static void parseDateYearMonthDay(String dateString, TimePeriod result) {
        String[] dates = dateString.split(dashOrWs);

        if (dates.length > 3 || dates.length < 1){
            logger.warn("Not 2 or 3 date parts in date string: " + dateString);
            result.setFreeText(dateString);
        }else {

            boolean hasYear = dates[0].trim().matches("\\d{4,4}");
            String strYear = hasYear ? dates[0] : null;
            String strMonth = hasYear ? dates[1] : dates[0];
            String strDay = (hasYear && dates.length == 2 || dates.length == 1) ? null : hasYear ? dates[2] : dates[1];
            try {
                if (strDay != null && strDay.endsWith("+")){
                    strDay = strDay.substring(0, strDay.length()-1).trim();
                    result.setContinued(true);
                }
                Integer year = strYear == null ? null : Integer.valueOf(strYear.trim());
                Integer month = monthNrFromName(strMonth.trim());
                Integer day = strDay == null ? null : Integer.valueOf(strDay.trim());

                Partial partial = makePartialFromDateParts(year, month, day);

                result.setStart(partial);
            } catch (IllegalArgumentException e) {
                result.setFreeText(dateString);
            }
        }
    }

    public static Partial makePartialFromDateParts(Integer year, Integer month, Integer day) {
        Partial partial = new Partial();
        //TODO deduplicate code with other routines
        if (year != null){
            if (year < 1000 && year > 2100){
                logger.warn("Not a valid year: " + year + ". Year must be between 1000 and 2100");
            }else if (year < 1700 && year > 2100){
                logger.warn("Not a valid taxonomic year: " + year + ". Year must be between 1750 and 2100");
                partial = partial.with(TimePeriod.YEAR_TYPE, year);
            }else{
                partial = partial.with(TimePeriod.YEAR_TYPE, year);
            }
        }
        if (month != null && month != 0){
            partial = partial.with(TimePeriod.MONTH_TYPE, month);
        }
        if (day != null && day != 0){
            partial = partial.with(TimePeriod.DAY_TYPE, day);
        }
        return partial;
    }

    private static Integer monthNrFromName(String strMonth) {

        switch (strMonth.substring(0, 3)) {
            case "Jan":
                return 1;
            case "Feb":
                return 2;
            case "Mar":
                return 3;
            case "Apr":
                return 4;
            case "May":
                return 5;
            case "Mai":
                return 5;
            case "Jun":
                return 6;
            case "Jul":
                return 7;
            case "Aug":
                return 8;
            case "Sep":
                return 9;
            case "Oct":
                return 10;
            case "Okt":
                return 10;
            case "Nov":
                return 11;
            case "Dec":
                return 12;
            case "Dez":
                return 12;
            default:
                throw new IllegalArgumentException("Month not recognized: " + strMonth);
        }
    }

    //TODO "continued" not yet handled, probably looks different here (e.g. 2017--x)
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

	private static void parseStandardPattern(String periodString,
			TimePeriod result) {
		String[] years = periodString.split("-");
		Partial dtStart = null;
		Partial dtEnd = null;

		if (years.length > 2 || years.length <= 0){
			logger.warn("More than 1 '-' in period String: " + periodString);
		}else {
			try {
			    dtEnd = handleContinued(years, dtEnd);
				//start
				if (! StringUtils.isBlank(years[0])){
					dtStart = parseSingleDate(years[0].trim());
				}

				//end
				if (years.length >= 2 && ! StringUtils.isBlank(years[1])){
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

	public static VerbatimTimePeriod parseStringVerbatim(String strPeriod) {
	    VerbatimTimePeriod timePeriod = VerbatimTimePeriod.NewVerbatimInstance();
	    String strDateOnly = parseVerbatimPart(timePeriod, strPeriod);
	    timePeriod = parseString(timePeriod, strDateOnly);
	    if (timePeriod.getFreeText()!= null){
	        //if date could not be parsed, handle only as freetext
	        timePeriod.setFreeText(strPeriod.trim());
	    }
	    return timePeriod;
	}

	static Pattern patVerbatim1;
    static Pattern patVerbatim2;
    static Pattern patVerbatim3;

    public static String verbatimStart;
    public static String verbatimEnd;

    static {
        verbatimStart = "[\"'" + UTF8.QUOT_DBL_LEFT + UTF8.QUOT_SINGLE_HIGH_REV9 + UTF8.QUOT_DBL_LOW9 + "]";
        verbatimEnd = "[\"'" + UTF8.QUOT_DBL_RIGHT + UTF8.QUOT_SINGLE_RIGHT + UTF8.QUOT_DBL_HIGH_REV9 + "]";
        String fWs = "\\s*"; //facultative whitespace
        String oWs = "\\s+"; //obligate whitespace
        String anyDate = "([^\"]+)";
        String anyVerbatim = "(.*)";
        String bracketStart = "\\[";
        String bracketEnd = "\\]";

        //very first implementation, only for years and following 1 format
        String reVerbatim1 = anyDate + fWs + bracketStart + verbatimStart + anyVerbatim + verbatimEnd + bracketEnd;
        patVerbatim1 = Pattern.compile(reVerbatim1);

        String reVerbatim2 = verbatimStart + anyVerbatim + verbatimEnd + fWs + bracketStart + anyDate + bracketEnd;
        patVerbatim2 = Pattern.compile(reVerbatim2);

        String reVerbatim3 = anyVerbatim + "(" + oWs + "publ\\." + oWs + "(" + anyDate + "))";
        patVerbatim3 = Pattern.compile(reVerbatim3);
    }

    private static String parseVerbatimPart(VerbatimTimePeriod timePeriod, String strPeriod) {
        if (strPeriod == null){
            return null;
        }

        Matcher matcher = patVerbatim1.matcher(strPeriod);
        if (matcher.matches()){
            String verbatimDate = matcher.group(2).trim();
            timePeriod.setVerbatimDate(verbatimDate);
            strPeriod = matcher.group(1).trim();
        }

        matcher = patVerbatim2.matcher(strPeriod);
        if (matcher.matches()){
            String verbatimDate = matcher.group(1).trim();
            timePeriod.setVerbatimDate(verbatimDate);
            strPeriod = matcher.group(2).trim();
        }

        matcher = patVerbatim3.matcher(strPeriod);
        if (matcher.matches()){
            String verbatimDate = matcher.group(1).trim();
            timePeriod.setVerbatimDate(verbatimDate);
            strPeriod = matcher.group(3).trim();
        }
        return strPeriod;
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

	//this code is very redundant to parseSingleDotDate
   protected static Partial parseSingleSlashDate(String singleDateString) throws IllegalArgumentException{
        Partial partial =  new Partial();
        singleDateString = singleDateString.trim();
        String[] split = singleDateString.split("/");
        int length = split.length;
        if (length > 3){
            throw new IllegalArgumentException(String.format("More than 2 slashes in date '%s'", singleDateString));
        }
        String strYear = split[split.length-1];
        String strMonth = length >= 2? split[split.length-2]: null;
        String strDay = length >= 3? split[split.length-3]: null;

        try {
            Integer year = Integer.valueOf(strYear.trim());
            Integer month = strMonth == null? null : Integer.valueOf(strMonth.trim());
            Integer day = strDay == null? null : Integer.valueOf(strDay.trim());
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

    //this code is very redundant to parseSingleDotDate
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
			Integer month = strMonth == null? null : Integer.valueOf(strMonth.trim());
			Integer day = strDay == null? null : Integer.valueOf(strDay.trim());
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

    /**
     * @see #parseEnglishDate(String, String, boolean)
     * @param strFrom the string representing the first part of the period
     * @param strTo the string representing the second part of the period
     * @return the parsed period
     */
    public static TimePeriod parseEnglishDate(String strFrom, String strTo) {
        return parseEnglishDate(strFrom, strTo, false);
    }

    /**
     * Parses 1 or 2 dates of format yyyy-mm-dd, where y, m and d are numbers.
     *
     * @param timePeriod
     * @param strFrom the string representing the first part of the period
     * @param strTo the string representing the second part of the period
     * @param isAmerican
     * @return the parsed period
     */
    private static TimePeriod parseEnglishDate(String strFrom, String strTo, boolean isAmerican) {
        Partial dateTo = parseSingleEnglishDate(strTo, isAmerican);
        if (strFrom.endsWith("+") && dateTo == null){
            dateTo = TimePeriod.CONTINUED;
            strFrom = strFrom.substring(0, strFrom.length()-1).trim();
        }

        Partial dateFrom = parseSingleEnglishDate(strFrom, isAmerican);
        TimePeriod result = TimePeriod.NewInstance(dateFrom, dateTo);
        return result;
    }


    private static final String ENGLISH_FORMAT = "\\d{4}-\\d{1,2}-\\d{1,2}";

    private static Partial parseSingleEnglishDate(String strDate, boolean isAmerican) {
        if (StringUtils.isEmpty(strDate)){
            return null;
        }
        strDate = strDate.replace("\\s", "");
        if (!strDate.matches(ENGLISH_FORMAT)){
            throw new NumberFormatException("The given date is not of expected format yyyy-mm-dd: " + strDate);
        }
        String[] splits = strDate.split("-");
        Integer year = Integer.valueOf(splits[0]);
        Integer month = Integer.valueOf(splits[isAmerican? 2 : 1]);
        Integer day = Integer.valueOf(splits[isAmerican? 1 : 2]);
        //switch month and day if obvious
        if (month > 12 && day < 12){
            Integer tmp = month;
            month = day;
            day = tmp;
        }

        Partial result = makePartialFromDateParts(year, month, day);

        return result;
    }

    private static boolean isBlank(String str){
        return StringUtils.isBlank(str);
    }
    private static boolean isNotBlank(String str){
        return StringUtils.isNotBlank(str);
    }
}
