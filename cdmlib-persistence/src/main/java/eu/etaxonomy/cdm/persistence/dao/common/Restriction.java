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
import java.util.List;

import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * See <code>NameServiceImplTest.testFindByTitle()</code> for usage examples.
 *
 * @author a.kohlbecker
 * @since May 8, 2017
 *
 */
public class Restriction<T extends Object> {

    private String propertyName;

    private MatchMode matchMode;

    private List<T> values = null;

    /**
     * @param propertyName
     * @param matchMode is only applied if the <code>value</code> is a <code>String</code> object
     */
    public Restriction(String propertyName, MatchMode matchMode, T ... values ) {
        this.propertyName = propertyName;
        if(values.length > 0){
            this.setValues(Arrays.asList(values));
            if(values[0] != null && values[0] instanceof String){
                this.matchMode = matchMode;
            }
        }
    }

    /**
     * @return the propertyName
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @param propertyName the propertyName to set
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @return the matchMode
     */
    public MatchMode getMatchMode() {
        return matchMode;
    }

    /**
     * @param matchMode the matchMode to set
     */
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

    /**
     * @param values the values to set
     */
    public void setValues(List<T> values) {
        this.values = values;
    }

    /**
     *
     * @param value
     */
    public void addValue(T value){
        getValues().add(value);
    }


}
