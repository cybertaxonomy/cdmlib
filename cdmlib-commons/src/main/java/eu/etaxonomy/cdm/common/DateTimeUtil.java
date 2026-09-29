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

/**
 * @author muellera
 * @since 27.05.2026
 */
public class DateTimeUtil {

    public static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

    public static final ZoneId UTC = ZoneOffset.UTC;


    public static ZonedDateTime of(int year, int month, int day) {
        return ZonedDateTime.of(year, month, day, 0, 0, 0, 0, BERLIN);
    }

}
