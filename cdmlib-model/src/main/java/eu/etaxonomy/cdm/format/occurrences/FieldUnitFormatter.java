// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.occurrences;

import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;

/**
 * @author pplitzner
 * @date Nov 30, 2015
 *
 */
public class FieldUnitFormatter extends SpecimenOrObservationBaseFormatter {

    public FieldUnitFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys);
    }

    @Override
    protected void initFormatKeys(Object object, FormatKey... formatKeys) {
        super.initFormatKeys(object);
        FieldUnit fieldUnit = (FieldUnit)object;
        GatheringEvent gatheringEvent = fieldUnit.getGatheringEvent();
        if(gatheringEvent!=null){
            if(gatheringEvent.getLocality()!=null){
                formatKeyMap.put(FormatKey.GATHERING_LOCALITY_TEXT, gatheringEvent.getLocality().getText());
            }
            if(gatheringEvent.getCollector()!=null){
                formatKeyMap.put(FormatKey.GATHERING_COLLECTOR, gatheringEvent.getCollector().toString());
            }
            if(gatheringEvent.getCountry()!=null){
                formatKeyMap.put(FormatKey.GATHERING_COUNTRY, gatheringEvent.getCountry().getLabel());
            }
            if(gatheringEvent.getGatheringDate()!=null){
                formatKeyMap.put(FormatKey.GATHERING_DATE, gatheringEvent.getGatheringDate().toString());
            }
        }
        formatKeyMap.put(FormatKey.FIELD_NUMBER, fieldUnit.getFieldNumber());
    }

}
