/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.in;

import eu.etaxonomy.cdm.io.excel.common.ExcelImportState;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;

/**
 * @author a.mueller
 * @since 28.05.2020
 */
public abstract class FactExcelImportStateBase<CONFIG extends FactExcelImportConfiguratorBase>
        extends ExcelImportState<CONFIG, ExcelRowBase>{

    public FactExcelImportStateBase(CONFIG config) {
        super(config);
    }
}
