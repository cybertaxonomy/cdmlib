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

import org.joda.time.DateTime;
import org.junit.Test;

/**
 * @author a.kohlbecker
 * @since Jan 15, 2019
 *
 */
public class DateTimeJSONValueProcessorTest {

    @Test
    public void testFormatDateTime(){
        DateTimeJSONValueProcessor processor = new DateTimeJSONValueProcessor();
        DateTime dateTime = DateTime.parse("2010-06-30T01:20");
        assertEquals("2010-06-30T01:20:00.000+02:00", processor.formatDateTime(dateTime));
    }

    @Test
    public void testFormatDateTimeNull(){
        DateTimeJSONValueProcessor processor = new DateTimeJSONValueProcessor();
        assertNull(processor.formatDateTime(null));
    }

}
