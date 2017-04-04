/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.distribution.excelupdate;

import java.util.HashMap;

import eu.etaxonomy.cdm.io.common.ImportResult;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.IoStateBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportState;
import eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase;

/**
 * This Import class updates existing distributions with the new state
 * described in the Excel file. It requires that the data was exported
 * before in the defined format.
 *
 * TODO where is the export to be found?
 *
 * This class is initiated by #6524
 *
 * @author a.mueller
 * @date 04.04.2017
 *
 */
public class ExcelDistributionUpdate extends ExcelImporterBase{

    private static final long serialVersionUID = 621338661492857764L;

    /**
     * {@inheritDoc}
     */
    @Override
    public ImportResult invoke(ImportStateBase state) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void analyzeRecord(HashMap record, ExcelImportState state) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void firstPass(ExcelImportState state) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void secondPass(ExcelImportState state) {
        // TODO Auto-generated method stub

    }



    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(IoStateBase state) {
        // TODO Auto-generated method stub
        return false;
    }


}
