/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeFromIndexedValueContext;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

import eu.etaxonomy.cdm.common.URI;

/**
 * @author a.mueller
 * @since 05.01.2021
 */
public class UriBridge implements ValueBridge<URI, String> {

    @Override
    public String toIndexedValue(URI value, ValueBridgeToIndexedValueContext context) {
        return value == null ? null : value.toString();
    }

    @Override
    public URI fromIndexedValue(String value, ValueBridgeFromIndexedValueContext context) {
        return value == null ? null : URI.create(value);
    }

    @Override
    public String parse(String value) {
        return value;
    }
}
