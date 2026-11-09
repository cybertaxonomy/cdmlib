/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.hibernate.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.jpa.PartialConverter;

/**
 * @author a.mueller
 */
public class PartialConverterTest {

    private PartialConverter converter;

    @Before
    public void setUp() {
        converter = new PartialConverter();
    }

    @Test
    public void testRoundTripDate() {
        Partial partial = new Partial()
                .with(DateTimeFieldType.year(), 2008)
                .with(DateTimeFieldType.monthOfYear(), 11)
                .with(DateTimeFieldType.dayOfMonth(), 15);
        String db = converter.convertToDatabaseColumn(partial);
        assertEquals("20081115", db);
        Partial restored = converter.convertToEntityAttribute(db);
        assertEquals(partial, restored);
    }

    @Test
    public void testNullAndEmpty() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToEntityAttribute("00000000"));
    }
}
