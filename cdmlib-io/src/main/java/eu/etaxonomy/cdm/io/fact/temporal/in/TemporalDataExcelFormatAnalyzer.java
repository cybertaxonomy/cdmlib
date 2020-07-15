/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.temporal.in;

import eu.etaxonomy.cdm.io.fact.altitude.in.analyze.ExcelFormatAnalyzer;

/**
 * Analyzer for taxon based temporal data import.
 *
 * TODO not yet implemented
 *
 * @author a.mueller
 * @since 15.07.2020
 */
public class TemporalDataExcelFormatAnalyzer<CONFIG extends TemporalDataExcelImportConfigurator<?>>
            extends ExcelFormatAnalyzer<CONFIG> {

    public TemporalDataExcelFormatAnalyzer(CONFIG config,
            String[] requiredWorksheets,
            String[] requiredColumns,
            String[] optionalColumns,
            String[] optionalMultiColumns) {
        super(config, requiredWorksheets, requiredColumns, optionalColumns, optionalMultiColumns);
    }

    protected TemporalDataExcelFormatAnalyzer(CONFIG config) {
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
