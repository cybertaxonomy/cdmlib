/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.hibernate.search;

import java.util.UUID;

import org.hibernate.search.bridge.StringBridge;

/**
 * @author andreas
 * @since Jun 19, 2012
 *
 */
public class UuidBridge implements StringBridge {

    public String objectToString(Object object) {
        if(object != null) {
            return ((UUID)object).toString();
        }
        return null;
    }

}
