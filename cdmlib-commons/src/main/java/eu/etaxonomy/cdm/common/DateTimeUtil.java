/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * @author muellera
 * @since 27.05.2026
 */
public class DateTimeUtil {

    public static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

    public static final ZoneId UTC = ZoneOffset.UTC;

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ZonedDateTime of(int year, int month, int day) {
        return ZonedDateTime.of(year, month, day, 0, 0, 0, 0, BERLIN);
    }

    public static final DateTimeFormatter FLEXIBEL_DATE_TIME_FORMATTER =

//            new DateTimeFormatterBuilder()
//
//            .appendPattern("yyyy-MM-dd'T'HH:mm")
//            //optional seconds
//            .optionalStart().appendPattern(":ss").optionalEnd()
//            //optional nanoseconds
//            .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd()
//            // optional timezone /Offset (e.g. +01:00 or [Europe/Berlin])
//            .optionalStart().appendPattern("[XXX][VV]").optionalEnd()
//
//            //defaults
//            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
//            .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
//
//            .toFormatter();

            DateTimeFormatter.ISO_DATE_TIME
            //set timezone if not available
            .withZone(BERLIN);

    public static final DateTimeFormatter ISO8601_FORMAT_WITH_MIN_3_NANOS =

            new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .appendFraction(ChronoField.NANO_OF_SECOND, 3, 3, true)
                .appendPattern("XXX") // Zeitzonen-Offset wie +02:00
                .toFormatter();

    public static final DateTimeFormatter FLEXIBLE_ISO =
            new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)

                //defaults
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter()
                .withZone(BERLIN);
}
