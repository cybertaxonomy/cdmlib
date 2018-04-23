/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Final result processor for Darwin Core Archive exports.
 *
 * @author a.mueller
 * @since 25.06.2017
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
    protected void doInvoke(DwcaTaxExportState state) {
        addReport(state);
        closeZip(state);
        state.getProcessor().createFinalResult();
    }

    /**
     * @param state
     */
    protected void addReport(DwcaTaxExportState state) {
        if (state.getConfig().getDestination() != null){
            try {
                StringBuffer report = state.getResult().createReport();
                PrintWriter writer = createPrintWriter(state, DwcaTaxExportFile.REPORT);
                writer.print(report.toString());
                writer.flush();

            } catch (IOException e) {
                state.getResult().addError("Unexpected exception when trying to add export report.");
            }
        }
    }

    /**
     * @param state
     */
    protected void closeZip(DwcaTaxExportState state) {
        if (state.isZip()){
            try {
                state.closeZip();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

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
    protected boolean isIgnore(DwcaTaxExportState state) {
        return false;
    }

}
