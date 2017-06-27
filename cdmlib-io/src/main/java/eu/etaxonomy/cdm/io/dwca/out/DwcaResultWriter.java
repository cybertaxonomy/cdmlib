/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * @author a.mueller
 * @date 25.06.2017
 *
 */
@Component
public class DwcaResultWriter extends DwcaExportBase {

    private static final long serialVersionUID = -1657568483721887287L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DwcaDescriptionExport.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheck(DwcaTaxExportState state) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInvoke(DwcaTaxExportState state) {
        state.getProcessor().createFinalResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(DwcaTaxExportState state) {
        return false;
    }

}
