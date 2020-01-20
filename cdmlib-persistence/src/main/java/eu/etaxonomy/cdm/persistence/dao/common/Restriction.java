/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import eu.etaxonomy.cdm.persistence.query.MatchMode;

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

    private List<T> values = null;

    public Restriction(){
        super();
    }

    /**
     * @param propertyName
     * @param matchMode is only applied if the <code>value</code> is a <code>String</code> object
     */
    public Restriction(String propertyName, MatchMode matchMode, T ... values ) {
        this(propertyName, Operator.AND, matchMode, values);
    }

    public Restriction(String propertyName, Operator operator, MatchMode matchMode, T ... values ) {
        this.propertyName = propertyName;
        this.operator = operator;
        if(values.length > 0){
            this.setValues(Arrays.asList(values));
            if(values[0] != null && values[0] instanceof String){
                this.matchMode = matchMode;
            }
        }
    }

    public String getPropertyName() {
        return propertyName;
    }
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public MatchMode getMatchMode() {
        return matchMode;
    }
    public void setMatchMode(MatchMode matchMode) {
        this.matchMode = matchMode;
    }

    /**
     * @return the values, never <code>null</code>
     */
    public List<T> getValues() {
        if(values == null){
            values = new ArrayList<>();
        }
        return values;
    }
    public void setValues(List<T> values) {
        this.values = values;
    }
    public void addValue(T value){
        getValues().add(value);
    }

    public Operator getOperator() {
        return operator;
    }
    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public boolean isNot() {
        return NOT_OPERATORS.contains(operator);
    }
}
