/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.altitude.in;

import eu.etaxonomy.cdm.io.fact.in.FactExcelImportStateBase;

/**
 * @author a.mueller
 * @since 28.05.2020
 */
public class AltitudeExcelImportState
        extends FactExcelImportStateBase<AltitudeExcelImportConfigurator>{

    public AltitudeExcelImportState(AltitudeExcelImportConfigurator config) {
        super(config);
    }
}
