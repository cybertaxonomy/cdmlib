/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.categorical.in;

import eu.etaxonomy.cdm.io.fact.altitude.in.analyze.ExcelFormatAnalyzer;

/**
 * @author a.mueller
 * @since 06.07.2020
 */
public class CategoricalDataExcelFormatAnalyzer
            extends ExcelFormatAnalyzer<CategoricalDataExcelImportConfigurator> {

    protected CategoricalDataExcelFormatAnalyzer(CategoricalDataExcelImportConfigurator config) {
        super(config, requiredWorksheets(), requiredColumns(), optionalColumns(), optionalMultiColumns());
    }

    private static String[] requiredWorksheets() {
        return new String[]{"Data","Vocabulary"};
    }

    private static String[] requiredColumns() {
        return new String[]{"taxonUuid", "xxx", "xxx"};
    }

    private static String[] optionalColumns() {
        return new String[]{"nameCache", "nameFullCache"};
    }
    private static String[] optionalMultiColumns() {
        return new String[]{};
    }


}
