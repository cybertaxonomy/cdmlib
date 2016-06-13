// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author a.kohlbecker
 * @date Jun 13, 2016
 *
 */
public class HHH_9751_UtilTest extends Assert {

    @Test
    public void testNullRemoval() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        list.add(null);
        list.add(null);
        list.add("3");

        int removed = HHH_9751_Util.removeAllNull(list);
        assertEquals(2, removed);
        assertEquals(3, list.size());
        assertNotNull(list.get(2));
    }

}
