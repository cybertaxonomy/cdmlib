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

import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;


/**
 * @author pplitzner
 * @date Nov 30, 2015
 *
 */
public class DerivedUnitFormatter extends AbstractCdmFormatter{

    public DerivedUnitFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys);
    }

    @Override
    protected void initFormatKeys(Object object, FormatKey... formatKeys) {
        super.initFormatKeys(object);
        DerivedUnit derivedUnit = (DerivedUnit)object;
        for (FormatKey formatKey : formatKeys) {
            switch (formatKey) {
            case ACCESSION_NUMBER:
                formatKeyMap.put(FormatKey.ACCESSION_NUMBER, derivedUnit.getAccessionNumber());
                break;

            default:
                break;
            }
        }
    }

}
