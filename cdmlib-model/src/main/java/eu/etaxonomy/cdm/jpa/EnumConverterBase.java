/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.jpa;

import eu.etaxonomy.cdm.model.term.IKeyTerm;
import jakarta.persistence.AttributeConverter;

/**
 * Base {@link AttributeConverter} for {@link IKeyTerm} enums persisted as VARCHAR keys.
 *
 * @param <E> enum type
 * @author muellera
 * @since 09.10.2026
 */
public abstract class EnumConverterBase<E extends Enum<E> & IKeyTerm>
        implements AttributeConverter<E, String> {

    private final Class<E> enumClass;

    protected EnumConverterBase(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.getKey();
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.getKey().equals(dbData)) {
                return enumConstant;
            }
        }
        throw new IllegalArgumentException("Unknown database value: " + dbData);

        //AI idea:
//        return (E) KeyTermLookup.getTerm(enumClass, dbData);
    }
}
