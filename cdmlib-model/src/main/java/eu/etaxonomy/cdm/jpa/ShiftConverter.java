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

import eu.etaxonomy.cdm.model.molecular.SingleReadAlignment.Shift;

/**
 * Converter for arrays of {@link Shift} persisted as CLOB/VARCHAR
 * ({@code position,shift;position,shift;...}).
 *
 * @author a.mueller
 * @since 03.12.2014
 */
@Converter
public class ShiftConverter implements AttributeConverter<Shift[], String> {

    private static final String SHIFT_SEPARATOR = ";";
    private static final String ATTR_SEPARATOR = ",";

    @Override
    public String convertToDatabaseColumn(Shift[] attribute) {
        if (attribute == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (Shift shift : attribute) {
            if (shift != null) {
                if (result.length() > 0) {
                    result.append(SHIFT_SEPARATOR);
                }
                result.append(shift.position).append(ATTR_SEPARATOR).append(shift.shift);
            }
        }
        return result.toString();
    }

    @Override
    public Shift[] convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        if (dbData.length() == 0) {
            return new Shift[0];
        }
        String[] splits = dbData.split(SHIFT_SEPARATOR);
        Shift[] result = new Shift[splits.length];
        for (int i = 0; i < splits.length; i++) {
            result[i] = new Shift();
            String[] split = splits[i].split(ATTR_SEPARATOR);
            result[i].position = Integer.valueOf(split[0]);
            result[i].shift = Integer.valueOf(split[1]);
        }
        return result;
    }
}
