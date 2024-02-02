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

import eu.etaxonomy.cdm.io.common.ExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;

/**
 * @author a.mueller
 * @date 20.07.2023
 */
public abstract class TaxonTreeExportConfiguratorBase
           <STATE extends TaxonTreeExportStateBase<CONFIG,STATE>, CONFIG extends TaxonTreeExportConfiguratorBase<STATE, CONFIG>>
        extends ExportConfiguratorBase<STATE, IExportTransformer, File> {

    private static final long serialVersionUID = 1663876643435871032L;


    private boolean doSynonyms = true;

    protected TaxonTreeExportConfiguratorBase(IExportTransformer transformer) {
        super(transformer);
    }

    public boolean isDoSynonyms() {
        return doSynonyms;
    }
    public void setDoSynonyms(boolean doSynonyms) {
        this.doSynonyms = doSynonyms;
    }
}