/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

import eu.etaxonomy.cdm.model.common.WikiDataItemId;

/**
 * @author muellera
 * @since 17.10.2024
 */
public class WikiDataItemIdBridge implements ValueBridge<WikiDataItemId, String> {

    @Override
    public String toIndexedValue(WikiDataItemId value, ValueBridgeToIndexedValueContext context) {
        return value == null ? null : value.toString();
    }

    @Override
    public String parse(String value) {
        return value;
    }
}
