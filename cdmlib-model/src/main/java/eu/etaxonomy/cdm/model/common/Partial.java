/**
* Copyright (C) 2026 EDIT
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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * Immutable partial date/time value (replacement for {@code org.joda.time.Partial}).
 * Fields are nullable {@link Integer}s: year, month (1-12), day (1-31), hour (0-23),
 * minute (0-59), plus second and millis for JAXB round-trips.
 *
 * @author a.mueller
 * @since 26.11.2026
 */
public final class Partial implements Serializable, Comparable<Partial> {

    private static final long serialVersionUID = 2L;

    private final Integer year;
    private final Integer month;
    private final Integer day;
    private final Integer hour;
    private final Integer minute;
    private final Integer second;
    private final Integer millis;

    public Partial() {
        this(null, null, null, null, null, null, null);
    }

    private Partial(Integer year, Integer month, Integer day, Integer hour, Integer minute,
            Integer second, Integer millis) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.millis = millis;
    }

//********************** FACTORIES **********************************/

    public static Partial ofYear(int year) {
        return new Partial(year, null, null, null, null, null, null);
    }

    public static Partial ofMonth(int month) {
        return new Partial(null, month, null, null, null, null, null);
    }

    public static Partial ofDay(int day) {
        return new Partial(null, null, day, null, null, null, null);
    }

    public static Partial ofMonthDay(int month, int day) {
        return new Partial(null, month, day, null, null, null, null);
    }

    public static Partial of(int year, int month) {
        return new Partial(year, month, null, null, null, null, null);
    }

    public static Partial of(int year, int month, int day) {
        return new Partial(year, month, day, null, null, null, null);
    }

    public static Partial of(int year, int month, int day, int hour, int minute) {
        return new Partial(year, month, day, hour, minute, null, null);
    }

    /**
     * Factory for adapters / converters that may pass null for unused fields.
     */
    public static Partial of(Integer year, Integer month, Integer day, Integer hour,
            Integer minute, Integer second, Integer millis) {
        return new Partial(year, month, day, hour, minute, second, millis);
    }

    public static Partial fromLocalDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return of(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    public static Partial fromZonedDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                dateTime.getHour(), dateTime.getMinute());
    }

    public static Partial fromInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return fromZonedDateTime(instant.atZone(ZoneOffset.UTC));
    }

    public static Partial fromDate(Date date) {
        if (date == null) {
            return null;
        }
        return fromInstant(date.toInstant());
    }

//********************** ACCESSORS **********************************/

    public boolean isEmpty() {
        return year == null && month == null && day == null
                && hour == null && minute == null
                && second == null && millis == null;
    }

    public boolean hasYear() {
        return year != null;
    }

    public Integer getYear() {
        return year;
    }

    public Partial withYear(int year) {
        return new Partial(year, month, day, hour, minute, second, millis);
    }

    public Partial withYear(Integer year) {
        return year == null ? withoutYear() : withYear(year.intValue());
    }

    public Partial withoutYear() {
        return year == null ? this : new Partial(null, month, day, hour, minute, second, millis);
    }

    public boolean hasMonth() {
        return month != null;
    }

    public Integer getMonth() {
        return month;
    }

    public Partial withMonth(int month) {
        return new Partial(year, month, day, hour, minute, second, millis);
    }

    public Partial withMonth(Integer month) {
        return month == null ? withoutMonth() : withMonth(month.intValue());
    }

    public Partial withoutMonth() {
        return month == null ? this : new Partial(year, null, day, hour, minute, second, millis);
    }

    public boolean hasDay() {
        return day != null;
    }

    public Integer getDay() {
        return day;
    }

    public Partial withDay(int day) {
        return new Partial(year, month, day, hour, minute, second, millis);
    }

    public Partial withDay(Integer day) {
        return day == null ? withoutDay() : withDay(day.intValue());
    }

    public Partial withoutDay() {
        return day == null ? this : new Partial(year, month, null, hour, minute, second, millis);
    }

    public boolean hasHour() {
        return hour != null;
    }

    public Integer getHour() {
        return hour;
    }

    public Partial withHour(int hour) {
        return new Partial(year, month, day, hour, minute, second, millis);
    }

    public Partial withHour(Integer hour) {
        return hour == null ? withoutHour() : withHour(hour.intValue());
    }

    public Partial withoutHour() {
        return hour == null ? this : new Partial(year, month, day, null, minute, second, millis);
    }

    public boolean hasMinute() {
        return minute != null;
    }

    public Integer getMinute() {
        return minute;
    }

    public Partial withMinute(int minute) {
        return new Partial(year, month, day, hour, minute, second, millis);
    }

    public Partial withMinute(Integer minute) {
        return minute == null ? withoutMinute() : withMinute(minute.intValue());
    }

    public Partial withoutMinute() {
        return minute == null ? this : new Partial(year, month, day, hour, null, second, millis);
    }

    public boolean hasSecond() {
        return second != null;
    }

    public Integer getSecond() {
        return second;
    }

    public Partial withSecond(int second) {
        return new Partial(year, month, day, hour, minute, second, millis);
    }

    public Partial withSecond(Integer second) {
        return second == null ? withoutSecond() : withSecond(second.intValue());
    }

    public Partial withoutSecond() {
        return second == null ? this : new Partial(year, month, day, hour, minute, null, millis);
    }

    public boolean hasMillis() {
        return millis != null;
    }

    public Integer getMillis() {
        return millis;
    }

    public Partial withMillis(int millis) {
        return new Partial(year, month, day, hour, minute, second, millis);
    }

    public Partial withMillis(Integer millis) {
        return millis == null ? withoutMillis() : withMillis(millis.intValue());
    }

    public Partial withoutMillis() {
        return millis == null ? this : new Partial(year, month, day, hour, minute, second, null);
    }

