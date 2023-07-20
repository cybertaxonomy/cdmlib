/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.out;

import java.io.File;

import eu.etaxonomy.cdm.io.common.ExportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;

/**
 * @author a.mueller
 * @date 20.07.2023
 */
public class TaxonTreeExportStateBase
            <CONFIG extends TaxonTreeExportConfiguratorBase<STATE,CONFIG>, STATE extends TaxonTreeExportStateBase<CONFIG, STATE>>
        extends ExportStateBase<CONFIG, IExportTransformer, File> {

    protected TaxonTreeExportStateBase(CONFIG config) {
        super(config);
//        result = ExportResult.NewInstance(config.getResultType());
    }
}
