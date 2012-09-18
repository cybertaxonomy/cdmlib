// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.sdd.ikeyplus;

import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author andreas
 * @date Sep 18, 2012
 *
 */
public class IkeyPlusImportConfigurator extends ImportConfiguratorBase {

    public IkeyPlusImportConfigurator(IInputTransformer transformer) {
        super(transformer);
        // TODO Auto-generated constructor stub
    }

    @Override
    public <STATE extends ImportStateBase> STATE getNewState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void makeIoClassList() {
        // TODO Auto-generated method stub

    }

    @Override
    public Reference getSourceReference() {
        // TODO Auto-generated method stub
        return null;
    }

}
