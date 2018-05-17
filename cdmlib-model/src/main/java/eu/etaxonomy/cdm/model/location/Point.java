/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Latitude;
import org.hibernate.search.annotations.Longitude;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.Spatial;
import org.hibernate.search.annotations.SpatialMode;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.strategy.parser.location.CoordinateConverter;
import eu.etaxonomy.cdm.strategy.parser.location.CoordinateConverter.ConversionResults;
import eu.etaxonomy.cdm.validation.Level2;

/**
 * @author m.doering
 * @since 08-Nov-2007 13:06:44
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Point", propOrder = {
    "longitude",
    "latitude",
    "errorRadius",
    "referenceSystem"
})
@XmlRootElement(name = "Point")
@Embeddable
@Spatial(spatialMode=SpatialMode.RANGE, name="point")
public class Point implements Cloneable, Serializable {
    private static final long serialVersionUID = 531030660792800636L;
    private static final Logger logger = Logger.getLogger(Point.class);

    //TODO was Float but H2 threw errors
    @XmlElement(name = "Longitude")
    @Longitude(of="point")
    @NotNull(groups = Level2.class)
    private Double longitude;

    @XmlElement(name = "Latitude")
    @Latitude(of="point")
    @NotNull(groups = Level2.class)
    private Double latitude;

    /**
     * Error radius in Meters
     */
    @XmlElement(name = "ErrorRadius")
    @Field
    @NumericField
    private Integer errorRadius = 0;

    @XmlElement(name = "ReferenceSystem")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    private ReferenceSystem referenceSystem;


//******************** FACTORY METHODS ****************************

    /**
     * Factory method
     * @return
     */
    public static Point NewInstance(){
        return new Point();
    }

    /**
     * Factory method
     * @return
     */
    public static Point NewInstance(Double longitude, Double latitude, ReferenceSystem referenceSystem, Integer errorRadius){
        Point result = new Point();
        result.setLongitude(longitude);
        result.setLatitude(latitude);
        result.setReferenceSystem(referenceSystem);
        result.setErrorRadius(errorRadius);
        return result;
    }

// ******************** CONSTRUCTOR ***************************

    /**
     * Constructor
     */
    public Point() {
    }

