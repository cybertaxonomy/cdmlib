/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.hibernate.search;

import org.hibernate.search.bridge.StringBridge;

/**
 * @deprecated use HTMLStripStandardTokenizerFactory instead
 *
 */
@Deprecated
public class StripHtmlBridge implements StringBridge {

    public String objectToString(Object object) {
        if(object != null) {
          String string = (String) object;
          return string.replaceAll("\\<.*?\\>", "");
        } else {
          return null;
        }
    }

}