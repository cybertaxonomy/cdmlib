/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.altitude.in.analyze;

import java.io.FileNotFoundException;
import java.io.InputStream;
import eu.etaxonomy.cdm.common.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.io.fact.in.FactExcelImportConfiguratorBase;

/**
 * @author a.mueller
 * @since 02.06.2020
 */
public class ExcelFormatAnalyzer<CONFIG extends FactExcelImportConfiguratorBase<?>> {

    private CONFIG config;

    private List<String> requiredWorksheets = new ArrayList<>();

    private List<String> requiredColumns = new ArrayList<>();

    private List<String> optionalColumns = new ArrayList<>();

    private List<String> optionalMultiColumns = new ArrayList<>();

    public ExcelFormatAnalyzer(CONFIG config,
            String[] requiredWorksheets,
            String[] requiredColumns,
            String[] optionalColumns,
            String[] optionalMultiColumns) {
        this.config = config;
        this.requiredWorksheets.addAll(Arrays.asList(requiredWorksheets));
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
        ExcelFormatAnalyzeResult result = new ExcelFormatAnalyzeResult(this);

        //workbook format
        analyzeWorkbookFormat(result);
        if (result.hasFatalErrors()){
            return result;
        }

//        result.addError("Obligatory column xxx is missing. Running import not possible");
        return result;
    }

    protected void analyzeWorkbookFormat(ExcelFormatAnalyzeResult result) {
        URI uri = config.getSource();
        if (uri == null){
            result.addFatalError("Now source defined. Import not possible.");
            return;
        }
        try {
            InputStream stream = UriUtils.getInputStream(uri);
            Workbook wb = WorkbookFactory.create(stream);

            List<String> worksheetNames = new ArrayList<>();
            worksheetNames.add(config.getWorksheetName());
            for (String worksheetName : worksheetNames){
                analyzeWorksheetName(result, wb, worksheetNames, worksheetName);
            }
        } catch(FileNotFoundException fne) {
            result.addFatalError("Import file '" + uri.toString() + "' not found. Import not possible.");
        } catch(EncryptedDocumentException ede) {
            result.addFatalError("File is encrypted. Import not possible.");
        } catch(Exception ioe) {
            result.addFatalError("Unhandled exception  when reading '" + uri.toString() + "'. Import not possible.");
        }
    }

    private void analyzeWorksheetName(ExcelFormatAnalyzeResult result, Workbook wb, List<String> worksheetNames,
            String worksheetName) {
        try {
            Sheet worksheet = wb.getSheet(worksheetName);
            if(worksheet == null && worksheetNames.size() == 1){
                worksheet = wb.getSheetAt(0);
            }
            if (worksheet == null){
                result.addFatalError("Required worksheet "+worksheetName+" not found in file. Import not possible.");
            }
        } catch (Exception e) {
            result.addFatalError("Error when reading worksheet '" + worksheetName + "' not found. Import not possible.");
        }
    }
}
