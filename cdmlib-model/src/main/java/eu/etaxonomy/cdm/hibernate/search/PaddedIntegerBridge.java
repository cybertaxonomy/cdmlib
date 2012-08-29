// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import java.util.Map;

import org.hibernate.search.bridge.TwoWayStringBridge;

/**
 * Implemented for to fix the bug #3058 "hibernate search: not all description elements found" (http://dev.e-taxonomy.eu/trac/ticket/3058)
 * TODO Once the cdm library is updated to hibernate 4.x this class becomes obiolete, and should be removed.
 *
 * @author andreas
 * @date Aug 29, 2012
 *
 */
public class PaddedIntegerBridge implements TwoWayStringBridge {


    public static String NULL_STRING = "null";

    private static int padding = String.valueOf(Integer.MAX_VALUE).length(); // default



    /* (non-Javadoc)
     * @see org.hibernate.search.bridge.StringBridge#objectToString(java.lang.Object)
     */
    @Override
    public String objectToString(Object object) {
        if(object == null){
            return NULL_STRING;
        }
        return paddInteger((Integer) object);
    }

    /**
     * @param object
     * @return
     */
    public static String paddInteger(Integer integer) {
        String rawInteger = integer.toString();
        if (rawInteger.length() > padding) {
            throw new IllegalArgumentException("Try to pad on a number too big");
        }
        StringBuilder paddedInteger = new StringBuilder();
        for (int padIndex = rawInteger.length(); padIndex < padding; padIndex++) {
            paddedInteger.append('0');
        }
        String returnString = paddedInteger.append(rawInteger).toString();
        return returnString;
    }

    /* (non-Javadoc)
     * @see org.hibernate.search.bridge.TwoWayStringBridge#stringToObject(java.lang.String)
     */
    @Override
    public Object stringToObject(String stringValue) {
        if(stringValue.equals(NULL_STRING)){
            return null;
        }
        return new Integer(stringValue);
    }

}
