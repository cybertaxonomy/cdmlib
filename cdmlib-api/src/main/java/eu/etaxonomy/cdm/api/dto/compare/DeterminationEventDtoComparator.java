/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.compare;

import java.util.Comparator;

import eu.etaxonomy.cdm.api.dto.DeterminationEventDTO;

/**
 * @author muellera
 * @since 13.02.2024
 */
public class DeterminationEventDtoComparator implements Comparator<DeterminationEventDTO> {

    @Override
    public int compare(DeterminationEventDTO o1, DeterminationEventDTO o2) {
        if (o1 == o2) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        if (o1.isPreferred()) {
            return 1;
        }else if (o2.isPreferred()) {
            return -1;
        }
        if (o1.getTimePeriod().checkEmpty() && o2.getTimePeriod().checkEmpty()) {
            return o1.compareTo(o2);
        }
        if (o1.getTimePeriod().checkEmpty() ) {
            return -1;
        }
        if (o2.getTimePeriod().checkEmpty()) {
            return 1;
        }
        return o2.getTimePeriod().getStart().compareTo(o1.getTimePeriod().getStart());
    }
}