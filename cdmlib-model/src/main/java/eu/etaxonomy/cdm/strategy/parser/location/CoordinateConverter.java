/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*
* This file is an Java adaption from the orginal CoordinateConverter written by Dominik Mikiewicz
* @see www.cartomatic.pl
* @see http://dev.e-taxonomy.eu/svn/trunk/geo/coordinateConverter/CoordinateConverter.cs
* @see http://gis.miiz.waw.pl/webapps/coordinateconverter/
*/
package eu.etaxonomy.cdm.strategy.parser.location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;

/**
 * @author a.mueller
 * @since 07.06.2010
 *
 */
public class CoordinateConverter {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CoordinateConverter.class);

    //Patterns
    private List<CoordinatePattern> patterns;

    private static String minuteUtf8 = "\u02B9|\u00B4|\u02CA|\u0301|\u0374|\u2019";
    private static String secondUtf8 = "\u02BA|\u030B|\u2033|\u00B4\u00B4|\u201D";


    private class CoordinatePattern{
    	String description;
    	String pattern;
    }


    private Comparator<CustomHemisphereIndicator> lengthComparator = new Comparator<CustomHemisphereIndicator>(){
		@Override
        public int compare(CustomHemisphereIndicator ind1, CustomHemisphereIndicator ind2) {
			return Integer.valueOf(ind1.getLength()).compareTo(ind2.getLength());
		}
    };

    //Class constructor
    public CoordinateConverter() {
        //initialise pattern array
        patterns = new ArrayList<CoordinatePattern>();

        //temp pattern variable
        CoordinatePattern pattern;


        //variations of DD.DDD with white space characters
        pattern = new CoordinatePattern();
        pattern.description = "Variation of DD.DDD";
        pattern.pattern =
            //+/-/Nn/Ss/Ww/EeDD.DDDD
            "(^" +
            "(\\s)*(\\+|-|W|w|E|e|N|n|S|s)?(\\s)*" +
            "((\\d{1,3}(\\.|\\,)?(\\s)*$)|(\\d{1,3}(\\.|\\,)\\d+(\\s)*$))" +
            ")" +
            ////DD.DDDDNn/Ss/Ww/Ee
            "|(^" +
            "(\\s)*((\\d{1,3}(\\.|\\,)?(\\s)*)|(\\d{1,3}(\\.|\\,)\\d+(\\s)*))" +
            "(W|w|E|e|N|n|S|s)?(\\s)*$" +
            ")";
        patterns.add(pattern);


        //Variations of DD(\u00B0|d)MM.MMM' with whitespace characters
        pattern = new CoordinatePattern();
        pattern.description = "Variation of DD(\u00B0|d)MM.MMM('|m)";
        pattern.pattern =
            "(^" +
            "(\\s)*(\\+|-|W|w|E|e|N|n|S|s)?(\\s)*" +
            "((\\d{1,3}(\\s)*(\u00B0|\u00BA|D|d)?(\\s)*$)|(\\d{1,3}(\\s)*(\u00B0|\u00BA|D|d)(\\s)*\\d{1,2}(\\.|\\,)?("+ minuteUtf8 + "|'|M|m)?$)|(\\d{1,3}(\\s)*(\u00B0|\u00BA|D|d)(\\s)*\\d{1,2}(\\.|\\,)\\d+(\\s)*("+ minuteUtf8 + "|'|M|m)?(\\s)*$))" +
            ")" +
            "|(^" +
            "(\\s)*((\\d{1,3}(\\s)*(\u00B0|\u00BA|D|d)?(\\s)*)|(\\d{1,3}(\\s)*(\u00B0|\u00BA|D|d)(\\s)*\\d{1,2}(\\.|\\,)?("+ minuteUtf8 + "|'|M|m)?)|(\\d{1,3}(\\s)*(\u00B0|\u00BA|D|d)(\\s)*\\d{1,2}(\\.|\\,)\\d+(\\s)*("+ minuteUtf8 + "|'|M|m)?(\\s)*))" +
            "(W|w|E|e|N|n|S|s)?(\\s)*$" +
            ")";
        patterns.add(pattern);


        //Variations of DD\u00B0MM'SS.SSS" with whitespace characters
        pattern = new CoordinatePattern();
        pattern.description = "Variation of DD(\u00B0|d)MM("+ minuteUtf8 + "|m)SS.SSS("+secondUtf8+"|s)";
        pattern.pattern =
            //+/-/Nn/Ss/Ww/EeDD\u00B0MM"+ minuteUtf8 + "SS.SSS
            "(^" +
            "(\\s)*(\\+|-|W|w|E|e|N|n|S|s)?(\\s)*" +
            "((\\d{1,3}(\\s)*(\u00B0|\u00BA|D|d)?(\\s)*$)" +
            "|(\\d{1,3}(\\s)*(\u00B0|\u00BA|D|d)(\\s)*\\d{1,2}(\\s)*("+ minuteUtf8 + "|'|M|m)?(\\s)*$)" +
            "|(\\d{1,3}(\\s)*(\u00B0|\u00BA|D|d)(\\s)*\\d{1,2}(\\s)*("+ minuteUtf8 + "|'|M|m)(\\s)*\\d{1,2}(\\.|\\,)?(\\s)*("+secondUtf8+"|\"|''|S|s)?(\\s)*$)" +
            "|(\\d{1,3}(\\s)*(\u00B0|\u00BA|D|d)(\\s)*\\d{1,2}(\\s)*("+ minuteUtf8 + "|'|M|m)(\\s)*\\d{1,2}(\\.|\\,)\\d+(\\s)*("+secondUtf8+"|\"|''|S|s)?(\\s)*$))" +
            ")" +
            //DD°MM"+ minuteUtf8 + "SS.SSSNn/Ss/Ww/Ee
            "|(^(\\s)*" +
            "((\\d{1,3}(\\s)*(\u00B0|\u00BA|D|d)?(\\s)*)|" +
            "(\\d{1,3}(\\s)*(\u00B0|\u00BA|D|d)(\\s)*\\d{1,2}(\\s)*("+ minuteUtf8 + "|'|M|m)?(\\s)*)|" +
            "(\\d{1,3}(\\s)*(\u00B0|\u00BA|D|d)(\\s)*\\d{1,2}(\\s)*("+ minuteUtf8 + "|'|M|m)(\\s)*\\d{1,2}(\\.|\\,)?(\\s)*("+secondUtf8+"|\"|''|S|s)?(\\s)*)|" +
            "(\\d{1,3}(\\s)*(\u00B0|\u00BA|D|d)(\\s)*\\d{1,2}(\\s)*("+ minuteUtf8 + "|'|M|m)(\\s)*\\d{1,2}(\\.|\\,)\\d+(\\s)*("+secondUtf8+"|\"|''|S|s)?(\\s)*))" +
            "(W|w|E|e|N|n|S|s)?(\\s)*$" +
            ")";
        patterns.add(pattern);


        //Variations of DD:MM:SS.SSS with whitespace characters
        pattern = new CoordinatePattern();
        pattern.description = "Variation of DD:MM:SS.SSS";
        pattern.pattern =
            // +/-/Nn/Ss/Ww/EeDD:MM:SS.SSS
            "(^" +
            "(\\s)*(\\+|-|W|w|E|e|N|n|S|s)?(\\s)*" +
            "((\\d{1,3}(\\s)*\\:?(\\s)*$)|(\\d{1,3}(\\s)*\\:(\\s)*\\d{1,2}(\\s)*\\:?(\\s)*$)|(\\d{1,3}(\\s)*\\:(\\s)*\\d{1,2}(\\s)*\\:(\\s)*\\d{1,2}(\\.|\\,)?(\\s)*$)|(\\d{1,3}(\\s)*\\:(\\s)*\\d{1,2}(\\s)*\\:(\\s)*\\d{1,2}(\\.|\\,)\\d+(\\s)*$))" +
            ")" +
            //DD:MM:SS.SSSNn/Ss/Ww/Ee
            "|(^" +
            "(\\s)*((\\d{1,3}(\\s)*\\:?(\\s)*)|(\\d{1,3}(\\s)*\\:(\\s)*\\d{1,2}(\\s)*\\:?(\\s)*)|(\\d{1,3}(\\s)*\\:(\\s)*\\d{1,2}(\\s)*\\:(\\s)*\\d{1,2}(\\.|\\,)?(\\s)*)|(\\d{1,3}(\\s)*\\:(\\s)*\\d{1,2}(\\s)*\\:(\\s)*\\d{1,2}(\\.|\\,)\\d+(\\s)*))" +
            "(W|w|E|e|N|n|S|s)?(\\s)*$" +
            ")";
        patterns.add(pattern);

    }


    //tests if a string matches one of the defined patterns
    private int matchPattern(String str){
        int recognised = -1;

        //match the string against each available patern
        for (int i = 0; i < patterns.size(); i++){

        	CoordinatePattern pattern = patterns.get(i);
        	Pattern regEx = Pattern.compile(pattern.pattern);
        	if (regEx.matcher(str).find()) {
        		recognised = i;
            	break;
            }

        }
        return recognised;
    }


    //gets sign of the coordinate (tests for presence of negative sign)
    private int getSign(String str){

        //This regex checks for the negative hemisphere indicator
        Pattern regexNegative = Pattern.compile("(-|S|s|W|w)");

        //This regex checks if there weren't any other hemisphere indicators
        //it is needed for the specific case of the DDdMMmSSs S
        //so it needs to be ensured there where no positive indicators
        Pattern regexPositive = Pattern.compile("(\\+|N|n|E|e)");

        //if a positive indicator is found no need to search further
        if (regexPositive.matcher(str).find()){
            return 1;
        }else{
            //if not check whether there was a negative indicator. if so negate otherwise return positive
            if (regexNegative.matcher(str).find()){
                return -1;
            }else{
                return 1;
            }
        }
    }


    //this checks for the coordinate sign by evaluating user supplied data
    private int getCustomSign(String str){
        //Note:
        //Indicators are evaluated from the longest ones to the shortes ones
        //So when searching for "P" does not affect "PN" as "PN" is evaluated earlier


        //search for the presence of indicators
        boolean hasPositive = false;
        boolean hasNegative = false;

        //keep previous negative indicators here
        List<String> previousNegatives = new ArrayList<String>();

        //compare the string with user supplied custom pattern
        for (int x = customPtrn.hemisphereIndicators.size() - 1; x >= 0; x--){

            CustomHemisphereIndicator ind = customPtrn.hemisphereIndicators.get(x);

            //test here if the indicator exists (has length >0)
            if (ind.getLength() > 0){

                //check if the supplied pattern was marked as case insensitive?
                String caseInsensitive = "";

                if (customPtrn.caseInsensitive){
                    caseInsensitive = "(?i)";
                }

                //create a regex
                Pattern tempRegex = Pattern.compile(caseInsensitive + ind.getIndicator());

                //if a pattern is found
                if (tempRegex.matcher(str).find()){
                    //check whether it's a positive or negative indicator
                    if (ind.getPositive()){
                        /* Note:
                         * See the note below to understand why checking for previous negatives is performed here
                        */

                        //check the previous negatives
                        if (previousNegatives.size() != 0){
                            boolean sameNegative = false;

                            for (int i = previousNegatives.size() - 1; i >= 0; i--){
                                if (ind.getIndicator() == previousNegatives.get(i)){
                                    sameNegative = true;
                                    break;
                                }
                            }

                            //mark as positive only if the previously found negative is the same
                            if (sameNegative){
                                hasPositive = true;
                            }

                        }else{ //if no negatives before it already marks the sign as positive
                            hasPositive = true;
                        }

                    } else {
                        /* Note:
                         * save the negative indicator here so it can be compared later if a positive wants to overwrite it!
                         * in a case a longer negative "Pn" has already been found a shorter positive "P" will not overwrite it
                         * and the hasPositive will remain false;
                         * In a case a "P" negative indicator has already been found, positive will mark hasPositive and therefore
                         * later a default positive value will be returned (if the indicators for positive & negative are the same
                         * positive is returned)
                         * testing for previous positives is not required since if a hasPositive is already true method will return
                         * true anyway
                         *
                        */
                        previousNegatives.add(ind.getIndicator());

                        hasNegative = true;

                    }
                }
            }

        }

        //Note:
        //positive indicator has priority here - if both indicators supplied by the user are the same, a positive is chosen
        //if there were no indicator found in the tested coordinate, a positive value is returned by default

        if (hasPositive){
            return 1;
        } else {
            if (hasNegative) {
                return -1;
            } else {
                return 1;
            }
        }
    }



    //returns a currently used decimal separator
    private String getDecimalSeparator(){
    	//TODO not yet transformed from C#
//    	return System.Globalization.NumberFormatInfo.CurrentInfo.NumberDecimalSeparator;
        return ".";
    }


    //replaces comma or dot for current decimal separator
    private String fixDecimalSeparator(String str){
        //Note:
        //Coma is replaced as parsers often recognise dot as a decimal separator
        //Comma or dot is replaced with a decimal separator here (environment settings)
        //But decimal separator has to be used later too;

        String regExReplaceComma = "(\\,|\\.)";
        str = str.replaceAll(regExReplaceComma, getDecimalSeparator());

        return str;
    }


    //removes sign
    private String removeSign(String str){
        String regExRemoveSign = "(\\+|-|S|s|W|w|N|n|E|e)";
        str = str.replaceAll(regExRemoveSign, "");
        return str;
    }

    //removes custom sign indicators
    private String removeCustomPatternParts(String str){

        /* Note:
         * Symbols are added here so the removing tries to not affect the coordinate too much
         * Strings to be removed then are evaluated from the longest ones to the shortes ones
         * So when searching for "P" does not affect "PN" as "PN" is evaluated earlier
         * */

        //CustomHemisphereIndicator is used here so another object does not have to be created
        //only for the string cleanning
        List<CustomHemisphereIndicator> stringsToRemove = customPtrn.hemisphereIndicators;

        //add degree symbol
        CustomHemisphereIndicator stringToRemove = new CustomHemisphereIndicator("Degree", customPtrn.degreeSymbol,customPtrn.degreeSymbol.length(), false);
        stringsToRemove.add(stringToRemove);

        //add minute symbol
        stringToRemove = new CustomHemisphereIndicator("Minute", customPtrn.minuteSymbol, customPtrn.minuteSymbol.length(), false);
        stringsToRemove.add(stringToRemove);

        //add second symbol
        stringToRemove = new CustomHemisphereIndicator("Second", customPtrn.secondSymbol, customPtrn.secondSymbol.length(), false);
        stringsToRemove.add(stringToRemove);

        //sort the list (by element's Length property)
        Collections.sort(stringsToRemove, lengthComparator);


//        ListSelectionEv.sort(lengthComparator);


        for (int x = stringsToRemove.size() - 1; x >= 0; x--){

            CustomHemisphereIndicator toBeRemoved = stringsToRemove.get(x);

            //check if the string exists so replacing does not yield errors
            if (toBeRemoved.getLength() > 0)
            {
                //check if the supplied pattern was marked as case insensitive?
                String CaseInsensitive = "";

                if (customPtrn.caseInsensitive){
                    CaseInsensitive = "(?i)";
                }

                //create regex for replacing
                String tempRegex = CaseInsensitive + toBeRemoved.getIndicator();


                if (toBeRemoved.getName().equals("Degree") || toBeRemoved.getName().equals("Minute")) {
                    //replace with a symbol used later for splitting
                    str =  str.replaceAll(tempRegex, ":");
                } else {
                    //remove the string
                    str = str.replaceAll(tempRegex, "");
                }
            }
        }
        return str;
    }



    //removes whitespace characters
    private String removeWhiteSpace(String str){
        str = str.replaceFirst("\\s+", "");
        return str;
    }


    //Object for the conversion results
    public class ConversionResults{
        public boolean patternRecognised;
        public String patternMatched;
        public String patternType;

        public boolean conversionSuccessful;
        public double convertedCoord;
        public boolean canBeLat;

        public String conversionComments;

        public Boolean isLongitude;

        public int dd;
        public int mm;
        public double mmm;
        public int ss;
        public double sss;

    }


    public ConversionResults tryConvert(String str){
        //some local variables
        int sign; //sign of the coordinate
        String[] decimalBit, ddmmss, ddmm; //arrays for splitting
        double dd = 0, mm = 0, ss = 0, mmm = 0, sss = 0, dec = 0; //parts of the coordinates

        String decSeparatorRaw = String.valueOf(getDecimalSeparator()); //gets the current decimal separator
        String decSeparatorRegEx = decSeparatorRaw.replace(".", "\\.");

        ConversionResults results = new ConversionResults();

        //Get the matched pattern
        CoordinatePattern pattern;
        int ptrnnum = matchPattern(str);
        if (ptrnnum != -1) {
            pattern = patterns.get(ptrnnum);
        } else {
            pattern = new CoordinatePattern();
            pattern.description = "Unknown";
            pattern.pattern = "No pattern matched";
        }



        if (pattern.description.equals("Variation of DD.DDD")){

       	  	//Sets pattern machted, successful, pattern type and pattern info
        	initializeResult(results, pattern);

            //get sign
            sign = getSign(str);
            results.isLongitude = getIsLongitude(str);

            //Replace comma or dot with a current decimal separator
            str = fixDecimalSeparator(str);

            //Remove all the unwanted stuff
            str = removeSign(str);
            str = removeWhiteSpace(str);

            //Since this is already a decimal degree no spliting is needed
            dd =  Double.valueOf(str);

            checkDegreeRange(dd, results);
            doConvertWithCheck(sign, dd, mm, mmm, ss, sss, results);

        }else if (pattern.description.equals("Variation of DD(\u00B0|d)MM.MMM('|m)")){

        	//Sets pattern machted, successful, pattern type and pattern info
        	initializeResult(results, pattern);

            //get sign
            sign = getSign(str);
            results.isLongitude = getIsLongitude(str);

            //Replace comma or dot with a current decimal separator
            str = fixDecimalSeparator(str);

            //Remove all the unwanted stuff
            str = removeSign(str);
            str = removeWhiteSpace(str);

            //do some further replacing
            //Replace degree symbol
            str = str.replaceAll("(\u00B0|\u00BA|D|d)", ":");

            //remove minute symbol
            str = str.replaceAll("("+ minuteUtf8 + "|'|M|m)", "");

            //Extract decimal part
            decimalBit = str.split(decSeparatorRegEx);

            //split degrees and minutes
            ddmm = decimalBit[0].split(":");


            //extract values from the strings
            dd = Integer.valueOf(ddmm[0]); //Degrees

            if (ddmm.length > 1){ //Minutes
                //check if the string is not empty
                if (ddmm[1] != "") {
                	mm = Integer.valueOf(ddmm[1]);
                }
            }

            if (decimalBit.length > 1){//DecimalSeconds
                //check if the string is not empty
                if (decimalBit[1] != "") {
                    mmm = Double.valueOf(decimalBit[1]) / Math.pow(10, (decimalBit[1].length()));
                }
            }

            checkDegreeRange(dd, results);
            checkMinuteRange(mm, results);
            doConvertWithCheck(sign, dd, mm, mmm, ss, sss, results);

        }else if (pattern.description.equals("Variation of DD(\u00B0|d)MM("+ minuteUtf8 + "|m)SS.SSS("+secondUtf8+"|s)")){

        	/*
             * Note:
             * This pattern allows the seconds to be specified with S, s or " or nothing at all
             * If the seconds are marked with "s" and there is no other indication of the hemisphere
             * the coordinate will be parsed as southern (negative).
             *
             * If the N / E / W / + indicator is found the coordinate will be parsed appropriately no matter
             * what is the second notation
            */

        	//Sets pattern machted, successful, pattern type and pattern info
        	initializeResult(results, pattern);

            //get sign
            sign = getSign(str);
            //TODO test S
            results.isLongitude = getIsLongitude(str);

            //Replace comma or dot with a current decimal separator
            str = fixDecimalSeparator(str);

            //Remove all the unwanted stuff
            str = removeSign(str);
            str = removeWhiteSpace(str);

            //remove second symbol (s is removed by the get sign method)
            //double apostrophe is not removed here as single apostrphe may mark minutes!
            //it's taken care of later after extracting the decimal part
            str = str.replaceAll("("+secondUtf8+"|\")", "");

            //do some further replacing
            //Replace degree symbol
            str = str.replaceAll("(\u00B0|\u00B0|D|d|"+ minuteUtf8 + "|'|M|m)",":");

            //Extract decimal part
            decimalBit = str.split(decSeparatorRegEx);

            //remove : from the decimal part [1]! This is needed when a double apostrophe was used to mark seconds
            if (decimalBit.length > 1)
            {
                decimalBit[1].replace(":", "");
            }

            //split degrees and minutes
            ddmmss = decimalBit[0].split(":");


            //extract values from the strings
            dd = Integer.valueOf(ddmmss[0]); //Degrees
            if (ddmmss.length > 1){//Minutes
                //check if the string is not empty
                if (ddmmss[1] != "") {
                	mm = Integer.valueOf(ddmmss[1]);
                }
            }
            if (ddmmss.length > 2){//Seconds
                //check if the string is not empty
                if (ddmmss[2] != "") {
                	ss = Integer.valueOf(Nz(ddmmss[2]).trim());
                }
            }
            if (decimalBit.length > 1) { //DecimalSeconds
                //check if the string is not empty
                if (decimalBit[1] != "") {
                    sss = Double.valueOf(decimalBit[1]) / Math.pow(10, (decimalBit[1].length()));
                }
            }

            checkDegreeRange(dd, results);
            checkMinuteRange(mm, results);
            checkSecondRange(ss, results);

            doConvertWithCheck(sign, dd, mm, mmm, ss, sss, results);

        }else if (pattern.description.equals("Variation of DD:MM:SS.SSS")){

        	//Sets pattern machted, successful, pattern type and pattern info
        	initializeResult(results, pattern);

            //get sign
            sign = getSign(str);
            results.isLongitude = getIsLongitude(str);

            //Replace comma or dot with a current decimal separator
            str = fixDecimalSeparator(str);

            //Remove all the unwanted stuff
            str = removeSign(str);
            str = removeWhiteSpace(str);

            //Do some splitting
            decimalBit = str.split(decSeparatorRegEx);
            ddmmss = decimalBit[0].split(":");


            //extract values from the strings
            dd = Integer.valueOf(ddmmss[0]); //Degrees
            if (ddmmss.length > 1)//Minutes
            {
                //check if the string is not empty
                if (ddmmss[1] != "") { mm = Integer.valueOf(ddmmss[1]); }
            }
            if (ddmmss.length > 2) {//Seconds{
                //check if the string is not empty
                if (ddmmss[2] != "") {
                	ss = Integer.valueOf(ddmmss[2]);
                }
            }
            if (decimalBit.length > 1) { //DecimalSeconds
                //check if the string is not empty
                if (decimalBit[1] != "") {
                    sss = Double.valueOf(decimalBit[1]) / Math.pow(10, (decimalBit[1].length()));
                }
            }

            checkDegreeRange(dd, results);
            checkMinuteRange(mm, results);
            checkSecondRange(ss, results);

            doConvertWithCheck(sign, dd, mm, mmm, ss, sss, results);

        }else if (pattern.description.equals("Custom variation of DD.DDD")){

        	//Sets pattern machted, successful, pattern type and pattern info
        	initializeResult(results, pattern);


	        //get sign
	        sign = getCustomSign(str);

	        //TODO still needs to be adapted to custom pattern
	        results.isLongitude = getIsLongitude(str);


	        //Remove all the unwanted stuff
	        //Note: This method also replaces the symbols with ":"
	        //Note: In certain cases it may make the coord unparsable
	        str = removeCustomPatternParts(str);

	        str = removeWhiteSpace(str);

	        //Replace comma or dot with a current decimal separator
	        str = fixDecimalSeparator(str);

	        //remove the ":" here as it is not needed here for decimal degrees
	        str = str.replace(":", "");

	        try {
	            //Since this is already a decimal degree no spliting is needed
	            dd = Double.valueOf(str);
	        } catch (Exception e)  {
	            results.conversionSuccessful = false;
	            results.convertedCoord = 99999; //this is to mark an error...
	            results.conversionComments =
	                "It looks like the supplied pattern has some ambiguous elements and the parser was unable to parse the coordinate." +
	                "<br/>If the supplied symbols used for marking degrees, minutes or seconds contain hemisphere indicators, " +
	                "the parser is likely to fail or yield rubbish results even though the pattern itself has been recognised."
	                ;

	            //exit method
	            return results;
	        }

	        //Since this is already a decimal degree no spliting is needed
	        dd = Double.valueOf(str);

	        checkDegreeRange(dd, results);
	        doConvertWithCheck(sign, dd, mm, mmm, ss, sss, results);


	    }else if (pattern.description.equals("Custom variation of DD:MM.MMM")){
           //-------------Customs patterns start here-------------

	    	//Sets pattern machted, successful, pattern type and pattern info
        	initializeResult(results, pattern);

            //get sign
            sign = getCustomSign(str);

            //TODO still needs to be adapted to custom pattern
	        results.isLongitude = getIsLongitude(str);



            //Remove all the unwanted stuff
            //Note: This method also replaces the symbols with ":"
            //Note: In certain cases it may make the coord unparsable
            str = removeCustomPatternParts(str);

            str = removeWhiteSpace(str);

            //Replace comma or dot with a current decimal separator
            str = fixDecimalSeparator(str);


            //Extract decimal part
            decimalBit = str.split(decSeparatorRegEx);

            //split degrees and minutes
            ddmm = decimalBit[0].split(":");


            try {
                //extract values from the strings
                dd = Integer.valueOf(ddmm[0]); //Degrees

                if (ddmm.length > 1){//Minutes
                    //check if the string is not empty
                    if (ddmm[1] != "") { mm = Integer.valueOf(ddmm[1]); }
                }

                if (decimalBit.length > 1){//DecimalSeconds
                    //check if the string is not empty
                    if (decimalBit[1] != ""){
                        //replace the ":" if any (may be here as a result of custom symbol replacement
                        decimalBit[1] = decimalBit[1].replace(":", "");

                        mmm = Double.valueOf(decimalBit[1]) / Math.pow(10, (decimalBit[1].length()));
                    }
                }
            } catch (Exception e){
                results.conversionSuccessful = false;
                results.convertedCoord = 99999; //this is to mark an error...
                results.conversionComments =
                    "It looks like the supplied pattern has some ambiguous elements and the parser was unable to parse the coordinate." +
                    "<br/>If the supplied symbols used for marking degrees, minutes or seconds contain hemisphere indicators, " +
                    "the parser is likely to fail or yield rubbish results even though the pattern itself has been recognised."
                    ;

                //exit method
                return results;
            }


            checkDegreeRange(dd, results);
            checkMinuteRange(mm, results);
            doConvertWithCheck(sign, dd, mm, mmm, ss, sss, results);

	    } else if (pattern.description.equals("Custom variation of DD:MM:SS.SSS")){

	    	//Sets pattern machted, successful, pattern type and pattern info
        	initializeResult(results, pattern);


            //get sign
            sign = getCustomSign(str);

            //TODO still needs to be adapted to custom pattern
	        results.isLongitude = getIsLongitude(str);


            //Remove all the unwanted stuff
            //Note: This method also replaces the symbols with ":"
            //Note: In certain cases it may make the coord unparsable
            str = removeCustomPatternParts(str);

            str = removeWhiteSpace(str);

            //Replace comma or dot with a current decimal separator
            str = fixDecimalSeparator(str);


            //Extract decimal part
            decimalBit = str.split(decSeparatorRegEx);

            //split degrees and minutes
            ddmmss = decimalBit[0].split(":");


            try {

                //extract values from the strings
                dd = Integer.valueOf(ddmmss[0]); //Degrees
                if (ddmmss.length > 1) {//Minutes
                    //check if the string is not empty
                    if (ddmmss[1] != "") {
                    	mm = Integer.valueOf(ddmmss[1]);
                    }
                }
                if (ddmmss.length > 2){ //Seconds
                    //check if the string is not empty
                    if (ddmmss[2] != "") {
                    	ss = Integer.valueOf(ddmmss[2]);
                    }
                }
                if (decimalBit.length > 1){ //DecimalSeconds
                    //check if the string is not empty
                    if (decimalBit[1] != "") {
                        sss = Double.valueOf(decimalBit[1]) / Math.pow(10, (decimalBit[1].length()));
                    }
                }
            } catch (Exception e) {
                results.conversionSuccessful = false;
                results.convertedCoord = 99999; //this is to mark an error...
                results.conversionComments =
                    "It looks like the supplied pattern has some ambiguous elements and the parser was unable to parse the coordinate." +
                    "<br/>If the supplied symbols used for marking degrees, minutes or seconds contain hemisphere indicators, " +
                    "the parser is likely to fail or yield rubbish results even though the pattern itself has been recognised."
                    ;

                //exit method
                return results;
            }


            checkDegreeRange(dd, results);
            checkMinuteRange(mm, results);
            checkSecondRange(ss, results);

            doConvertWithCheck(sign, dd, mm, mmm, ss, sss, results);

	    }else {   //default  : pattern not recognized
            results.patternRecognised = false;
            results.patternType = pattern.description;
            results.patternMatched = pattern.pattern;

            results.conversionSuccessful = false;
            results.convertedCoord = 99999; //this is to mark an error...

            results.conversionComments = "Coordinate pattern not recognised!";

 	    }

        //do the self check here
        results = selfTest(results);

        //return conversion results
        return results;
    }


	/**
     * @param string
     * @return
     */
    private String Nz(String string) {
        return CdmUtils.Nz(string);
    }


    /**
	 * @param sign
	 * @param dd
	 * @param mm
	 * @param ss
	 * @param sss
	 * @param results
	 */
	private void doConvertWithCheck(int sign, double dd, double mm, double mmm, double ss, double sss, ConversionResults results) {
		double dec;
		//Do the conversion if everything ok
		if (results.conversionSuccessful){
		    results.conversionComments = "Conversion successful.";

		    dec = sign * (dd + (mm + mmm) / 60 + (ss + sss) / 3600);

		    //one more check to ensure a coord does not exceed 180
		    if (dec > 180 | dec < -180){
		        results.conversionSuccessful = false;
		        results.convertedCoord = 99999; //this is to mark an error...
		        results.conversionComments += "Coordinate is either > 180 or < -180; ";
		    } else {
		        results.convertedCoord = dec;

		        results.conversionComments = "Conversion successful.";

		        //Check whether the coordinate exceeds +/- 90 and mark it in comments

		        if (dec <= 90 && dec >= -90 && (results.isLongitude == null || results.isLongitude == false) ) {
                	results.canBeLat = true;
                }else{
                	results.isLongitude = true;
                }
		    }
		}
	}


	/**
	 * @param ss
	 * @param results
	 */
	private void checkSecondRange(double ss, ConversionResults results) {
		if (ss > 59) {//seconds
		    results.conversionSuccessful = false;
		    results.convertedCoord = 99999; //this is to mark an error...
		    results.conversionComments += "Seconds fall outside the range: MM >= 60; ";
		}
	}


	/**
	 * @param mm
	 * @param results
	 */
	private void checkMinuteRange(double mm, ConversionResults results) {
		if (mm > 59) {//minutes
		    results.conversionSuccessful = false;
		    results.convertedCoord = 99999; //this is to mark an error...
		    results.conversionComments += "Minutes fall outside the range: MM > 59; ";
		}
	}


	/**
	 * @param dd
	 * @param results
	 */
	private void checkDegreeRange(double dd, ConversionResults results) {
		//do some additional checking if the coords fall into the range
		if (dd < -180 | dd > 180){  //degree may require another param specifying whether it's lat or lon...
		    results.conversionSuccessful = false;
		    results.convertedCoord = 99999; //this is to mark an error...
		    results.conversionComments += "Degrees fall outside the range: DD < -180 | DD > 180; ";
		}
	}


	/**
	 * @param str
	 * @return
	 */
	private Boolean getIsLongitude(String str) {
	    //This regex checks for the negative hemisphere indicator
		Pattern regexLatitudeNonAmbigous = Pattern.compile("(N|n)");
		Pattern regexLatitudeAmbigous = Pattern.compile("(S|s)");

        //This regex checks if there weren't any other hemisphere indicators
        //it is needed for the specific case of the DDdMMmSSs S
        //so it needs to be ensured there where no positive indicators
        Pattern regexLongitude = Pattern.compile("(W|w|E|e)");

        //if a positive indicator is found no need to search further
        if (regexLongitude.matcher(str).find()){
            return true;
        }else if (regexLatitudeNonAmbigous.matcher(str).find()){
        	return false;
        }else if (regexLatitudeAmbigous.matcher(str).find()){
        	Pattern regexLiteralUnits = Pattern.compile("(D|d|M|m)");

        	//if there are no other literal units we assume that S is a
        	//direction and not a second indicator
            if (! regexLiteralUnits.matcher(str).find()){
                return false;
            }else if (regexLatitudeAmbigous.matcher(str).groupCount() > 1){
            	return false;
            }else{
            	return null;
            }
        }else{
        	return null;
        }
	}


	/**
	 * Sets pattern machted, successful, pattern type and pattern info
	 * @param results
	 * @param pattern
	 */
	private void initializeResult(ConversionResults results,
			CoordinatePattern pattern) {
		//Pattern matched
		results.patternRecognised = true;

		//Matching pattern succeeded so intialy the parsing is ok
		results.conversionSuccessful = true;

		//pattern info
		results.patternType = pattern.description;
		results.patternMatched = pattern.pattern;
	}


    private ConversionResults selfTest(ConversionResults results){

        ConversionResults newresults = results;

        if (results.conversionSuccessful != false){
            int sign = 1;
            if (Math.signum(results.convertedCoord) < 0) {
            	sign = -1;
            }

            double decimalDegrees = sign * results.convertedCoord;
            int fullDegrees;

            double decimalMinutes;
            int fullMinutes;

            double decimalSeconds;
            int fullSeconds;

            //Get full degrees
            fullDegrees = (int)Math.floor(decimalDegrees);

            //get minutes
            decimalMinutes = (decimalDegrees - fullDegrees) * 60;
            fullMinutes = (int)Math.floor(decimalMinutes);

            decimalSeconds = (decimalMinutes - fullMinutes) * 60;
            fullSeconds = (int)Math.floor(decimalSeconds);

            //save the test results
            newresults.dd = fullDegrees;
            newresults.mm = fullMinutes;
            newresults.mmm = decimalSeconds;
            newresults.ss = fullSeconds;
            newresults.sss = decimalSeconds;

        }

        return newresults;

    }



    //------------ CUSTOM PATTERN BUILDER--------------

    public class CustomPatternIn {
        public String north;
        public String south;
        public String east;
        public String west;

        public String degreeSymbol;
        public String minuteSymbol;
        public String secondSymbol;

        public boolean caseInsensitive;
        public boolean allowWhiteSpace;
        public boolean priorityOverDefaultPatterns;
        public boolean disableDefaultPatterns;

    }


    private class CustomPattern{

        public List<CustomHemisphereIndicator> hemisphereIndicators;

        public String degreeSymbol;
        public String minuteSymbol;
        public String secondSymbol;

        public boolean caseInsensitive;

    }

    //global variable to be used if a custom pattern is used
    private CustomPattern customPtrn;

    //escape some of the chars
    private String escapeChars(String str){
        // backslash - first so it is not messed when other escape chars are corrected for being used in a string
        str = str.replace("\\", "\\\\");

        //dot and comma
        str = str.replace(".", "\\.");
        str = str.replace(",", "\\,");

        //brackets
        str = str.replace("(", "\\(");
        str = str.replace(")", "\\)");
        str = str.replace("[", "\\[");
        str = str.replace("]", "\\]");
        str = str.replace("{", "\\{");
        str = str.replace("}", "\\}");

        //other replacements
        str = str.replace("^", "\\^");
        str = str.replace("$", "\\$");
        str = str.replace("+", "\\+");
        str = str.replace("*", "\\*");
        str = str.replace("?", "\\?");
        str = str.replace("|", "\\|");

        return str;
    }


    //this implements sorting by using system.Icomparable - sorting is needed later when replacing
    private class CustomHemisphereIndicator implements Comparable<CustomHemisphereIndicator> {
        //private variables
        private int m_length;
        private String m_name;
        private String m_indicator;
        private boolean m_positive;

        //constructor
        public CustomHemisphereIndicator(String name, String indicator, int length, boolean positive){
            this.m_name = name;
            this.m_indicator = indicator;
            this.m_length = length;
            this.m_positive = positive;
        }

        //properties

        public String getName(){
        	return this.m_name;
        }
        public void setName(String value){
        	this.m_name = value;
        }

        public String getIndicator(){
            return this.m_indicator;
        }
        public void setIndicator(String value){
            this.m_indicator = value;
        }

        public int getLength(){
            return this.m_length;
        }
        public void setLength(int value){
            this.m_length = value;
        }


        public boolean getPositive(){
            return this.m_positive;
        }
        public void setPositive(boolean value){
            this.m_positive = value;
        }

        /* Less than zero if this instance is less than obj.
         * Zero if this instance is equal to obj.
         * Greater than zero if this instance is greater than obj.
         *
         * This method uses the predefined method Int32.CompareTo
         * */

        @Override
        public int compareTo(CustomHemisphereIndicator ind){

	        //no need to rewrite the code again, we have Integer.compareTo ready to use
	        return Integer.valueOf(this.getLength()).compareTo(Integer.valueOf(ind.getLength()));
        }
    }



    //This adds custom pattern to a list of already predefined patterns
    //useful for batch conversions - allows for totally mixed input data (predefined & custom)
    public void addCustomPattern(CustomPatternIn patternIn){

        //new custom pattern object - to pass the needed data farther
        CustomPattern pattern = new CustomPattern();

        //keep indicators for parsing
        List<CustomHemisphereIndicator> indicators = new ArrayList<CustomHemisphereIndicator>();

        //north
        CustomHemisphereIndicator ind = new CustomHemisphereIndicator("North", patternIn.north, patternIn.north.length() ,true);
        indicators.add(ind);

        //south
        ind = new CustomHemisphereIndicator("South", patternIn.south, patternIn.south.length(), false);
        indicators.add(ind);

        //east
        ind = new CustomHemisphereIndicator("East", patternIn.east, patternIn.east.length(), true);
        indicators.add(ind);

        //west
        ind = new CustomHemisphereIndicator("West", patternIn.west, patternIn.west.length(), false);
        indicators.add(ind);

        //sort the arraylist
        Collections.sort(indicators, lengthComparator);


        //add it to the pattern object
        pattern.hemisphereIndicators = indicators;

        //case insensitive
        pattern.caseInsensitive = patternIn.caseInsensitive;

        //keep symbols for parsing
        pattern.degreeSymbol = patternIn.degreeSymbol;
        pattern.minuteSymbol = patternIn.minuteSymbol;
        pattern.secondSymbol = patternIn.secondSymbol;


        //save the data
        customPtrn = pattern;


        //----------------build custom patterns----------------

        //prepare hemisphere indicators
        String north = escapeChars(patternIn.north);
        String south = escapeChars(patternIn.south);
        String east = escapeChars(patternIn.east);
        String west = escapeChars(patternIn.west);

        //prepare symbols
        String degreesymbol = "";
        if (patternIn.degreeSymbol != ""){
            degreesymbol = "(" + escapeChars(patternIn.degreeSymbol) + ")?";
        }

        String minutesymbol = "";
        if (patternIn.minuteSymbol != ""){
            minutesymbol = "(" + escapeChars(patternIn.minuteSymbol) + ")?";
        }

        String secondsymbol = "";
        if (escapeChars(patternIn.secondSymbol) != ""){
            secondsymbol = "(" + escapeChars(patternIn.secondSymbol) + ")?";
        }


        //is the pattern to be case insensitive?
        String CaseInsensitive = "";
        if (patternIn.caseInsensitive){
            CaseInsensitive = "(?i)";
        }

        //allow whitespace
        String WhiteSpace = "";
        if (patternIn.allowWhiteSpace == true){
            WhiteSpace = "(\\s)*";
        }

        //hemisphere indicator
        String HemisphereIndicator = "";

        //add north if present
        if (north == ""){
            HemisphereIndicator += south;
        }else{
            HemisphereIndicator += north;
            if (south != ""){
                HemisphereIndicator += "|" + south;
            }
        }

        //add east
        if (north == "" & south == ""){
            HemisphereIndicator += east;
        } else {
            if (east != ""){
                HemisphereIndicator += "|" + east;
            }
        }

        //add west
        if (north == "" & south == "" & east == ""){
            HemisphereIndicator += west;
        } else {
            if (west != "") {
                HemisphereIndicator += "|" + west;
            }
        }

        //add remaining bits if not empty
        if (HemisphereIndicator != "") {
            HemisphereIndicator = "(" + HemisphereIndicator + ")?";
        }

        List<CoordinatePattern> customPatterns = new ArrayList<CoordinatePattern>();

        //create custom patterns based on the specified user's input
        CoordinatePattern ptrn;

        //Custom variation of DD.DDD
        ptrn = new CoordinatePattern();
        ptrn.description = "Custom variation of DD.DDD";
        ptrn.pattern  =
            CaseInsensitive + "(^" +
            WhiteSpace + HemisphereIndicator + WhiteSpace +
            "(" +
            "(\\d{1,3}(\\.|\\,)?" + WhiteSpace + degreesymbol + WhiteSpace + "$)|(\\d{1,3}(\\.|\\,)\\d+" + WhiteSpace + degreesymbol + WhiteSpace + "$)" +
            ")" +
            "|(^" + WhiteSpace +
            "(" +
            "(\\d{1,3}(\\.|\\,)?" + WhiteSpace + degreesymbol + WhiteSpace + ")|(\\d{1,3}(\\.|\\,)\\d+" + WhiteSpace + degreesymbol + WhiteSpace + ")" +
            ")" +
            HemisphereIndicator + WhiteSpace + "$" +
            "))"
            ;
        customPatterns.add(ptrn);

        //Custom variation of DD:MM.MMM
        ptrn = new CoordinatePattern();
        ptrn.description = "Custom variation of DD:MM.MMM";
        ptrn.pattern =
            CaseInsensitive + "(^" +
            WhiteSpace + HemisphereIndicator + WhiteSpace +
            "(" +
            "(\\d{1,3}" + WhiteSpace + degreesymbol + WhiteSpace + "$)|(\\d{1,3}" + WhiteSpace + degreesymbol + WhiteSpace + "\\d{1,2}(\\.|\\,)?" + WhiteSpace + minutesymbol + WhiteSpace + "$)|(\\d{1,3}" + WhiteSpace + degreesymbol + WhiteSpace + "\\d{1,2}(\\.|\\,)\\d+" + WhiteSpace + minutesymbol + WhiteSpace + "$)" +
            ")" +
            "|(^" + WhiteSpace +
            "(" +
            "(\\d{1,3}" + WhiteSpace + degreesymbol + WhiteSpace + ")|(\\d{1,3}" + WhiteSpace + degreesymbol + WhiteSpace + "\\d{1,2}(\\.|\\,)?" + WhiteSpace + minutesymbol + WhiteSpace + ")|(\\d{1,3}" + WhiteSpace + degreesymbol + WhiteSpace + "\\d{1,2}(\\.|\\,)\\d+" + WhiteSpace + minutesymbol + WhiteSpace + ")" +
            ")" +
            HemisphereIndicator + WhiteSpace + "$" +
            "))"
            ;
        customPatterns.add(ptrn);

        //Custom variation of DD:MM:SS.SSS
        ptrn = new CoordinatePattern();
        ptrn.description = "Custom variation of DD:MM:SS.SSS";
        ptrn.pattern =
            CaseInsensitive + "(^" +
            WhiteSpace + HemisphereIndicator + WhiteSpace +
            "(" +
            "(\\d{1,3}" + WhiteSpace + degreesymbol + WhiteSpace + "$)|(\\d{1,3}" + WhiteSpace + degreesymbol + WhiteSpace + "\\d{1,2}" + WhiteSpace + minutesymbol + WhiteSpace + "$)|(\\d{1,3}" + WhiteSpace + degreesymbol + WhiteSpace + "\\d{1,2}" + WhiteSpace + minutesymbol + WhiteSpace + "\\d{1,2}(\\.|\\,)?" + WhiteSpace + secondsymbol + WhiteSpace + "$)|(\\d{1,3}" + WhiteSpace + degreesymbol + WhiteSpace + "\\d{1,2}" + WhiteSpace + minutesymbol + WhiteSpace + "\\d{1,2}(\\.|\\,)\\d+" + WhiteSpace + secondsymbol + WhiteSpace + "$)" +
            ")" +
            "|(^" + WhiteSpace +
            "(" +
            "(\\d{1,3}" + WhiteSpace + degreesymbol + WhiteSpace + ")|(\\d{1,3}" + WhiteSpace + degreesymbol + WhiteSpace + "\\d{1,2}" + WhiteSpace + minutesymbol + WhiteSpace + ")|(\\d{1,3}" + WhiteSpace + degreesymbol + WhiteSpace + "\\d{1,2}" + WhiteSpace + minutesymbol + WhiteSpace + "\\d{1,2}(\\.|\\,)?" + WhiteSpace + secondsymbol + WhiteSpace + ")|(\\d{1,3}" + WhiteSpace + degreesymbol + WhiteSpace + "\\d{1,2}" + WhiteSpace + minutesymbol + WhiteSpace + "\\d{1,2}(\\.|\\,)\\d+" + WhiteSpace + secondsymbol + WhiteSpace + ")" +
            ")" +
            HemisphereIndicator + WhiteSpace + "$" +
            "))"
            ;
        customPatterns.add(ptrn);

        //check if the default patterns are to be used
        if (patternIn.disableDefaultPatterns) {
            patterns = customPatterns;
        } else { //if all patterns are to be used check which set has the matching priority

            //check if the custom patterns are to have priority over the default ones
            if (patternIn.priorityOverDefaultPatterns){

                //add default patterns to the custom patterns
                for (int i = 0; i < patterns.size(); i++){
                    customPatterns.add(patterns.get(i));
                }

                //swap array lists
                patterns = customPatterns;

            }else{
                //add custom patterns to the default patterns
                for (int i = 0; i < customPatterns.size(); i++){
                    patterns.add(customPatterns.get(i));

                }
            }
        }
    }

}
