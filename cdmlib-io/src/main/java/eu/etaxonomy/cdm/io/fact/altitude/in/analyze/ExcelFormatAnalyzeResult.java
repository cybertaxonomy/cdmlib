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
import java.util.List;

/**
 * Result for an {@link ExcelFormatAnalyzer}
 *
 * @author a.mueller
 * @since 02.06.2020
 */
public class ExcelFormatAnalyzeResult {

    private List<FormatAnalyzeInfo> fatalErrors = new ArrayList<>();
    private List<FormatAnalyzeInfo> errors = new ArrayList<>();
    private List<FormatAnalyzeInfo> warnings = new ArrayList<>();
    private List<FormatAnalyzeInfo> infos = new ArrayList<>();

    private ExcelFormatAnalyzer<?> analyzer;

    public ExcelFormatAnalyzeResult(ExcelFormatAnalyzer<?> excelFormatAnalyzer) {
        this.analyzer = excelFormatAnalyzer;
    }

    public void addFatalError(String description) {
        FormatAnalyzeInfo error = FormatAnalyzeInfo.NewInstance(AnalyzeResultSeverity.FATAL, description);
        fatalErrors.add(error);
    }

    public void addError(String description) {
        FormatAnalyzeInfo error = FormatAnalyzeInfo.NewInstance(AnalyzeResultSeverity.ERROR, description);
        errors.add(error);
    }

    public void addWarning(String description) {
        FormatAnalyzeInfo warning = FormatAnalyzeInfo.NewInstance(AnalyzeResultSeverity.ERROR, description);
        warnings.add(warning);
    }

    public void addInfo(String description) {
        FormatAnalyzeInfo info = FormatAnalyzeInfo.NewInstance(AnalyzeResultSeverity.ERROR, description);
        infos.add(info);
    }

    public boolean hasFatalErrors() {
        return !fatalErrors.isEmpty();
    }

    public boolean hasErrors() {
        return hasFatalErrors() || !errors.isEmpty() ;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(underline("\nAnalyze result"));
        //fatal
        int i = 1;
        result.append(hasFatalErrors()? underline("Fatal errors"): "No fatal errors.\n");
        for (FormatAnalyzeInfo info : fatalErrors){
            result.append(i + ": " + info.toString());
        }
        //error
        result.append(!errors.isEmpty()? underline("Errors"): "No errors.\n");
        for (FormatAnalyzeInfo info : errors){
            result.append(i + ": " + info.toString());
        }
        //warning
        result.append(!warnings.isEmpty()? underline("Warnings"): "No warnings.\n");
        for (FormatAnalyzeInfo info : errors){
            result.append(i + ": " + info.toString());
        }
        //info
        result.append(!infos.isEmpty()? underline("Infos"): "No infos.\n");
        for (FormatAnalyzeInfo info : infos){
            result.append(i + ": " + info.toString());
        }
        result.append("\nEnd analyze result.\n");

        return result.toString();
    }

    private StringBuilder underline(String string) {
        StringBuilder result = new StringBuilder(string.length()*2+3);
        result.append(string);
        result.append(":\n");
        for (@SuppressWarnings("unused") char c : string.toCharArray()){
            result.append("=");
        }
        result.append("\n");
        return result;
    }
}
