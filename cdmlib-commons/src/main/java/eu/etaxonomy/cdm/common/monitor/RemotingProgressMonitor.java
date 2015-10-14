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

import java.util.ArrayList;
import java.util.List;


/**
 * @author cmathew
 * @date 14 Oct 2015
 *
 */
public class RemotingProgressMonitor extends RestServiceProgressMonitor implements IRemotingProgressMonitor {

    private Object result;
    private List<String> reports = new ArrayList<String>();
    private String owner;


    /**
     * {@inheritDoc}
     */
    @Override
    public Object getResult() {
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setResult(Object result) {
        this.result = result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getReports() {
        return reports;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addReport(String report) {
        reports.add(report);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getOwner() {
        return owner;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

}