//************** Sexagesimal /decimal METHODS *******************

    public enum Direction {
        WEST {

            @Override
            public String toString() {
                return "W";
            }
        },
        EAST {

            @Override
            public String toString() {
                return "E";
            }
        },
        NORTH {

            @Override
            public String toString() {
                return "N";
            }
        },
        SOUTH {

            @Override
            public String toString() {
                return "S";
            }
        };
    }

    public static final class CoordinateParser {

        /**
         * Pattern zum parsen von Sexagesimalen Grad: 145°
         */
        private static final String DEGREE_REGEX = "([0-9]*)\u00B0";
        /**
         * Pattern zum parsen von Sexagesimalen Minuten: 65'
         */
        private static final String MINUTES_REGEX = "(?:([0-9]*)')?";
        /**
         * Pattern zum parsen von Sexagesimalen Sekunden: 17"
         */
        private static final String SECONDS_REGEX = "(?:([0-9]*)(?:''|\"))?";
        /**
         * Himmelsrichtung Längengrad
         */
        private static final String LONGITUDE_DIRECTION_REGEX = "([OEW])";
        /**
         * Himmelsrichtung Breitengrad
         */
        private static final String LATITUDE_DIRECTION_REGEX = "([NS])";

        /**
         * Pattern zum Parsen von Breitengraden.
         */
        private static final Pattern LATITUDE_PATTERN = Pattern
                .compile(DEGREE_REGEX + MINUTES_REGEX + SECONDS_REGEX
                        + LATITUDE_DIRECTION_REGEX);

        /**
         * Pattern zum Parsen von Längengraden.
         */
        private static final Pattern LONGITUDE_PATTERN = Pattern
                .compile(DEGREE_REGEX + MINUTES_REGEX + SECONDS_REGEX
                        + LONGITUDE_DIRECTION_REGEX);

        private CoordinateParser() {
            throw new AssertionError( );
        }

        /**
         * Parst einen Breitengrad der Form<br>
         * G°M'S""(OEW)<br>
         * Die Formen<br>
         * G°(OEW)<br>
         * G°M'(OEW)<br>
         * sind ebenfalls erlaubt.
         *
         * @param strg
         * @return Die geparsten Koordinaten
         * @throws ParseException
         *             Wenn eine Fehler beim Parsen aufgetreten ist.
         */
        public static Sexagesimal parseLatitude(final String strg)
                throws ParseException {
            return parseCoordinates(strg, LATITUDE_PATTERN);
        }

        /**
         * Parst einen Längengrad der Form<br>
         * G°M'S"(NS)<br>
         * Die Formen<br>
         * G°(NS)<br>
         * G°M'(NS)<br>
         * sind ebenfalls erlaubt.
         *
         * @param strg
         * @return Die geparsten Koordinaten
         * @throws ParseException
         *             Wenn eine Fehler beim Parsen aufgetreten ist.
         */
        public static Sexagesimal parseLongitude(final String strg)
                throws ParseException {
            return parseCoordinates(strg, LONGITUDE_PATTERN);
        }


        /**
         * Not used at the moment. Use CoordinateConverter instead.
         * @param strg
         * @param pattern
         * @return
         * @throws ParseException
         */
        private static Sexagesimal parseCoordinates(final String strg, final Pattern pattern) throws ParseException {
            if (strg == null) {
                throw new java.text.ParseException("Keine Koordinaten gegeben.", -1);
            }
            final Matcher matcher = pattern.matcher(strg);
            if (matcher.matches( )) {
                if (matcher.groupCount( ) == 4) {
                    // Grad
                    String tmp = matcher.group(1);
                    int degree = Integer.parseInt(tmp);

                    // Optional minutes
                    tmp = matcher.group(2);
                    int minutes = Sexagesimal.NONE;
                    if (tmp != null) {
                        minutes = Integer.parseInt(tmp);
                    }

                    // Optional seconds
                    tmp = matcher.group(3);
                    int seconds = Sexagesimal.NONE;
                    if (tmp != null) {
                        seconds = Integer.parseInt(tmp);
                    }

                    // directions
                    tmp = matcher.group(4);
                    final Direction direction;
                    if (tmp.equals("N")) {
                        direction = Direction.NORTH;
                    }
                    else if (tmp.equals("S")) {
                        direction = Direction.SOUTH;
                    }
                    else if (tmp.equals("E") || tmp.equals("O")) {
                        direction = Direction.EAST;
                    }
                    else if (tmp.equals("W")) {
                        direction = Direction.WEST;
                    }
                    else {
                        direction = null;
                    }
                    return Sexagesimal.NewInstance(degree, minutes, seconds, direction);
                }
                else {
                    throw new java.text.ParseException(
                            "Die Koordinaten-Darstellung ist fehlerhaft: " + strg,
                            -1);
                }
            }
            else {
                throw new java.text.ParseException(
                        "Die Koordinaten-Darstellung ist fehlerhaft: " + strg, -1);
            }
        }

    }


    private static final BigDecimal SIXTY = BigDecimal.valueOf(60.0);
    private static final MathContext MC = new MathContext(34, RoundingMode.HALF_UP);
    private static final double HALF_SECOND = 1. / 7200.;

    //see http://www.tutorials.de/forum/archiv/348596-quiz-10-zeja-java.html
    public static class Sexagesimal{
        public static Sexagesimal NewInstance(Integer degree, Integer minutes, Integer seconds, Direction direction){
            Sexagesimal result = new Sexagesimal();
            result.degree = degree; result.minutes = minutes; result.seconds = seconds;
            return result;
        }

        public static final int NONE = 0;
        public Integer degree;
        public Integer minutes;
        public Integer seconds;
        public Double tertiers;

        public Direction direction;


        public boolean isLatitude(){
            return (direction == Direction.WEST) || (direction == Direction.EAST) ;
        }
        public boolean isLongitude(){
            return ! isLatitude();
        }


        public static Sexagesimal valueOf(Double decimal, boolean isLatitude){
            return valueOf(decimal, isLatitude, false, false, true);
        }

        public static Sexagesimal valueOf(Double decimal, boolean isLatitude, boolean nullSecondsToNull, boolean nullMinutesToNull, boolean allowTertiers){
            if(decimal == null){
                return null;
            }
            Sexagesimal sexagesimal = new Sexagesimal();
            Double decimalDegree = decimal;
                if (isLatitude) {
                    if (decimalDegree < 0) {
                        sexagesimal.direction = Direction.SOUTH;
                    }
                    else {
                        sexagesimal.direction = Direction.NORTH;
                    }
                }
                else {
                    if (decimalDegree < 0) {
                           sexagesimal.direction = Direction.WEST;
                        }
                        else {
                            sexagesimal.direction = Direction.EAST;
                        }
                }

                // Decimal in \u00B0'" umrechnen
                double d = Math.abs(decimalDegree);
                if (! allowTertiers){
                    d += HALF_SECOND; // add half a second for rounding
                }else{
                    d += HALF_SECOND / 10000;  //to avoid rounding errors
                }
                sexagesimal.degree = (int) Math.floor(d);
                sexagesimal.minutes = (int) Math.floor((d - sexagesimal.degree) * 60.0);
                sexagesimal.seconds = (int) Math.floor((d - sexagesimal.degree - sexagesimal.minutes / 60.0) * 3600.0);
                sexagesimal.tertiers = (d - sexagesimal.degree - sexagesimal.minutes / 60.0 - sexagesimal.seconds / 3600.0) * 3600.0;

                if (sexagesimal.seconds == 0 && nullSecondsToNull){
                    sexagesimal.seconds = null;
                }
                if (sexagesimal.seconds == null && sexagesimal.minutes == 0 && nullMinutesToNull){
                    sexagesimal.minutes = null;
                }

               // sexagesimal.decimalRadian = Math.toRadians(this.decimalDegree);
                return sexagesimal;
        }



        private Double toDecimal(){
            BigDecimal value = BigDecimal.valueOf(CdmUtils.Nz(this.seconds)).divide(SIXTY, MC).add
                (BigDecimal.valueOf(CdmUtils.Nz(this.minutes))).divide(SIXTY, MC).add
                (BigDecimal.valueOf(CdmUtils.Nz(this.degree)));

            if (this.direction == Direction.WEST || this.direction == Direction.SOUTH) {
                value = value.negate( );
            }
            return value.doubleValue( );
        }

        @Override
        public String toString(){
            return toString(false, false);
        }
        public String toString(boolean includeEmptySeconds){
            return toString(includeEmptySeconds, false);
        }

        public String toString(boolean includeEmptySeconds, boolean removeTertiers){
            String result;
            result = String.valueOf(CdmUtils.Nz(degree)) + "\u00B0";
            if (seconds != null || minutes != null){
                result += String.valueOf(CdmUtils.Nz(minutes)) + "'";
            }
            if (seconds != null ){
                if (seconds != 0 || includeEmptySeconds){
                    result += String.valueOf(CdmUtils.Nz(seconds)) + getTertiersString(tertiers, removeTertiers) + "\"";
                }
            }
            result += direction;
            return result;
        }
        private String getTertiersString(Double tertiers, boolean removeTertiers) {
            if (tertiers == null || removeTertiers){
                return "";
            }else{
                if (tertiers >= 1.0 || tertiers < 0.0){
                    throw new IllegalStateException("Tertiers should be 0.0 <= tertiers < 1.0 but are '" + tertiers + "'");
                }
                String result = tertiers.toString();
                int pos = result.indexOf("E");
                if (pos > -1){
                    int exp = - Integer.valueOf(result.substring(pos + 1));
                    result = result.substring(0, pos).replace(".", "");
                    result = "0." + StringUtils.leftPad("", exp - 1, "0") +  result;

                }

                if (result.length() > 5){
                    result = result.substring(0, 5);
                }
                while (result.endsWith("0")){
                    result = result.substring(0, result.length() -1);
                }
                result = result.substring(1);
                if (result.equals(".")){
                    result = "";
                }
                return result;
            }

        }

    }


    @Transient
    public Sexagesimal getLongitudeSexagesimal (){
        boolean isLatitude = false;
        return Sexagesimal.valueOf(longitude, isLatitude);
    }

    @Transient
    public Sexagesimal getLatitudeSexagesimal (){
        boolean isLatitude = true;
        return Sexagesimal.valueOf(latitude, isLatitude);
    }

    @Transient
    public void setLatitudeSexagesimal(Sexagesimal sexagesimalLatitude){
        this.latitude = sexagesimalLatitude.toDecimal();
    }
    @Transient
    public void setLongitudeSexagesimal(Sexagesimal sexagesimalLongitude){
        this.longitude = sexagesimalLongitude.toDecimal();
    }

    @Transient
    public void setLatitudeByParsing(String string) throws ParseException{
        this.setLatitude(parseLatitude(string));
    }

    @Transient
    public void setLongitudeByParsing(String string) throws ParseException{
        this.setLongitude(parseLongitude(string));
    }


    public static Double parseLatitude(String string) throws ParseException{
        try{
            if (string == null){
                return null;
            }
            string = setCurrentDoubleSeparator(string);
            if (isDouble(string)){
                Double result = Double.valueOf(string);
                if (Math.abs(result) > 90.0){
                    throw new ParseException("Latitude could not be parsed", 0);
                }
                return result;
            }else{
                CoordinateConverter converter = new CoordinateConverter();
                ConversionResults result = converter.tryConvert(string);
                if (! result.conversionSuccessful || (result.isLongitude != null  && result.isLongitude)  ){
                    throw new ParseException("Latitude could not be parsed", 0);
                }else{
                    return result.convertedCoord;
                }
            }
        } catch (Exception e) {
            String message = "Latitude %s could not be parsed";
            message = String.format(message, string);
            throw new ParseException(message, 0);
        }
    }

    public static Double parseLongitude(String string) throws ParseException{
        try {
            if (string == null){
                return null;
            }
            string = setCurrentDoubleSeparator(string);
            if (isDouble(string)){
                Double result = Double.valueOf(string);
                if (Math.abs(result) > 180.0){
                    throw new ParseException("Longitude could not be parsed", 0);
                }
                return result;
            }else{
                CoordinateConverter converter = new CoordinateConverter();
                ConversionResults result = converter.tryConvert(string);
                if (! result.conversionSuccessful || (result.isLongitude != null  && ! result.isLongitude)){
                    throw new ParseException("Longitude could not be parsed", 0);
                }else{
                    return result.convertedCoord;
                }
            }
        } catch (Exception e) {
            String message = "Longitude %s could not be parsed";
            message = String.format(message, string);
            throw new ParseException(message, 0);
        }
    }

    private static String setCurrentDoubleSeparator(String string) {
        String regExReplaceComma = "(\\,|\\.)";
        string = string.replaceAll(regExReplaceComma,".");
        return string;

    }

    private static boolean isDouble(String string) {
        try {
            Double.valueOf(string);
            return true;

        } catch (NumberFormatException e) {
            return false;
        } catch (Exception e) {
            return false;
        }

    }

    /**
     * <code>true</code>, if none of the attributes (lat, long, errRadius, refSys) is set.
     */
    @Transient
    public boolean isEmpty(){
        if (errorRadius == null && latitude == null && longitude == null
                && referenceSystem == null){
            return true;
        }else{
            return false;
        }
    }

