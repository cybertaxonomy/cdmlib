/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.altitude.in.analyze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author a.mueller
 * @since 02.06.2020
 */
public class ExcelFormatAnalyzer {

    private List<String> requiredColumns = new ArrayList<>();

    private List<String> optionalColumns = new ArrayList<>();

    private List<String> optionalMultiColumns = new ArrayList<>();

    public ExcelFormatAnalyzer(String[] requiredColumns, String[] optionalColumns,
            String[] optionalMultiColumns) {
        this.requiredColumns.addAll(Arrays.asList(requiredColumns));
        this.optionalColumns.addAll(Arrays.asList(optionalColumns));
        this.optionalMultiColumns.addAll(Arrays.asList(optionalMultiColumns));
    }

//******************* GETTER / SETTER ****************************/

    public List<String> getRequiredColumns() {
        return requiredColumns;
    }
    public void setRequiredColumns(List<String> requiredColumns) {
        this.requiredColumns = requiredColumns;
    }

    public List<String> getOptionalColumns() {
        return optionalColumns;
    }
    public void setOptionalColumns(List<String> optionalColumns) {
        this.optionalColumns = optionalColumns;
    }

    public List<String> getOptionalMultiColumns() {
        return optionalMultiColumns;
    }
    public void setOptionalMultiColumns(List<String> optionalMultiColumns) {
        this.optionalMultiColumns = optionalMultiColumns;
    }

//************************* METHOD ************************/

    public ExcelFormatAnalyzeResult invoke(){
        ExcelFormatAnalyzeResult result = new ExcelFormatAnalyzeResult();

        analyzeWorkbook(result);

        result.addError(FormatAnalyzeInfo.NewInstance("Obligatory column xxx is missing. Running import not possible"));
        return result;
    }

    private void analyzeWorkbook(ExcelFormatAnalyzeResult result) {
        // TODO Auto-generated method stub

    }
}
