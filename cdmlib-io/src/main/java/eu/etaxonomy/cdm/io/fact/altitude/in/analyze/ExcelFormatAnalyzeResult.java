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
 * @author a.mueller
 * @since 02.06.2020
 */
public class ExcelFormatAnalyzeResult {

    private List<FormatAnalyzeInfo> errors = new ArrayList<>();
    private List<FormatAnalyzeInfo> warnings = new ArrayList<>();
    private List<FormatAnalyzeInfo> infos = new ArrayList<>();

    public void addError(FormatAnalyzeInfo error) {
        errors.add(error);
    }

    public void addWarning(FormatAnalyzeInfo warning) {
        warnings.add(warning);
    }

    public void addInfo(FormatAnalyzeInfo info) {
        infos.add(info);
    }
}
