/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.taxonx2013;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;

/**
 * @author p.kelbert
 * @created 2012
 */
public class TaxonXImportState
        extends  ImportStateBase<TaxonXImportConfigurator, CdmImportBase<TaxonXImportConfigurator , TaxonXImportState >>{

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TaxonXImportState.class);

    public TaxonXImportState(TaxonXImportConfigurator config) {
        super(config);
    }

}
