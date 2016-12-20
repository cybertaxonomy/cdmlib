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
 * @date 31 Jul 2015
 *
 */
public class ExportResult implements Serializable {

    private boolean success;
    private List<byte[]> data;

    public ExportResult() {
        success = true;
        data = new ArrayList<byte[]>();
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
    public List<byte[]> getExportData() {
        return data;
    }
    /**
     * @param exportData the exportData to set
     */
    public void setExportData(List<byte[]> data) {
        this.data = data;
    }

    public void addExportData(byte[] exportData) {
        data.add(exportData);
    }

}
