// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.monitor;

import java.util.List;

/**
 * @author cmathew
 * @date 14 Oct 2015
 *
 */
public interface IRemotingProgressMonitor extends IRestServiceProgressMonitor {

    /**
     * @return
     */
    public Object getResult();

    /**
     * @param result
     */
    public void setResult(Object result);

    /**
     * @return
     */
    public List<String> getReports();

    /**
     * @param report
     */
    public void addReport(String report);

    /**
     * @return
     */
    public String getOwner();

    /**
     * @param owner
     */
    public void setOwner(String owner);

}
