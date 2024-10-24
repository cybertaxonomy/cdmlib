/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.hibernate.search.bridge.StringBridge;

import eu.etaxonomy.cdm.model.common.WikiDataItemId;

/**
 * @author muellera
 * @since 17.10.2024
 */
public class WikiDataItemIdBridge implements StringBridge {

    @Override
    public String objectToString(Object object) {
        if(object != null) {
            return ((WikiDataItemId)object).toString();
        }
        return null;
    }

}
