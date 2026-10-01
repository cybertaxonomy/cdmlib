/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

/**
 * @deprecated use the solr HTMLStripStandardTokenizerFactory instead
 *  why is the solr implementation a better option?
 */
@Deprecated
public class StripHtmlBridge implements ValueBridge<String, String> {

    @Override
    public String toIndexedValue(String value, ValueBridgeToIndexedValueContext context) {
        return value == null ? null : value.replaceAll("\\<.*?\\>", "");
    }

    @Override
    public String parse(String value) {
        return value;
    }
}
