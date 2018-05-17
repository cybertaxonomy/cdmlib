/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;

/**
 * @author pplitzner
 * @since 16.06.2015
 *
 */
public class AbcdImportUtility {

    public static String getUnitID(DerivedUnit derivedUnit, Abcd206ImportConfigurator config){
        if(config.isMapUnitIdToAccessionNumber()){
            return derivedUnit.getAccessionNumber();
        }
        if(config.isMapUnitIdToBarcode()){
            return derivedUnit.getBarcode();
        }
        return derivedUnit.getCatalogNumber();
    }

    public static void setUnitID(DerivedUnit derivedUnit, String unitId, Abcd206ImportConfigurator config){
        if(config.isMapUnitIdToCatalogNumber()
                || !(config.isMapUnitIdToAccessionNumber() || config.isMapUnitIdToBarcode() || config.isMapUnitIdToCatalogNumber())){
            // set catalog number (default if nothing is set)
            derivedUnit.setCatalogNumber(unitId);
        }
        if(config.isMapUnitIdToAccessionNumber()){
            derivedUnit.setAccessionNumber(unitId);
        }
        if(config.isMapUnitIdToBarcode()){
            derivedUnit.setBarcode(unitId);
        }
    }

}
