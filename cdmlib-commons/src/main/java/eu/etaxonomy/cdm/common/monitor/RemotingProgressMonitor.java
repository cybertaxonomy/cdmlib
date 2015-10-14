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

/**
 * @author cmathew
 * @date 14 Oct 2015
 *
 */
public class RemotingProgressMonitor extends RestServiceProgressMonitor implements IRemotingProgressMonitor {

    private Object result;

    /**
     * @return the result
     */
    @Override
    public Object getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    @Override
    public void setResult(Object result) {
        this.result = result;
    }
}
