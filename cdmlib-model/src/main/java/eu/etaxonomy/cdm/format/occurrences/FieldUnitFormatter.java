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

/**
 * @author pplitzner
 * @date Nov 30, 2015
 *
 */
public class FieldUnitFormatter extends AbstractCdmFormatter {

    public FieldUnitFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys);
    }

    @Override
    protected void initFormatKeys(Object object, FormatKey... formatKeys) {
        super.initFormatKeys(object);
        FieldUnit fieldUnit = (FieldUnit)object;
        if(fieldUnit.getGatheringEvent()!=null){
            formatKeyMap.put(FormatKey.LOCALITY_TEXT, fieldUnit.getGatheringEvent().getLocality().getText());
        }
    }

}