// ******************** GETTER / SETTER ********************************

    public ReferenceSystem getReferenceSystem(){
        return this.referenceSystem;
    }

    /**
     *
     * @param referenceSystem    referenceSystem
     */
    public void setReferenceSystem(ReferenceSystem referenceSystem){
        this.referenceSystem = referenceSystem;
    }

    public Double getLongitude(){
        return this.longitude;
    }

    /**
     *
     * @param longitude    longitude
     */
    public void setLongitude(Double longitude){
        this.longitude = longitude;
    }

    public Double getLatitude(){
        return this.latitude;
    }

    /**
     *
     * @param latitude    latitude
     */
    public void setLatitude(Double latitude){
        this.latitude = latitude;
    }

    /**
     * Error radius in Meters
     */
    public Integer getErrorRadius(){
        return this.errorRadius;
    }

    /**
     *
     * @param errorRadius    errorRadius
     */
    public void setErrorRadius(Integer errorRadius){
        this.errorRadius = errorRadius;
    }

// **************** toString *************************/


    /**
     * Returns a string representation in sexagesimal coordinates.
     * @return
     */
    public String toSexagesimalString(boolean includeEmptySeconds, boolean includeReferenceSystem){
        String result = "";
        result += getLatitudeSexagesimal() == null ? "" : getLatitudeSexagesimal().toString(includeEmptySeconds);
        result = CdmUtils.concat(", ", result, getLongitudeSexagesimal() == null ? "" : getLongitudeSexagesimal().toString(includeEmptySeconds));
        if (includeReferenceSystem && getReferenceSystem() != null){
            String refSys = CdmUtils.isBlank(getReferenceSystem().getLabel()) ? "" : "(" + getReferenceSystem().getLabel() + ")";
            result = CdmUtils.concat(" ", result, refSys);
        }
        return result;
    }

    @Override
    public String toString(){
        String result = "";
        boolean includeEmptySeconds = true;
        result += getLatitudeSexagesimal() == null ? "" : getLatitudeSexagesimal().toString(includeEmptySeconds);
        result = CdmUtils.concat(", ", result, getLongitudeSexagesimal() == null ? "" : getLongitudeSexagesimal().toString(includeEmptySeconds));
        return result;
    }


//*********** CLONE **********************************/

    /**
     * Clones <i>this</i> point. This is a shortcut that enables to
     * create a new instance that differs only slightly from <i>this</i> point
     * by modifying only some of the attributes.<BR>
     * This method overrides the clone method from {@link DerivedUnit DerivedUnit}.
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public Point clone(){
        try{
            Point result = (Point)super.clone();
            result.setReferenceSystem(this.referenceSystem);
            //no changes to: errorRadius, latitude, longitude
            return result;
        } catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }
    }


}