//********************** FORMAT / COMPARE **********************************/

    /**
     * ISO-like date string {@code yyyy[-MM[-dd]]} based on present date fields.
     * Used for JSON and exports formerly powered by joda {@code ISODateTimeFormat.date()}.
     */
    public String toIsoDate() {
        if (!hasYear()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%04d", year));
        if (hasMonth()) {
            sb.append('-').append(String.format("%02d", month));
            if (hasDay()) {
                sb.append('-').append(String.format("%02d", day));
            }
        }
        return sb.toString();
    }

    /**
     * Compares using year → month → day → hour → minute.
     * Missing fields are considered greater than present ones (nulls last).
     */
    @Override
    public int compareTo(Partial other) {
        if (other == null) {
            return -1;
        }
        int cmp = compareNullable(year, other.year);
        if (cmp != 0) {
            return cmp;
        }
        cmp = compareNullable(month, other.month);
        if (cmp != 0) {
            return cmp;
        }
        cmp = compareNullable(day, other.day);
        if (cmp != 0) {
            return cmp;
        }
        cmp = compareNullable(hour, other.hour);
        if (cmp != 0) {
            return cmp;
        }
        return compareNullable(minute, other.minute);
    }

    private static int compareNullable(Integer v1, Integer v2) {
        if (v1 == null && v2 == null) {
            return 0;
        }
        if (v1 == null) {
            return 1;
        }
        if (v2 == null) {
            return -1;
        }
        return v1.compareTo(v2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Partial)) {
            return false;
        }
        Partial other = (Partial) obj;
        return Objects.equals(year, other.year)
                && Objects.equals(month, other.month)
                && Objects.equals(day, other.day)
                && Objects.equals(hour, other.hour)
                && Objects.equals(minute, other.minute)
                && Objects.equals(second, other.second)
                && Objects.equals(millis, other.millis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, day, hour, minute, second, millis);
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        first = appendField(sb, first, "year", year);
        first = appendField(sb, first, "month", month);
        first = appendField(sb, first, "day", day);
        first = appendField(sb, first, "hour", hour);
        first = appendField(sb, first, "minute", minute);
        first = appendField(sb, first, "second", second);
        appendField(sb, first, "millis", millis);
        sb.append(']');
        return sb.toString();
    }

    private static boolean appendField(StringBuilder sb, boolean first, String name, Integer value) {
        if (value == null) {
            return first;
        }
        if (!first) {
            sb.append(", ");
        }
        sb.append(name).append('=').append(value);
        return false;
    }
}
