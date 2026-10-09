/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;

/**
 * Persist {@link Partial} as VARCHAR (yyyyMMdd or yyyyMMdd_HHmm).
 * Same storage format as the former {@code PartialUserType}.
 *
 * @author a.mueller
 * @since 11.11.2008
 */
@Converter
public class PartialConverter implements AttributeConverter<Partial, String> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public String convertToDatabaseColumn(Partial attribute) {
        if (isEmptyOrNull(attribute)) {
            return null;
        }
        return partialToString(attribute);
    }

    @Override
    public Partial convertToEntityAttribute(String dbData) {
        if (dbData == null || "00000000".equals(dbData) || "0000000000000".equals(dbData)) {
            return null;
        }
        if (dbData.length() != 8 && dbData.length() != 13) {
            throw new IllegalArgumentException("Format for Partial not supported. Length must be 8 or 13: " + dbData);
        }
        Partial result = new Partial();
        Integer year = Integer.valueOf(dbData.substring(0, 4));
        Integer month = Integer.valueOf(dbData.substring(4, 6));
        Integer day = Integer.valueOf(dbData.substring(6, 8));
        Integer hour = null;
        Integer minute = null;
        if (dbData.length() == 13) {
            hour = Integer.valueOf(dbData.substring(9, 11));
            minute = Integer.valueOf(dbData.substring(11, 13));
        }
        if (year != 0) {
            result = result.with(DateTimeFieldType.year(), year);
        }
        if (month != 0) {
            result = result.with(DateTimeFieldType.monthOfYear(), month);
        }
        if (day != 0) {
            result = result.with(DateTimeFieldType.dayOfMonth(), day);
        }
        if (hour != null) {
            result = result.with(DateTimeFieldType.hourOfDay(), hour);
        }
        if (minute != null) {
            result = result.with(DateTimeFieldType.minuteOfHour(), minute);
        }
        return isEmptyOrNull(result) ? null : result;
    }

    private static boolean isEmptyOrNull(Partial partial) {
        return partial == null || partial.getValues().length == 0;
    }

    /**
     * @return an ISO 8601 like time representation of the form yyyyMMdd[_HHmm]
     */
    public static String partialToString(Partial p) {
        String strYear = getNullFilledString(p, DateTimeFieldType.year(), 4);
        String strMonth = getNullFilledString(p, DateTimeFieldType.monthOfYear(), 2);
        String strDay = getNullFilledString(p, DateTimeFieldType.dayOfMonth(), 2);
        String strHour = getNullFilledString(p, DateTimeFieldType.hourOfDay(), 2);
        String strMinute = getNullFilledString(p, DateTimeFieldType.minuteOfHour(), 2);
        boolean timeExists = timeExists(p);
        String result = strYear + strMonth + strDay;
        if (timeExists) {
            result = result + "_" + strHour + strMinute;
        }
        return result;
    }

    private static boolean timeExists(Partial partial) {
        return partial.isSupported(DateTimeFieldType.hourOfDay())
                || partial.isSupported(DateTimeFieldType.minuteOfHour());
    }

    private static String getNullFilledString(Partial partial, DateTimeFieldType type, int count) {
        String nul = "0000000000";
        if (!partial.isSupported(type)) {
            return nul.substring(0, count);
        }
        int value = partial.get(type);
        String result = String.valueOf(value);
        if (result.length() > count) {
            logger.error("value to long");
            result = result.substring(0, count);
        } else if (result.length() < count) {
            result = nul.substring(0, count - result.length()) + result;
        }
        return result;
    }
}
