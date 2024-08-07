/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;

/**
 * @author a.mueller
 * @since 14.03.2017
 */
public abstract class SimpleImportConfiguratorBase<SOURCE extends Object>
        extends ImportConfiguratorBase<EmptyImportState<SimpleImportConfiguratorBase,?>, SOURCE>{
    private static final long serialVersionUID = 5310312930219876822L;

    protected SimpleImportConfiguratorBase(SOURCE source, ICdmDataSource destination, IInputTransformer transformer) {
        super(transformer);
        setSource(source);
        setDestination(destination);
    }

    @Override
    public EmptyImportState getNewState() {
        return new EmptyImportState(this);
    }
}