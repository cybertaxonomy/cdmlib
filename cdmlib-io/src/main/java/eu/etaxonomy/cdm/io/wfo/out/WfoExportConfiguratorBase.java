/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.wfo.out;

import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.out.TaxonTreeExportConfiguratorBase;

/**
 * @author muellera
 * @since 20.03.2024
 */
public abstract class WfoExportConfiguratorBase<S extends WfoExportStateBase<C,S>,C extends WfoExportConfiguratorBase<S,C>>
        extends TaxonTreeExportConfiguratorBase<S,C>  {

    private static final long serialVersionUID = 5162242403155621130L;

    protected WfoExportConfiguratorBase(IExportTransformer transformer) {
        super(transformer);
    }

}
