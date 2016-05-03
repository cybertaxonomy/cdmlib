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
public class DerivedUnitFormatter extends SpecimenOrObservationBaseFormatter{

    public DerivedUnitFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys);
    }

    @Override
    protected void initFormatKeys(Object object) {
        super.initFormatKeys(object);
        DerivedUnit derivedUnit = (DerivedUnit)object;
        for (FormatKey formatKey : formatKeys) {
            switch (formatKey) {
            case ACCESSION_NUMBER:
                formatKeyMap.put(FormatKey.ACCESSION_NUMBER, derivedUnit.getAccessionNumber());
                break;
            case BARCODE:
                formatKeyMap.put(FormatKey.BARCODE, derivedUnit.getBarcode());
                break;
            case CATALOG_NUMBER:
                formatKeyMap.put(FormatKey.CATALOG_NUMBER, derivedUnit.getCatalogNumber());
                break;
            case MOST_SIGNIFICANT_IDENTIFIER:
                formatKeyMap.put(FormatKey.MOST_SIGNIFICANT_IDENTIFIER, derivedUnit.getMostSignificantIdentifier());
                break;
            case COLLECTION_CODE:
                if(derivedUnit.getCollection()!=null){
                    formatKeyMap.put(FormatKey.COLLECTION_CODE, derivedUnit.getCollection().getCode());
                }
                break;
            case COLLECTION_NAME:
                if(derivedUnit.getCollection()!=null){
                    formatKeyMap.put(FormatKey.COLLECTION_NAME, derivedUnit.getCollection().getName());
                }
                break;

            default:
                break;
            }
        }
    }

}
