/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.altitude.in.analyze;

/**
 * @author a.mueller
 * @since 02.06.2020
 */
public class FormatAnalyzeInfo {

    private AnalyzeResultSeverity severity;
    private String description;

    public static FormatAnalyzeInfo NewInstance(AnalyzeResultSeverity severity, String description) {
        FormatAnalyzeInfo result = new FormatAnalyzeInfo(severity, description);
        return result;
    }

// ************************* GETTER /SETTER **********************/

    private FormatAnalyzeInfo(AnalyzeResultSeverity severity, String description) {
        this.setSeverity(severity);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public AnalyzeResultSeverity getSeverity() {
        return severity;
    }
    public void setSeverity(AnalyzeResultSeverity severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        return severity.toString() + ":" + getDescription();
    }
}
