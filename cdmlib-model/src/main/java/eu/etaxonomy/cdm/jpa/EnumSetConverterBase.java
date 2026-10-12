/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.jpa;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.model.term.IKeyTerm;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Base converter for {@link EnumSet} of {@link IKeyTerm} enums, persisted as
 * {@code #key1#key2#} VARCHAR (same format as the former {@code EnumSetUserType}).
 *
 * @param <E> enum type
 * @author a.mueller
 * @since 25-02-2019
 */
public abstract class EnumSetConverterBase<E extends Enum<E> & IKeyTerm>
        implements AttributeConverter<EnumSet<E>, String> {

    private static final String SEP = "#";

    private final Class<E> enumClass;

    protected EnumSetConverterBase(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(EnumSet<E> attribute) {
        if (attribute == null) {
            return null;
        }
        StringBuilder key = new StringBuilder(SEP);
        for (E e : attribute) {
            key.append(e.getKey()).append(SEP);
        }
        return key.toString();
    }

    @Override
    public EnumSet<E> convertToEntityAttribute(String dbData) {
        if (StringUtils.isBlank(dbData) || dbData.equals(SEP)) {
            return EnumSet.noneOf(enumClass);
        }

        Set<E> resultSet = EnumSet.noneOf(enumClass);
        String[] keys = dbData.split(SEP);

        for (String key : keys) {
           if (key.isEmpty()) {
               continue;
           }
           for (E enumConstant : enumClass.getEnumConstants()) {
               if (enumConstant.getKey().equals(dbData)) {
                   resultSet.add(enumConstant);
                   break;
               }
           }
        }

        return EnumSet.copyOf(resultSet);

        //AI version:

//        EnumSet<E> result = EnumSet.noneOf(enumClass);
//        if (dbData == null) {
//            return result;
//        }
//        String[] splits = dbData.split(SEP);
//        for (String split : splits) {
//            if (StringUtils.isNotEmpty(split)) {
//                E term = (E) KeyTermLookup.getTerm(enumClass, split);
//                if (term == null) {
//                    throw new IllegalArgumentException(
//                            split + " is not a valid key value for enumeration " + enumClass.getCanonicalName());
//                }
//                result.add(term);
//            }
//        }
//        return result;
    }

    // ******** FOR QUERIES ********* //

    public static <F extends Enum<F>> Predicate hasEnumValue(F enumValue, CriteriaBuilder cb, Root<?> root) {
        if (!(enumValue instanceof IKeyTerm)) {
            throw new IllegalArgumentException(
                    "Enum is not of type IKeyTerm and can not be handled by EnumSet converter");
        }
        IKeyTerm keyValue = (IKeyTerm) enumValue;
        return cb.like(root.get("types"), "%#" + keyValue.getKey() + "#%");
    }

    public static <F extends Enum<F>> Predicate hasAllEnumValues(Set<F> enumValues, CriteriaBuilder cb, Root<?> root) {
        List<Predicate> predicates = enumValues.stream()
                .map(enumValue -> hasEnumValue(enumValue, cb, root))
                .collect(Collectors.toList());
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    //FROM first try:

    //TODO copied from old: EnumSetUserType, not yet adjusted

    //******** FOR QUERIES *********//

    //Note: in future maybe better use spring-data Specification.toPredicate(...) (e.g. https://docs.spring.io/spring-data/jpa/docs/current/api/org/springframework/data/jpa/domain/Specification.html)

//    public static <F extends Enum<F>> Predicate hasEnumValue(F enumValue, CriteriaBuilder cb, Root<?> root) {
//        if (! (enumValue instanceof IKeyTerm)) {
//            throw new IllegalArgumentException("Enum is not of type IKeyTerm and can not be handled by EnumSetUserType");
//        }
//        IKeyTerm keyValue = (IKeyTerm)enumValue;
//        Predicate p = cb.like(root.get("types"), "%#"+keyValue.getKey()+"#%");
//        return p;
//    }
//
//    public static <F extends Enum<F>> Predicate hasAllEnumValues(Set<F> enumValues, CriteriaBuilder cb, Root<?> root) {
//        List<Predicate> predicates = enumValues.stream()
//                .map(enumValue -> hasEnumValue(enumValue, cb, root))
//                .collect(Collectors.toList());
//        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
//        return cb.and(predicateArray);
//    }
}
