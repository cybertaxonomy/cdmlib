/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.distribution.excelupdate;

import eu.etaxonomy.cdm.io.excel.common.ExcelImportState;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;

/**
 * @author a.mueller
 * @date 06.04.2017
 */
public class ExcelDistributionUpdateState
    extends ExcelImportState<ExcelDistributionUpdateConfigurator, ExcelRowBase>{

    /**
     * @param config
     */
    public ExcelDistributionUpdateState(ExcelDistributionUpdateConfigurator config) {
        super(config);
    }

}
