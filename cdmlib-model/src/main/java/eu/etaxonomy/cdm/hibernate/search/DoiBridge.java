/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

import eu.etaxonomy.cdm.common.DOI;

/**
 * @author a.mueller
 * @since Sep 05, 2013
 */
public class DoiBridge implements ValueBridge<DOI, String> {

    @Override
    public String toIndexedValue(DOI value, ValueBridgeToIndexedValueContext context) {
        return value == null ? null : value.toString();
    }

    @Override
    public String parse(String value) {
        return value;
    }
}
