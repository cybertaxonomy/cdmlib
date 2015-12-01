// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format;

import eu.etaxonomy.cdm.format.ICdmFormatter.FormatKey;
import eu.etaxonomy.cdm.format.occurrences.DefaultCdmFormatter;
import eu.etaxonomy.cdm.format.occurrences.DerivedUnitFormatter;
import eu.etaxonomy.cdm.format.occurrences.FieldUnitFormatter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;

/**
 * @author pplitzner
 * @date Nov 30, 2015
 *
 */
public class CdmFormatter {

    private FormatKey[] formatKeys;

    public CdmFormatter(FormatKey... formatKeys) {
        this.formatKeys = formatKeys;
    }

    public String format(Object object){
        return format(object, formatKeys);
    }

    public String format(Object object, FormatKey... formatKeys){
        ICdmFormatter formatter = null;
        if(object instanceof CdmBase){
            CdmBase cdmBase = (CdmBase)object;
            if(cdmBase.isInstanceOf(DerivedUnit.class)){
                formatter = new DerivedUnitFormatter(object, formatKeys);
            }
            if(cdmBase.isInstanceOf(FieldUnit.class)){
                formatter = new FieldUnitFormatter(object, formatKeys);
            }
        }
        if(formatter==null){
            formatter = new DefaultCdmFormatter(object, formatKeys);
        }
        return formatter.format(object, formatKeys);
    }

}
