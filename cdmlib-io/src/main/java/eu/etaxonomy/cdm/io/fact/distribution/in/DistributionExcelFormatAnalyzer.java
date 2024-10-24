/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.distribution.in;

import eu.etaxonomy.cdm.io.fact.altitude.in.analyze.ExcelFormatAnalyzer;

/**
 * @author a.mueller
 * @since 08.10.2024
 */
public class DistributionExcelFormatAnalyzer
            extends ExcelFormatAnalyzer<DistributionExcelImportConfigurator> {

    protected DistributionExcelFormatAnalyzer(DistributionExcelImportConfigurator config) {
        super(config, requiredWorksheets(), requiredColumns(), optionalColumns(), optionalMultiColumns());
    }

    private static String[] requiredWorksheets() {
        return new String[]{"Distribution"};
    }

    private static String[] requiredColumns() {
        return new String[]{"taxonUuid", "Area", "Status"};
    }

    private static String[] optionalColumns() {
        return new String[]{"nameCache", "nameFullCache"};
    }
    private static String[] optionalMultiColumns() {
        return new String[]{};
    }
}