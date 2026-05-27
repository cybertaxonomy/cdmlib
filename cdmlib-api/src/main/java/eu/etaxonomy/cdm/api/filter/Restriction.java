/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * See <code>NameServiceImplTest.testFindByTitle()</code> for usage examples.
 *
 * @author a.kohlbecker
 * @since May 8, 2017
 */
public class Restriction<T extends Object> {

    public enum Operator {
        AND,
        OR,
        AND_NOT,
        OR_NOT;
    }

    private static final EnumSet<Operator> NOT_OPERATORS = EnumSet.of(Operator.AND_NOT, Operator.OR_NOT);

    private String propertyName;

    private MatchMode matchMode;

    private Operator operator = Operator.AND;

    private Collection<T> values = null;

    @SuppressWarnings("unused")
    private Restriction(){} //required for deserialization in RestrictionConverter

    /**
     * @param propertyName
     * @param matchMode is only applied if the <code>value</code> is a <code>String</code> object
     */
    public Restriction (String propertyName, MatchMode matchMode, T value) {
        this(propertyName, Operator.AND, matchMode, value);
    }
    public Restriction(String propertyName, MatchMode matchMode, T value1, T value2) {
        this(propertyName, Operator.AND, matchMode, value1, value2);
    }
    public Restriction(String propertyName, MatchMode matchMode, List<T> values) {
        this(propertyName, Operator.AND, matchMode, values);
    }
    public Restriction(String propertyName, MatchMode matchMode, T[] values) {
        this(propertyName, Operator.AND, matchMode, Arrays.asList(values));
    }
    public Restriction(String propertyName, MatchMode matchMode, EnumSet includedIn) {
        this(propertyName, Operator.AND, matchMode, (Collection)Arrays.asList(includedIn));
    }

    //with operator
    public Restriction(String propertyName, Operator operator, MatchMode matchMode, T value) {
        this(propertyName, operator, matchMode, Arrays.asList(value));
    }

    public Restriction(String propertyName, Operator operator, MatchMode matchMode, T value1, T value2) {
        this(propertyName, operator, matchMode, Arrays.asList(value1, value2));
    }

    public Restriction(String propertyName, Operator operator, EnumSet includedIn) {
        this(propertyName, operator, null, (Collection)Arrays.asList(includedIn));
    }

    public Restriction(String propertyName, Operator operator, MatchMode matchMode, Collection<T> values ) {
        this.propertyName = propertyName;
        this.operator = operator;
        if(!values.isEmpty()){
            this.setValues(values);
            T next = values.iterator().next();
            if(next != null && next instanceof String){
                this.matchMode = matchMode;
            }
        }
    }

    public String getPropertyName() {
        return propertyName;
    }

    public MatchMode getMatchMode() {
        return matchMode;
    }

    /**
     * @return the values, never <code>null</code>
     */
    public Collection<T> getValues() {
        if(values == null){
            values = new ArrayList<>();
        }
        return values;
    }
    public void setValues(Collection<T> values) {
        this.values = values;
    }

    public Operator getOperator() {
        return operator;
    }

    public boolean isNot() {
        return NOT_OPERATORS.contains(operator);
    }
}