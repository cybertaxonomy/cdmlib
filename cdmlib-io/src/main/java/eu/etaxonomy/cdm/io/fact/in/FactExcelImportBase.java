/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.in;

import java.util.Map;

import eu.etaxonomy.cdm.io.excel.common.ExcelImportBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @since 28.05.2020
 */
public abstract class FactExcelImportBase<STATE extends FactExcelImportStateBase<CONFIG>, CONFIG extends FactExcelImportConfiguratorBase, ROW extends ExcelRowBase>
        extends ExcelImportBase<STATE, CONFIG, ExcelRowBase>{

    private static final long serialVersionUID = 2233954525898978414L;

    protected static final String COL_TAXON_UUID = "taxonUuid";
    protected static final String COL_NAME_CACHE = "nameCache";
    protected static final String COL_NAME_TITLE = "nameTitle";
    protected static final String COL_TAXON_TITLE = "taxonTitle";

    @Override
    protected void analyzeRecord(Map<String, String> record, STATE state) {
        // do nothing
    }

    @Override
    protected void firstPass(STATE state) {
        String line = "row " + state.getCurrentLine() + ": ";
        String linePure = "row " + state.getCurrentLine();
        System.out.println(linePure);

        //taxon
        Taxon taxon = getTaxonByCdmId(state, COL_TAXON_UUID,
                COL_NAME_CACHE, COL_NAME_TITLE, COL_TAXON_TITLE,
                Taxon.class, linePure);

        doFirstPass(state, taxon, line, linePure);
    }

    protected abstract void doFirstPass(STATE state, Taxon taxon, String line, String linePure);

    @Override
    protected void secondPass(STATE state) {
        //override if necessary
    }
}
