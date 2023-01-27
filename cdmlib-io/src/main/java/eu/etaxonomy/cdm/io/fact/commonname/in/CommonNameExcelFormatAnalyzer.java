/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.commonname.in;

import eu.etaxonomy.cdm.io.fact.altitude.in.analyze.ExcelFormatAnalyzer;

/**
 * @author a.mueller
 * @since 24.01.2023
 */
public class CommonNameExcelFormatAnalyzer
            extends ExcelFormatAnalyzer<CommonNameExcelImportConfigurator> {

    protected CommonNameExcelFormatAnalyzer(CommonNameExcelImportConfigurator config) {
        super(config, requiredWorksheets(), requiredColumns(), optionalColumns(), optionalMultiColumns());
    }

    private static String[] requiredWorksheets() {
        return new String[]{"Altitude"};
    }

    private static String[] requiredColumns() {
        return new String[]{"taxonUuid", "Altitude Min", "Altitude Max"};
    }

    private static String[] optionalColumns() {
        return new String[]{"nameCache", "nameFullCache"};
    }
    private static String[] optionalMultiColumns() {
        return new String[]{};
    }
}