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

    private String description;

    public static FormatAnalyzeInfo NewInstance(String description) {
        FormatAnalyzeInfo result = new FormatAnalyzeInfo();
        result.description = description;
        return result;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }



}
