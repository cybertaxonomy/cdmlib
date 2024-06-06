/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.wfo.out;

import eu.etaxonomy.cdm.io.out.TaxonTreeExportStateBase;

/**
 * @author muellera
 * @since 20.03.2024
 */
public abstract class WfoExportStateBase<C extends WfoExportConfiguratorBase<S,C>, S extends WfoExportStateBase<C,S>>
        extends TaxonTreeExportStateBase<C,S>{

    protected WfoExportStateBase(C config) {
        super(config);
    }

}
