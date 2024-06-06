/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.occurrence;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.compare.common.TimePeriodComparator;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * @author muellera
 * @since 21.02.2024
 */
@SuppressWarnings("rawtypes")
public class SpecimenOrObservationBaseComparator implements Comparator<SpecimenOrObservationBase> {

    private static final SpecimenOrObservationBaseComparator instance = new SpecimenOrObservationBaseComparator();

    public static final SpecimenOrObservationBaseComparator INSTANCE() {
        return instance;
    }

    @Override
    public int compare(SpecimenOrObservationBase o1, SpecimenOrObservationBase o2) {
        if(o1 instanceof FieldUnit && o2 instanceof FieldUnit) {
            FieldUnit fu1 = (FieldUnit)o1;
            FieldUnit fu2 = (FieldUnit)o2;
            TimePeriod tp1 = fu1.getGatheringEvent() == null ? null : fu1.getGatheringEvent().getTimeperiod();
            TimePeriod tp2 = fu2.getGatheringEvent() == null ? null : fu2.getGatheringEvent().getTimeperiod();

            boolean nullFirst = false;
            TimePeriodComparator comparator = TimePeriodComparator.INSTANCE(nullFirst);
            return comparator.compare(tp1, tp2);
        }
        if(o1 instanceof DerivedUnit && o2 instanceof DerivedUnit) {
            SpecimenOrObservationBase<?> du1 = o1;
            SpecimenOrObservationBase<?> du2 = o2;
            return StringUtils.compare(du1.getTitleCache(), du2.getTitleCache());
        }
        if(o1 instanceof FieldUnit && o2 instanceof DerivedUnit) {
            return -1;
        } else {
            return 1;
        }
    }
}