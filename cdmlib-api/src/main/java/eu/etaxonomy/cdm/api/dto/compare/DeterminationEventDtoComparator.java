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
import eu.etaxonomy.cdm.compare.common.TimePeriodComparator;

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
        if (hasNoTimePeriod(o1) && hasNoTimePeriod(o2)) {
            return o1.compareTo(o2);
        }
        if (hasNoTimePeriod(o1)) {
            return -1;
        }
        if (hasNoTimePeriod(o2)) {
            return 1;
        }
        return TimePeriodComparator.INSTANCE().compare(o1.getTimePeriod(), o2.getTimePeriod());
    }

    private boolean hasNoTimePeriod(DeterminationEventDTO o1) {
        return o1.getTimePeriod() == null ? true : o1.getTimePeriod().checkEmpty();
    }
}