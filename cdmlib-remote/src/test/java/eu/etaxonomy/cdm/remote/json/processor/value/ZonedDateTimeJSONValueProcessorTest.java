/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

import eu.etaxonomy.cdm.common.DateTimeUtil;

/**
 * @author a.kohlbecker
 * @since Jan 15, 2019
 */
public class ZonedDateTimeJSONValueProcessorTest {

    @Test
    public void testFormatDateTime(){
        DateTimeFormatter formatter = DateTimeUtil.FLEXIBEL_DATE_TIME_FORMATTER;
        ZonedDateTime dateTime = ZonedDateTime.parse("2010-06-30T01:20+02:00", formatter);
        ZonedDateTimeJSONValueProcessor processor = new ZonedDateTimeJSONValueProcessor();
        //Note: nanos are optional, maybe we should remove them from being obligatory here
        assertEquals("2010-06-30T01:20:00+02:00", processor.formatDateTime(dateTime));
    }

    @Test
    public void testFormatDateTimeNull(){
        ZonedDateTimeJSONValueProcessor processor = new ZonedDateTimeJSONValueProcessor();
        assertNull(processor.formatDateTime(null));
    }
}