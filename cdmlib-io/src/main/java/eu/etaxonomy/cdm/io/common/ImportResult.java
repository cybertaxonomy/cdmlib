// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cmathew
 * @date 5 Aug 2015
 *
 */
public class ImportResult implements Serializable {

    private boolean success;
    private List<byte[]> reports;

    public ImportResult() {
        success = true;
        reports = new ArrayList<byte[]>();
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }
    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    /**
     * @return the exportData
     */
    public List<byte[]> getReports() {
        return reports;
    }
    /**
     * @param exportData the exportData to set
     */
    public void setReports(List<byte[]> reports) {
        this.reports = reports;
    }

    public void addReport(byte[] report) {
        reports.add(report);
    }
}
