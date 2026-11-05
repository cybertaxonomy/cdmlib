/**
 * Copyright (C) 2016 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.hibernate.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.jpa.ShiftConverter;
import eu.etaxonomy.cdm.model.molecular.SingleReadAlignment.Shift;

/**
 * @author a.mueller
 */
public class ShiftConverterTest {

    private ShiftConverter converter;

    @BeforeEach
    public void setUp() throws Exception {
        converter = new ShiftConverter();
    }

    @Test
    public void testConvertToDatabaseColumn() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertEquals("", converter.convertToDatabaseColumn(new Shift[0]));
        assertEquals("2,3", converter.convertToDatabaseColumn(new Shift[] { new Shift(2, 3) }));
        assertEquals("2,3;3,-1",
                converter.convertToDatabaseColumn(new Shift[] { new Shift(2, 3), new Shift(3, -1) }));
    }

    @Test
    public void testConvertToEntityAttribute() {
        Shift[] emptyResult = converter.convertToEntityAttribute("");
        assertEquals(0, emptyResult.length);
        Shift[] singleResult = converter.convertToEntityAttribute("2,2");
        assertEquals(1, singleResult.length);
        assertEquals(2, singleResult[0].position);
        assertEquals(2, singleResult[0].shift);
    }
}
