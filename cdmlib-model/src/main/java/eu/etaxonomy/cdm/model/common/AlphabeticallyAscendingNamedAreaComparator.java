// $Id$
/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.Comparator;

import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author freimeier
 \* @since 07.02.2018
 *
 */
public class AlphabeticallyAscendingNamedAreaComparator implements Comparator<NamedArea>{

    @Override
    public int compare(NamedArea o1, NamedArea o2) {
        return o2.compareTo(o1);
    }
}
